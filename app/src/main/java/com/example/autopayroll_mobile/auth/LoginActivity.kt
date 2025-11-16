package com.example.autopayroll_mobile.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast // <-- 1. ADD THIS IMPORT
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.autopayroll_mobile.composableUI.LoginScreen
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.LoginViewModel

class LoginActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    companion object {
        const val EXTRA_VERIFICATION_REASON = "com.example.autopayroll_mobile.auth.VERIFICATION_REASON"
        const val REASON_FORGOT_PASSWORD = "forgot_password"
        const val REASON_LOGIN_VERIFICATION = "login_verification"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AutoPayrollMobileTheme {
                LoginScreen(loginViewModel = loginViewModel)
            }
        }

        loginViewModel.loginSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                val intent = Intent(this, VerificationActivity::class.java)
                intent.putExtra(EXTRA_VERIFICATION_REASON, REASON_LOGIN_VERIFICATION)
                startActivity(intent)
                finish()
            }
        }

        // --- 2. ADD THIS BLOCK TO OBSERVE AND SHOW ERRORS ---
        loginViewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                // Show the toast with the exact message from the server
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                // Tell the ViewModel the error has been shown
                // This prevents the toast from showing again on rotation
                loginViewModel.onErrorShown()
            }
        }
        // --- END OF NEW BLOCK ---
    }
}