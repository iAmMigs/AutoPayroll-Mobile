package com.example.autopayroll_mobile.data.AdjustmentModule

import java.io.File

/**
 * Represents the complete state for the entire Adjustment Module.
 * UPDATED for new API.
 */
data class AdjustmentModuleUiState(
    // --- Data States ---
    val employeeId: String? = null, // For submitting the form
    val adjustmentTypes: List<AdjustmentType> = emptyList(), // For the dropdown
    val adjustmentRequests: List<AdjustmentRequest> = emptyList(), // Master list of all requests
    val pendingRequests: List<AdjustmentRequest> = emptyList(), // For the hub count
    val selectedRequest: AdjustmentRequest? = null, // The single request for the detail screen

    // --- Loading States ---
    val isLoading: Boolean = true, // A single state for initial data load
    val isLoadingTypes: Boolean = false, // For the dropdown spinner
    val isSubmitting: Boolean = false,

    // --- Error States ---
    val pageError: String? = null, // For general load failures
    val submissionError: String? = null,

    // --- Form Input State ---
    val formMainType: String = "leave", // "leave", "attendance", "payroll"
    val formSubType: AdjustmentType? = null,
    val formStartDate: String = "",
    val formEndDate: String = "",
    val formAffectedDate: String = "", // ## NEWLY ADDED ##
    val formReason: String = "",
    val formAttachment: File? = null,

    // --- Submission State ---
    val submissionStatus: FormSubmissionStatus = FormSubmissionStatus.IDLE,

    // --- Filter State ---
    val filterStatus: String = "All"
)

/**
 * Enum to track the result of the form submission
 */
enum class FormSubmissionStatus {
    IDLE,
    SUCCESS,
    ERROR
}