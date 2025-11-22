package com.example.autopayroll_mobile.composableUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.ui.theme.TextPrimary
import com.example.autopayroll_mobile.viewmodel.ChangePasswordNavigationEvent
import com.example.autopayroll_mobile.viewmodel.ChangePasswordViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel,
    onBack: () -> Unit,
    onConfirmReset: () -> Unit // Action to be triggered after internal validation passes
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle Errors and Success messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMsg ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMsg,
                    withDismissAction = true
                )
            }
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { successMsg ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = successMsg,
                    withDismissAction = true
                )
            }
        }
    }

    // Handle Navigation
    LaunchedEffect(viewModel.navigationEvent) {
        viewModel.navigationEvent.collect { event ->
            if (event == ChangePasswordNavigationEvent.NavigateBack) {
                viewModel.clearInputs()
                onBack()
                viewModel.onNavigationHandled()
            }
        }
    }

    // State for password visibility
    var isCurrentPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isNewPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 32.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // --- Header Section ---

            Spacer(Modifier.height(40.dp))
            Text(
                text = "Change Your Password",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Please enter your current password and choose a new one.",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(32.dp))

            // 1. Current Password
            PasswordTextField(
                value = uiState.currentPasswordInput,
                onValueChange = viewModel::onCurrentPasswordChange,
                label = "Current Password",
                isVisible = isCurrentPasswordVisible,
                onToggleVisibility = { isCurrentPasswordVisible = !isCurrentPasswordVisible }
            )
            Spacer(Modifier.height(16.dp))

            // 2. New Password
            PasswordTextField(
                value = uiState.newPasswordInput,
                onValueChange = viewModel::onNewPasswordChange,
                label = "New Password (min 8 chars)",
                isVisible = isNewPasswordVisible,
                onToggleVisibility = { isNewPasswordVisible = !isNewPasswordVisible }
            )
            Spacer(Modifier.height(16.dp))

            // 3. Confirm New Password
            PasswordTextField(
                value = uiState.confirmPasswordInput,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = "Confirm New Password",
                isVisible = isConfirmPasswordVisible,
                onToggleVisibility = { isConfirmPasswordVisible = !isConfirmPasswordVisible }
            )

            Spacer(Modifier.weight(1f))

            // --- Buttons ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp, top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.clearInputs()
                        onBack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(55.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", fontSize = 16.sp)
                }
                Button(
                    onClick = onConfirmReset, // Trigger external confirmation pop-up
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Reset Password", color = Color.Black, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (isVisible) "Hide password" else "Show password"
            IconButton(onClick = onToggleVisibility) {
                Icon(imageVector = image, description)
            }
        }
    )
}