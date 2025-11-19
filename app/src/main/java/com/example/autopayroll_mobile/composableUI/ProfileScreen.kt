package com.example.autopayroll_mobile.composableUI

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.autopayroll_mobile.data.generalData.Employee
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars

// Define custom design colors here, matching the Dashboard/Menu aesthetic
val AppBackground = Color(0xFFEEEEEE) // Light Gray (Darker Header Background)
val CardSurface = Color.White // White (Lighter Content Background)
val TextPrimary = Color(0xFF3C3C3C) // Assuming TextPrimary

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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: ${uiState.error}")
        }
    } else if (uiState.employee != null) {
        val employee = uiState.employee!!

        // Calculate formatted full name with middle initial here, for reuse
        val formattedMiddleInitial = employee.middleName?.takeIf { it.isNotBlank() }?.let {
            "${it.first().uppercaseChar()}." // Get first char, uppercase it, add period
        } ?: "" // If middle name is blank or null, return empty string

        val displayFullName = listOfNotNull(
            employee.firstName,
            formattedMiddleInitial.takeIf { it.isNotBlank() }, // Only include if it's not empty
            employee.lastName
        ).joinToString(" ")

        // Outer Column/Box sets the entire background to WHITE and handles insets
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CardSurface) // Base background is White
                .windowInsetsPadding(WindowInsets.statusBars) // Apply status bar padding
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardSurface) // Explicitly set to White
                        .padding(horizontal = 16.dp) // Horizontal padding for the content
                        .padding(bottom = 24.dp) // Space before the first information card
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onBack() }
                            .padding(top = 16.dp) // Top padding below the status bar padding
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_back_arrow), contentDescription = "Back")
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("User Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp)) // Space below the title

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

                        // Column for Name and Title
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayFullName, // Line 142
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("${employee.jobPosition} • ${employee.companyName}", fontSize = 14.sp)
                        }

                        // Edit Button
                        Button(
                            onClick = { /* TODO: Edit Profile */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFBC02D), // Deeper Yellow
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 2.dp
                            )
                        ) {
                            Text("Edit")
                        }
                    }
                }


                // --- 2. CONTENT AREA (White BG - implicit from the outer Box) ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardSurface) // Explicit white background for safety
                        .padding(horizontal = 16.dp) // Horizontal padding for the content area
                ) {
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

                    Spacer(modifier = Modifier.height(32.dp)) // Padding at the bottom
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

                // Construct the full name, ensuring proper spacing
                val fullName = listOfNotNull(
                    employee.firstName,
                    formattedMiddleInitial.takeIf { it.isNotBlank() }, // Only include if it's not empty
                    employee.lastName
                ).joinToString(" ")

                InfoItem("Full Name", fullName, Modifier.weight(1f)) // Line 212
                InfoItem("Gender", employee.gender.replaceFirstChar { it.uppercase() }, Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Marital Status", employee.maritalStatus.replaceFirstChar { it.uppercase() }, Modifier.weight(1f))
                InfoItem("Date of Birth", formatApiDate(employee.birthdate), Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                val placeOfBirth = listOfNotNull(employee.city, employee.province, employee.country)
                    .joinToString(", ")
                InfoItem("Place of Birth", placeOfBirth.ifEmpty { "N/A" }, Modifier.weight(1f))
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
        shape = RoundedCornerShape(16.dp)
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
        shape = RoundedCornerShape(16.dp)
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
                // TODO: Replace with your contact icon
                Icon(painter = painterResource(id = R.drawable.ic_personal_info), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))

                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoItem("Phone Number", employee.phoneNumber)

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