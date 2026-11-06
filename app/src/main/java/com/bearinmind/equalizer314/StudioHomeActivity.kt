package com.bearinmind.equalizer314

import com.bearinmind.equalizer314.audio.EqService

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class StudioHomeActivity : AppCompatActivity() {

    private val backgroundColor = Color.rgb(13, 17, 27)
    private val surfaceColor = Color.rgb(24, 31, 45)
    private val surfaceRaised = Color.rgb(31, 40, 57)
    private val accentColor = Color.rgb(105, 132, 255)
    private val secondaryColor = Color.rgb(87, 207, 183)
    private val primaryText = Color.rgb(240, 243, 250)
    private val secondaryText = Color.rgb(160, 172, 193)

    private var engineStatus: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor
        window.decorView.systemUiVisibility = 0

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(backgroundColor)
        }

        root.addView(createHeader())

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            setPadding(dp(18), dp(8), dp(18), dp(20))
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(content)

        content.addView(createIntro())
        content.addView(createEngineCard())
        content.addView(sectionHeading("PROCESSING"))
        content.addView(moduleCard(
            "Equalizer",
            "Parametric, graphic and simple EQ controls",
            "OPEN EQUALIZER",
            MainActivity::class.java,
            accentColor
        ))
        content.addView(moduleCard(
            "Effects pipeline",
            "View and manage the available audio processing chain",
            "OPEN PIPELINE",
            AudioEffectsPipelineActivity::class.java,
            secondaryColor
        ))

        content.addView(sectionHeading("DYNAMICS AND SPACE"))
        content.addView(moduleCard(
            "Multiband compressor",
            "Compression, gating and expansion controls",
            "OPEN COMPRESSOR",
            MbcActivity::class.java,
            accentColor
        ))
        content.addView(moduleCard(
            "Limiter",
            "Configure the existing output limiter",
            "OPEN LIMITER",
            LimiterActivity::class.java,
            secondaryColor
        ))
        content.addView(moduleCard(
            "Environmental reverb",
            "Available Android environmental reverb controls",
            "OPEN REVERB",
            EnvironmentalReverbActivity::class.java,
            accentColor
        ))
        content.addView(moduleCard(
            "Bass enhancement",
            "Low-frequency enhancement using Android BassBoost",
            "OPEN BASS ENHANCEMENT",
            BassEnhancementActivity::class.java,
            secondaryColor
        ))

        content.addView(sectionHeading("ANALYSIS AND LIBRARY"))
        content.addView(moduleCard(
            "Spectrum analyser",
            "Open the existing spectrum and visualisation settings",
            "OPEN ANALYSER",
            SpectrumControlActivity::class.java,
            secondaryColor
        ))
        content.addView(moduleCard(
            "Presets",
            "Manage and organise your saved EQ presets",
            "OPEN PRESETS",
            CustomPresetsActivity::class.java,
            accentColor
        ))

        content.addView(sectionHeading("APPLICATION"))
        content.addView(moduleCard(
            "Audio settings",
            "Output, routing and additional audio configuration",
            "OPEN SETTINGS",
            AudioMiscSettingsActivity::class.java,
            secondaryColor
        ))

        content.addView(TextView(this).apply {
            text = "AudioFX Studio  •  Built on the Equalizer314 audio engine"
            textSize = 11f
            setTextColor(secondaryText)
            gravity = Gravity.CENTER
            setPadding(0, dp(22), 0, dp(12))
        })

        root.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        root.addView(createBottomNavigation())

        setContentView(root)
        refreshEngineStatus()
    }

    override fun onResume() {
        super.onResume()
        refreshEngineStatus()
    }

    private fun refreshEngineStatus() {
        val running = EqService.isDpRunning
        engineStatus?.text = if (running) {
            "Dynamics Processing engine is running"
        } else {
            "Dynamics Processing engine is not currently running"
        }
        engineStatus?.setTextColor(if (running) secondaryColor else secondaryText)
    }

    private fun createHeader(): View {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(17), dp(16), dp(12))
        }

        val titleGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        titleGroup.addView(TextView(this).apply {
            text = "AUDIOFX"
            textSize = 11f
            letterSpacing = 0.20f
            setTextColor(secondaryColor)
            setTypeface(null, Typeface.BOLD)
        })

        titleGroup.addView(TextView(this).apply {
            text = "Studio"
            textSize = 27f
            setTextColor(primaryText)
            setTypeface(null, Typeface.BOLD)
        })

        header.addView(titleGroup, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(actionButton("SETTINGS") {
            open(AudioMiscSettingsActivity::class.java)
        })
        return header
    }

    private fun createIntro(): View {
        val group = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(17))
        }

        group.addView(TextView(this).apply {
            text = "Your audio workspace"
            textSize = 21f
            setTextColor(primaryText)
            setTypeface(null, Typeface.BOLD)
        })

        group.addView(TextView(this).apply {
            text = "Control, shape and monitor your audio processing."
            textSize = 13f
            setTextColor(secondaryText)
            setPadding(0, dp(5), 0, 0)
        })

        return group
    }

    private fun createEngineCard(): View {
        val card = MaterialCardView(this).apply {
            radius = dp(19).toFloat()
            cardElevation = 0f
            strokeWidth = dp(1)
            strokeColor = Color.rgb(54, 69, 101)
            setCardBackgroundColor(surfaceRaised)
            useCompatPadding = false
        }

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(17), dp(18), dp(17))
        }

        body.addView(TextView(this).apply {
            text = "AUDIO ENGINE"
            textSize = 10f
            letterSpacing = 0.15f
            setTextColor(secondaryText)
            setTypeface(null, Typeface.BOLD)
        })

        engineStatus = TextView(this).apply {
            text = "Checking engine status..."
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(8), 0, dp(12))
        }
        body.addView(engineStatus)

        body.addView(TextView(this).apply {
            text = "Processing availability depends on Android audio sessions, the active player and device compatibility."
            textSize = 12f
            setTextColor(secondaryText)
        })

        val button = actionButton("OPEN EQUALIZER") {
            open(MainActivity::class.java)
        }
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accentColor))
        button.setTextColor(Color.WHITE)

        body.addView(button, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(46)
        ).apply {
            topMargin = dp(16)
        })

        card.addView(body)
        return card.withMargins(bottom = 8)
    }

    private fun sectionHeading(label: String): View {
        return TextView(this).apply {
            text = label
            textSize = 10f
            letterSpacing = 0.16f
            setTextColor(secondaryColor)
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(2), dp(20), 0, dp(10))
        }
    }

    private fun moduleCard(
        title: String,
        description: String,
        action: String,
        destination: Class<out android.app.Activity>,
        accent: Int
    ): View {
        val card = MaterialCardView(this).apply {
            radius = dp(16).toFloat()
            cardElevation = 0f
            strokeWidth = dp(1)
            strokeColor = Color.rgb(43, 53, 72)
            setCardBackgroundColor(surfaceColor)
            isClickable = true
            isFocusable = true
        }

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(14))
        }

        val heading = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val marker = View(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(accent)
            }
        }
        heading.addView(marker, LinearLayout.LayoutParams(dp(8), dp(8)))

        heading.addView(TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(primaryText)
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(10), 0, 0, 0)
        })

        body.addView(heading)

        body.addView(TextView(this).apply {
            text = description
            textSize = 12f
            setTextColor(secondaryText)
            setPadding(dp(18), dp(7), 0, dp(12))
        })

        val button = actionButton(action) {
            open(destination)
        }
        button.setTextColor(accent)
        button.strokeColor = android.content.res.ColorStateList.valueOf(accent)
        body.addView(button, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(40)
        ))

        card.addView(body)
        card.setOnClickListener { open(destination) }
        return card.withMargins(bottom = 9)
    }

    private fun createBottomNavigation(): View {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(7), dp(8), dp(8))
            setBackgroundColor(Color.rgb(19, 24, 36))
        }

        val items = listOf(
            "HOME" to null,
            "EFFECTS" to AudioEffectsPipelineActivity::class.java,
            "EQ" to MainActivity::class.java,
            "ANALYSE" to SpectrumControlActivity::class.java,
            "PRESETS" to CustomPresetsActivity::class.java
        )

        items.forEach { (label, destination) ->
            val button = MaterialButton(
                this, null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle
            ).apply {
                text = label
                textSize = 9f
                isAllCaps = false
                insetTop = 0
                insetBottom = 0
                minHeight = dp(43)
                minWidth = 0
                setPadding(dp(2), 0, dp(2), 0)
                setTextColor(if (destination == null) secondaryColor else secondaryText)
                strokeWidth = 0
                backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.TRANSPARENT)
                setOnClickListener {
                    if (destination != null) open(destination)
                }
            }
            bar.addView(button, LinearLayout.LayoutParams(0, dp(45), 1f))
        }

        return bar
    }

    private fun actionButton(label: String, action: () -> Unit): MaterialButton {
        return MaterialButton(
            this, null,
            com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply {
            text = label
            textSize = 10f
            isAllCaps = false
            minHeight = dp(40)
            minWidth = 0
            insetTop = 0
            insetBottom = 0
            setTextColor(accentColor)
            strokeColor =
                android.content.res.ColorStateList.valueOf(Color.rgb(65, 82, 117))
            strokeWidth = dp(1)
            cornerRadius = dp(10)
            setOnClickListener { action() }
        }
    }

    private fun open(destination: Class<out android.app.Activity>) {
        startActivity(Intent(this, destination))
    }

    private fun View.withMargins(
        top: Int = 0,
        bottom: Int = 0
    ): View {
        return this.also {
            it.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(top)
                bottomMargin = dp(bottom)
            }
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
