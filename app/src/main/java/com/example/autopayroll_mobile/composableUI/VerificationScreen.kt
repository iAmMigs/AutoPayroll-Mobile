package com.example.autopayroll_mobile.composableUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme

@Composable
fun VerificationScreen(
    onVerify: (String) -> Unit,
    onCancel: () -> Unit,
    onResend: () -> Unit
) {
    var otpValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.5f))
        Image(
            painter = painterResource(id = R.drawable.autopayrolltitle),
            contentDescription = "Application Logo",
            modifier = Modifier
                .height(100.dp)
                .padding(bottom = 50.dp)
        )
        Text(
            text = "Enter Verification Code",
            fontSize = 22.sp,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We've sent a code to example@gmail.com",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(40.dp))
        OtpTextField(
            otpText = otpValue,
            onOtpTextChange = { value, _ ->
                otpValue = value
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        TextButton(onClick = onResend) {
            Text(
                text = "Didn't get a code? Click to resend.",
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp), // Increased bottom padding
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(55.dp)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
            ) {
                Text(text = "Cancel", color = Color.Black)
            }
            Button(
                onClick = { onVerify(otpValue) },
                modifier = Modifier
                    .weight(1f)
                    .height(55.dp)
                    .padding(start = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
            ) {
                Text(text = "Verify", color = Color.Black)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OtpTextField(
    modifier: Modifier = Modifier,
    otpText: String,
    otpCount: Int = 6,
    onOtpTextChange: (String, Boolean) -> Unit
) {
    // A list to hold the individual digit values
    val otpValues = remember { mutableStateListOf(*Array(otpCount) { "" }) }
    // A list of focus requesters, one for each box
    val focusRequesters = remember { List(otpCount) { FocusRequester() } }
    val keyboardController = LocalSoftwareKeyboardController.current

    // This effect synchronizes the external otpText state with the internal list
    // This is useful if the state is cleared from outside
    LaunchedEffect(otpText) {
        if (otpText.length <= otpCount && otpText != otpValues.joinToString("")) {
            otpValues.forEachIndexed { index, _ ->
                otpValues[index] = otpText.getOrNull(index)?.toString() ?: ""
            }
        }
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        repeat(otpCount) { index ->
            OutlinedTextField(
                modifier = Modifier
                    .width(50.dp)
                    .padding(2.dp)
                    .focusRequester(focusRequesters[index])
                    .onKeyEvent { event ->
                        // Handle backspace navigation
                        if (event.key == Key.Backspace &&
                            event.type == KeyEventType.KeyUp &&
                            otpValues[index].isEmpty() &&
                            index > 0
                        ) {
                            focusRequesters[index - 1].requestFocus()
                            return@onKeyEvent true
                        }
                        false
                    },
                value = otpValues[index],
                onValueChange = { newValue ->
                    // Handle space key press to move next
                    if (newValue.endsWith(" ")) {
                        if (index < otpCount - 1) {
                            focusRequesters[index + 1].requestFocus()
                        }
                        return@OutlinedTextField
                    }

                    // Filter to keep only the last digit entered
                    val newChar = newValue.filter { it.isDigit() }.takeLast(1)

                    if (otpValues[index] != newChar) {
                        otpValues[index] = newChar

                        // Re-calculate the full OTP string
                        val fullOtp = otpValues.joinToString("")
                        onOtpTextChange(fullOtp, fullOtp.length == otpCount)

                        // Move focus to the next box if a digit was entered
                        if (newChar.isNotEmpty() && index < otpCount - 1) {
                            focusRequesters[index + 1].requestFocus()
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = if (index == otpCount - 1) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (index < otpCount - 1) {
                            focusRequesters[index + 1].requestFocus()
                        }
                    },
                    onDone = {
                        keyboardController?.hide()
                        // Optionally, trigger verification here if otp is full
                        // onVerify(otpValues.joinToString(""))
                    }
                ),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                ),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                )
            )
        }
    }

    // Request focus on the first empty box when the composable is first displayed
    LaunchedEffect(Unit) {
        val firstEmpty = otpValues.indexOfFirst { it.isEmpty() }
        if (firstEmpty != -1) {
            focusRequesters[firstEmpty].requestFocus()
        } else {
            focusRequesters[0].requestFocus()
        }
        keyboardController?.show()
    }
}

@Preview(showBackground = true)
@Composable
fun VerificationScreenPreview() {
    AutoPayrollMobileTheme {
        VerificationScreen(onVerify = {}, onCancel = {}, onResend = {})
    }
}