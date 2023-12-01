package com.cbnu.project.cpr.heartsignal.manager.framemanager

import android.animation.ValueAnimator
import android.content.Context
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import com.cbnu.project.cpr.heartsignal.R
import kotlin.properties.Delegates

object FrameBackgroundManager {
    private lateinit var frameLayoutTop: FrameLayout
    private lateinit var frameLayoutBottom: FrameLayout
    private lateinit var frameLayoutLeft: FrameLayout
    private lateinit var frameLayoutRight: FrameLayout
    private lateinit var context: Context
    private var colorCorrect by Delegates.notNull<Int>()
    private var colorDisCorrect by Delegates.notNull<Int>()
    private var colorclear by Delegates.notNull<Int>()

    private var isAnimatingTop = false
    private var isAnimatingBottom = false
    private var isAnimatingLeft = false
    private var isAnimatingRight = false

    fun initialize(top: FrameLayout, bottom: FrameLayout, left: FrameLayout, right: FrameLayout, context: Context) {
        frameLayoutTop = top
        frameLayoutBottom = bottom
        frameLayoutLeft = left
        frameLayoutRight = right
        this.context = context
        setUpColor()
    }

    fun setUpColor() {
        colorCorrect = ContextCompat.getColor(this.context, R.color.colorCompressionCorrect)
        colorDisCorrect = ContextCompat.getColor(this.context, R.color.colorCompressionDisCorrect)
        colorclear = ContextCompat.getColor(this.context, R.color.card_background_border_color)

    }


    fun colorUpdateTop(){
        animateBackgroundColorChange(frameLayoutTop, colorclear, colorDisCorrect, isAnimatingTop)
    }

    fun colorUpdateBottom(){
        animateBackgroundColorChange(frameLayoutBottom, colorclear, colorDisCorrect, isAnimatingBottom)
    }

    fun colorUpdateLeft(){
        animateBackgroundColorChange(frameLayoutLeft, colorclear, colorDisCorrect, isAnimatingLeft)
    }

    fun colorUpdateRight(){
        animateBackgroundColorChange(frameLayoutRight, colorclear, colorDisCorrect, isAnimatingRight)
    }

    fun colorDiscorrect() {
        animateBackgroundColorChange(frameLayoutTop, colorclear, colorDisCorrect, isAnimatingTop)
        animateBackgroundColorChange(frameLayoutBottom, colorclear, colorDisCorrect, isAnimatingBottom)
        animateBackgroundColorChange(frameLayoutLeft, colorclear, colorDisCorrect, isAnimatingLeft)
        animateBackgroundColorChange(frameLayoutRight, colorclear, colorDisCorrect, isAnimatingRight)
    }

    fun colorcorrect() {
        animateBackgroundColorChange(frameLayoutTop, colorclear, colorCorrect, isAnimatingTop)
        animateBackgroundColorChange(frameLayoutBottom, colorclear, colorCorrect, isAnimatingBottom)
        animateBackgroundColorChange(frameLayoutLeft, colorclear, colorCorrect, isAnimatingLeft)
        animateBackgroundColorChange(frameLayoutRight, colorclear, colorCorrect, isAnimatingRight)
    }

    fun colorClear(){
        frameLayoutTop.setBackgroundColor(colorclear)
        frameLayoutBottom.setBackgroundColor(colorclear)
        frameLayoutLeft.setBackgroundColor(colorclear)
        frameLayoutRight.setBackgroundColor(colorclear)
    }
    private fun animateBackgroundColorChange(frameLayout: FrameLayout, startColor: Int, endColor: Int, isAnimating: Boolean) {
        if (isAnimating) {
            // 이미 애니메이션 중이라면 색상 변경을 취소하고 초기 상태로 복원
            frameLayout.setBackgroundColor(colorclear)
            return
        }

        // 애니메이션 상태 설정
        when (frameLayout) {
            frameLayoutTop -> isAnimatingTop = true
            frameLayoutBottom -> isAnimatingBottom = true
            frameLayoutLeft -> isAnimatingLeft = true
            frameLayoutRight -> isAnimatingRight = true
        }

        val colorAnimation = ValueAnimator.ofArgb(startColor, endColor)
        colorAnimation.duration = 1000 // 3초 동안 실행
        colorAnimation.interpolator = AccelerateDecelerateInterpolator()
        colorAnimation.addUpdateListener { animator ->
            frameLayout.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.doOnEnd {
            // 애니메이션이 끝나면 상태를 재설정
            when (frameLayout) {
                frameLayoutTop -> isAnimatingTop = false
                frameLayoutBottom -> isAnimatingBottom = false
                frameLayoutLeft -> isAnimatingLeft = false
                frameLayoutRight -> isAnimatingRight = false
            }
        }
        colorAnimation.start()    }
}
