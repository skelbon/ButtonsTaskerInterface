package com.example.buttonstaskerinterface

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PermissionDialog(
    permissionTextProvider: PermissionTextProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGotoAppSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        buttons = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ){
                Divider()
                Text(
                    text = if(isPermanentlyDeclined){
                        "Grant permission"
                    } else {
                        "OK"
                    },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isPermanentlyDeclined) {
                                onGotoAppSettingsClick()
                            } else {
                                onOkClick()
                            }
                        }
                        .padding(16.dp)
                )
            }
        },
        title = {
            Text(text = "Permission required")
        },
        text = {
            Text(
                text = permissionTextProvider.getDescription(
                    isPermanentlyDeclined = isPermanentlyDeclined
                )
            )
        },
        modifier = modifier
    )
}

interface PermissionTextProvider {
    fun getDescription(isPermanentlyDeclined: Boolean): String
}

class BluetoothPermissionProvider: PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
       return if(isPermanentlyDeclined){
           "Bluetooth permission is permanently declined. Enable in App Settings"
       } else {
           "App needs BT permission to function"
       }
    }
}

class BluetoothAdminPermissionProvider: PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined){
            "Bluetooth admin permission is permanently declined. Enable in App Settings"
        } else {
            "App needs BT admin permission to function"
        }
    }
}

class BluetoothScanPermissionProvider: PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined){
            "Bluetooth scan permission is permanently declined. Enable in App Settings"
        } else {
            "App needs BT scan permission to function"
        }
    }
}

class BluetoothConnectPermissionProvider: PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined){
            "Bluetooth connect permission is permanently declined. Enable in App Settings"
        } else {
            "App needs BT connect permission to function"
        }
    }
}


class AccessFineLocationPermissionProvider: PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined){
            "Access fine location permission is permanently declined. Enable in App Settings"
        } else {
            "App needs access fine location permission to function"
        }
    }
}


class ForegroundServicePermissionProvider: PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined){
            "Foreground service permission is permanently declined. Enable in App Settings"
        } else {
            "App needs foreground service permission to function"
        }
    }
}

class NotificationServicePermissionProvider: PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if(isPermanentlyDeclined){
            "Notification service permission is permanently declined. Enable in App Settings"
        } else {
            "App needs notification service permission to function"
        }
    }
}