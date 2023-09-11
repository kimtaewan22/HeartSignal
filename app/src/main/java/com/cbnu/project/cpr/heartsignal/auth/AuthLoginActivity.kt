package com.cbnu.project.cpr.heartsignal.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cbnu.project.cpr.heartsignal.databinding.ActivityAuthLoginBinding

class AuthLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}