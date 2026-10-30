package dev.frameforge.core

/** Stats for one window of frame timestamps. Pure Kotlin so it is unit-testable. */
data class SecondStats(
    val fps: Double, val minFps: Double, val maxFps: Double,
    val low1Fps: Double, val low5Fps: Double,
    val medianFrameMs: Double, val frameTimeSdMs: Double, val jankFrames: Int,
)

object FrameStats {
    /** @param presentNs monotonically increasing present timestamps in nanoseconds. */
    fun compute(presentNs: List<Long>, windowNs: Long = 1_000_000_000L): SecondStats? {
        if (presentNs.size < 2) return null
        val deltasMs = presentNs.zipWithNext { a, b -> (b - a) / 1e6 }.filter { it > 0 }
        if (deltasMs.isEmpty()) return null
        val spanNs = (presentNs.last() - presentNs.first()).coerceAtLeast(1)
        val fps = deltasMs.size * 1e9 / spanNs
        val sorted = deltasMs.sortedDescending() // slowest frames first
        fun lowPct(p: Double): Double {
            val n = maxOf(1, (sorted.size * p).toInt())
            return 1000.0 / sorted.take(n).average()
        }
        val mean = deltasMs.average()
        val sd = Math.sqrt(deltasMs.sumOf { (it - mean) * (it - mean) } / deltasMs.size)
        val med = deltasMs.sorted().let { if (it.size % 2 == 1) it[it.size / 2] else (it[it.size / 2 - 1] + it[it.size / 2]) / 2 }
        val jank = deltasMs.count { it > 2 * med }
        return SecondStats(fps, 1000.0 / sorted.first(), 1000.0 / sorted.last(),
            lowPct(0.01), lowPct(0.05), med, sd, jank)
    }
}
