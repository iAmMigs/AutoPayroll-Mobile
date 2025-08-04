package com.example.autopayroll_mobile // <-- IMPORTANT: Make sure this is your correct package name

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.autopayroll_mobile.databinding.DashboardBinding // <-- IMPORTANT: This is the auto-generated class for dashboard.xml

class DashboardActivity : AppCompatActivity() {

    // Declare the binding variable
    private lateinit var binding: DashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout and set the content view
        binding = DashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the default fragment when the activity starts
        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment()) // Assuming you have a DashboardFragment
        }

        // Set the listener for the bottom navigation
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