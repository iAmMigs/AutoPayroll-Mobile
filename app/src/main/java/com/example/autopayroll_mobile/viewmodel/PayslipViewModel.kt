package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

        viewModelScope.launch {
            try {
                // First API Call: Get Employee
                val employee = apiService.getEmployeeProfile(employeeId)

                // Dummy payslip data for now
                val payslips = listOf(
                    Payslip("July 16 - 31, 2025", "5,456.15", "Processing"),
                    Payslip("July 1 - 15, 2025", "5,456.15", "Completed"),
                    Payslip("June 16 - 30, 2025", "5,456.15", "Completed"),
                    Payslip("June 1 - 15, 2025", "5,456.15", "Completed")
                )
                
                // Update state with partial data
                _uiState.value = PayslipUiState(
                    isLoading = true, // Still loading company
                    employeeName = "${employee.firstName} ${employee.lastName}",
                    jobAndCompany = "${employee.jobPosition} • Loading company...",
                    payslips = payslips
                )

                // Second API Call: Get Company
                try {
                    val company = apiService.getCompany(employee.companyId)

                    // Final Success Update
                    _uiState.value = PayslipUiState(
                        isLoading = false,
                        employeeName = "${employee.firstName} ${employee.lastName}",
                        jobAndCompany = "${employee.jobPosition} • ${company.companyName}",
                        payslips = payslips
                    )
                } catch (companyError: Exception) {
                    Log.e("PayslipViewModel", "Error fetching company", companyError)
                    // Error on second call
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        jobAndCompany = "${employee.jobPosition} • Unknown Company"
                    )
                }
            } catch (employeeError: Exception) {
                Log.e("PayslipViewModel", "Error fetching employee", employeeError)
                // Error on first call
                _uiState.value = PayslipUiState(
                    isLoading = false,
                    employeeName = "Error loading data",
                    jobAndCompany = "Error"
                )
            }
        }
    }
}
