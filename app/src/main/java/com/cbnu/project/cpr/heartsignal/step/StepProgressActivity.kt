package com.cbnu.project.cpr.heartsignal.step

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.databinding.ActivityStepProgressBinding
import com.kofigyan.stateprogressbar.StateProgressBar

class StepProgressActivity : AppCompatActivity() {
    private lateinit var binding : ActivityStepProgressBinding
    private lateinit var stateProgressBar: StateProgressBar
    private val DEFAULTVALUE = "FALSE"


    var descriptionData = arrayOf("훈련 설명", "음성 인식", "심폐 소생술", "결과")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        stateProgressBar = binding.stateProgressBar


        val stepFlag = intent.getStringExtra("stepFlag")
        when (stepFlag) {
            "훈련 설명" -> {
                stateProgressBar.setStateDescriptionData(descriptionData)
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE)
                stateProgressBar.foregroundColor = ContextCompat.getColor(this, R.color.colorStateOne)
                stateProgressBar.stateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateOne)
                stateProgressBar.currentStateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateOne)

                navigateNext(stepFlag)
            }
            "음성 인식" -> {
                stateProgressBar.setStateDescriptionData(descriptionData)
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO)
                stateProgressBar.foregroundColor = ContextCompat.getColor(this, R.color.colorStateTwo)
                stateProgressBar.stateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateTwo)
                stateProgressBar.currentStateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateTwo)
                navigateNext(stepFlag)
            }
            "심폐소생술" -> {
                stateProgressBar.setStateDescriptionData(descriptionData)
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE)
                stateProgressBar.foregroundColor = ContextCompat.getColor(this, R.color.colorStateThree)
                stateProgressBar.stateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateThree)
                stateProgressBar.currentStateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateThree)
                navigateNext(stepFlag)
            }
            "결과" -> {
                stateProgressBar.setStateDescriptionData(descriptionData)
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR)
                stateProgressBar.foregroundColor = ContextCompat.getColor(this, R.color.colorStateFour)
                stateProgressBar.stateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateFour)
                stateProgressBar.currentStateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateFour)
                navigateNext(stepFlag)
            }
            else -> {

            }
        }
    }

    private fun navigateNext(stepFlag: String) {
        when(stepFlag)
        {
            "훈련 설명" -> {
                intent = Intent(this@StepProgressActivity, Step0Activity::class.java)
            }
            "음성 인식" -> {
                intent = Intent(this@StepProgressActivity, Step1Activity::class.java)
            }
            "심폐소생술" -> {
                intent = Intent(this@StepProgressActivity, Step2Activity::class.java)
            }
            "결과" -> {
                intent = Intent(this@StepProgressActivity, Step2Activity::class.java)
            }
        }


        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(intent)
            finish()
        }, 3000)
    }

}