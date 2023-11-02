package com.cbnu.project.cpr.heartsignal.custom

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.databinding.LayoutResultBottomSheetBinding
import com.cbnu.project.cpr.heartsignal.manager.chartmanager.LineChartManager
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Collections

class ResultBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private lateinit var binding:LayoutResultBottomSheetBinding
    // Bundle에서 데이터를 가져오기 위한 key
    companion object {
        const val DATA_KEY = "DATA_KEY"

        fun newInstance(dataSet: ArrayList<Entry>): ResultBottomSheetDialogFragment {
            val fragment = ResultBottomSheetDialogFragment()
            val args = Bundle()
            args.putParcelableArrayList(DATA_KEY, dataSet)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // BottomSheetDialog의 상태를 STATE_EXPANDED로 설정
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // DataBinding을 사용하여 레이아웃을 inflate 합니다.
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.layout_result_bottom_sheet, // 여기서 레이아웃 이름을 확인하세요. 예시로 작성한 이름이라 실제 레이아웃 이름과 다를 수 있습니다.
            container,
            false
        )


        // 닫기 버튼에 클릭 리스너 설정
        binding.resultCloseButton.setOnClickListener {  // 이 ID는 레이아웃 파일의 ImageView ID와 일치해야 합니다.
            dismiss()
            LineChartManager.resetTotalDataset()

        }

        val dataSet = arguments?.getParcelableArrayList<Entry>(DATA_KEY)
        dataSet?.let {
            setupLineChart()
            drawGraph(it)
        }


        return binding.root
    }

    private fun drawGraph(dataSet: ArrayList<Entry>) {
        Collections.sort(dataSet, EntryXComparator())
        val lineDataSet = LineDataSet(dataSet, "Label")
        lineDataSet.setDrawCircles(false)
        val purpleColor = ContextCompat.getColor(requireContext(), android.R.color.holo_purple)
        lineDataSet.color = purpleColor
        val lineData = LineData(lineDataSet)
        binding.resultLineChart.data = lineData
        binding.resultLineChart.invalidate() // refresh the chart
    }
    private fun setupLineChart() {
        binding.resultLineChart.apply {
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
}
