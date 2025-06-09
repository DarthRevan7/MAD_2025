package com.example.voyago.view

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.voyago.R
import com.example.voyago.viewmodel.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController, auth: FirebaseAuth,
    uvm: UserViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }

    // Google Sign-In state
    var isGoogleLoading by remember { mutableStateOf(false) }

    // Configure Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Launcher for Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleGoogleSignInResult(
            result = result,
            auth = auth,
            navController = navController,
            setError = { errorMessage = it },
            setLoading = { isGoogleLoading = it },
            uvm = uvm
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        item {
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
                    text = "Log in existing account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "Enter your email and password to log in for this app",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Email TextField
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        if (emailTouched && email.isBlank()) {
                            Text("This field cannot be empty")
                        }
                    },
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
                    onValueChange = { password = it; passwordTouched = true },
                    placeholder = {
                        Text(
                            text = "password",
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = passwordTouched && password.isBlank(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        if (passwordTouched && password.isBlank()) {
                            Text("This field cannot be empty")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6B46C1),
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Login Button
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Email and password cannot be empty"
                            return@Button
                        } else {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userId = auth.currentUser?.uid
                                        userId?.let { saveFcmTokenToFirestore(it) }

                                        navController.navigate("home_main") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        errorMessage = "Wrong email or password"
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
                        text = "Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Forgot Password
                TextButton(onClick = {
                    navController.navigate("retrieve_password")
                }) {
                    Text(
                        text = "Forgot your password?",
                        color = Color(0xFF6B46C1),
                        fontSize = 16.sp,
                        textDecoration = TextDecoration.Underline
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error Message
                errorMessage?.let { message ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
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

                // Or text
                Text(
                    text = "or",
                    color = Color.Gray,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sign In Button
                Button(
                    onClick = {
                        navController.navigate("register")
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
                        text = "Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Sign-In Button
                Button(
                    onClick = {
                        isGoogleLoading = true
                        errorMessage = null
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.LightGray),
                    enabled = !isGoogleLoading
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "G",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4285F4),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = if (isGoogleLoading) "Signing in..." else "Continue with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun handleGoogleSignInResult(
    result: androidx.activity.result.ActivityResult,
    auth: FirebaseAuth,
    navController: NavHostController,
    setError: (String?) -> Unit,
    setLoading: (Boolean) -> Unit,
    uvm: UserViewModel
) {
    setLoading(false)
    if (result.resultCode == Activity.RESULT_OK) {
        val data = result.data
        if (data == null) {
            setError("Google sign-in failed: No intent data.")
            return
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrEmpty()) {
                setError("Google sign-in failed: Missing ID token.")
                return
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        userId?.let { saveFcmTokenToFirestore(it) }

                        navController.navigate("home_main") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        setError("Google sign-in failed: ${authResult.exception?.localizedMessage ?: "Unknown error"}")
                    }
                }
        } catch (e: Exception) {
            setError("Google sign-in failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    } else {
        setError("Google sign-in canceled.")
    }
}

fun saveFcmTokenToFirestore(userId: String) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val token = task.result
            val userRef = Firebase.firestore.collection("users").document(userId)
            userRef.update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "Token saved successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Failed to save token: ${e.localizedMessage}")
                }
        } else {
            Log.e("FCM", "Fetching FCM token failed: ${task.exception?.localizedMessage}")
        }
    }
}


fun checkUserExistsByEmail(email: String, callback: (Boolean) -> Unit) {
    Firebase.firestore.collection("users")
        .whereEqualTo("email", email)
        .limit(1)
        .get()
        .addOnSuccessListener { documents ->
            callback(!documents.isEmpty)
        }
        .addOnFailureListener {
            callback(false) // Or handle error more explicitly
        }
}


/*
@Preview(showBackground = true, device = "spec:width=412dp,height=892dp")
@Composable
fun VoyagoLoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
        )
    }
}

@Preview(showBackground = true, device = "spec:width=412dp,height=892dp")
@Composable
fun VoyagoLoginScreenWithErrorPreview() {
    MaterialTheme {
        LoginScreen(
            errorMessage = "Wrong email or password"
        )
    }
}*/
