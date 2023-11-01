package com.cbnu.project.cpr.heartsignal.manager.chartmanager

import android.R.attr.entries
import android.graphics.Color
import android.util.Log
import com.cbnu.project.cpr.heartsignal.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import java.util.Collections


object LineChartManager {
    private lateinit var m_lineChart: LineChart
    private val lineDataSetList = ArrayList<Entry>() // entry_chart1
    private val xValues = mutableListOf<Float>()
    private val maxDataPoints = 30 // 최대 데이터 포인트 개수
    private val totalDataSet = ArrayList<Entry>()



    fun initialize(lineChart: LineChart) {
        m_lineChart = lineChart
        setupLineChart() // Call setup once when initializing
    }

    fun setData() {

    }

    fun setupLineChart() {
        Log.d("CAMERAFRAGMENT_addBLEDataToList_setupLineChart", lineDataSetList.toString())

        m_lineChart.apply {
            setTouchEnabled(true)
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
                axisMinimum = 0f // y-축 최소값 설정
                axisMaximum = 200f // y-축 최대값 설정
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
        }
    }

    fun addBLEDataToList(bleData: String) {
        if (bleData.contains("_")) {
            val parts = bleData.replace("?", "").split("_")
            val time = (parts[0].toFloat() / 1000) // 첫 번째 부분
            val intensity = parts[1].split(",")[0].toFloat()

            Log.d("CAMERAFRAGMENT_addBLEDataToList_setupLineChartData", "time :${time}, intensity: ${intensity}")

//            if (lineDataSetList.size >= maxDataPoints) {
//                // x 좌표 값이 최대 개수를 초과하면 이전 값 제거
//                lineDataSetList.removeAt(0)
//                xValues.removeAt(0)
//            }
            totalDataSet.add(Entry(time, intensity))
            lineDataSetList.add(Entry(time, intensity))
            xValues.add(time)

            Collections.sort(lineDataSetList, EntryXComparator())

            var lineDataSet = LineDataSet(lineDataSetList, "LineGraph1")
            lineDataSet.color = R.drawable.fade
            lineDataSet.setDrawFilled(true)
            lineDataSet.fillColor = R.drawable.fade
            lineDataSet.setDrawCircleHole(false)
            lineDataSet.setDrawCircles(false)
            lineDataSet.setDrawValues(false)

            // Ensure LineDataSet is not null
            if (lineDataSet != null) {
                // xValues가 비어있지 않은 경우에만 x축 범위를 업데이트합니다.
                if (xValues.size >= 2) { // 최소 2개의 값을 가지고 있을 때만 첫 번째와 마지막 값을 사용
                    m_lineChart.xAxis?.axisMinimum = xValues.first()
                    m_lineChart.xAxis?.axisMaximum = xValues.last()
                } else {
                    // xValues가 비어있을 때 또는 한 개의 값만 있을 때 예외 처리
                    m_lineChart.xAxis?.axisMinimum = 0f
                    m_lineChart.xAxis?.axisMaximum = 1f
                }

                val lineData = LineData(lineDataSet)
                m_lineChart.data = lineData
            }
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
//        m_lineChart.invalidate()
//        m_lineChart.clear()
    }

    fun setUpTotalData(){

    }
}