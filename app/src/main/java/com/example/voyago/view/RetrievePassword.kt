package com.example.voyago.view


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetrievePassword(navController: NavController) {

    // State variables for user input and UI feedback
    var email by remember { mutableStateOf("") }
    var emailTouched by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Outer container with background color
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // Inner content container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // User icon placeholder
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

            // Title text
            Text(
                text = "Retrieve your password",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle text
            Text(
                text = "Enter your email to reset your password",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email input field with error handling
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailTouched = true },
                placeholder = {
                    Text(
                        text = "email@domain.com",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = emailTouched && email.isBlank(),
                supportingText = {
                    if (emailTouched && email.isBlank()) {
                        Text("This field cannot be empty")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6B46C1),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Button to send password reset email
            Button(
                onClick = {
                    // If the email is not empty
                    if (email.isNotBlank()) {
                        // Get the firebase authentication
                        val auth = FirebaseAuth.getInstance()
                        // Send the email to reset the password
                        auth.sendPasswordResetEmail(email.toString())
                            .addOnCompleteListener { task ->
                                errorMessage = if (task.isSuccessful) {
                                    // If the task is successful
                                    "Password reset email send successfully. Please check your inbox."
                                } else {
                                    // If the task fails
                                    "Failed to send password reset email"
                                }
                            }

                    } else {
                        // Trigger error state if email is empty
                        emailTouched = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B46C1)
                )
            ) {
                Text(
                    text = "Send",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // If the error message is not empty
            if (errorMessage.isNotEmpty()) {
                // Show the message in a card
                errorMessage.let { message ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9) // light green
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            Color(0xFF81C784) // medium green
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
                                color = Color(0xFF388E3C), // dark green
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation back to login screen
            TextButton(onClick = {
                navController.navigate("login") {
                    popUpTo("retrieve_password") {
                        // Remove previous screen to prevent back navigation
                        inclusive = true
                    }
                    // Prevent duplicate destinations on the stack
                    launchSingleTop = true
                }
            }) {
                Text(
                    text = "Go back to login",
                    color = Color(0xFF6B46C1),
                    fontSize = 16.sp,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}