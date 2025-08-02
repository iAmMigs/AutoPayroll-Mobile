package com.example.autopayroll_mobile.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.auth.loginActivity.Companion.EXTRA_VERIFICATION_REASON
import com.example.autopayroll_mobile.auth.loginActivity.Companion.REASON_FORGOT_PASSWORD

class verificationActivity : AppCompatActivity() {

    private var verificationReason: String? = null
    private lateinit var subtitleTextView: TextView
    private lateinit var otpBoxes: List<EditText> // Store OTP boxes in a list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verification)

        verificationReason = intent.getStringExtra(loginActivity.EXTRA_VERIFICATION_REASON)

        subtitleTextView = findViewById(R.id.subtitleTextView)
        val verifyButton: Button = findViewById(R.id.verifyButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)
        val resendCodeTextView: TextView = findViewById(R.id.resendCodeTextView)

        // Initialize OTP boxes
        otpBoxes = listOf(
            findViewById(R.id.otpBox1),
            findViewById(R.id.otpBox2),
            findViewById(R.id.otpBox3),
            findViewById(R.id.otpBox4),
            findViewById(R.id.otpBox5),
            findViewById(R.id.otpBox6)
        )

        setupOtpInputListeners()

        // --- Your existing subtitle logic can go here ---
        // val userEmail = intent.getStringExtra("USER_EMAIL_FOR_VERIFICATION")
        // subtitleTextView.text = "We've sent a code to $userEmail"

        verifyButton.setOnClickListener {
            val otpCode = getOtpCode()
            val isCodeValid = otpCode.length == 6 // Simple validation

            if (isCodeValid) {
                handleSuccessfulVerification()

            } else {
                Toast.makeText(this, "Invalid OTP code. Please enter all 6 digits.", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            finish()
        }

        resendCodeTextView.setOnClickListener {
            Toast.makeText(this, "Resending code...", Toast.LENGTH_SHORT).show()
            // --- Implement your actual resend code logic here ---
            // For example, clear OTP boxes and request focus on the first one
            otpBoxes.forEach { it.text.clear() }
            otpBoxes.firstOrNull()?.requestFocus()
        }

        // Request focus on the first OTP box when the activity starts
        otpBoxes.firstOrNull()?.requestFocus()
    }

    private fun setupOtpInputListeners() {
        for (i in otpBoxes.indices) {
            val currentOtpBox = otpBoxes[i]
            val nextOtpBox = if (i < otpBoxes.size - 1) otpBoxes[i + 1] else null
            val prevOtpBox = if (i > 0) otpBoxes[i - 1] else null

            currentOtpBox.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && nextOtpBox != null) {
                        nextOtpBox.requestFocus()
                    } else if (s?.length == 0 && prevOtpBox != null) {
                        // This case is handled by onKeyListener for backspace
                    }
                }
            })

            // Handle backspace key press to move focus to the previous box
            currentOtpBox.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (currentOtpBox.text.isEmpty() && prevOtpBox != null) {
                        prevOtpBox.requestFocus()
                        prevOtpBox.text.clear() // Or select all: prevOtpBox.selectAll()
                        return@OnKeyListener true
                    }
                }
                false
            })
        }
    }

    private fun getOtpCode(): String {
        return otpBoxes.joinToString("") { it.text.toString() }
    }

    private fun handleSuccessfulVerification() {
        // !! IMPORTANT LOGGING !!
        android.util.Log.d("VerificationActivity", "handleSuccessfulVerification called. Reason: '$verificationReason'")

        when (verificationReason) {
            loginActivity.REASON_FORGOT_PASSWORD -> {
                Toast.makeText(this, "OTP Verified.", Toast.LENGTH_LONG).show()
                android.util.Log.d("VerificationActivity", "Branch: REASON_FORGOT_PASSWORD - Preparing to start resetPassword")
                try {
                    val intent = Intent(this, resetPassword::class.java)
                    startActivity(intent)
                    finish() // Finish verification activity so user can't go back to it
                    android.util.Log.d("VerificationActivity", "Successfully called startActivity for resetPassword and finish from FORGOT_PASSWORD branch.")
                } catch (e: Exception) {
                    android.util.Log.e("VerificationActivity", "Error starting resetPassword from FORGOT_PASSWORD branch", e)
                    Toast.makeText(this, "Error starting Reset Password screen: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            loginActivity.REASON_LOGIN_VERIFICATION -> {
                Toast.makeText(this, "OTP Verified. Navigating to Dashboard (Not Implemented Yet).", Toast.LENGTH_LONG).show()
                android.util.Log.d("VerificationActivity", "Branch: REASON_LOGIN_VERIFICATION - Preparing to go to Dashboard (placeholder)")
                // TODO: Implement navigation to your DashboardActivity when it's ready
                // For example:
                // val intent = Intent(this, DashboardActivity::class.java)
                // startActivity(intent)
                // finish()
                // For now, you might just finish or stay, or show a more specific Toast
                // finish() // Optional: finish verification if login OTP is successful
            }
            else -> {
                Toast.makeText(this, "OTP Verified. Action undefined. Reason: '$verificationReason'", Toast.LENGTH_LONG).show()
                android.util.Log.w("VerificationActivity", "Branch: else. Actual reason: '$verificationReason'")
                // finish() // Decide if you want to finish or stay on this page
            }
        }
    }
}