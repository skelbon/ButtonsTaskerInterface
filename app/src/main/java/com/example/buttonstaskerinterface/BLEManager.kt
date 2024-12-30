package com.example.buttonstaskerinterface

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.*

private const val TAG = "BLEManager"
private const val SERVICE_UUID = "12345678-1234-1234-1234-123456789abc"
private const val BUTTONSTATE_CHARACTERISTIC_UUID = "abcd1234-5678-90ef-1234-567890abcdef"
private const val LED_COMMAND_CHAR_UUID = "abcd1234-5678-90ef-1234-567890abcdeg"

object BLEManager {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var gatt: BluetoothGatt? = null

    var onValueChanged: ((String) -> Unit)? = null // Callback for value changes

    // Initialize the Bluetooth adapter
    fun initialize(context: Context) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        startScan("ESP32_BLE_Server", context )
    }

    // Start scanning for a specific device
    @SuppressLint("MissingPermission")
    fun startScan(deviceName: String, context: Context) {
        val adapter = bluetoothAdapter ?: return
        val deviceCallback = object : BluetoothAdapter.LeScanCallback {
            @SuppressLint("MissingPermission")
            override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray?) {
                Log.d(TAG, "Found device: ${device.name} - ${device.address}")
                if (device.name == deviceName) {
                    adapter.stopLeScan(this)
                    connectToDevice(device, context)
                }
            }
        }
        adapter.startLeScan(deviceCallback)
    }

    // Connect to the BLE device
    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice, context: Context) {
        gatt = device.connectGatt(context, true, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d(TAG, "Connected to GATT server.")
                    gatt?.discoverServices()
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    Log.d(TAG, "Disconnected from GATT server.")
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                val service = gatt?.getService(UUID.fromString(SERVICE_UUID))
                val buttonStateCharacteristic = service?.getCharacteristic(UUID.fromString(BUTTONSTATE_CHARACTERISTIC_UUID))
                buttonStateCharacteristic?.let {
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
                }
            }
        })
    }

    // Write data to a characteristic
    @SuppressLint("MissingPermission")
    fun writeToCharacteristic(data: ByteArray) {
        val service = gatt?.getService(UUID.fromString(SERVICE_UUID))
        val characteristic = service?.getCharacteristic(UUID.fromString(LED_COMMAND_CHAR_UUID))
        if (characteristic != null && data.size <= 20) {
            characteristic.value = data
            gatt?.writeCharacteristic(characteristic)
            Log.d(TAG, "Data written to characteristic: ${data.contentToString()}")
        } else {
            Log.e(TAG, "Characteristic not found or data size exceeds limit.")
        }
    }

    // Disconnect and clean up resources
    @SuppressLint("MissingPermission")
    fun cleanup() {
        gatt?.close()
        gatt = null
        bluetoothAdapter = null
    }
}

