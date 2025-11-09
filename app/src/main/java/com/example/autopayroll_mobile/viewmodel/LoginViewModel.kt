package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.model.LoginRequest
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.utils.SessionManager
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> = _password

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

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
            showToast("Email or Username is required")
            return
        }

        if (currentPassword.isEmpty()) {
            showToast("Password is required")
            return
        }

        authenticateUserOnline(currentEmail, currentPassword)
    }

    private fun authenticateUserOnline(loginIdentifier: String, pass: String) {
        _isLoading.value = true
        val loginRequest = LoginRequest(identifier = loginIdentifier, password = pass)
        val apiService = ApiClient.getClient(getApplication<Application>().applicationContext)

        viewModelScope.launch {
            try {
                val response = apiService.login(loginRequest)
                val sessionManager = SessionManager(getApplication<Application>().applicationContext)
                sessionManager.saveSession(response.employee_id, response.token)
                _loginSuccess.value = true
                showToast("Login Successful!")
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login failed", e)
                showToast("Login failed: ${e.message}")
                _loginSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(getApplication<Application>().applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}