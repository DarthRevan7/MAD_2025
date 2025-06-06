package com.example.voyago.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoyagoCreateAccountScreen(
    onBackClick: () -> Unit = {},
    onContinueClick: (name: String, surname: String, email: String, password: String, dateOfBirth: String, country: String) -> Unit = { _, _, _, _, _, _ -> },
    onGoogleSignUpClick: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Create an account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Enter your data to sign up for this app",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Name TextField
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = {
                    Text(
                        text = "name",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6B46C1),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Surname TextField
            OutlinedTextField(
                value = surname,
                onValueChange = { surname = it },
                placeholder = {
                    Text(
                        text = "surname",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6B46C1),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email TextField
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        text = "email@domain.com",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6B46C1),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password TextField
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        text = "password",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6B46C1),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date of Birth TextField
            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it },
                placeholder = {
                    Text(
                        text = "date of birth (dd/mm/yyyy)",
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

            Spacer(modifier = Modifier.height(16.dp))

            // Country TextField
            OutlinedTextField(
                value = country,
                onValueChange = { country = it },
                placeholder = {
                    Text(
                        text = "country",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6B46C1),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Continue Button
            Button(
                onClick = { onContinueClick(name, surname, email, password, dateOfBirth, country) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B46C1)
                )
            ) {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Or text
            Text(
                text = "or",
                color = Color.Gray,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Google Sign Up Button
            OutlinedButton(
                onClick = onGoogleSignUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.LightGray
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Google Icon placeholder - you would use actual Google icon here
                    Text(
                        text = "G",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Continue with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, device = "spec:width=412dp,height=892dp")
@Composable
fun VoyagoCreateAccountScreenPreview() {
    MaterialTheme {
        VoyagoCreateAccountScreen()
    }
}