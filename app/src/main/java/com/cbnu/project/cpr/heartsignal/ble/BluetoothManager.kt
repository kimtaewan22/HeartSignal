package com.cbnu.project.cpr.heartsignal.ble

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.nio.charset.Charset
import java.util.UUID

class BluetoothManager(private val context: Context) {
    private val TAG = "BluetoothManager"
    private var bluetoothGatt: BluetoothGatt? = null
    var deviceName: String? = ""
    var deviceAddress: String? = ""
    val handler = Handler(Looper.getMainLooper())
    val serviceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")

    fun connectToDevice(device: BluetoothDevice, connectedDeviceName: TextView, connectedDeviceAddr: TextView) {
        // 여기에서 BluetoothGattCallback 및 다른 필요한 변수를 초기화합니다.

        val gattCallback = object : BluetoothGattCallback() {
            // BluetoothGattCallback 메서드를 이전 코드와 동일하게 구현합니다.
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // 연결이 성공적으로 완료된 경우
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        return
                    }
                    gatt?.discoverServices()

                    handler.post {
                        // UI 스레드에서 Toast 띄우기
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return@post
                        }
                        Toast.makeText(context, "연결 성공! : ${gatt?.device?.name} , ${gatt?.device?.address}  ", Toast.LENGTH_SHORT).show()
                        // BluetoothGatt 객체로부터 서비스 목록을 가져옵니다.
                        deviceName = gatt?.device?.name
                        deviceAddress = gatt?.device?.address

                        connectedDeviceName.text = deviceName
                        connectedDeviceAddr.text = deviceAddress

                        val services = gatt?.services
                        if (services != null) {
                            for (service in services) {
                                // 각 서비스에 대한 처리를 수행합니다.
                                // 이곳에서 필요한 작업을 수행합니다.
                            }
                        }

                    }
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    // 연결 성공 후 추가 작업 수행
                    // 예: 서비스 발견을 시작하거나 특성에 대한 구성을 수행합니다.
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // 연결이 해제된 경우
                    handler.post {
                        // UI 스레드에서 Toast 띄우기
                        Toast.makeText(context, "연결 해제", Toast.LENGTH_SHORT).show()
                    }

                    // 연결이 끊어졌으므로 BluetoothGatt를 닫아야 합니다.
                    bluetoothGatt?.close()
                    bluetoothGatt = null
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val services = gatt?.services
                    if (services != null) {
                        for (service in services) {
                            // 원하는 서비스를 찾으면 해당 서비스 내부의 특성을 확인
                            if (service.uuid == serviceUUID) {
                                val characteristics = service.characteristics
                                for (characteristic in characteristics) {
                                    // 특성(UUID) 확인
                                    Log.d(TAG, "Characteristic UUID: ${characteristic.uuid}")

                                    // 원하는 특성(UUID)와 일치하는 경우에만 읽기 작업을 수행
                                    if (characteristic.uuid == UUID.fromString(BluetoothUtils.NOTIFY_UUID)) {
                                        if (ActivityCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.BLUETOOTH_CONNECT
                                            ) != PackageManager.PERMISSION_GRANTED
                                        ) {
                                            // TODO: 권한 요청 처리
                                            return
                                        }

                                        // Notify 활성화
                                        gatt?.setCharacteristicNotification(characteristic, true)

//                                         해당 특성에 대한 디스크립터 설정
                                        val descriptor =
                                            characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                                        descriptor?.value =
                                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

                                        // 디스크립터 설정 적용
                                        gatt?.writeDescriptor(descriptor)

                                        Log.d(
                                            TAG,
                                            "Notify enabled for characteristic: ${characteristic.uuid}"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?
            ) {
                if (characteristic == null) {
                    return
                }
                val data = characteristic.value // 데이터를 바이트 배열로 가져옵니다.
                val decodedData = String(data, Charset.forName("UTF-8")) // 데이터를 디코딩합니다.

                // Local Broadcast Manager를 사용하여 데이터를 CameraFragment로 전송합니다.
                val intent = Intent("BLUETOOTH_DATA_RECEIVED")
                intent.putExtra("data", decodedData)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                Log.d(TAG, "Received data: $decodedData")
            }
        }

        // 여기에서 BluetoothGatt를 초기화하고 연결합니다.
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }
    // 필요한 경우 다른 Bluetooth 관리 메서드를 추가합니다.
}