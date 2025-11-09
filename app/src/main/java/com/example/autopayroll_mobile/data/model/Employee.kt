package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

data class Employee(
    @SerializedName("employee_id") val employeeId: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("job_position") val jobPosition: String,
    @SerializedName("company_id") val companyId: String
)