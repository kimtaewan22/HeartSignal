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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
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
import com.airbnb.lottie.LottieAnimationView
import com.cbnu.project.cpr.heartsignal.viewModel.MainViewModel
import com.cbnu.project.cpr.heartsignal.PoseLandmarkerHelper
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.databinding.FragmentCameraBinding
import com.cbnu.project.cpr.heartsignal.manager.chartmanager.LineChartManager
import com.cbnu.project.cpr.heartsignal.manager.chartmanager.ProgressiveGaugeManager
import com.cbnu.project.cpr.heartsignal.manager.chartmanager.VerticalProgressbarManager
import com.cbnu.project.cpr.heartsignal.manager.framemanager.FrameBackgroundManager
import com.cbnu.project.cpr.heartsignal.manager.resultmanager.ResultBottomSheetManager
import com.cbnu.project.cpr.heartsignal.manager.soundmanager.AnimationManager
import com.cbnu.project.cpr.heartsignal.manager.soundmanager.SoundManager
import com.cbnu.project.cpr.heartsignal.step.StepProgressActivity
import com.github.anastr.speedviewlib.ProgressiveGauge
import com.github.mikephil.charting.charts.LineChart
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

    private lateinit var progressBar: ProgressBar
    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper
    private val viewModel: MainViewModel by activityViewModels()
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT
    //그래프 설정
    private lateinit var lineChart: LineChart
    private lateinit var progressiveGauge: ProgressiveGauge

    private lateinit var frameLayoutTop: FrameLayout
    private lateinit var frameLayoutBottom: FrameLayout
    private lateinit var frameLayoutLeft: FrameLayout
    private lateinit var frameLayoutRight: FrameLayout

    var isAnimation1Played = false
    var isAnimation2Played = false
    var isAnimation3Played = false
    var isAnimation4Played = false

    // lottie
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var lottie_count: LottieAnimationView


    //tickView
    private lateinit var tickerView: TickerView
    private var processingData = false
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

        frameLayoutTop = fragmentCameraBinding.compressionArrowTop
        frameLayoutBottom = fragmentCameraBinding.compressionArrowBottom
        frameLayoutLeft = fragmentCameraBinding.compressionArrowLeft
        frameLayoutRight = fragmentCameraBinding.compressionArrowRight


        //line
        lineChart = fragmentCameraBinding.lineChart
        progressBar = fragmentCameraBinding.verticalProgressbar
        progressiveGauge = fragmentCameraBinding.progressiveGauge
        lineChart.setNoDataText("잠시만 기다려 주세요")

        // lottie
        lottieAnimationView = fragmentCameraBinding.lottie
        lottie_count = fragmentCameraBinding.lottieCount
        //tickerView
        tickerView = fragmentCameraBinding.tickerView
        tickerView = fragmentCameraBinding.tickerView
        tickerView.setCharacterLists(TickerUtils.provideNumberList())
        AnimationManager.initialize(tickerView, lottieAnimationView, requireContext())
        ProgressiveGaugeManager.initialize(progressiveGauge)
        VerticalProgressbarManager.initialize(progressBar)
        FrameBackgroundManager.initialize(frameLayoutTop, frameLayoutBottom, frameLayoutLeft, frameLayoutRight, requireContext())

        // 블루투스 데이터를 수신하기 위해 로컬 브로드캐스트 수신기를 등록합니다.
        val intentFilter = IntentFilter("BLUETOOTH_DATA_RECEIVED")
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val data = intent?.getStringExtra("data") ?: return // 'data'가 null이면 여기서 함수 종료

                when {
                    data.contains("SO") && !processingData -> {
                        // 데이터 처리 시작
                        processingData = true
                        SoundManager.initialize(getContext())
                        LineChartManager.generateInitialData()
                        LineChartManager.initialize(lineChart)
                        // 딜레이 후에 처리 시작
                        CoroutineScope(Dispatchers.Main).launch {
                            AnimationManager.showLottieCountDown(lottie_count, fragmentCameraBinding)
                            val delayMillis = 500L // 0.5초마다 업데이트
                            while (processingData) {
//                                SoundManager.playBeepSound()
                                val currentTime = AnimationManager.getSecondRemainingTime()
//                                val successCount = LineChartManager.getCompressionSuccessCount().toFloat()
                                SoundManager.playBeepSound()


                                // 현재 시간을 기준으로 애니메이션 변경
                                when {
                                    currentTime in 1f..15f && !isAnimation4Played -> {
                                        AnimationManager.showLottieAnimation(R.raw.heart_success1, 30)
                                        isAnimation4Played = true // 애니메이션이 재생되었다고 표시
                                    }
                                    currentTime in 16f..30f && !isAnimation3Played -> {
                                        AnimationManager.showLottieAnimation(R.raw.heart_bad3, 30)
                                        isAnimation3Played = true // 애니메이션이 재생되었다고 표시
                                    }
                                    currentTime in 31f..45f && !isAnimation2Played -> {
                                        AnimationManager.showLottieAnimation(R.raw.heart_bad2, 30)
                                        isAnimation2Played = true // 애니메이션이 재생되었다고 표시
                                    }
                                    currentTime in 46f..60f && !isAnimation1Played -> {
                                        AnimationManager.showLottieAnimation(R.raw.heart_bad1, 30)
                                        isAnimation1Played = true // 애니메이션이 재생되었다고 표시
                                    }
                                }

                                delay(delayMillis)
                            }
                        }
                    }
                    data.contains("ST") -> { // 데이터 받기 시작
                        AnimationManager.tickerViewCountDown()
//                        LineChartManager.flagChange()
                    }
                    data.contains("SF") -> {
                        // 한 라운드 종료 처리
                        LineChartManager.clearData()
                        SoundManager.releaseBeepSound()
                        isAnimation1Played = false
                        isAnimation2Played = false
                        isAnimation3Played = false
                        isAnimation4Played = false
                        processingData = false
                        // 결과 보여주기
                        ResultBottomSheetManager().showBottomSheet(parentFragmentManager)
                    }
                    else -> {
                        updateUI(data) // 다른 데이터 처리
                    }
                }
            }
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, intentFilter)
//        SoundManager.introSound()


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
    //
    override fun onAttach(context: Context) {
        super.onAttach(context)
        SoundManager.getContext(context)

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

    private fun goToAnotherActivity() {
        // 현재 프래그먼트를 종료
        fragmentManager?.beginTransaction()?.remove(this)?.commit()

        // 새 액티비티 시작을 위한 인텐트 생성
        val intent = Intent(activity, StepProgressActivity::class.java)

        // 옵션으로, 인텐트에 데이터 추가 가능
         intent.putExtra("stepFlag", "결과")

        // 새 액티비티 시작
        startActivity(intent)

        // (선택사항) 현재 호스팅 액티비티 종료
        activity?.finish()
    }
}
