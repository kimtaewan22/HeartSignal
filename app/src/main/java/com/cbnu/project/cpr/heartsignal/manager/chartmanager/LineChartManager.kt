package com.cbnu.project.cpr.heartsignal.manager.chartmanager

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.data.TimeIntensityPair
import com.cbnu.project.cpr.heartsignal.manager.framemanager.FrameBackgroundManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet



object LineChartManager {

    private lateinit var m_lineChart: LineChart
    private val lineDataSetList = ArrayList<Entry>() // entry_chart1
    private val xValues = mutableListOf<Float>()
    private val totalDataSet = ArrayList<Entry>()

    private var lastTime = 0f
    private var a_bpm = 0f
    private var compressionCount = ""
    private var compressionSuccessCount = ""
    private var isUpdating = false // Runnable 실행 여부를 추적하는 플래그
    private var dataCollection = mutableListOf<String>()

//    private var trueCount = 0
//    private var falseCount = 0
//
//    private var count0_1 = 0
//    private var count0_2 = 0
//    private var count0_3 = 0
//    private var count0_4 = 0
//    private var count1_2 = 0
//    private var count1_3 = 0
//    private var count1_4 = 0
//    private var count2_3 = 0
//    private var count2_4 = 0
//    private var count3_4 = 0


    val timeIntensityPairs = mutableListOf<TimeIntensityPair>()
    private val handler = Handler(Looper.getMainLooper())
    private val updateLastTimeRunnable: Runnable = object : Runnable {
        override fun run() {
            isUpdating = true
            ProgressiveGaugeManager.setupSpeedText(a_bpm)
            handler.postDelayed(this, 2000) // 3 초 후에 다시 실행
            isUpdating = false
//            dataCollection.clear() // 이전 데이터 초기화

        }
    }

    fun initialize(lineChart: LineChart) {
        m_lineChart = lineChart
        setupLineChart() // Call setup once when initializing
    }

    fun getTotalDataSet(): ArrayList<Entry> {
        return totalDataSet
    }

    fun resetTotalDataset() {
        totalDataSet.clear()
    }

    fun setupLineChart() {
        Log.d("CAMERAFRAGMENT_addBLEDataToList_setupLineChart", lineDataSetList.toString())

        m_lineChart.apply {
//            setVisibleXRange(0f, 60f)
            setTouchEnabled(false) // 이제 그래프는 터치에 반응하지 않습니다.
            isClickable = false
            isDoubleTapToZoomEnabled = false
            setDrawBorders(false)
            setDrawGridBackground(false)
            description.isEnabled = false
            legend.isEnabled = false
            isAutoScaleMinMaxEnabled = false

            axisLeft.apply {
                setDrawGridLines(false)
                setDrawLabels(false)
                setDrawAxisLine(false)
                removeAllLimitLines()

//                // 수평선 추가
//                addLimitLine(LimitLine(400.5f).apply {
//                    lineWidth = 1f // 수평선 두께
//                    lineColor = Color.BLUE // 수평선 색상
//                    enableDashedLine(10f, 10f, 0f) // 점선 형태 설정
//                    textSize = 12f
//                })
//                addLimitLine(LimitLine(500.5f).apply {
//                    lineWidth = 1f // 수평선 두께
//                    lineColor = Color.BLUE // 수평선 색상
//                    enableDashedLine(10f, 10f, 0f) // 점선 형태 설정
//                    textSize = 12f
//                })
                axisMinimum = 0f // y-축 최소값 설정
                axisMaximum = 120f // y-축 최대값 설정
            }

            axisRight.apply {

                setDrawGridLines(false)
                setDrawLabels(false)
                setDrawAxisLine(false)

            }

            xAxis.apply {
                setVisibleXRangeMaximum(100f)
                axisMinimum = 0f // X축 최소값 설정
                axisMaximum = 60f // X축 최대값 설정
                setDrawLabels(false) // X축 라벨을 삭제합니다.
                enableGridDashedLine(16f, 12f, 0f)
                position = XAxis.XAxisPosition.BOTTOM
                setCenterAxisLabels(true)
            }
        }
    }

