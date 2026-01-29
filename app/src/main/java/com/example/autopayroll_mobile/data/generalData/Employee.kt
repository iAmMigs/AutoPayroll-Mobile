package com.example.autopayroll_mobile.data.generalData

import com.google.gson.annotations.SerializedName

data class Employee(
    @SerializedName("employee_id") val employeeId: String,
    @SerializedName("company_id") val companyId: String?,
    @SerializedName("company") val companyName: String?, // Now nullable to be safe
    @SerializedName("profile_photo") val profilePhoto: String?,

    // Personal Info
    @SerializedName("first_name") val firstName: String,
    @SerializedName("middle_name") val middleName: String?,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("suffix") val suffix: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("email") val email: String,
    @SerializedName("phone_number") val phoneNumber: String?,

    // Employment Info
    @SerializedName("job_position") val jobPosition: String,
    @SerializedName("contract_start") val contractStart: String?,
    @SerializedName("contract_end") val contractEnd: String?,
    @SerializedName("employment_type") val employmentType: String,

    // Bio Data
    @SerializedName("birthdate") val birthdate: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("marital_status") val maritalStatus: String,
    @SerializedName("blood_type") val bloodType: String?,

    // Current Address (Updated with new PHP fields)
    @SerializedName("house_number") val houseNumber: String?,
    @SerializedName("street") val street: String?,
    @SerializedName("barangay_name") val barangay: String?,
    @SerializedName("city_name") val city: String?,
    @SerializedName("province_name") val province: String?,
    @SerializedName("region_name") val region: String?,
    @SerializedName("zip") val zip: String?,
    @SerializedName("country") val country: String?,

    // ID Address (Updated with new PHP fields)
    @SerializedName("id_house_number") val idHouseNumber: String?,
    @SerializedName("id_street") val idStreet: String?,
    @SerializedName("id_barangay") val idBarangay: String?,
    @SerializedName("id_city") val idCity: String?,
    @SerializedName("id_province") val idProvince: String?,
    @SerializedName("id_region") val idRegion: String?,
    @SerializedName("id_zip") val idZip: String?,
    @SerializedName("id_country") val idCountry: String?,

    // Government IDs (New fields from PHP)
    @SerializedName("bank_account_number") val bankAccountNumber: String?,
    @SerializedName("sss_number") val sssNumber: String?,
    @SerializedName("phil_health_number") val philHealthNumber: String?,
    @SerializedName("pag_ibig_number") val pagIbigNumber: String?,
    @SerializedName("tin_number") val tinNumber: String?
)