package com.cbnu.project.cpr.heartsignal.fragment.mainFragment


import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.custom.VectorDrawableTagItem
import com.cbnu.project.cpr.heartsignal.databinding.FragmentMainHomeBinding
import com.cbnu.project.cpr.heartsignal.fragment.BadgeShowFragment
import com.db.williamchart.slidertooltip.SliderTooltip
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.magicgoop.tagsphere.OnTagTapListener
import com.magicgoop.tagsphere.item.TagItem
import com.marvel999.acr.ArcProgress
import com.sdsmdg.harjot.rotatingtext.RotatingTextWrapper
import com.sdsmdg.harjot.rotatingtext.models.Rotatable
import es.dmoral.toasty.Toasty


class MainHomeFragment : Fragment() {

    private var _binding: FragmentMainHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var radarChart: RadarChart
    private lateinit var rotatingTextWrapper: RotatingTextWrapper
    private lateinit var rotatable: Rotatable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        radarChart = binding.homeRadarChart
        radarChart.setBackgroundColor(Color.rgb(60, 65, 82))
        radarChart.description.isEnabled = false
        val linechart = binding.lineChart

        linechart.gradientFillColors =
            intArrayOf(
                Color.parseColor("#81FFFFFF"),
                Color.TRANSPARENT
            )
        linechart.animation.duration = animationDuration
        linechart.tooltip =
            SliderTooltip().also {
                it.color = Color.WHITE
            }
        linechart.onDataPointTouchListener = { index, _, _ ->
            binding.lineChartValue.text =
                lineSet.toList()[index]
                    .second
                    .toString()
        }
        binding.lineChart.animate(lineSet)
        // Initialize and set up the chart
        initializeRadarChart()
        setRadarData()
        radarChart.animateXY(1400, 1400, Easing.EaseInOutQuad)

        setUpArcProgress()
        initTagView()
//        setUpRotatingText()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 다시 화면에 표시될 때 수행할 작업을 여기에 추가
    }

    override fun onResume() {
        super.onResume()
        // 다시 화면에 표시될 때 수행할 작업을 여기에 추가
    }
    private fun setUpArcProgress() {
        val arc_img: ArcProgress = binding.arcImg
        val progress = 50f
        arc_img.setProgressWithAnimation(progress)
    }

//    private fun setUpRotatingText() {
//        rotatingTextWrapper = binding.customSwitcher
//
//        rotatingTextWrapper.size = 30
//        rotatable = Rotatable(Color.parseColor("#FFA036"), 1500, "출석 달성도", "총 달성도", "미션 달성도")
//        rotatable.size = 25f
//        rotatable.isCenter = true
//        rotatable.animationDuration = 500
//
//        rotatingTextWrapper.setContent("내 미션패스 > ?", rotatable)
//    }


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
        xAxis.textSize = 16f
        xAxis.yOffset = 0f
        xAxis.xOffset = 0f
        val labels = arrayOf("A+\n압박 강도", "음성 인식", "압박 횟수", "시간", "정확도")


        xAxis.setValueFormatter(object : IndexAxisValueFormatter(labels){})
        xAxis.textColor = Color.WHITE
        xAxis.typeface = Typeface.DEFAULT_BOLD
        xAxis.setCenterAxisLabels(false)

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



    private fun setRadarData() {
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
        data.setValueTextSize(12f)
        data.setDrawValues(false)
        data.setValueTextColor(Color.WHITE)

        radarChart.data = data
        radarChart.invalidate()
    }


    companion object {
        private val lineSet = listOf(
            "label1" to 5f,
            "label2" to 4.5f,
            "label3" to 4.7f,
            "label4" to 3.5f,
            "label5" to 3.6f,
            "label6" to 7.5f,
            "label7" to 7.5f,
            "label8" to 10f,
            "label9" to 5f,
            "label10" to 6.5f,
            "label11" to 3f,
            "label12" to 4f
        )

        private val barSet = listOf(
            "JAN" to 4F,
            "FEB" to 7F,
            "MAR" to 2F,
            "MAY" to 2.3F,
            "APR" to 5F,
            "JUN" to 4F
        )

        private val horizontalBarSet = listOf(
            "PORRO" to 5F,
            "FUSCE" to 6.4F,
            "EGET" to 3F
        )

        private val donutSet = listOf(
            20f,
            80f,
            100f
        )

        private const val animationDuration = 1000L

        val drawableResList = listOf(
            R.drawable.ic_badge_01,
            R.drawable.ic_badge_02,
            R.drawable.ic_badge_03,
            R.drawable.ic_badge_04,
            R.drawable.ic_badge_05,
            R.drawable.ic_badge_06,
            R.drawable.ic_badge_07,
            R.drawable.ic_badge_08,
            R.drawable.ic_badge_09,
            R.drawable.ic_badge_10,
            R.drawable.ic_badge_11,
            R.drawable.ic_badge_12
        )
    }

    private fun initTagView() {
        val tags = mutableListOf<VectorDrawableTagItem>()
        drawableResList.forEach { id ->
            getVectorDrawable(id)?.let {
                tags.add(VectorDrawableTagItem(it))
            }
        }
        binding.tagView.addTagList(tags)
        binding.tagView.setRadius(2.75f)
        binding.tagView.startAutoRotation()
        binding.tagView.setOnTagTapListener(object : OnTagTapListener {
            override fun onTap(tagItem: TagItem) {
                Toasty.info(requireContext(), "도감", Toast.LENGTH_SHORT, true).show();
                val fragment = BadgeShowFragment()
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                transaction.replace(R.id.mainFragmentContainer, fragment) // fragment_container는 프래그먼트를 호스팅하는 컨테이너 레이아웃의 ID입니다.
                transaction.addToBackStack(null) // 이전 프래그먼트로 돌아갈 수 있도록 스택에 추가합니다.
                transaction.commit()
            }
        })
    }
    private fun getVectorDrawable(id: Int): Drawable? =
        ContextCompat.getDrawable(requireContext(), id)

