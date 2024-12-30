package com.example.buttonstaskerinterface

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TaskerBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Ensure BLEManager is initialized before use
        //BLEManager.initialize(context.applicationContext)  // Initialize the BLEManager with applicationContext
        Log.d("TaskerReceiver", "Got intent here ${intent.data}")
        // Get the data from the broadcast
        val bleValue = intent.getStringExtra("led command")
        Log.d("TaskerReceiver", "$bleValue")

        // Ensure the value is not null before proceeding
        bleValue?.let {
            Log.d("TaskerReceiver", "Received BLE value: $it")

            // Example: Writing received data to the BLE characteristic
            val data = it.toByteArray()  // Convert string to byte array (modify this based on your needs)
            BLEManager.writeToCharacteristic(data)  // Write data to the characteristic via BLEManager
        }
    }
}
