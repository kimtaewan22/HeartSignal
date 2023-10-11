package com.cbnu.project.cpr.heartsignal.auth

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cbnu.project.cpr.heartsignal.MainActivity
import com.cbnu.project.cpr.heartsignal.databinding.ActivityAuthLoginBinding
import com.cbnu.project.cpr.heartsignal.step.Step2Activity
import com.royrodriguez.transitionbutton.TransitionButton
import com.royrodriguez.transitionbutton.TransitionButton.OnAnimationStopEndListener


class AuthLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthLoginBinding
    private lateinit var transitionButton: TransitionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthLoginBinding.inflate(layoutInflater)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(binding.root)

        requestPermission()
        transitionButton = binding.transitionButton
        if (allPermissionsGranted()) {
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS)
        }

        binding.testBtn.setOnClickListener {
            startActivity(Intent(this@AuthLoginActivity, Step2Activity::class.java))
        }

//        binding.authLogin.setOnClickListener {
//            val intent = Intent(this@AuthLoginActivity, MainActivity::class.java)
////            intent.putExtra("stepFlag", "훈련 설명")
//            startActivity(intent)
//        }

        transitionButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // Start the loading animation when the user tap the button
                transitionButton.startAnimation()

                // Do your networking task or background work here.
                val handler = Handler()
                handler.postDelayed(Runnable {
                    val isSuccessful = true

                    // Choose a stop animation if your call was succesful or not
                    if (isSuccessful) {
                        transitionButton.stopAnimation(TransitionButton.StopAnimationStyle.EXPAND,
                            OnAnimationStopEndListener {
                                val intent = Intent(baseContext, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                startActivity(intent)
                            })
                    } else {
                        transitionButton.stopAnimation(
                            TransitionButton.StopAnimationStyle.SHAKE,
                            null
                        )
                    }
                }, 2000)
            }
        })
    }


    private fun requestPermission() {
        // 버전 체크, 권한 허용했는지 체크
        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(this@AuthLoginActivity, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this@AuthLoginActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}