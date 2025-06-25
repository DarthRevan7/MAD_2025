package com.example.voyago.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voyago.model.TypeTravel
import com.example.voyago.model.User
import com.example.voyago.toCalendar
import com.example.voyago.viewmodel.UserViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.io.Serializable

// Data structure to pass registration data between screens
data class RegistrationFormValues(
    val name: String,
    val surname: String,
    val email: String,
    val password: String,
    val dateOfBirth: String,
    val country: String
) : Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccount2Screen(navController: NavController, uvm: UserViewModel) {
    // Retrieve previously saved registration form values from the navigation back stack
    val fields =
        navController.previousBackStackEntry?.savedStateHandle?.get<RegistrationFormValues>("registrationFormValues")

    // State variables to manage user input
    var username by rememberSaveable { mutableStateOf("") }
    var selectedTravelTypes by rememberSaveable { mutableStateOf(setOf<String>()) }
    var selectedDestinations by rememberSaveable { mutableStateOf(setOf<String>()) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var message by rememberSaveable { mutableStateOf("") }

    // Variable that manages query
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // Variable that track if the user interacted with the username field
    var usernameTouched by rememberSaveable { mutableStateOf(false) }

    // Boolean that tells if the username already exists
    var alreadyExists by rememberSaveable { mutableStateOf(false) }

    // Travel type options
    val travelTypes = listOf("ADVENTURE", "PARTY", "CULTURE", "RELAX")

    // List of available destinations
    val allDestinations = isCountryList

    // Filter destinations by search query
    val filteredDestinations = if (searchQuery.isEmpty()) {
        // If the query is empty return all destinations
        allDestinations
    } else {
        // Otherwise filter destinations by query
        allDestinations.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Scrollable content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // User profile icon placeholder
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

            // Main title
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
                text = "Complete your account and choose your preferences",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Username input field
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it; usernameTouched = true
                    // Check if username already exists
                    uvm.checkUserExistsAsync(
                        username
                    ) { exists, _ -> alreadyExists = exists; }
                },
                placeholder = {
                    Text(
                        text = "username",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = usernameTouched && (!isValidUsername(username) || alreadyExists),
                supportingText = {
                    // If the username is empty
                    if (usernameTouched && username.isEmpty()) {
                        Text("This field cannot be empty.", color = Color.Red)
                        // If the username is not valid
                    } else if (usernameTouched && !isValidUsername(username)) {
                        Text("Username must start with a letter and be 3-20 characters long.")
                        // If the username already exists
                    } else if (usernameTouched && alreadyExists) {
                        Text("Username already exists!")
                    }
                },
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

            // Travel Preferences Section
            Text(
                text = "Preferences about the type of travel",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Travel Type Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Split the list of travelTypes into sublists of 2 items each
                travelTypes.chunked(2).forEach { rowItems ->
                    // Create a column for each chunk
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { type ->
                            // Iterate through the travel types in this chunk
                            FilterChip(
                                onClick = {
                                    // Toggle selection state: add or remove the type from the selected set
                                    selectedTravelTypes = if (selectedTravelTypes.contains(type)) {
                                        selectedTravelTypes - type
                                    } else {
                                        selectedTravelTypes + type
                                    }
                                },
                                label = {
                                    // Display the travel type name inside the chip
                                    Text(
                                        text = type,
                                        fontSize = 14.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                selected = selectedTravelTypes.contains(type),      // Highlight if selected
                                modifier = Modifier.fillMaxWidth(),
                                // Define visual styling for chip colors
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6B46C1),     // Purple when selected
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,   // White background when not selected
                                    labelColor = Color.Gray
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedTravelTypes.contains(type),
                                    borderColor = Color.LightGray,
                                    selectedBorderColor = Color(0xFF6B46C1)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Destinations Section
            Text(
                text = "Most desired destinations",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Destinations Chips
            if (selectedDestinations.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Display up to 3 selected destinations as AssistChips
                    selectedDestinations.take(3).forEach { destination ->
                        AssistChip(
                            onClick = {
                                // Remove the destination from the selected list when clicked
                                selectedDestinations = selectedDestinations - destination
                            },
                            label = {
                                Text(
                                    destination,
                                    fontSize = 14.sp
                                )
                            },    // Destination name on the chip
                            trailingIcon = {
                                // Display a close icon to indicate removal
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFFE1D5F7),
                                labelColor = Color(0xFF6B46C1)
                            )
                        )
                    }
                }

                // Add vertical spacing below the chips
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Card for displaying a scrollable list of all destinations
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Display each filtered destination as a selectable row
                    items(filteredDestinations) { destination ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Toggle selection: add or remove destination
                                    selectedDestinations =
                                        if (selectedDestinations.contains(destination)) {
                                            selectedDestinations - destination
                                        } else {
                                            selectedDestinations + destination
                                        }
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,   // Space between text and icon
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Destination name
                            Text(
                                text = destination,
                                fontSize = 16.sp,
                                color = Color.Black
                            )

                            // Show a green checkmark if the destination is selected
                            if (selectedDestinations.contains(destination)) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Add a divider between items (except after the last one)
                        if (destination != filteredDestinations.last()) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // If the error message is not empty display it in a card
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

            Spacer(modifier = Modifier.height(32.dp))

            // Create Account Button
            Button(
                onClick = {
                    // Input Validation Section

                    // If the username is invalid or already taken, show error and prevent submission
                    if (!isValidUsername(username) || alreadyExists) {
                        usernameTouched = true
                        return@Button
                    }

                    // Ensure the user has selected at least one travel type
                    if (selectedTravelTypes.isEmpty()) {
                        errorMessage = "Please select at least one travel type"
                        return@Button
                    }

                    // Ensure the user has selected at least one desired destination
                    if (selectedDestinations.isEmpty()) {
                        errorMessage = "Please select at least one destination"
                        return@Button
                    }

                    // User Object Creation and Registration

                    // Placeholder user object in case 'fields' is null
                    var user = User()

                    // If previous form data exists, build a complete User object
                    if (fields != null) {
                        user = User(
                            id = -1,    // Temporary placeholder ID
                            uid = "",   // Firebase UID will be set after registration
                            firstname = fields.name,
                            surname = fields.surname,
                            username = username,
                            country = fields.country,
                            email = fields.email,
                            password = fields.password,
                            userDescription = "",
                            dateOfBirth = Timestamp(fields.dateOfBirth.toCalendar().time),
                            profilePictureUrl = null,
                            typeTravel = selectedTravelTypes.map { TypeTravel.valueOf(it) },    // Convert travel types from String to Enum
                            desiredDestination = selectedDestinations.toList(),
                            rating = 5f,    // Default rating
                            reliability = 100   // Default reliability
                        )

                        // Store user data locally in the ViewModel
                        uvm.storeUser(user)

                        // Register user with Firebase Authentication
                        val auth = FirebaseAuth.getInstance()
                        auth.createUserWithEmailAndPassword(user.email, fields.password)
                            .addOnSuccessListener { result ->
                                // If registration succeeds, send a verification email
                                result.user?.sendEmailVerification()
                                    ?.addOnSuccessListener {
                                        message =
                                            "Verification email sent. Please check your inbox."
                                    }
                                    ?.addOnFailureListener {
                                        message = "Failed to send verification email."
                                    }
                            }
                            .addOnFailureListener {
                                // Handle registration failure
                                message = "Registration failed: ${it.message}"
                            }
                    }

                    // Note: We don't call createUser here since the user will be created after email verification

                    // Pass the pending user to the next screen using saved state handle
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "userValues",
                        uvm.pendingUser
                    )

                    // Navigate to verification code screen and remove registration screen from back stack
                    navController.navigate("register_verification") {
                        popUpTo("register") {
                            // Remove previous screen to prevent back navigation
                            inclusive = true
                        }
                        // Prevent duplicate destinations on the stack
                        launchSingleTop = true
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
                // Button label
                Text(
                    text = "Create account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // Spacer below the button
            Spacer(modifier = Modifier.height(32.dp))

            // Display message text
            Text(text = message)
        }
    }
}

// Function used to validate the Username
fun isValidUsername(username: String): Boolean {
    // Remove leading/trailing whitespace from input
    val trimmed = username.trim()

    // Regex pattern:
    // ^[a-zA-Z] -> Must start with a letter
    // [a-zA-Z0-9_]{2,19} -> Followed by 2 to 19 characters that are letters, digits, or underscores
    // Username must start with a letter, and contain only letters, digits, or underscores
    val regex = Regex("^[a-zA-Z][a-zA-Z0-9_]{2,19}$")

    // Check if the trimmed username matches the pattern
    return regex.matches(trimmed)
}
