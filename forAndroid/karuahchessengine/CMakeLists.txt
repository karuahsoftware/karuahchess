cmake_minimum_required(VERSION 3.22.1)
project(karuahchessengine)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

add_definitions(-DNDEBUG -USE_POPCNT)



add_library(KaruahChessEngine-C
    SHARED
        src/main/cpp/KaruahChessEngine-C.cpp
        src/main/cpp/bitboard.h
        src/main/cpp/bitboard.cpp
        src/main/cpp/engine.h
        src/main/cpp/engine.cpp
        src/main/cpp/helper.h
        src/main/cpp/helper.cpp
        src/main/cpp/moverules.h
        src/main/cpp/moverules.cpp
        src/main/cpp/piecepattern.h
        src/main/cpp/piecepattern.cpp
        src/main/cpp/search.h
        src/main/cpp/search.cpp
        src/main/cpp/sf_bitboard.h
        src/main/cpp/sf_bitboard.cpp
        src/main/cpp/sf_evaluate.h
        src/main/cpp/sf_evaluate.cpp
        src/main/cpp/sf_misc.h
        src/main/cpp/sf_misc.cpp
        src/main/cpp/sf_movegen.h
        src/main/cpp/sf_movegen.cpp
        src/main/cpp/sf_movepick.h
        src/main/cpp/sf_movepick.cpp
        src/main/cpp/sf_position.h
        src/main/cpp/sf_position.cpp
        src/main/cpp/sf_search.h
        src/main/cpp/sf_search.cpp
        src/main/cpp/sf_thread.h
        src/main/cpp/sf_thread.cpp
        src/main/cpp/sf_thread_win32_osx.h
        src/main/cpp/sf_timeman.h
        src/main/cpp/sf_timeman.cpp
        src/main/cpp/sf_tt.h
        src/main/cpp/sf_tt.cpp
        src/main/cpp/sf_types.h
        src/main/cpp/sf_uci.h
        src/main/cpp/sf_uci.cpp
        src/main/cpp/sf_ucioption.cpp
        src/main/cpp/nnue/evaluate_nnue.cpp
        src/main/cpp/nnue/evaluate_nnue.h
        src/main/cpp/nnue/nnue_accumulator.h
        src/main/cpp/nnue/nnue_architecture.h
        src/main/cpp/nnue/nnue_common.h
        src/main/cpp/nnue/nnue_feature_transformer.h
        src/main/cpp/nnue/features/half_ka_v2_hm.cpp
        src/main/cpp/nnue/features/half_ka_v2_hm.h
        src/main/cpp/nnue/layers/affine_transform.h
        src/main/cpp/nnue/layers/affine_transform_sparse_input.h
        src/main/cpp/nnue/layers/clipped_relu.h
        src/main/cpp/nnue/layers/simd.h
        src/main/cpp/nnue/layers/sqr_clipped_relu.h
        src/main/cpp/syzygy/tbprobe.h
        )

add_library(KaruahChessEngine-C1
        SHARED
        src/main/cpp/KaruahChessEngine-C1.cpp
        src/main/cpp/bitboard.h
        src/main/cpp/bitboard.cpp
        src/main/cpp/engine.h
        src/main/cpp/engine.cpp
        src/main/cpp/helper.h
        src/main/cpp/helper.cpp
        src/main/cpp/moverules.h
        src/main/cpp/moverules.cpp
        src/main/cpp/piecepattern.h
        src/main/cpp/piecepattern.cpp
        src/main/cpp/search.h
        src/main/cpp/search.cpp
        src/main/cpp/sf_bitboard.h
        src/main/cpp/sf_bitboard.cpp
        src/main/cpp/sf_evaluate.h
        src/main/cpp/sf_evaluate.cpp
        src/main/cpp/sf_misc.h
        src/main/cpp/sf_misc.cpp
        src/main/cpp/sf_movegen.h
        src/main/cpp/sf_movegen.cpp
        src/main/cpp/sf_movepick.h
        src/main/cpp/sf_movepick.cpp
        src/main/cpp/sf_position.h
        src/main/cpp/sf_position.cpp
        src/main/cpp/sf_search.h
        src/main/cpp/sf_search.cpp
        src/main/cpp/sf_thread.h
        src/main/cpp/sf_thread.cpp
        src/main/cpp/sf_thread_win32_osx.h
        src/main/cpp/sf_timeman.h
        src/main/cpp/sf_timeman.cpp
        src/main/cpp/sf_tt.h
        src/main/cpp/sf_tt.cpp
        src/main/cpp/sf_types.h
        src/main/cpp/sf_uci.h
        src/main/cpp/sf_uci.cpp
        src/main/cpp/sf_ucioption.cpp
        src/main/cpp/nnue/evaluate_nnue.cpp
        src/main/cpp/nnue/evaluate_nnue.h
        src/main/cpp/nnue/nnue_accumulator.h
        src/main/cpp/nnue/nnue_architecture.h
        src/main/cpp/nnue/nnue_common.h
        src/main/cpp/nnue/nnue_feature_transformer.h
        src/main/cpp/nnue/features/half_ka_v2_hm.cpp
        src/main/cpp/nnue/features/half_ka_v2_hm.h
        src/main/cpp/nnue/layers/affine_transform.h
        src/main/cpp/nnue/layers/affine_transform_sparse_input.h
        src/main/cpp/nnue/layers/clipped_relu.h
        src/main/cpp/nnue/layers/simd.h
        src/main/cpp/nnue/layers/sqr_clipped_relu.h
        src/main/cpp/syzygy/tbprobe.h
        )

find_library(android-lib android)

target_link_libraries(KaruahChessEngine-C ${android-lib})
target_link_libraries(KaruahChessEngine-C1 ${android-lib})


