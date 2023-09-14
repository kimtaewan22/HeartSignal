/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cbnu.project.cpr.heartsignal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.DiscretePathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.icActive)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->

            getLandmarkValueList(poseLandmarkerResult, canvas)
            // 상반신 영역에 점선 그리기
//            drawDottedLineForUpperBody(canvas)
//            for(landmark in poseLandmarkerResult.landmarks()) {
//                for(normalizedLandmark in landmark) {
//                    canvas.drawPoint(
//                        normalizedLandmark.x() * imageWidth * scaleFactor,
//                        normalizedLandmark.y() * imageHeight * scaleFactor,
//                        pointPaint
//                    )
//                }
//
//                PoseLandmarker.POSE_LANDMARKS.forEach {
//                    canvas.drawLine(
//                        poseLandmarkerResult.landmarks().get(0).get(it!!.start()).x() * imageWidth * scaleFactor,
//                        poseLandmarkerResult.landmarks().get(0).get(it.start()).y() * imageHeight * scaleFactor,
//                        poseLandmarkerResult.landmarks().get(0).get(it.end()).x() * imageWidth * scaleFactor,
//                        poseLandmarkerResult.landmarks().get(0).get(it.end()).y() * imageHeight * scaleFactor,
//                        linePaint)
//                }
//            }
        }
    }

    fun setResults(
        poseLandmarkerResults: PoseLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = poseLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }


    private fun getLandmarkValueList(poseLandmarkerResult: PoseLandmarkerResult, canvas: Canvas) {
        val landmarks = poseLandmarkerResult.landmarks().getOrNull(0)
        val leftShoulder_x =
            landmarks?.getOrNull(11)?.x()?.times(imageWidth)?.times(scaleFactor) ?: 0.0
        val leftShoulder_y =
            landmarks?.getOrNull(11)?.y()?.times(imageHeight)?.times(scaleFactor) ?: 0.0

        val rightShoulder_x =
            landmarks?.getOrNull(12)?.x()?.times(imageWidth)?.times(scaleFactor) ?: 0.0
        val rightShoulder_y =
            landmarks?.getOrNull(12)?.y()?.times(imageHeight)?.times(scaleFactor) ?: 0.0

        val leftElbow_x =
            landmarks?.getOrNull(13)?.x()?.times(imageWidth)?.times(scaleFactor) ?: 0.0
        val leftElbow_y =
            landmarks?.getOrNull(13)?.y()?.times(imageHeight)?.times(scaleFactor) ?: 0.0

        val rightElbow_x =
            landmarks?.getOrNull(14)?.x()?.times(imageWidth)?.times(scaleFactor) ?: 0.0
        val rightElbow_y =
            landmarks?.getOrNull(14)?.y()?.times(imageHeight)?.times(scaleFactor) ?: 0.0

        val leftWrist_x =
            landmarks?.getOrNull(15)?.x()?.times(imageWidth)?.times(scaleFactor) ?: 0.0
        val leftWrist_y =
            landmarks?.getOrNull(15)?.y()?.times(imageHeight)?.times(scaleFactor) ?: 0.0

        val rightWrist_x =
            landmarks?.getOrNull(16)?.x()?.times(imageWidth)?.times(scaleFactor) ?: 0.0
        val rightWrist_y =
            landmarks?.getOrNull(16)?.y()?.times(imageHeight)?.times(scaleFactor) ?: 0.0

        val leftHip_x = landmarks?.getOrNull(23)?.x()?.times(imageWidth)?.times(scaleFactor) ?: 0.0
        val leftHip_y = landmarks?.getOrNull(23)?.y()?.times(imageHeight)?.times(scaleFactor) ?: 0.0

        val rightHip_x = landmarks?.getOrNull(24)?.x()?.times(imageWidth)?.times(scaleFactor) ?: 0.0
        val rightHip_y = landmarks?.getOrNull(24)?.y()?.times(imageHeight)?.times(scaleFactor) ?: 0.0
//
//        val leftKnee_x = landmarks?.getOrNull(25)?.x()?.times(imageWidth)?.times(scaleFactor) ?: 0.0
//        val leftKnee_y = landmarks?.getOrNull(25)?.y()?.times(imageHeight)?.times(scaleFactor) ?: 0.0
//
//        val rightKnee_x = landmarks?.getOrNull(26)?.x()?.times(imageWidth)?.times(scaleFactor) ?: 0.0
//        val rightKnee_y = landmarks?.getOrNull(26)?.y()?.times(imageHeight)?.times(scaleFactor) ?: 0.0
//
//        val leftAnkle_x = landmarks?.getOrNull(27)?.x()?.times(imageWidth)?.times(scaleFactor) ?: 0.0
//        val leftAnkle_y = landmarks?.getOrNull(27)?.y()?.times(imageHeight)?.times(scaleFactor) ?: 0.0
//
//        val rightAnkle_x = landmarks?.getOrNull(28)?.x()?.times(imageWidth)?.times(scaleFactor) ?: 0.0
//        val rightAnkle_y = landmarks?.getOrNull(28)?.y()?.times(imageHeight)?.times(scaleFactor) ?: 0.0

        // 좌측 사람 영역을 점선으로 그리기
        val leftBodyRect = RectF(
            leftShoulder_x.toFloat(),
            leftShoulder_y.toFloat(),
            leftWrist_x.toFloat(),
            leftWrist_y.toFloat()
        )

// 우측 사람 영역을 점선으로 그리기
        val rightBodyRect = RectF(
            rightShoulder_x.toFloat(),
            rightShoulder_y.toFloat(),
            rightWrist_x.toFloat(),
            rightWrist_y.toFloat()
        )

        val leftShoulderElbowWrist =
            if (leftShoulder_x != 0.0 && leftShoulder_y != 0.0 && leftElbow_x != 0.0 && leftElbow_y != 0.0 && leftWrist_x != 0.0 && leftWrist_x != 0.0) {
                calculateAngle(
                    leftShoulder_x.toFloat(),
                    leftShoulder_y.toFloat(),
                    leftElbow_x.toFloat(),
                    leftElbow_y.toFloat(),
                    leftWrist_x.toFloat(),
                    leftWrist_y.toFloat()
                )
            } else {
                0.0
            }

        val rightShoulderElbowWrist =
            if (leftShoulder_x != 0.0 && leftShoulder_y != 0.0 && rightElbow_x != 0.0 && rightElbow_y != 0.0 && rightWrist_x != 0.0 && rightWrist_x != 0.0) {
                calculateAngle(
                    rightShoulder_x.toFloat(),
                    rightShoulder_y.toFloat(),
                    rightElbow_x.toFloat(),
                    rightElbow_y.toFloat(),
                    rightWrist_x.toFloat(),
                    rightWrist_y.toFloat()
                )
            } else {
                0.0
            }

        Log.d("CameraFragment","leftShoulderElbowWrist :$leftShoulderElbowWrist, rightShoulderElbowWrist: $rightShoulderElbowWrist")

        linePaint.color =
            ContextCompat.getColor(
                context!!,
                R.color.icFocused
            )
        //몸의 중심부
//        canvas.drawLine(
//            leftShoulder_x.toFloat(),
//            leftShoulder_y.toFloat(),
//            rightShoulder_x.toFloat(),
//            rightShoulder_y.toFloat(),
//            linePaint
//        )

        //몸의 좌측 탐색
        if (leftShoulderElbowWrist >= 155){
            linePaint.color =
                ContextCompat.getColor(
                    context!!,
                    R.color.correct_pose
                )
            canvas.drawLine(
                leftShoulder_x.toFloat(),
                leftShoulder_y.toFloat(),
                leftElbow_x.toFloat(),
                leftElbow_y.toFloat(),
                linePaint
            )
            canvas.drawLine(
                leftElbow_x.toFloat(),
                leftElbow_y.toFloat(),
                leftWrist_x.toFloat(),
                leftWrist_y.toFloat(),
                linePaint
            )
        }
        else if (leftShoulderElbowWrist < 155) // 올바르지 못함
        {
            linePaint.color =
                ContextCompat.getColor(
                    context!!,
                    R.color.incorrect_pose
                )
            canvas.drawLine(
                leftShoulder_x.toFloat(),
                leftShoulder_y.toFloat(),
                leftElbow_x.toFloat(),
                leftElbow_y.toFloat(),
                linePaint
            )
            canvas.drawLine(
                leftElbow_x.toFloat(),
                leftElbow_y.toFloat(),
                leftWrist_x.toFloat(),
                leftWrist_y.toFloat(),
                linePaint
            )
        }
        // 몸의 우측 탐색
        if (rightShoulderElbowWrist >= 155){
            linePaint.color =
                ContextCompat.getColor(
                    context!!,
                    R.color.correct_pose
                )
            canvas.drawLine(
                rightShoulder_x.toFloat(),
                rightShoulder_y.toFloat(),
                rightElbow_x.toFloat(),
                rightElbow_y.toFloat(),
                linePaint
            )
            canvas.drawLine(
                rightElbow_x.toFloat(),
                rightElbow_y.toFloat(),
                rightWrist_x.toFloat(),
                rightWrist_y.toFloat(),
                linePaint
            )
        }
        else if (rightShoulderElbowWrist < 155) // 올바르지 못함
        {
            linePaint.color =
                ContextCompat.getColor(
                    context!!,
                    R.color.incorrect_pose
                )
            canvas.drawLine(
                rightShoulder_x.toFloat(),
                rightShoulder_y.toFloat(),
                rightElbow_x.toFloat(),
                rightElbow_y.toFloat(),
                linePaint
            )
            canvas.drawLine(
                rightElbow_x.toFloat(),
                rightElbow_y.toFloat(),
                rightWrist_x.toFloat(),
                rightWrist_y.toFloat(),
                linePaint
            )
        }
        linePaint.color =
            ContextCompat.getColor(
                context!!,
                R.color.icFocused)
        drawDottedLineForUpperBody(canvas, leftBodyRect, rightBodyRect, linePaint)
//        // 점선 스타일 설정
//        linePaint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
//
//        // 점선 그리기
//        canvas.drawLine(
//            leftShoulder_x.toFloat(),
//            leftShoulder_y.toFloat(),
//            leftElbow_x.toFloat(),
//            leftElbow_y.toFloat(),
//            linePaint
//        )
//        canvas.drawLine(
//            leftElbow_x.toFloat(),
//            leftElbow_y.toFloat(),
//            leftWrist_x.toFloat(),
//            leftWrist_y.toFloat(),
//            linePaint
//        )

        // 점선 스타일 초기화
//        linePaint.pathEffect = null


    }


    private fun calculateAngle(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float
    ): Double {
        val dx1 = (x1 - x2).toDouble()
        val dy1 = (y1 - y2).toDouble()
        val dx2 = (x3 - x2).toDouble()
        val dy2 = (y3 - y2).toDouble()

        val angle1 = Math.atan2(dy1, dx1)
        val angle2 = Math.atan2(dy2, dx2)

        var angle = Math.toDegrees(angle2 - angle1)
        if (angle < 0) angle += 360.0

        angle = normalizeAngle(angle)
        return angle
    }

    // 보정 함수
    private fun normalizeAngle(angle: Double): Double {
        var normalizedAngle = angle % 360.0
        if (normalizedAngle < 0) {
            normalizedAngle += 360.0
        }
        return if (normalizedAngle <= 180.0) normalizedAngle else 360.0 - normalizedAngle
    }
    // landmarks에서 상반신 영역 좌표를 얻은 후 호출하는 함수



//     상반신 영역에 점선 그리는 함수
    private fun drawDottedLineForUpperBody(canvas: Canvas, leftBodyRect: RectF, rightBodyRect: RectF, linePaint: Paint) {
        // 점선 스타일 설정
        val segmentLength = 20f // 일정한 간격을 20 픽셀로 설정
        val deviation = 5f // 간격을 랜덤하게 흔들어주는 정도를 5 픽셀로 설정
//        linePaint.pathEffect = DiscretePathEffect(segmentLength, deviation)
//        linePaint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        linePaint.pathEffect = CornerPathEffect(5f)



    // 좌측 사각형 그리기
        canvas.drawRect(leftBodyRect, linePaint)
        // 우측 사각형 그리기
        canvas.drawRect(rightBodyRect, linePaint)
        // 점선 스타일 초기화
        linePaint.pathEffect = null
    }



}