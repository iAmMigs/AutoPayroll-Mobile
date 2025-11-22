package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.model.ApiErrorResponse
import com.example.autopayroll_mobile.data.model.ChangePasswordRequest
import com.example.autopayroll_mobile.data.model.ValidationErrorResponse
import com.example.autopayroll_mobile.network.ApiClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ChangePasswordUiState(
    val currentPasswordInput: String = "",
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

sealed class ChangePasswordNavigationEvent {
    object NavigateBack : ChangePasswordNavigationEvent()
}

class ChangePasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiClient.getClient(application.applicationContext)

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<ChangePasswordNavigationEvent?>(null)
    val navigationEvent: StateFlow<ChangePasswordNavigationEvent?> = _navigationEvent.asStateFlow()

    fun onCurrentPasswordChange(value: String) {
        _uiState.update { it.copy(currentPasswordInput = value) }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPasswordInput = value) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPasswordInput = value) }
    }

    /**
     * Performs client-side validation and initiates the API call.
     */
    fun validateAndSubmitPasswordChange() {
        val state = _uiState.value

        // Basic client-side validation
        if (state.currentPasswordInput.isBlank() || state.newPasswordInput.isBlank() || state.confirmPasswordInput.isBlank()) {
            _uiState.update { it.copy(error = "All fields are required.") }
            return
        }
        if (state.newPasswordInput.length < 8) {
            _uiState.update { it.copy(error = "New password must be at least 8 characters.") }
            return
        }
        if (state.newPasswordInput != state.confirmPasswordInput) {
            _uiState.update { it.copy(error = "New password and confirmation password do not match.") }
            return
        }
        if (state.currentPasswordInput == state.newPasswordInput) {
            _uiState.update { it.copy(error = "New password cannot be the same as the current password.") }
            return
        }

        // Validation passed, proceed to API call
        submitPasswordChangeApi()
    }

    private fun submitPasswordChangeApi() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }

        viewModelScope.launch {
            try {
                val request = ChangePasswordRequest(
                    currentPassword = state.currentPasswordInput,
                    newPassword = state.newPasswordInput,
                    confirmPassword = state.confirmPasswordInput
                )

                val response = apiService.resetPassword(request)

                if (response.success) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = response.message ?: "Password reset successfully"
                        )
                    }
                    // Wait a moment then navigate back
                    kotlinx.coroutines.delay(1000)
                    _navigationEvent.value = ChangePasswordNavigationEvent.NavigateBack
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.message ?: "Reset failed. Please try again."
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e("ChangePasswordVM", "API call failed", e)

                var errorMessage = "An unexpected error occurred: ${e.message}"

                if (e is HttpException) {
                    val errorBodyString = e.response()?.errorBody()?.string()
                    when (e.code()) {
                        422 -> { // Validation error from Laravel (e.g., password not confirmed, min length)
                            try {
                                val validationResponse = Gson().fromJson(errorBodyString, ValidationErrorResponse::class.java)
                                val firstError = validationResponse.errors?.values?.firstOrNull()?.firstOrNull()
                                errorMessage = firstError ?: validationResponse.message ?: "Validation failed."
                            } catch (jsonError: Exception) {
                                Log.e("ChangePasswordVM", "Failed to parse 422 error body", jsonError)
                            }
                        }
                        400 -> { // Custom error from PHP like "Current password is incorrect"
                            try {
                                val errorResponse = Gson().fromJson(errorBodyString, ApiErrorResponse::class.java)
                                errorMessage = errorResponse.message
                            } catch (jsonError: Exception) {
                                Log.e("ChangePasswordVM", "Failed to parse 400 error body", jsonError)
                            }
                        }
                        else -> {
                            errorMessage = "Server error (${e.code()})."
                        }
                    }
                }

                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }

    fun clearInputs() {
        _uiState.update { it.copy(
            currentPasswordInput = "",
            newPasswordInput = "",
            confirmPasswordInput = ""
        ) }
    }
}