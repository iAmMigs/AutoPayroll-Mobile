package com.example.autopayroll_mobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.autopayroll_mobile.databinding.DashboardBinding
import androidx.core.view.WindowCompat

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: DashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = DashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    // Helper function to switch fragments
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}