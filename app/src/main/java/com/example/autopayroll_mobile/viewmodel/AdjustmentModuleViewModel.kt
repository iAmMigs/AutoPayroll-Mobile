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
import com.example.autopayroll_mobile.data.model.AdjustmentSubmitResponse
import com.example.autopayroll_mobile.network.ApiClient
// ## 1. ADD THIS IMPORT ##
import com.example.autopayroll_mobile.network.PublicApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class AdjustmentModuleViewModel(private val app: Application) : AndroidViewModel(app) {

    // ## 2. CREATE TWO SEPARATE API SERVICES ##
    // For routes that NEED an auth token
    private val authService = ApiClient.getClient(app.applicationContext)
    // For routes that MUST NOT have an auth token
    private val publicService = PublicApiClient.getService()

    private val _uiState = MutableStateFlow(AdjustmentModuleUiState())
    val uiState: StateFlow<AdjustmentModuleUiState> = _uiState.asStateFlow()

    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        Log.d("AdjVM", "Fetching initial data...")
        _uiState.update { it.copy(isLoading = true, pageError = null) }

        viewModelScope.launch {
            try {
                // 1. Get Employee (uses AUTH service)
                val employee = authService.getEmployeeProfile()
                _uiState.update { it.copy(employeeId = employee.employeeId) }

                // 2. Fetch all requests (uses AUTH service)
                fetchAdjustmentRequests()

                // 3. Fetch pending requests (uses AUTH service)
                fetchPendingAdjustments()

                // 4. Fetch default types (uses PUBLIC service)
                fetchAdjustmentTypes(mainType = "leave", isInitialLoad = true)

            } catch (e: Exception) {
                Log.e("AdjVM", "Error fetching initial data", e)
                _uiState.update {
                    it.copy(isLoading = false, pageError = "Failed to load data: ${e.message}")
                }
            }
        }
    }

    private fun fetchAdjustmentRequests() {
        viewModelScope.launch {
            try {
                // Use AUTH service
                val response = authService.getAdjustmentRequests()
                if (response.success) {
                    _uiState.update {
                        it.copy(adjustmentRequests = response.data)
                    }
                    Log.d("AdjVM", "Fetched ${response.data.size} total requests")
                }
            } catch (e: Exception) {
                Log.e("AdjVM", "Error fetching adjustment requests", e)
                _uiState.update {
                    it.copy(pageError = "Failed to load request list: ${e.message}")
                }
            }
        }
    }

    private fun fetchPendingAdjustments() {
        viewModelScope.launch {
            try {
                // Use AUTH service
                val response = authService.getPendingAdjustments()
                if (response.success) {
                    _uiState.update {
                        it.copy(pendingRequests = response.data)
                    }
                    Log.d("AdjVM", "Fetched ${response.data.size} pending requests")
                }
            } catch (e: Exception) {
                Log.e("AdjVM", "Error fetching pending requests", e)
            }
        }
    }

    private fun fetchAdjustmentTypes(mainType: String, isInitialLoad: Boolean = false) {
        Log.d("AdjVM", "Fetching types for: $mainType")
        _uiState.update { it.copy(isLoadingTypes = true) }

        viewModelScope.launch {
            try {
                // ## 3. USE THE PUBLIC SERVICE ##
                val response = publicService.getAdjustmentTypes(mainType.lowercase())
                if (response.success) {
                    _uiState.update {
                        it.copy(
                            isLoadingTypes = false,
                            adjustmentTypes = response.data,
                            isLoading = if (isInitialLoad) false else it.isLoading
                        )
                    }
                    Log.d("AdjVM", "Fetched ${response.data.size} types for $mainType")
                } else {
                    handleTypeFetchError(mainType, response.message ?: "No types found", isInitialLoad)
                }
            } catch (e: Exception) {
                Log.e("AdjVM", "Error fetching types for $mainType", e)
                handleTypeFetchError(mainType, "Error: ${e.message}", isInitialLoad)
            }
        }
    }

    private fun handleTypeFetchError(mainType: String, message: String, isInitialLoad: Boolean) {
        _uiState.update {
            it.copy(
                isLoadingTypes = false,
                adjustmentTypes = emptyList(),
                pageError = if (isInitialLoad) "Failed to load default types: $message" else it.pageError,
                isLoading = if (isInitialLoad) false else it.isLoading
            )
        }
    }

    fun submitAdjustmentRequest() {
        val currentState = _uiState.value
        val employeeId = currentState.employeeId
        val subType = currentState.formSubType

        Log.d("AdjVM", "Submitting form...")
        _uiState.update { it.copy(isSubmitting = true, submissionError = null, submissionStatus = FormSubmissionStatus.IDLE) }

        if (employeeId == null) {
            _uiState.update { it.copy(isSubmitting = false, submissionError = "Employee ID not found. Cannot submit.") }
            return
        }
        if (subType == null || currentState.formReason.isBlank()) {
            _uiState.update {
                it.copy(isSubmitting = false, submissionError = "Please fill in all required fields.")
            }
            return
        }

        viewModelScope.launch {
            try {
                val employeeIdPart = employeeId.toRequestBody("text/plain".toMediaTypeOrNull())
                val mainTypePart = currentState.formMainType.toRequestBody("text/plain".toMediaTypeOrNull())
                val subTypePart = subType.code.toRequestBody("text/plain".toMediaTypeOrNull())
                val reasonPart = currentState.formReason.toRequestBody("text/plain".toMediaTypeOrNull())

                val startDatePart = currentState.formStartDate.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())
                val endDatePart = currentState.formEndDate.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())

                val affectedDatePart = null

                val filePart = currentState.formAttachment?.let { file ->
                    val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("attachment", file.name, requestFile)
                }

                // ## 4. USE THE AUTH SERVICE ##
                val response = authService.submitAdjustmentRequest(
                    employeeId = employeeIdPart,
                    mainType = mainTypePart,
                    subtype = subTypePart,
                    startDate = startDatePart,
                    endDate = endDatePart,
                    affectedDate = affectedDatePart,
                    reason = reasonPart,
                    attachment = filePart
                )

                if (response.success) {
                    Log.d("AdjVM", "Submission successful")
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submissionStatus = FormSubmissionStatus.SUCCESS
                        )
                    }
                    fetchAdjustmentRequests()
                    fetchPendingAdjustments()
                } else {
                    val errorMsg = response.errors?.entries?.firstOrNull()?.value?.firstOrNull() ?: response.message ?: "Submission failed"
                    Log.w("AdjVM", "Submission failed: $errorMsg")
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submissionError = errorMsg,
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

    // --- Form State & Navigation Functions ---
    fun onMainTypeChanged(mainType: String) {
        val apiMainType = mainType.lowercase()
        _uiState.update {
            it.copy(
                formMainType = apiMainType,
                formSubType = null
            )
        }
        fetchAdjustmentTypes(apiMainType)
    }

    fun selectRequestById(requestId: String) {
        val request = _uiState.value.adjustmentRequests.find { it.id == requestId }
        _uiState.update { it.copy(selectedRequest = request) }
    }

    fun clearSelectedRequest() {
        _uiState.update { it.copy(selectedRequest = null) }
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

    fun onReasonChanged(reason: String) {
        _uiState.update { it.copy(formReason = reason) }
    }

    fun onAttachmentSelected(uri: Uri) {
        viewModelScope.launch {
            val file = copyUriToCache(uri)
            _uiState.update { it.copy(formAttachment = file) }
        }
    }

    fun onAttachmentRemoved() {
        _uiState.update { it.copy(formAttachment = null) }
    }

    fun clearForm() {
        _uiState.update {
            it.copy(
                formMainType = "leave",
                formSubType = null,
                formStartDate = "",
                formEndDate = "",
                formReason = "",
                formAttachment = null,
                submissionStatus = FormSubmissionStatus.IDLE,
                submissionError = null
            )
        }
        fetchAdjustmentTypes("leave")
    }

    fun onFilterChanged(status: String) {
        _uiState.update { it.copy(filterStatus = status) }
    }

    // --- Helper Function ---
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