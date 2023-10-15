package com.cbnu.project.cpr.heartsignal.manager.soundmanager

import android.animation.Animator
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.databinding.FragmentCameraBinding
import com.robinhood.ticker.TickerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AnimationManager {
    private var countdownJob: Job? = null
    private var remainingTimeInSeconds = 60 // 초기값을 60으로 설정하여 60부터 시작
    private var m_tickerView : TickerView? = null
    private var m_lottieAnimaionView: LottieAnimationView? = null
    private var currentTime = 0f
    private var isCountingDown = false

    fun initialize(tickerView: TickerView, lottieAnimationView: LottieAnimationView) {
        m_tickerView = tickerView
        m_lottieAnimaionView = lottieAnimationView
    }

    fun showLottieAnimation() {
        currentTime += 0.5f
        m_lottieAnimaionView?.repeatCount = 60
        m_lottieAnimaionView?.playAnimation()
        m_lottieAnimaionView?.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {
                // 현재 프레임을 로그로 출력합니다.
//                val currentTime = m_lottieAnimaionView?.progress

                // 원하는 프레임에서 이미지를 변경하려면 여기에서 조건문을 사용하여 작업을 수행하면 됩니다.
                if (currentTime in 15f..30f) {
                    m_lottieAnimaionView?.setAnimation(R.raw.heart_bad2)
                    m_lottieAnimaionView?.repeatCount = 30
                    m_lottieAnimaionView?.playAnimation()
                } else if (currentTime in 30f..45f) {
                    m_lottieAnimaionView?.setAnimation(R.raw.heart_bad3)
                    m_lottieAnimaionView?.repeatCount = 30
                    m_lottieAnimaionView?.playAnimation()
                } else if (currentTime in 45f..60f) {
                    m_lottieAnimaionView?.setAnimation(R.raw.heart_good1)
                    m_lottieAnimaionView?.repeatCount = 30
                    m_lottieAnimaionView?.playAnimation()
                }
                else {
                    m_lottieAnimaionView?.cancelAnimation()
                }
            }
        })
    }

    fun showLottieCountDown(lottie_count: LottieAnimationView, fragmentCameraBinding: FragmentCameraBinding) {
        // LottieAnimationView를 맨 앞으로 가져옵니다.
        lottie_count.visibility = View.VISIBLE
        lottie_count.bringToFront()
        lottie_count.playAnimation()
        SoundManager.startCountDownSound()
        lottie_count.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }
            override fun onAnimationEnd(animation: Animator) {
                fragmentCameraBinding.lottieCount.visibility = View.GONE
                tickerViewCountDown()
            }

            override fun onAnimationCancel(animation: Animator) {

            }
            override fun onAnimationRepeat(animation: Animator) {

            }
        })
    }




    fun tickerViewCountDown() {
        if (isCountingDown) {
            return  // 이미 실행 중인 경우 함수를 빠져나갑니다.
        }
        isCountingDown = true  // 이제 카운트 다운이 실행 중임을 나타냅니다.
        countdownJob = CoroutineScope(Dispatchers.Main).launch {
            while (NonCancellable.isActive && remainingTimeInSeconds >= 0) {
//            val hours = remainingTimeInSeconds / 3600
                val minutes = (remainingTimeInSeconds % 3600) / 60
                val secondsRemaining = remainingTimeInSeconds % 60

                val timeString = String.format("%02d:%02d", minutes, secondsRemaining)

                withContext(Dispatchers.Main) {
                    setTickerViewText(timeString)
                }

                delay(1000) // 1초 간격으로 업데이트

                remainingTimeInSeconds-- // 1초씩 감소
            }
            isCountingDown = false  // 카운트 다운이 완료되면 플래그를 다시 false로 설정합니다.
            currentTime = 0f
        }
    }

    fun stopTickerViewCountDown() {
        countdownJob?.cancel()
    }

    fun updateData(){
        remainingTimeInSeconds = 60
        m_tickerView?.text = "60:00"
    }

    fun setTickerViewText(timeString : String) {
        m_tickerView?.text = timeString
    }

}
