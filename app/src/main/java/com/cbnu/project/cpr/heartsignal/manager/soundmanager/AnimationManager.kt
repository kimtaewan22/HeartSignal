package com.cbnu.project.cpr.heartsignal.manager.soundmanager

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import com.airbnb.lottie.LottieAnimationView
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.ble.BluetoothManager
import com.cbnu.project.cpr.heartsignal.databinding.FragmentCameraBinding
import com.cbnu.project.cpr.heartsignal.manager.chartmanager.LineChartManager
import com.robinhood.ticker.TickerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

object AnimationManager {
    private var countdownJob: Job? = null
    private var remainingTimeInSeconds = 60 // 초기값을 60으로 설정하여 60부터 시작
    private var m_tickerView : TickerView? = null
    private var m_lottieAnimaionView: LottieAnimationView? = null
    private var currentTime = 60f
    private var isCountingDown = false
    private var startGetData = false
    @SuppressLint("StaticFieldLeak")
    private lateinit var bluetoothManager: BluetoothManager
    private var secondsRemaining = 60f

    //BLE 장치에 Write할 내용 설정
    private val characteristicUUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    private val data = "ST".toByteArray(Charsets.UTF_8)

    fun initialize(tickerView: TickerView, lottieAnimationView: LottieAnimationView, context: Context) {
        m_tickerView = tickerView
        m_lottieAnimaionView = lottieAnimationView
        bluetoothManager = BluetoothManager.getInstance(context)
    }

    fun showLottieAnimation(animationResource: Int, repeatCount: Int) {
        // 이미 재생중인 애니메이션을 취소
        m_lottieAnimaionView?.cancelAnimation()

        // 새 애니메이션 설정 및 재생
        m_lottieAnimaionView?.setAnimation(animationResource)
        m_lottieAnimaionView?.repeatCount = repeatCount
        m_lottieAnimaionView?.playAnimation()
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
                writeDataToBLEDevice()
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
                setSecondRemainingTime(secondsRemaining.toFloat())
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

    fun writeDataToBLEDevice() {
        bluetoothManager.writeDataToCharacteristic(data, characteristicUUID)
    }

    fun getSecondRemainingTime (): Float {
        return secondsRemaining
    }

    fun setSecondRemainingTime (time: Float) {
        this.secondsRemaining = time
    }


}
