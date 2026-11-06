package com.bearinmind.equalizer314.dsp

import com.bearinmind.equalizer314.model.FilterType
import com.bearinmind.equalizer314.model.ParametricFilter
import kotlin.math.*

/**
 * Standard Audio EQ Cookbook digital biquad filter calculations.
 */
object BiquadMath {

    const val SAMPLE_RATE = 48000.0

    class Coefficients(
        val b0: Double, val b1: Double, val b2: Double,
        val a0: Double, val a1: Double, val a2: Double
    ) {
        /**
         * Magnitude response in dB at frequency [f] in Hz.
         */
        fun magnitudeDbAt(f: Double): Double {
            val w = 2.0 * Math.PI * f / SAMPLE_RATE
            val cosW = cos(w)
            val cos2W = cos(2.0 * w)
            val sinW = sin(w)
            val sin2W = sin(2.0 * w)

            val numReal = b0 + b1 * cosW + b2 * cos2W
            val numImag = -(b1 * sinW + b2 * sin2W)

            val denReal = a0 + a1 * cosW + a2 * cos2W
            val denImag = -(a1 * sinW + a2 * sin2W)

            val numMagSq = numReal * numReal + numImag * numImag
            val denMagSq = denReal * denReal + denImag * denImag

            if (denMagSq < 1e-12) return 0.0
            val magRatio = sqrt(numMagSq / denMagSq)
            if (magRatio <= 1e-6) return -120.0
            return 20.0 * log10(magRatio)
        }
    }

    fun computeCoefficients(
        type: FilterType,
        frequency: Double,
        gainDb: Double,
        qFactor: Double,
        sampleRate: Double = SAMPLE_RATE
    ): Coefficients {
        val clampedFreq = frequency.coerceIn(10.0, sampleRate / 2.0 - 100.0)
        val clampedQ = qFactor.coerceIn(0.05, 30.0)
        val w0 = 2.0 * Math.PI * clampedFreq / sampleRate
        val cosW0 = cos(w0)
        val sinW0 = sin(w0)
        val alpha = sinW0 / (2.0 * clampedQ)
        val a = 10.0.pow(gainDb / 40.0)

        return when (type) {
            FilterType.PEAK -> {
                val b0 = 1.0 + alpha * a
                val b1 = -2.0 * cosW0
                val b2 = 1.0 - alpha * a
                val a0 = 1.0 + alpha / a
                val a1 = -2.0 * cosW0
                val a2 = 1.0 - alpha / a
                Coefficients(b0, b1, b2, a0, a1, a2)
            }
            FilterType.LOW_SHELF -> {
                val sqrtA = sqrt(a)
                val b0 = a * ((a + 1.0) - (a - 1.0) * cosW0 + 2.0 * sqrtA * alpha)
                val b1 = 2.0 * a * ((a - 1.0) - (a + 1.0) * cosW0)
                val b2 = a * ((a + 1.0) - (a - 1.0) * cosW0 - 2.0 * sqrtA * alpha)
                val a0 = (a + 1.0) + (a - 1.0) * cosW0 + 2.0 * sqrtA * alpha
                val a1 = -2.0 * ((a - 1.0) + (a + 1.0) * cosW0)
                val a2 = (a + 1.0) + (a - 1.0) * cosW0 - 2.0 * sqrtA * alpha
                Coefficients(b0, b1, b2, a0, a1, a2)
            }
            FilterType.HIGH_SHELF -> {
                val sqrtA = sqrt(a)
                val b0 = a * ((a + 1.0) + (a - 1.0) * cosW0 + 2.0 * sqrtA * alpha)
                val b1 = -2.0 * a * ((a - 1.0) + (a + 1.0) * cosW0)
                val b2 = a * ((a + 1.0) + (a - 1.0) * cosW0 - 2.0 * sqrtA * alpha)
                val a0 = (a + 1.0) - (a - 1.0) * cosW0 + 2.0 * sqrtA * alpha
                val a1 = 2.0 * ((a - 1.0) - (a + 1.0) * cosW0)
                val a2 = (a + 1.0) - (a - 1.0) * cosW0 - 2.0 * sqrtA * alpha
                Coefficients(b0, b1, b2, a0, a1, a2)
            }
            FilterType.LOW_PASS -> {
                val b0 = (1.0 - cosW0) / 2.0
                val b1 = 1.0 - cosW0
                val b2 = (1.0 - cosW0) / 2.0
                val a0 = 1.0 + alpha
                val a1 = -2.0 * cosW0
                val a2 = 1.0 - alpha
                Coefficients(b0, b1, b2, a0, a1, a2)
            }
            FilterType.HIGH_PASS -> {
                val b0 = (1.0 + cosW0) / 2.0
                val b1 = -(1.0 + cosW0)
                val b2 = (1.0 + cosW0) / 2.0
                val a0 = 1.0 + alpha
                val a1 = -2.0 * cosW0
                val a2 = 1.0 - alpha
                Coefficients(b0, b1, b2, a0, a1, a2)
            }
            FilterType.NOTCH -> {
                val b0 = 1.0
                val b1 = -2.0 * cosW0
                val b2 = 1.0
                val a0 = 1.0 + alpha
                val a1 = -2.0 * cosW0
                val a2 = 1.0 - alpha
                Coefficients(b0, b1, b2, a0, a1, a2)
            }
        }
    }

