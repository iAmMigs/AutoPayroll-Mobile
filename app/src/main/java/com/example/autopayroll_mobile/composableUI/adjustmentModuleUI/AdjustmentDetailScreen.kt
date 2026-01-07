package com.example.autopayroll_mobile.composableUI.adjustmentModuleUI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.composableUI.StatusChip
import com.example.autopayroll_mobile.data.AdjustmentModule.AdjustmentModuleUiState
import com.example.autopayroll_mobile.viewmodel.AdjustmentModuleViewModel

private val WebBackground = Color(0xFFF8F9FA)
private val WebSurface = Color.White
private val WebBorderColor = Color(0xFFE2E8F0)
private val TextHeader = Color(0xFF1E293B)
private val TextBody = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustmentDetailScreen(
    uiState: AdjustmentModuleUiState,
    requestId: String,
    viewModel: AdjustmentModuleViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(key1 = requestId) { viewModel.selectRequestById(requestId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Details", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { viewModel.clearSelectedRequest(); onBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextHeader) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WebBackground, titleContentColor = TextHeader)
            )
        },
        containerColor = WebBackground
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState())) {
            if (uiState.selectedRequest != null) {
                val details = uiState.selectedRequest!!
                Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = WebSurface), border = BorderStroke(1.dp, WebBorderColor), elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextBody)
                            StatusChip(status = details.status)
                        }
                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = WebBorderColor)
                        DetailItem("Type", details.type.replaceFirstChar { it.uppercase() })
                        DetailItem("Sub-Type", details.subType)
                        DetailItem("Date Submitted", details.dateSubmitted ?: "N/A")
                        val dateInfo = if (!details.startDate.isNullOrBlank()) "${details.startDate} to ${details.endDate}" else details.dateSubmitted
                        DetailItem("Affected Dates", dateInfo ?: "N/A")
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailItem("Reason", details.reason ?: "None")
                        if (details.remarks != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)), shape = RoundedCornerShape(4.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("REVIEWER REMARKS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextBody)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(details.remarks, fontSize = 14.sp, color = TextHeader)
                                }
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = TextHeader) }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(text = label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextBody, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 16.sp, color = TextHeader, fontWeight = FontWeight.Medium)
    }
}