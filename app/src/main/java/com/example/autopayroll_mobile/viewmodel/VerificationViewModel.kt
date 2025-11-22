package com.example.autopayroll_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VerificationViewModel : ViewModel() {

    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState = _verificationState.asStateFlow()

    fun verifyOtp(otp: String) {
        viewModelScope.launch {
            _verificationState.value = VerificationState.Loading
            if (otp.length == 6) { // Simplified validation
                _verificationState.value = VerificationState.Success
            } else {
                _verificationState.value = VerificationState.Error("Invalid OTP")
            }
        }
    }

    fun resendCode() {
        // Handle resend code logic
    }
}

sealed class VerificationState {
    object Idle : VerificationState()
    object Loading : VerificationState()
    object Success : VerificationState()
    data class Error(val message: String) : VerificationState()
}
