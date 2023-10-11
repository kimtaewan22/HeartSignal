package com.cbnu.project.cpr.heartsignal.fragment.mainFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cbnu.project.cpr.heartsignal.databinding.FragmentMainProfileBinding


class MainProfileFragment : Fragment() {

    private var _binding: FragmentMainProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

}