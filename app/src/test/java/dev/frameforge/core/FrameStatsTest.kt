package dev.frameforge.core
import org.junit.Assert.*
import org.junit.Test

class FrameStatsTest {
    @Test fun steady60fps() {
        val ts = (0..60).map { it * 16_666_667L }
        assertEquals(60.0, FrameStats.compute(ts)!!.fps, 0.5)
    }
    @Test fun lowsReflectStutter() {
        val ts = mutableListOf(0L); repeat(99) { ts += ts.last() + 16_666_667L }; ts += ts.last() + 100_000_000L
        val s = FrameStats.compute(ts)!!
        assertTrue(s.low1Fps < 15); assertEquals(1, s.jankFrames)
    }
    @Test fun parserSkipsPlaceholders() {
        val r = LatencyParser.parse("16666667\n1 100 200\n2 9223372036854775807 300\n3 300 400\n")!!
        assertEquals(listOf(100L, 300L), r.presentNs)
    }
}
