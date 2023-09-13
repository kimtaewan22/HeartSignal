package com.cbnu.project.cpr.heartsignal.ble


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cbnu.project.cpr.heartsignal.adapter.BluetoothDeviceListAdapter
import com.cbnu.project.cpr.heartsignal.ble.BluetoothUtils.Companion.findResponseCharacteristic
import com.cbnu.project.cpr.heartsignal.data.BluetoothDeviceInfo
import com.cbnu.project.cpr.heartsignal.databinding.ActivityBluetoothSearchBinding
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
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

    private lateinit var mGatt: BluetoothGatt
    private var readMsg = ""
    private var startTime: Long = 0


    var bluetoothGatt:BluetoothGatt? = null

    // 오렌지보드 UUID만 필터링
//    private val scanCallback = object : ScanCallback() {
//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            val device = result.device
//
//            // 원하는 서비스 UUID를 가진 장치만 필터링
//            val serviceUuid = UUID.fromString(BluetoothUtils.NOTIFY_UUID)
//            val serviceUuids = device.uuids
//
//            Log.d(TAG,"serviceUuids : ${serviceUuids}")
//
//            if (serviceUuids != null && serviceUuids.any { it.uuid == serviceUuid }) {
//                if (!scanResults.contains(device)) {
//                    scanResults.add(device)
//                    deviceList.add(BluetoothDeviceInfo(device?.name ?: "NULL", device.address,
//                        serviceUuid.toString(), device
//                    ))
//                    bluetoothDeviceListAdapter.notifyDataSetChanged()
//                    if (ActivityCompat.checkSelfPermission(
//                            this@BluetoothSearchActivity,
//                            Manifest.permission.BLUETOOTH_CONNECT
//                        ) != PackageManager.PERMISSION_GRANTED
//                    ) {
//                        // TODO: Consider calling
//                        //    ActivityCompat#requestPermissions
//                        // here to request the missing permissions, and then overriding
//                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                        //                                          int[] grantResults)
//                        // to handle the case where the user grants the permission. See the documentation
//                        // for ActivityCompat#requestPermissions for more details.
//                        return
//                    }
//                    Log.d(TAG, "Scanned Device: ${device?.name} - ${device.address} - $serviceUuid")
//                }
//            }
//        }
//
//        override fun onScanFailed(errorCode: Int) {
//            Log.e(TAG, "Scan failed with error code: $errorCode")
//        }
//    }



    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (!scanResults.contains(device)) {
                scanResults.add(device)
                deviceList.add(BluetoothDeviceInfo(device?.name ?: "NULL", device.address,
                    device.uuids?.toString() ?: "NULL", device
                ))
                bluetoothDeviceListAdapter.notifyDataSetChanged()
                if (ActivityCompat.checkSelfPermission(
                        this@BluetoothSearchActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                Log.d(TAG, "Scanned Device: ${device?.name} - ${device.address} - ${device.uuids}")

            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error code: $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG,"연결성공")
                if (ActivityCompat.checkSelfPermission(this@BluetoothSearchActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {  }
                mGatt?.discoverServices()
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG,"연결해제")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when(status){
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(TAG,"블루투스 셋팅완료")
                    if (ActivityCompat.checkSelfPermission(this@BluetoothSearchActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {  }

                    val respCharacteristic = gatt?.let {
                        findResponseCharacteristic(it)
                    }
                    if( respCharacteristic == null ) {
                        Log.e(TAG, "블루투스 커맨드를 찾지 못하였습니다.")
                        return
                    }
                    gatt.setCharacteristicNotification(respCharacteristic, true)
                    val descriptor: BluetoothGattDescriptor = respCharacteristic.getDescriptor(UUID.fromString(BluetoothUtils.NOTIFY_UUID))
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    gatt.writeDescriptor(descriptor)
                }
                else -> {
                    Log.e(TAG,"블루투스 셋팅실패")
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            when(status){
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(TAG,"데이터 보내기 성공")
                }
                else -> {
                    Log.d(TAG,"데이터 보내기 실패")
                }
            }
        }

        //안드로이드 13이상 호출
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                Log.d(TAG,"블루투스 수신성공")
                readMsg = String(value)
            }
        }

        //안드로이드 12까지 호출
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                Log.d(TAG,"블루투스 수신성공")
                readMsg = characteristic?.getStringValue(0).toString()
            }
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

        // 리사이클러뷰 초기화
        bleRecyclerView = binding.scannedBleRecyclerView // 리사이클러뷰 ID 변경 필요
        // BluetoothDeviceListAdapter 초기화 시 클릭 리스너 전달
        bluetoothDeviceListAdapter = BluetoothDeviceListAdapter(deviceList) { device ->
            connectToDevice(device)
        }
        bleRecyclerView.adapter = bluetoothDeviceListAdapter

        bleRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.scan.setOnClickListener {
            // 추가: "SCAN" 또는 "STOP"에 따라 스캔 시작 또는 중지
            if (binding.scan.text == "SCAN") {
                startScan()
            } else {
                // 스캔 중지 로직 추가
                val scanner = adapter.bluetoothLeScanner
                scanning = false
                scanner.stopScan(scanCallback)
                binding.scan.text = "SCAN"
            }
        }

    }

    private fun connectToDevice(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Bluetooth 연결 권한이 없는 경우 권한을 요청합니다.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                2 // 요청 코드, 필요에 따라 변경 가능
            )
            return
        }

        // BluetoothGattCallback을 사용하여 BluetoothGatt 객체를 초기화하고 연결을 시도합니다.
        val gattCallback = object : BluetoothGattCallback() {
            // 원하는 서비스 UUID
            val serviceUUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // 연결이 성공적으로 완료된 경우
                    runOnUiThread {
                        // UI 스레드에서 Toast 띄우기
                        if (ActivityCompat.checkSelfPermission(
                                this@BluetoothSearchActivity,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return@runOnUiThread
                        }
                        Toast.makeText(this@BluetoothSearchActivity, "연결 성공! : ${gatt?.device?.name} , ${gatt?.device?.address}  ", Toast.LENGTH_SHORT).show()
                        // BluetoothGatt 객체로부터 서비스 목록을 가져옵니다.
                        binding.connectedDeviceName.text = gatt?.device?.name
                        binding.connectedDeviceAddr.text = gatt?.device?.address



                        val services = gatt?.services
                        if (services != null) {
                            for (service in services) {
                                // 각 서비스에 대한 처리를 수행합니다.
                                Log.d(TAG, "Service UUID: ${service.uuid}")
                                // 이곳에서 필요한 작업을 수행합니다.
                            }
                        }

                    }
                    if (ActivityCompat.checkSelfPermission(
                            this@BluetoothSearchActivity,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                    // 연결 성공 후 추가 작업 수행
                    gatt?.discoverServices()
                    // 예: 서비스 발견을 시작하거나 특성에 대한 구성을 수행합니다.
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // 연결이 해제된 경우
                    runOnUiThread {
                        // UI 스레드에서 Toast 띄우기
                        Toast.makeText(this@BluetoothSearchActivity, "연결 해제", Toast.LENGTH_SHORT).show()
                    }

                    Log.d(TAG, "연결 해제")
                }


            }
            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                Log.d(TAG, "Service status: $status")

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

                                    // 원하는 특성(UUID)와 일치하는 경우에만 Notify 활성화
                                    if (characteristic.uuid == UUID.fromString(BluetoothUtils.NOTIFY_UUID)) {
                                        if (ActivityCompat.checkSelfPermission(
                                                this@BluetoothSearchActivity,
                                                Manifest.permission.BLUETOOTH_CONNECT
                                            ) != PackageManager.PERMISSION_GRANTED
                                        ) {
                                            // TODO: 권한 요청 처리
                                            return
                                        }

                                        // Notify 활성화
                                        gatt?.setCharacteristicNotification(characteristic, true)

                                        // 해당 특성에 대한 디스크립터 설정
                                        val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                                        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

                                        // 디스크립터 설정 적용
                                        gatt?.writeDescriptor(descriptor)

                                        Log.d(TAG, "Notify enabled for characteristic: ${characteristic.uuid}")
                                    }
                                }
                            }
                        }
                    }
                }
            }

