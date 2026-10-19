package com.example.videocamerasource

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.media3.ui.PlayerView

class FloatingService : Service() {

    private lateinit var wm: WindowManager
    private var floatView: View? = null
    private var playerView: PlayerView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startFg()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        val inflater = LayoutInflater.from(this)
        floatView = inflater.inflate(R.layout.floating_window, null)
        playerView = floatView!!.findViewById(R.id.floatPlayer)
        playerView!!.player = PlayerManager.getOrCreate(this)
        playerView!!.useController = true

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            640,
            480,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }

        // Keo di chuyen
        var lastX = 0
        var lastY = 0
        var downX = 0
        var downY = 0
        floatView!!.findViewById<View>(R.id.dragHandle).setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = params.x
                    lastY = params.y
                    downX = e.rawX.toInt()
                    downY = e.rawY.toInt()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = lastX + (e.rawX.toInt() - downX)
                    params.y = lastY + (e.rawY.toInt() - downY)
                    wm.updateViewLayout(floatView, params)
                    true
                }
                else -> false
            }
        }

        floatView!!.findViewById<View>(R.id.btnCloseFloat).setOnClickListener {
            stopSelf()
        }
        floatView!!.findViewById<View>(R.id.btnResize).setOnClickListener {
            // Toggle 2 kich thuoc
            if (params.width > 500) {
                params.width = 420
                params.height = 300
            } else {
                params.width = 640
                params.height = 480
            }
            wm.updateViewLayout(floatView, params)
        }

        wm.addView(floatView, params)
    }

    private fun startFg() {
        val id = "float_play"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(id, "Phat video nen", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
        val noti: Notification = NotificationCompat.Builder(this, id)
            .setContentTitle("Video Camera Source dang chay")
            .setContentText("Cua so noi dang phat lap video")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
        startForeground(1, noti)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            playerView?.player = null
            if (floatView != null) wm.removeView(floatView)
        } catch (_: Exception) {}
    }
}
