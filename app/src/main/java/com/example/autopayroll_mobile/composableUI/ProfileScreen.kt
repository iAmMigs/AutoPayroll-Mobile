package com.example.autopayroll_mobile.composableUI

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
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
import com.example.autopayroll_mobile.utils.TutorialManager
import com.example.autopayroll_mobile.utils.TutorialStep
import com.example.autopayroll_mobile.viewmodel.ProfileViewModel
import com.google.gson.Gson
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

val AppBackground = Color(0xFFEEEEEE)
val CardSurface = Color.White

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val isTutorialActive by TutorialManager.isTutorialActive.collectAsState()
    val currentStep by TutorialManager.currentStep.collectAsState()

    var backBtnRect by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(currentStep) {
        if (isTutorialActive && currentStep == TutorialStep.NAVIGATE_TO_PROFILE) {
            TutorialManager.nextStep(TutorialStep.PROFILE_OVERVIEW)
        }
    }

    // BULLETPROOF MOCK DATA
    val activeEmployee = remember(isTutorialActive, uiState.employee) {
        if (isTutorialActive) {
            try {
                Gson().fromJson("""{
                    "employee_id": "EMP-001", "first_name": "Juan", "last_name": "Cruz",
                    "job_position": "Software Engineer", "company_name": "AutoPayroll",
                    "gender": "Male", "marital_status": "Single", "birthdate": "1995-01-01",
                    "blood_type": "O+", "street": "123 Main St", "city": "Manila",
                    "employment_type": "Regular", "email": "juan@example.com"
                }""", Employee::class.java)
            } catch (e: Exception) { null }
        } else uiState.employee
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading && !isTutorialActive) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (uiState.error != null && !isTutorialActive) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.error ?: "Unknown Error", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                    Button(onClick = { profileViewModel.fetchEmployeeData() }) { Text("Retry") }
                }
            }
        } else if (activeEmployee != null) {
            val employee = activeEmployee
            val formattedMiddleInitial = employee.middleName?.takeIf { it.isNotBlank() }?.let { "${it.first().uppercaseChar()}." } ?: ""
            val displayFullName = listOfNotNull(employee.firstName, formattedMiddleInitial.takeIf { it.isNotBlank() }, employee.lastName).joinToString(" ")

            Column(modifier = Modifier.fillMaxSize().background(CardSurface).windowInsetsPadding(WindowInsets.statusBars).verticalScroll(rememberScrollState())) {
                Column(modifier = Modifier.fillMaxWidth().background(CardSurface).padding(horizontal = 16.dp).padding(bottom = 24.dp)) {

                    // HIGHLIGHT BACK BUTTON
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .tutorialTarget(isActive = currentStep == TutorialStep.NAVIGATE_BACK_FROM_PROFILE) { backBtnRect = it }
                            .clickable {
                                if (!isTutorialActive || currentStep == TutorialStep.NAVIGATE_BACK_FROM_PROFILE) onBack()
                            }
                            .padding(top = 16.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_back_arrow), contentDescription = "Back")
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("User Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.size(80.dp).clip(CircleShape).border(3.dp, Color(0xFFFFC107), CircleShape).background(Color.LightGray)) {
                            val painter = rememberAsyncImagePainter(model = employee.profilePhoto ?: R.drawable.profiledefault, error = painterResource(id = R.drawable.profiledefault))
                            Image(painter = painter, contentDescription = "Profile Picture", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(displayFullName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("${employee.jobPosition} • ${employee.companyName}", fontSize = 14.sp)
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth().background(CardSurface).padding(horizontal = 16.dp)) {
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

        if (isTutorialActive) {
            when(currentStep) {
                TutorialStep.PROFILE_OVERVIEW -> {
                    TutorialOverlay(title = "Profile Record", description = "Here you can verify all your personal, address, and employment details as saved in the system.", onNext = { TutorialManager.nextStep(TutorialStep.NAVIGATE_BACK_FROM_PROFILE) })
                }
                TutorialStep.NAVIGATE_BACK_FROM_PROFILE -> {
                    TutorialOverlay(title = "Going Back", description = "Tap the 'User Profile' back arrow to return to the menu.", targetRect = backBtnRect, showNextButton = false, onNext = {}, onBack = { TutorialManager.nextStep(TutorialStep.PROFILE_OVERVIEW) })
                }
                else -> {}
            }
        }
    }
}

// Keep existing InfoCard, AddressCard, EmploymentCard, ContactCard, InfoItem, formatApiDate, calculateAge ...
@Composable
fun InfoCard(title: String, icon: Int, employee: Employee) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = icon), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                val formattedMiddleInitial = employee.middleName?.takeIf { it.isNotBlank() }?.let { "${it.first().uppercaseChar()}." } ?: ""
                val fullName = listOfNotNull(employee.firstName, formattedMiddleInitial.takeIf { it.isNotBlank() }, employee.lastName).joinToString(" ")
                InfoItem("Full Name", fullName, Modifier.weight(1f))
                InfoItem("Gender", employee.gender.replaceFirstChar { it.uppercase() }, Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Marital Status", employee.maritalStatus.replaceFirstChar { it.uppercase() }, Modifier.weight(1f))
                InfoItem("Date of Birth", formatApiDate(employee.birthdate), Modifier.weight(1f))
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
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
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = R.drawable.ic_address), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            val residentialAddress = listOfNotNull(employee.street, employee.barangay, employee.city, employee.province, employee.country).joinToString(", ")
            InfoItem("Residential Address", residentialAddress.ifEmpty { "N/A" })
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            val idAddress = listOfNotNull(employee.idStreet, employee.idBarangay, employee.idCity, employee.idProvince, employee.idCountry).joinToString(", ")
            InfoItem("Address on Identification Card", idAddress.ifEmpty { "N/A" })
        }
    }
}

@Composable
fun EmploymentCard(title: String, employee: Employee) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = R.drawable.ic_payroll), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
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
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = R.drawable.ic_contact), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
        val dateTime = OffsetDateTime.parse(apiDate)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        try {
            val date = LocalDate.parse(apiDate)
            date.format(outputFormatter)
        } catch (e2: Exception) { apiDate }
    }
}

private fun calculateAge(birthdate: String): Int? {
    if (birthdate.isBlank()) return null
    return try {
        val parsedDate = OffsetDateTime.parse(birthdate).toLocalDate()
        Period.between(parsedDate, LocalDate.now()).years
    } catch (e: Exception) {
        try {
            val parsedDate = LocalDate.parse(birthdate)
            Period.between(parsedDate, LocalDate.now()).years
        } catch (e2: Exception) { null }
    }
}