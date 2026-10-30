package dev.frameforge.core

/**
 * Parses `dumpsys SurfaceFlinger --latency "<layer>"` output: line 1 is the refresh period (ns),
 * then up to 127 rows of "desiredPresent actualPresent frameReady". Rows with
 * Long.MAX_VALUE (or 0) in the present column are not-yet-presented placeholders.
 * NOTE: format taken from AOSP documentation behaviour; verify on target devices.
 */
object LatencyParser {
    data class Result(val refreshNs: Long, val presentNs: List<Long>)

    fun parse(text: String): Result? {
        val lines = text.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        val refresh = lines.firstOrNull()?.toLongOrNull() ?: return null
        val present = lines.drop(1).mapNotNull { l ->
            val p = l.split(Regex("\\s+"))
            if (p.size < 3) null else p[1].toLongOrNull()?.takeIf { it > 0 && it != Long.MAX_VALUE }
        }
        return Result(refresh, present)
    }
}
