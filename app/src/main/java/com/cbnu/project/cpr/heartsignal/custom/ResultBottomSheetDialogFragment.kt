package com.cbnu.project.cpr.heartsignal.custom

import android.animation.ObjectAnimator
import android.content.Intent
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
import com.cbnu.project.cpr.heartsignal.manager.soundmanager.AnimationManager
import com.cbnu.project.cpr.heartsignal.step.StepProgressActivity
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
    var compressionCount = ""
    var compressionSuccessCount = ""
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

//        (dialog as? BottomSheetDialog)?.behavior?.peekHeight = (resources.displayMetrics.heightPixels * 0.75).toInt()
//        // BottomSheetDialog의 상태를 STATE_EXPANDED로 설정
//        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED

        // dialog를 BottomSheetDialog로 캐스팅
        val bottomSheetDialog = dialog as? BottomSheetDialog

        // dialog의 bottomSheet을 가져옴
        val bottomSheet = bottomSheetDialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = bottomSheet?.let { BottomSheetBehavior.from(it) }
        // 높이를 원하는 만큼 설정. 예를 들어, 스크린 높이의 3/4로 설정
        behavior?.peekHeight = (resources.displayMetrics.heightPixels)
        // 상태를 STATE_EXPANDED로 설정
        behavior?.state = BottomSheetBehavior.STATE_EXPANDED


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
            AnimationManager.updateData()
            goToAnotherActivity()
        }
        val dataSet = arguments?.getParcelableArrayList<Entry>(DATA_KEY)
        dataSet?.let {
            setupLineChart()
            drawGraph(it)
        }
        setupCompressionCountText()
        setupCompressionSuccessCountText()
        setUpSuccessProgressBar()
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

    private fun setupCompressionCountText() {
        binding.compressionCount.text = "압박 시도 횟수: $compressionCount"
        binding.compressionCount.textSize = 22f
    }

    private fun setupCompressionSuccessCountText() {
        binding.compressionSuccessCount.text = "압박 성공 횟수: $compressionSuccessCount"
        binding.compressionSuccessCount.textSize = 22f
    }

    private fun setUpSuccessProgressBar() {
        ObjectAnimator.ofInt(binding.resultProgressBarStep, "progress", ((compressionCount.toFloat() / compressionSuccessCount.toFloat()).toInt() * 100))
            .setDuration(500) // 500ms 동안 지속
            .start()

    }

    private fun goToAnotherActivity() {
        // 현재 프래그먼트를 종료
        fragmentManager?.beginTransaction()?.remove(this)?.commit()

        // 새 액티비티 시작을 위한 인텐트 생성
        val intent = Intent(activity, StepProgressActivity::class.java)

        // 옵션으로, 인텐트에 데이터 추가 가능
        intent.putExtra("stepFlag", "결과")

        // 새 액티비티 시작
        startActivity(intent)

        // (선택사항) 현재 호스팅 액티비티 종료
        activity?.finish()
    }
}
