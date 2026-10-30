package dev.frameforge.engine

import dev.frameforge.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class FpsEngine(private val shell: ShellRunner, private val scope: CoroutineScope) {
    private val _stats = MutableStateFlow<SecondStats?>(null)
    val stats: StateFlow<SecondStats?> = _stats
    val history = ArrayDeque<SecondStats>()
    var historyLimit = 600 // configurable, not hardcoded
    private var job: Job? = null
    private var lastPresent = 0L

    fun start(layer: String, intervalMs: Long = 1000) {
        stop(); lastPresent = 0
        job = scope.launch(Dispatchers.IO) {
            while (isActive) {
                runCatching {
                    val r = LatencyParser.parse(shell.exec("dumpsys SurfaceFlinger --latency '$layer'"))
                    val fresh = r?.presentNs?.filter { it > lastPresent }.orEmpty()
                    if (fresh.isNotEmpty()) lastPresent = fresh.last()
                    FrameStats.compute(fresh)?.let {
                        _stats.value = it
                        history.addLast(it); while (history.size > historyLimit) history.removeFirst()
                    }
                }
                delay(intervalMs)
            }
        }
    }
    fun stop() { job?.cancel(); job = null }

    fun exportCsv(): String = buildString {
        appendLine("second,fps,min,max,low1,low5,median_ms,sd_ms,jank")
        history.forEachIndexed { i, s -> appendLine("$i,%.1f,%.1f,%.1f,%.1f,%.1f,%.2f,%.2f,%d".format(
            s.fps, s.minFps, s.maxFps, s.low1Fps, s.low5Fps, s.medianFrameMs, s.frameTimeSdMs, s.jankFrames)) }
    }
}
