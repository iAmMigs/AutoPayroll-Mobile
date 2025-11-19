package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.model.Employee
import com.example.autopayroll_mobile.data.model.Payroll
import com.example.autopayroll_mobile.data.model.PayrollResponse
import com.example.autopayroll_mobile.data.model.Schedule // Import Schedule
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
    val profilePhotoUrl: String? = null,
    val currentSchedule: Schedule? = null // ADDED: Schedule data
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = ApiClient.getClient(application.applicationContext)
    private val baseUrl = "https://autopayroll.org"

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        fetchData()
    }

    fun refreshData() {
        fetchData()
    }

    private fun fetchData() {
        // We don't set isLoading = true here to avoid flickering if refreshing silently
        // or you can keep it if you want the spinner every time.
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                // Use async/await pattern if you want parallel requests,
                // but sequential is fine for now.
                val employee: Employee = apiService.getEmployeeProfile()
                val payrollResponse: PayrollResponse = apiService.getPayrolls()

                // --- NEW: Fetch Schedule ---
                var schedule: Schedule? = null
                try {
                    val scheduleResponse = apiService.getSchedule()
                    if (scheduleResponse.success) {
                        schedule = scheduleResponse.schedule
                    }
                } catch (e: Exception) {
                    Log.e("DashboardViewModel", "Error fetching schedule (might be 404)", e)
                    // Schedule remains null
                }

                val mostRecentPayslip = payrollResponse.data
                    .sortedByDescending { it.payDate }
                    .firstOrNull()

                val fullPhotoUrl = employee.profilePhoto?.let { path ->
                    if (path.startsWith("http")) {
                        path
                    } else {
                        "$baseUrl/" + path.removePrefix("/")
                    }
                }

                _uiState.value = DashboardUiState(
                    isLoading = false,
                    employeeName = "${employee.firstName} ${employee.lastName}",
                    employeeId = employee.employeeId,
                    jobAndCompany = "${employee.jobPosition} • ${employee.companyName}",
                    recentPayslip = mostRecentPayslip,
                    profilePhotoUrl = fullPhotoUrl,
                    currentSchedule = schedule // Set the schedule
                )

            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error fetching dashboard data", e)
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    employeeName = "Error loading data",
                    employeeId = "N/A",
                    jobAndCompany = "Error: ${e.message}",
                    recentPayslip = null,
                    profilePhotoUrl = null,
                    currentSchedule = null
                )
            }
        }
    }
}