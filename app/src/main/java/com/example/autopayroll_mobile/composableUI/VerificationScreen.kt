package com.example.autopayroll_mobile.composableUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.R
import com.example.autopayroll_mobile.ui.theme.AutoPayrollMobileTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextFieldDefaults

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
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    BasicTextField(
        modifier = modifier.focusRequester(focusRequester),
        value = otpText,
        onValueChange = {
            if (it.length <= otpCount) {
                onOtpTextChange.invoke(it, it.length == otpCount)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.clickable {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            ) {
                repeat(otpCount) { index ->
                    val char = when {
                        index >= otpText.length -> ""
                        else -> otpText[index].toString()
                    }
                    OutlinedTextField(
                        modifier = Modifier
                            .width(50.dp)
                            .padding(2.dp),
                        value = char,
                        onValueChange = {},
                        enabled = false, // <--- Important: Makes the field not intercept clicks
                        readOnly = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
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