    /**
     * Evaluates total dB response for a set of parametric filters, preamp, and optional tonal tilt.
     */
    fun evaluateParametricCurve(
        filters: List<ParametricFilter>,
        preampDb: Float,
        tonalTiltDb: Float = 0f,
        points: Int = 120,
        minFreq: Float = 20f,
        maxFreq: Float = 20000f
    ): List<Pair<Float, Float>> {
        val coeffs = filters.filter { it.enabled }.map {
            computeCoefficients(it.type, it.frequency.toDouble(), it.gainDb.toDouble(), it.qFactor.toDouble())
        }

        val minLog = log10(minFreq.toDouble())
        val maxLog = log10(maxFreq.toDouble())
        val step = (maxLog - minLog) / (points - 1)

        val result = ArrayList<Pair<Float, Float>>(points)
        for (i in 0 until points) {
            val freq = 10.0.pow(minLog + i * step)
            var totalDb = preampDb.toDouble()

            // Apply tonal tilt around 1 kHz if non-zero
            if (abs(tonalTiltDb) > 0.05f) {
                val octavesFrom1k = log2(freq / 1000.0)
                totalDb += (tonalTiltDb / 4.0) * octavesFrom1k.coerceIn(-4.0, 4.0)
            }

            for (c in coeffs) {
                totalDb += c.magnitudeDbAt(freq)
            }
            result.add(Pair(freq.toFloat(), totalDb.coerceIn(-30.0, 30.0).toFloat()))
        }
        return result
    }

    /**
     * Evaluates total dB response for graphic EQ gains at ISO frequencies.
     */
    fun evaluateGraphicCurve(
        frequencies: List<Float>,
        gainsDb: List<Float>,
        preampDb: Float,
        tonalTiltDb: Float = 0f,
        qFactor: Double = 1.414,
        points: Int = 120
    ): List<Pair<Float, Float>> {
        val coeffs = frequencies.zip(gainsDb).mapNotNull { (freq, gain) ->
            if (abs(gain) < 0.05f) null
            else computeCoefficients(FilterType.PEAK, freq.toDouble(), gain.toDouble(), qFactor)
        }

        val minLog = log10(20.0)
        val maxLog = log10(20000.0)
        val step = (maxLog - minLog) / (points - 1)

        val result = ArrayList<Pair<Float, Float>>(points)
        for (i in 0 until points) {
            val freq = 10.0.pow(minLog + i * step)
            var totalDb = preampDb.toDouble()

            if (abs(tonalTiltDb) > 0.05f) {
                val octavesFrom1k = log2(freq / 1000.0)
                totalDb += (tonalTiltDb / 4.0) * octavesFrom1k.coerceIn(-4.0, 4.0)
            }

            for (c in coeffs) {
                totalDb += c.magnitudeDbAt(freq)
            }
            result.add(Pair(freq.toFloat(), totalDb.coerceIn(-30.0, 30.0).toFloat()))
        }
        return result
    }
}
