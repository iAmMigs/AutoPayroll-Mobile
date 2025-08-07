package com.example.autopayroll_mobile.auth

import android.app.Activity // Required for Activity.RESULT_OK
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
import com.example.autopayroll_mobile.NavbarActivity // Make sure this import is correct
import com.example.autopayroll_mobile.R
// It's good practice to import the companion object members directly if you use them often
// import com.example.autopayroll_mobile.auth.loginActivity.Companion.EXTRA_VERIFICATION_REASON
// import com.example.autopayroll_mobile.auth.loginActivity.Companion.REASON_FORGOT_PASSWORD
// import com.example.autopayroll_mobile.auth.loginActivity.Companion.REASON_LOGIN_VERIFICATION

class VerificationActivity : AppCompatActivity() {

    private var verificationReason: String? = null
    private lateinit var subtitleTextView: TextView
    private lateinit var otpBoxes: List<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.verification)

        verificationReason = intent.getStringExtra(LoginActivity.EXTRA_VERIFICATION_REASON)

        subtitleTextView = findViewById(R.id.subtitleTextView)
        val verifyButton: Button = findViewById(R.id.verifyButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)
        val resendCodeTextView: TextView = findViewById(R.id.resendCodeTextView)

        otpBoxes = listOf(
            findViewById(R.id.otpBox1),
            findViewById(R.id.otpBox2),
            findViewById(R.id.otpBox3),
            findViewById(R.id.otpBox4),
            findViewById(R.id.otpBox5),
            findViewById(R.id.otpBox6)
        )

        setupOtpInputListeners()

        // Update subtitle based on reason (optional but good UX)
        when (verificationReason) {
            LoginActivity.REASON_LOGIN_VERIFICATION ->
                subtitleTextView.text = "Enter the code sent to your email for login."
            LoginActivity.REASON_FORGOT_PASSWORD ->
                subtitleTextView.text = "Enter the code sent to your email to reset your password."
            else ->
                subtitleTextView.text = "Enter the verification code."
        }
        // If you were passing the email:
        // val userEmail = intent.getStringExtra("USER_EMAIL_FOR_VERIFICATION") // If you decide to pass email
        // if (userEmail != null) {
        //    subtitleTextView.text = "We've sent a code to $userEmail"
        // }


        verifyButton.setOnClickListener {
            val otpCode = getOtpCode()
            // For now, any 6-digit code is "valid"
            if (otpCode.length == 6) {
                handleSuccessfulVerification()
            } else {
                Toast.makeText(this, "Invalid OTP code. Please enter all 6 digits.", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            // When cancelling, set the result to CANCELED and finish
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        resendCodeTextView.setOnClickListener {
            Toast.makeText(this, "Resending code...", Toast.LENGTH_SHORT).show()
            otpBoxes.forEach { it.text.clear() }
            otpBoxes.firstOrNull()?.requestFocus()
        }

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
                    }
                }
            })

            currentOtpBox.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (currentOtpBox.text.isEmpty() && prevOtpBox != null) {
                        prevOtpBox.requestFocus()
                        // No need to clear text here, as it's already empty and focus is moving
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
        android.util.Log.d("VerificationActivity", "handleSuccessfulVerification. Reason: '$verificationReason'")

        when (verificationReason) {
            LoginActivity.REASON_FORGOT_PASSWORD -> {
                Toast.makeText(this, "OTP Verified. Proceeding to reset password.", Toast.LENGTH_LONG).show()
                val intent = Intent(this, ResetPassword::class.java) // Assuming resetPassword activity exists
                startActivity(intent)
                // We don't set RESULT_OK here because the login flow isn't complete.
                // Forgot password flow finishes here and goes to reset, then likely back to login.
                finish() // Finish verification activity
            }
            LoginActivity.REASON_LOGIN_VERIFICATION -> {
                Toast.makeText(this, "OTP Verified. Navigating to Dashboard.", Toast.LENGTH_LONG).show()
                android.util.Log.d("VerificationActivity", "Branch: REASON_LOGIN_VERIFICATION - Starting DashboardActivity")

                val intent = Intent(this, NavbarActivity::class.java)
                // These flags will clear the task stack up to DashboardActivity and make it the new root.
                // This means loginActivity and verificationActivity will be removed from the back stack.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

                // Instead of relying on CLEAR_TASK to finish loginActivity,
                // we'll set a result so loginActivity can finish itself explicitly.
                // However, since Dashboard is now the root of a new task, loginActivity might already be gone.
                // Using setResult is still good practice for the launcher.
                setResult(Activity.RESULT_OK)
                finish() // Finish verificationActivity
            }
            else -> {
                Toast.makeText(this, "OTP Verified. Action undefined. Reason: '$verificationReason'", Toast.LENGTH_LONG).show()
                android.util.Log.w("VerificationActivity", "Branch: else. Actual reason: '$verificationReason'")
                setResult(Activity.RESULT_CANCELED) // Or some other custom result if needed
                finish()
            }
        }
    }
}