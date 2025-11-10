package com.example.autopayroll_mobile.composableUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.data.model.Employee
import com.example.autopayroll_mobile.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(profileViewModel: ProfileViewModel = viewModel()) {
    val uiState by profileViewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: ${uiState.error}")
        }
    } else if (uiState.employee != null) {
        val employee = uiState.employee!!
        // val company = uiState.company // <-- This line is removed.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = R.drawable.ic_back_arrow), contentDescription = "Back")
                Spacer(modifier = Modifier.width(16.dp))
                Text("User Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                val painter = rememberAsyncImagePainter(
                    model = employee.profilePhoto ?: R.drawable.profiledefault
                )
                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("${employee.firstName} ${employee.lastName}", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    // --- THIS IS THE FIX ---
                    // We now get the company name from the employee object directly.
                    // Your server already provides "N/A" as a default, so no placeholder is needed.
                    Text("${employee.jobPosition} • ${employee.companyName}", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { /* TODO: Edit Profile */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Edit")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Personal Information
            InfoCard("Personal Information", R.drawable.ic_personal_info, employee)
            Spacer(modifier = Modifier.height(16.dp))

            // Address Information
            AddressCard("Address Information", employee)
            Spacer(modifier = Modifier.height(16.dp))

            // Employment Overview
            EmploymentCard("Employment Overview", employee)
            Spacer(modifier = Modifier.height(16.dp))

            // Contact Information
            ContactCard("Contact Information", employee)
        }
    }
}

// ... (The rest of your file remains unchanged) ...

@Composable
fun InfoCard(title: String, icon: Int, employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = icon), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Full Name", "${employee.firstName} ${employee.lastName}", Modifier.weight(1f))
                InfoItem("Gender", employee.gender, Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Marital Status", employee.maritalStatus, Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Place of Birth", "${employee.city}, ${employee.province}, ${employee.country}", Modifier.weight(1f))
                InfoItem("Date of Birth", employee.birthdate, Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Blood Type", employee.bloodType ?: "N/A", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AddressCard(title: String, employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            InfoItem("Residential Address", "${employee.street}, ${employee.barangay}, ${employee.city}, ${employee.province}, ${employee.country}")
            Spacer(modifier = Modifier.height(8.dp))
            InfoItem("Address on Identification Card", "${employee.idStreet}, ${employee.idBarangay}, ${employee.idCity}, ${employee.idProvince}, ${employee.idCountry}")
        }
    }
}

@Composable
fun EmploymentCard(title: String, employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Date Started", employee.contractStart, Modifier.weight(1f))
                InfoItem("Job Role", employee.jobPosition, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            InfoItem("Employment Status", employee.employmentType)
            TextButton(onClick = { /* TODO: View Contract */ }) {
                Text("View Contract >", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ContactCard(title: String, employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { /* TODO: Edit Contact Info */ }) {
                    Text("Edit", color = MaterialTheme.colorScheme.primary)
                }
                TextButton(onClick = { /* TODO: Save Contact Info */ }) {
                    Text("Save", color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoItem("Phone Number", employee.phoneNumber)
            Spacer(modifier = Modifier.height(8.dp))
            InfoItem("Email", employee.email)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Other Contact", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}