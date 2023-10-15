package com.cbnu.project.cpr.heartsignal.manager.soundmanager

import android.content.Context
import android.media.MediaPlayer
import com.cbnu.project.cpr.heartsignal.R
import java.lang.ref.WeakReference


object SoundManager {
    private var m_mediaPlayer: MediaPlayer? = null
    private var m_mediaCountDown: MediaPlayer? = null
    private var contextRef: WeakReference<Context>? = null
    // Context를 정적(static) 필드에 보관하는 것을 피해야 함. 이렇게 하면 메모리 누수(memory leak)가 발생 가능
    // Context 객체를 강한 참조 대신 약한 참조로 저장가능 이렇게 하면 Context가 필요하지 않을 때 가비지 컬렉터에 의해 수거됨
    fun initialize(context: Context) {
        this.contextRef = WeakReference(context)
        // "삐" 소리 재생을 위한 MediaPlayer 초기화
        m_mediaPlayer = MediaPlayer.create(context, R.raw.mp_beep)
        m_mediaCountDown = MediaPlayer.create(context, R.raw.countdown)
    }


    fun playBeepSound() {
        m_mediaPlayer?.seekTo(0) // 소리를 처음부터 재생
        m_mediaPlayer?.start() // 소리 재생
    }

    fun releaseBeepSound() {
        m_mediaPlayer?.release()
    }

    fun startCountDownSound() {
        m_mediaCountDown?.seekTo(0) // 소리를 처음부터 재생
        m_mediaCountDown?.start() // 소리 재생
        m_mediaCountDown?.setOnCompletionListener { mp ->
            // 미디어 재생이 완료될 때 실행할 동작을 여기에 추가합니다.
            // 예를 들어, 종료 메시지를 표시하거나 다른 작업을 수행할 수 있습니다.
            // 미디어 플레이어 해제
            releaseCountDownSound()
        }
    }

    fun releaseCountDownSound() {
        m_mediaCountDown?.release()
    }

}