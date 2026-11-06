package com.bearinmind.equalizer314.repository

import android.content.Context
import android.util.Log
import com.bearinmind.equalizer314.dsp.AudioDspEngine
import com.bearinmind.equalizer314.model.AutoEqEntry
import com.bearinmind.equalizer314.model.AutoEqProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class AutoEqRepository(private val context: Context) {

    private val tag = "AutoEqRepository"
    private var cachedEntries: List<AutoEqEntry>? = null
    private val gson = Gson()

    suspend fun getEntries(): List<AutoEqEntry> = withContext(Dispatchers.IO) {
        cachedEntries?.let { return@withContext it }

        try {
            context.assets.open("autoeq/index.json").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val type = object : TypeToken<List<AutoEqEntry>>() {}.type
                    val list: List<AutoEqEntry> = gson.fromJson(reader, type)
                    cachedEntries = list
                    list
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to load autoeq index.json: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun search(query: String, typeFilter: String? = null, limit: Int = 50): List<AutoEqEntry> =
        withContext(Dispatchers.Default) {
            val entries = getEntries()
            val trimmedQuery = query.trim().lowercase()

            entries.asSequence()
                .filter { entry ->
                    if (typeFilter != null && !entry.type.equals(typeFilter, ignoreCase = true)) {
                        return@filter false
                    }
                    if (trimmedQuery.isEmpty()) {
                        true
                    } else {
                        entry.name.lowercase().contains(trimmedQuery) ||
                                entry.source.lowercase().contains(trimmedQuery)
                    }
                }
                .take(limit)
                .toList()
        }

    suspend fun loadProfile(entry: AutoEqEntry): AutoEqProfile? = withContext(Dispatchers.IO) {
        try {
            val assetPath = "autoeq/profiles/${entry.path}"
            context.assets.open(assetPath).use { inputStream ->
                val content = inputStream.bufferedReader().use { it.readText() }
                val (preamp, filters) = AudioDspEngine.parseEqualizerApo(content)
                AutoEqProfile(
                    headphoneName = entry.name,
                    source = entry.source,
                    preampDb = preamp,
                    filters = filters
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to load profile for ${entry.name}: ${e.message}", e)
            null
        }
    }
}
