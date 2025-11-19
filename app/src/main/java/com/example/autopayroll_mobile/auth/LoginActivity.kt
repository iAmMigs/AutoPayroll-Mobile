package com.example.autopayroll_mobile.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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

        loginViewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                // Show the toast with the exact message from the server
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                loginViewModel.onErrorShown()
            }
        }
    }
}