//    private fun setUpVectorCircular() {
//        val anyChartView: AnyChartView = binding.anyChartView
//        anyChartView.setProgressBar(binding.progressBar)
//
//        val circularGauge: CircularGauge = AnyChart.circular()
//        circularGauge.animation(true, 3000)
//        circularGauge.fill("#fff")
//            .stroke("null")
//            .padding(0, 0, 0, 0)
//            .margin(30, 30, 30, 30)
//            .startAngle(0)
//            .sweepAngle(360)
//
//        circularGauge.data(SingleValueDataSet(arrayOf<Double>(18.1)))
//
//        circularGauge.axis(0)
//            .startAngle(0)
//            .radius(80)
//            .sweepAngle(360)
//            .width(3)
//            .drawFirstLabel(false)
//            .ticks("{ type: 'line', length: 4, position: 'outside' }")
//
//        circularGauge.axis(0).labels()
//            .position("outside")
//            .useHtml(true)
//        circularGauge.axis(0).labels().format(
//            """function () {
//    return this.value + '&deg;'
//  }"""
//        )
//
//        circularGauge.axis(0).scale()
//            .minimum(0)
//            .maximum(360)
//        circularGauge.axis(0).scale()
//            .ticks("{interval: 45}")
//            .minorTicks("{interval: 10}")
//
//
//        circularGauge.marker(0)
//            .fill(SolidFill("#64b5f6", 1))
//            .stroke("null")
//        circularGauge.marker(0)
//            .size(7)
//            .radius(80)
//
//
//        circularGauge.label(0)
//            .text("<span style=\"font-size: 25\"></span>")
//            .useHtml(true)
//            .hAlign(HAlign.CENTER)
//        circularGauge.label(0)
//            .anchor(Anchor.CENTER_TOP)
//            .offsetY(50)
//            .padding(15, 20, 0, 0)
//
//        circularGauge.label(1)
//            .text("<span style=\"font-size: 20\">18.1</span>")
//            .useHtml(true)
//            .hAlign(HAlign.CENTER)
//        circularGauge.label(1)
//            .anchor(Anchor.CENTER_TOP)
//            .offsetY(-20)
//            .padding(5, 10, 0, 0)
//            .background("{fill: 'none', stroke: '#c1c1c1', corners: 3, cornerType: 'ROUND'}")
//
//        anyChartView.setChart(circularGauge)
//    }

}

