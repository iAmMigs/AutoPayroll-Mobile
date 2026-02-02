package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.model.Payslip
import com.example.autopayroll_mobile.network.ApiClient
import com.google.gson.JsonSyntaxException
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

    private val apiService = ApiClient.getClient(application.applicationContext)
    private val _uiState = MutableStateFlow(PayslipUiState())
    val uiState: StateFlow<PayslipUiState> = _uiState.asStateFlow()

    private val outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())

    // URL to your public storage. Adjust if your server uses a different structure.
    private val baseUrl = "https://autopayroll.org/storage/"

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
                listErrorMessage = if (filtered.isEmpty() && it.allPayslips.isNotEmpty()) "No payslips found for $year." else it.listErrorMessage
            )
        }
    }

    private fun fetchData(initialLoad: Boolean) {
        if (initialLoad || _uiState.value.allPayslips.isEmpty()) {
            _uiState.update { it.copy(isLoading = true, listErrorMessage = null) }
        } else {
            _uiState.update { it.copy(listErrorMessage = null) }
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
                Log.e("PayslipViewModel", "Error fetching employee", e)
            }

            // --- PART 2: Fetch Payrolls ---
            try {
                val payrollResponse = apiService.getPayrolls()

                // Safely handle null data to prevent crashes
                val rawList = payrollResponse.data ?: emptyList()

                val realPayslips = rawList.map { apiPayroll ->
                    // 1. Safe Date Parsing
                    val rawDate = apiPayroll.payDate ?: ""
                    val dateStr = try {
                        if (rawDate.isNotEmpty()) OffsetDateTime.parse(rawDate).format(outputFormatter) else "N/A"
                    } catch (e: Exception) { rawDate }

                    val yearInt = try {
                        if (rawDate.isNotEmpty()) OffsetDateTime.parse(rawDate).year else Year.now().value
                    } catch (e: Exception) { Year.now().value }

                    // 2. Safe Strings
                    val net = apiPayroll.netPay ?: "0.00"
                    val status = apiPayroll.status?.replaceFirstChar { it.uppercase() } ?: "Unknown"

                    // 3. Construct PDF URL
                    val pdfUrl = if (!apiPayroll.filePath.isNullOrBlank()) {
                        // Remove leading slash if present to avoid double slashes
                        baseUrl + apiPayroll.filePath.removePrefix("/")
                    } else {
                        null
                    }

                    Payslip(
                        dateRange = dateStr,
                        netAmount = "₱$net",
                        status = status,
                        year = yearInt,
                        pdfUrl = pdfUrl
                    )
                }

                val years = realPayslips.map { it.year }.distinct().sortedDescending()
                val currentYear = years.firstOrNull() ?: Year.now().value

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allPayslips = realPayslips,
                        payslips = realPayslips.filter { p -> p.year == currentYear },
                        availableYears = years,
                        selectedYear = currentYear,
                        listErrorMessage = if (realPayslips.isEmpty()) "No available payslip" else null
                    )
                }
            } catch (e: Exception) {
                Log.e("PayslipViewModel", "Error fetching payrolls", e)
                val errorMessage = when (e) {
                    is JsonSyntaxException, is IllegalStateException -> "Error parsing data"
                    is HttpException -> {
                        if (e.code() == 404 || e.code() == 401) "No available payslip"
                        else "Server error: ${e.message()}"
                    }
                    else -> "An unexpected error occurred."
                }
                _uiState.update {
                    it.copy(isLoading = false, payslips = emptyList(), listErrorMessage = errorMessage)
                }
            }
        }
    }
}