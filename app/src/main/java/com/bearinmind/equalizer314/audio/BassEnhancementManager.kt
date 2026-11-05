package com.bearinmind.equalizer314.audio

import android.media.audiofx.BassBoost
import android.os.Build
import android.util.Log

class BassEnhancementManager {

    companion object {
        private const val TAG = "AudioFXBass"
    }

    private var effect: BassBoost? = null

    @Volatile
    var isActive: Boolean = false
        private set

    @Volatile
    var strength: Int = 350
        private set

    @Synchronized
    fun apply(enabled: Boolean, requestedStrength: Int): Boolean {
        strength = requestedStrength.coerceIn(0, 1000)

        if (!enabled) {
            release()
            return true
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            return false
        }

        try {
            var current = effect

            if (current == null || !current.hasControl()) {
                release()
                current = BassBoost(0, 0)
                effect = current
            }

            current.setStrength(strength.toShort())
            current.enabled = true

            isActive = current.enabled
            Log.i(TAG, "Bass enhancement active=$isActive strength=$strength")

            return isActive
        } catch (e: Exception) {
            Log.e(TAG, "Unable to activate global bass enhancement", e)
            release()
            return false
        }
    }

    @Synchronized
    fun release() {
        try {
            effect?.enabled = false
            effect?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing BassBoost", e)
        } finally {
            effect = null
            isActive = false
        }
    }
}
