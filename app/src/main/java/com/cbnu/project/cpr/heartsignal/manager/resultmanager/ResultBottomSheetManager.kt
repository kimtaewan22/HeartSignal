package com.cbnu.project.cpr.heartsignal.manager.resultmanager

import androidx.fragment.app.FragmentManager
import com.cbnu.project.cpr.heartsignal.custom.ResultBottomSheetDialogFragment
import com.cbnu.project.cpr.heartsignal.manager.chartmanager.LineChartManager
import com.github.mikephil.charting.data.Entry

class ResultBottomSheetManager {

    fun showBottomSheet(fragmentManager: FragmentManager) {

        val totalDataSet: ArrayList<Entry> = LineChartManager.getTotalDataSet() // 이 부분은 필요한 위치에 따라서 가져옵니다.
        val bottomSheetFragment = ResultBottomSheetDialogFragment.newInstance(totalDataSet)
        bottomSheetFragment.compressionCount = LineChartManager.getCompressionCount()
        bottomSheetFragment.compressionSuccessCount = LineChartManager.getCompressionSuccessCount()


        bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)

    }

    fun getStartNextActivity(): Boolean{
        return true
    }

    // 필요한 경우 이곳에 추가적인 메서드나 로직을 넣을 수 있습니다.
}
