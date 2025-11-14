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
import retrofit2.HttpException
import java.time.OffsetDateTime
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.Locale

data class PayslipUiState(
    val isLoading: Boolean = true,
    val employeeName: String = "Loading...",
    val jobAndCompany: String = "Loading...",
    val payslips: List<Payslip> = emptyList(),
    val listErrorMessage: String? = null,
    val allPayslips: List<Payslip> = emptyList(),
    val availableYears: List<Int> = emptyList(),
    val selectedYear: Int = Year.now().value
)

class PayslipViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application.applicationContext)
    private val apiService = ApiClient.getClient(application.applicationContext)

    private val _uiState = MutableStateFlow(PayslipUiState())
    val uiState: StateFlow<PayslipUiState> = _uiState.asStateFlow()

    private val outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())

    init {
        fetchData(initialLoad = true)
    }

    fun refreshData() {
        fetchData(initialLoad = false)
    }

    fun onYearSelected(year: Int) {
        val filtered = _uiState.value.allPayslips.filter { it.year == year }
        _uiState.update {
            it.copy(
                selectedYear = year,
                payslips = filtered,
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

    private fun fetchData(initialLoad: Boolean) {
        // Only show loading if there's no data yet, or it's a full refresh.
        // Don't set isLoading=true if we already have data and it's not an initial load/forced refresh
        if (initialLoad || _uiState.value.allPayslips.isEmpty()) {
            _uiState.update { it.copy(isLoading = true, listErrorMessage = null) }
        } else {
            _uiState.update { it.copy(listErrorMessage = null) } // Clear previous error messages
        }


        viewModelScope.launch {
            // --- PART 1: Fetch Employee ---
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
                // If there's an error, don't clear existing employee data if it's already there
                _uiState.update { currentState ->
                    currentState.copy(
                        // Only update isLoading to false if it was true due to this fetch
                        isLoading = if (currentState.isLoading) false else currentState.isLoading,
                        employeeName = if (currentState.employeeName == "Loading...") "Error loading profile" else currentState.employeeName,
                        jobAndCompany = if (currentState.jobAndCompany == "Loading...") "Error: ${e.message}" else currentState.jobAndCompany,
                        listErrorMessage = "Failed to load employee profile." // Set a specific error for employee
                    )
                }
                // Don't return here, attempt to fetch payslips even if employee profile fails
            }

            // --- PART 2: Fetch Payrolls ---
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
                        listErrorMessage = if (realPayslips.isEmpty()) "No payslips found." else null
                    )
                }
            } catch (e: Exception) {
                Log.e("PayslipViewModel", "Error fetching payrolls", e)

                val errorMessage = when (e) {
                    is HttpException -> {
                        when (e.code()) {
                            401, 404 -> "No payslips found."
                            else -> "Error loading payslips: ${e.message()}"
                        }
                    }
                    else -> "An unexpected error occurred: ${e.message}"
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        // Only clear payslips if there was no data before, or if the error is "No payslips found"
                        payslips = if (currentState.allPayslips.isEmpty() || errorMessage == "No payslips found.") emptyList() else currentState.payslips,
                        allPayslips = if (currentState.allPayslips.isEmpty() || errorMessage == "No payslips found.") emptyList() else currentState.allPayslips,
                        listErrorMessage = errorMessage
                    )
                }
            } finally {
                // Ensure isLoading is false once all data fetching attempts are complete
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
