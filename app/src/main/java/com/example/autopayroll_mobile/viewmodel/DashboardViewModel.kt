package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.composableUI.dashboardUI.DashboardUiState
import com.example.autopayroll_mobile.network.ApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

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
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // --- STEP 1: CRITICAL DATA (Sequential) ---
                val employee = apiService.getEmployeeProfile()

                val fullPhotoUrl = employee.profilePhoto?.let { path ->
                    if (path.startsWith("http")) path else "$baseUrl/" + path.removePrefix("/")
                }

                val companyName = employee.companyName
                val displayCompany = if (companyName.isNullOrBlank() || companyName.equals("N/A", ignoreCase = true)) {
                    "No Assigned Company"
                } else {
                    companyName
                }

                _uiState.update {
                    it.copy(
                        employeeName = "${employee.firstName} ${employee.lastName}",
                        employeeId = employee.employeeId,
                        jobAndCompany = "${employee.jobPosition} • $displayCompany",
                        profilePhotoUrl = fullPhotoUrl
                    )
                }

                // --- STEP 2: SECONDARY DATA (Parallel) ---
                supervisorScope {
                    val payrollDeferred = async {
                        try {
                            val response = apiService.getPayrolls()
                            Log.d("DashboardViewModel", "Payroll Response - Success: ${response.success}, Count: ${response.data.size}")
                            response
                        } catch (e: Exception) {
                            Log.e("DashboardViewModel", "Payroll fetch error", e)
                            null
                        }
                    }
                    val scheduleDeferred = async {
                        try { apiService.getSchedule() } catch (e: Exception) { null }
                    }
                    val hoursDeferred = async {
                        try { apiService.getTotalWorkedHours() } catch (e: Exception) { null }
                    }
                    val leavesDeferred = async {
                        try { apiService.getLeaveCredits() } catch (e: Exception) { null }
                    }
                    val absencesDeferred = async {
                        try { apiService.getAbsences() } catch (e: Exception) { null }
                    }

                    val payrollResponse = payrollDeferred.await()
                    val scheduleResponse = scheduleDeferred.await()
                    val hoursResponse = hoursDeferred.await()
                    val leavesResponse = leavesDeferred.await()
                    val absencesResponse = absencesDeferred.await()

                    // CRITICAL FIX: Process payroll results with proper validation
                    val mostRecentPayslip = payrollResponse?.data
                        ?.filter { payroll ->
                            // Filter out invalid entries
                            val isValid = payroll.payDate.isNotBlank() &&
                                    payroll.netPay.isNotBlank()

                            if (!isValid) {
                                Log.w("DashboardViewModel", "Filtered invalid payroll: ID=${payroll.payrollId}, PayDate=${payroll.payDate}")
                            }

                            isValid
                        }
                        ?.sortedByDescending { it.payDate }
                        ?.firstOrNull()

                    if (mostRecentPayslip != null) {
                        Log.d("DashboardViewModel", "Most recent payslip: Date=${mostRecentPayslip.payDate}, Amount=${mostRecentPayslip.netPay}")
                    } else {
                        Log.w("DashboardViewModel", "No valid payslips found")
                    }

                    val schedule = if (scheduleResponse?.success == true) scheduleResponse.schedule else null

                    // Use real values from API
                    val workedHours = if (hoursResponse?.success == true) {
                        hoursResponse.total.toInt().toString()
                    } else "0"

                    val overtime = if (hoursResponse?.success == true) {
                        hoursResponse.overtime.toInt().toString()
                    } else "0"

                    val late = if (hoursResponse?.success == true) {
                        hoursResponse.late.toString()
                    } else "0"

                    val credits = if (leavesResponse?.success == true) {
                        leavesResponse.creditDays.toInt().toString()
                    } else "0"

                    val absCount = if (absencesResponse?.success == true) {
                        absencesResponse.count.toString()
                    } else "0"

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recentPayslip = mostRecentPayslip,
                            currentSchedule = schedule,
                            lastWorkedHours = workedHours,
                            overtimeHours = overtime,
                            lateHours = late,
                            leaveCredits = credits,
                            absences = absCount
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error fetching dashboard data", e)

                val errorText = if (e is retrofit2.HttpException && e.code() == 401) {
                    "Session expired. Please login again."
                } else {
                    "Error: ${e.message}"
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        employeeName = "Error loading data",
                        employeeId = "N/A",
                        jobAndCompany = errorText,
                        recentPayslip = null,
                        currentSchedule = null
                    )
                }
            }
        }
    }
}