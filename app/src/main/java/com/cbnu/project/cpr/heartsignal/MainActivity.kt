package com.cbnu.project.cpr.heartsignal

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.cbnu.project.cpr.heartsignal.ble.BluetoothSearchActivity
import com.cbnu.project.cpr.heartsignal.databinding.ActivityMainBinding
import com.cbnu.project.cpr.heartsignal.fragment.mainFragment.MainCalendarFragment
import com.cbnu.project.cpr.heartsignal.fragment.mainFragment.MainHomeFragment
import com.cbnu.project.cpr.heartsignal.fragment.mainFragment.MainProfileFragment
import com.cbnu.project.cpr.heartsignal.step.Step0Activity
import com.hitomi.cmlibrary.OnMenuSelectedListener
import com.hitomi.cmlibrary.OnMenuStatusChangeListener
import es.dmoral.toasty.Toasty

class MainActivity : AppCompatActivity(){
    private lateinit var binding:ActivityMainBinding
    private lateinit var mainHomeFragment: MainHomeFragment
    private lateinit var mainCalendarFragment: MainCalendarFragment
    private lateinit var mainProfileFragment: MainProfileFragment
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.mainFragmentContainer) as NavHostFragment
        navController = navHostFragment.findNavController()

        mainHomeFragment = MainHomeFragment()
        mainCalendarFragment = MainCalendarFragment()
        mainProfileFragment = MainProfileFragment()

        setUpRailNavigation()
        setUpCircleMenu()
//        setupFabButtons()
    }

    private fun setUpCircleMenu() {
        binding.circleMenu.setMainMenu(Color.parseColor("#CDCDCD"), R.drawable.ic_menu, R.drawable.ic_cancel)
            .addSubMenu(Color.parseColor("#258CFF"), R.drawable.ic_bluetooth)
            .addSubMenu(Color.parseColor("#30A400"), R.drawable.ic_cpr)
            .addSubMenu(Color.parseColor("#FF4B32"), R.drawable.ic_ranking)
            .setOnMenuSelectedListener { index -> // 여기에 원하는 동작을 추가하세요
                when (index) {
                    0 -> {
                        val handler = Handler()
                        handler.postDelayed({
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    BluetoothSearchActivity::class.java
                                )
                            )
                        }, 1100)
                    }

                    1 -> {
                        val handler = Handler()
                        handler.postDelayed({
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    Step0Activity::class.java
                                )
                            )
                        }, 1100)
                    }

                    2 -> {
                        Toasty.success(this@MainActivity, "랭킹", Toast.LENGTH_SHORT, true).show();
                    }
                }
            }
            .setOnMenuStatusChangeListener(object : OnMenuStatusChangeListener {
                override fun onMenuOpened() {
                    // 메뉴가 열렸을 때의 동작을 추가하세요
                }

                override fun onMenuClosed() {
                    // 메뉴가 닫혔을 때의 동작을 추가하세요
                }
            })
    }

    private fun setUpRailNavigation() {
        binding.navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction().replace(
                        R.id.mainFragmentContainer, mainHomeFragment
                    ).commit()
                    true
                }
                R.id.nav_calendar -> {
                    supportFragmentManager.beginTransaction().replace(
                        R.id.mainFragmentContainer, mainCalendarFragment
                    ).commit()
                    true
                }
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction().replace(
                        R.id.mainFragmentContainer, mainProfileFragment
                    ).commit()
                    true
                }
                else -> false
            }
        }
    }


//    private fun setupFabButtons() {
//        binding.fabMenuActions.shrink()
//        binding.fabMenuActions.setOnClickListener(this)
//        binding.fabMenuBluetooth.setOnClickListener(this)
//        binding.fabMenuCpr.setOnClickListener(this)
//    }
//    override fun onClick(view: View?) {
//        when (view?.id) {
//            R.id.fab_menu_actions -> {
//                expandOrCollapseFAB()
//            }
//            R.id.fab_menu_bluetooth -> {
//                showToast("블루투스 연결")
//                startActivity(Intent(this@MainActivity, BluetoothSearchActivity::class.java))
//            }
//            R.id.fab_menu_cpr -> {
//                showToast("cpr훈련")
//                startActivity(Intent(this@MainActivity, Step0Activity::class.java))
//            }
//        }
//    }
//
//    private fun showToast(message: String) {
//        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
//    }
//
//    private fun expandOrCollapseFAB() {
//        if (binding.fabMenuActions.isExtended) {
//            binding.fabMenuActions.shrink()
//            binding.fabMenuBluetooth.hide()
//            binding.fabMenuAddAlarmText.visibility = View.GONE
//            binding.fabMenuCpr.hide()
//            binding.fabMenuAddPersonText.visibility = View.GONE
//        } else {
//            binding.fabMenuActions.extend()
//            binding.fabMenuBluetooth.show()
//            binding.fabMenuAddAlarmText.visibility = View.VISIBLE
//            binding.fabMenuCpr.show()
//            binding.fabMenuAddPersonText.visibility = View.VISIBLE
//        }
//    }


}