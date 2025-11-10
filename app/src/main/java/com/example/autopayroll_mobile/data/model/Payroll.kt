package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

// This class matches the main JSON response: { "data": [...], "success": true }
data class PayrollResponse(
    @SerializedName("data")
    val data: List<Payroll>,

    @SerializedName("success")
    val success: Boolean
)

// This class matches a single payroll object from your database
data class Payroll(
    @SerializedName("payroll_id")
    val payrollId: String,

    @SerializedName("employee_id")
    val employeeId: String,

    @SerializedName("payroll_period_id")
    val payrollPeriodId: String,

    @SerializedName("net_pay")
    val netPay: String, // Kept as String for easy display, matches decimal(10,2)

    @SerializedName("pay_date")
    val payDate: String, // Matches date type, e.g., "2025-11-15"

    @SerializedName("status")
    val status: String, // "released" or "processing"

    // Add any other fields you might need from the API
    @SerializedName("gross_salary")
    val grossSalary: String,

    @SerializedName("pag_ibig_deductions")
    val pagIbigDeductions: String,

    @SerializedName("phil_health_deductions")
    val philHealthDeductions: String,

    @SerializedName("sss_deductions")
    val sssDeductions: String
)