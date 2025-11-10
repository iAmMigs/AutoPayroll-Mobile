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
        val verificationReason = intent.getStringExtra(LoginActivity.EXTRA_VERIFICATION_REASON)

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
                        handleSuccessfulVerification(verificationReason)
                    }
                    is VerificationState.Error -> {
                        Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun handleSuccessfulVerification(verificationReason: String?) {
        when (verificationReason) {
            LoginActivity.REASON_FORGOT_PASSWORD -> {
                Toast.makeText(this, "OTP Verified. Proceeding to reset password.", Toast.LENGTH_LONG).show()
                val intent = Intent(this, ResetPassword::class.java)
                startActivity(intent)
                finish()
            }
            LoginActivity.REASON_LOGIN_VERIFICATION -> {
                Toast.makeText(this, "OTP Verified. Navigating to Dashboard.", Toast.LENGTH_LONG).show()
                val intent = Intent(this, NavbarActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                setResult(Activity.RESULT_OK)
                finish()
            }
            else -> {
                Toast.makeText(this, "OTP Verified. Action undefined.", Toast.LENGTH_LONG).show()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }
}
