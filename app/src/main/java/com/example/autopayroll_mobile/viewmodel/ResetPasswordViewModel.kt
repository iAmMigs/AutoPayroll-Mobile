package com.example.autopayroll_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.auth.ResetPasswordRequest
import com.example.autopayroll_mobile.network.PublicApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ResetPasswordViewModel : ViewModel() {

    private val apiService = PublicApiClient.getService()

    // State to track the UI
    private val _resetState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val resetState = _resetState.asStateFlow()

    fun submitNewPassword(email: String, pass: String, confirmPass: String) {
        if (email.isBlank()) {
            _resetState.value = ResetPasswordState.Error("Email is missing. Please restart the process.")
            return
        }

        if (pass.length < 8) {
            _resetState.value = ResetPasswordState.Error("Password must be at least 8 characters.")
            return
        }

        if (pass != confirmPass) {
            _resetState.value = ResetPasswordState.Error("Passwords do not match.")
            return
        }

        viewModelScope.launch {
            _resetState.value = ResetPasswordState.Loading
            try {
                // Laravel 'confirmed' validation expects 'password' and 'password_confirmation'
                val request = ResetPasswordRequest(
                    email = email,
                    password = pass,
                    passwordConfirmation = confirmPass
                )

                val response = apiService.changePassword(request)

                if (response.success) {
                    _resetState.value = ResetPasswordState.Success(response.message)
                } else {
                    _resetState.value = ResetPasswordState.Error(response.message)
                }

            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is IOException -> "Network error. Please check your connection."
                    is HttpException -> {
                        // Attempt to parse Laravel error JSON if needed, otherwise generic
                        "Server error: ${e.message()}"
                    }
                    else -> "An unexpected error occurred."
                }
                _resetState.value = ResetPasswordState.Error(errorMsg)
            }
        }
    }

    // Helper to reset state if user navigates away or retries
    fun resetStateToIdle() {
        _resetState.value = ResetPasswordState.Idle
    }
}

sealed class ResetPasswordState {
    object Idle : ResetPasswordState()
    object Loading : ResetPasswordState()
    data class Success(val message: String) : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}