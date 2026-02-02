package com.example.autopayroll_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.auth.ResetPasswordRequest
import com.example.autopayroll_mobile.data.model.ApiErrorResponse
import com.example.autopayroll_mobile.network.PublicApiClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ResetPasswordViewModel : ViewModel() {

    private val apiService = PublicApiClient.getService()

    private val _resetState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val resetState = _resetState.asStateFlow()

    fun submitNewPassword(email: String, pass: String, confirmPass: String) {
        if (email.isBlank()) {
            _resetState.value = ResetPasswordState.Error("Email is missing. Please restart.")
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
                // Now sends 'password_confirmation' correctly
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
                        try {
                            // 1. READ THE REAL ERROR BODY
                            val errorBody = e.response()?.errorBody()?.string()
                            if (errorBody != null) {
                                "Server Error: $errorBody"
                            } else {
                                "Server Error: ${e.code()} ${e.message()}"
                            }
                        } catch (jsonException: Exception) {
                            "Server error: ${e.code()} (Could not parse error)"
                        }
                    }
                    else -> "An unexpected error occurred: ${e.message}"
                }
                _resetState.value = ResetPasswordState.Error(errorMsg)
            }
        }
    }

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