package com.example.autopayroll_mobile.auth

import android.app.Activity
import android.content.Context // ## IMPORTED FOR SHAREDPREFS ##
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.utils.SessionManager
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import androidx.lifecycle.lifecycleScope
import com.example.autopayroll_mobile.data.model.LoginRequest
import com.example.autopayroll_mobile.network.ApiClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var userLoginInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordButton: Button
    private lateinit var loadingIndicator: ProgressBar

    companion object {
        const val EXTRA_VERIFICATION_REASON = "com.example.autopayroll_mobile.auth.VERIFICATION_REASON"
        const val REASON_FORGOT_PASSWORD = "forgot_password"
        const val REASON_LOGIN_VERIFICATION = "login_verification"

        // ## ADDED CONSTANTS FOR TOKEN SAVING ##
        const val PREFS_NAME = "com.example.autopayroll_mobile.PREFS"
        const val AUTH_TOKEN = "AUTH_TOKEN"
    }

    private val verificationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        // Make sure these IDs match your login_page.xml
        userLoginInput = findViewById(R.id.LoginInput)
        passwordInput = findViewById(R.id.PassInput)
        loginButton = findViewById(R.id.loginButton)
        forgotPasswordButton = findViewById(R.id.forgotPassButton)
        loadingIndicator = findViewById(R.id.loadingIndicator)

        loginButton.setOnClickListener {
            handleLogin()
        }

        forgotPasswordButton.setOnClickListener {
            handleForgotPassword()
        }
    }

    private fun handleLogin() {
        val emailOrUsername = userLoginInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        Log.d("LoginActivity", "Input value: '$emailOrUsername'")

        if (emailOrUsername.isEmpty()) {
            userLoginInput.error = "Email or Username is required"
            userLoginInput.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            passwordInput.requestFocus()
            return
        }

        authenticateUserOnline(emailOrUsername, password)
    }

    private fun authenticateUserOnline(loginIdentifier: String, pass: String) {
        loadingIndicator.visibility = View.VISIBLE
        loginButton.isEnabled = false

        // Create the request object
        val loginRequest = LoginRequest(identifier = loginIdentifier, password = pass)

        // Get the API service from your ApiClient
        val apiService = ApiClient.getClient(this)

        // Launch a coroutine on the main thread
        // Retrofit handles background work automatically
        lifecycleScope.launch {
            try {
                // This one line makes the network call and parses the JSON
                val response = apiService.login(loginRequest)

                // Success! Save the session
                val sessionManager = SessionManager(this@LoginActivity)
                sessionManager.saveSession(response.employee_id, response.token)

                Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()

                // Proceed to verification
                val intent = Intent(this@LoginActivity, VerificationActivity::class.java)
                intent.putExtra(EXTRA_VERIFICATION_REASON, REASON_LOGIN_VERIFICATION)
                verificationLauncher.launch(intent)

            } catch (e: Exception) {
                // Handle errors (e.g., wrong password, no internet)
                Log.e("LoginActivity", "Login failed", e)
                Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()

            } finally {
                // This runs whether the try or catch block finished
                loadingIndicator.visibility = View.GONE
                loginButton.isEnabled = true
            }
        }
    }

    private fun handleForgotPassword() {
        val intent = Intent(this, VerificationActivity::class.java)
        intent.putExtra(EXTRA_VERIFICATION_REASON, REASON_FORGOT_PASSWORD)
        startActivity(intent)
    }
}