package com.cbnu.project.cpr.heartsignal.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.adapter.GridBadgeAdapter
import com.cbnu.project.cpr.heartsignal.databinding.FragmentBadgeShowBinding
import com.cbnu.project.cpr.heartsignal.viewModel.BadgeModel

class BadgeShowFragment : Fragment() {
    private var _binding: FragmentBadgeShowBinding? = null
    private val binding get() = _binding!!

    private lateinit var gridView: GridView
    private lateinit var physicsLayoutFragment: PhysicsLayoutFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBadgeShowBinding.inflate(inflater, container, false)
        val view = binding.root


        gridView = binding.gridBadge
        val badgeModelArrayList: ArrayList<BadgeModel> = ArrayList<BadgeModel>()

        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_01_on, "용"))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_02_on, "토끼"))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_03_on,"소"))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_04_on,"호랑이"))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_05_on,"쥐"))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_06_on,"강아지"))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_07_on,"원숭이"))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_08_on,"돼지"))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_09_on,"양"))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_10_on,"말"))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_11_on,"뱀"))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_12_on,"닭"))

        val adapter = GridBadgeAdapter(requireContext(), badgeModelArrayList)
        gridView.adapter = adapter

        gridView.setOnItemClickListener { parent, view, position, id ->
            // 여기에 클릭 이벤트를 처리하는 코드를 작성합니다.
            // position 매개변수는 클릭된 항목의 위치를 나타냅니다.

            // 예를 들어, 클릭된 항목의 데이터를 가져오려면 다음과 같이 할 수 있습니다:
            val clickedBadgeModel = badgeModelArrayList[position]
            Log.d("BadgeShowCameraFragment",clickedBadgeModel.toString())
            // 이제 클릭된 데이터를 사용하여 원하는 작업을 수행할 수 있습니다.
        }
        val fragmentManager = childFragmentManager
        val transaction = fragmentManager.beginTransaction()
        physicsLayoutFragment = PhysicsLayoutFragment()
        transaction.replace(R.id.physicsLayout, physicsLayoutFragment) // 프레임 레이아웃에 Physics 프래그먼트 추가
        transaction.commit()

        return view
    }


    companion object {
    }

}