/*
  Stockfish, a UCI chess playing engine derived from Glaurung 2.1
  Copyright (C) 2004-2025 The Stockfish developers (see AUTHORS file)

  Stockfish is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Stockfish is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

#ifndef NUMA_H_INCLUDED
#define NUMA_H_INCLUDED

#include <algorithm>
#include <atomic>
#include <cstdint>
#include <cstdlib>
#include <functional>
#include <iostream>
#include <limits>
#include <map>
#include <memory>
#include <mutex>
#include <set>
#include <sstream>
#include <string>
#include <thread>
#include <utility>
#include <vector>
#include <cstring>

#include "sf_memory.h"
#include "sf_misc.h"

#include "helper.h"
#include "engine.h"

namespace Stockfish {

using CpuIndex  = size_t;
using NumaIndex = size_t;

inline CpuIndex get_hardware_concurrency() {
    CpuIndex concurrency = std::thread::hardware_concurrency();
        
    return concurrency;
}

inline const CpuIndex SYSTEM_THREADS_NB = std::max<CpuIndex>(1, get_hardware_concurrency());



// We want to abstract the purpose of storing the numa node index somewhat.
// Whoever is using this does not need to know the specifics of the replication
// machinery to be able to access NUMA replicated memory.
class NumaReplicatedAccessToken {
   public:
    NumaReplicatedAccessToken() :
        n(0) {}

    explicit NumaReplicatedAccessToken(NumaIndex idx) :
        n(idx) {}

    NumaIndex get_numa_index() const { return n; }

   private:
    NumaIndex n;
};

// Designed as immutable, because there is no good reason to alter an already
// existing config in a way that doesn't require recreating it completely, and
// it would be complex and expensive to maintain class invariants.
// The CPU (processor) numbers always correspond to the actual numbering used
// by the system. The NUMA node numbers MAY NOT correspond to the system's
// numbering of the NUMA nodes. In particular, empty nodes may be removed, or
// the user may create custom nodes. It is guaranteed that NUMA nodes are NOT
// empty: every node exposed by NumaConfig has at least one processor assigned.
//
// We use startup affinities so as not to modify its own behaviour in time.
//
// Since Stockfish doesn't support exceptions all places where an exception
// should be thrown are replaced by std::exit.
class NumaConfig {
   public:
    NumaConfig() :
        highestCpuIndex(0),
        customAffinity(false) {
        const auto numCpus = SYSTEM_THREADS_NB;
        add_cpu_range_to_node(NumaIndex{0}, CpuIndex{0}, numCpus - 1);
    }

    // This function queries the system for the mapping of processors to NUMA nodes.
    // On Linux we read from standardized kernel sysfs, with a fallback to single NUMA
    // node. On Windows we utilize GetNumaProcessorNodeEx, which has its quirks, see
    // comment for Windows implementation of get_process_affinity.
    static NumaConfig from_system([[maybe_unused]] bool respectProcessAffinity = true) {
        NumaConfig cfg = empty();


        // Fallback for unsupported systems.
        for (CpuIndex c = 0; c < SYSTEM_THREADS_NB; ++c)
            cfg.add_cpu_to_node(NumaIndex{0}, c);


        // We have to ensure no empty NUMA nodes persist.
        cfg.remove_empty_numa_nodes();

        // If the user explicitly opts out from respecting the current process affinity
        // then it may be inconsistent with the current affinity (obviously), so we
        // consider it custom.
        if (!respectProcessAffinity)
            cfg.customAffinity = true;

        return cfg;
    }

    // ':'-separated numa nodes
    // ','-separated cpu indices
    // supports "first-last" range syntax for cpu indices
    // For example "0-15,128-143:16-31,144-159:32-47,160-175:48-63,176-191"
    static NumaConfig from_string(const std::string& s) {
        NumaConfig cfg = empty();

        NumaIndex n = 0;
        for (auto&& nodeStr : split(s, ":"))
        {
            auto indices = indices_from_shortened_string(std::string(nodeStr));
            if (!indices.empty())
            {
                for (auto idx : indices)
                {
                    if (!cfg.add_cpu_to_node(n, CpuIndex(idx)))
                        // Karuah Chess - lock engine without exiting
                        KaruahChess::Engine::engineErr.add(KaruahChess::helper::NUMA_A_ERROR);
                }

                n += 1;
            }
        }

        cfg.customAffinity = true;

        return cfg;
    }

    NumaConfig(const NumaConfig&)            = delete;
    NumaConfig(NumaConfig&&)                 = default;
    NumaConfig& operator=(const NumaConfig&) = delete;
    NumaConfig& operator=(NumaConfig&&)      = default;

    bool is_cpu_assigned(CpuIndex n) const { return nodeByCpu.count(n) == 1; }

    NumaIndex num_numa_nodes() const { return nodes.size(); }

    CpuIndex num_cpus_in_numa_node(NumaIndex n) const {
        assert(n < nodes.size());
        return nodes[n].size();
    }

    CpuIndex num_cpus() const { return nodeByCpu.size(); }

    bool requires_memory_replication() const { return customAffinity || nodes.size() > 1; }

    std::string to_string() const {
        std::string str;

        bool isFirstNode = true;
        for (auto&& cpus : nodes)
        {
            if (!isFirstNode)
                str += ":";

            bool isFirstSet = true;
            auto rangeStart = cpus.begin();
            for (auto it = cpus.begin(); it != cpus.end(); ++it)
            {
                auto next = std::next(it);
                if (next == cpus.end() || *next != *it + 1)
                {
                    // cpus[i] is at the end of the range (may be of size 1)
                    if (!isFirstSet)
                        str += ",";

                    const CpuIndex last = *it;

                    if (it != rangeStart)
                    {
                        const CpuIndex first = *rangeStart;

                        str += std::to_string(first);
                        str += "-";
                        str += std::to_string(last);
                    }
                    else
                        str += std::to_string(last);

                    rangeStart = next;
                    isFirstSet = false;
                }
            }

            isFirstNode = false;
        }

        return str;
    }

    bool suggests_binding_threads(CpuIndex numThreads) const {
        // If we can reasonably determine that the threads cannot be contained
        // by the OS within the first NUMA node then we advise distributing
        // and binding threads. When the threads are not bound we can only use
        // NUMA memory replicated objects from the first node, so when the OS
        // has to schedule on other nodes we lose performance. We also suggest
        // binding if there's enough threads to distribute among nodes with minimal
        // disparity. We try to ignore small nodes, in particular the empty ones.

        // If the affinity set by the user does not match the affinity given by
        // the OS then binding is necessary to ensure the threads are running on
        // correct processors.
        if (customAffinity)
            return true;

        // We obviously cannot distribute a single thread, so a single thread
        // should never be bound.
        if (numThreads <= 1)
            return false;

        size_t largestNodeSize = 0;
        for (auto&& cpus : nodes)
            if (cpus.size() > largestNodeSize)
                largestNodeSize = cpus.size();

        auto is_node_small = [largestNodeSize](const std::set<CpuIndex>& node) {
            static constexpr double SmallNodeThreshold = 0.6;
            return static_cast<double>(node.size()) / static_cast<double>(largestNodeSize)
                <= SmallNodeThreshold;
        };

        size_t numNotSmallNodes = 0;
        for (auto&& cpus : nodes)
            if (!is_node_small(cpus))
                numNotSmallNodes += 1;

        return (numThreads > largestNodeSize / 2 || numThreads >= numNotSmallNodes * 4)
            && nodes.size() > 1;
    }

    std::vector<NumaIndex> distribute_threads_among_numa_nodes(CpuIndex numThreads) const {
        std::vector<NumaIndex> ns;

        if (nodes.size() == 1)
        {
            // Special case for when there's no NUMA nodes. This doesn't buy us
            // much, but let's keep the default path simple.
            ns.resize(numThreads, NumaIndex{0});
        }
        else
        {
            std::vector<size_t> occupation(nodes.size(), 0);
            for (CpuIndex c = 0; c < numThreads; ++c)
            {
                NumaIndex bestNode{0};
                float     bestNodeFill = std::numeric_limits<float>::max();
                for (NumaIndex n = 0; n < nodes.size(); ++n)
                {
                    float fill =
                      static_cast<float>(occupation[n] + 1) / static_cast<float>(nodes[n].size());
                    // NOTE: Do we want to perhaps fill the first available node
                    //       up to 50% first before considering other nodes?
                    //       Probably not, because it would interfere with running
                    //       multiple instances. We basically shouldn't favor any
                    //       particular node.
                    if (fill < bestNodeFill)
                    {
                        bestNode     = n;
                        bestNodeFill = fill;
                    }
                }
                ns.emplace_back(bestNode);
                occupation[bestNode] += 1;
            }
        }

        return ns;
    }

    NumaReplicatedAccessToken bind_current_thread_to_numa_node(NumaIndex n) const {
        if (n >= nodes.size() || nodes[n].size() == 0)
            // Karuah Chess - lock engine without exiting
            KaruahChess::Engine::engineErr.add(KaruahChess::helper::NUMA_B_ERROR);

        return NumaReplicatedAccessToken(n);
    }

    template<typename FuncT>
    void execute_on_numa_node(NumaIndex n, FuncT&& f) const {
        std::thread th([this, &f, n]() {
            bind_current_thread_to_numa_node(n);
            std::forward<FuncT>(f)();
        });

        th.join();
    }

   private:
    std::vector<std::set<CpuIndex>> nodes;
    std::map<CpuIndex, NumaIndex>   nodeByCpu;
    CpuIndex                        highestCpuIndex;

    bool customAffinity;

    static NumaConfig empty() { return NumaConfig(EmptyNodeTag{}); }

    struct EmptyNodeTag {};

    NumaConfig(EmptyNodeTag) :
        highestCpuIndex(0),
        customAffinity(false) {}

    void remove_empty_numa_nodes() {
        std::vector<std::set<CpuIndex>> newNodes;
        for (auto&& cpus : nodes)
            if (!cpus.empty())
                newNodes.emplace_back(std::move(cpus));
        nodes = std::move(newNodes);
    }

    // Returns true if successful
    // Returns false if failed, i.e. when the cpu is already present
    //                          strong guarantee, the structure remains unmodified
    bool add_cpu_to_node(NumaIndex n, CpuIndex c) {
        if (is_cpu_assigned(c))
            return false;

        while (nodes.size() <= n)
            nodes.emplace_back();

        nodes[n].insert(c);
        nodeByCpu[c] = n;

        if (c > highestCpuIndex)
            highestCpuIndex = c;

        return true;
    }

    // Returns true if successful
    // Returns false if failed, i.e. when any of the cpus is already present
    //                          strong guarantee, the structure remains unmodified
    bool add_cpu_range_to_node(NumaIndex n, CpuIndex cfirst, CpuIndex clast) {
        for (CpuIndex c = cfirst; c <= clast; ++c)
            if (is_cpu_assigned(c))
                return false;

        while (nodes.size() <= n)
            nodes.emplace_back();

        for (CpuIndex c = cfirst; c <= clast; ++c)
        {
            nodes[n].insert(c);
            nodeByCpu[c] = n;
        }

        if (clast > highestCpuIndex)
            highestCpuIndex = clast;

        return true;
    }

    static std::vector<size_t> indices_from_shortened_string(const std::string& s) {
        std::vector<size_t> indices;

        if (s.empty())
            return indices;

        for (const auto& ss : split(s, ","))
        {
            if (ss.empty())
                continue;

            auto parts = split(ss, "-");
            if (parts.size() == 1)
            {
                const CpuIndex c = CpuIndex{str_to_size_t(std::string(parts[0]))};
                indices.emplace_back(c);
            }
            else if (parts.size() == 2)
            {
                const CpuIndex cfirst = CpuIndex{str_to_size_t(std::string(parts[0]))};
                const CpuIndex clast  = CpuIndex{str_to_size_t(std::string(parts[1]))};
                for (size_t c = cfirst; c <= clast; ++c)
                {
                    indices.emplace_back(c);
                }
            }
        }

        return indices;
    }
};

class NumaReplicationContext;

// Instances of this class are tracked by the NumaReplicationContext instance.
// NumaReplicationContext informs all tracked instances when NUMA configuration changes.
class NumaReplicatedBase {
   public:
    NumaReplicatedBase(NumaReplicationContext& ctx);

    NumaReplicatedBase(const NumaReplicatedBase&) = delete;
    NumaReplicatedBase(NumaReplicatedBase&& other) noexcept;

    NumaReplicatedBase& operator=(const NumaReplicatedBase&) = delete;
    NumaReplicatedBase& operator=(NumaReplicatedBase&& other) noexcept;

    virtual void on_numa_config_changed() = 0;
    virtual ~NumaReplicatedBase();

    const NumaConfig& get_numa_config() const;

   private:
    NumaReplicationContext* context;
};

// We force boxing with a unique_ptr. If this becomes an issue due to added
// indirection we may need to add an option for a custom boxing type. When the
// NUMA config changes the value stored at the index 0 is replicated to other nodes.
template<typename T>
class NumaReplicated: public NumaReplicatedBase {
   public:
    using ReplicatorFuncType = std::function<T(const T&)>;

    NumaReplicated(NumaReplicationContext& ctx) :
        NumaReplicatedBase(ctx) {
        replicate_from(T{});
    }

    NumaReplicated(NumaReplicationContext& ctx, T&& source) :
        NumaReplicatedBase(ctx) {
        replicate_from(std::move(source));
    }

    NumaReplicated(const NumaReplicated&) = delete;
    NumaReplicated(NumaReplicated&& other) noexcept :
        NumaReplicatedBase(std::move(other)),
        instances(std::exchange(other.instances, {})) {}

    NumaReplicated& operator=(const NumaReplicated&) = delete;
    NumaReplicated& operator=(NumaReplicated&& other) noexcept {
        NumaReplicatedBase::operator=(*this, std::move(other));
        instances = std::exchange(other.instances, {});

        return *this;
    }

    NumaReplicated& operator=(T&& source) {
        replicate_from(std::move(source));

        return *this;
    }

    ~NumaReplicated() override = default;

    const T& operator[](NumaReplicatedAccessToken token) const {
        assert(token.get_numa_index() < instances.size());
        return *(instances[token.get_numa_index()]);
    }

    const T& operator*() const { return *(instances[0]); }

    const T* operator->() const { return instances[0].get(); }

    template<typename FuncT>
    void modify_and_replicate(FuncT&& f) {
        auto source = std::move(instances[0]);
        std::forward<FuncT>(f)(*source);
        replicate_from(std::move(*source));
    }

    void on_numa_config_changed() override {
        // Use the first one as the source. It doesn't matter which one we use,
        // because they all must be identical, but the first one is guaranteed to exist.
        auto source = std::move(instances[0]);
        replicate_from(std::move(*source));
    }

   private:
    std::vector<std::unique_ptr<T>> instances;

    void replicate_from(T&& source) {
        instances.clear();

        const NumaConfig& cfg = get_numa_config();
        if (cfg.requires_memory_replication())
        {
            for (NumaIndex n = 0; n < cfg.num_numa_nodes(); ++n)
            {
                cfg.execute_on_numa_node(
                  n, [this, &source]() { instances.emplace_back(std::make_unique<T>(source)); });
            }
        }
        else
        {
            assert(cfg.num_numa_nodes() == 1);
            // We take advantage of the fact that replication is not required
            // and reuse the source value, avoiding one copy operation.
            instances.emplace_back(std::make_unique<T>(std::move(source)));
        }
    }
};

// We force boxing with a unique_ptr. If this becomes an issue due to added
// indirection we may need to add an option for a custom boxing type.
template<typename T>
class LazyNumaReplicated: public NumaReplicatedBase {
   public:
    using ReplicatorFuncType = std::function<T(const T&)>;

    LazyNumaReplicated(NumaReplicationContext& ctx) :
        NumaReplicatedBase(ctx) {
        prepare_replicate_from(T{});
    }

    LazyNumaReplicated(NumaReplicationContext& ctx, T&& source) :
        NumaReplicatedBase(ctx) {
        prepare_replicate_from(std::move(source));
    }

    LazyNumaReplicated(const LazyNumaReplicated&) = delete;
    LazyNumaReplicated(LazyNumaReplicated&& other) noexcept :
        NumaReplicatedBase(std::move(other)),
        instances(std::exchange(other.instances, {})) {}

    LazyNumaReplicated& operator=(const LazyNumaReplicated&) = delete;
    LazyNumaReplicated& operator=(LazyNumaReplicated&& other) noexcept {
        NumaReplicatedBase::operator=(*this, std::move(other));
        instances = std::exchange(other.instances, {});

        return *this;
    }

    LazyNumaReplicated& operator=(T&& source) {
        prepare_replicate_from(std::move(source));

        return *this;
    }

    ~LazyNumaReplicated() override = default;

    const T& operator[](NumaReplicatedAccessToken token) const {
        assert(token.get_numa_index() < instances.size());
        ensure_present(token.get_numa_index());
        return *(instances[token.get_numa_index()]);
    }

    const T& operator*() const { return *(instances[0]); }

    const T* operator->() const { return instances[0].get(); }

    template<typename FuncT>
    void modify_and_replicate(FuncT&& f) {
        auto source = std::move(instances[0]);
        std::forward<FuncT>(f)(*source);
        prepare_replicate_from(std::move(*source));
    }

    void on_numa_config_changed() override {
        // Use the first one as the source. It doesn't matter which one we use,
        // because they all must be identical, but the first one is guaranteed to exist.
        auto source = std::move(instances[0]);
        prepare_replicate_from(std::move(*source));
    }

   private:
    mutable std::vector<std::unique_ptr<T>> instances;
    mutable std::mutex                      mutex;

    void ensure_present(NumaIndex idx) const {
        assert(idx < instances.size());

        if (instances[idx] != nullptr)
            return;

        assert(idx != 0);

        std::unique_lock<std::mutex> lock(mutex);
        // Check again for races.
        if (instances[idx] != nullptr)
            return;

        const NumaConfig& cfg = get_numa_config();
        cfg.execute_on_numa_node(
          idx, [this, idx]() { instances[idx] = std::make_unique<T>(*instances[0]); });
    }

    void prepare_replicate_from(T&& source) {
        instances.clear();

        const NumaConfig& cfg = get_numa_config();
        if (cfg.requires_memory_replication())
        {
            assert(cfg.num_numa_nodes() > 0);

            // We just need to make sure the first instance is there.
            // Note that we cannot move here as we need to reallocate the data
            // on the correct NUMA node.
            cfg.execute_on_numa_node(
              0, [this, &source]() { instances.emplace_back(std::make_unique<T>(source)); });

            // Prepare others for lazy init.
            instances.resize(cfg.num_numa_nodes());
        }
        else
        {
            assert(cfg.num_numa_nodes() == 1);
            // We take advantage of the fact that replication is not required
            // and reuse the source value, avoiding one copy operation.
            instances.emplace_back(std::make_unique<T>(std::move(source)));
        }
    }
};

class NumaReplicationContext {
   public:
    NumaReplicationContext(NumaConfig&& cfg) :
        config(std::move(cfg)) {}

    NumaReplicationContext(const NumaReplicationContext&) = delete;
    NumaReplicationContext(NumaReplicationContext&&)      = delete;

    NumaReplicationContext& operator=(const NumaReplicationContext&) = delete;
    NumaReplicationContext& operator=(NumaReplicationContext&&)      = delete;

    ~NumaReplicationContext() {
        // The context must outlive replicated objects
        if (!trackedReplicatedObjects.empty())
            // Karuah Chess - lock engine without exiting
            KaruahChess::Engine::engineErr.add(KaruahChess::helper::NUMA_C_ERROR);
    }

    void attach(NumaReplicatedBase* obj) {
        assert(trackedReplicatedObjects.count(obj) == 0);
        trackedReplicatedObjects.insert(obj);
    }

    void detach(NumaReplicatedBase* obj) {
        assert(trackedReplicatedObjects.count(obj) == 1);
        trackedReplicatedObjects.erase(obj);
    }

    // oldObj may be invalid at this point
    void move_attached([[maybe_unused]] NumaReplicatedBase* oldObj, NumaReplicatedBase* newObj) {
        assert(trackedReplicatedObjects.count(oldObj) == 1);
        assert(trackedReplicatedObjects.count(newObj) == 0);
        trackedReplicatedObjects.erase(oldObj);
        trackedReplicatedObjects.insert(newObj);
    }

    void set_numa_config(NumaConfig&& cfg) {
        config = std::move(cfg);
        for (auto&& obj : trackedReplicatedObjects)
            obj->on_numa_config_changed();
    }

    const NumaConfig& get_numa_config() const { return config; }

   private:
    NumaConfig config;

    // std::set uses std::less by default, which is required for pointer comparison
    std::set<NumaReplicatedBase*> trackedReplicatedObjects;
};

inline NumaReplicatedBase::NumaReplicatedBase(NumaReplicationContext& ctx) :
    context(&ctx) {
    context->attach(this);
}

inline NumaReplicatedBase::NumaReplicatedBase(NumaReplicatedBase&& other) noexcept :
    context(std::exchange(other.context, nullptr)) {
    context->move_attached(&other, this);
}

inline NumaReplicatedBase& NumaReplicatedBase::operator=(NumaReplicatedBase&& other) noexcept {
    context = std::exchange(other.context, nullptr);

    context->move_attached(&other, this);

    return *this;
}

inline NumaReplicatedBase::~NumaReplicatedBase() {
    if (context != nullptr)
        context->detach(this);
}

inline const NumaConfig& NumaReplicatedBase::get_numa_config() const {
    return context->get_numa_config();
}

}  // namespace Stockfish


#endif  // #ifndef NUMA_H_INCLUDED
