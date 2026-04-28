// SPDX-License-Identifier: GPL-3.0-or-later
package dev.lyo.callrec.codec

import kotlin.math.sqrt

/**
 * Running RMS over 16-bit little-endian PCM. Cheap; runs on every pump tick.
 * Drives the live UI meter and the controller's audibility decision via an
 * adaptive noise floor — the first ~500 ms of samples set [calibratedFloor],
 * and [isAudible] is true iff the latest RMS is meaningfully above floor.
 *
 * RMS is normalised to int16 full-scale → returns Float in [0.0, 1.0].
 */
class AudioLevelMeter {

    @Volatile var lastRms: Float = 0f
        private set
    @Volatile var maxRms: Float = 0f
        private set
    @Volatile var totalFrames: Long = 0L
        private set
    @Volatile var silentFrames: Long = 0L
        private set
    /** Median RMS of the warmup window. INITIAL_FLOOR until warmup completes. */
    @Volatile var calibratedFloor: Float = INITIAL_FLOOR
        private set

    private val warmupSamples = ArrayList<Float>(WARMUP_TARGET)
    private var warmupComplete = false

    /** True when the latest RMS sample exceeds the calibrated noise floor by [AUDIBLE_DELTA]. */
    val isAudible: Boolean get() = lastRms > calibratedFloor + AUDIBLE_DELTA

    fun update(buf: ByteArray, off: Int, len: Int) {
        if (len < 2) return
        val frames = len / 2
        var sumSq = 0.0
        var i = off
        val end = off + len
        while (i < end) {
            val low = buf[i].toInt() and 0xFF
            val high = buf[i + 1].toInt()
            val sample = (high shl 8) or low
            val v = sample.toDouble()
            sumSq += v * v
            i += 2
        }
        val rms = (sqrt(sumSq / frames) / FULL_SCALE).toFloat().coerceIn(0f, 1f)
        lastRms = rms
        if (rms > maxRms) maxRms = rms
        totalFrames += frames
        if (rms < SILENCE_FLOOR) silentFrames += frames else silentFrames = 0

        // Warmup: collect first WARMUP_TARGET samples, then lock the median.
        if (!warmupComplete) {
            warmupSamples += rms
            if (warmupSamples.size >= WARMUP_TARGET) {
                val sorted = warmupSamples.sorted()
                calibratedFloor = sorted[sorted.size / 2].coerceAtLeast(INITIAL_FLOOR)
                warmupComplete = true
            }
        }
    }

    fun reset() {
        lastRms = 0f
        maxRms = 0f
        totalFrames = 0L
        silentFrames = 0L
        calibratedFloor = INITIAL_FLOOR
        warmupSamples.clear()
        warmupComplete = false
    }

    fun isSilent(sampleRate: Int, windowMs: Long = 2_000): Boolean =
        silentFrames * 1_000L / sampleRate >= windowMs

    companion object {
        private const val FULL_SCALE = 32_768.0
        /** -60 dBFS — quieter than ambient room noise. */
        private const val SILENCE_FLOOR = 0.001f
        /** Bootstrap floor before warmup completes. */
        private const val INITIAL_FLOOR = 0.001f
        /** ~+6 dB above floor — empirical voice-vs-drift discriminator. */
        private const val AUDIBLE_DELTA = 0.008f
        /** ~500 ms at 60 ms tick cadence. */
        private const val WARMUP_TARGET = 50
    }
}
