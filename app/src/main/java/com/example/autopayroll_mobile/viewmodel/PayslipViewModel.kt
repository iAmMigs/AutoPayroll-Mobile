package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// Import the Employee model, which is what getEmployeeProfile returns
import com.example.autopayroll_mobile.data.model.Employee
import com.example.autopayroll_mobile.models.Payslip
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PayslipUiState(
    val isLoading: Boolean = true,
    val employeeName: String = "Loading...",
    val jobAndCompany: String = "Loading...",
    val payslips: List<Payslip> = emptyList()
)

class PayslipViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application.applicationContext)
    private val apiService = ApiClient.getClient(application.applicationContext)

    private val _uiState = MutableStateFlow(PayslipUiState())
    val uiState: StateFlow<PayslipUiState> = _uiState

    init {
        fetchData()
    }

    // You can call this from your UI to refresh
    fun refreshData() {
        fetchData()
    }

    private fun fetchData() {
        val employeeId = sessionManager.getEmployeeId()

        if (employeeId == null) {
            _uiState.value = PayslipUiState(
                isLoading = false,
                employeeName = "Error: Not logged in",
                jobAndCompany = "Please log in"
            )
            return
        }

        // Set initial loading state
        _uiState.value = PayslipUiState(isLoading = true)

        viewModelScope.launch {
            try {
                // --- THIS IS THE FIX ---

                // 1. Call the correct function (no ID needed)
                val employee: Employee = apiService.getEmployeeProfile()

                // Dummy payslip data (as in original file)
                val payslips = listOf(
                    Payslip("July 16 - 31, 2025", "5,456.15", "Processing"),
                    Payslip("July 1 - 15, 2025", "5,456.15", "Completed"),
                    Payslip("June 16 - 30, 2025", "5,456.15", "Completed"),
                    Payslip("June 1 - 15, 2025", "5,456.15", "Completed")
                )

                // 2. Update the UI state ONCE with all data.
                // We get employee.companyName directly from the employee object.
                _uiState.value = PayslipUiState(
                    isLoading = false,
                    employeeName = "${employee.firstName} ${employee.lastName}",
                    jobAndCompany = "${employee.jobPosition} • ${employee.companyName}",
                    payslips = payslips
                )

                // 3. The second API call for getCompany() is completely removed.

            } catch (e: Exception) {
                Log.e("PayslipViewModel", "Error fetching data", e)
                // Error on first call
                _uiState.value = PayslipUiState(
                    isLoading = false,
                    employeeName = "Error loading data",
                    jobAndCompany = "Error: ${e.message}"
                )
            }
        }
    }
}