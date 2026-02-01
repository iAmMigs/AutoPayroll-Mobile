package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.auth.OtpVerifyRequest
import com.example.autopayroll_mobile.network.PublicApiClient
import com.google.gson.Gson
import com.example.autopayroll_mobile.data.model.ApiErrorResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class VerificationViewModel(application: Application) : AndroidViewModel(application) {

    // Use PublicApiClient because user is not logged in yet
    private val apiService = PublicApiClient.getService()
    private var userEmail: String = ""

    private val _verificationState = MutableStateFlow<VerificationState>(VerificationState.Idle)
    val verificationState = _verificationState.asStateFlow()

    fun setEmail(email: String) {
        this.userEmail = email
    }

    fun verifyOtp(otp: String) {
        if (otp.length != 6) {
            _verificationState.value = VerificationState.Error("Please enter a 6-digit code")
            return
        }
        if (userEmail.isBlank()) {
            _verificationState.value = VerificationState.Error("Email missing. Please restart.")
            return
        }

        viewModelScope.launch {
            _verificationState.value = VerificationState.Loading
            try {
                // Call API: /api/verify-otp
                val response = apiService.verifyOtp(OtpVerifyRequest(userEmail, otp))

                if (response.success) {
                    _verificationState.value = VerificationState.Success
                } else {
                    _verificationState.value = VerificationState.Error(response.message)
                }
            } catch (e: Exception) {
                val errorMsg = parseError(e)
                _verificationState.value = VerificationState.Error(errorMsg)
            }
        }
    }

    fun resendCode() {
        if (userEmail.isBlank()) return
        viewModelScope.launch {
            try {
                // Reuse the requestOtp endpoint
                // Note: You might need a simple OtpRequest(email) data class
                // apiService.requestOtp(OtpRequest(userEmail))
            } catch (e: Exception) {
                // Handle error silently or show toast
            }
        }
    }

    fun resetState() {
        _verificationState.value = VerificationState.Idle
    }

    private fun parseError(e: Exception): String {
        return if (e is HttpException) {
            try {
                val body = e.response()?.errorBody()?.string()
                val error = Gson().fromJson(body, ApiErrorResponse::class.java)
                error.message
            } catch (e: Exception) {
                "Verification failed: ${e.message}"
            }
        } else {
            "Network error. Please check your connection."
        }
    }
}

sealed class VerificationState {
    object Idle : VerificationState()
    object Loading : VerificationState()
    object Success : VerificationState()
    data class Error(val message: String) : VerificationState()
}