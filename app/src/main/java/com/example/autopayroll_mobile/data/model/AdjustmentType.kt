package com.example.autopayroll_mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * This represents a single item in the "Sub Type" dropdown.
 * This version matches your database schema.
 */
data class AdjustmentType(
    /**
     * Maps the "adjustment_type_id" (a String/UUID) from your
     * database to the "id" variable in our app.
     */
    @SerializedName("adjustment_type_id")
    val id: String,

    /**
     * Maps the "label" (e.g., "Sick Leave") from your
     * database to the "name" variable in our app.
     */
    @SerializedName("label")
    val name: String,

    /**
     * Maps the "main_type" (e.g., "leave", "payroll")
     * from your database.
     */
    @SerializedName("main_type")
    val mainType: String
)

/**
 * This wrapper class for the API response is unchanged.
 */
data class AdjustmentTypesResponse(
    val data: List<AdjustmentType>
)