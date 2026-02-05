package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.model.Payroll
import com.example.autopayroll_mobile.data.model.Payslip
import com.example.autopayroll_mobile.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.Locale

data class PayslipUiState(
    val isLoading: Boolean = true,
    val isPdfLoading: Boolean = false,
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

    private val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

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
            try {
                val payrollResponse = apiService.getPayrolls()

                val validPayrolls = payrollResponse.data.filter {
                    it.payDate.isNotBlank() && it.payrollId.isNotBlank()
                }

                val realPayslips = validPayrolls.mapNotNull { apiPayroll ->
                    try { mapToPayslipUiModel(apiPayroll) } catch (e: Exception) { null }
                }

                val sortedPayslips = realPayslips.sortedByDescending {
                    try { LocalDate.parse(it.originalPayDate, inputFormatter) } catch (e: Exception) { LocalDate.MIN }
                }

                val years = sortedPayslips.map { it.year }.distinct().sorted().reversed()
                val currentYear = years.firstOrNull() ?: Year.now().value

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allPayslips = sortedPayslips,
                        payslips = sortedPayslips.filter { p -> p.year == currentYear },
                        availableYears = years,
                        selectedYear = currentYear,
                        listErrorMessage = if (sortedPayslips.isEmpty()) "No available payslip" else null
                    )
                }

            } catch (e: Exception) {
                val msg = if (e is HttpException && e.code() == 401) "Session expired." else "Unable to load payslips."
                _uiState.update { it.copy(isLoading = false, listErrorMessage = msg) }
            }
        }
    }

    // --- PDF Functionality ---
    fun viewPayslipPdf(payslip: Payslip) {
        if (_uiState.value.isPdfLoading) return

        _uiState.update { it.copy(isPdfLoading = true) }
        showToast("Downloading payslip...")

        viewModelScope.launch {
            try {
                val date = LocalDate.parse(payslip.originalPayDate, inputFormatter)
                // Use the period identifier we calculated in mapToPayslipUiModel
                val period = payslip.periodIdentifier

                // 1. Download PDF
                val response = apiService.downloadPayslip(
                    year = date.year,
                    month = date.monthValue,
                    period = period
                )

                if (response.isSuccessful && response.body() != null) {
                    // 2. Save to Cache
                    val fileName = "Payslip_${date.year}_${date.monthValue}_$period.pdf"
                    val file = File(getApplication<Application>().cacheDir, fileName)

                    withContext(Dispatchers.IO) {
                        val inputStream = response.body()!!.byteStream()
                        val outputStream = FileOutputStream(file)
                        inputStream.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }
                    }

                    // 3. Open PDF
                    withContext(Dispatchers.Main) {
                        openPdfIntent(file)
                    }
                } else {
                    showToast("Download failed: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("PayslipViewModel", "PDF Error", e)
                showToast("Error: ${e.message}")
            } finally {
                _uiState.update { it.copy(isPdfLoading = false) }
            }
        }
    }

    private fun openPdfIntent(file: File) {
        try {
            val context = getApplication<Application>()

            // This AUTHORITY string must match the one in AndroidManifest.xml
            val authority = "${context.packageName}.provider"

            val uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            // Create a chooser so the user can pick their preferred PDF app
            val chooser = Intent.createChooser(intent, "Open Payslip")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(chooser)

        } catch (e: IllegalArgumentException) {
            // This usually happens if the Provider is not set up in Manifest
            Log.e("PayslipViewModel", "FileProvider Error", e)
            showToast("App Error: FileProvider not configured.")
        } catch (e: Exception) {
            Log.e("PayslipViewModel", "PDF Viewer Error", e)
            showToast("No PDF Viewer app installed.")
        }
    }

    private fun showToast(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun mapToPayslipUiModel(apiPayroll: Payroll): Payslip {
        val payDateObj = try {
            if (apiPayroll.payDate.isNotBlank()) LocalDate.parse(apiPayroll.payDate, inputFormatter) else LocalDate.now()
        } catch (e: Exception) { LocalDate.now() }

        // Logic to determine if it's 1-15 or 16-30
        val startDay = try {
            if (!apiPayroll.startDate.isNullOrBlank()) {
                LocalDate.parse(apiPayroll.startDate, inputFormatter).dayOfMonth
            } else {
                payDateObj.dayOfMonth
            }
        } catch (e: Exception) { payDateObj.dayOfMonth }

        val isFirstPeriod = startDay <= 15
        val periodString = if (isFirstPeriod) "1-15" else "16-30"

        val monthName = payDateObj.month.name.lowercase().replaceFirstChar { it.titlecase() }
        val displayRange = "$periodString $monthName"

        val netPayValue = try { apiPayroll.netPay.toDoubleOrNull() ?: 0.0 } catch (e: Exception) { 0.0 }

        return Payslip(
            dateRange = displayRange,
            periodIdentifier = periodString, // Use this for API calls
            originalPayDate = apiPayroll.payDate.ifBlank { payDateObj.format(inputFormatter) },
            netAmount = "₱${String.format(Locale.US, "%.2f", netPayValue)}",
            status = apiPayroll.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            month = monthName,
            year = payDateObj.year
        )
    }
}