# Build AudioFX Studio on GitHub

1. Create a new empty GitHub repository.
2. Extract this ZIP and upload the **contents** of the `AudioFX-Studio` folder to the repository root (the root should contain `settings.gradle.kts`, `gradlew`, and the `app/` folder).
3. Commit the files to the `main` branch.
4. Open the repository's **Actions** tab.
5. Select **Build Debug APK**. The workflow starts automatically after a push; you can also run it manually.
6. Open the completed workflow run and download the `AudioFX-Studio-debug-apk` artifact.
7. Extract the artifact to obtain `app-debug.apk`.

The workflow uses JDK 21, Android SDK, Android platform/build tools 34, NDK `27.2.12479018`, CMake `3.22.1`, and the repository's Gradle wrapper.

For local builds:

```bash
chmod +x ./gradlew
./gradlew assembleDebug
```

The local APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

A successful GitHub build proves that the source compiled in the hosted build environment. It does not prove that every Android audio effect works on a particular phone. Test audio routing and audible effect behaviour on the Samsung Galaxy A10s.
