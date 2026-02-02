package com.example.autopayroll_mobile.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.autopayroll_mobile.composableUI.VerificationScreen
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.VerificationState
import com.example.autopayroll_mobile.viewmodel.VerificationViewModel

class VerificationActivity : ComponentActivity() {

    private val verificationViewModel: VerificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Only retrieve the email
        val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""

        if (email.isNotEmpty()) {
            verificationViewModel.setEmail(email)
        } else {
            Toast.makeText(this, "Error: Email not provided", Toast.LENGTH_SHORT).show()
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

                when (val state = verificationState) {
                    is VerificationState.Success -> {
                        Toast.makeText(this, "Verified! Please create a new password.", Toast.LENGTH_SHORT).show()

                        // Proceed directly to ResetPassword
                        val intent = Intent(this, ResetPassword::class.java)
                        intent.putExtra("EXTRA_EMAIL", email) // Ensure "email" variable contains the user's email
                        startActivity(intent)
                        finish()

                        verificationViewModel.resetState()
                    }
                    is VerificationState.Error -> {
                        Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                        verificationViewModel.resetState()
                    }
                    is VerificationState.Loading -> {
                    }
                    else -> {}
                }
            }
        }
    }
}