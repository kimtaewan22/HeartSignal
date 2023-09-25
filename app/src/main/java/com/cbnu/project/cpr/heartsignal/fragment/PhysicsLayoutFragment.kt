package com.cbnu.project.cpr.heartsignal.fragment

import android.R.attr.button
import android.animation.AnimatorInflater
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Button
import androidx.fragment.app.Fragment
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.databinding.FragmentPhysicsLayoutBinding
import com.jawnnypoo.physicslayout.Physics


class PhysicsLayoutFragment : Fragment(){
    private var _binding: FragmentPhysicsLayoutBinding? = null
    private val binding get() = _binding!!
    private var onCollisionListener: Physics.OnCollisionListener? = null
    private var score = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPhysicsLayoutBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.physicsLayout.physics.setOnCollisionListener(object : Physics.OnCollisionListener {
            @SuppressLint("SetTextI18n")
            override fun onCollisionEntered(viewIdA: Int, viewIdB: Int) {
            }

            override fun onCollisionExited(viewIdA: Int, viewIdB: Int) {}
        })

        binding.physicsLayout.physics.isFlingEnabled = true

        binding.physicsLayout.physics.setOnCollisionListener(object : Physics.OnCollisionListener {
            override fun onCollisionEntered(viewIdA: Int, viewIdB: Int) {
                // 여기에서 충돌이 발생했을 때 처리할 작업을 수행합니다.
                // viewIdA와 viewIdB는 충돌한 이미지(뷰)의 ID입니다.
                Log.d("BadgeShowCameraFragment","viewIdA: $viewIdA, viewIdB: $viewIdB")

                // 예를 들어, 특정 이미지가 다른 이미지에 충돌했을 때 원하는 작업을 수행할 수 있습니다.
                // 이미지와 버튼이 충돌했을 때 호출됨
                if (viewIdA == R.id.collisionBlock) {
                    // 버튼과 충돌한 경우, 이미지를 팅기도록 처리
//                    Toast.makeText(requireContext(),"crash",Toast.LENGTH_SHORT).show()
                    score += 1
                    binding.score.text = score.toString()
                }
            }

            override fun onCollisionExited(viewIdA: Int, viewIdB: Int) {
                // 충돌이 끝났을 때 처리할 작업을 수행할 수 있습니다.
            }
        })
        // XML에서 버튼을 찾아옵니다.
        val collisionBlockButton: Button = binding.collisionBlock

        // 애니메이션을 시작합니다.

        // 애니메이션을 시작합니다.
//        collisionBlockButton.setOnClickListener {
//            // 애니메이션 설정
//            val animation = TranslateAnimation(-400f, 400f, 0f, 0f)
//            animation.duration = 10000 // 10초 동안 애니메이션 진행
//            animation.fillAfter = true
//
//            // 애니메이션 리스너 설정
//            animation.setAnimationListener(object : Animation.AnimationListener {
//                override fun onAnimationStart(animation: Animation?) {
//                    // 애니메이션 시작 시 처리할 작업
//                }
//
//                override fun onAnimationEnd(animation: Animation?) {
//                    // 애니메이션 종료 시 처리할 작업
//                    // 애니메이션을 다시 시작
//                    collisionBlockButton.startAnimation(animation)
//                }
//
//                override fun onAnimationRepeat(animation: Animation?) {
//                    // 애니메이션 반복 시 처리할 작업
//                }
//            })
//
//            // 애니메이션 시작
//            collisionBlockButton.startAnimation(animation)
//        }

//        val animation = TranslateAnimation(100f, 10f, 130f, 30f)
//        animation.duration = 1000 // duartion in ms
//
//        animation.fillAfter = false
//        collisionBlockButton.startAnimation(animation)


        return view
    }


}