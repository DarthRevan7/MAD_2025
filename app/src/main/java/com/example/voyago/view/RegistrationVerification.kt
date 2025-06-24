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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voyago.model.User
import com.example.voyago.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationVerificationScreen(navController: NavController, uvm: UserViewModel) {
    // Retrieve the User object from the ViewModel's pending user
    var user = uvm.pendingUser

    // Message state to display feedback to the user
    var message by remember { mutableStateOf("") }

    // Outer container filling the whole screen with a light background color
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // Main content column with padding and centered alignment
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Add space from the top
            Spacer(modifier = Modifier.height(60.dp))

            // Circular background box for profile icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Color(0xFFE1D5F7),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center     // Center icon inside the circle
            ) {
                // Person icon representing the user profile
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFF6B46C1)
                )
            }

            // Space below icon
            Spacer(modifier = Modifier.height(32.dp))

            // Title Text prompting the user to confirm registration
            Text(
                text = "Confirm Registration",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Text with instructions
            Text(
                text = "Click on the link sent to your E-mail to confirm your registration",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Space before button
            Spacer(modifier = Modifier.height(40.dp))

            // Button to resend verification email
            Button(
                onClick = {
                    user?.let { currentUser ->
                        // Validate user data before proceeding
                        if (currentUser.email.isEmpty()) {
                            message = "Email is missing. Please try registering again."
                            return@let
                        }
                        
                        val auth = FirebaseAuth.getInstance()
                        // Get the current Firebase user (should already exist from Registration2)
                        val firebaseUser = auth.currentUser
                        
                        if (firebaseUser != null) {
                            // User already exists, just resend verification email
                            firebaseUser.sendEmailVerification()
                                ?.addOnSuccessListener {
                                    message = "Verification email sent. Please check your inbox."
                                }
                                ?.addOnFailureListener {
                                    message = "Failed to send verification email: ${it.message}"
                                }
                        } else {
                            // User might not be signed in, try to sign in first
                            auth.signInWithEmailAndPassword(currentUser.email, currentUser.password)
                                .addOnSuccessListener { result ->
                                    result.user?.sendEmailVerification()
                                        ?.addOnSuccessListener {
                                            message = "Verification email sent. Please check your inbox."
                                        }
                                        ?.addOnFailureListener {
                                            message = "Failed to send verification email: ${it.message}"
                                        }
                                }
                                .addOnFailureListener {
                                    // Handle Firebase security blocks gracefully
                                    if (it.message?.contains("blocked") == true || it.message?.contains("unusual activity") == true) {
                                        message = "Too many attempts. Please try again later or contact support."
                                    } else {
                                        message = "Failed to resend verification email: ${it.message}"
                                    }
                                }
                        }
                    } ?: run {
                        message = "User data not available. Please try again."
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
                    text = "Send Again",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Show feedback message if available
            if (message.isNotEmpty()) {
                message.let { message ->
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
                        // Centered row containing the message text
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


            // Simple separator text
            Text(
                text = "or",
                color = Color.Gray,
                fontSize = 16.sp
            )

            // Space before next button
            Spacer(modifier = Modifier.height(32.dp))

            // Button to confirm registration after user clicked verification link in email
            Button(
                onClick = {
                    // Get current user from Firebase
                    val fireUser = FirebaseAuth.getInstance().currentUser

                    // Refresh user info from ViewModel’s pending user
                    val currentUser = uvm.pendingUser

                    // If both Firebase user and pending user are not null
                    if (fireUser != null && currentUser != null) {
                        // Check if the authenticated Firebase user email matches the saved user email
                        if (fireUser.email == currentUser.email) {
                            // Update user UID with Firebase user UID
                            currentUser.uid = fireUser.uid

                            // Update user data in ViewModel
                            uvm.editUserData(currentUser)

                            // Navigate to profile overview screen
                            navController.navigate("profile_overview") {
                                popUpTo("home_main") {
                                    // Keep home_main in back stack
                                    inclusive = false
                                }
                                // Prevent duplicate destinations on the stack
                                launchSingleTop = true
                            }
                        }
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
                    text = "Click here to confirm registration\n after clicking the link in your E-mail",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}


