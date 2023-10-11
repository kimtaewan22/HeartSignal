package com.cbnu.project.cpr.heartsignal.step

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.custom.RecordBottomSheetDialog
import com.cbnu.project.cpr.heartsignal.databinding.ActivityStep1Binding

class Step1Activity : AppCompatActivity() {
    private lateinit var binding: ActivityStep1Binding
    private val REQUEST_SPEECH_RECOGNITION = 1001
    private lateinit var speechRecognizer: SpeechRecognizer
    private var mediaPlayerEx: MediaPlayer? = null
    private var mediaPlayerSuc: MediaPlayer? = null
    private var mediaPlayerEnd: MediaPlayer? = null

    private lateinit var lottieAnimationViewExplain: LottieAnimationView
    private lateinit var bottomSheetDialog: RecordBottomSheetDialog
    private lateinit var bottomSheetTitle: TextView
    private lateinit var bottomSheetSubTitle: TextView
    private lateinit var bottomSheetProgressBar: ProgressBar
    private lateinit var bottomSheetLottieAnimationView: LottieAnimationView

    private var currentStep = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStep1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaPlayerEx = MediaPlayer.create(this, R.raw.step1)
        lottieAnimationViewExplain = binding.lottieExplain
        // RecognizerIntent 생성
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)    // 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")         // 언어 설정
        playTimerSound()

    }

    private fun showLottieAnimation() {
        lottieAnimationViewExplain.repeatCount = 5
        lottieAnimationViewExplain.backgroundTintMode
        lottieAnimationViewExplain.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }
            override fun onAnimationEnd(animation: Animator) {

            }

            override fun onAnimationCancel(animation: Animator) {

            }
            override fun onAnimationRepeat(animation: Animator) {

            }
        })
    }



    private fun playTimerSound() {
        showLottieAnimation()
        mediaPlayerEx?.seekTo(0) // 소리를 처음부터 재생
        mediaPlayerEx?.start() // 소리 재생
        mediaPlayerEx?.setOnCompletionListener { mp ->
            // 미디어 재생이 완료될 때 실행할 동작을 여기에 추가합니다.
            // 예를 들어, 종료 메시지를 표시하거나 다른 작업을 수행할 수 있습니다.

// 미디어 플레이어 해제
            mp.release()
            showRecordBottomSheetDialog() // Bottom Sheet 다이얼로그 표시
        }
    }

    // 리스너 설정


    private fun showRecordBottomSheetDialog() {
        val bottomSheetDialog = RecordBottomSheetDialog(this)
        bottomSheetTitle = bottomSheetDialog.binding.title
        bottomSheetProgressBar = bottomSheetDialog.binding.progressBarStep
        bottomSheetSubTitle = bottomSheetDialog.binding.subTitle
        bottomSheetLottieAnimationView = bottomSheetDialog.binding.voiceButton
        bottomSheetDialog.let {
            if(it.isShown.not()) {
                it.show()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
                val recognitionListener = createRecognitionListener()
                speechRecognizer.setRecognitionListener(recognitionListener)
                speechRecognizer.startListening(
                    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(
                        RecognizerIntent.EXTRA_CALLING_PACKAGE,
                        packageName
                    ).putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
                )
            }
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                    SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
                    SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트웍 타임아웃"
                    SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER 가 바쁨"
                    SpeechRecognizer.ERROR_SERVER -> "서버가 이상함"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간초과"
                    else -> "알 수 없는 오류임"
                }
                bottomSheetTitle.text = "에러 발생: $message"
                startNextSpeechRecognition()
            }
            override fun onResults(results: Bundle?) {
                val resultData = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (resultData != null && resultData.isNotEmpty()) {
                    val recognizedText = resultData[0]
                    Log.d("TEXTSTRING", recognizedText)
                    // 각 키워드에 따라 프로그레스 업데이트
                    updateProgressAndText(recognizedText)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun startNextSpeechRecognition() {
        // 다음 음성 인식을 시작합니다.
        speechRecognizer.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(
                RecognizerIntent.EXTRA_CALLING_PACKAGE,
                packageName
            ).putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        )
    }

    private fun updateProgressAndText(recognizedText: String) {
        when (currentStep) {
            0 -> {
                if (recognizedText.contains("환자분 괜찮으세요")) {
                    currentStep++
                    bottomSheetTitle.text = "이렇게 말해보세요"
                    bottomSheetSubTitle.text = "몸 상태는 어떠세요?"
                    playMp3FileSuc()
                }
                else{
                    bottomSheetTitle.text = "다시 말해보세요"
                    startNextSpeechRecognition()

                }
            }
            1 -> {
                if (recognizedText.contains("몸 상태는 어떠세요")) {
                    currentStep++
                    bottomSheetTitle.text = "이렇게 말해보세요"
                    bottomSheetSubTitle.text = "조금만 참으세요"
                    playMp3FileSuc()
                }
                else{
                    bottomSheetTitle.text = "다시 말해보세요"
                    startNextSpeechRecognition()

                }
            }
            2 -> {
                if (recognizedText.contains("조금만 참으세요")) {
                    currentStep++
                    // 모든 작업이 완료되면 다음 동작을 수행하거나 종료
                    val currentProgress = (currentStep * 33)
                    bottomSheetProgressBar.progress = currentProgress
                    bottomSheetLottieAnimationView.setAnimation(R.raw.anim_success)
                    bottomSheetLottieAnimationView.repeatCount = 1
                    bottomSheetLottieAnimationView.playAnimation()
                    bottomSheetTitle.text = "곧 다음 단계로 이동합니다!"
                    // 2초 후에 함수 호출
                    Handler(Looper.getMainLooper()).postDelayed({
                        playMp3FileEnd()
                    }, 500)
                }
                else{
                    bottomSheetTitle.text = "다시 말해보세요"
                    startNextSpeechRecognition()

                }
            }
            else -> {

            }
        }

        if (currentStep <= 2) {
            val currentProgress = (currentStep * 33)
            bottomSheetProgressBar.progress = currentProgress
        }
    }

    private fun performFinalActionOrFinish() {
//        if (currentStep == 3) {
//            // 모든 작업이 완료될 때 추가 동작을 수행하거나 액티비티를 종료
//            speechRecognizer.stopListening()
//            speechRecognizer.destroy()
//            if (bottomSheetDialog?.isShown == true){
//                bottomSheetDialog.dismiss()
//            }
//        }
        val intent = Intent(this@Step1Activity, StepProgressActivity::class.java)
        intent.putExtra("stepFlag", "심폐소생술")
        startActivity(intent)
        finish()
    }
    private fun playMp3FileSuc(){
        mediaPlayerSuc = MediaPlayer.create(this, R.raw.success)
        mediaPlayerSuc?.setOnCompletionListener {
            // 음악 재생이 완료된 후 호출할 동작을 여기에 추가
            startNextSpeechRecognition()
        }
        mediaPlayerSuc?.seekTo(0) // 소리를 처음부터 재생
        mediaPlayerSuc?.start()

        mediaPlayerSuc?.setOnCompletionListener { mp ->
            // 미디어 재생이 완료될 때 실행할 동작을 여기에 추가합니다.
            // 예를 들어, 종료 메시지를 표시하거나 다른 작업을 수행할 수 있습니다.
            // 미디어 플레이어 해제
            mp.release()
            startNextSpeechRecognition()
        }
    }

    private fun playMp3FileEnd(){
        mediaPlayerEnd = MediaPlayer.create(this, R.raw.step1end)
        mediaPlayerEnd?.setOnCompletionListener {
            // 음악 재생이 완료된 후 호출할 동작을 여기에 추가
            startNextSpeechRecognition()
        }
        mediaPlayerEnd?.seekTo(0) // 소리를 처음부터 재생
        mediaPlayerEnd?.start()

        mediaPlayerEnd?.setOnCompletionListener { mp ->
            // 미디어 재생이 완료될 때 실행할 동작을 여기에 추가합니다.
            // 예를 들어, 종료 메시지를 표시하거나 다른 작업을 수행할 수 있습니다.
            // 미디어 플레이어 해제
            mp.release()
            performFinalActionOrFinish()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SPEECH_RECOGNITION && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (results != null && results.isNotEmpty()) {
                val recognizedText = results[0]
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy() // SpeechRecognizer를 해제합니다.
    }

}