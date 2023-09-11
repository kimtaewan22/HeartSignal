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

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
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
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.cbnu.project.cpr.heartsignal.MainViewModel
import com.cbnu.project.cpr.heartsignal.PoseLandmarkerHelper
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.adapter.ChartDataRecyclerViewAdapter
import com.cbnu.project.cpr.heartsignal.databinding.FragmentCameraBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.google.mediapipe.tasks.vision.core.RunningMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class CameraFragment : Fragment(), PoseLandmarkerHelper.LandmarkerListener {

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
    // Sound
    private var mediaPlayer: MediaPlayer? = null
    private val timer: CountDownTimer? = null

    //그래프 설정
    private lateinit var lineChart: LineChart
    private val entry_chart1 = ArrayList<Entry>()
    private lateinit var lineDataSet1: LineDataSet
    private lateinit var chartData: LineData

    private val maxDataPoints = 10 // 최대 데이터 포인트 개수
    private var currentTime = 0.0 // 현재 시간 값
    private val timeInterval = 0.5 // 핸들러 호출 간격 (초)

    // 리사이클러 뷰
    private lateinit var recyclerView: RecyclerView
    private lateinit var chartDataList: ArrayList<Entry>
    private lateinit var recyclerViewAdapter: ChartDataRecyclerViewAdapter

    //lottie
    private lateinit var lottieAnimationView: LottieAnimationView

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
        mediaPlayer?.release()
        timer?.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding =
            FragmentCameraBinding.inflate(inflater, container, false)
        lineChart = fragmentCameraBinding.lineChart
        recyclerView = fragmentCameraBinding.recyclerView
        chartDataList = ArrayList()
        // RecyclerView 설정
        recyclerViewAdapter = ChartDataRecyclerViewAdapter(chartDataList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = recyclerViewAdapter
        // "삐" 소리 재생을 위한 MediaPlayer 초기화
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.mp_beep)
        // lottie
        lottieAnimationView = fragmentCameraBinding.lottie

        showLottieAnimation()
        showLineChart()
        return fragmentCameraBinding.root
    }

    private fun showLottieAnimation() {
        lottieAnimationView.repeatCount = 60
        lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
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

        // Attach listeners to UI control widgets
//        initBottomSheetControls()
    }

//    private fun initBottomSheetControls() {
//        // init bottom sheet settings
//
//        fragmentCameraBinding.bottomSheetLayout.detectionThresholdValue.text =
//            String.format(
//                Locale.US, "%.2f", viewModel.currentMinPoseDetectionConfidence
//            )
//        fragmentCameraBinding.bottomSheetLayout.trackingThresholdValue.text =
//            String.format(
//                Locale.US, "%.2f", viewModel.currentMinPoseTrackingConfidence
//            )
//        fragmentCameraBinding.bottomSheetLayout.presenceThresholdValue.text =
//            String.format(
//                Locale.US, "%.2f", viewModel.currentMinPosePresenceConfidence
//            )
//
//        // When clicked, lower pose detection score threshold floor
//        fragmentCameraBinding.bottomSheetLayout.detectionThresholdMinus.setOnClickListener {
//            if (poseLandmarkerHelper.minPoseDetectionConfidence >= 0.2) {
//                poseLandmarkerHelper.minPoseDetectionConfidence -= 0.1f
//                updateControlsUi()
//            }
//        }
//
//        // When clicked, raise pose detection score threshold floor
//        fragmentCameraBinding.bottomSheetLayout.detectionThresholdPlus.setOnClickListener {
//            if (poseLandmarkerHelper.minPoseDetectionConfidence <= 0.8) {
//                poseLandmarkerHelper.minPoseDetectionConfidence += 0.1f
//                updateControlsUi()
//            }
//        }
//
//        // When clicked, lower pose tracking score threshold floor
//        fragmentCameraBinding.bottomSheetLayout.trackingThresholdMinus.setOnClickListener {
//            if (poseLandmarkerHelper.minPoseTrackingConfidence >= 0.2) {
//                poseLandmarkerHelper.minPoseTrackingConfidence -= 0.1f
//                updateControlsUi()
//            }
//        }
//
//        // When clicked, raise pose tracking score threshold floor
//        fragmentCameraBinding.bottomSheetLayout.trackingThresholdPlus.setOnClickListener {
//            if (poseLandmarkerHelper.minPoseTrackingConfidence <= 0.8) {
//                poseLandmarkerHelper.minPoseTrackingConfidence += 0.1f
//                updateControlsUi()
//            }
//        }
//
//        // When clicked, lower pose presence score threshold floor
//        fragmentCameraBinding.bottomSheetLayout.presenceThresholdMinus.setOnClickListener {
//            if (poseLandmarkerHelper.minPosePresenceConfidence >= 0.2) {
//                poseLandmarkerHelper.minPosePresenceConfidence -= 0.1f
//                updateControlsUi()
//            }
//        }
//
//        // When clicked, raise pose presence score threshold floor
//        fragmentCameraBinding.bottomSheetLayout.presenceThresholdPlus.setOnClickListener {
//            if (poseLandmarkerHelper.minPosePresenceConfidence <= 0.8) {
//                poseLandmarkerHelper.minPosePresenceConfidence += 0.1f
//                updateControlsUi()
//            }
//        }
//
//        // When clicked, change the underlying hardware used for inference.
//        // Current options are CPU and GPU
//        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.setSelection(
//            viewModel.currentDelegate, false
//        )
//        fragmentCameraBinding.bottomSheetLayout.spinnerDelegate.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
//                ) {
//                    try {
//                        poseLandmarkerHelper.currentDelegate = p2
//                        updateControlsUi()
//                    } catch(e: UninitializedPropertyAccessException) {
//                        Log.e(TAG, "PoseLandmarkerHelper has not been initialized yet.")
//                    }
//                }
//
//                override fun onNothingSelected(p0: AdapterView<*>?) {
//                    /* no op */
//                }
//            }
//
//        // When clicked, change the underlying model used for object detection
//        fragmentCameraBinding.bottomSheetLayout.spinnerModel.setSelection(
//            viewModel.currentModel,
//            false
//        )
//        fragmentCameraBinding.bottomSheetLayout.spinnerModel.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    p0: AdapterView<*>?,
//                    p1: View?,
//                    p2: Int,
//                    p3: Long
//                ) {
//                    poseLandmarkerHelper.currentModel = p2
//                    updateControlsUi()
//                }
//
//                override fun onNothingSelected(p0: AdapterView<*>?) {
//                    /* no op */
//                }
//            }
//    }

    // Update the values displayed in the bottom sheet. Reset Poselandmarker
    // helper.
