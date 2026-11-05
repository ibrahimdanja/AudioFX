package com.bearinmind.equalizer314

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import com.bearinmind.equalizer314.audio.EqService

class BassEnhancementActivity : Activity() {

    private val preferences by lazy {
        getSharedPreferences("audiofx_bass", MODE_PRIVATE)
    }

    private var enabled = false
    private var strength = 350
    private lateinit var strengthLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enabled = preferences.getBoolean("enabled", false)
        strength = preferences.getInt("strength", 350).coerceIn(0, 1000)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 32, 28, 28)
            setBackgroundColor(Color.rgb(15, 19, 27))
        }

        val title = TextView(this).apply {
            text = "Bass Enhancement"
            textSize = 26f
            setTextColor(Color.WHITE)
        }
        root.addView(title)

        val description = TextView(this).apply {
            text = "Low-frequency enhancement using Android's native audio effect."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 12, 0, 28)
        }
        root.addView(description)

        val toggle = Switch(this).apply {
            text = "Enable bass enhancement"
            textSize = 16f
            setTextColor(Color.WHITE)
            isChecked = enabled
        }
        root.addView(toggle)

        strengthLabel = TextView(this).apply {
            textSize = 17f
            setTextColor(Color.WHITE)
            setPadding(0, 36, 0, 12)
        }
        root.addView(strengthLabel)
        updateStrengthLabel()

        val slider = SeekBar(this).apply {
            max = 1000
            progress = strength
        }
        root.addView(slider)

        val note = TextView(this).apply {
            text = "Strength is adjustable from 0 to 1000. Actual output depends on device audio-effect support."
            textSize = 13f
            setTextColor(Color.LTGRAY)
            setPadding(0, 24, 0, 24)
        }
        root.addView(note)

        val applyButton = Button(this).apply {
            text = "Apply settings"
        }
        root.addView(applyButton)

        toggle.setOnCheckedChangeListener { _, checked ->
            enabled = checked
            saveAndApply()
        }

        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                strength = progress
                updateStrengthLabel()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                saveAndApply()
            }
        })

        applyButton.setOnClickListener {
            saveAndApply()
        }

        setContentView(root)

        // Reapply saved settings when the screen is opened.
        sendSettings()
    }

    private fun updateStrengthLabel() {
        strengthLabel.text = "Enhancement strength: $strength / 1000"
    }

    private fun saveAndApply() {
        preferences.edit()
            .putBoolean("enabled", enabled)
            .putInt("strength", strength)
            .apply()

        sendSettings()
    }

    private fun sendSettings() {
        val intent = Intent(this, EqService::class.java).apply {
            action = EqService.ACTION_APPLY_BASS_ENHANCEMENT
            putExtra("bass_enabled", enabled)
            putExtra("bass_strength", strength)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            android.util.Log.e("AudioFXBassUI", "Could not update audio service", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
