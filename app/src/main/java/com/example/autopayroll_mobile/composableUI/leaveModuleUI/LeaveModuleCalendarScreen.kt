package com.example.autopayroll_mobile.composableUI.leaveModuleUI

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.autopayroll_mobile.viewmodel.LeaveModuleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveModuleCalendarScreen(
    viewModel: LeaveModuleViewModel,
    onBackClicked: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave Calendar") },
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
        ) {
            // This is the full calendar composable
            // As you noted, we are not adding the logic to
            // mark absent days yet, but the UI is here.
            DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth()
            )

            // TODO: Add legend here later
            // (e.g., "Red = Absent", "Green = Present")
        }
    }
}