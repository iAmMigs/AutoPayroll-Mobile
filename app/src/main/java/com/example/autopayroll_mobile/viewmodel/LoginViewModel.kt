package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.loginModule.LoginRequest
import com.example.autopayroll_mobile.data.loginModule.LoginResponse
import com.example.autopayroll_mobile.data.model.ApiErrorResponse
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.utils.SessionManager
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.google.gson.Gson

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun login() {
        val currentEmail = _email.value ?: ""
        val currentPassword = _password.value ?: ""

        if (currentEmail.isEmpty()) {
            _errorMessage.value = "Email or Username is required"
            return
        }

        if (currentPassword.isEmpty()) {
            _errorMessage.value = "Password is required"
            return
        }

        val androidId = Settings.Secure.getString(
            getApplication<Application>().contentResolver,
            Settings.Secure.ANDROID_ID
        )

        if (androidId.isNullOrEmpty()) {
            _errorMessage.value = "Could not retrieve device ID"
            return
        }

        authenticateUserOnline(currentEmail, currentPassword, androidId)
    }

    private fun authenticateUserOnline(loginIdentifier: String, pass: String, androidId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        val loginRequest = LoginRequest(
            identifier = loginIdentifier,
            password = pass,
            androidId = androidId
        )

        val apiService = ApiClient.getClient(getApplication<Application>().applicationContext)
        val sessionManager = SessionManager(getApplication<Application>().applicationContext)

        viewModelScope.launch {
            try {
                val response: LoginResponse = apiService.login(loginRequest)
                sessionManager.saveSession(response.employeeId, response.token)
                _loginSuccess.value = true
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login failed", e)

                val errorMsg = if (e is HttpException) {
                    val errorBodyString = e.response()?.errorBody()?.string()
                    Log.e("LoginViewModel", "HTTP Error Code: ${e.code()}, Body: $errorBodyString")

                    if (errorBodyString != null) {
                        try {
                            val errorResponse = Gson().fromJson(errorBodyString, ApiErrorResponse::class.java)
                            errorResponse.message ?: "An unknown error occurred."
                        } catch (jsonParseException: Exception) {
                            Log.e("LoginViewModel", "Failed to parse error JSON", jsonParseException)
                            "An unexpected server error occurred. (Code: ${e.code()})"
                        }
                    } else {
                        "An unknown error occurred. (Code: ${e.code()})"
                    }
                } else {
                    "Login failed: ${e.message ?: "Unknown network error"}"
                }

                _errorMessage.value = errorMsg
                _loginSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onErrorShown() {
        _errorMessage.value = null
    }
}