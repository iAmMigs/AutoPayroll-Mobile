package com.example.autopayroll_mobile.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.autopayroll_mobile.composableUI.ResetPasswordScreen
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.ResetPasswordState
import com.example.autopayroll_mobile.viewmodel.ResetPasswordViewModel

// DO NOT ADD DATA CLASSES HERE. THEY ARE IN ResetPasswordModels.kt

class ResetPassword : ComponentActivity() {

    private val viewModel: ResetPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Retrieve the email passed from VerificationActivity
        val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""

        if (email.isEmpty()) {
            Toast.makeText(this, "Error: Email not found.", Toast.LENGTH_SHORT).show()
        }

        setContent {
            AutoPayrollMobileTheme {
                val state by viewModel.resetState.collectAsState()

                // 2. Render UI
                ResetPasswordScreen(
                    onConfirmClick = { newPass, confirmPass ->
                        viewModel.submitNewPassword(email, newPass, confirmPass)
                    },
                    onCancelClick = {
                        val intent = Intent(this@ResetPassword, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()
                    }
                )

                // 3. Handle State Changes
                when (val result = state) {
                    is ResetPasswordState.Success -> {
                        Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    is ResetPasswordState.Error -> {
                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                        viewModel.resetStateToIdle()
                    }
                    is ResetPasswordState.Loading -> {
                        // Loading handled in UI
                    }
                    else -> {}
                }
            }
        }
    }
}