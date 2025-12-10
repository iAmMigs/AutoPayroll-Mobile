package com.example.autopayroll_mobile.mainApp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.autopayroll_mobile.auth.LoginActivity
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.network.ApiService
import com.example.autopayroll_mobile.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

/**
 * An abstract base activity that handles automatic user logout after a period of inactivity.
 * Activities extending this class will be timed.
 * Also handles security checks for Developer Options, Emulator detection, and Root access.
 */
abstract class BaseActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService

    // Handler for the main thread to run the timeout logic
    private val timeoutHandler = Handler(Looper.getMainLooper())

    // Runnable that contains the logout action
    private val timeoutRunnable = Runnable {
        performAutomaticLogout()
    }

    private var securityDialog: AlertDialog? = null

    companion object {
        // 30 minutes in milliseconds
        private const val TIMEOUT_MS = 30 * 60 * 1000L
        // FOR TESTING: Use 1 minute (60 * 1000L) or 30 seconds (30 * 1000L)
        // private const val TIMEOUT_MS = 30 * 1000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize session and API client for logout operations
        sessionManager = SessionManager(applicationContext)
        apiService = ApiClient.getClient(applicationContext)
    }

    /**
     * Resets the inactivity timer.
     * This removes any pending timeout and schedules a new one.
     */
    private fun resetTimeoutTimer() {
        timeoutHandler.removeCallbacks(timeoutRunnable)
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_MS)
    }

    /**
     * This method is called whenever the user interacts with the screen.
     * It's the primary way we reset the inactivity timer.
     */
    override fun onUserInteraction() {
        super.onUserInteraction()
        resetTimeoutTimer()
    }

    /**
     * When the activity comes back into the foreground, reset the timer and check security.
     */
    override fun onResume() {
        super.onResume()
        resetTimeoutTimer()
        checkSecurityConstraints()
    }

    /**
     * When the activity is paused (e.g., app is minimized), we *do not*
     * remove the timer callback. This allows the timer to keep running
     * in the background, as requested.
     */
    override fun onPause() {
        super.onPause()

        // Dismiss dialog to avoid leaks, it will show again on Resume if needed
        if (securityDialog?.isShowing == true) {
            securityDialog?.dismiss()
        }
    }

    /**
     * When the activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()

        // Always clean up the handler to prevent leaks
        timeoutHandler.removeCallbacks(timeoutRunnable)

        if (isFinishing) {
            Log.d("BaseActivity", "Activity is finishing. Clearing session and launching 'fire-and-forget' logout task.")

            // This is a 'best effort' attempt to call the logout API.
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    apiService.logout()
                } catch (e: Exception) {
                    Log.e("BaseActivity", "Logout task API call failed.", e)
                } finally {
                    sessionManager.clearSession()
                }
            }
        }
    }

    /**
     * Performs the full logout procedure after the inactivity timeout.
     */
    private fun performAutomaticLogout() {
        Log.d("BaseActivity", "Inactivity timeout reached. Performing automatic logout.")

        GlobalScope.launch(Dispatchers.IO) {
            try {
                apiService.logout()
            } catch (e: Exception) {
                Log.e("BaseActivity", "Automatic logout API call failed.", e)
            } finally {
                sessionManager.clearSession()

                launch(Dispatchers.Main) {
                    val intent = Intent(applicationContext, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finishAffinity()
                }
            }
        }
    }

    // ================= SECURITY CHECKS =================

    /**
     * Checks if the device violates security policies (Emulator, Root, or Developer Options).
     */
    private fun checkSecurityConstraints() {
        // 1. Check for Emulator
        if (isEmulator()) {
            showSecurityDialog(
                title = "Device Not Supported",
                message = "AutoPayroll cannot be used on emulators (BlueStacks, Nox, LDPlayer, etc).",
                isFixable = false
            )
            return
        }

        // 2. Check for Rooted Device
        if (isRooted()) {
            showSecurityDialog(
                title = "Security Risk Detected",
                message = "For security reasons, AutoPayroll cannot be used on rooted devices.",
                isFixable = false
            )
            return
        }

        // 3. Check for Developer Options
        if (isDevOptionsEnabled()) {
            showSecurityDialog(
                title = "Developer Options Enabled",
                message = "Please disable Developer Options to use AutoPayroll.",
                isFixable = true
            )
            return
        }
    }

    /**
     * Returns true if Developer Options are enabled on the device.
     */
    private fun isDevOptionsEnabled(): Boolean {
        return try {
            Settings.Global.getInt(
                contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
            ) != 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Returns true if the device appears to be rooted.
     */
    private fun isRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )

        for (path in paths) {
            if (File(path).exists()) {
                return true
            }
        }

        return false
    }

    /**
     * Returns true if the app is running on an emulator.
     * Updated to target BlueStacks, Nox, LDPlayer, MEmu, and Google Play Games for PC.
     */
    private fun isEmulator(): Boolean {
        // 1. Check Google Play Games for PC specifically
        if (packageManager.hasSystemFeature("com.google.android.play.feature.HPE_EXPERIENCE")) {
            return true
        }

        val phoneInfo = (Build.MANUFACTURER + Build.MODEL + Build.BRAND + Build.PRODUCT + Build.HARDWARE + Build.FINGERPRINT + Build.BOARD + Build.BOOTLOADER).lowercase(Locale.ROOT)

        val emulatorKeywords = listOf(
            "bluestacks",  // BlueStacks
            "nox",         // NoxPlayer
            "ldplayer",    // LDPlayer
            "memu",        // MEmu Play
            "koplayer",    // KoPlayer
            "genymotion",  // Genymotion
            "ami",         // AMIDuOS
            "remix",       // RemixOS
            "phoenix",     // PhoenixOS
            "vbox",        // VirtualBox (used by many emulators)
            "goldfish",    // Android Emulator standard
            "ranchu",      // Android Emulator standard
            "sdk_gphone",  // Android Studio Emulator
            "google_sdk"   // Old Google SDK
        )

        // Check if any keyword exists in the phone info
        if (emulatorKeywords.any { phoneInfo.contains(it) }) {
            return true
        }

        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
    }

    private fun showSecurityDialog(title: String, message: String, isFixable: Boolean) {
        if (securityDialog?.isShowing == true) return

        val builder = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false) // Prevent clicking outside to dismiss

        if (isFixable) {
            builder.setPositiveButton("Open Settings") { _, _ ->
                try {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                } catch (e: Exception) {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
            builder.setNegativeButton("Exit") { _, _ ->
                finishAffinity()
            }
        } else {
            builder.setPositiveButton("Close") { _, _ ->
                finishAffinity()
            }
        }

        securityDialog = builder.create()
        securityDialog?.show()
    }
}