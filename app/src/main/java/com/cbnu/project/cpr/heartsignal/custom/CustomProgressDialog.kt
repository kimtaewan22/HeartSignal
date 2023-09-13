package com.cbnu.project.cpr.heartsignal.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.cbnu.project.cpr.heartsignal.R

class CustomProgressDialog : DialogFragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var textViewCountdown: TextView
    private lateinit var textViewSTT: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.dialog_countdown, container, false)
        progressBar = rootView.findViewById(R.id.progressBarCountdown)
        textViewCountdown = rootView.findViewById(R.id.textViewCountdown)
        textViewSTT = rootView.findViewById(R.id.textViewSTT)

        return rootView
    }

    fun updateProgress(progress: Int) {
        progressBar.progress = progress
        textViewCountdown.text = "Countdown: ${(100 - progress) / 10}s"
    }

    fun setUpTextView(result: String) {
        textViewSTT.text = result
    }

    fun dismissProgressDialog() {
        dismiss()
    }
}