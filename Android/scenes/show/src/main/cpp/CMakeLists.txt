cmake_minimum_required (VERSION 2.8)
project(apm-plugin-video)

set(CMAKE_C_FLAGS_DEBUG "-g -Wall")
set(CMAKE_CXX_FLAGS_DEBUG ${CMAKE_C_FLAGS_DEBUG})
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Wno-deprecated-declarations -Wno-reorder")
if(CLANG)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -stdlib=libc++")
endif()

set(SRCPATH ${CMAKE_CURRENT_SOURCE_DIR})

set(HEADERS

    ${SRCPATH}/video_premium_model_jni.h
    )

set(SOURCES
        ${SRCPATH}/video_premium_model_jni.cpp
)

set (PUBLIC_LINK_LIBS dl log)

set(TARGET_NAME ${PROJECT_NAME})
add_library (${TARGET_NAME} SHARED ${SOURCES} ${HEADERS})
target_link_libraries(${TARGET_NAME} ${PUBLIC_LINK_LIBS})
