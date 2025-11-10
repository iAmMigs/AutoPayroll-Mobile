package com.example.autopayroll_mobile.mainApp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.example.autopayroll_mobile.AnnouncementFragment
import com.example.autopayroll_mobile.DashboardFragment
import com.example.autopayroll_mobile.MenuFragment
import com.example.autopayroll_mobile.PayslipFragment
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.databinding.NavbarmainBinding
import com.example.autopayroll_mobile.mainApp.QrScannerFragment

class NavbarActivity : AppCompatActivity() {

    private lateinit var binding: NavbarmainBinding
    private var backPressedTime: Long = 0

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                if (isLocationEnabled()) {
                    replaceFragment(QrScannerFragment())
                    // MANUALLY update the highlight after successful permission grant
                    binding.bottomNavigation.selectedItemId = R.id.navigation_qr_scanner
                } else {
                    Toast.makeText(this, "Please turn on your location", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            } else {
                Toast.makeText(this, "Location permission is required for the QR scanner.", Toast.LENGTH_LONG).show()
            }
        }

    @OptIn(ExperimentalGetImage::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = NavbarmainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this) {
            val twoSeconds = 2000
            if (backPressedTime + twoSeconds > System.currentTimeMillis()) {
                finish()
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
                    checkLocationAndProceed()
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

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkLocationAndProceed(): Boolean {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                if (isLocationEnabled()) {
                    replaceFragment(QrScannerFragment())
                    return true // Success!
                } else {
                    Toast.makeText(this, "Please turn on your location", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    return false // Did not navigate
                }
            }
            else -> {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                return false // Did not navigate yet
            }
        }
    }
}