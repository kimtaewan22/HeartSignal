package com.cbnu.project.cpr.heartsignal.fragment.mainFragment


import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.databinding.FragmentMainHomeBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.marvel999.acr.ArcProgress

class MainHomeFragment : Fragment() {

    private var _binding: FragmentMainHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var radarChart: RadarChart
    private lateinit var progress: ProgressBar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        radarChart = binding.homeRadarChart
        radarChart.setBackgroundColor(Color.rgb(60, 65, 82))
        radarChart.description.isEnabled = false

        // Initialize and set up the chart
        initializeRadarChart()
        setData()

        radarChart.animateXY(1400, 1400, Easing.EaseInOutQuad)

        setUpArcProgress()


        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpArcProgress() {
        val arc_img: ArcProgress = binding.arcImg
        val progress = 50f
        arc_img.setProgressWithAnimation(progress)
    }

//    private fun setUpProgress() {
//        progress = binding.progressbar
//        // 프로그레스바의 현재 값
//        val currentProgress = progress.progress
//        // 원하는 최종 프로그레스 값
//        val targetProgress = 70
//
//        // ValueAnimator를 사용하여 애니메이션 생성
//        val animator = ValueAnimator.ofInt(currentProgress, targetProgress)
//        animator.duration = 1000 // 애니메이션 지속 시간 (밀리초)
//
//        animator.addUpdateListener { valueAnimator ->
//            val animatedValue = valueAnimator.animatedValue as Int
//            progress.progress = animatedValue // 프로그레스바 값 업데이트
//        }
//        animator.start() // 애니메이션 시작
//    }

    private fun initializeRadarChart() {
        radarChart.webLineWidth = 1f
        radarChart.webColor = Color.LTGRAY
        radarChart.webLineWidthInner = 1f
        radarChart.webColorInner = Color.LTGRAY
        radarChart.webAlpha = 100


        val xAxis = radarChart.xAxis
        xAxis.textSize = 12f
        xAxis.yOffset = 0f
        xAxis.xOffset = 0f
        val labels = arrayOf("압박 강도", "음성 인식", "압박 횟수", "시간", "정확도")

        xAxis.setValueFormatter(object : IndexAxisValueFormatter(labels){})
        xAxis.textColor = Color.WHITE

        val yAxis = radarChart.yAxis
//        yAxis.typeface = tfLight
        yAxis.setLabelCount(5, false)
        yAxis.textSize = 9f
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 80f
        yAxis.setDrawLabels(false)

        val l = radarChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
//        l.typeface = tfLight
        l.xEntrySpace = 7f
        l.yEntrySpace = 5f
        l.textColor = Color.WHITE
    }

    private fun setData() {
        val mul = 80f
        val min = 20f
        val cnt = 5

        val entries1 = ArrayList<RadarEntry>()
        val entries2 = ArrayList<RadarEntry>()

        for (i in 0 until cnt) {
            val val1 = (Math.random() * mul + min).toFloat()
            entries1.add(RadarEntry(val1))

            val val2 = (Math.random() * mul + min).toFloat()
            entries2.add(RadarEntry(val2))
        }

        val set1 = RadarDataSet(entries1, "저난주")
        set1.color = Color.rgb(103, 110, 129)
        set1.fillColor = Color.rgb(103, 110, 129)
        set1.setDrawFilled(true)
        set1.fillAlpha = 180
        set1.lineWidth = 2f
        set1.isDrawHighlightCircleEnabled = true
        set1.setDrawHighlightIndicators(false)

        val set2 = RadarDataSet(entries2, "이번주")
        set2.color = Color.rgb(121, 162, 175)
        set2.fillColor = Color.rgb(121, 162, 175)
        set2.setDrawFilled(true)
        set2.fillAlpha = 180
        set2.lineWidth = 2f
        set2.isDrawHighlightCircleEnabled = true
        set2.setDrawHighlightIndicators(false)

        val sets = ArrayList<IRadarDataSet>()
        sets.add(set1)
        sets.add(set2)

        val data = RadarData(sets)
//        data.setValueTypeface(tfLight)
        data.setValueTextSize(8f)
        data.setDrawValues(false)
        data.setValueTextColor(Color.WHITE)

        radarChart.data = data
        radarChart.invalidate()
    }


}