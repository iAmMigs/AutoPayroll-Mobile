package com.example.autopayroll_mobile.mainApp

import android.content.Intent
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize session and API client for logout operations
        sessionManager = SessionManager(applicationContext)
        apiService = ApiClient.getClient(applicationContext)
    }

    private fun resetTimeoutTimer() {
        timeoutHandler.removeCallbacks(timeoutRunnable)
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_MS)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetTimeoutTimer()
    }

    override fun onResume() {
        super.onResume()
        resetTimeoutTimer()
        checkSecurityConstraints()
    }

    override fun onPause() {
        super.onPause()
        if (securityDialog?.isShowing == true) {
            securityDialog?.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Always clean up the handler to prevent leaks
        timeoutHandler.removeCallbacks(timeoutRunnable)

        // --- FIX: REMOVED THE SESSION CLEARING LOGIC HERE ---
        // We do NOT want to clear the session just because an activity finishes.
        // This was causing the "Session Expired" loop when moving from Login -> Dashboard.
    }

    /**
     * Performs the full logout procedure after the inactivity timeout.
     */
    private fun performAutomaticLogout() {
        Log.d("BaseActivity", "Inactivity timeout reached. Performing automatic logout.")

        // Only logout if we actually have a session to clear (prevents loops on Login screen)
        if (sessionManager.getToken() == null) {
            return
        }

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

    private fun checkSecurityConstraints() {
        if (isEmulator()) {
            showSecurityDialog("Device Not Supported", "AutoPayroll cannot be used on emulators.", false)
            return
        }
        if (isRooted()) {
            showSecurityDialog("Security Risk Detected", "For security reasons, AutoPayroll cannot be used on rooted devices.", false)
            return
        }
        if (isDevOptionsEnabled()) {
            showSecurityDialog("Developer Options Enabled", "Please disable Developer Options to use AutoPayroll.", true)
            return
        }
    }

    private fun isDevOptionsEnabled(): Boolean {
        return try {
            Settings.Global.getInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0
        } catch (e: Exception) { false }
    }

    private fun isRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) return true

        val paths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"
        )
        return paths.any { File(it).exists() }
    }

    private fun isEmulator(): Boolean {
        if (packageManager.hasSystemFeature("com.google.android.play.feature.HPE_EXPERIENCE")) return true
        val phoneInfo = (Build.MANUFACTURER + Build.MODEL + Build.BRAND + Build.PRODUCT + Build.HARDWARE + Build.FINGERPRINT + Build.BOARD + Build.BOOTLOADER).lowercase(Locale.ROOT)
        val emulatorKeywords = listOf("bluestacks", "nox", "ldplayer", "memu", "koplayer", "genymotion", "ami", "remix", "phoenix", "vbox", "goldfish", "ranchu", "sdk_gphone", "google_sdk")
        return emulatorKeywords.any { phoneInfo.contains(it) } || Build.FINGERPRINT.startsWith("generic") || Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Emulator") || Build.PRODUCT.contains("sdk")
    }

    private fun showSecurityDialog(title: String, message: String, isFixable: Boolean) {
        if (securityDialog?.isShowing == true) return

        val builder = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)

        if (isFixable) {
            builder.setPositiveButton("Open Settings") { _, _ ->
                try {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                } catch (e: Exception) {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            }
            builder.setNegativeButton("Exit") { _, _ -> finishAffinity() }
        } else {
            builder.setPositiveButton("Close") { _, _ -> finishAffinity() }
        }

        securityDialog = builder.create()
        securityDialog?.show()
    }
}