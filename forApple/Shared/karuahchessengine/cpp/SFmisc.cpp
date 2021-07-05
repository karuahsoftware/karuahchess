/*
  Stockfish, a UCI chess playing engine derived from Glaurung 2.1
  Copyright (C) 2004-2020 The Stockfish developers (see AUTHORS file)

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

#ifdef _WIN32

#ifndef NOMINMAX
#define NOMINMAX
#endif

#include <windows.h>

#endif

#include <fstream>
#include <iomanip>
#include <iostream>
#include <sstream>
#include <vector>
#include <cstdlib>

#if defined(__linux__) && !defined(__ANDROID__)
#include <stdlib.h>
#include <sys/mman.h>
#endif

#if defined(__APPLE__) || defined(__ANDROID__) || defined(__OpenBSD__) || (defined(__GLIBCXX__) && !defined(_GLIBCXX_HAVE_ALIGNED_ALLOC) && !defined(_WIN32))
#define POSIXALIGNEDALLOC
#include <stdlib.h>
#endif

#include "SFmisc.h"
#include "SFthread.h"


    namespace SF {

        using namespace std;

        namespace {

            /// Version number. If Version is left empty, then compile date in the format
            /// DD-MM-YY and show in engine_info.
            const string Version = "12";
            

        } // namespace


        /// engine_info() returns the full name of the current Stockfish version. This
        /// will be either "Stockfish <Tag> DD-MM-YY" (where DD-MM-YY is the date when
        /// the program was compiled) or "Stockfish <Version>", depending on whether
        /// Version is empty.

        const string engine_info(bool to_uci) {

            const string months("Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec");
            string month, day, year;
            stringstream ss, date(__DATE__); // From compiler, format is "Sep 21 2008"

            ss << "Stockfish " << Version << setfill('0');

            if (Version.empty())
            {
                date >> month >> day >> year;
                ss << setw(2) << day << setw(2) << (1 + months.find(month) / 4) << year.substr(2);
            }

            ss << (to_uci ? "\nid author " : " by ")
                << "the Stockfish developers (see AUTHORS file)";

            return ss.str();
        }
                

        /// prefetch() preloads the given address in L1/L2 cache. This is a non-blocking
        /// function that doesn't stall the CPU waiting for data to be loaded from memory,
        /// which can be quite slow.
#ifdef NO_PREFETCH

        void prefetch(void*) {}

#else

        void prefetch(void* addr) {

#  if defined(__INTEL_COMPILER)
            // This hack prevents prefetches from being optimized away by
            // Intel compiler. Both MSVC and gcc seem not be affected by this.
            __asm__("");
#  endif

#  if (defined(__INTEL_COMPILER) || defined(_MSC_VER)) && (defined(_M_X64) || defined(_M_IX86))
            _mm_prefetch((char*)addr, _MM_HINT_T0);
#  elif (defined(__INTEL_COMPILER) || defined(_MSC_VER)) && (defined(_M_ARM) || defined(_M_ARM64))
            __prefetch(addr);
#  else
            __builtin_prefetch(addr);
#  endif
        }

#endif


        /// std_aligned_alloc() is our wrapper for systems where the c++17 implementation
        /// does not guarantee the availability of aligned_alloc(). Memory allocated with
        /// std_aligned_alloc() must be freed with std_aligned_free().

        void* std_aligned_alloc(size_t alignment, size_t size) {

#if defined(POSIXALIGNEDALLOC)
            void* mem;
            return posix_memalign(&mem, alignment, size) ? nullptr : mem;
#elif defined(_WIN32)
            return _mm_malloc(size, alignment);
#else
            return std::aligned_alloc(alignment, size);
#endif
        }

        void std_aligned_free(void* ptr) {

#if defined(POSIXALIGNEDALLOC)
            free(ptr);
#elif defined(_WIN32)
            _mm_free(ptr);
#else
            free(ptr);
#endif
        }

        /// aligned_ttmem_alloc() will return suitably aligned memory, if possible using large pages.
        /// The returned pointer is the aligned one, while the mem argument is the one that needs
        /// to be passed to free. With c++17 some of this functionality could be simplified.

#if defined(__linux__) && !defined(__ANDROID__)

        void* aligned_ttmem_alloc(size_t allocSize, void*& mem) {

            constexpr size_t alignment = 2 * 1024 * 1024; // assumed 2MB page sizes
            size_t size = ((allocSize + alignment - 1) / alignment) * alignment; // multiple of alignment
            if (posix_memalign(&mem, alignment, size))
                mem = nullptr;
#if defined(MADV_HUGEPAGE)
            madvise(mem, allocSize, MADV_HUGEPAGE);
#endif
            return mem;
        }

#elif defined(_WIN64)

        static void* aligned_ttmem_alloc_large_pages(size_t allocSize) {

            HANDLE hProcessToken{ };
            LUID luid{ };
            void* mem = nullptr;

            const size_t largePageSize = GetLargePageMinimum();
            if (!largePageSize)
                return nullptr;

            // We need SeLockMemoryPrivilege, so try to enable it for the process
            if (!OpenProcessToken(GetCurrentProcess(), TOKEN_ADJUST_PRIVILEGES | TOKEN_QUERY, &hProcessToken))
                return nullptr;

            if (LookupPrivilegeValue(NULL, SE_LOCK_MEMORY_NAME, &luid))
            {
                TOKEN_PRIVILEGES tp{ };
                TOKEN_PRIVILEGES prevTp{ };
                DWORD prevTpLen = 0;

                tp.PrivilegeCount = 1;
                tp.Privileges[0].Luid = luid;
                tp.Privileges[0].Attributes = SE_PRIVILEGE_ENABLED;

                // Try to enable SeLockMemoryPrivilege. Note that even if AdjustTokenPrivileges() succeeds,
                // we still need to query GetLastError() to ensure that the privileges were actually obtained.
                if (AdjustTokenPrivileges(
                    hProcessToken, FALSE, &tp, sizeof(TOKEN_PRIVILEGES), &prevTp, &prevTpLen) &&
                    GetLastError() == ERROR_SUCCESS)
                {
                    // Round up size to full pages and allocate
                    allocSize = (allocSize + largePageSize - 1) & ~size_t(largePageSize - 1);
                    mem = VirtualAlloc(
                        NULL, allocSize, MEM_RESERVE | MEM_COMMIT | MEM_LARGE_PAGES, PAGE_READWRITE);

                    // Privilege no longer needed, restore previous state
                    AdjustTokenPrivileges(hProcessToken, FALSE, &prevTp, 0, NULL, NULL);
                }
            }

            CloseHandle(hProcessToken);

            return mem;
        }

        void* aligned_ttmem_alloc(size_t allocSize, void*& mem) {

            
            // Try to allocate large pages
            mem = aligned_ttmem_alloc_large_pages(allocSize);

            
            // Fall back to regular, page aligned, allocation if necessary
            if (!mem)
                mem = VirtualAlloc(NULL, allocSize, MEM_RESERVE | MEM_COMMIT, PAGE_READWRITE);

            return mem;
        }

#else

        void* aligned_ttmem_alloc(size_t allocSize, void*& mem) {

            constexpr size_t alignment = 64; // assumed cache line size
            size_t size = allocSize + alignment - 1; // allocate some extra space
            mem = malloc(size);
            void* ret = reinterpret_cast<void*>((uintptr_t(mem) + alignment - 1) & ~uintptr_t(alignment - 1));
            return ret;
        }

#endif


        /// aligned_ttmem_free() will free the previously allocated ttmem

#if defined(_WIN64)

        void aligned_ttmem_free(void* mem) {

            if (mem && !VirtualFree(mem, 0, MEM_RELEASE))
            {
                DWORD err = GetLastError();
                std::cerr << "Failed to free transposition table. Error code: 0x" <<
                    std::hex << err << std::dec << std::endl;
                exit(EXIT_FAILURE);
            }
        }

#else

        void aligned_ttmem_free(void* mem) {
            free(mem);
        }

#endif


     

        

    }
