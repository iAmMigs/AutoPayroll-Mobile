package com.example.autopayroll_mobile.composableUI.dashboardUI

import com.example.autopayroll_mobile.data.model.Employee
import com.example.autopayroll_mobile.data.model.Payroll

sealed interface DashboardUiState {

    // Represents the loading state
    object Loading : DashboardUiState

    // Represents a successful state, holding the data
    data class Success(
        val employee: Employee?, // Placeholder for employee data
        val mostRecentPayslip: Payroll? // The single most recent payslip
        // Add other data as needed, e.g., val attendanceStats: ...
    ) : DashboardUiState

    // Represents an error state
    data class Error(val message: String) : DashboardUiState
}