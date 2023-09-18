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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.cbnu.project.cpr.heartsignal.MainViewModel
import com.cbnu.project.cpr.heartsignal.PoseLandmarkerHelper
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.adapter.ChartDataRecyclerViewAdapter
import com.cbnu.project.cpr.heartsignal.databinding.FragmentCameraBinding
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random
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
    // Sound
    private var mediaPlayer: MediaPlayer? = null
    private val timer: CountDownTimer? = null
    private var mediaCountDown: MediaPlayer? = null
    //그래프 설정
    private lateinit var lineChart: LineChart
    private val entry_chart1 = ArrayList<Entry>()
    private lateinit var lineDataSet1: LineDataSet
    private lateinit var chartData: LineData
    private val xValues = mutableListOf<Float>()

    private val maxDataPoints = 100 // 최대 데이터 포인트 개수
    private var currentTime = 0.0 // 현재 시간 값
    private val timeInterval = 0.5 // 핸들러 호출 간격 (초)

    // 리사이클러 뷰
    private lateinit var recyclerView: RecyclerView
    private lateinit var chartDataList: ArrayList<Entry>
    private lateinit var recyclerViewAdapter: ChartDataRecyclerViewAdapter

    // lottie
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var lottie_count: LottieAnimationView

    // horizontal bar
    private lateinit var horizontalBarChart: HorizontalBarChart
    private val bar_entries = ArrayList<BarEntry>()
    private lateinit var barDataSet: BarDataSet
    private lateinit var barData : BarData

    //tickView
    private var countdownJob: Job? = null
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
        mediaPlayer?.release()
        timer?.cancel()
        countdownJob?.cancel()

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
        recyclerView = fragmentCameraBinding.recyclerView
        chartDataList = ArrayList()

        // horizontal
        horizontalBarChart = fragmentCameraBinding.horizontalBar
        // RecyclerView 설정
        recyclerViewAdapter = ChartDataRecyclerViewAdapter(chartDataList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = recyclerViewAdapter
        // "삐" 소리 재생을 위한 MediaPlayer 초기화
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.mp_beep)
        mediaCountDown = MediaPlayer.create(requireContext(), R.raw.countdown)
        // lottie
        lottieAnimationView = fragmentCameraBinding.lottie
        lottie_count = fragmentCameraBinding.lottieCount
        //tickerView
        tickerView = fragmentCameraBinding.tickerView
        tickerView.setCharacterLists(TickerUtils.provideNumberList())


        // 블루투스 데이터를 수신하기 위해 로컬 브로드캐스트 수신기를 등록합니다.
        val intentFilter = IntentFilter("BLUETOOTH_DATA_RECEIVED")
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "BLUETOOTH_DATA_RECEIVED") {
                    val data = intent.getStringExtra("data")
                    // 수신한 데이터를 필요에 따라 처리합니다.
                    // if data가 스위치 클릭한 데이터일때,
                    Log.d(TAG, "Received data_f: $data")
                    // 데이터 처리 중으로 플래그 설정
                    // 데이터 처리 중인 경우에만 작업을 실행
                    if(data!!.contains("SO")) {
                        if (!processingData) {
                            launchDataProcessing()

                            generateInitialData()
                            setUpBarChartData()
                            //        setupLineChart()
                            createChartData()
                        }
                    }
                    else {
                        if(data!!.contains("SF"))
                        {
                            // 데이터 초기화
                            entry_chart1.clear()
                            xValues.clear()
                            bar_entries.clear()
                            //TODO 관련 데이터 초기화

                        }
                        else{
                            updateUI(data)
                        }
                    }
                }
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, intentFilter)
        return fragmentCameraBinding.root
    }
    // UI 업데이트를 비동기로 처리하는 함수
    private fun updateUI(data: String?) {
        // 코루틴을 사용하여 메인 스레드에서 UI 업데이트 작업을 실행
        lifecycleScope.launch(Dispatchers.Main) {
            // UI 업데이트 작업을 여기에 배치
            addBluetoothDataPoint(data!!)
            updateChart()
        }
    }


    private fun launchDataProcessing() {
        processingData = true // 데이터 처리 중으로 플래그 설정
        countDown()
    }

    private suspend fun startCountdown() {
        var remainingTimeInSeconds = 60 // 초기값을 60으로 설정하여 60부터 시작

        while (isActive && remainingTimeInSeconds >= 0) {
//            val hours = remainingTimeInSeconds / 3600
            val minutes = (remainingTimeInSeconds % 3600) / 60
            val secondsRemaining = remainingTimeInSeconds % 60

            val timeString = String.format("%02d:%02d", minutes, secondsRemaining)

            withContext(Dispatchers.Main) {
                tickerView.text = timeString
            }

            delay(1000) // 1초 간격으로 업데이트

            remainingTimeInSeconds-- // 1초씩 감소
        }
    }



    private fun showLottieAnimation() {
        lottieAnimationView.repeatCount = 60
        lottieAnimationView.backgroundTintMode
        lottieAnimationView.playAnimation()
        lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }
            override fun onAnimationEnd(animation: Animator) {

            }

            override fun onAnimationCancel(animation: Animator) {

            }
            override fun onAnimationRepeat(animation: Animator) {
                // 현재 프레임을 로그로 출력합니다.
                Log.d("LottieAnimation", "Current Time: $currentTime")

//                 원하는 프레임에서 이미지를 변경하려면 여기에서 조건문을 사용하여 작업을 수행하면 됩니다.
                if (currentTime in 15f..30f) {
                    lottieAnimationView.setAnimation(R.raw.heart_bad2)
                    lottieAnimationView.repeatCount = 30
                    lottieAnimationView.playAnimation()
                }
                else if (currentTime in 30f..45f)
                {
                    lottieAnimationView.setAnimation(R.raw.heart_bad3)
                    lottieAnimationView.repeatCount = 30
                    lottieAnimationView.playAnimation()
                }
                else if (currentTime in 45f..60f)
                {
                    lottieAnimationView.setAnimation(R.raw.heart_good1)
                    lottieAnimationView.repeatCount = 30
                    lottieAnimationView.playAnimation()
                }
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
    private fun countDown() {
        // LottieAnimationView를 맨 앞으로 가져옵니다.
        lottie_count.visibility = View.VISIBLE
        lottie_count.bringToFront()
        lottie_count.playAnimation()
        playCountDownSound()
        lottie_count.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }
            override fun onAnimationEnd(animation: Animator) {
                fragmentCameraBinding.lottieCount.visibility = View.GONE
                countdownJob = CoroutineScope(Dispatchers.Main).launch {
                    startCountdown()
                }
                showChart()
            }

            override fun onAnimationCancel(animation: Animator) {

            }
            override fun onAnimationRepeat(animation: Animator) {

            }
        })
    }

    private fun showChart() {
        // 그래프 설정 및 초기 데이터 채우기


        // 코루틴을 사용한 실시간 업데이트
        lifecycleScope.launch {
            val delayMillis = 500L // 0.5초마다 업데이트

            while (true) {
                // 새 데이터 포인트 추가
                updateBarChartData()

                // UI 업데이트를 메인 스레드에서 수행
                withContext(Dispatchers.Main) {
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
    private fun playCountDownSound() {
        mediaCountDown?.seekTo(0) // 소리를 처음부터 재생
        mediaCountDown?.start() // 소리 재생
        mediaCountDown?.setOnCompletionListener { mp ->
            // 미디어 재생이 완료될 때 실행할 동작을 여기에 추가합니다.
            // 예를 들어, 종료 메시지를 표시하거나 다른 작업을 수행할 수 있습니다.
            // 미디어 플레이어 해제
            mp.release()
        }
    }


    private fun setupLineChart() {
        lineDataSet1 = LineDataSet(entry_chart1, "LineGraph1")

        // 선의 색상을 보라색으로 변경
        lineDataSet1.color = R.drawable.fade

        // 그래프 내부를 색상으로 채우기
        lineDataSet1.setDrawFilled(true)
        lineDataSet1.fillColor = R.drawable.fade
//        lineDataSet1.fillAlpha = 30 // 채우기 색상의 투명도 설정 (0-255 사이의 값)
        val minX = xValues.minOrNull() ?: 0f
        val maxX = xValues.maxOrNull() ?: 30f  // 기본값 설정 (원하는 범위로 설정)
        Log.e(TAG, "minX: $minX, maxX: $maxX")

//        lineDataSet1.mode = LineDataSet.Mode.LINEAR
//        lineDataSet1.cubicIntensity = 0.1f
        lineDataSet1.setDrawCircleHole(false)
        lineDataSet1.setDrawCircles(false)
        lineDataSet1.setDrawValues(false)
        lineChart.xAxis.axisMaximum = maxX
        lineChart.xAxis.axisMinimum = minX


        chartData = LineData(lineDataSet1)

        lineChart.apply {
            setTouchEnabled(true)
            isClickable = false
            isDoubleTapToZoomEnabled = false
            setDrawBorders(false)
            setDrawGridBackground(false)
            description.isEnabled = false
            legend.isEnabled = false
            isAutoScaleMinMaxEnabled = true

            axisLeft.apply {
                setDrawGridLines(false)
                setDrawLabels(false)
                setDrawAxisLine(false)
                removeAllLimitLines()

                // 수평선 추가
                addLimitLine(LimitLine(400.5f).apply {
                    lineWidth = 1f // 수평선 두께
                    lineColor = Color.BLUE // 수평선 색상
                    enableDashedLine(10f, 10f, 0f) // 점선 형태 설정
                    textSize = 12f
                })
                addLimitLine(LimitLine(500.5f).apply {
                    lineWidth = 1f // 수평선 두께
                    lineColor = Color.BLUE // 수평선 색상
                    enableDashedLine(10f, 10f, 0f) // 점선 형태 설정
                    textSize = 12f
                })
//                axisMinimum = 0f // y-축 최소값 설정
//                axisMaximum = 6f // y-축 최대값 설정
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
                // x축의 범위를 업데이트합니다.

            }

//            animateXY(500, 500)
            data = chartData
            invalidate()
//            setTouchEnabled(true)
        }
    }


    private fun generateInitialData() {
        entry_chart1.add(Entry(0f, 0f))
    }

//    private fun addRandomDataPoint() {
//        val random = java.util.Random()
//        val randomValue = (1 + random.nextInt(5)).toFloat()
//
//        if (entry_chart1.size >= maxDataPoints) {
//            // 최대 데이터 포인트 개수에 도달하면 첫 번째 데이터 포인트를 제거
//            entry_chart1.removeAt(0)
//            // x 좌표 업데이트
//            for (i in 0 until entry_chart1.size) {
//                entry_chart1[i].x = i.toFloat()
//            }
//        }
//        chartDataList.add(Entry(currentTime.toFloat(), randomValue))
//        // 새 데이터 포인트 추가
//        entry_chart1.add(Entry(currentTime.toFloat(), randomValue))
//        currentTime += timeInterval
//    }

    private fun addBluetoothDataPoint(data: String) {
        if(data.contains("_"))
        {
            val parts = data.replace("?","").split("_")
            val time = (parts[0].toFloat() / 1000) // 첫 번째 부분
            val intensity = parts[1].toFloat()

            xValues.add(time)
            Log.d("CAMERAFRAGMENT", xValues.toString())

            // x 좌표 값 추가 및 관리
            if (entry_chart1.size > maxDataPoints) {
                // x 좌표 값이 최대 개수를 초과하면 이전 값 제거
                entry_chart1.removeAt(0)
                xValues.removeAt(0)
                for (i in 0 until entry_chart1.size) {
                    if (i < xValues.size) {
                        entry_chart1[i].x = xValues[i]
                    }
                }
            }

            chartDataList.add(Entry(time, intensity))
            entry_chart1.add(Entry(time, intensity))
            currentTime += timeInterval

            setupLineChart()
        }
    }
    private fun updateChart() {


        chartData.notifyDataChanged()
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()

        barData.notifyDataChanged()
        horizontalBarChart.notifyDataSetChanged()
        horizontalBarChart.invalidate()
    }

    private fun updateRecyclerView() {
        // RecyclerView 어댑터에 데이터 변경을 알립니다.
        recyclerViewAdapter.notifyDataSetChanged()

        // 스크롤을 마지막 항목으로 이동시킵니다 (선택 사항).
        recyclerView.scrollToPosition(chartDataList.size - 1)
    }



    private fun setUpBarChartData () {
        // 1. [BarEntry] BarChart에 표시될 데이터 값 생성
        for (i in 0 until 4) {
            val x = i.toFloat()
            val y: Float = 0.0F
            bar_entries.add(BarEntry(x, y))
        }
    }

    private fun createChartData() {
        val apps = arrayOf("김민정", "정상수", "카리나", "마틴루터")

        // 2. [BarDataSet] 단순 데이터를 막대 모양으로 표시, BarChart의 막대 커스텀
        barDataSet = BarDataSet(bar_entries, "압박 성공 횟수")
        barDataSet.setDrawIcons(false)
        barDataSet.setDrawValues(true)
        barDataSet.color = Color.parseColor("#66767676") // 색상 설정
        // 데이터 값 원하는 String 포맷으로 설정하기 (ex. ~회)
        barDataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString() + "회"
            }
        }
        // 3. [BarData] 보여질 데이터 구성
        barData = BarData(barDataSet)
        barData.barWidth = 0.5f
        barData.setValueTextSize(15f)

        horizontalBarChart.axisLeft.apply {
            axisMinimum = 0f     // 그래프의 시작점을 0으로 설정
            axisMaximum = 120f   // 그래프의 최댓값을 120으로 설정
        }
        horizontalBarChart.xAxis.apply {
            labelCount = apps.size // 라벨 개수 설정
            setDrawGridLines(false) // X축 격자선 제거
            setDrawAxisLine(true) // X축 라인 표시
            position = XAxis.XAxisPosition.BOTTOM // X축 위치 설정
//            labelRotationAngle = 30f // 라벨의 각도 설정
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    if (index >= 0 && index < apps.size) {
                        return apps[index]
                    }
                    return ""
                }
            }
        }
        // 오른쪽 Y축 설정 숨기기
        horizontalBarChart.axisRight.isEnabled = false
        // Description 제거
        horizontalBarChart.description.isEnabled = false

        horizontalBarChart.data = barData
        horizontalBarChart.invalidate()
    }

    private fun updateBarChartData() {
        val random = Random()
        for (entry in bar_entries) {
            // 0 또는 1을 더한 값으로 업데이트
            entry.y = entry.y + random.nextInt(2)
        }
    }

}
