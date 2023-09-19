package com.cbnu.project.cpr.heartsignal.fragment.mainFragment

import android.R.attr.height
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cbnu.project.cpr.heartsignal.databinding.FragmentMainProfileBinding
import com.thoughtbot.stencil.StencilView
import java.nio.file.Path


class MainProfileFragment : Fragment() {

    private var _binding: FragmentMainProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var regular: StencilView
    private lateinit var customFont: StencilView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        regular = binding.stencilSample
        customFont = binding.stencilSampleWithCustomFont

        regular.animatePath()
        customFont.animatePath()


        return view
    }

}