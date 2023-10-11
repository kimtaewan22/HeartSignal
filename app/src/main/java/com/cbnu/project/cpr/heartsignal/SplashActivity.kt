package com.cbnu.project.cpr.heartsignal

import android.animation.Animator
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.airbnb.lottie.LottieAnimationView
import com.cbnu.project.cpr.heartsignal.auth.AuthLoginActivity
import com.cbnu.project.cpr.heartsignal.databinding.ActivitySplashBinding
import com.thoughtbot.stencil.StencilView

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var lottieAnimationViewSignal: LottieAnimationView

    private lateinit var welcome: StencilView
    private lateinit var to: StencilView
    private lateinit var heartsignal: StencilView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContentView(binding.root)
        lottieAnimationViewSignal = binding.lottieHeartsignal

        welcome = binding.stencilWelcome
        to = binding.stencilTo
        heartsignal = binding.stencilHeartSignal

        showLottieAnimation()
    }

    private fun showLottieAnimation() {
        lottieAnimationViewSignal.backgroundTintMode
        lottieAnimationViewSignal.playAnimation()
        lottieAnimationViewSignal.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }
            override fun onAnimationEnd(animation: Animator) {
                welcome.animatePath()
                to.animatePath()
                heartsignal.animatePath()
                val handler = Handler()
                handler.postDelayed({
                    startActivity(
                        Intent(
                            this@SplashActivity,
                            AuthLoginActivity::class.java
                        )
                    )
                }, 3000)
            }

            override fun onAnimationCancel(animation: Animator) {

            }
            override fun onAnimationRepeat(animation: Animator) {

            }
        })
    }
}