package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

data class Company(
    @SerializedName("company_id") val companyId: String,
    @SerializedName("company_name") val companyName: String
    // Add other fields like 'address' if you need them
)