package com.cbnu.project.cpr.heartsignal.manager.chartmanager

import android.graphics.Color
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter

class BarChartManager {
    private lateinit var horizontalBarChart: HorizontalBarChart
    private val bar_entries = ArrayList<BarEntry>()
    private lateinit var barDataSet: BarDataSet
    private lateinit var barData : BarData

    private fun createBarChartData() {
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
}