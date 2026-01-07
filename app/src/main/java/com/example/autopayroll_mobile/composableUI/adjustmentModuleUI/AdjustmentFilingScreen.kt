package com.example.autopayroll_mobile.composableUI.adjustmentModuleUI

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.data.AdjustmentModule.AdjustmentModuleUiState
import com.example.autopayroll_mobile.data.AdjustmentModule.AdjustmentType
import com.example.autopayroll_mobile.data.AdjustmentModule.FormSubmissionStatus
import com.example.autopayroll_mobile.viewmodel.AdjustmentModuleViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val WebBackground = Color(0xFFF8F9FA)
private val TextHeader = Color(0xFF1E293B)
private val AccentYellow = Color(0xFFFFC107)

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

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.onAttachmentSelected(it) } }

    LaunchedEffect(uiState.submissionStatus) {
        if (uiState.submissionStatus == FormSubmissionStatus.SUCCESS) {
            scope.launch { snackbarHostState.showSnackbar("Request submitted successfully!") }
            onBack()
        } else if (uiState.submissionStatus == FormSubmissionStatus.ERROR) {
            scope.launch { snackbarHostState.showSnackbar(uiState.submissionError ?: "Error") }
        }
        if (uiState.submissionStatus != FormSubmissionStatus.IDLE) viewModel.clearForm()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("File New Request", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextHeader)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WebBackground, titleContentColor = TextHeader)
            )
        },
        containerColor = WebBackground
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp).verticalScroll(scrollState)
        ) {
            MainTypeDropdown("Adjustment Type", mainTypesMap, uiState.formMainType) { viewModel.onMainTypeChanged(it) }
            Spacer(Modifier.height(16.dp))
            SubTypeDropdown(uiState.adjustmentTypes, uiState.formSubType, { viewModel.onSubTypeChanged(it) }, "Adjustment Sub-type", uiState.isLoadingTypes, uiState.formSubType == null && uiState.submissionError != null, "Please select a sub-type")
            Spacer(Modifier.height(24.dp))
            Text("Affected Date (Single Day)", style = MaterialTheme.typography.labelMedium, color = TextHeader)
            Spacer(Modifier.height(8.dp))
            DatePickerField("Select Date", uiState.formAffectedDate) { viewModel.onAffectedDateChanged(it) }
            Spacer(Modifier.height(16.dp))
            Text("Date Range (Optional)", style = MaterialTheme.typography.labelMedium, color = TextHeader)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) { DatePickerField("Start", uiState.formStartDate) { viewModel.onStartDateChanged(it) } }
                Box(modifier = Modifier.weight(1f)) { DatePickerField("End", uiState.formEndDate) { viewModel.onEndDateChanged(it) } }
            }
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = uiState.formReason,
                onValueChange = { viewModel.onReasonChanged(it) },
                label = { Text("Reason for Adjustment") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(8.dp),
                isError = uiState.formReason.isBlank() && uiState.submissionError != null
            )
            Spacer(Modifier.height(24.dp))
            AttachmentButton(uiState.formAttachment?.name, { filePickerLauncher.launch("*/*") }, { viewModel.onAttachmentRemoved() })
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { viewModel.submitAdjustmentRequest() },
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow, contentColor = TextHeader)
            ) {
                if (uiState.isSubmitting) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TextHeader)
                else Text("Submit Request", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// --- HELPER COMPOSABLES ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTypeDropdown(label: String, options: Map<String, String>, selectedApiKey: String, onOptionSelected: (String) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val selectedDisplayName = options.entries.find { it.value == selectedApiKey }?.key ?: ""
    ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isExpanded = !isExpanded }) {
        OutlinedTextField(value = selectedDisplayName, onValueChange = {}, label = { Text(label) }, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(8.dp))
        ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }, modifier = Modifier.background(Color.White)) {
            options.forEach { (name, key) -> DropdownMenuItem(text = { Text(name) }, onClick = { onOptionSelected(key); isExpanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubTypeDropdown(options: List<AdjustmentType>, selectedOption: AdjustmentType?, onOptionSelected: (AdjustmentType) -> Unit, label: String, isLoading: Boolean, isError: Boolean, errorMessage: String) {
    var isExpanded by remember { mutableStateOf(false) }
    Column {
        ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { if (!isLoading) isExpanded = it }) {
            OutlinedTextField(
                value = selectedOption?.name ?: "",
                onValueChange = {},
                label = { Text(label) },
                readOnly = true,
                trailingIcon = { if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                isError = isError
            )
            ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }, modifier = Modifier.background(Color.White)) {
                options.forEach { option -> DropdownMenuItem(text = { Text(option.name) }, onClick = { onOptionSelected(option); isExpanded = false }) }
            }
        }
        if (isError) Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
    }
}

@Composable
private fun AttachmentButton(fileName: String?, onFilePick: () -> Unit, onFileRemove: () -> Unit) {
    OutlinedButton(
        onClick = { if (fileName == null) onFilePick() else onFileRemove() },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Icon(Icons.Default.AttachFile, null, modifier = Modifier.padding(end = 8.dp), tint = TextHeader)
        if (fileName == null) Text("Attach Document", color = TextHeader) else { Text(fileName, modifier = Modifier.weight(1f), maxLines = 1, color = TextHeader); Text(" (Remove)", color = Color.Red) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(label: String, date: String, onDateSelected: (String) -> Unit) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = { Button(onClick = { datePickerState.selectedDateMillis?.let { onDateSelected(Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate().format(dateFormatter)) }; showDialog = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
    Box(modifier = Modifier.fillMaxWidth().clickable { showDialog = true }) {
        OutlinedTextField(
            value = if (date.isNotBlank()) try { LocalDate.parse(date, dateFormatter).format(DateTimeFormatter.ofPattern("MM-dd-yyyy")) } catch (e: Exception) { "" } else "",
            onValueChange = {}, label = { Text(label) }, trailingIcon = { Icon(Icons.Default.DateRange, "Select Date") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = Color.Gray, disabledTextColor = TextHeader, disabledLabelColor = Color.Gray, disabledTrailingIconColor = Color.Gray)
        )
    }
}