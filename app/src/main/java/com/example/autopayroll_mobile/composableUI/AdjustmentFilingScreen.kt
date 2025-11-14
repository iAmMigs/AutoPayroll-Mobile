package com.example.autopayroll_mobile.composableUI

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.data.model.AdjustmentModuleUiState
import com.example.autopayroll_mobile.data.model.AdjustmentType
import com.example.autopayroll_mobile.data.model.FormSubmissionStatus
import com.example.autopayroll_mobile.ui.theme.TextPrimary
import com.example.autopayroll_mobile.viewmodel.AdjustmentModuleViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentFilingScreen(
    uiState: AdjustmentModuleUiState,
    viewModel: AdjustmentModuleViewModel,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val mainTypesMap = mapOf(
        "Leave" to "leave",
        "Attendance" to "attendance",
        "Payroll" to "payroll",
        "Official Business" to "official_business"
    )

    val subTypeOptions by remember(uiState.adjustmentTypes) {
        derivedStateOf {
            uiState.adjustmentTypes
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onAttachmentSelected(it) }
    }

    val isReasonError = uiState.formReason.isBlank()
    val isSubTypeError = uiState.formSubType == null

    LaunchedEffect(uiState.submissionStatus) {
        when (uiState.submissionStatus) {
            FormSubmissionStatus.SUCCESS -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Request submitted successfully!",
                        withDismissAction = true
                    )
                }
                onBack()
            }
            FormSubmissionStatus.ERROR -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = uiState.submissionError ?: "An unknown error occurred.",
                        withDismissAction = true
                    )
                }
            }
            FormSubmissionStatus.IDLE -> {}
        }
        if (uiState.submissionStatus != FormSubmissionStatus.IDLE) {
            viewModel.clearForm()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Request Filing") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {

            // --- 1. Adjustment Type (Dropdown) ---
            MainTypeDropdown(
                label = "Adjustment Type",
                options = mainTypesMap,
                selectedApiKey = uiState.formMainType,
                onOptionSelected = { apiKey ->
                    viewModel.onMainTypeChanged(apiKey)
                }
            )

            Spacer(Modifier.height(16.dp))

            // --- 2. Sub Type (Dropdown) ---
            SubTypeDropdown(
                options = subTypeOptions,
                selectedOption = uiState.formSubType,
                onOptionSelected = { viewModel.onSubTypeChanged(it) },
                label = "Adjustment Sub-type",
                isLoading = uiState.isLoadingTypes,
                isError = isSubTypeError && uiState.submissionError != null,
                errorMessage = "Please select a sub-type"
            )

            Spacer(Modifier.height(16.dp))

            // --- 3. Affected Date (Pickers) ---
            Text(
                "For single-day requests:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            DatePickerField(
                label = "Affected Date",
                date = uiState.formAffectedDate,
                onDateSelected = { viewModel.onAffectedDateChanged(it) } // ## UPDATED ##
            )

            Spacer(Modifier.height(16.dp))
            Text(
                "For date-range requests:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            DatePickerField(
                label = "Start Date",
                date = uiState.formStartDate,
                onDateSelected = { viewModel.onStartDateChanged(it) }
            )
            Spacer(Modifier.height(16.dp))
            DatePickerField(
                label = "End Date",
                date = uiState.formEndDate,
                onDateSelected = { viewModel.onEndDateChanged(it) }
            )
            Spacer(Modifier.height(16.dp))

            // --- 4. Reason for Adjustment ---
            OutlinedTextField(
                value = uiState.formReason,
                onValueChange = { viewModel.onReasonChanged(it) },
                label = { Text("Reason for Adjustment") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp),
                isError = isReasonError && uiState.submissionError != null
            )
            if (isReasonError && uiState.submissionError != null) {
                Text(
                    text = "Reason is required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // --- 5. Attachment ---
            AttachmentButton(
                fileName = uiState.formAttachment?.name,
                onFilePick = { filePickerLauncher.launch("*/*") },
                onFileRemove = { viewModel.onAttachmentRemoved() }
            )
            Spacer(Modifier.height(32.dp))

            // --- 6. Send Button ---
            Button(
                onClick = { viewModel.submitAdjustmentRequest() },
                enabled = !uiState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TextPrimary
                )
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Send Request", fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTypeDropdown(
    label: String,
    options: Map<String, String>, // <"Leave", "leave">
    selectedApiKey: String,
    onOptionSelected: (String) -> Unit // returns "leave"
) {
    var isExpanded by remember { mutableStateOf(false) }

    val selectedDisplayName = options.entries.find { it.value == selectedApiKey }?.key ?: ""

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded }
    ) {
        OutlinedTextField(
            value = selectedDisplayName,
            onValueChange = { },
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            options.forEach { (displayName, apiKey) ->
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        onOptionSelected(apiKey)
                        isExpanded = false
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubTypeDropdown(
    options: List<AdjustmentType>,
    selectedOption: AdjustmentType?,
    onOptionSelected: (AdjustmentType) -> Unit,
    label: String,
    isLoading: Boolean,
    isError: Boolean,
    errorMessage: String
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = {
                if (!isLoading && !isError) {
                    isExpanded = it
                }
            }
        ) {
            OutlinedTextField(
                value = selectedOption?.name ?: "",
                onValueChange = { },
                label = { Text(label) },
                readOnly = true,
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                    }
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = isError
            )
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name) },
                        onClick = {
                            onOptionSelected(option)
                            isExpanded = false
                        }
                    )
                }
            }
        }
        if (isError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun AttachmentButton(
    fileName: String?,
    onFilePick: () -> Unit,
    onFileRemove: () -> Unit
) {
    OutlinedButton(
        onClick = { if (fileName == null) onFilePick() else onFileRemove() },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextPrimary
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Icon(
            Icons.Default.AttachFile,
            contentDescription = "Attach File",
            modifier = Modifier.padding(end = 8.dp)
        )
        if (fileName == null) {
            Text("Attach Image / File")
        } else {
            Text(fileName, modifier = Modifier.weight(1f))
            Text(" (Remove)", color = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    date: String,
    onDateSelected: (String) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var showDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        onDateSelected(selectedDate.format(dateFormatter))
                    }
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    ) {
        OutlinedTextField(
            value = if (date.isNotBlank()) {
                try {
                    LocalDate.parse(date, dateFormatter)
                        .format(DateTimeFormatter.ofPattern("MM-dd-yyyy"))
                } catch (e: Exception) { "" }
            } else { "" },
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = {
                Icon(Icons.Default.DateRange, "Select Date")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = false,
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}