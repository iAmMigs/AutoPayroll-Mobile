package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.model.Payroll
import com.example.autopayroll_mobile.data.model.Payslip
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.utils.SessionManager
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
    private val sessionManager = SessionManager(application.applicationContext)

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
                listErrorMessage = if (filtered.isEmpty() && it.allPayslips.isNotEmpty()) "No payslips found for $year." else null
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
                // Fetch directly from the corrected /api/payroll/view endpoint
                val payrollResponse = apiService.getPayrolls()

                val validPayrolls = payrollResponse.data

                // Map database records directly to UI models
                val realPayslips = validPayrolls.mapNotNull { apiPayroll ->
                    try { mapToPayslipUiModel(apiPayroll) } catch (e: Exception) { null }
                }

                // Sort newest records first
                val sortedPayslips = realPayslips.sortedWith(
                    compareByDescending<Payslip> { it.year }
                        .thenByDescending { it.downloadMonth }
                        .thenByDescending { it.downloadPeriod == "16-30" }
                )

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
                // To help with debugging in the future, we can print the actual error to the console
                e.printStackTrace()
                val msg = if (e is HttpException && e.code() == 401) "Session expired." else "Unable to load payslips."
                _uiState.update { it.copy(isLoading = false, listErrorMessage = msg) }
            }
        }
    }

    fun viewPayslipPdf(payslip: Payslip) {
        if (_uiState.value.isPdfLoading) return

        _uiState.update { it.copy(isPdfLoading = true) }
        showToast("Requesting PDF...")

        viewModelScope.launch {
            try {
                val response = apiService.downloadPayslip(
                    year = payslip.downloadYear,
                    month = payslip.downloadMonth,
                    period = payslip.downloadPeriod
                )

                if (response.isSuccessful && response.body() != null) {
                    val fileName = "Payslip_${payslip.downloadYear}_${payslip.downloadMonth}_${payslip.downloadPeriod}.pdf"
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

                    withContext(Dispatchers.Main) {
                        openPdfIntent(file)
                    }
                } else {
                    showToast("Download failed: ${response.code()}")
                }

            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            } finally {
                _uiState.update { it.copy(isPdfLoading = false) }
            }
        }
    }

    private fun openPdfIntent(file: File) {
        try {
            val context = getApplication<Application>()
            val authority = "${context.packageName}.provider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val chooser = Intent.createChooser(intent, "Open Payslip")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            showToast("No PDF Viewer app installed.")
        }
    }

    private fun showToast(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun mapToPayslipUiModel(apiPayroll: Payroll): Payslip {
        return Payslip(
            dateRange = apiPayroll.period, // Directly uses "1-15" or "16-30" from DB
            referenceId = apiPayroll.reference ?: "#PAY-PENDING",
            originalPayDate = apiPayroll.payDate ?: "N/A",
            // Safely parse the decimal from the DB into a formatted string
            netAmount = "₱${String.format(Locale.US, "%.2f", apiPayroll.netPay.toDoubleOrNull() ?: 0.0)}",
            status = apiPayroll.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },

            downloadPeriod = apiPayroll.period,
            downloadYear = apiPayroll.year,
            downloadMonth = apiPayroll.month,
            year = apiPayroll.year
        )
    }
}