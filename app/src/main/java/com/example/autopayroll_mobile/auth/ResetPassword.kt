package com.example.autopayroll_mobile.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.autopayroll_mobile.composableUI.ResetPasswordScreen
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme

class ResetPassword : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutoPayrollMobileTheme {
                ResetPasswordScreen(
                    onConfirmClick = { newPassword, confirmPassword ->
                        handleConfirmNewPassword(newPassword, confirmPassword)
                    },
                    onCancelClick = {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    private fun handleConfirmNewPassword(newPassword: String, confirmedPassword: String) {
        if (newPassword.isEmpty()) {
            Toast.makeText(this, "New password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (confirmedPassword.isEmpty()) {
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmedPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Password reset successfully (simulated)", Toast.LENGTH_LONG).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
