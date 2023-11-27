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
                setUpGridHeart1()
                stateProgressBar.setStateDescriptionData(descriptionData)
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO)
                stateProgressBar.foregroundColor = ContextCompat.getColor(this, R.color.colorStateTwo)
                stateProgressBar.stateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateTwo)
                stateProgressBar.currentStateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateTwo)
                navigateNext(stepFlag)

            }
            "심폐소생술" -> {
                setUpGridHeart()
                stateProgressBar.setStateDescriptionData(descriptionData)
                stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE)
                stateProgressBar.foregroundColor = ContextCompat.getColor(this, R.color.colorStateThree)
                stateProgressBar.stateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateThree)
                stateProgressBar.currentStateDescriptionColor = ContextCompat.getColor(this, R.color.colorStateThree)
                navigateNext(stepFlag)
            }
            "결과" -> {
                setUpGridHeart2()
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
    private fun setUpGridHeart() {
        gridLayout = binding.gridBadge
        val badgeModelArrayList: MutableList<BadgeHeartModel> = mutableListOf<BadgeHeartModel>()

//        for i in [0, 1]
//            badgeModelArrayList[i] = BadgeHeartModel(R.drawable.heart_image, "")

        //badgeModelArrayList[0] = BadgeHeartModel(R.drawable.w_heart_image, "")

        //step1
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart_image, ""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart_image, ""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart_image,""))
        //step2
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart2_image,""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart2_image,""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart2_image,""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart2_image,""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart2_image,""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart2_image,""))

        val adapter = GridHeartAdapter(this, badgeModelArrayList)
        gridLayout.adapter = adapter
    }
    private fun setUpGridHeart1() {
        gridLayout = binding.gridBadge
        val badgeModelArrayList: MutableList<BadgeHeartModel> = mutableListOf<BadgeHeartModel>()

//        for i in [0, 1]
//            badgeModelArrayList[i] = BadgeHeartModel(R.drawable.heart_image, "")

        //badgeModelArrayList[0] = BadgeHeartModel(R.drawable.w_heart_image, "")

        //step1
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart2_image, ""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart2_image, ""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart2_image,""))
        //step2
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart2_image,""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart2_image,""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart2_image,""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart2_image,""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart2_image,""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart2_image,""))

        val adapter = GridHeartAdapter(this, badgeModelArrayList)
        gridLayout.adapter = adapter
    }
    private fun setUpGridHeart2() {
        gridLayout = binding.gridBadge
        val badgeModelArrayList: MutableList<BadgeHeartModel> = mutableListOf<BadgeHeartModel>()

//        for i in [0, 1]
//            badgeModelArrayList[i] = BadgeHeartModel(R.drawable.heart_image, "")

        //badgeModelArrayList[0] = BadgeHeartModel(R.drawable.w_heart_image, "")

        //step1
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart_image, ""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart_image, ""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart_image,""))
        //step2
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart_image,""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart_image,""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart_image,""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart_image,""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart_image,""))
        badgeModelArrayList.add(BadgeHeartModel(R.drawable.heart_image,""))

        val adapter = GridHeartAdapter(this, badgeModelArrayList)
        gridLayout.adapter = adapter
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