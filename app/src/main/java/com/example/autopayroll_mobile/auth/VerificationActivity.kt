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
import com.example.autopayroll_mobile.mainApp.NavbarActivity
import com.example.autopayroll_mobile.composableUI.VerificationScreen
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.VerificationViewModel
import com.example.autopayroll_mobile.viewmodel.VerificationState
import com.example.autopayroll_mobile.auth.ResetPassword

class VerificationActivity : ComponentActivity() {

    private val verificationViewModel: VerificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the reason for verification (Login vs Forgot Password)
        val verificationReason = intent.getStringExtra(LoginActivity.EXTRA_VERIFICATION_REASON)

        // Retrieve the email passed from the previous activity
        val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""

        // Pass the email to the ViewModel so it can be used for the API call
        if (email.isNotEmpty()) {
            verificationViewModel.setEmail(email)
        } else {
            Toast.makeText(this, "Error: Email not found.", Toast.LENGTH_SHORT).show()
        }

        setContent {
            AutoPayrollMobileTheme {
                val verificationState by verificationViewModel.verificationState.collectAsState()

                VerificationScreen(
                    onVerify = { otp ->
                        verificationViewModel.verifyOtp(otp)
                    },
                    onCancel = {
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

                // Observe the state from the ViewModel
                when (val state = verificationState) {
                    is VerificationState.Success -> {
                        handleSuccessfulVerification(verificationReason, email)
                    }
                    is VerificationState.Error -> {
                        Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    }
                    // Optional: Handle Loading state if you want to show a spinner
                    is VerificationState.Loading -> {
                        // You can add a loading overlay here if desired
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleSuccessfulVerification(verificationReason: String?, email: String) {
        when (verificationReason) {
            LoginActivity.REASON_FORGOT_PASSWORD -> {
                Toast.makeText(this, "OTP Verified. Proceeding to reset password.", Toast.LENGTH_LONG).show()
                val intent = Intent(this, ResetPassword::class.java)
                // Pass the email forward to the ResetPassword activity
                intent.putExtra("EXTRA_EMAIL", email)
                startActivity(intent)
                finish()
            }

//            ----- No need unless implement verification login -----
//            LoginActivity.REASON_LOGIN_VERIFICATION -> {
//                Toast.makeText(this, "OTP Verified. Navigating to Dashboard.", Toast.LENGTH_LONG).show()
//                val intent = Intent(this, NavbarActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//                setResult(Activity.RESULT_OK)
//                finish()
//            }


            else -> {
                Toast.makeText(this, "OTP Verified. Action undefined.", Toast.LENGTH_LONG).show()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }
}