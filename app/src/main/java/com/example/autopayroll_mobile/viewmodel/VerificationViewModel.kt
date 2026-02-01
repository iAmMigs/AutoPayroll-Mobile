package com.example.autopayroll_mobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.auth.OtpErrorResponse
import com.example.autopayroll_mobile.data.auth.OtpRequest
import com.example.autopayroll_mobile.data.auth.OtpVerifyRequest
import com.example.autopayroll_mobile.network.PublicApiClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class VerificationViewModel : ViewModel() {

    private val apiService = PublicApiClient.getService()
    private var userEmail: String = ""

    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState = _verificationState.asStateFlow()

    fun setEmail(email: String) {
        this.userEmail = email
    }

    fun verifyOtp(otp: String) {
        if (userEmail.isBlank()) {
            _verificationState.value = VerificationState.Error("Email not found. Please try again.")
            return
        }
        if (otp.length != 6) {
            _verificationState.value = VerificationState.Error("OTP must be 6 digits")
            return
        }

        viewModelScope.launch {
            _verificationState.value = VerificationState.Loading
            try {
                val response = apiService.verifyOtp(OtpVerifyRequest(email = userEmail, otp = otp))
                if (response.success) {
                    _verificationState.value = VerificationState.Success
                } else {
                    _verificationState.value = VerificationState.Error(response.message)
                }
            } catch (e: Exception) {
                val errorMsg = parseOtpError(e)
                _verificationState.value = VerificationState.Error(errorMsg)
            }
        }
    }

    fun resendCode() {
        if (userEmail.isBlank()) return

        viewModelScope.launch {
            try {
                // Call requestOtp again to resend
                apiService.requestOtp(OtpRequest(email = userEmail))
                Log.d("VerificationVM", "Resend OTP successful")
            } catch (e: Exception) {
                Log.e("VerificationVM", "Resend failed", e)
            }
        }
    }

    private fun parseOtpError(e: Exception): String {
        if (e is HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            return try {
                val err = Gson().fromJson(errorBody, OtpErrorResponse::class.java)
                err.otp ?: err.message ?: "Verification failed"
            } catch (jsonEx: Exception) {
                "Server error: ${e.code()}"
            }
        }
        return "Network error"
    }
}

sealed class VerificationState {
    object Idle : VerificationState()
    object Loading : VerificationState()
    object Success : VerificationState()
    data class Error(val message: String) : VerificationState()
}