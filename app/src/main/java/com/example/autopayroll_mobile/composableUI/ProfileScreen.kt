package com.example.autopayroll_mobile.composableUI

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // ## NEW IMPORT ##
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
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onBack: () -> Unit // ## NEW: Callback for back navigation ##
) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                // --- 1. CHANGE SCREEN BACKGROUND ---
                .background(Color(0xFFF5F5F5)) // Light Gray Background
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                // ## NEW: Add clickable modifier to the Icon for back functionality ##
                modifier = Modifier.clickable { onBack() }
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_back_arrow), contentDescription = "Back")
                Spacer(modifier = Modifier.width(16.dp))
                Text("User Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Info Column
            Column(modifier = Modifier.fillMaxWidth()) {
                // Row for Image and Name/Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Box wrapper for Image to fix layout stretching
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray) // Placeholder background
                    ) {
                        val painter = rememberAsyncImagePainter(
                            model = employee.profilePhoto ?: R.drawable.profiledefault,
                            error = painterResource(id = R.drawable.profiledefault)
                        )
                        Image(
                            painter = painter,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(), // Fill the Box
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Column for Name and Title
                    Column {
                        Text("${employee.firstName} ${employee.lastName}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("${employee.jobPosition} • ${employee.companyName}", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp)) // Space between name and button

                // New Row just for the Button, aligned to the end
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End // Pushes button to the right
                ) {
                    Button(
                        onClick = { /* TODO: Edit Profile */ },
                        colors = ButtonDefaults.buttonColors(
                            // --- 2. CHANGE BUTTON COLOR ---
                            containerColor = Color(0xFFFBC02D), // Deeper Yellow
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        // --- 3. ADD BUTTON SHADOW ---
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Text("Edit")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. ADD SHAPE TO ALL CARDS ---

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

@Composable
fun InfoCard(title: String, icon: Int, employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp) // <-- ADDED SHAPE
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = icon), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Full Name / Gender
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Full Name", "${employee.firstName} ${employee.lastName}", Modifier.weight(1f))
                InfoItem("Gender", employee.gender.replaceFirstChar { it.uppercase() }, Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Marital Status / Religion
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Marital Status", employee.maritalStatus.replaceFirstChar { it.uppercase() }, Modifier.weight(1f))
                InfoItem("Religion", employee.religion ?: "N/A", Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Place of Birth / Date of Birth (Formatted)
            Row(modifier = Modifier.fillMaxWidth()) {
                val placeOfBirth = listOfNotNull(employee.city, employee.province, employee.country)
                    .joinToString(", ")
                InfoItem("Place of Birth", placeOfBirth.ifEmpty { "N/A" }, Modifier.weight(1f))
                InfoItem("Date of Birth", formatApiDate(employee.birthdate), Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Blood Type / Age (Calculated)
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Blood Type", employee.bloodType ?: "N/A", Modifier.weight(1f))
                val age = calculateAge(employee.birthdate)?.toString() ?: "N/A"
                InfoItem("Age", age, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AddressCard(title: String, employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp) // <-- ADDED SHAPE
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // TODO: Replace with your address icon
                Icon(painter = painterResource(id = R.drawable.ic_personal_info), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            val residentialAddress = listOfNotNull(employee.street, employee.barangay, employee.city, employee.province, employee.country)
                .joinToString(", ")
            InfoItem("Residential Address", residentialAddress.ifEmpty { "N/A" })

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            val idAddress = listOfNotNull(employee.idStreet, employee.idBarangay, employee.idCity, employee.idProvince, employee.idCountry)
                .joinToString(", ")
            InfoItem("Address on Identification Card", idAddress.ifEmpty { "N/A" })
        }
    }
}

@Composable
fun EmploymentCard(title: String, employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp) // <-- ADDED SHAPE
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // TODO: Replace with your employment icon
                Icon(painter = painterResource(id = R.drawable.ic_personal_info), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Date Started", formatApiDate(employee.contractStart), Modifier.weight(1f))
                InfoItem("Job Role", employee.jobPosition, Modifier.weight(1f))
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            InfoItem("Employment Status", employee.employmentType)

            Box(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = { /* TODO: View Contract */ },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text("View Contract >", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun ContactCard(title: String, employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp) // <-- ADDED SHAPE
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // TODO: Replace with your contact icon
                Icon(painter = painterResource(id = R.drawable.ic_personal_info), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))

                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = { /* TODO: Edit Contact Info */ },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Text("Edit", color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoItem("Phone Number", employee.phoneNumber)

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            InfoItem("Email", employee.email)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Other Contact", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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

private fun formatApiDate(apiDate: String): String {
    return try {
        val outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
        val dateTime = OffsetDateTime.parse(apiDate)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        Log.e("ProfileScreen", "Error parsing date: $apiDate", e)
        "Invalid Date"
    }
}

private fun calculateAge(birthdate: String): Int? {
    return try {
        val parsedDate = OffsetDateTime.parse(birthdate).toLocalDate()
        val today = LocalDate.now()
        Period.between(parsedDate, today).years
    } catch (e: Exception) {
        null
    }
}