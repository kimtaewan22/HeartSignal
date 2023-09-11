package com.google.mediapipe.examples.poselandmarker.chart

import android.graphics.Color
import androidx.core.content.ContextCompat.getColor
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.mediapipe.examples.poselandmarker.R

class LineChart {

    private fun showLineChart(list: ArrayList<Int>, lineChart: LineChart, ) {
        val entry_chart1 = ArrayList<Entry>()
        var previousValue = 0.0f

        for (i in list.indices) {
            val value = list[i].toFloat()
            val entryValue: Float

            if (value == 0F) {
                // When value is 0, assign a slightly larger value than the previous value
                entryValue = previousValue + 0.1f
            } else {
                // When value is 1, assign a larger value than the previous value
                entryValue = previousValue + 0.5f
            }

            entry_chart1.add(Entry(i.toFloat(), entryValue))
            previousValue = entryValue
        }

        val lineDataSet1 = LineDataSet(entry_chart1, "LineGraph1")
        lineDataSet1.color = Color.RED
        lineDataSet1.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet1.cubicIntensity = 0.2f
        //lineDataSet1.setCircleColor(getColor(R.color.blue_grey_400))
        lineDataSet1.setDrawCircleHole(false)
        lineDataSet1.setDrawValues(false)

        val chartData = LineData(lineDataSet1)

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
                addLimitLine(LimitLine(150f, "Upper Limit").apply {
                    lineWidth = 4f
                    enableDashedLine(10f, 10f, 0f)
                    labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                    //lineColor = getColor(R.color.blue_grey_400)
                    textSize = 10f
                })
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

            animateXY(2000, 2000)
            data = chartData
            invalidate()
            setTouchEnabled(true)
        }
    }
}