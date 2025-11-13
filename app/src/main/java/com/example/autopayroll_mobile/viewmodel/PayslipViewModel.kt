package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import retrofit2.HttpException // <-- ADD THIS IMPORT
import java.time.OffsetDateTime
import java.time.Year // <-- ADD THIS IMPORT
import java.time.format.DateTimeFormatter
import java.util.Locale

// --- 1. UPDATE YOUR UISTATE ---
data class PayslipUiState(
    val isLoading: Boolean = true,
    val employeeName: String = "Loading...",
    val jobAndCompany: String = "Loading...",
    val payslips: List<Payslip> = emptyList(), // This will be the FILTERED list
    val listErrorMessage: String? = null,
    // --- ADD THESE NEW FIELDS ---
    val allPayslips: List<Payslip> = emptyList(), // This is the MASTER list
    val availableYears: List<Int> = emptyList(),
    val selectedYear: Int = Year.now().value // Default to current year
)

class PayslipViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application.applicationContext)
    private val apiService = ApiClient.getClient(application.applicationContext)

    private val _uiState = MutableStateFlow(PayslipUiState())
    val uiState: StateFlow<PayslipUiState> = _uiState.asStateFlow()

    // Formatter for display, e.g., "September 20, 2025"
    private val outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())

    init {
        fetchData()
    }

    fun refreshData() {
        fetchData()
    }

    // --- 2. ADD THIS NEW FUNCTION FOR FILTERING ---
    fun onYearSelected(year: Int) {
        // Filter the master list to create the new displayed list
        val filtered = _uiState.value.allPayslips.filter { it.year == year }
        _uiState.update {
            it.copy(
                selectedYear = year,
                payslips = filtered,
                // Show a message if the filtered list is empty
                listErrorMessage = if (filtered.isEmpty()) "No payslips found for $year." else null
            )
        }
    }

    private fun formatApiDate(apiDate: String): String {
        return try {
            val dateTime = OffsetDateTime.parse(apiDate)
            dateTime.format(outputFormatter)
        } catch (e: Exception) {
            Log.e("PayslipViewModel", "Error parsing date: $apiDate", e)
            "Invalid Date"
        }
    }

    // --- 3. THIS IS THE UPDATED FETCHDATA FUNCTION ---
    private fun fetchData() {
        _uiState.update { it.copy(isLoading = true, listErrorMessage = null) }

        viewModelScope.launch {
            // --- PART 1: Fetch Employee (Unchanged) ---
            try {
                val employee = apiService.getEmployeeProfile()
                _uiState.update {
                    it.copy(
                        employeeName = "${employee.firstName} ${employee.lastName}",
                        jobAndCompany = "${employee.jobPosition} • ${employee.companyName}"
                    )
                }
            } catch (e: Exception) {
                Log.e("PayslipViewModel", "Error fetching employee profile", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        employeeName = "Error loading profile",
                        jobAndCompany = "Error: ${e.message}"
                    )
                }
                return@launch
            }

            // --- PART 2: Fetch Payrolls (Updated) ---
            try {
                val payrollResponse = apiService.getPayrolls()

                val realPayslips = payrollResponse.data.map { apiPayroll ->
                    val dateTime = OffsetDateTime.parse(apiPayroll.payDate)
                    Payslip(
                        dateRange = dateTime.format(outputFormatter),
                        netAmount = "₱${apiPayroll.netPay}",
                        status = apiPayroll.status.replaceFirstChar { it.uppercase() },
                        year = dateTime.year
                    )
                }

                val years = realPayslips.map { it.year }.distinct().sortedDescending()
                val currentYear = years.firstOrNull() ?: Year.now().value

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allPayslips = realPayslips,
                        payslips = realPayslips.filter { it.year == currentYear },
                        availableYears = years,
                        selectedYear = currentYear,
                        // This is the original "empty list" logic
                        listErrorMessage = if (realPayslips.isEmpty()) "No payslips found." else null
                    )
                }
            } catch (e: Exception) {
                Log.e("PayslipViewModel", "Error fetching payrolls", e)

                // ## THIS IS THE FIX ##
                // We will now check the HTTP error code
                val errorMessage = when (e) {
                    is HttpException -> {
                        when (e.code()) {
                            // Treat 401 AND 404 as "No payslips found"
                            401 -> "No payslips found."
                            404 -> "No payslips found."
                            else -> "Error loading payslips: ${e.message()}"
                        }
                    }
                    else -> "An unexpected error occurred: ${e.message}"
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        payslips = emptyList(),
                        allPayslips = emptyList(),
                        listErrorMessage = errorMessage
                    )
                }
            }
        }
    }
}