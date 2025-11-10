package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// import com.example.autopayroll_mobile.data.model.Company // No longer needed
import com.example.autopayroll_mobile.data.model.Employee
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val employee: Employee? = null,
    // val company: Company? = null, // We removed this
    val error: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val sessionManager = SessionManager(application)
    // Get the apiService once
    private val apiService = ApiClient.getClient(application)

    init {
        fetchEmployeeData()
    }

    fun fetchEmployeeData() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoading = true)

            // We can still check if a user is logged in
            if (sessionManager.getEmployeeId() == null) {
                _uiState.value = ProfileUiState(isLoading = false, error = "Employee ID not found. Please log in again.")
                return@launch
            }

            try {
                // --- THIS IS THE FIX ---
                // 1. Call the correct function (no ID passed)
                val employee = apiService.getEmployeeProfile()

                // 2. Set the state with the employee data.
                // The employee object already contains the companyName.
                // No second API call is needed.
                _uiState.value = ProfileUiState(isLoading = false, employee = employee)

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching employee data", e)
                _uiState.value = ProfileUiState(isLoading = false, error = "Failed to fetch employee data: ${e.message}")
            }
        }
    }
}