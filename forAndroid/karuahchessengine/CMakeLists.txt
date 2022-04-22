cmake_minimum_required(VERSION 3.10.0)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

add_definitions(-DNDEBUG -USE_POPCNT)

add_library(KaruahChessEngine-C
    SHARED
        src/main/cpp/KaruahChessEngine-C.cpp
        src/main/cpp/BitBoard.h
        src/main/cpp/BitBoard.cpp
        src/main/cpp/Engine.h
        src/main/cpp/Engine.cpp
        src/main/cpp/Helper.h
        src/main/cpp/Helper.cpp
        src/main/cpp/MoveRules.h
        src/main/cpp/MoveRules.cpp
        src/main/cpp/PiecePattern.h
        src/main/cpp/PiecePattern.cpp
        src/main/cpp/Search.h
        src/main/cpp/Search.cpp
        src/main/cpp/SFbitbase.cpp
        src/main/cpp/SFbitboard.h
        src/main/cpp/SFbitboard.cpp
        src/main/cpp/SFendgame.h
        src/main/cpp/SFendgame.cpp
        src/main/cpp/SFevaluate.h
        src/main/cpp/SFevaluate.cpp
        src/main/cpp/SFmaterial.h
        src/main/cpp/SFmaterial.cpp
        src/main/cpp/SFmisc.h
        src/main/cpp/SFmisc.cpp
        src/main/cpp/SFmovegen.h
        src/main/cpp/SFmovegen.cpp
        src/main/cpp/SFmovepick.h
        src/main/cpp/SFmovepick.cpp
        src/main/cpp/SFpawns.h
        src/main/cpp/SFpawns.cpp
        src/main/cpp/SFposition.h
        src/main/cpp/SFposition.cpp
        src/main/cpp/SFpsqt.h
        src/main/cpp/SFpsqt.cpp
        src/main/cpp/SFsearch.h
        src/main/cpp/SFsearch.cpp
        src/main/cpp/SFthread.h
        src/main/cpp/SFthread.cpp
        src/main/cpp/SFtimeman.h
        src/main/cpp/SFtimeman.cpp
        src/main/cpp/SFtt.h
        src/main/cpp/SFtt.cpp
        src/main/cpp/SFtypes.h
        src/main/cpp/SFuci.h
        src/main/cpp/SFuci.cpp
        src/main/cpp/SFucioption.cpp
        )

