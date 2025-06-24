package com.example.voyago.view

import android.util.Log
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

// Ask the missing information to users that to the registration with google
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteAccount(
    navController: NavController,
    onGoogleSignUpClick: () -> Unit = {},
    uvm: UserViewModel
) {

    // Form states
    var dateOfBirth by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Field interaction flags for validation
    var dateOfBirthTouched by remember { mutableStateOf(false) }
    var countryTouched by remember { mutableStateOf(false) }
    var usernameTouched by remember { mutableStateOf(false) }

    // Additional user data
    var username by remember { mutableStateOf("") }
    var selectedTravelTypes by remember { mutableStateOf(setOf<String>()) }
    var selectedDestinations by remember { mutableStateOf(setOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }

    // Static list of options
    val travelTypes = listOf("ADVENTURE", "PARTY", "CULTURE", "RELAX")
    val allDestinations = isCountryList

    // Filtered destinations based on search
    val filteredDestinations = if (searchQuery.isEmpty()) {
        allDestinations
    } else {
        allDestinations.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    // Outer container
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Placeholder profile icon
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

            // Page title
            Text(
                text = "Complete your account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Page subtitle
            Text(
                text = "Enter your data to sign up for this app",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // DATE OF BIRTH INPUT
            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it; dateOfBirthTouched = true },
                placeholder = {
                    Text(
                        text = "date of birth (yyyy-mm-dd)",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = dateOfBirthTouched && !isValidDateOfBirth(dateOfBirth),
                supportingText = {
                    if (dateOfBirthTouched && dateOfBirth.isEmpty()) {
                        Text("This field cannot be empty")
                    } else if (dateOfBirthTouched && !isValidDateOfBirth(dateOfBirth)) {
                        Text("Invalid date format or underage. Use yyyy-mm-dd format.")
                    }
                },
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

            // COUNTRY INPUT
            OutlinedTextField(
                value = country,
                onValueChange = { country = it; countryTouched = true },
                placeholder = {
                    Text(
                        text = "country",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = countryTouched && !isValidCountry(country),
                supportingText = {
                    if (countryTouched && country.isEmpty()) {
                        Text("This field cannot be empty")
                    } else if (countryTouched && !isValidCountry(country)) {
                        Text("Invalid country name")
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

            // USERNAME INPUT
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; usernameTouched = true },
                placeholder = {
                    Text(
                        text = "username",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = usernameTouched && !isValidUsername(username),
                supportingText = {
                    if (usernameTouched && username.isEmpty()) {
                        Text("This field cannot be empty.")
                    } else if (usernameTouched && !isValidUsername(username)) {
                        Text("Username must start with a letter and be 3-20 characters long.")
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

            Spacer(modifier = Modifier.height(16.dp))

            // TRAVEL TYPE PREFERENCES
            Text(
                text = "Preferences about the type of travel",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Travel type filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                travelTypes.chunked(2).forEach { rowItems ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { type ->
                            FilterChip(
                                onClick = {
                                    selectedTravelTypes = if (selectedTravelTypes.contains(type)) {
                                        selectedTravelTypes - type
                                    } else {
                                        selectedTravelTypes + type
                                    }
                                },
                                label = {
                                    Text(
                                        text = type,
                                        fontSize = 14.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                selected = selectedTravelTypes.contains(type),
                                modifier = Modifier.fillMaxWidth(),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6B46C1),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
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

            // DESTINATIONS SECTION
            Text(
                text = "Most desired destinations",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selected destinations shown as chips with removal option
            if (selectedDestinations.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedDestinations.take(3).forEach { destination ->
                        AssistChip(
                            onClick = {
                                selectedDestinations = selectedDestinations - destination
                            },
                            label = { Text(destination, fontSize = 14.sp) },
                            trailingIcon = {
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Scrollable card containing destination selection list
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
                    items(filteredDestinations) { destination ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedDestinations =
                                        if (selectedDestinations.contains(destination)) {
                                            selectedDestinations - destination
                                        } else {
                                            selectedDestinations + destination
                                        }
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = destination,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            if (selectedDestinations.contains(destination)) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
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

            // Error message display (if any)
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

            // SUBMIT BUTTON
            Button(
                onClick = {
                    // Validations before submission
                    if (!isValidUsername(username)) {
                        usernameTouched = true
                        return@Button
                    }
                    if (selectedTravelTypes.isEmpty()) {
                        errorMessage = "Please select at least one travel type"
                        return@Button
                    }
                    if (selectedDestinations.isEmpty()) {
                        errorMessage = "Please select at least one destination"
                        return@Button
                    }
                    if (!isValidDateOfBirth(dateOfBirth) || !isValidCountry(country)) {
                        errorMessage = "Please correct the errors above."
                        return@Button
                    }

                    // Build user object
                    val fireUserUid = FirebaseAuth.getInstance().currentUser?.uid

                    var user = User(
                        id = -1,
                        firstname = uvm.account?.givenName.toString(),
                        surname = uvm.account?.familyName.toString(),
                        country = country,
                        username = username,
                        email = uvm.account?.email.toString(),
                        userDescription = "",
                        dateOfBirth = Timestamp(dateOfBirth.toCalendar().time),
                        password = "",
                        profilePictureUrl = uvm.account?.photoUrl?.toString(),
                        typeTravel = selectedTravelTypes.map { TypeTravel.valueOf(it) },
                        desiredDestination = selectedDestinations.toList(),
                        rating = 5f,
                        reliability = 100
                    )

                    // Set UID if available
                    if (fireUserUid != null) {
                        user = User(
                            id = -1,
                            uid = fireUserUid,
                            firstname = uvm.account?.givenName.toString(),
                            surname = uvm.account?.familyName.toString(),
                            country = country,
                            username = username,
                            email = uvm.account?.email.toString(),
                            userDescription = "",
                            dateOfBirth = Timestamp(dateOfBirth.toCalendar().time),
                            password = "",
                            profilePictureUrl = uvm.account?.photoUrl?.toString(),
                            typeTravel = selectedTravelTypes.map { TypeTravel.valueOf(it) },
                            desiredDestination = selectedDestinations.toList(),
                            rating = 5f,
                            reliability = 100
                        )
                    }

                    // Store user
                    uvm.createUser(user)

                    // Navigate to profile screen
                    navController.navigate("profile_overview") {
                        popUpTo("home_main") {
                            // Keep the "home_main" destination in the back stack (do not remove it)
                            inclusive = false
                        }
                        // Avoid multiple instances of the same destination on the back stack
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
                Text(
                    text = "Create Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

        }
    }
}