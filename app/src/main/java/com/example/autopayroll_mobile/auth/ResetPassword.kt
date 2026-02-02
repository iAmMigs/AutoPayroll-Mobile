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

class ResetPassword : ComponentActivity() {

    private val viewModel: ResetPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""
        if (email.isEmpty()) Toast.makeText(this, "Error: Email missing", Toast.LENGTH_SHORT).show()

        setContent {
            AutoPayrollMobileTheme {
                val state by viewModel.resetState.collectAsState()

                ResetPasswordScreen(
                    isLoading = state is ResetPasswordState.Loading, // Pass loading state
                    onConfirmClick = { newPass, confirmPass ->
                        viewModel.submitNewPassword(email, newPass, confirmPass)
                    },
                    onCancelClick = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )

                when (val result = state) {
                    is ResetPasswordState.Success -> {
                        Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    is ResetPasswordState.Error -> {
                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                        viewModel.resetStateToIdle()
                    }
                    else -> {}
                }
            }
        }
    }
}