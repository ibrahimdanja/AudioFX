package com.bearinmind.equalizer314.model

import com.google.gson.annotations.SerializedName

enum class EqMode(val displayName: String) {
    GRAPHIC_10("10-Band (1-Octave)"),
    GRAPHIC_20("20-Band (1/2-Octave)"),
    GRAPHIC_31("31-Band (1/3-Octave ISO)"),
    PARAMETRIC("Parametric Biquad")
}

data class EqBand(
    val id: Int,
    val frequency: Float,
    val gainDb: Float,
    val label: String
)

enum class FilterType(val displayName: String, val shortName: String) {
    PEAK("Peaking EQ", "PK"),
    LOW_SHELF("Low Shelf", "LSC"),
    HIGH_SHELF("High Shelf", "HSC"),
    LOW_PASS("Low Pass", "LP"),
    HIGH_PASS("High Pass", "HP"),
    NOTCH("Notch Filter", "NOTCH")
}

data class ParametricFilter(
    val id: Int,
    val enabled: Boolean = true,
    val type: FilterType = FilterType.PEAK,
    val frequency: Float = 1000f,
    val gainDb: Float = 0f,
    val qFactor: Float = 1.414f
)

data class CompressorBand(
    val id: Int,
    val name: String,
    val lowCutoffHz: Float,
    val highCutoffHz: Float,
    val thresholdDb: Float = -24f,
    val ratio: Float = 3.0f,
    val attackMs: Float = 10f,
    val releaseMs: Float = 100f,
    val kneeDb: Float = 6f,
    val preGainDb: Float = 0f,
    val postGainDb: Float = 0f,
    val noiseGateThresholdDb: Float = -70f,
    val gainReductionDb: Float = 0f
)

data class LimiterConfig(
    val enabled: Boolean = true,
    val inputGainDb: Float = 0f,
    val postGainDb: Float = 0f,
    val thresholdDb: Float = -1.0f,
    val ceilingDb: Float = -0.3f,
    val attackMs: Float = 1.0f,
    val releaseMs: Float = 50f,
    val ratio: Float = 20.0f,
    val lookaheadMs: Float = 2.0f,
    val isClipping: Boolean = false,
    val currentPeakDb: Float = -12f
)

data class LoudnessConfig(
    val enabled: Boolean = false,
    val targetGainMilliBels: Int = 400 // 0 to 2000 mB (0 to +20 dB boost)
)

enum class ReverbPreset(val displayName: String) {
    NONE("Bypass / Off"),
    SMALL_ROOM("Small Room"),
    MEDIUM_ROOM("Medium Room"),
    LARGE_ROOM("Large Room"),
    MEDIUM_HALL("Medium Concert Hall"),
    LARGE_HALL("Large Concert Hall"),
    PLATE("Studio Plate"),
    CATHEDRAL("Cathedral")
}

data class ReverbConfig(
    val enabled: Boolean = false,
    val preset: ReverbPreset = ReverbPreset.MEDIUM_ROOM,
    val decayTimeMs: Int = 1400,
    val roomSize: Float = 0.5f,
    val damping: Float = 0.5f,
    val reverbLevelDb: Float = -6f,
    val stereoWidth: Float = 1.0f
)

data class StereoConfig(
    val balance: Float = 0f,              // -1.0 (Full Left) to +1.0 (Full Right)
    val monoDownmix: Boolean = false,     // Sum L+R to both channels
    val crossfeedEnabled: Boolean = false,
    val crossfeedLevel: Float = 0.4f,     // 0.0 to 1.0 (Bauer crossfeed depth)
    val stereoWidth: Float = 1.0f,        // 0.0 (Mono) to 2.0 (Ultra-wide)
    val muteLeft: Boolean = false,
    val muteRight: Boolean = false
)

data class BassExciterConfig(
    val bassBoostStrength: Float = 150f,        // 0 to 1000
    val harmonicExciterStrength: Float = 20f,   // 0 to 100 (high-frequency air brilliance)
    val bassCenterFreqHz: Float = 80f,          // 40 to 150 Hz
    val tonalTiltDb: Float = 0f                 // -6.0 (Warm/Dark) to +6.0 (Bright/Crisp)
)

enum class EffectScopeType(val label: String, val badgeColorHex: Long) {
    GLOBAL_SESSION_0("Global Session 0", 0xFF00E5FF),
    PER_SESSION("Per-Session Active", 0xFFB388FF),
    LOCAL_DSP("DSP Engine", 0xFFFFD54F),
    HARDWARE_NATIVE("Hardware Native", 0xFF00E676),
    MONITORING_ONLY("Monitoring Only", 0xFF94A3B8)
}

data class PipelineModule(
    val id: String,
    val name: String,
    val description: String,
    val enabled: Boolean,
    val scopeType: EffectScopeType,
    val statusText: String,
    val routeIndex: Int
)

data class AudioHardwareCapabilities(
    val hasDynamicsProcessing: Boolean = true,
    val hasBassBoost: Boolean = true,
    val hasVirtualizer: Boolean = true,
    val hasEnvironmentalReverb: Boolean = true,
    val hasLoudnessEnhancer: Boolean = true,
    val isSession0Permissive: Boolean = true,
    val activeSessionCount: Int = 0
)

data class AutoEqEntry(
    @SerializedName("n") val name: String,
    @SerializedName("s") val source: String,
    @SerializedName("t") val type: String,
    @SerializedName("r") val rig: String,
    @SerializedName("p") val path: String
)

data class AutoEqProfile(
    val headphoneName: String,
    val source: String,
    val preampDb: Float,
    val filters: List<ParametricFilter>
)

data class EqPreset(
    val id: String,
    val name: String,
    val isFactory: Boolean = false,
    val category: String = "General",
    val preampDb: Float = 0f,
    val graphic10Gains: List<Float> = List(10) { 0f },
    val graphic20Gains: List<Float> = List(20) { 0f },
    val graphic31Gains: List<Float> = List(31) { 0f },
    val parametricFilters: List<ParametricFilter> = emptyList(),
    val bassBoost: Float = 0f,
    val virtualizer: Float = 0f,
    val tonalTiltDb: Float = 0f,
    val reverbPreset: ReverbPreset = ReverbPreset.NONE
)

enum class AudioOutputDevice(val displayName: String, val iconName: String) {
    PHONE_SPEAKER("Phone Speaker", "Speaker"),
    WIRED_HEADPHONES("Wired Headphones", "Headphones"),
    BLUETOOTH("Bluetooth Audio", "Bluetooth"),
    USB_DAC("USB DAC / Interface", "Usb")
}
