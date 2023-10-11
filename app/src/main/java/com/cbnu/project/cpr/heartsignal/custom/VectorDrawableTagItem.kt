package com.cbnu.project.cpr.heartsignal.custom

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.TextPaint
import com.magicgoop.tagsphere.item.TagItem
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class VectorDrawableTagItem(private val drawable: Drawable) : TagItem() {

    init {
        val fixedSize = 44.dpToPx() // 24dp를 픽셀로 변환
        drawable.setBounds(0, 0, fixedSize, fixedSize)
    }

    override fun drawSelf(
        x: Float,
        y: Float,
        canvas: Canvas,
        paint: TextPaint,
        easingFunction: ((t: Float) -> Float)?
    ) {
        canvas.save()
        val fixedSize = 24.dpToPx() // 24dp를 픽셀로 변환
        canvas.translate(x - fixedSize / 2f, y - fixedSize / 2f)
        val alpha = easingFunction?.let { calc ->
            val ease = calc(getEasingValue())
            if (!ease.isNaN()) max(0, min(255, (255 * ease).roundToInt())) else 0
        } ?: 255
        drawable.alpha = alpha
        drawable.draw(canvas)
        canvas.restore()
    }

    private fun Int.dpToPx(): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }
}