package com.bearinmind.equalizer314.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.util.Log

class AudioSessionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR_BAD_VALUE)
        val packageName = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME)

        Log.i("AudioSessionReceiver", "Received session broadcast: $action (sessionId=$sessionId, pkg=$packageName)")

        val service = EqService.instance ?: return
        when (action) {
            AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                if (sessionId > 0) {
                    service.dspEngine.attachSession(sessionId, packageName)
                }
            }
            AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> {
                if (sessionId > 0) {
                    service.dspEngine.detachSession(sessionId)
                }
            }
        }
    }
}
