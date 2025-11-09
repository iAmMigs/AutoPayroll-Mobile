package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.model.Company
import com.example.autopayroll_mobile.data.model.Employee
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val employee: Employee? = null,
    val company: Company? = null,
    val error: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val sessionManager = SessionManager(application)

    init {
        fetchEmployeeData()
    }

    fun fetchEmployeeData() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoading = true)
            try {
                val employeeId = sessionManager.getEmployeeId()
                if (employeeId == null) {
                    _uiState.value = ProfileUiState(isLoading = false, error = "Employee ID not found")
                    return@launch
                }

                val apiService = ApiClient.getClient(getApplication())
                val employee = apiService.getEmployeeProfile(employeeId)
                var company: Company? = null
                try {
                    company = apiService.getCompany(employee.companyId)
                } catch (companyError: Exception) {
                    Log.e("ProfileViewModel", "Error fetching company", companyError)
                }

                _uiState.value = ProfileUiState(isLoading = false, employee = employee, company = company)

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching employee data", e)
                _uiState.value = ProfileUiState(isLoading = false, error = "Failed to fetch employee data: ${e.message}")
            }
        }
    }
}
