package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.composableUI.dashboardUI.DashboardUiState
import com.example.autopayroll_mobile.data.model.Payroll
import com.example.autopayroll_mobile.network.ApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.time.LocalDateTime
import java.time.LocalTime
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

                // --- STEP 2: FETCH ALL DATA PARALLEL ---
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

                    // --- LOGIC: WORKED HOURS / OVERTIME / LATE ---
                    var workedHoursVal = 0.0
                    var overtimeVal = 0.0
                    var lateMins = 0L

                    // 1. EXTRACT SCHEDULE (or default 8am-5pm)
                    val schedule = if (scheduleRes?.success == true) scheduleRes.schedule else null
                    val schedStart = parseTime(schedule?.startTime) ?: LocalTime.of(8, 0)
                    val schedEnd = parseTime(schedule?.endTime) ?: LocalTime.of(17, 0)

                    // 2. CHECK LIVE ATTENDANCE
                    val todayLog = todayRes?.data
                    val clockInRaw = todayLog?.clock_in_time

                    if (clockInRaw != null) {
                        // --- LIVE CALCULATION (User clocked in today) ---
                        try {
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            val clockIn = LocalDateTime.parse(clockInRaw, formatter)

                            // Late: Compare ClockIn vs Schedule Start
                            val shiftStart = clockIn.toLocalDate().atTime(schedStart)
                            if (clockIn.isAfter(shiftStart)) {
                                lateMins = ChronoUnit.MINUTES.between(shiftStart, clockIn)
                            }

                            // Overtime: Compare ClockOut (or Now) vs Schedule End
                            val clockOutRaw = todayLog.clock_out_time
                            val clockOut = if (clockOutRaw != null) {
                                LocalDateTime.parse(clockOutRaw, formatter)
                            } else {
                                LocalDateTime.now()
                            }

                            val shiftEnd = clockIn.toLocalDate().atTime(schedEnd)

                            // Total Duration
                            val totalDurationMinutes = ChronoUnit.MINUTES.between(clockIn, clockOut)
                            workedHoursVal = totalDurationMinutes / 60.0

                            if (clockOut.isAfter(shiftEnd)) {
                                val otMinutes = ChronoUnit.MINUTES.between(shiftEnd, clockOut)
                                if (otMinutes > 0) {
                                    overtimeVal = otMinutes / 60.0
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("DashboardVM", "Live Calc error", e)
                        }
                    } else if (historyRes?.success == true) {
                        // --- HISTORY API DATA ---
                        // Use values DIRECTLY from the API response
                        workedHoursVal = historyRes.total
                        overtimeVal = historyRes.overtime ?: 0.0
                        lateMins = historyRes.late?.toLong() ?: 0L
                    }

                    // --- UPDATE UI ---
                    // 1. Payslip: Sort by payrollDate (descending)
                    val mostRecentPayslip = payrollRes?.data
                        ?.sortedByDescending { it.payrollDate ?: "" }
                        ?.firstOrNull()

                    // 2. Credits & Absences
                    val credits = if (leavesRes?.success == true) leavesRes.creditDays.toString() else "0"
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
                Log.e("DashboardViewModel", "Error fetching dashboard data", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun parseTime(timeStr: String?): LocalTime? {
        if (timeStr.isNullOrBlank()) return null
        return try {
            LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm:ss"))
        } catch (e: Exception) { null }
    }
}