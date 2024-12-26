package com.example.buttonstaskerinterface

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.buttonstaskerinterface.ui.theme.ButtonsTaskerInterfaceTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private lateinit var bleManager: BLEManager
    private var bleValue by mutableStateOf("Waiting for BLE value...")


    private val permissionsToRequest = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.POST_NOTIFICATIONS

    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bleManager = BLEManager(applicationContext).apply {
            onValueChanged = { value ->
                // Update the state whenever the BLE value changes
                bleValue = value
            }
        }
        setContent {
            ButtonsTaskerInterfaceTheme {
                val viewModel = viewModel<MainViewModel>()
                val dialogQueue = viewModel.visiblePermissionDialogQueue

                val permissionsResultLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = { perms ->
                        permissionsToRequest.forEach{ permission ->
                            viewModel.onPermissionResult(
                                permission = permission,
                                isGranted = perms[permission] == true
                            )
                        }
                    }
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        permissionsResultLauncher.launch(
                            permissionsToRequest
                        )
                    }) {
                        Text(text = "request permissions")
                    }
                    Divider()
                    Button(onClick = {
                        Intent(applicationContext, BLEService::class.java).also {
                            it.action = BLEService.Actions.START.toString()
                            startService(it)
                            bleManager.startScan("ESP32_BLE_Server")
                        }
                    }){
                        Text("Start the interface")
                    }
                    Divider()
                    Text("Current BLE Value: $bleValue")

                    Spacer(modifier = Modifier.height(16.dp))


                    Button(onClick = {
                        Intent(applicationContext, BLEService::class.java).also {
                            it.action = BLEService.Actions.STOP.toString()
                            startService(it)
                            bleManager.disconnect()
                        }
                    }){
                        Text("Stop the interface")
                    }
                }

                dialogQueue
                    .reversed()
                    .forEach { permission ->
                        PermissionDialog(
                            permissionTextProvider = when (permission) {
                                Manifest.permission.BLUETOOTH -> {
                                    BluetoothPermissionProvider()
                                }
                                Manifest.permission.BLUETOOTH_ADMIN -> {
                                    BluetoothAdminPermissionProvider()
                                }

                                Manifest.permission.BLUETOOTH_SCAN -> {
                                    BluetoothScanPermissionProvider()
                                }

                                Manifest.permission.BLUETOOTH_CONNECT -> {
                                    BluetoothConnectPermissionProvider()
                                }

                                Manifest.permission.ACCESS_FINE_LOCATION -> {
                                    AccessFineLocationPermissionProvider()
                                }

                                Manifest.permission.FOREGROUND_SERVICE -> {
                                    ForegroundServicePermissionProvider()
                                }
                                else -> return@forEach
                            },
                            isPermanentlyDeclined =  !shouldShowRequestPermissionRationale(
                                permission
                            ),
                            onDismiss = viewModel::dismissDialog,
                            onOkClick = {
                                viewModel.dismissDialog()
                                permissionsResultLauncher.launch(
                                    arrayOf(permission)
                                )
                            },
                            onGotoAppSettingsClick = ::openAppSettings,
                        )
                    }
            }
        }
    }
}

fun Activity.openAppSettings(){
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}