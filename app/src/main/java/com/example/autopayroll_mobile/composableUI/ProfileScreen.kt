package com.example.autopayroll_mobile.composableUI

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.autopayroll_mobile.data.generalData.Employee
import com.example.autopayroll_mobile.viewmodel.ProfileViewModel
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

// Define custom design colors here, matching the Dashboard/Menu aesthetic
val AppBackground = Color(0xFFEEEEEE) // Light Gray
val CardSurface = Color.White // White

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.error != null) {
        // Show error with a retry or re-login hint
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.error ?: "Unknown Error",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                Button(onClick = { profileViewModel.fetchEmployeeData() }) {
                    Text("Retry")
                }
            }
        }
    } else if (uiState.employee != null) {
        val employee = uiState.employee!!

        val formattedMiddleInitial = employee.middleName?.takeIf { it.isNotBlank() }?.let {
            "${it.first().uppercaseChar()}."
        } ?: ""

        val displayFullName = listOfNotNull(
            employee.firstName,
            formattedMiddleInitial.takeIf { it.isNotBlank() },
            employee.lastName
        ).joinToString(" ")

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CardSurface)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // --- HEADER SECTION ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardSurface)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onBack() }
                            .padding(top = 16.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_back_arrow), contentDescription = "Back")
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("User Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Profile Picture
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        ) {
                            val painter = rememberAsyncImagePainter(
                                model = employee.profilePhoto ?: R.drawable.profiledefault,
                                error = painterResource(id = R.drawable.profiledefault)
                            )
                            Image(
                                painter = painter,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Name and Title
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayFullName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("${employee.jobPosition} • ${employee.companyName}", fontSize = 14.sp)
                        }

                        // Edit Button (Visual only for now)
                        Button(
                            onClick = { /* TODO: Edit Profile */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFBC02D),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text("Edit")
                        }
                    }
                }

                // --- CONTENT AREA ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardSurface)
                        .padding(horizontal = 16.dp)
                ) {
                    InfoCard("Personal Information", R.drawable.ic_personal_info, employee)
                    Spacer(modifier = Modifier.height(16.dp))

                    AddressCard("Address Information", employee)
                    Spacer(modifier = Modifier.height(16.dp))

                    EmploymentCard("Employment Overview", employee)
                    Spacer(modifier = Modifier.height(16.dp))

                    ContactCard("Contact Information", employee)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, icon: Int, employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
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
                val formattedMiddleInitial = employee.middleName?.takeIf { it.isNotBlank() }?.let {
                    "${it.first().uppercaseChar()}."
                } ?: ""

                val fullName = listOfNotNull(
                    employee.firstName,
                    formattedMiddleInitial.takeIf { it.isNotBlank() },
                    employee.lastName
                ).joinToString(" ")

                InfoItem("Full Name", fullName, Modifier.weight(1f))
                InfoItem("Gender", employee.gender.replaceFirstChar { it.uppercase() }, Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Marital Status / DOB
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Marital Status", employee.maritalStatus.replaceFirstChar { it.uppercase() }, Modifier.weight(1f))
                InfoItem("Date of Birth", formatApiDate(employee.birthdate), Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Blood Type / Age
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Blood Type", employee.bloodType ?: "N/A", Modifier.weight(1f))
                val age = calculateAge(employee.birthdate)?.toString() ?: "N/A"
                InfoItem("Age", age, Modifier.weight(1f))
            }

            // REMOVED: "Place of Birth" row (Redundant with address and not explicitly provided by API)
        }
    }
}

@Composable
fun AddressCard(title: String, employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = R.drawable.ic_address), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Combine address parts, filtering out nulls
            val residentialAddress = listOfNotNull(
                employee.street,
                employee.barangay,
                employee.city,
                employee.province,
                employee.country
            ).joinToString(", ")

            InfoItem("Residential Address", residentialAddress.ifEmpty { "N/A" })

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            val idAddress = listOfNotNull(
                employee.idStreet,
                employee.idBarangay,
                employee.idCity,
                employee.idProvince,
                employee.idCountry
            ).joinToString(", ")

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
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = R.drawable.ic_payroll), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // FIX: Handle nullable contractStart safely
                val startDate = employee.contractStart?.let { formatApiDate(it) } ?: "N/A"

                InfoItem("Date Started", startDate, Modifier.weight(1f))
                InfoItem("Job Role", employee.jobPosition, Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            InfoItem("Employment Status", employee.employmentType)
        }
    }
}

@Composable
fun ContactCard(title: String, employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = R.drawable.ic_contact), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoItem("Phone Number", employee.phoneNumber ?: "N/A")

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            InfoItem("Email", employee.email)
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
    if (apiDate.isBlank()) return "N/A"

    val outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())

    return try {
        // 1. Try parsing as standard ISO DateTime (e.g., 2023-10-05T14:30:00.000000Z)
        val dateTime = OffsetDateTime.parse(apiDate)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        try {
            // 2. Fallback: Try parsing as Date only (e.g., 2023-10-05) - Common in Laravel
            val date = LocalDate.parse(apiDate)
            date.format(outputFormatter)
        } catch (e2: Exception) {
            Log.e("ProfileScreen", "Error parsing date: $apiDate", e2)
            apiDate // Return original string if we can't parse it
        }
    }
}

private fun calculateAge(birthdate: String): Int? {
    if (birthdate.isBlank()) return null
    return try {
        // Try parsing as DateTime
        val parsedDate = OffsetDateTime.parse(birthdate).toLocalDate()
        Period.between(parsedDate, LocalDate.now()).years
    } catch (e: Exception) {
        try {
            // Fallback: Try parsing as Date only
            val parsedDate = LocalDate.parse(birthdate)
            Period.between(parsedDate, LocalDate.now()).years
        } catch (e2: Exception) {
            null
        }
    }
}