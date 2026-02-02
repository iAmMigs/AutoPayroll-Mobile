package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

// Matches: { "data": [...], "success": true }
data class PayrollResponse(
    @SerializedName("data")
    val data: List<Payroll>?,
    @SerializedName("success")
    val success: Boolean
)

// Matches the new structure from PayrollController (2).php
data class Payroll(
    @SerializedName("payroll_id")
    val payrollId: String?,

    @SerializedName("employee_id")
    val employeeId: String?,

    @SerializedName("employee_name")
    val employeeName: String?,

    @SerializedName("net_salary") // Changed from net_pay
    val netSalary: String?,

    @SerializedName("gross_salary")
    val grossSalary: String?,

    @SerializedName("payroll_date") // Changed from pay_date
    val payrollDate: String?,

    // The new API returns a single "deductions" field (likely total)
    @SerializedName("deductions")
    val deductions: String?,

    @SerializedName("overtime")
    val overtime: String?,

    @SerializedName("night_differential")
    val nightDifferential: String?,

    @SerializedName("holiday_pay")
    val holidayPay: String?,

    @SerializedName("late_time")
    val lateTime: String?,

    @SerializedName("work_hours")
    val workHours: String?,

    // Attendance details for the payroll period
    @SerializedName("clock_in_date")
    val clockInDate: String?,

    @SerializedName("clock_in_time")
    val clockInTime: String?,

    @SerializedName("clock_out_date")
    val clockOutDate: String?,

    @SerializedName("clock_out_time")
    val clockOutTime: String?

    // Note: 'file_path' and 'status' are no longer returned by the new API.
)