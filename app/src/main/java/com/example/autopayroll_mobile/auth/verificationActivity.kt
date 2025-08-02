package com.example.autopayroll_mobile.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.autopayroll_mobile.R // Import your R file
// Import your future ChangePasswordActivity (you'll need to create this)
// import com.example.autopayroll_mobile.auth.ChangePasswordActivity
// Import your future MainActivity or HomeActivity (you'll need to create this)
// import com.example.autopayroll_mobile.main.MainActivity


class verificationActivity : AppCompatActivity() {

    private var verificationReason: String? = null
    private lateinit var otpBox1: EditText
    // ... declare other OTP boxes
    private lateinit var subtitleTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verification) // Make sure this matches your XML file name

        // Retrieve the verification reason from the intent
        verificationReason = intent.getStringExtra(loginActivity.EXTRA_VERIFICATION_REASON)

        subtitleTextView = findViewById(R.id.subtitleTextView)
        otpBox1 = findViewById(R.id.otpBox1)
        // ... findViewById for other OTP boxes (otpBox2 to otpBox6)
        val verifyButton: Button = findViewById(R.id.verifyButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)
        val resendCodeTextView: TextView = findViewById(R.id.resendCodeTextView)

        // Optionally, update UI based on the reason (e.g., subtitle)
        // For example, if the email is dynamic:
        // val userEmail = intent.getStringExtra("USER_EMAIL_FOR_VERIFICATION") // You'd pass this from loginActivity
        // subtitleTextView.text = "We've sent a code to $userEmail"
        // For now, we'll use the static text from your XML.

        verifyButton.setOnClickListener {
            val otpCode = getOtpCode() // Implement this function to get the code from OTP boxes

            // --- Your actual OTP verification logic will go here ---
            // For this example, let's assume verification is successful if a code is entered
            val isCodeValid = otpCode.length == 6 // Replace with real validation

            if (isCodeValid) {
                handleSuccessfulVerification()
            } else {
                Toast.makeText(this, "Invalid OTP code", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            // Navigate back or to the login screen
            finish() // Finishes current activity and goes back to the previous one in the stack
            // Or:
            // val intent = Intent(this, loginActivity::class.java)
            // intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // startActivity(intent)
        }

        resendCodeTextView.setOnClickListener {
            // --- Implement your resend code logic here ---
            Toast.makeText(this, "Resending code...", Toast.LENGTH_SHORT).show()
        }

        // --- Add logic for OTP input handling (auto-focus next, backspace) ---
        // This can be complex, you might want to look for libraries or a more detailed implementation
        // For a basic example, see notes below.
    }

    private fun getOtpCode(): String {
        // Concatenate the text from all OTP EditTexts
        return findViewById<EditText>(R.id.otpBox1).text.toString() +
                findViewById<EditText>(R.id.otpBox2).text.toString() +
                findViewById<EditText>(R.id.otpBox3).text.toString() +
                findViewById<EditText>(R.id.otpBox4).text.toString() +
                findViewById<EditText>(R.id.otpBox5).text.toString() +
                findViewById<EditText>(R.id.otpBox6).text.toString()
    }

    private fun handleSuccessfulVerification() {
        when (verificationReason) {
            loginActivity.REASON_FORGOT_PASSWORD -> {
                Toast.makeText(this, "OTP Verified. Redirecting to change password.", Toast.LENGTH_LONG).show()
                // Navigate to ChangePasswordActivity
                // val intent = Intent(this, ChangePasswordActivity::class.java)
                // startActivity(intent)
                // finish() // Optional: finish this activity so user can't go back to it
            }
            loginActivity.REASON_LOGIN_VERIFICATION -> {
                Toast.makeText(this, "OTP Verified. Logging you in.", Toast.LENGTH_LONG).show()
                // Navigate to your main app screen (e.g., MainActivity or HomeActivity)
                // val intent = Intent(this, MainActivity::class.java)
                // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear back stack
                // startActivity(intent)
                // finish()
            }
            else -> {
                // Default behavior or error handling if the reason is unknown
                Toast.makeText(this, "Verification successful, but action is undefined.", Toast.LENGTH_LONG).show()
                // Potentially navigate to a default screen or back to login
                // val intent = Intent(this, loginActivity::class.java)
                // startActivity(intent)
                // finish()
            }
        }
    }
}