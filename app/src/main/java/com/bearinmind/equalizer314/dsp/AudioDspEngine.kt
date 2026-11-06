package com.bearinmind.equalizer314.dsp

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.audiofx.BassBoost
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.EnvironmentalReverb
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Build
import android.util.Log
import com.bearinmind.equalizer314.model.*
import kotlinx.coroutines.*
import kotlin.math.*

class AudioDspEngine(private val context: Context) {

    private val tag = "AudioDspEngine"
    private var isPoweredOn = true

    // Audio Effects for Session 0 (Global System Audio)
    private var dynamicsProcessing: DynamicsProcessing? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var bassBoostEffect: BassBoost? = null
    private var virtualizerEffect: Virtualizer? = null
    private var reverbEffect: EnvironmentalReverb? = null

    // Per-session effects map (for active music players registering via broadcasts)
    private val perSessionEffects = mutableMapOf<Int, MutableList<Any>>()

    // Hardware probing flags
    var hardwareCapabilities = AudioHardwareCapabilities()
        private set

    // Audio test playback (for interactive auditioning)
    private var testAudioTrack: AudioTrack? = null
    private var isTestTonePlaying = false
    private var testToneJob: Job? = null
    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Active DSP parameter cache
    private var cachedStereoConfig = StereoConfig()

    init {
        initAudioEffects(0)
    }

