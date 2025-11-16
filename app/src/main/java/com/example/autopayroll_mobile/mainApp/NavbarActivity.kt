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
import com.example.autopayroll_mobile.mainApp.BaseActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.databinding.NavbarmainBinding

import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController

class NavbarActivity : BaseActivity() {

    private lateinit var binding: NavbarmainBinding
    private var backPressedTime: Long = 0

    private lateinit var navController: NavController

    // Keep track of the currently selected bottom nav item ID for resetting
    private var currentBottomNavSelectedItemId: Int = R.id.navigation_dashboard // Initialize with start destination

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                if (isLocationEnabled()) {
                    // Navigate to QR scanner. This is the custom navigation for the QR button.
                    navController.navigate(R.id.navigation_qr_scanner)
                    // Update the highlight manually if navigation was successful
                    binding.bottomNavigation.selectedItemId = R.id.navigation_qr_scanner
                    currentBottomNavSelectedItemId = R.id.navigation_qr_scanner
                } else {
                    Toast.makeText(this, "Please turn on your location", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    // Revert selection if location is not turned on
                    binding.bottomNavigation.selectedItemId = currentBottomNavSelectedItemId
                }
            } else {
                Toast.makeText(this, "Location permission is required.", Toast.LENGTH_LONG).show()
                // Revert selection if permission is denied
                binding.bottomNavigation.selectedItemId = currentBottomNavSelectedItemId
            }
        }

    @OptIn(ExperimentalGetImage::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = NavbarmainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Initialize currentBottomNavSelectedItemId with the actual start destination
        currentBottomNavSelectedItemId = R.id.navigation_dashboard // Assuming this is your start destination from XML

        onBackPressedDispatcher.addCallback(this) {
            val twoSeconds = 2000
            if (backPressedTime + twoSeconds > System.currentTimeMillis()) {
                finish()
            } else {
                Toast.makeText(this@NavbarActivity, "Click again to exit the app", Toast.LENGTH_SHORT).show()
            }
            backPressedTime = System.currentTimeMillis()
        }

        // Correct way to set up bottom navigation with a custom listener for a specific item
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            // Update the stored selected item ID first
            val previouslySelectedItemId = currentBottomNavSelectedItemId
            currentBottomNavSelectedItemId = item.itemId

            when (item.itemId) {
                R.id.navigation_qr_scanner -> {
                    // Custom logic for QR scanner
                    val handledByCustomLogic = checkLocationAndProceed()
                    if (!handledByCustomLogic) {
                        // If custom logic didn't navigate (e.g., permission denied),
                        // revert the UI selection to the previous item
                        binding.bottomNavigation.selectedItemId = previouslySelectedItemId
                        currentBottomNavSelectedItemId = previouslySelectedItemId
                    }
                    return@setOnItemSelectedListener handledByCustomLogic // Return true if handled, false otherwise
                }

                // FIX: Explicitly handle Dashboard navigation to ensure it always pops the stack.
                R.id.navigation_dashboard -> {
                    // Navigate to Dashboard, ensuring the stack is popped to the Dashboard itself (inclusive: false)
                    navController.popBackStack(R.id.navigation_dashboard, false)
                    navController.navigate(R.id.navigation_dashboard)
                    return@setOnItemSelectedListener true
                }

                // FIX: Explicitly handle navigation back to the Menu fragment
                R.id.navigation_menu -> {
                    // Pop stack up to the Dashboard (root) and then navigate to Menu
                    navController.popBackStack(R.id.navigation_dashboard, false)
                    navController.navigate(R.id.navigation_menu)
                    return@setOnItemSelectedListener true
                }

                else -> {
                    // For all other top-level items (Payslip, Announcement, etc.)
                    return@setOnItemSelectedListener NavigationUI.onNavDestinationSelected(item, navController)
                }
            }
        }

        // Only use setOnItemReselectedListener for special reselection behavior.
        binding.bottomNavigation.setOnItemReselectedListener {
            // For all top-level destinations, pop the back stack to the tab's root destination
            navController.popBackStack(it.itemId, false)
        }
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
                    navController.navigate(R.id.navigation_qr_scanner)
                    return true // Navigation performed
                } else {
                    Toast.makeText(this, "Please turn on your location", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    return false // Did not navigate due to location being off
                }
            }
            else -> {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                return false // Did not navigate yet, permission request initiated
            }
        }
    }
}