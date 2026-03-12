package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

data class PayrollResponse(
    @SerializedName("data")
    val data: List<Payroll> = emptyList(),

    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null
)

data class Payroll(
    @SerializedName("payslips_id", alternate = ["id", "payroll_id"])
    val payrollId: String = "",

    @SerializedName("employee_id")
    val employeeId: String = "",

    @SerializedName("reference")
    val reference: String? = null,

    @SerializedName("net_pay", alternate = ["net_salary"])
    val netPay: String = "0.00",

    @SerializedName("pay_date", alternate = ["payroll_date"])
    val payDate: String = "",

    @SerializedName("status")
    val status: String = "Released",

    @SerializedName("period_start", alternate = ["start_date", "clock_in_date"])
    val startDate: String? = null,

    @SerializedName("period_end", alternate = ["end_date", "clock_out_date"])
    val endDate: String? = null,

    // --- NEW: Read these exactly from the Payslip table ---
    @SerializedName("period")
    val period: String? = null,

    @SerializedName("year")
    val year: Int? = null,

    @SerializedName("month")
    val month: Int? = null,

    @SerializedName("breakdown")
    val breakdown: String? = null
)