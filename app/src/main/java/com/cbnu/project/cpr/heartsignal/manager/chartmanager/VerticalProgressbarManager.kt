package com.cbnu.project.cpr.heartsignal.manager.chartmanager

import android.widget.ProgressBar

object VerticalProgressbarManager {
    private lateinit var progressBar: ProgressBar


    fun initialize(progressBar: ProgressBar) {
        this.progressBar = progressBar
    }

    fun setProgress(progress: Int) {
        this.progressBar.progress = progress
    }
}