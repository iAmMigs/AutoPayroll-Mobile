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
    @SerializedName("id", alternate = ["payroll_id"])
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

    @SerializedName("start_date", alternate = ["clock_in_date"])
    val startDate: String? = null,

    @SerializedName("end_date", alternate = ["clock_out_date"])
    val endDate: String? = null,

    @SerializedName("breakdown")
    val breakdown: String? = null
)