//    private fun updateControlsUi() {
//        if(this::poseLandmarkerHelper.isInitialized) {
//            fragmentCameraBinding.bottomSheetLayout.detectionThresholdValue.text =
//                String.format(
//                    Locale.US,
//                    "%.2f",
//                    poseLandmarkerHelper.minPoseDetectionConfidence
//                )
//            fragmentCameraBinding.bottomSheetLayout.trackingThresholdValue.text =
//                String.format(
//                    Locale.US,
//                    "%.2f",
//                    poseLandmarkerHelper.minPoseTrackingConfidence
//                )
//            fragmentCameraBinding.bottomSheetLayout.presenceThresholdValue.text =
//                String.format(
//                    Locale.US,
//                    "%.2f",
//                    poseLandmarkerHelper.minPosePresenceConfidence
//                )
//
//            // Needs to be cleared instead of reinitialized because the GPU
//            // delegate needs to be initialized on the thread using it when applicable
//            backgroundExecutor.execute {
//                poseLandmarkerHelper.clearPoseLandmarker()
//                poseLandmarkerHelper.setupPoseLandmarker()
//            }
//            fragmentCameraBinding.overlay.clear()
//        }
//    }

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

    // draw Line Chart

//    private fun showLineChart() {
//        val entry_chart1 = ArrayList<Entry>()
//
//        // 1에서 5 사이의 랜덤 정수 값을 10개 추가
//        val random = java.util.Random()
//        for (i in 0 until 10) {
//            val randomValue = (1 + random.nextInt(5)).toFloat()
//            entry_chart1.add(Entry(i.toFloat(), randomValue))
//        }
//
//        val lineDataSet1 = LineDataSet(entry_chart1, "LineGraph1")
//        lineDataSet1.color = Color.RED
//        lineDataSet1.mode = LineDataSet.Mode.CUBIC_BEZIER
//        lineDataSet1.cubicIntensity = 0.2f
//        //lineDataSet1.setCircleColor(getColor(R.color.blue_grey_400))
//        lineDataSet1.setDrawCircleHole(false)
//        lineDataSet1.setDrawValues(false)
//
//        val chartData = LineData(lineDataSet1)
//
//        lineChart.apply {
//            setTouchEnabled(true)
//            isClickable = false
//            isDoubleTapToZoomEnabled = false
//            setDrawBorders(false)
//            setDrawGridBackground(false)
//            description.isEnabled = false
//            legend.isEnabled = false
//
//            axisLeft.apply {
//                setDrawGridLines(false)
//                setDrawLabels(false)
//                setDrawAxisLine(false)
//                removeAllLimitLines()
//                addLimitLine(LimitLine(150f, "Upper Limit").apply {
//                    lineWidth = 4f
//                    enableDashedLine(10f, 10f, 0f)
//                    labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
//                    //lineColor = getColor(R.color.blue_grey_400)
//                    textSize = 10f
//                })
//            }
//
//            axisRight.apply {
//                setDrawGridLines(false)
//                setDrawLabels(false)
//                setDrawAxisLine(false)
//            }
//
//            xAxis.apply {
//                enableGridDashedLine(16f, 12f, 0f)
//                position = XAxis.XAxisPosition.BOTTOM
//                setCenterAxisLabels(true)
//            }
//
//            animateXY(2000, 2000)
//            data = chartData
//            invalidate()
//            setTouchEnabled(true)
//        }
//    }

    private fun showLineChart() {
        // 그래프 설정 및 초기 데이터 채우기
        generateInitialData()
        setupChart()


//        // 실시간 업데이트를 위한 핸들러 설정
//        val handler = Handler()
//        val delay = 300L // 1초마다 업데이트
//
//        handler.postDelayed(object : Runnable {
//            override fun run() {
//                // 새 데이터 포인트 추가
//                addRandomDataPoint()
//
//                // 그래프 업데이트
//                updateChart()
//                updateRecyclerView()
//
//                // 다음 업데이트 예약
//                handler.postDelayed(this, delay)
//            }
//        }, delay)

        // 코루틴을 사용한 실시간 업데이트
        lifecycleScope.launch {
            val delayMillis = 500L // 0.3초마다 업데이트

            while (true) {
                // 새 데이터 포인트 추가
                addRandomDataPoint()

                // UI 업데이트를 메인 스레드에서 수행
                withContext(Dispatchers.Main) {
                    // 그래프 업데이트
                    updateChart()
                    updateRecyclerView()
                }
                // 타이머 소리 재생
                playTimerSound()
                // lottie play
                showLottieAnimation()
                // 지정된 시간만큼 대기
                delay(delayMillis)
            }
        }
    }
    private fun playTimerSound() {
        mediaPlayer?.seekTo(0) // 소리를 처음부터 재생
        mediaPlayer?.start() // 소리 재생
    }

    private fun setupChart() {
        lineDataSet1 = LineDataSet(entry_chart1, "LineGraph1")

        // 선의 색상을 보라색으로 변경
        lineDataSet1.color = R.drawable.fade
        lineDataSet1.setFillFormatter(IFillFormatter { lineDataSet1, dataProvider -> // change the return value here to better understand the effect
            // return 600;
            lineChart.axisLeft.axisMinimum
        })


        lineDataSet1.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet1.cubicIntensity = 0.1f
        lineDataSet1.setDrawCircleHole(false)
        lineDataSet1.setDrawValues(false)

        chartData = LineData(lineDataSet1)

        lineChart.apply {
            setTouchEnabled(true)
            isClickable = false
            isDoubleTapToZoomEnabled = false
            setDrawBorders(false)
            setDrawGridBackground(false)
            description.isEnabled = false
            legend.isEnabled = false

            axisLeft.apply {
                setDrawGridLines(false)
                setDrawLabels(false)
                setDrawAxisLine(false)
                removeAllLimitLines()

                // 수평선 추가
                addLimitLine(LimitLine(3.5f).apply {
                    lineWidth = 1f // 수평선 두께
                    lineColor = Color.BLUE // 수평선 색상
                    enableDashedLine(10f, 10f, 0f) // 점선 형태 설정
                    textSize = 12f
                })
                axisMinimum = 0f // y-축 최소값 설정
                axisMaximum = 6f // y-축 최대값 설정
            }

            axisRight.apply {
                setDrawGridLines(false)
                setDrawLabels(false)
                setDrawAxisLine(false)
            }

            xAxis.apply {
                enableGridDashedLine(16f, 12f, 0f)
                position = XAxis.XAxisPosition.BOTTOM
                setCenterAxisLabels(true)
            }

            animateXY(300, 300)
            data = chartData
            invalidate()
//            setTouchEnabled(true)
            data = chartData
            invalidate()
        }
    }

    private fun generateInitialData() {
        val random = java.util.Random()
        for (i in 0 until maxDataPoints) {
            val randomValue = (1 + random.nextInt(5)).toFloat()
            entry_chart1.add(Entry(i.toFloat(), randomValue))
        }
    }

    private fun addRandomDataPoint() {
        val random = java.util.Random()
        val randomValue = (1 + random.nextInt(5)).toFloat()

        if (entry_chart1.size >= maxDataPoints) {
            // 최대 데이터 포인트 개수에 도달하면 첫 번째 데이터 포인트를 제거
            entry_chart1.removeAt(0)
            // x 좌표 업데이트
            for (i in 0 until entry_chart1.size) {
                entry_chart1[i].x = i.toFloat()
            }
        }
        chartDataList.add(Entry(currentTime.toFloat(), randomValue))
        // 새 데이터 포인트 추가
        entry_chart1.add(Entry(currentTime.toFloat(), randomValue))
        currentTime += timeInterval

//        currentIndex++
    }

    private fun updateChart() {
        chartData.notifyDataChanged()
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
    }

    private fun updateRecyclerView() {
        // RecyclerView 어댑터에 데이터 변경을 알립니다.
        recyclerViewAdapter.notifyDataSetChanged()

        // 스크롤을 마지막 항목으로 이동시킵니다 (선택 사항).
        recyclerView.scrollToPosition(chartDataList.size - 1)
    }
}
