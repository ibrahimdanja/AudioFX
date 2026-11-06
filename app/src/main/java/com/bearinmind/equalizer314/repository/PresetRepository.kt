package com.bearinmind.equalizer314.repository

import android.content.Context
import android.content.SharedPreferences
import com.bearinmind.equalizer314.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PresetRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("audiofx_presets", Context.MODE_PRIVATE)
    private val gson = Gson()

    val factoryPresets: List<EqPreset> = listOf(
        EqPreset(
            id = "factory_flat",
            name = "Flat (Neutral)",
            isFactory = true,
            category = "Reference",
            preampDb = 0f,
            graphic10Gains = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
            graphic20Gains = List(20) { 0f },
            graphic31Gains = List(31) { 0f },
            parametricFilters = listOf(
                ParametricFilter(1, true, FilterType.LOW_SHELF, 100f, 0f, 0.71f),
                ParametricFilter(2, true, FilterType.PEAK, 1000f, 0f, 1.41f),
                ParametricFilter(3, true, FilterType.PEAK, 3500f, 0f, 2.0f),
                ParametricFilter(4, true, FilterType.HIGH_SHELF, 8000f, 0f, 0.71f)
            )
        ),
        EqPreset(
            id = "factory_bass",
            name = "Bass Enhancement",
            isFactory = true,
            category = "Music",
            preampDb = -3.5f,
            graphic10Gains = listOf(6.5f, 5.5f, 4.0f, 2.0f, 0.5f, 0f, 0f, 1f, 1.5f, 2.0f),
            graphic20Gains = listOf(7f, 6f, 5.5f, 5f, 4f, 3f, 2f, 1f, 0.5f, 0f, 0f, 0f, 0.5f, 1f, 1.5f, 2f, 2.5f, 2.5f, 2f, 1.5f),
            graphic31Gains = List(31) { idx -> if (idx < 8) 6.0f - (idx * 0.7f) else 0f },
            parametricFilters = listOf(
                ParametricFilter(1, true, FilterType.LOW_SHELF, 75f, 6.5f, 0.71f),
                ParametricFilter(2, true, FilterType.PEAK, 180f, 3.0f, 1.2f),
                ParametricFilter(3, true, FilterType.PEAK, 3000f, 1.0f, 1.4f),
                ParametricFilter(4, true, FilterType.HIGH_SHELF, 10000f, 2.0f, 0.71f)
            ),
            bassBoost = 600f,
            tonalTiltDb = -1.5f
        ),
        EqPreset(
            id = "factory_vocal",
            name = "Vocal Clarity",
            isFactory = true,
            category = "Speech & Vocals",
            preampDb = -2.0f,
            graphic10Gains = listOf(-2f, -1.5f, 0f, 1.5f, 3.5f, 4.0f, 3.0f, 2.0f, 1.0f, 0f),
            graphic20Gains = List(20) { idx -> if (idx in 6..14) 3.5f else 0f },
            graphic31Gains = List(31) { idx -> if (idx in 10..22) 3.0f else 0f },
            parametricFilters = listOf(
                ParametricFilter(1, true, FilterType.HIGH_PASS, 80f, 0f, 0.71f),
                ParametricFilter(2, true, FilterType.PEAK, 1200f, 3.0f, 1.8f),
                ParametricFilter(3, true, FilterType.PEAK, 3200f, 4.5f, 2.2f),
                ParametricFilter(4, true, FilterType.PEAK, 6000f, -2.0f, 3.0f)
            ),
            tonalTiltDb = 2.0f
        ),
        EqPreset(
            id = "factory_classical",
            name = "Classical & Symphony",
            isFactory = true,
            category = "Acoustic",
            preampDb = -1.5f,
            graphic10Gains = listOf(2.5f, 2.0f, 1.0f, 0f, -0.5f, 0.5f, 1.5f, 2.5f, 3.0f, 3.5f),
            graphic20Gains = List(20) { idx -> if (idx > 12) 2.5f else 1.0f },
            graphic31Gains = List(31) { 0f },
            parametricFilters = listOf(
                ParametricFilter(1, true, FilterType.PEAK, 60f, 2.0f, 1.0f),
                ParametricFilter(2, true, FilterType.PEAK, 2500f, 1.5f, 1.5f),
                ParametricFilter(3, true, FilterType.HIGH_SHELF, 7500f, 3.0f, 0.71f)
            ),
            virtualizer = 350f,
            reverbPreset = ReverbPreset.MEDIUM_HALL
        ),
        EqPreset(
            id = "factory_acoustic",
            name = "Acoustic & Unplugged",
            isFactory = true,
            category = "Acoustic",
            preampDb = -1.5f,
            graphic10Gains = listOf(2.0f, 1.5f, 0.5f, 0f, 1.0f, 1.5f, 2.5f, 3.0f, 2.5f, 2.0f),
            graphic20Gains = List(20) { 1.5f },
            graphic31Gains = List(31) { 0f },
            parametricFilters = listOf(
                ParametricFilter(1, true, FilterType.PEAK, 110f, 2.5f, 1.4f),
                ParametricFilter(2, true, FilterType.PEAK, 800f, -1.0f, 1.0f),
                ParametricFilter(3, true, FilterType.PEAK, 4200f, 3.0f, 2.0f)
            ),
            virtualizer = 200f,
            reverbPreset = ReverbPreset.SMALL_ROOM
        ),
        EqPreset(
            id = "factory_electronic",
            name = "Electronic & EDM",
            isFactory = true,
            category = "Music",
            preampDb = -3.0f,
            graphic10Gains = listOf(5.5f, 4.5f, 2.0f, 0f, -1f, 1f, 2.5f, 4.0f, 5.0f, 4.5f),
            graphic20Gains = List(20) { idx -> if (idx < 5 || idx > 15) 4.5f else -1f },
            graphic31Gains = List(31) { 0f },
            parametricFilters = listOf(
                ParametricFilter(1, true, FilterType.PEAK, 55f, 6.0f, 1.5f),
                ParametricFilter(2, true, FilterType.PEAK, 500f, -2.0f, 1.0f),
                ParametricFilter(3, true, FilterType.PEAK, 4500f, 3.5f, 1.8f),
                ParametricFilter(4, true, FilterType.HIGH_SHELF, 9000f, 4.5f, 0.71f)
            ),
            bassBoost = 450f,
            virtualizer = 300f
        ),
        EqPreset(
            id = "factory_hiphop",
            name = "Hip-Hop & R&B",
            isFactory = true,
            category = "Music",
            preampDb = -3.5f,
            graphic10Gains = listOf(6.0f, 5.0f, 3.0f, 1.0f, 0f, 1.0f, 2.0f, 3.5f, 4.0f, 3.5f),
            graphic20Gains = List(20) { idx -> if (idx < 6) 5.5f else 1.5f },
            graphic31Gains = List(31) { 0f },
            parametricFilters = listOf(
                ParametricFilter(1, true, FilterType.LOW_SHELF, 65f, 6.0f, 0.71f),
                ParametricFilter(2, true, FilterType.PEAK, 350f, 2.0f, 1.2f),
                ParametricFilter(3, true, FilterType.HIGH_SHELF, 8000f, 3.0f, 0.71f)
            ),
            bassBoost = 500f
        ),
        EqPreset(
            id = "factory_rock",
            name = "Rock & Metal",
            isFactory = true,
            category = "Music",
            preampDb = -2.5f,
            graphic10Gains = listOf(4.5f, 3.0f, 1.5f, -1.0f, -1.5f, 0.5f, 2.5f, 3.5f, 4.0f, 4.0f),
            graphic20Gains = List(20) { idx -> if (idx < 4 || idx > 14) 3.5f else -1.0f },
            graphic31Gains = List(31) { 0f },
            parametricFilters = listOf(
                ParametricFilter(1, true, FilterType.LOW_SHELF, 100f, 4.0f, 0.71f),
                ParametricFilter(2, true, FilterType.PEAK, 650f, -2.0f, 1.5f),
                ParametricFilter(3, true, FilterType.PEAK, 2800f, 3.5f, 2.0f),
                ParametricFilter(4, true, FilterType.HIGH_SHELF, 8000f, 3.5f, 0.71f)
            )
        ),
        EqPreset(
            id = "factory_podcast",
            name = "Podcast & Speech",
            isFactory = true,
            category = "Speech & Vocals",
            preampDb = -1.0f,
            graphic10Gains = listOf(-4f, -2f, 0f, 1.5f, 3.0f, 3.5f, 2.5f, 1.0f, -1f, -3f),
            graphic20Gains = List(20) { 0f },
            graphic31Gains = List(31) { 0f },
            parametricFilters = listOf(
                ParametricFilter(1, true, FilterType.HIGH_PASS, 90f, 0f, 0.71f),
                ParametricFilter(2, true, FilterType.PEAK, 250f, -2.0f, 1.5f),
                ParametricFilter(3, true, FilterType.PEAK, 2400f, 3.5f, 1.8f),
                ParametricFilter(4, true, FilterType.LOW_PASS, 12000f, 0f, 0.71f)
            ),
            tonalTiltDb = 1.0f
        ),
        EqPreset(
            id = "factory_cinema",
            name = "Cinema Surround",
            isFactory = true,
            category = "Spatial",
            preampDb = -2.5f,
            graphic10Gains = listOf(4.0f, 3.0f, 1.5f, 0f, 1.0f, 2.0f, 2.5f, 3.5f, 3.5f, 3.0f),
            graphic20Gains = List(20) { 2.0f },
            graphic31Gains = List(31) { 0f },
            parametricFilters = listOf(
                ParametricFilter(1, true, FilterType.PEAK, 45f, 5.0f, 1.2f),
                ParametricFilter(2, true, FilterType.PEAK, 1500f, 2.0f, 1.5f),
                ParametricFilter(3, true, FilterType.PEAK, 4000f, 3.5f, 2.0f),
                ParametricFilter(4, true, FilterType.HIGH_SHELF, 11000f, 3.5f, 0.71f)
            ),
            virtualizer = 700f,
            bassBoost = 300f,
            reverbPreset = ReverbPreset.LARGE_ROOM
        )
    )

    fun getUserPresets(): List<EqPreset> {
        val json = prefs.getString("custom_presets_list", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<EqPreset>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveUserPreset(preset: EqPreset) {
        val current = getUserPresets().toMutableList()
        val index = current.indexOfFirst { it.id == preset.id }
        if (index >= 0) {
            current[index] = preset
        } else {
            current.add(preset)
        }
        prefs.edit().putString("custom_presets_list", gson.toJson(current)).apply()
    }

    fun deleteUserPreset(id: String) {
        val current = getUserPresets().toMutableList()
        current.removeAll { it.id == id }
        prefs.edit().putString("custom_presets_list", gson.toJson(current)).apply()
    }

    fun getDeviceBinding(device: AudioOutputDevice): String? {
        return prefs.getString("device_binding_${device.name}", null)
    }

    fun setDeviceBinding(device: AudioOutputDevice, presetId: String) {
        prefs.edit().putString("device_binding_${device.name}", presetId).apply()
    }
}
