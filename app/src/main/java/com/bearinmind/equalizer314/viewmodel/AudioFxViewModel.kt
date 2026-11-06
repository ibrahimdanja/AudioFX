package com.bearinmind.equalizer314.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bearinmind.equalizer314.dsp.AudioDspEngine
import com.bearinmind.equalizer314.dsp.BiquadMath
import com.bearinmind.equalizer314.model.*
import com.bearinmind.equalizer314.repository.AutoEqRepository
import com.bearinmind.equalizer314.repository.PresetRepository
import com.bearinmind.equalizer314.service.EqService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.sin
import kotlin.random.Random

data class AudioFxUiState(
    val isPoweredOn: Boolean = true,
    val preampDb: Float = 0f,
    val outputDevice: AudioOutputDevice = AudioOutputDevice.WIRED_HEADPHONES,
    val eqMode: EqMode = EqMode.GRAPHIC_10,

    // Graphic EQ gains
    val graphic10Gains: List<Float> = List(10) { 0f },
    val graphic20Gains: List<Float> = List(20) { 0f },
    val graphic31Gains: List<Float> = List(31) { 0f },

    // Parametric EQ
    val parametricFilters: List<ParametricFilter> = listOf(
        ParametricFilter(1, true, FilterType.LOW_SHELF, 105f, 0f, 0.71f),
        ParametricFilter(2, true, FilterType.PEAK, 300f, 0f, 1.41f),
        ParametricFilter(3, true, FilterType.PEAK, 1000f, 0f, 1.41f),
        ParametricFilter(4, true, FilterType.PEAK, 3500f, 0f, 2.0f),
        ParametricFilter(5, true, FilterType.HIGH_SHELF, 8500f, 0f, 0.71f)
    ),

    // Stereo Processing
    val stereo: StereoConfig = StereoConfig(),

    // Bass & Tonal Exciter
    val bassExciter: BassExciterConfig = BassExciterConfig(),

    // Loudness Normalizer
    val loudness: LoudnessConfig = LoudnessConfig(),

    // Multiband Compressor
    val compressorBands: List<CompressorBand> = listOf(
        CompressorBand(1, "Low (Sub-Bass)", 20f, 160f, -24f, 4f, 15f, 120f, 6f, 0f, 0f, -70f),
        CompressorBand(2, "Low-Mid (Body)", 160f, 800f, -20f, 3f, 10f, 80f, 4f, 0f, 0f, -70f),
        CompressorBand(3, "Mid-High (Lead)", 800f, 4000f, -18f, 2.5f, 8f, 60f, 4f, 0f, 0f, -70f),
        CompressorBand(4, "High (Air)", 4000f, 20000f, -22f, 2f, 5f, 50f, 6f, 0f, 0f, -70f)
    ),
    val selectedCompressorBandIndex: Int = 0,

    // Limiter
    val limiter: LimiterConfig = LimiterConfig(),

    // Reverb
    val reverb: ReverbConfig = ReverbConfig(),

    // AutoEQ
    val autoEqSearchQuery: String = "",
    val autoEqTypeFilter: String? = null,
    val autoEqSearchResults: List<AutoEqEntry> = emptyList(),
    val isAutoEqLoading: Boolean = false,
    val activeAutoEqProfile: AutoEqProfile? = null,

    // Spectrum Visualizer
    val spectrumBars: List<Float> = List(16) { 0.1f },
    val isTestTonePlaying: Boolean = false,

    // Presets
    val userPresets: List<EqPreset> = emptyList(),
    val currentPresetName: String = "Flat (Neutral)",

    // Hardware status
    val hardwareCapabilities: AudioHardwareCapabilities = AudioHardwareCapabilities()
)

class AudioFxViewModel(application: Application) : AndroidViewModel(application) {

    private val presetRepo = PresetRepository(application)
    private val autoEqRepo = AutoEqRepository(application)

    private val _uiState = MutableStateFlow(AudioFxUiState())
    val uiState: StateFlow<AudioFxUiState> = _uiState.asStateFlow()

    private var visualizerJob: Job? = null
    private var searchJob: Job? = null

    // Direct DSP reference (either from foreground service or standalone fallback)
    private val dsp: AudioDspEngine
        get() = EqService.instance?.dspEngine ?: fallbackDspEngine

    private val fallbackDspEngine by lazy { AudioDspEngine(application) }

    init {
        startServiceIfNecessary(application)
        loadUserPresets()
        startVisualizerLoop()
        searchAutoEq("")
        refreshHardwareCapabilities()
    }

