package com.example.autopayroll_mobile.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.autopayroll_mobile.composableUI.VerificationScreen
import com.example.autopayroll_mobile.mainApp.NavbarActivity
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.VerificationState
import com.example.autopayroll_mobile.viewmodel.VerificationViewModel

class VerificationActivity : ComponentActivity() {

    private val verificationViewModel: VerificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Retrieve data passed from ForgotPasswordActivity or LoginActivity
        val verificationReason = intent.getStringExtra(LoginActivity.EXTRA_VERIFICATION_REASON)
        val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""

        // 2. Pass the email to the ViewModel immediately
        if (email.isNotEmpty()) {
            verificationViewModel.setEmail(email)
        } else {
            // Fallback for testing or error cases
            Toast.makeText(this, "Error: Email not provided", Toast.LENGTH_SHORT).show()
        }

        setContent {
            AutoPayrollMobileTheme {
                val verificationState by verificationViewModel.verificationState.collectAsState()

                VerificationScreen(
                    onVerify = { otp ->
                        // Call the API via ViewModel
                        verificationViewModel.verifyOtp(otp)
                    },
                    onCancel = {
                        // Go back to Login
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()
                    },
                    onResend = {
                        verificationViewModel.resendCode()
                        Toast.makeText(this, "Resending code...", Toast.LENGTH_SHORT).show()
                    }
                )

                // 3. Observe State for Navigation
                when (val state = verificationState) {
                    is VerificationState.Success -> {
                        // Pass the email to the next function
                        handleSuccessfulVerification(verificationReason, email)
                        // Reset state to avoid repeated navigation on recomposition
                        verificationViewModel.resetState()
                    }
                    is VerificationState.Error -> {
                        Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                        verificationViewModel.resetState() // Optional: clear error after showing
                    }
                    is VerificationState.Loading -> {
                        // Loading handled in UI or you can show a dialog here
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleSuccessfulVerification(verificationReason: String?, email: String) {
        when (verificationReason) {
            LoginActivity.REASON_FORGOT_PASSWORD -> {
                Toast.makeText(this, "Verified! Please create a new password.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ResetPassword::class.java)
                // IMPORTANT: Pass the email to the Reset Password screen
                intent.putExtra("EXTRA_EMAIL", email)
                startActivity(intent)
                finish()
            }
            LoginActivity.REASON_LOGIN_VERIFICATION -> {
                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, NavbarActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                setResult(Activity.RESULT_OK)
                finish()
            }
            else -> {
                Toast.makeText(this, "Verified", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }
}