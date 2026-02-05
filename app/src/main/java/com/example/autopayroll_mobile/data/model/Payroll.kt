package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

data class PayrollResponse(
    @SerializedName("data")
    val data: List<Payroll> = emptyList(),

    @SerializedName("success")
    val success: Boolean = false
)

data class Payroll(
    // Using 'alternate' to accept either 'id' or 'payroll_id'
    @SerializedName("id", alternate = ["payroll_id"])
    val payrollId: String = "",

    @SerializedName("employee_id")
    val employeeId: String = "",

    @SerializedName("net_pay")
    val netPay: String = "0.00",

    // CRITICAL FIX: Made non-nullable with default value to prevent crashes
    @SerializedName("pay_date")
    val payDate: String = "",

    @SerializedName("status")
    val status: String = "pending",

    @SerializedName("start_date")
    val startDate: String? = null,

    @SerializedName("end_date")
    val endDate: String? = null
)