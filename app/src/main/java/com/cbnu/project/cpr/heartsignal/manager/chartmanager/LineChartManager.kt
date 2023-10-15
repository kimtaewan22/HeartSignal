package com.cbnu.project.cpr.heartsignal.manager.chartmanager

import android.graphics.Color
import android.util.Log
import com.cbnu.project.cpr.heartsignal.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

object LineChartManager {

    private lateinit var m_lineChart: LineChart
    private val lineDataSetList = ArrayList<Entry>() // entry_chart1
    private val xValues = mutableListOf<Float>()
    private val maxDataPoints = 100 // 최대 데이터 포인트 개수
    private val TotalDataSet = ArrayList<Entry>()



    fun initialize(lineChart: LineChart) {
        m_lineChart = lineChart
        setupLineChart() // Call setup once when initializing
    }

    fun setData() {

    }

    fun setupLineChart() {
        Log.d("CAMERAFRAGMENT_addBLEDataToList_setupLineChart", lineDataSetList.toString())

        m_lineChart?.apply {
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
                axisMaximum = 300f // y-축 최대값 설정
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
        if(bleData.contains("_"))
        {
            val parts = bleData.replace("?","").split("_")
            val time = (parts[0].toFloat() / 1000) // 첫 번째 부분
            val intensity = parts[1].toFloat()

            xValues.add(time)
//            Log.d("CAMERAFRAGMENT_addBLEDataToList", xValues.toString())

            // x 좌표 값 추가 및 관리
            if (lineDataSetList.size > maxDataPoints) {
                // x 좌표 값이 최대 개수를 초과하면 이전 값 제거
                lineDataSetList.removeAt(0)
                xValues.removeAt(0)
                for (i in 0 until lineDataSetList.size) {
                    if (i < xValues.size) {
                        lineDataSetList[i].x = xValues[i]
                    }
                }
            }
            lineDataSetList.add(Entry(time, intensity))
            var lineDataSet = LineDataSet(lineDataSetList, "LineGraph1")
            // 선의 색상을 보라색으로 변경
            lineDataSet.color = R.drawable.fade

            // 그래프 내부를 색상으로 채우기
            lineDataSet.setDrawFilled(true)
            lineDataSet.fillColor = R.drawable.fade
//        lineDataSet1.fillAlpha = 30 // 채우기 색상의 투명도 설정 (0-255 사이의 값)
            val minX = xValues.minOrNull() ?: 0f
            val maxX = xValues.maxOrNull() ?: 30f  // 기본값 설정 (원하는 범위로 설정)

//        lineDataSet1.mode = LineDataSet.Mode.LINEAR
//        lineDataSet1.cubicIntensity = 0.1f
            lineDataSet.setDrawCircleHole(false)
            lineDataSet.setDrawCircles(false)
            lineDataSet.setDrawValues(false)
            m_lineChart.xAxis?.axisMaximum = maxX
            m_lineChart.xAxis?.axisMinimum = minX

            var lineData = LineData(lineDataSet)
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
//        m_lineChart.invalidate()
//        m_lineChart.clear()
    }

    fun setUpTotalData(){

    }

}