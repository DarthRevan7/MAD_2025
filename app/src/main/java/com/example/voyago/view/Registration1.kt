package com.example.voyago.view

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(navController: NavController) {

    // User input state holders
    var name by rememberSaveable { mutableStateOf("") }
    var surname by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var dateOfBirth by rememberSaveable { mutableStateOf("") }
    var country by rememberSaveable { mutableStateOf("") }

    // For displaying general form validation error
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Touched flags help us only validate fields after the user interacts with them
    var nameTouched by rememberSaveable { mutableStateOf(false) }
    var surnameTouched by rememberSaveable { mutableStateOf(false) }
    var emailTouched by rememberSaveable { mutableStateOf(false) }
    var passwordTouched by rememberSaveable { mutableStateOf(false) }
    var dateOfBirthTouched by rememberSaveable { mutableStateOf(false) }
    var countryTouched by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // Main content column with padding and scroll support
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Profile placeholder icon
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

            // Title text
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

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameTouched = true },
                placeholder = {
                    Text(
                        text = "name",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameTouched && !isValidName(name),
                supportingText = {
                    // If the field is empty
                    if (nameTouched && name.isEmpty()) {
                        Text("This field cannot be empty")
                        // If the field is not valid
                    } else if (nameTouched && !isValidName(name)) {
                        Text("Invalid name format. Only letters, spaces, apostrophes, and hyphens are allowed.")
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

            // Surname Field
            OutlinedTextField(
                value = surname,
                onValueChange = { surname = it; surnameTouched = true },
                placeholder = {
                    Text(
                        text = "surname",
                        color = Color.Gray
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = surnameTouched && !isValidName(surname),
                supportingText = {
                    // If the field is empty
                    if (nameTouched && name.isEmpty()) {
                        Text("This field cannot be empty")
                        //If the field is not valid
                    } else if (nameTouched && !isValidName(name)) {
                        Text("Invalid name format. Only letters, spaces, apostrophes, and hyphens are allowed.")
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

            // Email Field
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
                isError = emailTouched && !isValidEmail(email),
                supportingText = {
                    // If the field is empty
                    if (emailTouched && email.isEmpty()) {
                        Text("This field cannot be empty")
                        // If the field is not valid
                    } else if (emailTouched && !isValidEmail(email)) {
                        Text("Invalid email format")
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

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
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
                isError = passwordTouched && !isValidPassword(password),
                supportingText = {
                    // If the field is empty
                    if (passwordTouched && password.isEmpty()) {
                        Text("This field cannot be empty")
                        // If the field is not valid
                    } else if (passwordTouched && !isValidPassword(password)) {
                        Text("Password must be at least 8 characters long, include uppercase, lowercase, digit, and special character.")
                    }
                },
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

            // Date of Birth Field
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
                    // If the field is empty
                    if (dateOfBirthTouched && dateOfBirth.isEmpty()) {
                        Text("This field cannot be empty")
                        // If the field is not valid
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

            // Country Field
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
                    // If the field is empty
                    if (countryTouched && country.isEmpty()) {
                        Text("This field cannot be empty")
                        // If the field is not valid
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

            // Continue Button
            Button(
                onClick = {
                    // Validation check for all fields
                    if (!isValidName(name) || !isValidName(surname) || !isValidEmail(email) ||
                        !isValidPassword(password) || !isValidDateOfBirth(dateOfBirth) || !isValidCountry(
                            country
                        )
                    ) {
                        // If not valid set the Error Message and return the Button
                        errorMessage = "Please correct the errors above."
                        return@Button
                    } else {
                        // If valid, pass the values to the next screen via saved state handle
                        val fields = RegistrationFormValues(
                            name,
                            surname,
                            email,
                            password,
                            dateOfBirth,
                            country
                        )

                        // Pass the data using navController's SavedStateHandle
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            "registrationFormValues",
                            fields
                        )

                        // Navigate to the second page for the registration
                        navController.navigate("register2")
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
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Function used to validate Name and Surname
fun isValidName(name: String): Boolean {
    // Trim leading and trailing whitespace from the input
    val trimmed = name.trim()

    // Check the length of the trimmed name
    // Valid names must be at least 1 character and at most 100 characters
    if (trimmed.length !in 1..100) {
        return false
    }

    // Define the allowed pattern using a regular expression
    // ^ and $ ensure the pattern must match the entire string
    // \\p{L} allows any kind of letter from any language
    // \\s allows whitespace characters
    // '- allows apostrophes and hyphens
    // The + means one or more of these characters must be present
    val regex = Regex("^[\\p{L}\\s'-]+$")

    // Return whether the trimmed name matches the pattern
    return regex.matches(trimmed)
}

// Function used to validate the Email
fun isValidEmail(email: String): Boolean {
    // Trim leading and trailing whitespace from the input
    // Check that is not empty
    // Check that respect the email address pattern
    return email.trim().let {
        it.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(it).matches()
    }
}

// Function used to validate the Password
fun isValidPassword(password: String): Boolean {
    // Trim leading and trailing whitespace from the input
    val trimmed = password.trim()

    // Define the allowed pattern using a regular expression
    // ^ and $ ensure the pattern must match the entire string
    //(?=.*[a-z]): Lookahead to ensure at least one lowercase letter
    // (?=.*[A-Z]): Lookahead to ensure at least one uppercase letter
    // (?=.*\\d): Lookahead to ensure at least one digit
    // (?=.*[@#$%^&+=!]): Lookahead to ensure at least one special character from this set
    // .{8,}$: Match any character (.) at least 8 times (.{8,})
    // Summary: at least 8 characters, 1 uppercase, 1 lowercase, 1 digit, 1 special character
    val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")

    // Return whether the trimmed name matches the pattern
    return regex.matches(trimmed)
}

// Function used to validate the Date Of Birth
fun isValidDateOfBirth(dob: String, minAge: Int = 13): Boolean {
    return try {
        // Use 'uuuu' for the Gregorian year, and strict resolution to reject invalid dates like Feb 30
        val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT)

        // Parse the input string using the formatter
        val birthDate = LocalDate.parse(dob.trim(), formatter)

        val today = LocalDate.now()

        // Calculate the age
        val age = Period.between(birthDate, today).years

        // Return true only if date is in the past and meets age requirement
        !birthDate.isAfter(today) && age >= minAge
    } catch (_: DateTimeParseException) {
        false
    }
}


val isCountryList = listOf(
    "Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antigua and Barbuda",
    "Argentina", "Armenia", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain",
    "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bhutan", "Bolivia",
    "Bosnia and Herzegovina", "Botswana", "Brazil", "Brunei", "Bulgaria", "Burkina Faso",
    "Burundi", "Cabo Verde", "Cambodia", "Cameroon", "Canada", "Central African Republic",
    "Chad", "Chile", "China", "Colombia", "Comoros", "Congo (Congo-Brazzaville)",
    "Costa Rica", "Croatia", "Cuba", "Cyprus", "Czechia", "Democratic Republic of the Congo",
    "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador",
    "Equatorial Guinea", "Eritrea", "Estonia", "Eswatini", "Ethiopia", "Fiji", "Finland",
    "France", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Greece", "Grenada",
    "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Honduras", "Hungary",
    "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy",
    "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Kuwait", "Kyrgyzstan",
    "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania",
    "Luxembourg", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta",
    "Marshall Islands", "Mauritania", "Mauritius", "Mexico", "Micronesia", "Moldova",
    "Monaco", "Mongolia", "Montenegro", "Morocco", "Mozambique", "Myanmar", "Namibia",
    "Nauru", "Nepal", "Netherlands", "New Zealand", "Nicaragua", "Niger", "Nigeria",
    "North Korea", "North Macedonia", "Norway", "Oman", "Pakistan", "Palau", "Palestine State",
    "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland", "Portugal",
    "Qatar", "Romania", "Russia", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia",
    "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe",
    "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore",
    "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Korea",
    "South Sudan", "Spain", "Sri Lanka", "Sudan", "Suriname", "Sweden", "Switzerland",
    "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tonga",
    "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Tuvalu", "Uganda",
    "Ukraine", "United Arab Emirates", "UK", "United Kingdom", "USA", "United States of America",
    "Uruguay", "Uzbekistan", "Vanuatu", "Vatican City", "Venezuela", "Vietnam", "Yemen",
    "Zambia", "Zimbabwe"
)

// Function used to validate the Country
fun isValidCountry(input: String): Boolean {
    // If the country is in the isCountryList return true
    return isCountryList.any { it.equals(input.trim(), ignoreCase = true) }
}
