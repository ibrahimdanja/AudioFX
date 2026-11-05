# AudioFX Studio: source-level feature audit

This audit is based on the supplied Equalizer314 source tree. It describes what is present in the code; it is not a claim that every effect has been verified on the target phone.

## Present in the supplied codebase

- Android `DynamicsProcessing` engine and audio-effect session management.
- Global session-0 mode and optional per-player audio-session mode.
- Parametric EQ with biquad filters and parametric-to-DynamicsProcessing conversion.
- Graphic/parametric editing UI. The current default is four editable parametric filters, with a default user cap of 16 and an optional cap up to 64. The separate simple EQ is a 10-band control. It is **not currently a fixed 31-band ISO graphic EQ**.
- Multiband compression with configurable bands, attack, release, ratio, threshold, knee, pre/post gain, noise-gate threshold and expander ratio.
- Limiter and related metering/visual feedback.
- Environmental reverb controls and device-capability handling.
- Visualizer/FFT spectrum analysis and waveform processing.
- AutoEQ measurement/target workflow, preset storage, backup/restore and EqualizerAPO conversion.
- Per-app preset bindings where audio-session detection is available.
- Per-output-device preset bindings and route-change handling.
- Foreground audio service, notification controls, Quick Settings tile and boot restoration.
- Optional experimental HRIR virtual-surround path. This is a separate AudioPlaybackCapture/MediaProjection capture-and-replay feature, not the main session-0 processing engine. It is not required for ordinary EQ processing and should remain off unless explicitly tested and wanted.
- Optional LAN TV remote-control subsystem.

## Not present as a confirmed, independent global effect

- A dedicated EasyEffects-style crossfeed module.
- A general stereo-width processor operating on arbitrary external playback.
- A standalone harmonic exciter.
- A general convolution-reverb module on the global audio stream.

Those features need a valid way to access and process the external stream's samples. Adding controls alone would not make them functional. Do not represent them as implemented until the processing path is real and verified.

## Immediate engineering priorities

1. Build the untouched derivative in GitHub Actions and resolve any build errors first.
2. Install and smoke-test the baseline on the Samsung Galaxy A10s.
3. Only then change DSP defaults or add features. Keep a known-good build available at each change.
4. Evaluate a dedicated 31-band ISO graphic EQ as a coordinated view over the existing parametric engine, not as a second stacked equalizer.
5. Evaluate optional effects against Android's actual global/session effect limitations before implementing them.

## Resource note

The project includes a large AutoEQ profile dataset (thousands of small text profiles). It is retained because it supports AutoEQ functionality. The native HRIR convolver is also retained because the experimental virtual-surround feature references it. Removing either feature requires removing its dependent source, manifest declarations, UI, assets and build configuration together.