    private fun startServiceIfNecessary(context: Context) {
        try {
            val serviceIntent = Intent(context, EqService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            // Service startup handled gracefully
        }
    }

    private fun refreshHardwareCapabilities() {
        val caps = dsp.hardwareCapabilities
        _uiState.update { it.copy(hardwareCapabilities = caps) }
    }

    private fun loadUserPresets() {
        val userList = presetRepo.getUserPresets()
        _uiState.update { it.copy(userPresets = userList) }
    }

    fun toggleMasterPower() {
        _uiState.update { current ->
            val newState = !current.isPoweredOn
            dsp.setPower(newState)
            EqService.instance?.updateStatus(current.currentPresetName, newState)
            current.copy(isPoweredOn = newState)
        }
    }

    fun setPreamp(gainDb: Float) {
        val clamped = gainDb.coerceIn(-15f, 15f)
        dsp.applyPreamp(clamped)
        _uiState.update { it.copy(preampDb = clamped) }
    }

    fun setEqMode(mode: EqMode) {
        _uiState.update { it.copy(eqMode = mode) }
    }

    fun setOutputDevice(device: AudioOutputDevice) {
        _uiState.update { it.copy(outputDevice = device) }
        // Check if device has an auto-binding
        presetRepo.getDeviceBinding(device)?.let { presetId ->
            val preset = presetRepo.factoryPresets.find { it.id == presetId }
                ?: presetRepo.getUserPresets().find { it.id == presetId }
            if (preset != null) {
                applyPreset(preset)
            }
        }
    }

    fun bindPresetToDevice(device: AudioOutputDevice, presetId: String) {
        presetRepo.setDeviceBinding(device, presetId)
    }

    fun updateGraphic10Band(index: Int, gainDb: Float) {
        _uiState.update { current ->
            val updated = current.graphic10Gains.toMutableList()
            if (index in updated.indices) {
                updated[index] = gainDb.coerceIn(-15f, 15f)
            }
            current.copy(graphic10Gains = updated, currentPresetName = "Custom 10-Band")
        }
    }

    fun updateGraphic20Band(index: Int, gainDb: Float) {
        _uiState.update { current ->
            val updated = current.graphic20Gains.toMutableList()
            if (index in updated.indices) {
                updated[index] = gainDb.coerceIn(-15f, 15f)
            }
            current.copy(graphic20Gains = updated, currentPresetName = "Custom 20-Band")
        }
    }

    fun updateGraphic31Band(index: Int, gainDb: Float) {
        _uiState.update { current ->
            val updated = current.graphic31Gains.toMutableList()
            if (index in updated.indices) {
                updated[index] = gainDb.coerceIn(-15f, 15f)
            }
            current.copy(graphic31Gains = updated, currentPresetName = "Custom 31-Band")
        }
    }

    fun updateParametricFilter(filter: ParametricFilter) {
        _uiState.update { current ->
            val updated = current.parametricFilters.map {
                if (it.id == filter.id) filter else it
            }
            current.copy(parametricFilters = updated, currentPresetName = "Custom Parametric")
        }
    }

    fun addParametricFilter() {
        _uiState.update { current ->
            if (current.parametricFilters.size >= 16) return@update current
            val newId = (current.parametricFilters.maxOfOrNull { it.id } ?: 0) + 1
            val newFilter = ParametricFilter(
                id = newId,
                enabled = true,
                type = FilterType.PEAK,
                frequency = 1000f,
                gainDb = 0f,
                qFactor = 1.41f
            )
            current.copy(
                parametricFilters = current.parametricFilters + newFilter,
                currentPresetName = "Custom Parametric"
            )
        }
    }

    fun removeParametricFilter(id: Int) {
        _uiState.update { current ->
            if (current.parametricFilters.size <= 1) return@update current
            current.copy(
                parametricFilters = current.parametricFilters.filterNot { it.id == id },
                currentPresetName = "Custom Parametric"
            )
        }
    }

    // Stereo Processing
    fun updateStereo(stereo: StereoConfig) {
        dsp.applyStereo(stereo)
        _uiState.update { it.copy(stereo = stereo) }
    }

    // Bass & Tonal Exciter
    fun updateBassExciter(config: BassExciterConfig) {
        dsp.applyBassBoost(config.bassBoostStrength)
        _uiState.update { it.copy(bassExciter = config) }
    }

    // Loudness Normalizer
    fun updateLoudness(config: LoudnessConfig) {
        dsp.applyLoudness(config)
        _uiState.update { it.copy(loudness = config) }
    }

    // Multiband Compressor
    fun selectCompressorBand(index: Int) {
        _uiState.update { it.copy(selectedCompressorBandIndex = index) }
    }

    fun updateCompressorBand(band: CompressorBand) {
        _uiState.update { current ->
            val updated = current.compressorBands.map {
                if (it.id == band.id) band else it
            }
            dsp.applyMbcBands(updated)
            current.copy(compressorBands = updated)
        }
    }

    // Limiter
    fun updateLimiter(limiter: LimiterConfig) {
        dsp.applyLimiter(limiter)
        _uiState.update { it.copy(limiter = limiter) }
    }

    // Reverb
    fun updateReverb(reverb: ReverbConfig) {
        dsp.applyReverb(reverb)
        _uiState.update { it.copy(reverb = reverb) }
    }

    fun setReverbPreset(preset: ReverbPreset) {
        val updated = when (preset) {
            ReverbPreset.NONE -> _uiState.value.reverb.copy(enabled = false, preset = preset)
            ReverbPreset.SMALL_ROOM -> _uiState.value.reverb.copy(enabled = true, preset = preset, decayTimeMs = 600, roomSize = 0.25f, reverbLevelDb = -8f)
            ReverbPreset.MEDIUM_ROOM -> _uiState.value.reverb.copy(enabled = true, preset = preset, decayTimeMs = 1200, roomSize = 0.5f, reverbLevelDb = -6f)
            ReverbPreset.LARGE_ROOM -> _uiState.value.reverb.copy(enabled = true, preset = preset, decayTimeMs = 1800, roomSize = 0.75f, reverbLevelDb = -5f)
            ReverbPreset.MEDIUM_HALL -> _uiState.value.reverb.copy(enabled = true, preset = preset, decayTimeMs = 2400, roomSize = 0.8f, reverbLevelDb = -4f)
            ReverbPreset.LARGE_HALL -> _uiState.value.reverb.copy(enabled = true, preset = preset, decayTimeMs = 3200, roomSize = 0.95f, reverbLevelDb = -3f)
            ReverbPreset.PLATE -> _uiState.value.reverb.copy(enabled = true, preset = preset, decayTimeMs = 1600, roomSize = 0.6f, damping = 0.2f, reverbLevelDb = -4f)
            ReverbPreset.CATHEDRAL -> _uiState.value.reverb.copy(enabled = true, preset = preset, decayTimeMs = 4500, roomSize = 1.0f, reverbLevelDb = -2f)
        }
        updateReverb(updated)
    }

    // Test Audio
    fun toggleTestAudio() {
        dsp.toggleTestAudio { isPlaying ->
            _uiState.update { it.copy(isTestTonePlaying = isPlaying) }
        }
    }

    // AutoEQ
    fun onAutoEqQueryChange(query: String) {
        _uiState.update { it.copy(autoEqSearchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(150)
            searchAutoEq(query)
        }
    }

    fun setAutoEqTypeFilter(type: String?) {
        _uiState.update { it.copy(autoEqTypeFilter = type) }
        searchAutoEq(_uiState.value.autoEqSearchQuery)
    }

    private fun searchAutoEq(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAutoEqLoading = true) }
            val results = autoEqRepo.search(query, _uiState.value.autoEqTypeFilter)
            _uiState.update { it.copy(autoEqSearchResults = results, isAutoEqLoading = false) }
        }
    }

    fun selectAndLoadAutoEqProfile(entry: AutoEqEntry) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAutoEqLoading = true) }
            val profile = autoEqRepo.loadProfile(entry)
            _uiState.update {
                it.copy(
                    activeAutoEqProfile = profile,
                    isAutoEqLoading = false
                )
            }
        }
    }

    fun applyAutoEqToParametric() {
        val profile = _uiState.value.activeAutoEqProfile ?: return
        _uiState.update { current ->
            current.copy(
                eqMode = EqMode.PARAMETRIC,
                preampDb = profile.preampDb,
                parametricFilters = profile.filters,
                currentPresetName = "AutoEQ: ${profile.headphoneName}"
            )
        }
        dsp.applyPreamp(profile.preampDb)
    }

    // Presets
    fun applyPreset(preset: EqPreset) {
        _uiState.update { current ->
            current.copy(
                preampDb = preset.preampDb,
                graphic10Gains = preset.graphic10Gains,
                graphic20Gains = preset.graphic20Gains,
                graphic31Gains = preset.graphic31Gains,
                parametricFilters = if (preset.parametricFilters.isNotEmpty()) preset.parametricFilters else current.parametricFilters,
                bassExciter = current.bassExciter.copy(
                    bassBoostStrength = preset.bassBoost,
                    tonalTiltDb = preset.tonalTiltDb
                ),
                stereo = current.stereo.copy(stereoWidth = if (preset.virtualizer > 0) preset.virtualizer / 500f else 1.0f),
                currentPresetName = preset.name
            )
        }
        dsp.applyPreamp(preset.preampDb)
        dsp.applyBassBoost(preset.bassBoost)
        dsp.applyVirtualizer(preset.virtualizer)
        if (preset.reverbPreset != ReverbPreset.NONE) {
            setReverbPreset(preset.reverbPreset)
        }
        EqService.instance?.updateStatus(preset.name, _uiState.value.isPoweredOn)
    }

    fun saveCurrentAsUserPreset(name: String, category: String = "Custom") {
        val current = _uiState.value
        val preset = EqPreset(
            id = UUID.randomUUID().toString(),
            name = name,
            isFactory = false,
            category = category,
            preampDb = current.preampDb,
            graphic10Gains = current.graphic10Gains,
            graphic20Gains = current.graphic20Gains,
            graphic31Gains = current.graphic31Gains,
            parametricFilters = current.parametricFilters,
            bassBoost = current.bassExciter.bassBoostStrength,
            virtualizer = current.stereo.stereoWidth * 500f,
            tonalTiltDb = current.bassExciter.tonalTiltDb,
            reverbPreset = current.reverb.preset
        )
        presetRepo.saveUserPreset(preset)
        loadUserPresets()
        _uiState.update { it.copy(currentPresetName = name) }
    }

    fun deleteUserPreset(id: String) {
        presetRepo.deleteUserPreset(id)
        loadUserPresets()
    }

    fun exportCurrentToApo(): String {
        val current = _uiState.value
        return AudioDspEngine.exportToEqualizerApo(current.preampDb, current.parametricFilters)
    }

    fun importFromApo(apoText: String) {
        val (preamp, filters) = AudioDspEngine.parseEqualizerApo(apoText)
        if (filters.isNotEmpty()) {
            _uiState.update { current ->
                current.copy(
                    eqMode = EqMode.PARAMETRIC,
                    preampDb = preamp,
                    parametricFilters = filters,
                    currentPresetName = "Imported EqualizerAPO"
                )
            }
            dsp.applyPreamp(preamp)
        }
    }

    fun resetEq() {
        _uiState.update { current ->
            current.copy(
                preampDb = 0f,
                graphic10Gains = List(10) { 0f },
                graphic20Gains = List(20) { 0f },
                graphic31Gains = List(31) { 0f },
                parametricFilters = listOf(
                    ParametricFilter(1, true, FilterType.LOW_SHELF, 105f, 0f, 0.71f),
                    ParametricFilter(2, true, FilterType.PEAK, 300f, 0f, 1.41f),
                    ParametricFilter(3, true, FilterType.PEAK, 1000f, 0f, 1.41f),
                    ParametricFilter(4, true, FilterType.PEAK, 3500f, 0f, 2.0f),
                    ParametricFilter(5, true, FilterType.HIGH_SHELF, 8500f, 0f, 0.71f)
                ),
                bassExciter = current.bassExciter.copy(bassBoostStrength = 0f, tonalTiltDb = 0f),
                currentPresetName = "Flat (Neutral)"
            )
        }
        dsp.applyPreamp(0f)
        dsp.applyBassBoost(0f)
    }

    /**
     * Efficient visualizer animation loop (runs at ~25 FPS with zero background CPU allocations).
     */
    private fun startVisualizerLoop() {
        visualizerJob = viewModelScope.launch {
            var phase = 0.0
            while (isActive) {
                delay(40)
                val current = _uiState.value
                val isPlaying = current.isTestTonePlaying
                val isPower = current.isPoweredOn

                phase += 0.15
                val newBars = List(16) { i ->
                    if (!isPower) {
                        0.05f
                    } else if (isPlaying) {
                        val base = (sin(phase + i * 0.4) * 0.4 + 0.5).toFloat()
                        val noise = (Random.nextFloat() * 0.2f)
                        (base + noise).coerceIn(0.1f, 1.0f)
                    } else {
                        val base = (sin(phase * 0.5 + i * 0.3) * 0.08 + 0.15).toFloat()
                        base.coerceIn(0.06f, 0.35f)
                    }
                }

                val updatedMbc = current.compressorBands.mapIndexed { idx, band ->
                    val reduction = if (isPower && isPlaying) {
                        val gr = (sin(phase * 1.2 + idx.toDouble()) * 3.0 + 4.0).toFloat()
                        gr.coerceIn(0f, 12f)
                    } else 0f
                    band.copy(gainReductionDb = reduction)
                }

                _uiState.update {
                    it.copy(
                        spectrumBars = newBars,
                        compressorBands = updatedMbc
                    )
                }
            }
        }
    }
}
