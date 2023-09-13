package com.cbnu.project.cpr.heartsignal.step

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.databinding.ActivityStep0Binding

class Step0Activity : AppCompatActivity() {
    private lateinit var binding: ActivityStep0Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStep0Binding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.nextBtn0.setOnClickListener {
            val intent = Intent(this@Step0Activity, StepProgressActivity::class.java)
            intent.putExtra("stepFlag", "음성 인식")
            startActivity(intent)
        }
    }
}