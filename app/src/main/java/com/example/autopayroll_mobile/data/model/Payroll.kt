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
    @SerializedName("payslips_id")
    val payslipsId: String = "",

    @SerializedName("reference")
    val reference: String? = null,

    @SerializedName("employee_id")
    val employeeId: String = "",

    @SerializedName("year")
    val year: Int = 0,

    @SerializedName("month")
    val month: Int = 0,

    @SerializedName("period")
    val period: String = "",

    @SerializedName("period_start")
    val periodStart: String? = null,

    @SerializedName("period_end")
    val periodEnd: String? = null,

    @SerializedName("pay_date")
    val payDate: String? = null,

    @SerializedName("net_pay")
    val netPay: String = "0.00",

    @SerializedName("status")
    val status: String = "pending",

    @SerializedName("breakdown")
    val breakdown: String? = null
)