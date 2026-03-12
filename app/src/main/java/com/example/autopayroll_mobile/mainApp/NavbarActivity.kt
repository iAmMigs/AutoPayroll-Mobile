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
import androidx.camera.core.ExperimentalGetImage
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.databinding.NavbarmainBinding
import com.example.autopayroll_mobile.utils.TutorialManager
import com.example.autopayroll_mobile.utils.TutorialStep
import kotlinx.coroutines.launch

class NavbarActivity : BaseActivity() {

    private lateinit var binding: NavbarmainBinding
    private var backPressedTime: Long = 0

    private lateinit var navController: NavController
    private var currentBottomNavSelectedItemId: Int = R.id.navigation_dashboard

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                if (isLocationEnabled()) {
                    navController.navigate(R.id.navigation_qr_scanner)
                    binding.bottomNavigation.selectedItemId = R.id.navigation_qr_scanner
                    currentBottomNavSelectedItemId = R.id.navigation_qr_scanner
                } else {
                    Toast.makeText(this, "Please turn on your location", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    binding.bottomNavigation.selectedItemId = currentBottomNavSelectedItemId
                }
            } else {
                Toast.makeText(this, "Location permission is required.", Toast.LENGTH_LONG).show()
                binding.bottomNavigation.selectedItemId = currentBottomNavSelectedItemId
            }
        }

    @OptIn(ExperimentalGetImage::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = NavbarmainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        currentBottomNavSelectedItemId = R.id.navigation_dashboard

        // --- GLOBAL TUTORIAL OBSERVER ---
        var wasTutorialActive = false
        lifecycleScope.launch {
            TutorialManager.isTutorialActive.collect { isActive ->
                if (isActive) {
                    // Tutorial started! Force the navbar to visually switch to Dashboard so "Menu" isn't left highlighted.
                    if (binding.bottomNavigation.selectedItemId != R.id.navigation_dashboard) {
                        binding.bottomNavigation.selectedItemId = R.id.navigation_dashboard
                        currentBottomNavSelectedItemId = R.id.navigation_dashboard
                    }
                } else if (!isActive && wasTutorialActive) {
                    // Tutorial exited or ended! Reset to Dashboard cleanly.
                    navController.popBackStack(R.id.navigation_dashboard, false)
                    if (navController.currentDestination?.id != R.id.navigation_dashboard) {
                        navController.navigate(R.id.navigation_dashboard)
                    }
                    binding.bottomNavigation.selectedItemId = R.id.navigation_dashboard
                    currentBottomNavSelectedItemId = R.id.navigation_dashboard
                }
                wasTutorialActive = isActive
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            val twoSeconds = 2000
            if (backPressedTime + twoSeconds > System.currentTimeMillis()) {
                finish()
            } else {
                Toast.makeText(this@NavbarActivity, "Click again to exit the app", Toast.LENGTH_SHORT).show()
            }
            backPressedTime = System.currentTimeMillis()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->

            // --- AGGRESSIVE TUTORIAL NAVIGATION LOCK ---
            if (TutorialManager.isTutorialActive.value) {
                val currentStep = TutorialManager.currentStep.value

                val isStartingTutorial = currentStep == TutorialStep.DASHBOARD_WELCOME && item.itemId == R.id.navigation_dashboard

                val isAllowedPayslip = currentStep == TutorialStep.NAVIGATE_TO_PAYSLIP && item.itemId == R.id.navigation_payslip
                val isAllowedQR = currentStep == TutorialStep.NAVIGATE_TO_QR && item.itemId == R.id.navigation_qr_scanner
                val isAllowedAnnouncement = currentStep == TutorialStep.NAVIGATE_TO_ANNOUNCEMENT && item.itemId == R.id.navigation_announcement

                // NEW: Allow clicking the menu when instructed
                val isAllowedMenu = currentStep == TutorialStep.NAVIGATE_TO_MENU && item.itemId == R.id.navigation_menu

                if (!isStartingTutorial && !isAllowedPayslip && !isAllowedQR && !isAllowedAnnouncement && !isAllowedMenu) {
                    return@setOnItemSelectedListener false // HARD BLOCK
                }
            }
            // ------------------------------------------

            val previouslySelectedItemId = currentBottomNavSelectedItemId
            currentBottomNavSelectedItemId = item.itemId

            when (item.itemId) {
                R.id.navigation_qr_scanner -> {
                    val handledByCustomLogic = checkLocationAndProceed()
                    if (!handledByCustomLogic) {
                        binding.bottomNavigation.selectedItemId = previouslySelectedItemId
                        currentBottomNavSelectedItemId = previouslySelectedItemId
                    }
                    return@setOnItemSelectedListener handledByCustomLogic
                }
                R.id.navigation_dashboard -> {
                    navController.popBackStack(R.id.navigation_dashboard, false)
                    navController.navigate(R.id.navigation_dashboard)
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_menu -> {
                    navController.popBackStack(R.id.navigation_dashboard, false)
                    navController.navigate(R.id.navigation_menu)
                    return@setOnItemSelectedListener true
                }
                else -> {
                    return@setOnItemSelectedListener NavigationUI.onNavDestinationSelected(item, navController)
                }
            }
        }

        // --- LOCK THE RESELECT LISTENER TOO! ---
        binding.bottomNavigation.setOnItemReselectedListener {
            if (TutorialManager.isTutorialActive.value) {
                return@setOnItemReselectedListener // Block any random double-taps on icons breaking the tutorial
            }
            navController.popBackStack(it.itemId, false)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkLocationAndProceed(): Boolean {
        // BYPASS LOCATION REQUIREMENT DURING TUTORIAL
        if (TutorialManager.isTutorialActive.value) {
            navController.navigate(R.id.navigation_qr_scanner)
            return true
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                if (isLocationEnabled()) {
                    navController.navigate(R.id.navigation_qr_scanner)
                    return true
                } else {
                    Toast.makeText(this, "Please turn on your location", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    return false
                }
            }
            else -> {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                return false
            }
        }
    }
}