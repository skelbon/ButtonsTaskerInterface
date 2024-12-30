package com.example.buttonstaskerinterface

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat

class BLEService : Service() {

    // Instance of your dynamically registered receiver
    private val receiver = TaskerBroadcastReceiver()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("NewApi")
    private fun start() {
        // Initialize the BLEManager with application context
        BLEManager.initialize(applicationContext)

        // Register the receiver dynamically to listen for the broadcast
        val intentFilter = IntentFilter("com.example.buttonstaskerinterface.BLE_INTENT")
        registerReceiver(receiver, intentFilter, RECEIVER_EXPORTED)

        // Set up the foreground service notification
        val notification = NotificationCompat.Builder(this, "Button_Interface_Channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("JP Buttons")
            .setContentText("Button Interface Active")
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unregister the receiver when the service is destroyed to avoid memory leaks
        unregisterReceiver(receiver)
    }

    enum class Actions {
        START, STOP
    }
}
