package com.example.autopayroll_mobile.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns // For email validation
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.autopayroll_mobile.DashboardActivity // Make sure this import is correct
import com.example.autopayroll_mobile.R

class loginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordButton: Button

    companion object {
        const val EXTRA_VERIFICATION_REASON = "com.example.autopayroll_mobile.auth.VERIFICATION_REASON"
        const val REASON_FORGOT_PASSWORD = "forgot_password"
        const val REASON_LOGIN_VERIFICATION = "login_verification"
    }

    // Modern way to handle Activity Results for verification
    private val verificationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // This result comes from verificationActivity when login OTP is successful
            // and DashboardActivity has been launched.
            // Now, we can safely finish loginActivity.
            finish()
        }
        // You could also handle RESULT_CANCELED or other results if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        emailInput = findViewById(R.id.EmailInput)
        passwordInput = findViewById(R.id.PassInput)
        loginButton = findViewById(R.id.loginButton)
        forgotPasswordButton = findViewById(R.id.forgotPassButton)

        loginButton.setOnClickListener {
            handleLogin()
        }

        forgotPasswordButton.setOnClickListener {
            handleForgotPassword()
        }
    }

    private fun handleLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            emailInput.requestFocus()
            return
        }

        // Standard email validation
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Enter a valid email address"
            emailInput.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            passwordInput.requestFocus()
            return
        }

        // For now, if fields are filled and email is valid, proceed to OTP verification
        Toast.makeText(this, "Credentials entered. Proceeding to OTP verification.", Toast.LENGTH_SHORT).show()
        // It's generally better to clear fields *after* a successful operation or if explicitly navigating away permanently.
        // Let's hold off on clearing fields here for now, in case OTP is cancelled and user returns.

        val intent = Intent(this, verificationActivity::class.java)
        intent.putExtra(EXTRA_VERIFICATION_REASON, REASON_LOGIN_VERIFICATION)
        // Start verificationActivity expecting a result
        verificationLauncher.launch(intent)
    }

    private fun handleForgotPassword() {
        // Clear fields before navigating (optional, but can be good UX)
        // clearInputFields() // You can decide if you want to clear fields here

        val intent = Intent(this, verificationActivity::class.java)
        intent.putExtra(EXTRA_VERIFICATION_REASON, REASON_FORGOT_PASSWORD)
        // For "Forgot Password", we might not need a result back in the same way as login,
        // as its flow leads to resetPassword and then typically back to login.
        startActivity(intent)
    }

    private fun clearInputFields() {
        emailInput.text.clear()
        passwordInput.text.clear()
        emailInput.error = null
        passwordInput.error = null
    }

    // This function is not directly used for navigation in the current flow but good to keep
    // private fun isValidCredentials(email: String, password: String): Boolean {
    // return email == "test@example.com" && password == "password123"
    // }

    // --- Lifecycle methods (optional) ---
}