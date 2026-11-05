# AudioFX Studio for Android

AudioFX Studio is an Android audio-effects application derived from the open-source Equalizer314 project. It is intended for system audio processing on Android devices, with a primary compatibility target of Android 10 and low-memory ARM phones such as the Samsung Galaxy A10s (2 GB RAM).

## Important project facts

- Language: Kotlin, with a small native C/C++ component inherited for the optional HRIR convolution feature.
- Android minimum: API 24; compile/target SDK: API 34; Android 10 (API 29) is within the supported range.
- Main processing foundation: Android `DynamicsProcessing`, `AudioEffect`, and `Visualizer` APIs.
- Main capabilities inherited from Equalizer314 include parametric/graphic EQ, multiband dynamics (including gate/expander controls), limiter, reverb controls, spectrum analysis, AutoEQ/preset tooling, per-app session routing, and per-output-device profiles.
- The supplied code currently has a 10-band simple EQ and a parametric editor (four initial filters, 16-band default cap, optionally up to 64); it does not yet provide a fixed 31-band ISO graphic EQ. See `FEATURE_AUDIT.md`.
- The app is designed to process external playback through Android audio-effect sessions. Actual routing support depends on the Android build, player, and whether another effect app owns the same session.

## Build a debug APK

Requirements: Android Studio or Android SDK command-line tools, JDK 21, Android NDK `27.2.12479018`, CMake `3.22.1`, and Gradle 8.5 (the wrapper downloads this when internet access is available).

```bash
chmod +x ./gradlew
./gradlew assembleDebug
```

APK output:

`app/build/outputs/apk/debug/app-debug.apk`

Install with ADB:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Build on GitHub

Push this repository to GitHub. The `Build Debug APK` workflow runs on pushes and manual dispatch, then publishes the debug APK as a downloadable workflow artifact.

## Device testing

Compilation is not proof of audio functionality. Test on the actual Samsung Galaxy A10s: global/session routing, audible EQ changes, compressor/limiter behaviour, reverb, spectrum display, output changes, service persistence, and coexistence with other audio-effect apps.

## Upstream and licence

This project is a modified derivative of [Equalizer314](https://github.com/bearinmindcat/Equalizer314). The upstream project is licensed under GNU GPL v3. This derivative retains the GPL-3.0 licence; modifications and redistributed source must remain GPL-3.0-compatible. See `LICENSE` and `UPSTREAM_NOTICES.md`.
