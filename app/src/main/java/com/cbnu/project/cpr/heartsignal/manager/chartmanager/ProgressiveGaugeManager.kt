package com.cbnu.project.cpr.heartsignal.manager.chartmanager

import android.annotation.SuppressLint
import com.cbnu.project.cpr.heartsignal.R
import com.github.anastr.speedviewlib.ProgressiveGauge

object ProgressiveGaugeManager {
    private lateinit var progressiveGauge: ProgressiveGauge

    @SuppressLint("ResourceAsColor")
    fun initialize(progressiveGauge: ProgressiveGauge ) {
        this.progressiveGauge = progressiveGauge
//        this.pointerSpeedometer.setBackgroundColor(R.color.background_color)
//        this.progressiveGauge.setBackgroundColor(R.color.polluted_waves4) //TODO 색상 선택
        this.progressiveGauge.speedometerColor = R.color.background_color
        this.progressiveGauge.speedTextSize = 34f
        this.progressiveGauge.textSize = 34f
        this.progressiveGauge.unit = "(예상 압박 횟수)"
        this.progressiveGauge.maxSpeed = 150f


    }
//    fun setupSpeedmeter(speed: Float) {
//        this.progressiveGauge.setSpeedAt(speed)
////        this.pointerSpeedometer.speedUp()
//    }

    fun setupSpeedText(speed: Float) {
//        this.progressiveGauge.realSpeedPercentTo(speed)
        // change speed to 50 Km/h
        this.progressiveGauge.speedTo(speed, 3000)
//        this.pointerSpeedometer.speedUp()
    }

}