//            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
//                // 데이터가 변경되었을 때 호출됩니다.
//                if (characteristic == null) {
//                    return
//                }
//
//                val data = characteristic.value // 데이터를 바이트 배열로 가져옵니다.
//
//                // 데이터를 원하는 인코딩 방식으로 디코딩
//                val decodedData = String(data, Charset.forName("UTF-8"))
//
//                // 디코딩된 데이터를 로그에 출력
//                Log.d(TAG, "Received data: $decodedData")
//            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                // 데이터가 변경되었을 때 호출됩니다.
                if (characteristic == null) {
                    return
                }

                val data = characteristic.value // 데이터를 바이트 배열로 가져옵니다.

                // 현재 시간 기록
                val currentTime = System.currentTimeMillis()

                // 데이터를 원하는 인코딩 방식으로 디코딩
                val decodedData = String(data, Charset.forName("UTF-8"))
                // 송신측에서 보내는 데이터 형식을 맞춰서 디코딩 해야함
//                val decodedData = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).float

                // 데이터를 받는 데 걸린 시간 계산
                val elapsedTime = currentTime - startTime

                // 디코딩된 데이터와 수신 시간을 로그에 출력
                Log.d(TAG, "Received data: $decodedData")

                // 현재 시간을 시작 시간으로 설정하여 다음 데이터 수신 시간 측정
                startTime = currentTime
            }



