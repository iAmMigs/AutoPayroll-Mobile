package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

// Matches: { "data": [...], "success": true }
data class PayrollResponse(
    @SerializedName("data")
    val data: List<Payroll>?, // Made nullable to prevent crashes if key is missing

    @SerializedName("success")
    val success: Boolean
)

// Matches a single payroll object from your database
data class Payroll(
    @SerializedName("payroll_id", alternate = ["id"])
    val payrollId: String?,

    @SerializedName("employee_id")
    val employeeId: String?,

    @SerializedName("payroll_period_id")
    val payrollPeriodId: String?,

    @SerializedName("net_pay")
    val netPay: String?,

    @SerializedName("pay_date")
    val payDate: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("gross_salary")
    val grossSalary: String?,

    @SerializedName("pag_ibig_deductions")
    val pagIbigDeductions: String?,

    @SerializedName("phil_health_deductions")
    val philHealthDeductions: String?,

    @SerializedName("sss_deductions")
    val sssDeductions: String?,

    // NEW: Path to the PDF file (e.g., "payrolls/sample.pdf")
    @SerializedName("file_path", alternate = ["attachment", "pdf_path"])
    val filePath: String?
)