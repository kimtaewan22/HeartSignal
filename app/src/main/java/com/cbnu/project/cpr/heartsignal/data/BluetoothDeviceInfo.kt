package com.cbnu.project.cpr.heartsignal.data

import android.bluetooth.BluetoothDevice

data class BluetoothDeviceInfo(val name: String?, val address: String, val uuids: String?, val device: BluetoothDevice)
