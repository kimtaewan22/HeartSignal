package com.cbnu.project.cpr.heartsignal.step

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.adapter.GridHeartAdapter
import com.cbnu.project.cpr.heartsignal.databinding.ActivityStepProgressBinding
import com.cbnu.project.cpr.heartsignal.viewModel.BadgeHeartModel
import com.kofigyan.stateprogressbar.StateProgressBar

class StepProgressActivity : AppCompatActivity() {
    private lateinit var binding : ActivityStepProgressBinding
    private lateinit var stateProgressBar: StateProgressBar
    private lateinit var gridLayout: GridView
    private val DEFAULTVALUE = "FALSE"


    var descriptionData = arrayOf("훈련 설명", "음성 인식", "심폐 소생술", "결과")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        stateProgressBar = binding.stateProgressBar
        gridLayout = binding.gridBadge


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
                setUpGridHeart(0)
                stateProgressBar.setStateDescriptionData(descriptionData)
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO)
                stateProgressBar.foregroundColor = ContextCompat.getColor(this, R.color.colorStateTwo)
                stateProgressBar.stateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateTwo)
                stateProgressBar.currentStateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateTwo)
                navigateNext(stepFlag)
            }
            "심폐소생술" -> {
                setUpGridHeart(1)
                stateProgressBar.setStateDescriptionData(descriptionData)
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE)
                stateProgressBar.foregroundColor = ContextCompat.getColor(this, R.color.colorStateThree)
                stateProgressBar.stateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateThree)
                stateProgressBar.currentStateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateThree)
                navigateNext(stepFlag)
            }
            "결과" -> {
                setUpGridHeart(2)
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
    private fun setUpGridHeart(step: Int) {
        gridLayout = binding.gridBadge
        val badgeModelArrayList: MutableList<BadgeHeartModel> = mutableListOf()

        // 각 단계에 따른 이미지 리소스 결정
        val imageResourceOn = if (step >= 1) R.drawable.heart_on_image else R.drawable.heart_off_image
        val imageResourceOff = R.drawable.heart_off_image

        // 첫 번째 단계의 아이템 추가
        for (i in 0 until 3) {
            badgeModelArrayList.add(BadgeHeartModel(imageResourceOn, ""))
        }

        // 두 번째 단계의 아이템 추가
        for (i in 0 until 6) {
            val imageResource = if (step == 2) imageResourceOn else imageResourceOff
            badgeModelArrayList.add(BadgeHeartModel(imageResource, ""))
        }

        val adapter = GridHeartAdapter(this, badgeModelArrayList)
        gridLayout.adapter = adapter
    }


}