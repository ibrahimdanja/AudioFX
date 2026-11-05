# Privacy Policy — AudioFX Studio

**App:** AudioFX Studio (`com.audiofxstudio.android`)
**Source:** This repository
**Licence:** GNU General Public License v3.0

## Summary

AudioFX Studio is designed to process audio locally on the Android device. It does not include advertising, analytics, user accounts, or a cloud audio-processing service. EQ settings and presets are stored locally.

The app does request Android's `INTERNET` permission because its optional TV/remote-control feature communicates with another device over the local network. That feature is intended for LAN communication; it is not an audio-upload or cloud-sync feature. Do not interpret the presence of the permission as proof that no network communication is possible. Review the source before distributing a modified build.

## Audio access

The app uses Android audio-effect APIs to process playback through supported audio sessions. The Android `Visualizer` API can expose short-lived audio measurements for the spectrum and dynamics displays. Those measurements are processed locally and are not intentionally saved or transmitted.

The source also contains an optional experimental virtual-surround feature that uses Android `MediaProjection` and `AudioPlaybackCapture` after explicit system consent. It captures selected app playback for local HRIR processing and replay. This is separate from the normal `DynamicsProcessing` session-0 path. Do not enable it unless you understand its routing, latency, battery and compatibility implications.

## Permissions declared

The manifest may declare the following permissions, depending on the feature set retained in a build:

- `MODIFY_AUDIO_SETTINGS`: attach and configure Android audio effects.
- `RECORD_AUDIO`: support the Visualizer API and the optional capture feature where Android requires it. This is not permission to secretly record microphone conversations.
- `INTERNET`, `ACCESS_WIFI_STATE`, `CHANGE_WIFI_MULTICAST_STATE`: optional local-network TV remote discovery and communication.
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `FOREGROUND_SERVICE_MEDIA_PROJECTION`: foreground audio processing and the optional projection-based feature.
- `POST_NOTIFICATIONS`: foreground-service notification on Android versions that require it.
- `BLUETOOTH_CONNECT`: identify/manage supported Bluetooth audio outputs on Android 12 and later.
- `RECEIVE_BOOT_COMPLETED`: restore the audio service when the user has enabled boot restoration.
- `DUMP`: an optional protected permission used by the audio-policy inspection path. Stock Android normally does not grant it to ordinary applications; failure is handled as a fallback.

## Local storage

Settings, presets, imported measurement/HRIR files and other app data are stored in the application's private storage unless the user explicitly exports or shares them. Uninstalling the app normally removes its private data.

## Network behaviour

The optional remote-control subsystem is intended to communicate with paired/discovered devices on the local network. The app does not need a cloud account for its core audio-processing functions. No claim is made here that a modified build has no network activity; inspect the source and permissions of the exact build you install.

## Third-party components and licensing

This project is derived from Equalizer314 and retains its GPL-3.0 licensing obligations. See `LICENSE` and `UPSTREAM_NOTICES.md`. Preserve bundled component notices and licences when redistributing.

## Changes

This policy should be updated whenever permissions, data handling, network behaviour or optional capture functionality changes.
