package com.example.autopayroll_mobile.composableUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autopayroll_mobile.R
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField

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
            modifier = Modifier.fillMaxWidth(),
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

@Composable
fun OtpTextField(
    modifier: Modifier = Modifier,
    otpText: String,
    otpCount: Int = 6,
    onOtpTextChange: (String, Boolean) -> Unit
) {
    BasicTextField(
        modifier = modifier,
        value = otpText,
        onValueChange = {
            if (it.length <= otpCount) {
                onOtpTextChange.invoke(it, it.length == otpCount)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(otpCount) { index ->
                    val char = when {
                        index >= otpText.length -> ""
                        else -> otpText[index].toString()
                    }
                    val isFocused = otpText.length == index
                    OutlinedTextField(
                        modifier = Modifier
                            .width(50.dp)
                            .padding(2.dp),
                        value = char,
                        onValueChange = {},
                        readOnly = true,
                        shape = RoundedCornerShape(8.dp),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }
    )
}