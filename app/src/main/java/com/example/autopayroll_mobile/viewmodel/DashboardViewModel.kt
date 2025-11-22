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

                // Check if company is empty, null, or "N/A"
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
                        try { apiService.getPayrolls() } catch (e: Exception) { null }
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

                    // Process Results
                    val mostRecentPayslip = payrollResponse?.data
                        ?.sortedByDescending { it.payDate }
                        ?.firstOrNull()

                    val schedule = if (scheduleResponse?.success == true) scheduleResponse.schedule else null

                    val workedHours = if (hoursResponse?.success == true) {
                        hoursResponse.total.toInt().toString()
                    } else "0"

                    // ## FIX: Convert to Int to remove decimal ##
                    val credits = if (leavesResponse?.success == true) {
                        leavesResponse.creditDays.toInt().toString()
                    } else "0"

                    val absCount = if (absencesResponse?.success == true) {
                        absencesResponse.count.toString()
                    } else "0"

                    val overtime = "0"
                    val late = "0"

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