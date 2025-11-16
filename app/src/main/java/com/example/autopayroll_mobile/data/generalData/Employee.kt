package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

data class Employee(
    @SerializedName("employee_id") val employeeId: String,
    @SerializedName("company") val companyName: String,
    @SerializedName("profile_photo") val profilePhoto: String?,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("middle_name") val middleName: String?,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("suffix") val suffix: String?,
    @SerializedName("email") val email: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("job_position") val jobPosition: String,
    @SerializedName("company_id") val companyId: String,
    @SerializedName("contract_start") val contractStart: String,
    @SerializedName("employment_type") val employmentType: String,
    @SerializedName("birthdate") val birthdate: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("marital_status") val maritalStatus: String,
    @SerializedName("blood_type") val bloodType: String?,
    @SerializedName("street") val street: String?,
    @SerializedName("barangay_name") val barangay: String?,
    @SerializedName("city_name") val city: String?,
    @SerializedName("province_name") val province: String?,
    @SerializedName("zip") val zip: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("id_street") val idStreet: String?,
    @SerializedName("id_barangay") val idBarangay: String?,
    @SerializedName("id_city") val idCity: String?,
    @SerializedName("id_province") val idProvince: String?,
    @SerializedName("id_country") val idCountry: String?,
    @SerializedName("religion_name") val religion: String?,
    @SerializedName("available_leaves") val availableLeaves: Int,
    @SerializedName("used_leaves") val usedLeaves: Int
)