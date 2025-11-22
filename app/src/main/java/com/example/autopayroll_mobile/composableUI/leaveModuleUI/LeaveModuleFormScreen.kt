package com.example.autopayroll_mobile.composableUI.leaveModuleUI

import android.net.Uri // NEW IMPORT
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult // NEW IMPORT
import androidx.activity.result.contract.ActivityResultContracts // NEW IMPORT
import androidx.compose.foundation.BorderStroke // NEW IMPORT for AttachmentButton
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile // NEW IMPORT for AttachmentButton
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // NEW IMPORT for AttachmentButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // NEW IMPORT for AttachmentButton
import com.example.autopayroll_mobile.ui.theme.TextPrimary // NEW IMPORT for AttachmentButton
import com.example.autopayroll_mobile.viewmodel.LeaveModuleViewModel
import com.example.autopayroll_mobile.viewmodel.NavigationEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveModuleFormScreen(
    viewModel: LeaveModuleViewModel,
    onBackClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult( // ## NEW ##
        contract = ActivityResultContracts.GetContent() // Contract to pick any content type
    ) { uri: Uri? ->
        uri?.let { viewModel.onAttachmentSelected(it) }
    }


    // Observe error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }

    // Observe navigation events
    LaunchedEffect(viewModel.navigationEvent) {
        viewModel.navigationEvent.collect { event ->
            if (event == NavigationEvent.NavigateBack) {
                onBackClicked()
                viewModel.onNavigationHandled()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("File a Leave") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Leave Type Dropdown
            LeaveTypeDropdown(
                selectedType = uiState.formLeaveType,
                leaveTypes = uiState.leaveTypes,
                onTypeSelected = { viewModel.onLeaveTypeChanged(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Date Pickers
            DatePickerField(
                label = "Start Date",
                date = uiState.formStartDate,
                onDateSelected = { viewModel.onStartDateChanged(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            DatePickerField(
                label = "End Date",
                date = uiState.formEndDate,
                onDateSelected = { viewModel.onEndDateChanged(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Reason Text Field
            OutlinedTextField(
                value = uiState.formReason,
                onValueChange = { viewModel.onReasonChanged(it) },
                label = { Text("Reason") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // ## NEW: Attachment Button ##
            AttachmentButton(
                fileName = uiState.formAttachment?.name,
                onFilePick = { filePickerLauncher.launch("*/*") }, // Launch file picker for any file type
                onFileRemove = { viewModel.onAttachmentRemoved() }
            )
            Spacer(modifier = Modifier.height(32.dp))

            // 4. Submit Button
            Button(
                onClick = { viewModel.submitLeaveRequest() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.formIsSubmitting
            ) {
                if (uiState.formIsSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("SUBMIT")
                }
            }
        }
    }
}

// (LeaveTypeDropdown and DatePickerField composables are unchanged, keeping them here for completeness)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveTypeDropdown(
    selectedType: String,
    leaveTypes: Map<String, String>,
    onTypeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            readOnly = true,
            label = { Text("Leave Type") },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, "Dropdown")
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            leaveTypes.values.forEach { displayName ->
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        onTypeSelected(displayName)
                        expanded = false
                    }
                )
            }
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

// ## NEW: AttachmentButton composable (copied and adapted from AdjustmentFilingScreen.kt) ##
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
            contentColor = TextPrimary // Assuming TextPrimary is a theme color
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