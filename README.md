# AudioFX Studio for Android

AudioFX Studio is a modern, professional audio effects application for Android built with **Kotlin** and **Jetpack Compose** following Material Design 3 guidelines.

## Features

- **Equalizer Engine**:
  - **10-Band Graphic Equalizer**: Standard 1-octave bands (31 Hz – 16 kHz) with smooth biquad interpolation.
  - **31-Band ISO Graphic Equalizer**: High-resolution 1/3-octave ISO standard frequency bands (20 Hz – 20 kHz).
  - **Parametric Equalizer**: Up to 16 configurable biquad filters with Peaking, Low Shelf, High Shelf, Low Pass, High Pass, and Notch filter types.
  - **Real-Time Interactive Frequency Response Curve**: Logarithmic 20 Hz – 20 kHz response curve with draggable parametric nodes.
- **Multiband Dynamics Compressor (MBC)**:
  - 4 independent frequency bands (Low, Low-Mid, Mid-High, High).
  - Configurable Threshold (-60 dB to 0 dB), Ratio (1:1 to 20:1), Knee, Attack, Release, Make-up Gain, and Noise Gate.
  - Live Gain Reduction (GR) dynamic meters.
- **Studio Limiter & Reverb**:
  - Zero-clipping brickwall peak limiter with ceiling control and lookahead.
  - Environmental Reverb modeler with acoustic spaces (Small Room, Medium Room, Large Room, Medium Hall, Large Hall, Studio Plate, Cathedral) and decay time controls.
- **AutoEQ Headphone Correction**:
  - Integrated database of over 10,000 headphone calibration measurements (Harman Target) from oratory1990, Crinacle, Rtings, Super Review, etc.
  - Real-time search and filtering (In-Ear, Over-Ear, Earbud).
  - Target compensation curve preview.
  - One-tap "Apply to EQ" to automatically transfer parametric calibration filters.
- **Presets & EqualizerAPO Interoperability**:
  - Studio presets: Flat (Neutral), Bass Cannon, Vocal Clarifier, Electronic & Club, Rock & Metal, Cinema Surround.
  - Save, edit, and delete custom user presets with persistent storage.
  - Export and import configurations using standard EqualizerAPO / Peace `.txt` format.
- **Acoustic Enhancements & Auditioning**:
  - Sub-Bass booster and 3D Spatial Virtualizer.
  - Built-in test tone / chord synthesizer for immediate auditory auditioning.
  - Audio output routing (Phone Speaker, Wired Headphones, Bluetooth, USB DAC).
- **Real-Time Visualizer**:
  - 16-band animated audio spectrum analyzer with peak-hold indicators.

## Tech Stack

- **UI**: 100% Jetpack Compose with Material 3 Dark Studio Theme
- **Language**: Kotlin 2.0 with Kotlin Coroutines & StateFlow
- **DSP**: Digital Biquad filter mathematics (Audio EQ Cookbook)
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)
