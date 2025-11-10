package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// Import our updated Employee data class
import com.example.autopayroll_mobile.data.model.Employee
import com.example.autopayroll_mobile.network.ApiClient
// SessionManager is no longer needed here to get the ID
// import com.example.autopayroll_mobile.utils.SessionManager
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

    // private val sessionManager = SessionManager(application.applicationContext) // Not needed
    private val apiService = ApiClient.getClient(application.applicationContext)

    // A "StateFlow" that the UI will listen to for changes
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        fetchData() // Fetch data as soon as the ViewModel is created
    }

    // You can call this from your UI to refresh
    fun refreshData() {
        fetchData()
    }

    private fun fetchData() {
        // Set state to loading
        _uiState.value = DashboardUiState(isLoading = true)

        // Launch a coroutine in the ViewModel's own scope
        viewModelScope.launch {
            try {
                // --- THIS IS THE SIMPLIFIED LOGIC ---

                // 1. Call the new function. No ID is needed.
                // The AuthInterceptor automatically adds the token.
                val employee: Employee = apiService.getEmployeeProfile()

                // 2. Update the UI state with all data at once.
                // We can now use employee.companyName!
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    employeeName = "${employee.firstName} ${employee.lastName}",
                    employeeId = employee.employeeId,
                    jobAndCompany = "${employee.jobPosition} • ${employee.companyName}"
                )

            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error fetching employee profile", e)
                // Handle the error
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    employeeName = "Error loading data",
                    employeeId = "N/A",
                    jobAndCompany = "Error: ${e.message}"
                )
            }
        }
    }
}