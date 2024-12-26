package com.example.buttonstaskerinterface



import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

private const val TAG = "BLEManager"
private const val SERVICE_UUID = "12345678-1234-1234-1234-123456789abc"
private const val CHARACTERISTIC_UUID = "abcd1234-5678-90ef-1234-567890abcdef"

class BLEManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var gatt: BluetoothGatt? = null
    var onValueChanged: ((String) -> Unit)? = null // Callback for value changes

    @SuppressLint("MissingPermission")
    fun startScan(deviceName: String) {
        val deviceCallback = object : BluetoothAdapter.LeScanCallback {
            @SuppressLint("MissingPermission")
            override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray?) {
                Log.d(TAG, "Found device: ${device.name} - ${device.address}")
                if (device.name == deviceName) {
                    bluetoothAdapter.stopLeScan(this)
                    connectToDevice(device)
                }
            }
        }

        bluetoothAdapter.startLeScan(deviceCallback)
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        gatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothGatt.STATE_DISCONNECTED){
                    Log.d(TAG, "Connection lost - restarting scan")
                    startScan("ESP32_BLE_Server")
                }

                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d(TAG, "Connected to GATT server.")
                    gatt?.discoverServices()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                val service = gatt?.getService(UUID.fromString(SERVICE_UUID))
                val characteristic = service?.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID))
                characteristic?.let {
                    gatt.setCharacteristicNotification(it, true)
                    val descriptor = it.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                    Log.d(TAG, "Subscribed to characteristic notifications.")
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                val value = characteristic?.getStringValue(0)
                value?.let {
                    Log.d(TAG, "Characteristic value updated: $value")
                    onValueChanged?.invoke(it)
                    sendIntentToTasker(it)
                }
            }
        })
    }

    private fun sendIntentToTasker(value: String) {
        val intent = Intent("com.example.ACTION_BLE_UPDATE").apply {
            putExtra("ble_value", value)
        }
        context.sendBroadcast(intent)
        Log.d(TAG, "Broadcast sent to Tasker with value: $value")
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        gatt?.close()
        gatt = null
    }
}
