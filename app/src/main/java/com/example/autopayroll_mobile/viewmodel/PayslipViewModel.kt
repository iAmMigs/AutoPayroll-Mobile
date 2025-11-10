package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// Import your new API models
import com.example.autopayroll_mobile.data.model.PayrollResponse
import com.example.autopayroll_mobile.data.model.Employee
import com.example.autopayroll_mobile.data.model.Payslip
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    private fun formatApiDate(apiDate: String): String {
        return try {
            // 1. Define the format of the output, e.g., "September 20, 2025"
            val outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())

            // 2. Parse the input string (e.g., "2025-09-20T16:00:00.000000Z")
            val dateTime = OffsetDateTime.parse(apiDate)

            // 3. Format it into the new, clean string
            dateTime.format(outputFormatter)
        } catch (e: Exception) {
            // If parsing fails, just return the original (or "Invalid Date")
            Log.e("PayslipViewModel", "Error parsing date: $apiDate", e)
            "Invalid Date"
        }
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
                        // --- THIS IS THE CHANGE ---
                        dateRange = formatApiDate(apiPayroll.payDate),
                        // --- END OF CHANGE ---
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