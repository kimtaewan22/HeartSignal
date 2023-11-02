package com.cbnu.project.cpr.heartsignal.manager.chartmanager

import android.annotation.SuppressLint
import com.cbnu.project.cpr.heartsignal.R
import com.github.anastr.speedviewlib.PointerSpeedometer

object PointerSpeedmeterManager {
    private lateinit var pointerSpeedometer: PointerSpeedometer

    @SuppressLint("ResourceAsColor")
    fun initialize(pointerSpeedometer: PointerSpeedometer) {
        this.pointerSpeedometer = pointerSpeedometer
//        this.pointerSpeedometer.setBackgroundColor(R.color.background_color)
        this.pointerSpeedometer.setSpeedometerColor(R.color.polluted_waves4) //TODO 색상 선택
        this.pointerSpeedometer.setSpeedAt(60f)
    }

    fun setupSpeedmeter(speed: Float) {
        this.pointerSpeedometer.setSpeedAt(speed)
//        this.pointerSpeedometer.speedUp()
    }

}