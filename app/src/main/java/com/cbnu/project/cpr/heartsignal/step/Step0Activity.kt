package com.cbnu.project.cpr.heartsignal.step

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.databinding.ActivityStep0Binding
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.daimajia.androidviewhover.BlurLayout

class Step0Activity : AppCompatActivity() {
    private lateinit var binding: ActivityStep0Binding

    private lateinit var mContext: Context
    private lateinit var mSampleLayout: BlurLayout
    private lateinit var mSampleLayout2: BlurLayout
    private lateinit var mSampleLayout3: BlurLayout
    private lateinit var mSampleLayout4: BlurLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        binding = ActivityStep0Binding.inflate(layoutInflater)
        setContentView(binding.root)
        BlurLayout.setGlobalDefaultDuration(450)


        mSampleLayout = binding.blurLayout
        setupSampleLayout1()

        mSampleLayout2 = binding.blurLayout2
        setupSampleLayout2()

        mSampleLayout3 = binding.blurLayout3
        setupSampleLayout3()

        mSampleLayout4 = binding.blurLayout4
        setupSampleLayout4()

        binding.nextBtn0.setOnClickListener {
            val intent = Intent(this@Step0Activity, StepProgressActivity::class.java)
            intent.putExtra("stepFlag", "음성 인식")
            startActivity(intent)
        }
    }
    private fun setupSampleLayout1() {
        val hover = LayoutInflater.from(mContext).inflate(R.layout.hover_sample, null)
        hover.findViewById<View>(R.id.heart).setOnClickListener {
            YoYo.with(Techniques.Tada)
                .duration(550)
                .playOn(it)
        }
        hover.findViewById<View>(R.id.share).setOnClickListener {
            YoYo.with(Techniques.Swing)
                .duration(550)
                .playOn(it)
        }
        mSampleLayout.setHoverView(hover)
        mSampleLayout.setBlurDuration(550)

        mSampleLayout.addChildAppearAnimator(hover, R.id.heart, Techniques.FlipInX, 550, 0)
        mSampleLayout.addChildAppearAnimator(hover, R.id.share, Techniques.FlipInX, 550, 250)
        mSampleLayout.addChildAppearAnimator(hover, R.id.more, Techniques.FlipInX, 550, 500)

        mSampleLayout.addChildDisappearAnimator(hover, R.id.heart, Techniques.FlipOutX, 550, 500)
        mSampleLayout.addChildDisappearAnimator(hover, R.id.share, Techniques.FlipOutX, 550, 250)
        mSampleLayout.addChildDisappearAnimator(hover, R.id.more, Techniques.FlipOutX, 550, 0)

        mSampleLayout.addChildAppearAnimator(hover, R.id.description, Techniques.FadeInUp)
        mSampleLayout.addChildDisappearAnimator(hover, R.id.description, Techniques.FadeOutDown)
    }

    private fun setupSampleLayout2() {
        val hover2 = LayoutInflater.from(mContext).inflate(R.layout.hover_sample2, null)
        hover2.findViewById<View>(R.id.avatar).setOnClickListener {
            Toast.makeText(mContext, "Pretty Cool, Right?", Toast.LENGTH_SHORT).show()
        }
        mSampleLayout2.setHoverView(hover2)

        mSampleLayout2.addChildAppearAnimator(hover2, R.id.description, Techniques.FadeInUp)
        mSampleLayout2.addChildDisappearAnimator(hover2, R.id.description, Techniques.FadeOutDown)
        mSampleLayout2.addChildAppearAnimator(hover2, R.id.avatar, Techniques.DropOut, 1200)
        mSampleLayout2.addChildDisappearAnimator(hover2, R.id.avatar, Techniques.FadeOutUp)
        mSampleLayout2.setBlurDuration(1000)
    }

    private fun setupSampleLayout3() {
        val hover3 = LayoutInflater.from(mContext).inflate(R.layout.hover_sample3, null)
        mSampleLayout3.setHoverView(hover3)

        mSampleLayout3.addChildAppearAnimator(hover3, R.id.eye, Techniques.Landing)
        mSampleLayout3.addChildDisappearAnimator(hover3, R.id.eye, Techniques.TakingOff)
        mSampleLayout3.enableZoomBackground(true)
        mSampleLayout3.setBlurDuration(1200)
    }

    private fun setupSampleLayout4() {
        val hover4 = LayoutInflater.from(mContext).inflate(R.layout.hover_sample4, null)
        mSampleLayout4.setHoverView(hover4)

        mSampleLayout4.addChildAppearAnimator(hover4, R.id.cat, Techniques.SlideInLeft)
        mSampleLayout4.addChildAppearAnimator(hover4, R.id.mail, Techniques.SlideInRight)
        mSampleLayout4.addChildDisappearAnimator(hover4, R.id.cat, Techniques.SlideOutLeft)
        mSampleLayout4.addChildDisappearAnimator(hover4, R.id.mail, Techniques.SlideOutRight)

        hover4.findViewById<View>(R.id.cat).setOnClickListener {
            val getWebPage = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/daimajia"))
            startActivity(getWebPage)
        }

        hover4.findViewById<View>(R.id.mail).setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.type = "plain/text"
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("daimajia@gmail.com"))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "About AndroidViewHover")
            emailIntent.putExtra(Intent.EXTRA_TEXT, "I have a good idea about this project..")
            startActivity(Intent.createChooser(emailIntent, "Send mail..."))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}