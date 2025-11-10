package com.example.autopayroll_mobile.composableUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.models.Payslip
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.PayslipUiState
import com.example.autopayroll_mobile.viewmodel.PayslipViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayslipScreen(viewModel: PayslipViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payroll") },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back press */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            EmployeeHeader(uiState)
            HorizontalDivider()
            Text(
                text = "My Pay Slips",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn {
                items(uiState.payslips) { payslip ->
                    PayslipItem(payslip = payslip)
                }
            }
        }
    }
}

@Composable
fun EmployeeHeader(uiState: PayslipUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.profiledefault),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = uiState.employeeName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = uiState.jobAndCompany,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PayslipItem(payslip: Payslip) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = payslip.dateRange, fontWeight = FontWeight.Bold)
                Text(text = "Net Amount: ${payslip.netAmount}", color = Color.Gray)
            }
            Text(
                text = payslip.status,
                color = when (payslip.status) {
                    "Processing" -> Color(0xFFFFA500) // Orange
                    "Completed" -> Color(0xFF4CAF50) // Green
                    else -> Color.Black
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PayslipScreenPreview() {
    AutoPayrollMobileTheme {
        Box(modifier = Modifier.background(Color.White)) {
            // Dummy data for preview
            val dummyUiState = PayslipUiState(
                employeeName = "Marc Jurell Afable",
                jobAndCompany = "Janitor - Wilson Trading Inc",
                payslips = listOf(
                    Payslip("July 16 - 31, 2025", "5,456.15", "Processing"),
                    Payslip("July 1 - 15, 2025", "5,456.15", "Completed")
                )
            )
           // A full preview requires a ViewModel instance.
           // For a static preview, we can build the UI with dummy data.
        }
    }
}