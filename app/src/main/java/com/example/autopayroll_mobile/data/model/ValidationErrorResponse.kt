package com.example.autopayroll_mobile.data.model

/**
 * Models the JSON error response sent by Laravel on a 422 Validation failure.
 */
data class ValidationErrorResponse(
    // This is the general message, e.g., "The given data was invalid."
    val message: String,

    // This holds the specific field errors
    // e.g., "errors": { "start_date": ["The start date must be a date after today."] }
    val errors: Map<String, List<String>>?
)