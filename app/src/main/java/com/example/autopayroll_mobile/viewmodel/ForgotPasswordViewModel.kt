package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {

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
        val currentEmail = _email.value ?: ""

        if (currentEmail.isEmpty()) {
            _errorMessage.value = "Please enter your email address"
            return
        }

        // TODO: Integrate actual API call here later
        simulateApiCall(currentEmail)
    }

    private fun simulateApiCall(email: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            // Simulate network delay
            delay(2000)

            // Mock success logic
            if (email.contains("@")) {
                _submitSuccess.value = true
            } else {
                _errorMessage.value = "Invalid email format"
                _submitSuccess.value = false
            }

            _isLoading.value = false
        }
    }

    fun onErrorShown() {
        _errorMessage.value = null
    }
}