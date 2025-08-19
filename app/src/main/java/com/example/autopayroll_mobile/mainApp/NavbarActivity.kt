package com.example.autopayroll_mobile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.autopayroll_mobile.databinding.NavbarmainBinding
import com.example.autopayroll_mobile.mainApp.QrScannerFragment

class NavbarActivity : AppCompatActivity() {

    private lateinit var binding: NavbarmainBinding
    private var backPressedTime: Long = 0

    @OptIn(ExperimentalGetImage::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = NavbarmainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Modern way to handle back press
        onBackPressedDispatcher.addCallback(this) {
            val twoSeconds = 2000
            if (backPressedTime + twoSeconds > System.currentTimeMillis()) {
                finish() // Exit the app
            } else {
                Toast.makeText(this@NavbarActivity, "Click again to exit the app", Toast.LENGTH_SHORT).show()
            }
            backPressedTime = System.currentTimeMillis()
        }

        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    replaceFragment(DashboardFragment())
                    true
                }
                R.id.navigation_payslip -> {
                    replaceFragment(PayslipFragment())
                    true
                }
                R.id.navigation_qr_scanner -> {
                    replaceFragment(QrScannerFragment())
                    true
                }
                R.id.navigation_announcement -> {
                    replaceFragment(AnnouncementFragment())
                    true
                }
                R.id.navigation_menu -> {
                    replaceFragment(MenuFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}