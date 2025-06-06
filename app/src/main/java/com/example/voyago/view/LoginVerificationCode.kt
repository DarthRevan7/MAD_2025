package com.example.voyago.view

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoyagoVerificationCodeScreen(
    onBackClick: () -> Unit = {},
    onConfirmClick: (String) -> Unit = { _ -> },
    onSendAgainClick: () -> Unit = {},
    errorMessage: String? = null,
    onErrorDismiss: () -> Unit = {}
) {
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {


        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Profile Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Color(0xFFE1D5F7),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFF6B46C1)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Retrieve your password",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Enter the code to reset your password",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Code TextField
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                placeholder = {
                    Text(
                        text = "code",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6B46C1),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Confirm Button
            Button(
                onClick = { onConfirmClick(code) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B46C1)
                )
            ) {
                Text(
                    text = "Confirm",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        Color(0xFFE57373)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Or text
            Text(
                text = "or",
                color = Color.Gray,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Send Again Button
            Button(
                onClick = onSendAgainClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B46C1)
                )
            ) {
                Text(
                    text = "Send Again",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=412dp,height=892dp")
@Composable
fun VoyagoVerificationCodeScreenPreview() {
    MaterialTheme {
        VoyagoVerificationCodeScreen()
    }
}

@Preview(showBackground = true, device = "spec:width=412dp,height=892dp")
@Composable
fun VoyagoVerificationCodeScreenWithErrorPreview() {
    MaterialTheme {
        VoyagoVerificationCodeScreen(
            errorMessage = "Invalid code"
        )
    }
}