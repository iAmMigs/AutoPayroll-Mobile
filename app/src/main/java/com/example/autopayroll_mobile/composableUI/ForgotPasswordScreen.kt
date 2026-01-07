package com.example.autopayroll_mobile.composableUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import com.example.autopayroll_mobile.viewmodel.ForgotPasswordViewModel

// 1. STATEFUL COMPOSABLE (Used by the Activity)
// This deals with the ViewModel and "State Hoisting"
@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val isLoading by viewModel.isLoading.observeAsState(false)
    val email by viewModel.email.observeAsState("")

    // Pass the state values down to the stateless content
    ForgotPasswordContent(
        email = email,
        isLoading = isLoading,
        onEmailChange = { viewModel.onEmailChange(it) },
        onSubmit = { viewModel.submitRequest() },
        onNavigateBack = onNavigateBack
    )
}

// 2. STATELESS COMPOSABLE (The actual UI)
// This takes pure data, making it easy to Preview without a ViewModel
@Composable
fun ForgotPasswordContent(
    email: String,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.autopayrolltitle),
            contentDescription = "Application Logo"
        )

        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Forgot Password?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter your registered email address, mobile number, or username to receive an OTP for password reset.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email, Number, or Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            shape = RoundedCornerShape(24.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    "SEND RESET LINK",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateBack) {
            Text("Back to Login", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordPreview() {
    AutoPayrollMobileTheme {
        ForgotPasswordContent(
            email = "employee@autopayroll.com",
            isLoading = false,
            onEmailChange = {},
            onSubmit = {},
            onNavigateBack = {}
        )
    }
}