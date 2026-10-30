package dev.frameforge.overlay

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.IBinder
import android.view.*
import android.widget.TextView
import dev.frameforge.engine.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class OverlayService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var wm: WindowManager
    private var view: TextView? = null

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ch = "overlay"
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel(ch, "Overlay", NotificationManager.IMPORTANCE_LOW))
        startForeground(1, Notification.Builder(this, ch).setContentTitle("FrameForge overlay running")
            .setSmallIcon(android.R.drawable.ic_menu_info_details).build())
        val layer = intent?.getStringExtra("layer") ?: return START_NOT_STICKY
        wm = getSystemService(WindowManager::class.java)
        val tv = TextView(this).apply {
            setTextColor(0xFF00FF88.toInt()); textSize = 16f; typeface = Typeface.MONOSPACE
            setBackgroundColor(0x99000000.toInt()); setPadding(16, 8, 16, 8); text = "-- FPS"
        }
        val lp = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = 24; y = 96 }
        tv.setOnTouchListener(object : View.OnTouchListener { // drag to move
            var dx = 0f; var dy = 0f
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> { dx = e.rawX - lp.x; dy = e.rawY - lp.y }
                    MotionEvent.ACTION_MOVE -> { lp.x = (e.rawX - dx).toInt(); lp.y = (e.rawY - dy).toInt(); wm.updateViewLayout(v, lp) }
                }; return true
            }
        })
        wm.addView(tv, lp); view = tv
        val engine = FpsEngine(ShizukuShellRunner(this), CoroutineScope(SupervisorJob() + Dispatchers.Default))
        engine.start(layer)
        scope.launch { engine.stats.collect { s -> s?.let { tv.text = "%.0f FPS\n1%%low %.0f".format(it.fps, it.low1Fps) } } }
        return START_NOT_STICKY
    }

    override fun onDestroy() { view?.let { wm.removeView(it) }; scope.cancel() }
}
