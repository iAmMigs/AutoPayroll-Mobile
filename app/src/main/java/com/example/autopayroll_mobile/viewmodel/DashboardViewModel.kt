package com.example.autopayroll_mobile.viewmodel // ## Updated Package ##

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.model.Employee
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// This data class holds all the data for your screen
data class DashboardUiState(
    val isLoading: Boolean = true,
    val employeeName: String = "Loading...",
    val employeeId: String = "...",
    val jobAndCompany: String = "Loading..."
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application.applicationContext)
    private val apiService = ApiClient.getClient(application.applicationContext)

    // A "StateFlow" that the UI will listen to for changes
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        fetchData() // Fetch data as soon as the ViewModel is created
    }

    private fun fetchData() {
        val employeeId = sessionManager.getEmployeeId()

        if (employeeId == null) {
            _uiState.value = DashboardUiState(
                isLoading = false,
                employeeName = "Error: Not logged in",
                employeeId = "N/A",
                jobAndCompany = "Please log in"
            )
            return
        }

        // Launch a coroutine in the ViewModel's own scope
        viewModelScope.launch {
            try {
                // First API Call: Get Employee
                val employee = apiService.getEmployeeProfile(employeeId)

                // Update state with partial data
                _uiState.value = DashboardUiState(
                    isLoading = true, // Still loading company
                    employeeName = "${employee.firstName} ${employee.lastName}",
                    employeeId = employee.employeeId,
                    jobAndCompany = "${employee.jobPosition} • Loading company..."
                )

                // Second API Call: Get Company
                try {
                    val company = apiService.getCompany(employee.companyId)

                    // Final Success Update
                    _uiState.value = DashboardUiState(
                        isLoading = false,
                        employeeName = "${employee.firstName} ${employee.lastName}",
                        employeeId = employee.employeeId,
                        jobAndCompany = "${employee.jobPosition} • ${company.companyName}"
                    )
                } catch (companyError: Exception) {
                    Log.e("DashboardViewModel", "Error fetching company", companyError)
                    // Error on second call
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        jobAndCompany = "${employee.jobPosition} • Unknown Company"
                    )
                }
            } catch (employeeError: Exception) {
                Log.e("DashboardViewModel", "Error fetching employee", employeeError)
                // Error on first call
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    employeeName = "Error loading data",
                    employeeId = "N/A",
                    jobAndCompany = "Error"
                )
            }
        }
    }
}







