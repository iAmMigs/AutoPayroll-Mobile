package com.example.autopayroll_mobile.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast // For showing login success/failure messages
import androidx.appcompat.app.AppCompatActivity
import com.example.autopayroll_mobile.R // Your R file
// Import your DashboardActivity if you have one, otherwise remove or comment out
// import com.example.autopayroll_mobile.main.DashboardActivity

class loginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordButton: Button // Assuming you have a button for this

    companion object {
        // Define your constants for verification reasons
        const val EXTRA_VERIFICATION_REASON = "com.example.autopayroll_mobile.auth.VERIFICATION_REASON"
        const val REASON_FORGOT_PASSWORD = "forgot_password"
        const val REASON_LOGIN_VERIFICATION = "login_verification" // For 2FA or other login OTP flows
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page) // Make sure this is your login layout XML file

        // Initialize views
        emailInput = findViewById(R.id.EmailInput)       // Replace with your actual Email EditText ID
        passwordInput = findViewById(R.id.PassInput)      // Replace with your actual Password EditText ID
        loginButton = findViewById(R.id.loginButton)         // Replace with your actual Login Button ID
        forgotPasswordButton = findViewById(R.id.forgotPassButton) // Replace with your actual Forgot Password Button ID

        // Set up listeners
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

        // Basic validation (you should have more robust validation)
        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            emailInput.requestFocus()
            return
        }
        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            passwordInput.requestFocus()
            return
        }

        // --- Simulate Login Logic ---
        // Replace this with your actual authentication logic (e.g., API call)
        if (isValidCredentials(email, password)) {
            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
            clearInputFields() // Clear fields before navigating

            // Navigate to Dashboard or Main Activity
            // For example:
            // val intent = Intent(this, DashboardActivity::class.java)
            // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clears back stack
            // startActivity(intent)
            // finish() // Finish loginActivity so user can't go back

            // If login requires OTP (2FA):
            // clearInputFields()
            // val intent = Intent(this, verificationActivity::class.java)
            // intent.putExtra(EXTRA_VERIFICATION_REASON, REASON_LOGIN_VERIFICATION)
            // startActivity(intent)
            // // Do NOT finish() here if verificationActivity should return to a logged-in state.

        } else {
            Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_LONG).show()
            // Optionally clear only password or shake animation, etc.
            passwordInput.text.clear()
            passwordInput.requestFocus()
        }
    }

    private fun handleForgotPassword() {
        clearInputFields() // Clear fields before navigating to verification

        val intent = Intent(this, verificationActivity::class.java)
        intent.putExtra(EXTRA_VERIFICATION_REASON, REASON_FORGOT_PASSWORD)
        startActivity(intent)
        // Do not finish loginActivity here, user might want to come back using the back button
        // from verificationActivity if they cancel the OTP process.
    }

    private fun clearInputFields() {
        emailInput.text.clear()
        passwordInput.text.clear()

        // Optional: Clear any error messages that might have been set
        emailInput.error = null
        passwordInput.error = null

        // Optional: Request focus on the first field again
        // emailInput.requestFocus()
    }

    // Dummy validation function - replace with your actual logic
    private fun isValidCredentials(email: String, password: String): Boolean {
        // In a real app, you'd check against a database or API
        // This is just a placeholder for demonstration
        return email == "test@example.com" && password == "password123"
    }

    // --- Lifecycle methods (optional, for logging or other purposes) ---
    override fun onStart() {
        super.onStart()
        // You could also choose to clear fields here if the activity is being restarted
        // and it wasn't just a configuration change, but Method 2 is more targeted.
        // android.util.Log.d("loginActivity", "onStart called")
    }

    override fun onResume() {
        super.onResume()
        // android.util.Log.d("loginActivity", "onResume called")
    }

    override fun onPause() {
        super.onPause()
        // android.util.Log.d("loginActivity", "onPause called")
    }

    override fun onStop() {
        super.onStop()
        // android.util.Log.d("loginActivity", "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        // android.util.Log.d("loginActivity", "onDestroy called")
    }
}