package com.cbnu.project.cpr.heartsignal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.cbnu.project.cpr.heartsignal.ble.BluetoothSearchActivity
import com.cbnu.project.cpr.heartsignal.databinding.ActivityMainBinding
import com.cbnu.project.cpr.heartsignal.step.Step0Activity

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFabButtons()
    }
    private fun setupFabButtons() {
        binding.fabMenuActions.shrink()
        binding.fabMenuActions.setOnClickListener(this)
        binding.fabMenuBluetooth.setOnClickListener(this)
        binding.fabMenuCpr.setOnClickListener(this)
    }
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.fab_menu_actions -> {
                expandOrCollapseFAB()
            }
            R.id.fab_menu_bluetooth -> {
                showToast("블루투스 연결")
                startActivity(Intent(this@MainActivity, BluetoothSearchActivity::class.java))
            }
            R.id.fab_menu_cpr -> {
                showToast("cpr훈련")
                startActivity(Intent(this@MainActivity, Step0Activity::class.java))
            }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun expandOrCollapseFAB() {
        if (binding.fabMenuActions.isExtended) {
            binding.fabMenuActions.shrink()
            binding.fabMenuBluetooth.hide()
            binding.fabMenuAddAlarmText.visibility = View.GONE
            binding.fabMenuCpr.hide()
            binding.fabMenuAddPersonText.visibility = View.GONE
        } else {
            binding.fabMenuActions.extend()
            binding.fabMenuBluetooth.show()
            binding.fabMenuAddAlarmText.visibility = View.VISIBLE
            binding.fabMenuCpr.show()
            binding.fabMenuAddPersonText.visibility = View.VISIBLE
        }
    }


}