    fun addBLEDataToList(bleData: String) {
        if (bleData.contains("_")) {
//            Log.d("CAMERAFRAGMENT_addBLEDataToList_Counting", "top-2: $bleData")

            val parts = bleData.replace("?", "").split("_")
            val time = (parts[0].toFloat() / 1000) // 첫 번째 부분
            val intensity = parts[1].split(",")[0].toFloat()
//            val top_2 = parts[2].split(",")
            val top_2 = parts[2]
            val count = parts[3]
            val bpm = parts[4].toFloat()
            val success_count = parts[5].trim().replace("?", "")
            determineDirectionAfterTwoSeconds()

            dataCollection.add(top_2)
            Log.d("CAMERAFRAGMENT_addBLEDataToList_Counting_S", "dataCollection: ${dataCollection}")


//            val successFlag = parts[2].split(",").last()
//            Log.d("CAMERAFRAGMENT_addBLEDataToList_Counting", "successFlag: ${successFlag}")
//
//            if (successFlag == "true") {
//                trueCount++
//                Log.d("CAMERAFRAGMENT_addBLEDataToList_Counting_S", "successFlag: ${successFlag}")
//
//            } else {
//                falseCount++
//                Log.d("CAMERAFRAGMENT_addBLEDataToList_Counting_F", "successFlag: ${successFlag}")
//                updateDirectionCount(parts[2].split(",").take(2).joinToString(","))
//            }


            a_bpm = bpm
            compressionCount = count
            compressionSuccessCount = success_count.replace("?","")
            Log.d("CAMERAFRAGMENT_addBLEDataToList_Counting", "top-2: ${top_2}, count: ${count}, bpm: ${bpm}, success_count: ${compressionSuccessCount}")

            lastTime = time
            // Runnable 실행 전에 isUpdating 상태 확인
            if (!isUpdating) {
                handler.post(updateLastTimeRunnable)
            }

//            determineDirectionAfterTwoSeconds()
            totalDataSet.add(Entry(time, intensity))
            lineDataSetList.add(Entry(time, intensity))
            if(intensity.toInt() >= 110)
            {
                VerticalProgressbarManager.setProgress(0)
            } else {
                val progress = 110 - intensity.toInt()
                VerticalProgressbarManager.setProgress(progress)
            }
            xValues.add(time)

            var lineDataSet = LineDataSet(lineDataSetList, "LineGraph1")
            lineDataSet.color = R.drawable.fade
            lineDataSet.setDrawFilled(true)
            lineDataSet.fillColor = R.drawable.fade
            lineDataSet.setDrawCircleHole(false)
            lineDataSet.setDrawCircles(false)
            lineDataSet.setDrawValues(false)


            val lineData = LineData(lineDataSet)
            m_lineChart.data = lineData
        }
    }

    fun generateInitialData(){
        lineDataSetList.add(Entry(0f,0f))
        xValues.add(0f)
    }

    fun updateLineChart() {
//        lineData.notifyDataChanged()
        m_lineChart.notifyDataSetChanged()
        m_lineChart.invalidate()
    }

    fun clearData() {
        lineDataSetList.clear()
        xValues.clear()
        timeIntensityPairs.clear()
        stopUpdatingLastTime()
//        m_lineChart.invalidate()
//        m_lineChart.clear()
    }

//    fun findTopTwoIndices(intensityValues: List<Float>): List<Int> {
//        // 가장 큰 두 값의 인덱스를 찾기
//        val topTwoIndices = intensityValues
//            .mapIndexed { index, value -> index to value } // 인덱스와 값을 쌍으로 변환
//            .sortedByDescending { it.second } // 값에 따라 내림차순으로 정렬
//            .take(2) // 상위 2개 항목 선택
//            .map { it.first } // 인덱스만 추출
//
//        return topTwoIndices
//    }

    fun stopUpdatingLastTime() {
        handler.removeCallbacks(updateLastTimeRunnable) // Runnable 중지
    }

    fun getCompressionCount(): String {
        return compressionCount
    }

    fun getCompressionSuccessCount(): String {

        return compressionSuccessCount

    }
//    fun updateDirectionCount(top_2: String) {
//        // 'top_2'에서 조합 추출
//        val parts = top_2.split(",").map { it.toInt() }
//
//        if (parts.size == 2 && parts.all { it in 0..4 }) {
//            val key = parts.sorted().joinToString(",") // 정렬하여 키 생성
//            directionCountMap[key] = directionCountMap.getOrDefault(key, 0) + 1
//        }
//    }
//    fun updateDirectionCount(top_2: String) {
//        val parts = top_2.split(",").map { it.toInt() }.sorted()
//
//        if (parts.size == 2 && parts.all { it in 0..4 }) {
//            when (parts.joinToString(",")) {
//                "0,1" -> count0_1++
//                "0,2" -> count0_2++
//                "0,3" -> count0_3++
//                "0,4" -> count0_4++
//                "1,2" -> count1_2++
//                "1,3" -> count1_3++
//                "1,4" -> count1_4++
//                "2,3" -> count2_3++
//                "2,4" -> count2_4++
//                "3,4" -> count3_4++
//            }
//        }
//        Log.d("CAMERAFRAGMENT_addBLEDataToList_trueCount", "count0_1: ${count0_1}, count0_2: ${count0_2} count0_3: ${count0_3}, count0_4: ${count0_4} " +
//                "count1_2: ${count1_2}, count1_3: ${count1_3} count1_4: ${count1_4}, count2_3: ${count2_3} count2_4: ${count2_4}, count3_4: ${count3_4}")
//    }

