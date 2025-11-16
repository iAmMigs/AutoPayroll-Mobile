package com.example.autopayroll_mobile.mainApp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.autopayroll_mobile.auth.LoginActivity
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.network.ApiService
import com.example.autopayroll_mobile.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * An abstract base activity that handles automatic user logout after a period of inactivity.
 * Activities extending this class will be timed.
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
        // Log.d("BaseActivity", "Inactivity timer reset.")
    }

    /**
     * Starts the inactivity timer.
     */
    private fun startTimeoutTimer() {
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_MS)
        // Log.d("BaseActivity", "Inactivity timer started.")
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
     * When the activity comes back into the foreground, reset the timer.
     */
    override fun onResume() {
        super.onResume()
        resetTimeoutTimer()
    }

    /**
     * When the activity is paused (e.g., app is minimized), we *do not*
     * remove the timer callback. This allows the timer to keep running
     * in the background, as requested.
     */
    override fun onPause() {
        super.onPause()
        // We intentionally do *not* call timeoutHandler.removeCallbacks(timeoutRunnable)
        // Log.d("BaseActivity", "onPause: Timer continues to run.")
    }

    /**
     * When the activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()

        // Always clean up the handler to prevent leaks
        timeoutHandler.removeCallbacks(timeoutRunnable)

        // `isFinishing` is true if the activity is being finished by a call
        // to finish() or if the user is closing the app (e.g., back button, swipe from recents).
        if (isFinishing) {
            Log.d("BaseActivity", "Activity is finishing. Clearing session and launching 'fire-and-forget' logout task.")

            // This is a 'best effort' attempt to call the logout API.
            // It's not guaranteed to complete if the app process is killed too quickly.
            // We use GlobalScope because the Activity's scope is ending.
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    Log.d("BaseActivity", "Logout task executing API call...")
                    apiService.logout()
                    Log.d("BaseActivity", "Logout task API call successful.")
                } catch (e: Exception) {
                    Log.e("BaseActivity", "Logout task API call failed.", e)
                } finally {
                    // CRITICAL: Always clear the session locally, even if API call fails.
                    // This ensures the user is logged out on the device.
                    sessionManager.clearSession()
                    Log.d("BaseActivity", "Logout task session cleared.")
                }
            }
        }
    }

    /**
     * Performs the full logout procedure after the inactivity timeout.
     */
    private fun performAutomaticLogout() {
        Log.d("BaseActivity", "Inactivity timeout reached. Performing automatic logout.")

        // Use GlobalScope as this may be called when the app is in the background
        // and the normal lifecycleScope might not be active.
        GlobalScope.launch(Dispatchers.IO) {
            try {
                apiService.logout()
                Log.d("BaseActivity", "Automatic logout API call successful.")
            } catch (e: Exception) {
                Log.e("BaseActivity", "Automatic logout API call failed.", e)
            } finally {
                // Regardless of API success, clear the local session
                sessionManager.clearSession()

                // Redirect to LoginActivity.
                // This must run on the main thread, but we are in a coroutine.
                // We can't directly launch an activity from a background thread.
                // Instead, we prepare the intent and start it from the main context.
                launch(Dispatchers.Main) {
                    val intent = Intent(applicationContext, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)

                    // Finish all activities in the current task stack
                    finishAffinity()
                }
            }
        }
    }
}