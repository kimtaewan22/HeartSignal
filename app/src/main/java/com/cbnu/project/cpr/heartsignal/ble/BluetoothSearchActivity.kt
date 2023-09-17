package com.cbnu.project.cpr.heartsignal.ble


import android.Manifest
import android.animation.Animator
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cbnu.project.cpr.heartsignal.PoseLandmarkerHelper.Companion.TAG
import com.cbnu.project.cpr.heartsignal.adapter.BluetoothDeviceListAdapter
import com.cbnu.project.cpr.heartsignal.ble.BluetoothUtils.Companion.findResponseCharacteristic
import com.cbnu.project.cpr.heartsignal.data.BluetoothDeviceInfo
import com.cbnu.project.cpr.heartsignal.databinding.ActivityBluetoothSearchBinding
import com.cbnu.project.cpr.heartsignal.fragment.CameraFragment
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.github.angads25.toggle.model.ToggleableView
import com.github.angads25.toggle.widget.LabeledSwitch
import java.nio.charset.Charset
import java.util.Random
import java.util.UUID


class BluetoothSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBluetoothSearchBinding

    private val mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper())
    private val adapter = BluetoothAdapter.getDefaultAdapter()
    private var scanning = false
    private val scanResults = mutableListOf<BluetoothDevice>()

    private lateinit var bleRecyclerView: RecyclerView
    private lateinit var bluetoothDeviceListAdapter: BluetoothDeviceListAdapter
    private val deviceList = mutableListOf<BluetoothDeviceInfo>()


    private lateinit var bluetoothManager: BluetoothManager

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val address = device.address
            if (address == "D4:9D:65:20:C1:4D" && !scanResults.contains(device)) {
                // "aa" 주소를 가진 기기만 추가 -> 오렌지보드 address값으로 변경
                scanResults.add(device)
                deviceList.add(BluetoothDeviceInfo(device?.name ?: "NULL", address,
                    device.uuids?.toString() ?: "NULL", device
                ))
                bluetoothDeviceListAdapter.notifyDataSetChanged()
                if (ActivityCompat.checkSelfPermission(
                        this@BluetoothSearchActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
//                Log.d(TAG, "Scanned Device: ${device?.name} - $address - ${device.uuids}")

            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error code: $errorCode")
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bluetooth 스캔 권한 확인 및 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 1)
        } else {
//            // 권한이 이미 부여되어 있는 경우 블루투스 스캔을 시작하십시오.s
//            startScan()
        }

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.e(TAG, "BLE not supported")
        } else {
//            startScan()
        }
        bluetoothManager = BluetoothManager(this)

        // 리사이클러뷰 초기화
        bleRecyclerView = binding.scannedBleRecyclerView // 리사이클러뷰 ID 변경 필요
        // BluetoothDeviceListAdapter 초기화 시 클릭 리스너 전달
        bluetoothDeviceListAdapter = BluetoothDeviceListAdapter(deviceList) { device ->
            bluetoothManager.connectToDevice(device, binding.connectedDeviceName, binding.connectedDeviceAddr)
        }
        bleRecyclerView.adapter = bluetoothDeviceListAdapter

        bleRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.scan.labelOff = " SCAN "
        binding.scan.labelOn = " STOP "

        binding.scan.setOnToggledListener(object : OnToggledListener {
            override fun onSwitched(toggleableView: ToggleableView?, isOn: Boolean) {
                if (isOn) {
                    // 버튼이 켜질 때 실행할 작업
                    startScan()
                    showLottieAnimation()
                } else {
                    // 버튼이 꺼질 때 실행할 작업
                    hideLottieAnimation()
                    val scanner = adapter.bluetoothLeScanner
                    scanning = false
                    if (ActivityCompat.checkSelfPermission(
                            this@BluetoothSearchActivity,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    scanner.stopScan(scanCallback)
                }
            }
        })
    }

//    private fun connectToDevice(device: BluetoothDevice) {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Bluetooth 연결 권한이 없는 경우 권한을 요청합니다.
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
//                2 // 요청 코드, 필요에 따라 변경 가능
//            )
//            return
//        }
//
//
//        // BluetoothGattCallback을 사용하여 BluetoothGatt 객체를 초기화하고 연결을 시도합니다.
//        val gattCallback = object : BluetoothGattCallback() {
//            // 원하는 서비스 UUID
//            val serviceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
//            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
//                if (newState == BluetoothProfile.STATE_CONNECTED) {
//                    // 연결이 성공적으로 완료된 경우
//                    if (ActivityCompat.checkSelfPermission(
//                            this@BluetoothSearchActivity,
//                            Manifest.permission.BLUETOOTH_CONNECT
//                        ) != PackageManager.PERMISSION_GRANTED
//                    ) {
//
//                        return
//                    }
//                    gatt?.discoverServices()
//                    // 연결된 블루투스 장치의 주소를 SharedPreferences에 저장
//                    val deviceAddress = gatt?.device?.address
//                    if (deviceAddress != null) {
//                        bluetoothPrefs.edit().putString(bluetoothDeviceAddressKey, deviceAddress).apply()
//                    }
//
//                    runOnUiThread {
//                        // UI 스레드에서 Toast 띄우기
//                        if (ActivityCompat.checkSelfPermission(
//                                this@BluetoothSearchActivity,
//                                Manifest.permission.BLUETOOTH_CONNECT
//                            ) != PackageManager.PERMISSION_GRANTED
//                        ) {
//                            return@runOnUiThread
//                        }
//                        Toast.makeText(this@BluetoothSearchActivity, "연결 성공! : ${gatt?.device?.name} , ${gatt?.device?.address}  ", Toast.LENGTH_SHORT).show()
//                        // BluetoothGatt 객체로부터 서비스 목록을 가져옵니다.
//                        binding.connectedDeviceName.text = gatt?.device?.name
//                        binding.connectedDeviceAddr.text = gatt?.device?.address
//
//
//                        val services = gatt?.services
//                        if (services != null) {
//                            for (service in services) {
//                                // 각 서비스에 대한 처리를 수행합니다.
//                                Log.d(TAG, "Service UUID: ${service.uuid}")
//                                // 이곳에서 필요한 작업을 수행합니다.
//                            }
//                        }
//
//                    }
//                    if (ActivityCompat.checkSelfPermission(
//                            this@BluetoothSearchActivity,
//                            Manifest.permission.BLUETOOTH_CONNECT
//                        ) != PackageManager.PERMISSION_GRANTED
//                    ) {
//                        return
//                    }
//                    // 연결 성공 후 추가 작업 수행
//                    // 예: 서비스 발견을 시작하거나 특성에 대한 구성을 수행합니다.
//                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                    // 연결이 해제된 경우
//                    runOnUiThread {
//                        // UI 스레드에서 Toast 띄우기
//                        Toast.makeText(this@BluetoothSearchActivity, "연결 해제", Toast.LENGTH_SHORT).show()
//                    }
//
//                    // 연결이 끊어졌으므로 BluetoothGatt를 닫아야 합니다.
//                    bluetoothGatt?.close()
//                    bluetoothGatt = null
//
//                    Log.d(TAG, "연결 해제")
//                }
//
//
//            }
//
//            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
//                Log.d(TAG, "Service status: $status")
//
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    val services = gatt?.services
//                    if (services != null) {
//                        for (service in services) {
//                            // 원하는 서비스를 찾으면 해당 서비스 내부의 특성을 확인
//                            if (service.uuid == serviceUUID) {
//                                val characteristics = service.characteristics
//                                for (characteristic in characteristics) {
//                                    // 특성(UUID) 확인
//                                    Log.d(TAG, "Characteristic UUID: ${characteristic.uuid}")
//
//                                    // 원하는 특성(UUID)와 일치하는 경우에만 읽기 작업을 수행
//                                    if (characteristic.uuid == UUID.fromString(BluetoothUtils.NOTIFY_UUID)) {
//                                        if (ActivityCompat.checkSelfPermission(
//                                                this@BluetoothSearchActivity,
//                                                Manifest.permission.BLUETOOTH_CONNECT
//                                            ) != PackageManager.PERMISSION_GRANTED
//                                        ) {
//                                            // TODO: 권한 요청 처리
//                                            return
//                                        }
//
//                                        // Notify 활성화
//                                        gatt?.setCharacteristicNotification(characteristic, true)
//
//                                        // 해당 특성에 대한 디스크립터 설정
//                                        val descriptor =
//                                            characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
//                                        descriptor?.value =
//                                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//
//                                        // 디스크립터 설정 적용
//                                        gatt?.writeDescriptor(descriptor)
//
//                                        Log.d(
//                                            TAG,
//                                            "Notify enabled for characteristic: ${characteristic.uuid}"
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//
//
//            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
//                // 데이터가 변경되었을 때 호출됩니다.
//                if (characteristic == null) {
//                    return
//                }
//                val data = characteristic.value // 데이터를 바이트 배열로 가져옵니다.
//                // 데이터를 원하는 인코딩 방식으로 디코딩
//                val decodedData = String(data, Charset.forName("UTF-8"))
//                // 디코딩된 데이터와 수신 시간을 로그에 출력
//                Log.d(TAG, "Received data: $decodedData")
//
//            }
//
//
//            // 다른 BluetoothGattCallback 메서드들도 필요에 따라 추가합니다.
//        }
//
//
//        // BluetoothGatt 객체 초기화 및 연결 시도
//        bluetoothGatt = device.connectGatt(this@BluetoothSearchActivity, false, gattCallback)
//
//    }


    private fun startScan() {
        if (!adapter.isEnabled) return
        if (scanning) return
        val scanner = adapter.bluetoothLeScanner

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
            || (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                1
            )
            return
        }

        // 추가: 상태를 "STOP"으로 변경
//        binding.scan.text = "STOP"

        mainThreadHandler.postDelayed({
            scanning = false
            scanner.stopScan(scanCallback)
            // 추가: 스캔이 중지되면 상태를 "SCAN"으로 변경
//            binding.scan.text = "SCAN"
        }, SCAN_PERIOD)

        scanning = true
        scanResults.clear() // Clear previous scan results
        scanner.startScan(scanCallback)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 부여되었으므로 블루투스 스캔을 시작하십시오.
                startScan()
            } else {
                // 권한이 거부되었을 때 사용자에게 설명을 제공하거나 다른 조치를 취할 수 있습니다.
            }
        }
    }

    private fun showLottieAnimation() {
        val scanAnim = binding.animScanning
        scanAnim.repeatCount = 20
        scanAnim.visibility = View.VISIBLE
        scanAnim.playAnimation()
        scanAnim.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }
            override fun onAnimationEnd(animation: Animator) {

            }

            override fun onAnimationCancel(animation: Animator) {

            }
            override fun onAnimationRepeat(animation: Animator) {

            }
        })
    }

    private fun hideLottieAnimation() {
        val scanAnim = binding.animScanning
        scanAnim.visibility = View.GONE // Lottie 애니메이션을 숨깁니다.
        scanAnim.cancelAnimation() // 애니메이션을 중지합니다.
    }

    companion object {
        private const val TAG = "BluetoothSearchActivity"
        private const val SCAN_PERIOD = 2 * 60 * 1000L // 2 minutes
    }

}

