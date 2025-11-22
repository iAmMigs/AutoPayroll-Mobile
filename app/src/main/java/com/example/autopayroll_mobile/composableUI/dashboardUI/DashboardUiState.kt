package com.example.autopayroll_mobile.composableUI.dashboardUI

import com.example.autopayroll_mobile.data.model.Payroll
import com.example.autopayroll_mobile.data.model.Schedule

data class DashboardUiState(
    val isLoading: Boolean = true,
    val employeeName: String = "Loading...",
    val employeeId: String = "...",
    val jobAndCompany: String = "Loading...",
    val recentPayslip: Payroll? = null,
    val profilePhotoUrl: String? = null,
    val currentSchedule: Schedule? = null,

    // Statistics - stored as Strings to control formatting (no decimals) in ViewModel
    val lastWorkedHours: String = "0", // Regular
    val overtimeHours: String = "0",   // Overtime
    val lateHours: String = "0",       // Late

    val leaveCredits: String = "0",
    val absences: String = "0"
)