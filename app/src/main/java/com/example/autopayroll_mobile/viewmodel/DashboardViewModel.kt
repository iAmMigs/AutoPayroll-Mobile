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
        // Set loading state but keep existing data if any (for pull-to-refresh)
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // --- STEP 1: CRITICAL DATA (Sequential) ---
                // Fetch Profile FIRST to validate token.
                // If this fails (401), we go to catch block immediately.
                val employee = apiService.getEmployeeProfile()

                val fullPhotoUrl = employee.profilePhoto?.let { path ->
                    if (path.startsWith("http")) path else "$baseUrl/" + path.removePrefix("/")
                }

                // Update UI immediately with Profile info so the user sees they are logged in
                _uiState.update {
                    it.copy(
                        employeeName = "${employee.firstName} ${employee.lastName}",
                        employeeId = employee.employeeId,
                        jobAndCompany = "${employee.jobPosition} • ${employee.companyName}",
                        profilePhotoUrl = fullPhotoUrl
                        // Keep isLoading = true because we are fetching stats next
                    )
                }

                // --- STEP 2: SECONDARY DATA (Parallel) ---
                // Now that we know the token works, fetch the rest safely.
                supervisorScope {
                    // We wrap ALL of these in try-catch so one failure doesn't kill the whole dashboard

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

                    // Await Results
                    val payrollResponse = payrollDeferred.await()
                    val scheduleResponse = scheduleDeferred.await()
                    val hoursResponse = hoursDeferred.await()
                    val leavesResponse = leavesDeferred.await()
                    val absencesResponse = absencesDeferred.await()

                    // --- PROCESS RESULTS ---

                    // 1. Payroll
                    val mostRecentPayslip = payrollResponse?.data
                        ?.sortedByDescending { it.payDate }
                        ?.firstOrNull()

                    // 2. Schedule
                    val schedule = if (scheduleResponse?.success == true) scheduleResponse.schedule else null

                    // 3. Stats Formatting
                    val workedHours = if (hoursResponse?.success == true) {
                        hoursResponse.total.toInt().toString()
                    } else "0"

                    val credits = if (leavesResponse?.success == true) {
                        leavesResponse.creditDays.toString()
                    } else "0"

                    val absCount = if (absencesResponse?.success == true) {
                        absencesResponse.count.toString()
                    } else "0"

                    // Overtime/Late placeholders (update logic if API endpoints exist later)
                    val overtime = "0"
                    val late = "0"

                    // Update Final State
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

                // Only overwrite name/job if we really failed to load the profile
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        employeeName = "Error loading data",
                        employeeId = "N/A",
                        jobAndCompany = errorText,
                        // Reset data on critical error
                        recentPayslip = null,
                        currentSchedule = null
                    )
                }
            }
        }
    }
}