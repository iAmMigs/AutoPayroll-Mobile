package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.model.AdjustmentModuleUiState
import com.example.autopayroll_mobile.data.model.AdjustmentType
import com.example.autopayroll_mobile.data.model.FormSubmissionStatus
import com.example.autopayroll_mobile.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class AdjustmentModuleViewModel(private val app: Application) : AndroidViewModel(app) {

    private val apiService = ApiClient.getClient(app.applicationContext)

    private val _uiState = MutableStateFlow(AdjustmentModuleUiState())
    val uiState: StateFlow<AdjustmentModuleUiState> = _uiState.asStateFlow()

    init {
        // Load the initial data needed for the module
        fetchAdjustmentTypes()
        fetchAdjustmentRequests()
    }

    // --- Public API Call Functions ---

    /**
     * Fetches the list of adjustment "sub-types" for the form dropdown.
     */
    fun fetchAdjustmentTypes() {
        Log.d("AdjVM", "Fetching adjustment types...")
        _uiState.update { it.copy(isLoadingTypes = true, typesError = null) }
        viewModelScope.launch {
            try {
                val response = apiService.getAdjustmentTypes()
                _uiState.update {
                    it.copy(
                        isLoadingTypes = false,
                        adjustmentTypes = response.data
                    )
                }
                Log.d("AdjVM", "Fetched ${response.data.size} types")
            } catch (e: Exception) {
                Log.e("AdjVM", "Error fetching types", e)
                _uiState.update {
                    it.copy(isLoadingTypes = false, typesError = "Failed to load types")
                }
            }
        }
    }

    /**
     * Fetches the list of all past requests for the "Track Request" screen.
     */
    fun fetchAdjustmentRequests() {
        Log.d("AdjVM", "Fetching adjustment requests...")
        _uiState.update { it.copy(isLoadingRequests = true, requestsError = null) }
        viewModelScope.launch {
            try {
                val response = apiService.getAdjustmentRequests()
                _uiState.update {
                    it.copy(
                        isLoadingRequests = false,
                        adjustmentRequests = response.data
                    )
                }
                Log.d("AdjVM", "Fetched ${response.data.size} requests")
            } catch (e: Exception) {
                Log.e("AdjVM", "Error fetching requests", e)
                _uiState.update {
                    it.copy(isLoadingRequests = false, requestsError = "Failed to load requests")
                }
            }
        }
    }

    /**
     * Fetches the detailed information for a single, selected request.
     */
    fun fetchAdjustmentRequestDetail(requestId: Int) {
        Log.d("AdjVM", "Fetching detail for request ID: $requestId")
        _uiState.update { it.copy(isLoadingDetail = true, detailError = null, selectedRequestDetail = null) }
        viewModelScope.launch {
            try {
                val response = apiService.getAdjustmentRequestDetail(requestId)
                _uiState.update {
                    it.copy(
                        isLoadingDetail = false,
                        selectedRequestDetail = response.data
                    )
                }
                Log.d("AdjVM", "Fetched detail successfully")
            } catch (e: Exception) {
                Log.e("AdjVM", "Error fetching detail", e)
                _uiState.update {
                    it.copy(isLoadingDetail = false, detailError = "Failed to load details")
                }
            }
        }
    }

    /**
     * Submits the new adjustment request form.
     */
    fun submitAdjustmentRequest() {
        Log.d("AdjVM", "Submitting form...")
        _uiState.update { it.copy(isSubmitting = true, submissionError = null, submissionStatus = FormSubmissionStatus.IDLE) }

        // Get the current state values
        val currentState = _uiState.value

        // --- Data Validation ---
        if (currentState.formSubType == null || currentState.formStartDate.isBlank() || currentState.formEndDate.isBlank() || currentState.formReason.isBlank()) {
            _uiState.update {
                it.copy(isSubmitting = false, submissionError = "Please fill in all required fields.")
            }
            return
        }

        viewModelScope.launch {
            try {
                // --- 1. Prepare Text Data (as RequestBody) ---
                val typePart = currentState.formMainType.toRequestBody("text/plain".toMediaTypeOrNull())
                val subTypePart = currentState.formSubType.name.toRequestBody("text/plain".toMediaTypeOrNull())
                val startDatePart = currentState.formStartDate.toRequestBody("text/plain".toMediaTypeOrNull())
                val endDatePart = currentState.formEndDate.toRequestBody("text/plain".toMediaTypeOrNull())
                val reasonPart = currentState.formReason.toRequestBody("text/plain".toMediaTypeOrNull())

                // Handle optional "hours" field
                val hoursPart = if (currentState.formHours.isNotBlank()) {
                    currentState.formHours.toRequestBody("text/plain".toMediaTypeOrNull())
                } else {
                    null // Send null if it's empty
                }

                // --- 2. Prepare File Data (as MultipartBody.Part) ---
                val filePart = currentState.formAttachment?.let { file ->
                    val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("file", file.name, requestFile)
                }

                // --- 3. Make API Call ---
                val response = apiService.submitAdjustmentRequest(
                    adjustmentType = typePart,
                    subType = subTypePart,
                    startDate = startDatePart,
                    endDate = endDatePart,
                    reason = reasonPart,
                    hours = hoursPart,
                    file = filePart
                )

                if (response.success) {
                    Log.d("AdjVM", "Submission successful: ${response.message}")
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submissionStatus = FormSubmissionStatus.SUCCESS
                        )
                    }
                    // Refresh the list of requests
                    fetchAdjustmentRequests()
                    // Clear the form
                    clearForm()
                } else {
                    Log.w("AdjVM", "Submission failed: ${response.message}")
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submissionError = response.message,
                            submissionStatus = FormSubmissionStatus.ERROR
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e("AdjVM", "Error submitting form", e)
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submissionError = "An unexpected error occurred: ${e.message}",
                        submissionStatus = FormSubmissionStatus.ERROR
                    )
                }
            }
        }
    }


    // --- Public Form State Update Functions ---

    fun onMainTypeChanged(mainType: String) {
        _uiState.update {
            it.copy(
                formMainType = mainType,
                formSubType = null // Reset sub-type when main type changes
            )
        }
    }

    fun onSubTypeChanged(subType: AdjustmentType) {
        _uiState.update { it.copy(formSubType = subType) }
    }

    fun onStartDateChanged(date: String) {
        _uiState.update { it.copy(formStartDate = date) }
    }

    fun onEndDateChanged(date: String) {
        _uiState.update { it.copy(formEndDate = date) }
    }

    fun onHoursChanged(hours: String) {
        _uiState.update { it.copy(formHours = hours) }
    }

    fun onReasonChanged(reason: String) {
        _uiState.update { it.copy(formReason = reason) }
    }

    /**
     * Handles the file Uri received from the file picker.
     * It copies the file to the app's cache to get a real File object.
     */
    fun onAttachmentSelected(uri: Uri) {
        viewModelScope.launch {
            val file = copyUriToCache(uri)
            _uiState.update { it.copy(formAttachment = file) }
        }
    }

    fun onAttachmentRemoved() {
        _uiState.update { it.copy(formAttachment = null) }
    }

    /**
     * Resets all form fields and the submission status.
     */
    fun clearForm() {
        _uiState.update {
            it.copy(
                formMainType = "Leave",
                formSubType = null,
                formStartDate = "",
                formEndDate = "",
                formHours = "",
                formReason = "",
                formAttachment = null,
                submissionStatus = FormSubmissionStatus.IDLE,
                submissionError = null
            )
        }
    }

    // ## NEW ##
    fun onFilterChanged(status: String) {
        _uiState.update { it.copy(filterStatus = status) }
    }

    // --- Helper Function ---

    /**
     * Copies a file from a content Uri (from the file picker) to our app's
     * internal cache directory. This is necessary to convert a Uri into a File
     * object that Retrofit can upload.
     */
    private fun copyUriToCache(uri: Uri): File? {
        return try {
            val contentResolver = app.contentResolver
            val cursor = contentResolver.query(uri, null, null, null, null)
            val name = cursor.use {
                if (it?.moveToFirst() == true) {
                    it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else {
                    "temp_file"
                }
            }

            val file = File(app.cacheDir, name)
            FileOutputStream(file).use { outputStream ->
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file
        } catch (e: Exception) {
            Log.e("AdjVM", "Error copying file from Uri to cache", e)
            null
        }
    }
}