    // 가장 많이 발생한 조합 찾기
//    fun findMostCommonDirection(): String {
//        val counts = listOf(
//            "0,1" to count0_1,
//            "0,2" to count0_2,
//            "0,3" to count0_3,
//            "0,4" to count0_4,
//            "1,2" to count1_2,
//            "1,3" to count1_3,
//            "1,4" to count1_4,
//            "2,3" to count2_3,
//            "2,4" to count2_4,
//            "3,4" to count3_4
//            )
//        return counts.maxByOrNull { it.second }?.first.orEmpty()
//    }
//    fun determineDirectionAfterTwoSeconds() {
//        val mostCommonData = dataCollection
//            .groupingBy { it }
//            .eachCount()
//            .maxByOrNull { it.value }
////        Log.d("CAMERAFRAGMENT_addBLEDataToList_trueCount", "trueCount-2: ${trueCount}, falseCount: ${falseCount}, max: ${max}")
//
//
//        FrameBackgroundManager.colorClear()
//
////        Log.d("CAMERAFRAGMENT_addBLEDataToList_trueCount", "trueCount-2: ${trueCount}, falseCount: ${falseCount}, max: ${max}")
//
////        FrameBackgroundManager.colorClear()
////        if(trueCount>= falseCount){
////            FrameBackgroundManager.colorcorrect()
////            trueCount = 0
////            falseCount = 0
////            clearDirectionList()
////            return
////        }
////        else{
////            when(mostCommon?.key)
////            {
////                "N" -> FrameBackgroundManager.colorUpdateTop() // 위 방향 처리
////                "E" -> FrameBackgroundManager.colorUpdateRight() // 우측 방향 처리
////                "S" -> FrameBackgroundManager.colorUpdateBottom() // 아래 방향 처리
////                "W" -> FrameBackgroundManager.colorUpdateLeft()// 좌측 방향 처리
////                "AC" -> FrameBackgroundManager.colorcorrect()
////                "ADC" -> FrameBackgroundManager.colorDiscorrect()
//////                else -> FrameBackgroundManager.colorDiscorrect() // 올바르지 않은 값
////            }
////        }
//        when(mostCommonData?.key)
//            {
//            "N" -> FrameBackgroundManager.colorUpdateTop()
//            "E" -> FrameBackgroundManager.colorUpdateRight()
//            "S" -> FrameBackgroundManager.colorUpdateBottom()
//            "W" -> FrameBackgroundManager.colorUpdateLeft()
//            "AC" -> FrameBackgroundManager.colorcorrect()
//            "ADC" -> FrameBackgroundManager.colorDiscorrect()
//            else -> {}
//        }
//        Log.d("CAMERAFRAGMENT_addBLEDataToList_Counting_F", "가장 많이 나타난 값: ${mostCommonData?.key},횟수: ${mostCommonData?.value}")
//
////        println("가장 많이 나타난 값: ${mostCommon?.key}, 횟수: ${mostCommon?.value}")
//
//    }

    fun determineDirectionAfterTwoSeconds() {
        val mostCommonData = dataCollection
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }


        if (mostCommonData != null && mostCommonData.value >= 10) {
            dataCollection.clear()
            when (mostCommonData.key) {
                "N" -> FrameBackgroundManager.colorUpdateTop()
                "E" -> FrameBackgroundManager.colorUpdateRight()
                "S" -> FrameBackgroundManager.colorUpdateBottom()
                "W" -> FrameBackgroundManager.colorUpdateLeft()
                "AC" -> FrameBackgroundManager.colorcorrect()
                "ADC" -> FrameBackgroundManager.colorDiscorrect()
            }
        }

        Log.d("CAMERAFRAGMENT_addBLEDataToList_Counting_F", "가장 많이 나타난 값: ${mostCommonData?.key},횟수: ${mostCommonData?.value}")
    }

}