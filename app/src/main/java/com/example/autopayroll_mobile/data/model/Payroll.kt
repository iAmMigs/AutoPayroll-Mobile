package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

data class PayrollResponse(
    @SerializedName("data")
    val data: List<Payroll> = emptyList(),

    @SerializedName("success")
    val success: Boolean = false
)

data class Payroll(
    @SerializedName("id", alternate = ["payroll_id"])
    val payrollId: String = "",

    @SerializedName("employee_id")
    val employeeId: String = "",

    @SerializedName("reference")
    val reference: String? = null,

    @SerializedName("net_pay")
    val netPay: String = "0.00",

    @SerializedName("pay_date")
    val payDate: String = "",

    @SerializedName("status")
    val status: String = "pending",

    @SerializedName("start_date")
    val startDate: String? = null,

    @SerializedName("end_date")
    val endDate: String? = null
)