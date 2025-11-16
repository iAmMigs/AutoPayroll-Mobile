package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.model.Employee
import com.example.autopayroll_mobile.data.model.Payroll
import com.example.autopayroll_mobile.data.model.PayrollResponse
import com.example.autopayroll_mobile.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val employeeName: String = "Loading...",
    val employeeId: String = "...",
    val jobAndCompany: String = "Loading...",
    val recentPayslip: Payroll? = null,
    val profilePhotoUrl: String? = null // ADDED: To hold the image URL
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiClient.getClient(application.applicationContext)
    private val baseUrl = "https://autopayroll.org" // Base URL for constructing image paths

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        fetchData()
    }

    fun refreshData() {
        fetchData()
    }

    private fun fetchData() {
        _uiState.value = DashboardUiState(isLoading = true)

        viewModelScope.launch {
            try {
                val employee: Employee = apiService.getEmployeeProfile()
                val payrollResponse: PayrollResponse = apiService.getPayrolls()

                val mostRecentPayslip = payrollResponse.data
                    .sortedByDescending { it.payDate }
                    .firstOrNull()

                // --- MODIFIED: Construct the full photo URL ---
                val fullPhotoUrl = employee.profilePhoto?.let { path ->
                    if (path.startsWith("http")) {
                        path // It's already a full URL
                    } else {
                        // It's a relative path. Remove potential leading slash.
                        "$baseUrl/" + path.removePrefix("/")
                    }
                }

                _uiState.value = DashboardUiState(
                    isLoading = false,
                    employeeName = "${employee.firstName} ${employee.lastName}",
                    employeeId = employee.employeeId,
                    jobAndCompany = "${employee.jobPosition} • ${employee.companyName}",
                    recentPayslip = mostRecentPayslip,
                    profilePhotoUrl = fullPhotoUrl // Set the URL here
                )

            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error fetching dashboard data", e)
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    employeeName = "Error loading data",
                    employeeId = "N/A",
                    jobAndCompany = "Error: ${e.message}",
                    recentPayslip = null,
                    profilePhotoUrl = null // Ensure it's null on error
                )
            }
        }
    }
}