package com.example.autopayroll_mobile.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autopayroll_mobile.data.AdjustmentModule.AdjustmentModuleUiState
import com.example.autopayroll_mobile.data.AdjustmentModule.AdjustmentType
import com.example.autopayroll_mobile.data.AdjustmentModule.FormSubmissionStatus
import com.example.autopayroll_mobile.network.ApiClient
import com.example.autopayroll_mobile.network.PublicApiClient
import com.google.gson.JsonSyntaxException
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

sealed class AdjustmentNavigationEvent {
    object NavigateBackToMenu : AdjustmentNavigationEvent()
}

class AdjustmentModuleViewModel(private val app: Application) : AndroidViewModel(app) {

    private val authService = ApiClient.getClient(app.applicationContext)
    private val publicService = PublicApiClient.getService()

    private val _uiState = MutableStateFlow(AdjustmentModuleUiState())
    val uiState: StateFlow<AdjustmentModuleUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<AdjustmentNavigationEvent?>(null)
    val navigationEvent: StateFlow<AdjustmentNavigationEvent?> = _navigationEvent.asStateFlow()

    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        _uiState.update { it.copy(isLoading = true, pageError = null) }
        viewModelScope.launch {
            try {
                val employee = authService.getEmployeeProfile()
                _uiState.update { it.copy(employeeId = employee.employeeId) }
                fetchAdjustmentRequests()
                fetchAdjustmentTypes(mainType = "leave", isInitialLoad = true)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, pageError = "Failed to load data: ${e.message}") }
            }
        }
    }

    private fun fetchAdjustmentRequests() {
        viewModelScope.launch {
            try {
                val response = authService.getAdjustmentRequests()
                if (response.success) {
                    _uiState.update { it.copy(adjustmentRequests = response.data, isLoading = false) }
                }
            } catch (e: Exception) {
                if (e is JsonSyntaxException || e is IllegalStateException) {
                    _uiState.update { it.copy(adjustmentRequests = emptyList(), isLoading = false) }
                } else {
                    _uiState.update { it.copy(pageError = "Failed to load requests: ${e.message}", isLoading = false) }
                }
            }
        }
    }

    private fun fetchAdjustmentTypes(mainType: String, isInitialLoad: Boolean = false) {
        val typeToQuery = mainType.lowercase()
        _uiState.update { it.copy(isLoadingTypes = true) }
        viewModelScope.launch {
            try {
                val response = publicService.getAdjustmentTypes(typeToQuery)
                if (response.success) {
                    _uiState.update {
                        it.copy(
                            isLoadingTypes = false,
                            adjustmentTypes = response.data,
                            isLoading = if (isInitialLoad) false else it.isLoading
                        )
                    }
                } else {
                    handleTypeFetchError(mainType, response.message ?: "No types", isInitialLoad)
                }
            } catch (e: Exception) {
                handleTypeFetchError(mainType, "Error: ${e.message}", isInitialLoad)
            }
        }
    }

    private fun handleTypeFetchError(mainType: String, message: String, isInitialLoad: Boolean) {
        _uiState.update {
            it.copy(
                isLoadingTypes = false,
                adjustmentTypes = emptyList(),
                pageError = if (isInitialLoad) "Failed types: $message" else it.pageError,
                isLoading = if (isInitialLoad) false else it.isLoading
            )
        }
    }

    fun submitAdjustmentRequest() {
        val currentState = _uiState.value
        val employeeId = currentState.employeeId
        val subType = currentState.formSubType

        _uiState.update { it.copy(isSubmitting = true, submissionError = null, submissionStatus = FormSubmissionStatus.IDLE) }

        if (employeeId == null || subType == null || currentState.formReason.isBlank()) {
            _uiState.update { it.copy(isSubmitting = false, submissionError = "Missing required fields") }
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
                val affectedDatePart = currentState.formAffectedDate.takeIf { it.isNotBlank() }?.toRequestBody("text/plain".toMediaTypeOrNull())

                val filePart = currentState.formAttachment?.let { file ->
                    MultipartBody.Part.createFormData("attachment", file.name, file.asRequestBody("application/octet-stream".toMediaTypeOrNull()))
                }

                val response = authService.submitAdjustmentRequest(
                    employeeIdPart, mainTypePart, subTypePart, startDatePart, endDatePart, affectedDatePart, reasonPart, filePart
                )

                if (response.success) {
                    _uiState.update { it.copy(isSubmitting = false, submissionStatus = FormSubmissionStatus.SUCCESS) }
                    fetchAdjustmentRequests()
                } else {
                    _uiState.update { it.copy(isSubmitting = false, submissionError = response.message ?: "Failed", submissionStatus = FormSubmissionStatus.ERROR) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, submissionError = "Error: ${e.message}", submissionStatus = FormSubmissionStatus.ERROR) }
            }
        }
    }

    fun onMainTypeChanged(mainType: String) {
        _uiState.update { it.copy(formMainType = mainType.lowercase(), formSubType = null) }
        fetchAdjustmentTypes(mainType.lowercase())
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

    fun onAffectedDateChanged(date: String) {
        _uiState.update { it.copy(formAffectedDate = date) }
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
                formMainType = "leave", formSubType = null, formStartDate = "", formEndDate = "",
                formAffectedDate = "", formReason = "", formAttachment = null,
                submissionStatus = FormSubmissionStatus.IDLE, submissionError = null
            )
        }
        fetchAdjustmentTypes("leave")
    }

    fun onFilterChanged(status: String) {
        _uiState.update { it.copy(filterStatus = status) }
    }

    private fun copyUriToCache(uri: Uri): File? {
        return try {
            val cursor = app.contentResolver.query(uri, null, null, null, null)
            val name = cursor?.use {
                if (it.moveToFirst()) it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) else "temp"
            } ?: "temp"
            val file = File(app.cacheDir, name)
            FileOutputStream(file).use { out ->
                app.contentResolver.openInputStream(uri)?.use { inp -> inp.copyTo(out) }
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    // --- ADDED MISSING NAVIGATION FUNCTIONS ---
    fun navigateBackToMenu() {
        _navigationEvent.value = AdjustmentNavigationEvent.NavigateBackToMenu
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}