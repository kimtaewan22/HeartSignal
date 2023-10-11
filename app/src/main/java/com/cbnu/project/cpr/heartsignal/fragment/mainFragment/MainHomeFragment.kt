package com.cbnu.project.cpr.heartsignal.fragment.mainFragment


import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private fun setUpFadingText() {

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
            R.drawable.ic_badge_01_on,
            R.drawable.ic_badge_02_on,
            R.drawable.ic_badge_03_on,
            R.drawable.ic_badge_04_on,
            R.drawable.ic_badge_05_on,
            R.drawable.ic_badge_06_on,
            R.drawable.ic_badge_07_on,
            R.drawable.ic_badge_08_on,
            R.drawable.ic_badge_09_on,
            R.drawable.ic_badge_10_on,
            R.drawable.ic_badge_11_on,
            R.drawable.ic_badge_12_on
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
                transaction.setCustomAnimations(R.anim.slide_enter_from_left, R.anim.slide_exit_to_left)
                transaction.commit()
//                requireActivity().supportFragmentManager.find
//                findNavController().navigate(R.id.action_mainHomeFragment_to_badgeShowFragment)
            }
        })
    }
    private fun getVectorDrawable(id: Int): Drawable? =
        ContextCompat.getDrawable(requireContext(), id)
}

