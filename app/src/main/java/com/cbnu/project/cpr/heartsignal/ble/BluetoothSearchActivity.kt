package com.cbnu.project.cpr.heartsignal.ble


import android.Manifest
import android.animation.Animator
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cbnu.project.cpr.heartsignal.adapter.BluetoothDeviceListAdapter
import com.cbnu.project.cpr.heartsignal.data.BluetoothDeviceInfo
import com.cbnu.project.cpr.heartsignal.databinding.ActivityBluetoothSearchBinding
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.github.angads25.toggle.model.ToggleableView

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
    private var scanner: BluetoothLeScanner? = null


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
                Log.d(TAG, "Scanned Device: ${device?.name} - $address - ${device.uuids}")

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
                    scanner = adapter.bluetoothLeScanner
                    scanning = false
                    if (ActivityCompat.checkSelfPermission(
                            this@BluetoothSearchActivity,
                            Manifest.permission.BLUETOOTH_SCAN
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    scanner?.stopScan(scanCallback)
                }
            }
        })
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
        private const val SCAN_PERIOD = 2 * 10 * 1000L // 2 minutes
    }
    override fun onDestroy() {
        super.onDestroy()
        hideLottieAnimation()

        // 액티비티가 종료될 때 스캔을 중지
        scanning = false
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
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
        scanner?.stopScan(scanCallback)

    }
}

