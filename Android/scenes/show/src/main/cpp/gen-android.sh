#!/bin/bash

mkdir -p build
cd build

abi="armeabi-v7a with NEON" # "arm64-v8a or x86
toolchain=arm-linux-androideabi-4.9

if [[ $abi =~ armeabi-v7a ]]; then
    # abi="armeabi-v7a with NEON"
    toolchain=arm-linux-androideabi-4.9
elif [[ $abi =~ arm64-v8a ]]; then
    # abi="arm64-v8a"
    toolchain=aarch64-linux-android-4.9
elif [[ $arch =~ x86 ]]; then
    # abi="x86"
    toolchain=x86-4.9
else
    echo "arch must be armv7a, arm64, or x86"
    exit 1
fi

cmake ../ \
    -DCMAKE_TOOLCHAIN_FILE=../android.toolchain.cmake \
    -DCMAKE_BUILD_TYPE=Release \
    -DANDROID_NDK=$ANDROID_NDK_ROOT \
    -DANDROID_ABI="$abi" \
    -DANDROID_TOOLCHAIN_NAME=$toolchain \
    -DANDROID_NATIVE_API_LEVEL=android-16 \
    -DANDROID_STL_FORCE_FEATURES=OFF \
    -G"Unix Makefiles" || exit 1
# cmake --build . -- VERBOSE=1 || exit 1
cmake --build . || exit 1
#make -j4 || exit 1

cd ..

exit 0

