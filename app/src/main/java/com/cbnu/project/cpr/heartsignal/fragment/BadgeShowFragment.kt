package com.cbnu.project.cpr.heartsignal.fragment

import android.os.Bundle
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBadgeShowBinding.inflate(inflater, container, false)
        val view = binding.root


        gridView = binding.gridBadge
        val badgeModelArrayList: ArrayList<BadgeModel> = ArrayList<BadgeModel>()

        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_01_on))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_02_on))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_03_on))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_04_on))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_05_on))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_06_on))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_07_on))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_08_on))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_09_on))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_10_on))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_11_on))
        badgeModelArrayList.add(BadgeModel(R.drawable.ic_badge_12_on))

        val adapter = GridBadgeAdapter(requireContext(), badgeModelArrayList)
        gridView.adapter = adapter

        return view
    }


    companion object {
    }

}