    /**
     * Initializes Android system audio effects on target [sessionId].
     * Session 0 targets the global Android mixer.
     */
    fun initAudioEffects(sessionId: Int) {
        var dpOk = false
        var loudOk = false
        var bassOk = false
        var virtOk = false
        var revOk = false
        var session0Permissive = true

        // 1. DynamicsProcessing (API 28+, Android 10 fully supported)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                // Configure 4-band MBC, PreEQ, PostEQ and Limiter
                val builder = DynamicsProcessing.Config.Builder(
                    DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                    2, // 2 channels (Stereo)
                    true, // PreEQ
                    10,   // PreEQ 10 bands
                    true, // MBC
                    4,    // MBC 4 bands
                    true, // PostEQ
                    10,   // PostEQ 10 bands
                    true  // Limiter
                )
                val config = builder.build()
                dynamicsProcessing = DynamicsProcessing(0, sessionId, config).apply {
                    enabled = isPoweredOn
                }
                dpOk = true
                Log.i(tag, "DynamicsProcessing attached on session $sessionId")
            } catch (e: Exception) {
                Log.w(tag, "DynamicsProcessing session $sessionId warning: ${e.message}")
                if (sessionId == 0) session0Permissive = false
            }
        }

        // 2. LoudnessEnhancer
        try {
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                enabled = isPoweredOn
            }
            loudOk = true
        } catch (e: Exception) {
            Log.w(tag, "LoudnessEnhancer session $sessionId warning: ${e.message}")
        }

        // 3. BassBoost
        try {
            bassBoostEffect = BassBoost(0, sessionId).apply {
                enabled = isPoweredOn
            }
            bassOk = true
        } catch (e: Exception) {
            Log.w(tag, "BassBoost session $sessionId warning: ${e.message}")
        }

        // 4. Virtualizer
        try {
            virtualizerEffect = Virtualizer(0, sessionId).apply {
                enabled = isPoweredOn
            }
            virtOk = true
        } catch (e: Exception) {
            Log.w(tag, "Virtualizer session $sessionId warning: ${e.message}")
        }

        // 5. EnvironmentalReverb
        try {
            reverbEffect = EnvironmentalReverb(0, sessionId).apply {
                enabled = false
            }
            revOk = true
        } catch (e: Exception) {
            Log.w(tag, "EnvironmentalReverb session $sessionId warning: ${e.message}")
        }

        hardwareCapabilities = AudioHardwareCapabilities(
            hasDynamicsProcessing = dpOk,
            hasLoudnessEnhancer = loudOk,
            hasBassBoost = bassOk,
            hasVirtualizer = virtOk,
            hasEnvironmentalReverb = revOk,
            isSession0Permissive = session0Permissive,
            activeSessionCount = perSessionEffects.size
        )
    }

    fun attachSession(sessionId: Int, packageName: String?) {
        if (sessionId <= 0 || perSessionEffects.containsKey(sessionId)) return
        Log.i(tag, "Attaching per-session effects for session $sessionId ($packageName)")

        val effectsList = mutableListOf<Any>()
        try {
            val bass = BassBoost(100, sessionId).apply { enabled = isPoweredOn }
            effectsList.add(bass)
        } catch (e: Exception) { /* ignore */ }

        try {
            val virt = Virtualizer(100, sessionId).apply { enabled = isPoweredOn }
            effectsList.add(virt)
        } catch (e: Exception) { /* ignore */ }

        try {
            val loud = LoudnessEnhancer(sessionId).apply { enabled = isPoweredOn }
            effectsList.add(loud)
        } catch (e: Exception) { /* ignore */ }

        perSessionEffects[sessionId] = effectsList
        hardwareCapabilities = hardwareCapabilities.copy(activeSessionCount = perSessionEffects.size)
    }

    fun detachSession(sessionId: Int) {
        val effects = perSessionEffects.remove(sessionId) ?: return
        effects.forEach { effect ->
            try {
                if (effect is android.media.audiofx.AudioEffect) {
                    effect.enabled = false
                    effect.release()
                }
            } catch (e: Exception) { /* ignore */ }
        }
        hardwareCapabilities = hardwareCapabilities.copy(activeSessionCount = perSessionEffects.size)
    }

    fun setPower(enabled: Boolean) {
        isPoweredOn = enabled
        try {
            dynamicsProcessing?.enabled = enabled
            loudnessEnhancer?.enabled = enabled
            bassBoostEffect?.enabled = enabled
            virtualizerEffect?.enabled = enabled
            reverbEffect?.enabled = enabled

            // Also toggle attached per-player sessions
            perSessionEffects.values.flatten().forEach { effect ->
                if (effect is android.media.audiofx.AudioEffect) {
                    effect.enabled = enabled
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Set power error: ${e.message}")
        }
    }

    fun applyPreamp(preampDb: Float) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && dynamicsProcessing != null) {
                dynamicsProcessing?.setInputGainAllChannelsTo(preampDb)
            }
        } catch (e: Exception) {
            Log.w(tag, "Apply preamp error: ${e.message}")
        }
    }

    fun applyStereo(config: StereoConfig) {
        cachedStereoConfig = config
        // In DynamicsProcessing, channel 0 is Left, channel 1 is Right
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && dynamicsProcessing != null) {
                val leftGain = when {
                    config.muteLeft -> -100f
                    config.balance > 0f -> -config.balance * 12f
                    else -> 0f
                }
                val rightGain = when {
                    config.muteRight -> -100f
                    config.balance < 0f -> config.balance * 12f
                    else -> 0f
                }
                dynamicsProcessing?.setInputGainbyChannel(0, leftGain)
                dynamicsProcessing?.setInputGainbyChannel(1, rightGain)
            }
        } catch (e: Exception) {
            Log.w(tag, "Apply stereo error: ${e.message}")
        }
    }

    fun applyLoudness(config: LoudnessConfig) {
        try {
            val loud = loudnessEnhancer ?: return
            loud.enabled = isPoweredOn && config.enabled
            if (loud.enabled) {
                loud.setTargetGain(config.targetGainMilliBels)
            }
        } catch (e: Exception) {
            Log.w(tag, "Apply loudness error: ${e.message}")
        }
    }

    fun applyBassBoost(strength: Float) {
        try {
            val clamped = strength.toInt().coerceIn(0, 1000).toShort()
            bassBoostEffect?.setStrength(clamped)
            perSessionEffects.values.flatten().filterIsInstance<BassBoost>().forEach {
                try { it.setStrength(clamped) } catch (e: Exception) {}
            }
        } catch (e: Exception) {
            Log.w(tag, "BassBoost set error: ${e.message}")
        }
    }

    fun applyVirtualizer(strength: Float) {
        try {
            val clamped = strength.toInt().coerceIn(0, 1000).toShort()
            virtualizerEffect?.setStrength(clamped)
            perSessionEffects.values.flatten().filterIsInstance<Virtualizer>().forEach {
                try { it.setStrength(clamped) } catch (e: Exception) {}
            }
        } catch (e: Exception) {
            Log.w(tag, "Virtualizer set error: ${e.message}")
        }
    }

    fun applyReverb(config: ReverbConfig) {
        try {
            val rev = reverbEffect ?: return
            rev.enabled = isPoweredOn && config.enabled && config.preset != ReverbPreset.NONE
            if (rev.enabled) {
                rev.decayTime = config.decayTimeMs.coerceIn(100, 20000)
                rev.roomLevel = (config.reverbLevelDb * 100).toInt().coerceIn(-9000, 0).toShort()
            }
        } catch (e: Exception) {
            Log.w(tag, "Reverb apply error: ${e.message}")
        }
    }

    fun applyLimiter(config: LimiterConfig) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && dynamicsProcessing != null) {
                val limiter = DynamicsProcessing.Limiter(
                    isPoweredOn && config.enabled,
                    config.enabled,
                    0, // link group
                    config.attackMs,
                    config.releaseMs,
                    config.ratio,
                    config.thresholdDb,
                    config.postGainDb
                )
                dynamicsProcessing?.setLimiterAllChannelsTo(limiter)
            }
        } catch (e: Exception) {
            Log.w(tag, "Limiter apply error: ${e.message}")
        }
    }

    fun applyMbcBands(bands: List<CompressorBand>) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && dynamicsProcessing != null) {
                bands.forEachIndexed { index, band ->
                    val mbcBand = DynamicsProcessing.MbcBand(
                        true,
                        band.highCutoffHz,
                        band.attackMs,
                        band.releaseMs,
                        band.ratio,
                        band.thresholdDb,
                        band.kneeDb,
                        band.noiseGateThresholdDb,
                        1.0f,
                        band.preGainDb,
                        band.postGainDb
                    )
                    dynamicsProcessing?.setMbcBandAllChannelsTo(index, mbcBand)
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "MBC apply error: ${e.message}")
        }
    }

    /**
     * Toggles interactive stereo auditioning synthesizer (warm chord + rhythm pulse).
     */
    fun toggleTestAudio(onStatusChange: (Boolean) -> Unit) {
        if (isTestTonePlaying) {
            stopTestAudio()
            onStatusChange(false)
        } else {
            startTestAudio()
            onStatusChange(true)
        }
    }

    private fun startTestAudio() {
        stopTestAudio()
        isTestTonePlaying = true

        val sampleRate = 44100
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        ) * 2

        try {
            testAudioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            testAudioTrack?.play()

            testToneJob = engineScope.launch {
                val chunkSize = 2048
                val shortBuf = ShortArray(chunkSize * 2)
                var phase1 = 0.0
                var phase2 = 0.0
                var phase3 = 0.0
                var time = 0.0

                // Synth chord: C3 (130.81Hz), G3 (196.00Hz), E4 (329.63Hz)
                val f1 = 130.81
                val f2 = 196.00
                val f3 = 329.63

                while (isActive && isTestTonePlaying) {
                    val stereo = cachedStereoConfig

                    for (i in 0 until chunkSize) {
                        time += 1.0 / sampleRate
                        val beatLFO = (sin(2.0 * Math.PI * 1.5 * time) + 1.0) * 0.25 + 0.5

                        var sampleL = (sin(phase1) * 0.4 + sin(phase2) * 0.3 + sin(phase3) * 0.2) * beatLFO
                        var sampleR = (sin(phase1 * 1.002) * 0.4 + sin(phase2 * 0.998) * 0.3 + sin(phase3 * 1.001) * 0.2) * beatLFO

                        // Mono downmix
                        if (stereo.monoDownmix) {
                            val mono = (sampleL + sampleR) * 0.5
                            sampleL = mono
                            sampleR = mono
                        }

                        // Crossfeed simulation (Bauer-style)
                        if (stereo.crossfeedEnabled) {
                            val cf = stereo.crossfeedLevel * 0.3
                            val newL = sampleL * (1.0 - cf) + sampleR * cf
                            val newR = sampleR * (1.0 - cf) + sampleL * cf
                            sampleL = newL
                            sampleR = newR
                        }

                        // Balance & Mutes
                        if (stereo.muteLeft) sampleL = 0.0
                        if (stereo.muteRight) sampleR = 0.0
                        if (stereo.balance < 0) sampleR *= (1.0 + stereo.balance)
                        if (stereo.balance > 0) sampleL *= (1.0 - stereo.balance)

                        shortBuf[i * 2] = (sampleL.coerceIn(-1.0, 1.0) * 26000).toInt().toShort()
                        shortBuf[i * 2 + 1] = (sampleR.coerceIn(-1.0, 1.0) * 26000).toInt().toShort()

                        phase1 += 2.0 * Math.PI * f1 / sampleRate
                        phase2 += 2.0 * Math.PI * f2 / sampleRate
                        phase3 += 2.0 * Math.PI * f3 / sampleRate

                        if (phase1 > 2.0 * Math.PI) phase1 -= 2.0 * Math.PI
                        if (phase2 > 2.0 * Math.PI) phase2 -= 2.0 * Math.PI
                        if (phase3 > 2.0 * Math.PI) phase3 -= 2.0 * Math.PI
                    }
                    testAudioTrack?.write(shortBuf, 0, shortBuf.size)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error starting test audio: ${e.message}", e)
            isTestTonePlaying = false
        }
    }

    fun stopTestAudio() {
        isTestTonePlaying = false
        testToneJob?.cancel()
        testToneJob = null
        try {
            testAudioTrack?.stop()
            testAudioTrack?.release()
        } catch (e: Exception) {}
        testAudioTrack = null
    }

    fun release() {
        stopTestAudio()
        try {
            dynamicsProcessing?.release()
            loudnessEnhancer?.release()
            bassBoostEffect?.release()
            virtualizerEffect?.release()
            reverbEffect?.release()
            perSessionEffects.values.flatten().forEach {
                if (it is android.media.audiofx.AudioEffect) it.release()
            }
            perSessionEffects.clear()
        } catch (e: Exception) {}
    }

    companion object {
        val ISO_10_FREQUENCIES = listOf(
            31f, 62f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f
        )

        val ISO_20_FREQUENCIES = listOf(
            31.5f, 45f, 63f, 90f, 125f, 180f, 250f, 355f, 500f, 710f,
            1000f, 1400f, 2000f, 2800f, 4000f, 5600f, 8000f, 11200f, 16000f, 20000f
        )

        val ISO_31_FREQUENCIES = listOf(
            20f, 25f, 31.5f, 40f, 50f, 63f, 80f, 100f, 125f, 160f, 200f, 250f,
            315f, 400f, 500f, 630f, 800f, 1000f, 1250f, 1600f, 2000f, 2500f,
            3150f, 4000f, 5000f, 6300f, 8000f, 10000f, 12500f, 16000f, 20000f
        )

        fun formatFrequency(freq: Float): String {
            return if (freq >= 1000f) {
                if (freq % 1000f == 0f) "${(freq / 1000f).toInt()}k"
                else String.format("%.1fk", freq / 1000f)
            } else {
                if (freq == freq.toInt().toFloat()) "${freq.toInt()}"
                else String.format("%.1f", freq)
            }
        }

        fun exportToEqualizerApo(preampDb: Float, filters: List<ParametricFilter>): String {
            val sb = StringBuilder()
            sb.append(String.format("Preamp: %.1f dB\n", preampDb))
            filters.forEachIndexed { index, filter ->
                val state = if (filter.enabled) "ON" else "OFF"
                val typeStr = filter.type.shortName
                sb.append(
                    String.format(
                        "Filter %d: %s %s Fc %.0f Hz Gain %.1f dB Q %.2f\n",
                        index + 1, state, typeStr, filter.frequency, filter.gainDb, filter.qFactor
                    )
                )
            }
            return sb.toString()
        }

        fun parseEqualizerApo(text: String): Pair<Float, List<ParametricFilter>> {
            var preamp = 0f
            val filters = mutableListOf<ParametricFilter>()
            var idCounter = 1

            text.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("Preamp:", ignoreCase = true)) {
                    val parts = trimmed.split(":")
                    if (parts.size >= 2) {
                        val numStr = parts[1].replace("dB", "", ignoreCase = true).trim()
                        preamp = numStr.toFloatOrNull() ?: 0f
                    }
                } else if (trimmed.startsWith("Filter", ignoreCase = true)) {
                    try {
                        val isEnabled = !trimmed.contains("OFF", ignoreCase = true)
                        val type = when {
                            trimmed.contains("LSC", ignoreCase = true) || trimmed.contains("LowShelf", ignoreCase = true) -> FilterType.LOW_SHELF
                            trimmed.contains("HSC", ignoreCase = true) || trimmed.contains("HighShelf", ignoreCase = true) -> FilterType.HIGH_SHELF
                            trimmed.contains("LP", ignoreCase = true) || trimmed.contains("LowPass", ignoreCase = true) -> FilterType.LOW_PASS
                            trimmed.contains("HP", ignoreCase = true) || trimmed.contains("HighPass", ignoreCase = true) -> FilterType.HIGH_PASS
                            trimmed.contains("NOTCH", ignoreCase = true) -> FilterType.NOTCH
                            else -> FilterType.PEAK
                        }

                        val fcRegex = Regex("""Fc\s+([0-9.]+)\s*Hz""", RegexOption.IGNORE_CASE)
                        val gainRegex = Regex("""Gain\s+([+-]?[0-9.]+)\s*dB""", RegexOption.IGNORE_CASE)
                        val qRegex = Regex("""Q\s+([0-9.]+)""", RegexOption.IGNORE_CASE)

                        val fc = fcRegex.find(trimmed)?.groupValues?.get(1)?.toFloatOrNull() ?: 1000f
                        val gain = gainRegex.find(trimmed)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                        val q = qRegex.find(trimmed)?.groupValues?.get(1)?.toFloatOrNull() ?: 1.41f

                        filters.add(
                            ParametricFilter(
                                id = idCounter++,
                                enabled = isEnabled,
                                type = type,
                                frequency = fc,
                                gainDb = gain,
                                qFactor = q
                            )
                        )
                    } catch (e: Exception) {
                        Log.w("AudioDspEngine", "Failed to parse filter line: $trimmed")
                    }
                }
            }
            return Pair(preamp, filters)
        }
    }
}
