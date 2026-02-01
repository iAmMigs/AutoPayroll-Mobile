package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.generalData.Employee
import com.example.autopayroll_mobile.network.ApiClient
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ProfileUiState(
    val isLoading: Boolean = true,
    val employee: Employee? = null,
    val error: String? = null
)

sealed class ProfileNavigationEvent {
    object NavigateBack : ProfileNavigationEvent()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _navigationEvent = MutableStateFlow<ProfileNavigationEvent?>(null)
    val navigationEvent: StateFlow<ProfileNavigationEvent?> = _navigationEvent.asStateFlow()

    private val apiService = ApiClient.getClient(application)
    private val baseUrl = "https://autopayroll.org"

    init {
        fetchEmployeeData()
    }

    fun fetchEmployeeData() {
        // Reset state to loading
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // 1. Call API directly
                val employee = apiService.getEmployeeProfile()

                // 2. Fix Profile Photo URL
                val fullPhotoUrl = employee.profilePhoto?.let { path ->
                    if (path.startsWith("http")) path else "$baseUrl/" + path.removePrefix("/")
                }

                // Create a copy of the employee with the corrected photo URL
                val updatedEmployee = employee.copy(profilePhoto = fullPhotoUrl)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        employee = updatedEmployee,
                        error = null
                    )
                }

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching employee data", e)

                // 3. Robust Error Handling
                val errorText = when {
                    e is HttpException && e.code() == 401 -> "Session expired. Please login again."
                    e is JsonSyntaxException -> "Data format error: ${e.message}. Please check App Updates."
                    else -> "Failed to fetch profile: ${e.message}"
                }

                _uiState.update {
                    it.copy(isLoading = false, error = errorText)
                }
            }
        }
    }

    fun navigateBack() {
        _navigationEvent.value = ProfileNavigationEvent.NavigateBack
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}