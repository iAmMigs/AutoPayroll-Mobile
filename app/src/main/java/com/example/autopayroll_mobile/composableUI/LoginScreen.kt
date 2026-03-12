package com.example.autopayroll_mobile.composableUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.viewmodel.LoginViewModel

@Composable
fun LoginScreen(loginViewModel: LoginViewModel = viewModel(), onForgotPasswordClick: () -> Unit) {
    val isLoading by loginViewModel.isLoading.observeAsState(false)
    val email by loginViewModel.email.observeAsState("")
    val password by loginViewModel.password.observeAsState("")

    // State to track password visibility
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

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
        Spacer(modifier = Modifier.height(80.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { loginViewModel.onEmailChange(it) },
            label = { Text("Email or Username") },
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
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { loginViewModel.onPasswordChange(it) },
            label = { Text("Password") },
            // Update visual transformation based on visibility state
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            // Add trailing eye icon
            trailingIcon = {
                val image = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (isPasswordVisible) "Hide password" else "Show password"
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            )
        )
        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot Password?", color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { loginViewModel.login() },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onSecondary
                )
            } else {
                Text(
                    "LOGIN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
    }
}