//            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
//                Log.d(TAG, "Service status: ${status}")
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
////                                    if (characteristic.uuid == UUID.fromString(BluetoothUtils.NOTIFY_UUID)) {
////                                        if (ActivityCompat.checkSelfPermission(
////                                                this@BluetoothSearchActivity,
////                                                Manifest.permission.BLUETOOTH_CONNECT
////                                            ) != PackageManager.PERMISSION_GRANTED
////                                        ) {
////                                            // TODO: 권한 요청 처리
////                                            return
////                                        }
////
////                                        // 데이터를 특성에 쓰기
////                                        val writeCharacteristic = gatt?.getService(serviceUUID)?.getCharacteristic(characteristic.uuid)
////                                        val dataToWrite = "Your Data to Write".toByteArray() // 쓸 데이터를 바이트 배열로 변환
////
////                                        writeCharacteristic?.value = dataToWrite // 데이터 설정
////
////                                        val success = gatt?.writeCharacteristic(writeCharacteristic)
////                                        if (success == true) {
////                                            // 쓰기 작업이 성공한 경우
////                                            Log.d(TAG, "Write successful. Data written: ${String(dataToWrite)}")
////                                        } else {
////                                            // 쓰기 작업이 실패한 경우
////                                            Log.e(TAG, "Failed to write characteristic: ${characteristic.uuid}")
////                                        }
////                                    }
//                                    for (characteristic in characteristics) {
//                                        // 특성(UUID) 확인
//                                        Log.d(TAG, "Characteristic UUID: ${characteristic.uuid}")
//
////                                        // 원하는 특성(UUID)와 일치하는 경우에만 읽기 작업을 수행
////                                        if (characteristic.uuid == UUID.fromString(BluetoothUtils.NOTIFY_UUID)) {
////                                            if (ActivityCompat.checkSelfPermission(
////                                                    this@BluetoothSearchActivity,
////                                                    Manifest.permission.BLUETOOTH_CONNECT
////                                                ) != PackageManager.PERMISSION_GRANTED
////                                            ) {
////                                                // TODO: 권한 요청 처리
////                                                return
////                                            }
////
////                                            // 데이터를 특성에서 읽기
////                                            val success = gatt?.readCharacteristic(characteristic)
////                                            Log.d(TAG, "Success to read characteristic: ${success}")
////
////                                            if (success != true) {
////                                                // 읽기 작업이 실패한 경우 처리
////                                                Log.e(TAG, "Failed to read characteristic: ${characteristic.uuid}")
////                                            }
////                                        }
//
//                                        // 원하는 특성(UUID)와 일치하는 경우에만 Notify 활성화
//                                        if (characteristic.uuid == UUID.fromString(BluetoothUtils.NOTIFY_UUID)) {
//                                            if (ActivityCompat.checkSelfPermission(
//                                                    this@BluetoothSearchActivity,
//                                                    Manifest.permission.BLUETOOTH_CONNECT
//                                                ) != PackageManager.PERMISSION_GRANTED
//                                            ) {
//                                                // TODO: 권한 요청 처리
//                                                return
//                                            }
//
//                                            // Notify 활성화
//                                            gatt?.setCharacteristicNotification(characteristic, true)
//
//                                            // 해당 특성에 대한 디스크립터 설정
//                                            val descriptor = characteristic.getDescriptor(UUID.fromString(("00002902-0000-1000-8000-00805f9b34fb")
//                                            ))
//                                            descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//
////                                            val dataAsString =
////                                                descriptor?.value?.let { String(it, Charset.forName("UTF-8")) }
////
////                                            gatt?.writeDescriptor(descriptor)
//
//                                            // 디스크립터 값을 문자열로 변환하여 dataAsString 변수에 저장
//                                            val dataAsString = descriptor?.value?.let { String(it, Charset.forName("UTF-16")) }
//
//                                            // 디스크립터 설정 적용
//                                            gatt?.writeDescriptor(descriptor)
//
//                                            // dataAsString 변수를 사용하여 로그에 출력
//                                            Log.d(TAG, "Notify enabled for characteristic: $dataAsString")
//                                            Log.d(TAG, "Notify enabled for characteristic: ${descriptor?.value}")
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }


            // 다른 BluetoothGattCallback 메서드들도 필요에 따라 추가합니다.
        }


        // BluetoothGatt 객체 초기화 및 연결 시도
        bluetoothGatt = device.connectGatt(this@BluetoothSearchActivity, false, gattCallback)

    }





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
        binding.scan.text = "STOP"

        mainThreadHandler.postDelayed({
            scanning = false
            scanner.stopScan(scanCallback)
            // 추가: 스캔이 중지되면 상태를 "SCAN"으로 변경
            binding.scan.text = "SCAN"
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

    companion object {
        private const val TAG = "BluetoothSearchActivity"
        private const val SCAN_PERIOD = 2 * 60 * 1000L // 2 minutes
    }


}

