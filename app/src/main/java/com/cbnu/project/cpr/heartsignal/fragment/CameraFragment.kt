/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cbnu.project.cpr.heartsignal.fragment

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import kotlinx.coroutines.*
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.cbnu.project.cpr.heartsignal.viewModel.MainViewModel
import com.cbnu.project.cpr.heartsignal.PoseLandmarkerHelper
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.adapter.ChartDataRecyclerViewAdapter
import com.cbnu.project.cpr.heartsignal.databinding.FragmentCameraBinding
import com.cbnu.project.cpr.heartsignal.manager.chartmanager.LineChartManager
import com.cbnu.project.cpr.heartsignal.manager.soundmanager.AnimationManager
import com.cbnu.project.cpr.heartsignal.manager.soundmanager.SoundManager
import com.cbnu.project.cpr.heartsignal.step.Step0Activity
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class CameraFragment : Fragment(), PoseLandmarkerHelper.LandmarkerListener{

    companion object {
        private const val TAG = "Pose Landmarker"
    }

    private var _fragmentCameraBinding: FragmentCameraBinding? = null

    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper
    private val viewModel: MainViewModel by activityViewModels()
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT
    //그래프 설정
    private lateinit var lineChart: LineChart



    // 리사이클러 뷰
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var chartDataList: ArrayList<Entry>
//    private lateinit var recyclerViewAdapter: ChartDataRecyclerViewAdapter

    // lottie
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var lottie_count: LottieAnimationView

    // horizontal bar
//    private lateinit var horizontalBarChart: HorizontalBarChart
//    private val bar_entries = ArrayList<BarEntry>()
//    private lateinit var barDataSet: BarDataSet
//    private lateinit var barData : BarData

    //tickView
    private lateinit var tickerView: TickerView
    private var processingData = false



//    val apiService = RetrofitInstance.retrofit.create(ApiService::class.java)
//    val dataPointList = ArrayList<DataPoint>()
//    val serverUrl = "http://127.0.0.1:5000/upload_data/"


    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    override fun onResume() {
        super.onResume()
        // Make sure that all permissions are still present, since the
        // user could have removed them while the app was in paused state.
        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(
                requireActivity(), R.id.fragment_container
            ).navigate(R.id.action_camera_to_permissions)
        }

        // Start the PoseLandmarkerHelper again when users come back
        // to the foreground.
        backgroundExecutor.execute {
            if(this::poseLandmarkerHelper.isInitialized) {
                if (poseLandmarkerHelper.isClose()) {
                    poseLandmarkerHelper.setupPoseLandmarker()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if(this::poseLandmarkerHelper.isInitialized) {
            viewModel.setMinPoseDetectionConfidence(poseLandmarkerHelper.minPoseDetectionConfidence)
            viewModel.setMinPoseTrackingConfidence(poseLandmarkerHelper.minPoseTrackingConfidence)
            viewModel.setMinPosePresenceConfidence(poseLandmarkerHelper.minPosePresenceConfidence)
            viewModel.setDelegate(poseLandmarkerHelper.currentDelegate)

            // Close the PoseLandmarkerHelper and release resources
            backgroundExecutor.execute { poseLandmarkerHelper.clearPoseLandmarker() }
        }
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        // Shut down our background executor
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
        //프래그먼트가 종료될 때 MediaPlayer와 타이머를 정리합니다.
        SoundManager.releaseBeepSound()
        SoundManager.releaseCountDownSound()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding =
            FragmentCameraBinding.inflate(inflater, container, false)


        //line
        lineChart = fragmentCameraBinding.lineChart
        lineChart.setNoDataText("잠시만 기다려 주세요")
//        recyclerView = fragmentCameraBinding.recyclerView
//        chartDataList = ArrayList()

        // horizontal
//        horizontalBarChart = fragmentCameraBinding.horizontalBar
//        // RecyclerView 설정
//        recyclerViewAdapter = ChartDataRecyclerViewAdapter(chartDataList)
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        recyclerView.adapter = recyclerViewAdapter
        // lottie
        lottieAnimationView = fragmentCameraBinding.lottie
        lottie_count = fragmentCameraBinding.lottieCount
        //tickerView
        tickerView = fragmentCameraBinding.tickerView
        tickerView.setCharacterLists(TickerUtils.provideNumberList())
        AnimationManager.initialize(tickerView, lottieAnimationView)
        SoundManager.getContext(requireContext())

        // 블루투스 데이터를 수신하기 위해 로컬 브로드캐스트 수신기를 등록합니다.
        val intentFilter = IntentFilter("BLUETOOTH_DATA_RECEIVED")
        val receiver = object : BroadcastReceiver() {
            override  fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "BLUETOOTH_DATA_RECEIVED") {
                    val data = intent.getStringExtra("data")
                    // 데이터 처리 중인 경우에만 작업을 실행
                    if(data!!.contains("SO")) {
//                        processingData = true // 데이터 처리 중으로 플래그 설정
                        LineChartManager.generateInitialData()
                        LineChartManager.initialize(lineChart)
                        /* "SO" 신호를 받은 뒤 5초의 딜레이 후 실제 데이터 전송 받음 */
                        if (!processingData) {
                            processingData = true
                            SoundManager.initialize(requireContext())
                            AnimationManager.updateData()

                            val handler = Handler()
                            handler.postDelayed({
                                CoroutineScope(Dispatchers.Main).launch {
                                    AnimationManager.showLottieCountDown(
                                        lottie_count,
                                        fragmentCameraBinding
                                    )
                                    val delayMillis = 500L // 0.5초마다 업데이트
                                    while (processingData) {
                                        // 타이머 소리 재생
                                        SoundManager.playBeepSound()
                                        // lottie play
                                        AnimationManager.showLottieAnimation()
                                        // 지정된 시간만큼 대기
                                        delay(delayMillis)
                                    }
                                }
                            }, 5000)
                        }
                    }
                    else {
                        if(data!!.contains("SF"))
                        {
                            /* 한 라운드가 종료 - 사용한 리스트나 데이터들 메모리 정리 필요 */
                            LineChartManager.clearData()
                            SoundManager.releaseBeepSound()
//                            AnimationManager.stopTickerViewCountDown()
                            processingData = false
                        }
                        else{
                            updateUI(data)
                        }
                    }
                }
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, intentFilter)
        SoundManager.introSound()


        return fragmentCameraBinding.root
    }
    // UI 업데이트를 비동기로 처리하는 함수
    private fun updateUI(data: String?) {
        // UI 업데이트 작업을 여기에 배치
        lifecycleScope.launch {
            LineChartManager.addBLEDataToList(data!!)
            LineChartManager.updateLineChart()
        }
    }




    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)






        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()

        // Wait for the views to be properly laid out
        fragmentCameraBinding.viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }

        // Create the PoseLandmarkerHelper that will posele the inference
        backgroundExecutor.execute {
            poseLandmarkerHelper = PoseLandmarkerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                minPoseDetectionConfidence = viewModel.currentMinPoseDetectionConfidence,
                minPoseTrackingConfidence = viewModel.currentMinPoseTrackingConfidence,
                minPosePresenceConfidence = viewModel.currentMinPosePresenceConfidence,
                currentDelegate = viewModel.currentDelegate,
                poseLandmarkerHelperListener = this
            )
        }

    }



    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        detectPose(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun detectPose(imageProxy: ImageProxy) {
        if(this::poseLandmarkerHelper.isInitialized) {
            poseLandmarkerHelper.detectLiveStream(
                imageProxy = imageProxy,
                isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            fragmentCameraBinding.viewFinder.display.rotation
    }

    // Update UI after pose have been detected. Extracts original
    // image height/width to scale and place the landmarks properly through
    // OverlayView
    override fun onResults(
        resultBundle: PoseLandmarkerHelper.ResultBundle
    ) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {
//                fragmentCameraBinding.bottomSheetLayout.inferenceTimeVal.text =
//                    String.format("%d ms", resultBundle.inferenceTime)

                // Pass necessary information to OverlayView for drawing on the canvas
                fragmentCameraBinding.overlay.setResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM
                )

                // Force a redraw
                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            if (errorCode == PoseLandmarkerHelper.GPU_ERROR) {
//                fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(
//                    PoseLandmarkerHelper.DELEGATE_CPU, false
//                )
            }
        }
    }

}
