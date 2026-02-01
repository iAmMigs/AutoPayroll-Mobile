package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.auth.OtpRequest
import com.example.autopayroll_mobile.data.model.ApiErrorResponse
import com.example.autopayroll_mobile.network.PublicApiClient
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = PublicApiClient.getService() // Use Public Client (no auth token)

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _submitSuccess = MutableLiveData<Boolean>()
    val submitSuccess: LiveData<Boolean> = _submitSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun submitRequest() {
        val currentEmail = _email.value?.trim() ?: ""

        if (currentEmail.isEmpty()) {
            _errorMessage.value = "Please enter your email address"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = apiService.requestOtp(OtpRequest(email = currentEmail))

                if (response.success) {
                    _submitSuccess.value = true
                } else {
                    _errorMessage.value = response.message
                }

            } catch (e: Exception) {
                Log.e("ForgotPasswordVM", "API call failed", e)
                val msg = when (e) {
                    is HttpException -> {
                        // Handle 429 Too Many Requests or 422 Validation Errors
                        val errorBody = e.response()?.errorBody()?.string()
                        try {
                            // PHP returns {"message": "..."} for 429
                            val err = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                            err.message
                        } catch (ex: Exception) {
                            "Request failed: ${e.message()}"
                        }
                    }
                    else -> "Network error. Please try again."
                }
                _errorMessage.value = msg
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onErrorShown() {
        _errorMessage.value = null
    }
}