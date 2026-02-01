package com.example.autopayroll_mobile.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.autopayroll_mobile.composableUI.ForgotPasswordScreen
import com.example.autopayroll_mobile.mainApp.BaseActivity
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.ForgotPasswordViewModel

class ForgotPasswordActivity : BaseActivity() {

    private val forgotPasswordViewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AutoPayrollMobileTheme {
                ForgotPasswordScreen(
                    viewModel = forgotPasswordViewModel,
                    onNavigateBack = { finish() } // Close activity to go back to Login
                )
            }
        }

        // Observe success state (e.g., Email sent successfully)
        forgotPasswordViewModel.submitSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "OTP sent to your email", Toast.LENGTH_LONG).show()

                val intent = Intent(this, VerificationActivity::class.java)
                intent.putExtra(LoginActivity.EXTRA_VERIFICATION_REASON, LoginActivity.REASON_FORGOT_PASSWORD)

                // IMPORTANT: Pass the email to VerificationActivity
                intent.putExtra("EXTRA_EMAIL", forgotPasswordViewModel.email.value)

                startActivity(intent)
                finish()
            }
        }

        // Observe errors
        forgotPasswordViewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                forgotPasswordViewModel.onErrorShown()
            }
        }
    }
}