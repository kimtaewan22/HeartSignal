package com.cbnu.project.cpr.heartsignal.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cbnu.project.cpr.heartsignal.databinding.FragmentBadgeShowBinding
import com.xwray.groupie.GroupieAdapter

class BadgeShowFragment : Fragment() {
    private var _binding: FragmentBadgeShowBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBadgeShowBinding.inflate(inflater, container, false)
        val view = binding.root
        val recyclerView = binding.recyclerView

        val adapter = GroupieAdapter()
        recyclerView.adapter = adapter





        return view
    }


    companion object {
    }

}