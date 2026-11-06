package com.bearinmind.equalizer314.dsp

import com.bearinmind.equalizer314.model.FilterType
import com.bearinmind.equalizer314.model.ParametricFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class BiquadMathTest {

    @Test
    fun testPeakingFilterGainAtCenterFrequency() {
        val centerFreq = 1000.0
        val targetGainDb = 6.0
        val q = 1.414

        val coeff = BiquadMath.computeCoefficients(
            type = FilterType.PEAK,
            frequency = centerFreq,
            gainDb = targetGainDb,
            qFactor = q
        )

        val computedGain = coeff.magnitudeDbAt(centerFreq)
        assertTrue(
            "Expected center frequency gain to be approx $targetGainDb dB, got $computedGain",
            abs(computedGain - targetGainDb) < 0.1
        )
    }

    @Test
    fun testEqualizerApoExportAndParse() {
        val filters = listOf(
            ParametricFilter(1, true, FilterType.LOW_SHELF, 105f, 2.5f, 0.71f),
            ParametricFilter(2, true, FilterType.PEAK, 1000f, -3.0f, 1.41f),
            ParametricFilter(3, true, FilterType.HIGH_SHELF, 10000f, 4.0f, 0.71f)
        )
        val preamp = -4.5f

        val apoText = AudioDspEngine.exportToEqualizerApo(preamp, filters)
        val (parsedPreamp, parsedFilters) = AudioDspEngine.parseEqualizerApo(apoText)

        assertEquals(preamp, parsedPreamp, 0.01f)
        assertEquals(3, parsedFilters.size)
        assertEquals(FilterType.LOW_SHELF, parsedFilters[0].type)
        assertEquals(105f, parsedFilters[0].frequency, 1.0f)
        assertEquals(2.5f, parsedFilters[0].gainDb, 0.1f)
        assertEquals(1000f, parsedFilters[1].frequency, 1.0f)
        assertEquals(-3.0f, parsedFilters[1].gainDb, 0.1f)
    }
}
