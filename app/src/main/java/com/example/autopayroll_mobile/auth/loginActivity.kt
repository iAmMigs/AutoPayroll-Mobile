package com.example.autopayroll_mobile.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button // Assuming your forgotPassButton is a standard Button
import androidx.appcompat.app.AppCompatActivity
import com.example.autopayroll_mobile.R // Import your R file

class loginActivity : AppCompatActivity() {

    // Define a constant for the extra key
    companion object {
        const val EXTRA_VERIFICATION_REASON = "com.example.autopayroll_mobile.auth.VERIFICATION_REASON"
        const val REASON_FORGOT_PASSWORD = "FORGOT_PASSWORD"
        const val REASON_LOGIN_VERIFICATION = "LOGIN_VERIFICATION" // Example for another potential flow
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page) // Make sure this matches your XML file name

        val forgotPasswordButton: Button = findViewById(R.id.forgotPassButton)
        val loginButton: Button = findViewById(R.id.loginButton) // Assuming you'll need this too

        forgotPasswordButton.setOnClickListener {
            val intent = Intent(this, verificationActivity::class.java)
            // Pass an extra to indicate the reason for verification
            intent.putExtra(EXTRA_VERIFICATION_REASON, REASON_FORGOT_PASSWORD)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            // Handle regular login
            // If login requires verification (e.g., 2FA), you might also go to verificationActivity:
            // val intent = Intent(this, verificationActivity::class.java)
            // intent.putExtra(EXTRA_VERIFICATION_REASON, REASON_LOGIN_VERIFICATION)
            // startActivity(intent)
            // For now, let's assume direct login or other logic
        }

        // ... other login logic
    }
}