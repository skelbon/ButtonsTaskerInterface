package com.example.buttonstaskerinterface

import android.app.Application
import android.content.Intent
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {


    private val _bleValue = MutableLiveData<String>("Waiting for BLE value...")
    val bleValue: LiveData<String> get() = _bleValue


    fun startInterface() {
        Intent(getApplication<Application>(), BLEService::class.java).also {
            it.action = BLEService.Actions.START.toString()
            getApplication<Application>().startService(it)
        }
    }

    fun stopInterface() {
        Intent(getApplication<Application>(), BLEService::class.java).also {
            it.action = BLEService.Actions.STOP.toString()
            getApplication<Application>().startService(it)
        }
    }
    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    fun dismissDialog(){
        visiblePermissionDialogQueue.removeFirst()
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ){
        if(!isGranted && !visiblePermissionDialogQueue.contains(permission)){
            visiblePermissionDialogQueue.add(permission)
        }
    }
}

// BLUETOOTH
//BLUETOOTH_ADMIN
//BLUETOOTH_CONNECT
//BLUETOOTH_SCAN
//ACCESS_FINE_LOCATION
//FOREGROUND_SERVICE