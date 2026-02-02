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
import retrofit2.HttpException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
                // --- STEP 1: EMPLOYEE PROFILE ---
                val employee = apiService.getEmployeeProfile()
                val fullPhotoUrl = employee.profilePhoto?.let { path ->
                    if (path.startsWith("http")) path else "$baseUrl/" + path.removePrefix("/")
                }
                val companyName = if (employee.companyName.isNullOrBlank() || employee.companyName.equals("N/A", true)) "No Assigned Company" else employee.companyName

                _uiState.update {
                    it.copy(
                        employeeName = "${employee.firstName} ${employee.lastName}",
                        employeeId = employee.employeeId,
                        jobAndCompany = "${employee.jobPosition} • $companyName",
                        profilePhotoUrl = fullPhotoUrl
                    )
                }

                // --- STEP 2: FETCH ALL DATA ---
                supervisorScope {
                    val payrollDef = async { try { apiService.getPayrolls() } catch (e: Exception) { null } }
                    val scheduleDef = async { try { apiService.getSchedule() } catch (e: Exception) { null } }
                    val leavesDef = async { try { apiService.getLeaveCredits() } catch (e: Exception) { null } }
                    val absencesDef = async { try { apiService.getAbsences() } catch (e: Exception) { null } }
                    val historyDef = async { try { apiService.getTotalWorkedHours() } catch (e: Exception) { null } }
                    val todayDef = async { try { apiService.getTodayAttendance() } catch (e: Exception) { null } }

                    val payrollRes = payrollDef.await()
                    val scheduleRes = scheduleDef.await()
                    val leavesRes = leavesDef.await()
                    val absencesRes = absencesDef.await()
                    val historyRes = historyDef.await()
                    val todayRes = todayDef.await()

                    // --- CALCULATION LOGIC ---
                    var workedHoursVal = 0.0
                    var overtimeVal = 0.0
                    var lateMins = 0L

                    // 1. SAFE NULL HANDLING to prevent "String? but String expected" crash
                    val todayLog = todayRes?.data
                    val clockInRaw = todayLog?.clock_in_time // Extracts the nullable string safely

                    if (clockInRaw != null) {
                        // LIVE DATA: User is currently clocked in
                        try {
                            // Laravel Format: "2026-02-02 08:15:00"
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            val clockIn = LocalDateTime.parse(clockInRaw, formatter)

                            // LATE CALCULATION (vs 8:00 AM)
                            val startShift = clockIn.toLocalDate().atTime(8, 0)
                            if (clockIn.isAfter(startShift)) {
                                lateMins = ChronoUnit.MINUTES.between(startShift, clockIn)
                            }

                            // OVERTIME CALCULATION
                            val clockOutRaw = todayLog.clock_out_time
                            val clockOut = if (clockOutRaw != null) {
                                LocalDateTime.parse(clockOutRaw, formatter)
                            } else {
                                LocalDateTime.now() // Still working, calculate duration so far
                            }

                            val durationMinutes = ChronoUnit.MINUTES.between(clockIn, clockOut)
                            val totalHours = durationMinutes / 60.0

                            // Rule: > 9 Hours is Overtime
                            if (totalHours > 9.0) {
                                overtimeVal = totalHours - 9.0
                                workedHoursVal = 9.0
                            } else {
                                workedHoursVal = totalHours
                            }

                        } catch (e: Exception) {
                            Log.e("DashboardVM", "Calc error", e)
                        }
                    } else if (historyRes?.success == true) {
                        // HISTORY DATA: User hasn't clocked in today, show last log
                        workedHoursVal = historyRes.total

                        // Fallback logic if API doesn't send OT/Late yet
                        if (workedHoursVal > 9.0) {
                            overtimeVal = workedHoursVal - 9.0
                            workedHoursVal = 9.0
                        }
                        // Late cannot be determined from just "total hours" in history
                    }

                    // --- UPDATE UI ---
                    val mostRecentPayslip = payrollRes?.data?.sortedByDescending { it.payDate }?.firstOrNull()
                    val schedule = if (scheduleRes?.success == true) scheduleRes.schedule else null
                    val credits = if (leavesRes?.success == true) leavesRes.creditDays.toInt().toString() else "0"
                    val absCount = if (absencesRes?.success == true) absencesRes.count.toString() else "0"

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recentPayslip = mostRecentPayslip,
                            currentSchedule = schedule,
                            lastWorkedHours = String.format("%.1f", workedHoursVal),
                            overtimeHours = String.format("%.1f", overtimeVal),
                            lateHours = lateMins.toString(),
                            leaveCredits = credits,
                            absences = absCount
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error", e)
            }
        }
    }
}