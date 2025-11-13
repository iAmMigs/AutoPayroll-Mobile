package com.example.autopayroll_mobile.composableUI

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
// ## ADD THIS IMPORT ##
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
// ## REMOVE THIS IMPORT (it was causing the error) ##
// import androidx.compose.material3.OutlinedButtonDefaults

/**
 * Replaces `RequestFilingFragment`.
 * This version matches the style of your LeaveModuleFormScreen.
 */
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

    // --- Derived State for Form ---
    val mainTypes = listOf("Leave", "Attendance", "Payroll")

    val subTypeOptions by remember(uiState.adjustmentTypes, uiState.formMainType) {
        derivedStateOf {
            uiState.adjustmentTypes.filter {
                it.mainType.equals(uiState.formMainType, ignoreCase = true)
            }
        }
    }

    // --- ActivityResultLauncher for File Picker ---
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onAttachmentSelected(it) }
    }

    // --- Handle Submission Status ---
    LaunchedEffect(uiState.submissionStatus) {
        when (uiState.submissionStatus) {
            FormSubmissionStatus.SUCCESS -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Request submitted successfully!",
                        withDismissAction = true
                    )
                }
                onBack() // Go back after success
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
            viewModel.clearForm() // Clears form and resets status
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
            // --- 1. Adjustment Type (Radio Buttons) ---
            Text(
                "Adjustment Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                mainTypes.forEach { type ->
                    Row(
                        Modifier
                            .selectable(
                                selected = (uiState.formMainType == type),
                                onClick = { viewModel.onMainTypeChanged(type) }
                            )
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (uiState.formMainType == type),
                            onClick = { viewModel.onMainTypeChanged(type) }
                        )
                        Text(text = type, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // --- 2. Sub Type (Dropdown) ---
            SubTypeDropdown(
                options = subTypeOptions,
                selectedOption = uiState.formSubType,
                onOptionSelected = { viewModel.onSubTypeChanged(it) },
                label = "Adjustment Sub-type"
            )
            Spacer(Modifier.height(16.dp))

            // --- 3. Affected Date (Pickers) ---
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

            // --- 4. Number of Hours (Optional) ---
            OutlinedTextField(
                value = uiState.formHours,
                onValueChange = { viewModel.onHoursChanged(it) },
                label = { Text("Number of Hours (Optional)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(16.dp))

            // --- 5. Reason for Adjustment ---
            OutlinedTextField(
                value = uiState.formReason,
                onValueChange = { viewModel.onReasonChanged(it) },
                label = { Text("Reason for Adjustment") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(24.dp))

            // --- 6. Attachment ---
            AttachmentButton(
                fileName = uiState.formAttachment?.name,
                onFilePick = { filePickerLauncher.launch("*/*") },
                onFileRemove = { viewModel.onAttachmentRemoved() }
            )
            Spacer(Modifier.height(32.dp))

            // --- 7. Send Button ---
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

/**
 * A reusable dropdown for the Sub-Type.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubTypeDropdown(
    options: List<AdjustmentType>,
    selectedOption: AdjustmentType?,
    onOptionSelected: (AdjustmentType) -> Unit,
    label: String
) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded }
    ) {
        OutlinedTextField(
            value = selectedOption?.name ?: "",
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
}

/**
 * A custom button for handling file attachments.
 */
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
        // ## THIS IS THE FIX ##
        // Replaced the error line with a manual BorderStroke
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

/**
 * This is the DatePickerField composable, copied
 * EXACTLY from your LeaveModuleFormScreen.kt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    date: String,
    onDateSelected: (String) -> Unit // Returns "YYYY-MM-DD"
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
            value = date.ifEmpty { "Select a date" },
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