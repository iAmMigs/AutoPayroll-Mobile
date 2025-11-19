package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.composableUI.dashboardUI.DashboardUiState
import com.example.autopayroll_mobile.data.generalData.Employee
import com.example.autopayroll_mobile.data.model.Payroll
import com.example.autopayroll_mobile.data.model.PayrollResponse
import com.example.autopayroll_mobile.data.model.Schedule
import com.example.autopayroll_mobile.network.ApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                // 1. Main Profile & Payroll
                val employeeDeferred = async { apiService.getEmployeeProfile() }
                val payrollDeferred = async { apiService.getPayrolls() }

                // 2. Independent Stats
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

                // Await Data
                val employee = employeeDeferred.await()
                val payrollResponse = payrollDeferred.await()
                val scheduleResponse = scheduleDeferred.await()
                val hoursResponse = hoursDeferred.await()
                val leavesResponse = leavesDeferred.await()
                val absencesResponse = absencesDeferred.await()

                // Process Data
                val mostRecentPayslip = payrollResponse.data
                    .sortedByDescending { it.payDate }
                    .firstOrNull()

                val fullPhotoUrl = employee.profilePhoto?.let { path ->
                    if (path.startsWith("http")) path else "$baseUrl/" + path.removePrefix("/")
                }

                val schedule = if (scheduleResponse?.success == true) scheduleResponse.schedule else null

                // --- FORMATTING LOGIC: Use .toInt() to remove decimals ---
                val workedHours = if (hoursResponse?.success == true) {
                    hoursResponse.total.toInt().toString()
                } else "0"

                // Placeholder logic for Overtime/Late (formatted as Ints)
                val overtime = "0"
                val late = "0"

                val credits = if (leavesResponse?.success == true) {
                    leavesResponse.creditDays.toString()
                } else "0"

                val absCount = if (absencesResponse?.success == true) {
                    absencesResponse.count.toString()
                } else "0"


                _uiState.value = DashboardUiState(
                    isLoading = false,
                    employeeName = "${employee.firstName} ${employee.lastName}",
                    employeeId = employee.employeeId,
                    jobAndCompany = "${employee.jobPosition} • ${employee.companyName}",
                    recentPayslip = mostRecentPayslip,
                    profilePhotoUrl = fullPhotoUrl,
                    currentSchedule = schedule,
                    // Set formatted strings
                    lastWorkedHours = workedHours,
                    overtimeHours = overtime,
                    lateHours = late,
                    leaveCredits = credits,
                    absences = absCount
                )

            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error fetching dashboard data", e)
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    employeeName = "Error loading data",
                    employeeId = "N/A",
                    jobAndCompany = "Error: ${e.message}",
                    recentPayslip = null,
                    profilePhotoUrl = null,
                    currentSchedule = null
                )
            }
        }
    }
}