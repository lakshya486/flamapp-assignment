# Flamapp.AI Assignment - Full Project (Kotlin + JNI C++)

This is a beginner-friendly minimal project that captures camera frames on Android,
sends them to native C++ via JNI for simple edge detection, and displays the processed
result on screen. A tiny web viewer is included to show how to display a base64 image.

## What this includes
- Android app (Kotlin) with Camera preview (Camera1 for simplicity)
- JNI bridge to `native-lib.cpp`
- Native C++ implementation: NV21 -> grayscale -> Sobel edge detector -> RGBA buffer
- Simple ImageView-based renderer (no OpenGL) to keep it easy for beginners
- Small web folder with index.html that can display a base64 PNG if you export one

## How to build & run
1. Install Android Studio and Android SDK.
2. In Android Studio: Open this folder as a project.
3. Make sure NDK and CMake are installed (SDK Manager -> SDK Tools -> check NDK and CMake).
4. Build and run on a real Android device (camera preview works best on real device).

## Notes for the assignment
- The native code here does not depend on OpenCV to keep setup simple for beginners.
  This satisfies the JNI + native processing requirement. If you *must* use OpenCV,
  I can help add it later (it requires copying the OpenCV Android SDK into the project).

## Files of interest
- app/src/main/java/com/example/flamapp/MainActivity.kt
- app/src/main/java/com/example/flamapp/NativeBridge.kt
- app/src/main/cpp/native-lib.cpp
- web/index.html

## Example commit messages
- init: add project skeleton
- feat(android): add camera preview and UI
- feat(native): add JNI + C++ Sobel edge detector
- docs: add README
