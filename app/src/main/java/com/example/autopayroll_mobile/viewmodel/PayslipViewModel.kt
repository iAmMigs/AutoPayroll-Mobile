package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// Import your new API models
import com.example.autopayroll_mobile.data.model.PayrollResponse
import com.example.autopayroll_mobile.data.model.Employee
import com.example.autopayroll_mobile.models.Payslip
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PayslipUiState(
    val isLoading: Boolean = true,
    val employeeName: String = "Loading...",
    val jobAndCompany: String = "Loading...",
    val payslips: List<Payslip> = emptyList(),
    val listErrorMessage: String? = null // <-- ADD THIS NEW FIELD
)

class PayslipViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application.applicationContext)
    // Make sure your ApiClient.getClient() returns the ApiService interface
    private val apiService = ApiClient.getClient(application.applicationContext)

    private val _uiState = MutableStateFlow(PayslipUiState())
    val uiState: StateFlow<PayslipUiState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    fun refreshData() {
        fetchData()
    }

    private fun fetchData() {
        // Set initial loading state
        _uiState.update { it.copy(isLoading = true, listErrorMessage = null) }

        viewModelScope.launch {
            // --- PART 1: Fetch Employee (and update header) ---
            try {
                val employee = apiService.getEmployeeProfile()
                // Success: Update the header UI immediately
                _uiState.update {
                    it.copy(
                        employeeName = "${employee.firstName} ${employee.lastName}",
                        jobAndCompany = "${employee.jobPosition} • ${employee.companyName}"
                        // Note: isLoading is still true until the list loads
                    )
                }
            } catch (e: Exception) {
                Log.e("PayslipViewModel", "Error fetching employee profile", e)
                // Failed: Show error just for the header
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        employeeName = "Error loading profile",
                        jobAndCompany = "Error: ${e.message}"
                    )
                }
                return@launch // Stop if the profile fails
            }

            // --- PART 2: Fetch Payrolls (and update list) ---
            try {
                val payrollResponse = apiService.getPayrolls() // <-- This will still fail with 404
                val realPayslips = payrollResponse.data.map { apiPayroll ->
                    Payslip(
                        dateRange = apiPayroll.payDate, // TODO: Fix date range
                        netAmount = "₱${apiPayroll.netPay}",
                        status = apiPayroll.status.replaceFirstChar { it.uppercase() }
                    )
                }

                // Success: Update the list and set loading to false
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        payslips = realPayslips,
                        listErrorMessage = if (realPayslips.isEmpty()) "No payslips found." else null
                    )
                }
            } catch (e: Exception) {
                Log.e("PayslipViewModel", "Error fetching payrolls", e)
                // Failed: The 404 error will be caught here
                // Now we can show the error for the LIST, not the header
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        payslips = emptyList(),
                        listErrorMessage = "Error loading payslips: ${e.message}" // e.g., "Error... HTTP 404"
                    )
                }
            }
        }
    }
}