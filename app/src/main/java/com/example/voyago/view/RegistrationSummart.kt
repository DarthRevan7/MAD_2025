package com.example.voyago.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class UserRegistrationData(
    val name: String,
    val surname: String,
    val email: String,
    val birthday: String,
    val country: String,
    val username: String,
    val travelPreferences: List<String>,
    val desiredDestinations: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationSummaryScreen(
    userData: UserRegistrationData = UserRegistrationData(
        name = "Lucy",
        surname = "Williams",
        email = "lucywilliams@gmail.com",
        birthday = "18/06/1998",
        country = "Canada",
        username = "lucy98",
        travelPreferences = listOf("Culture", "Relax"),
        desiredDestinations = listOf("Japan")
    ),
    onBackClick: () -> Unit = {},
    onConfirmRegistration: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Profile Icon with gradient background
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Color(0xFF6B46C1),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Welcome Voyago!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Please review your information before creating your account",
                fontSize = 16.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Information Cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    InfoItem(
                        icon = Icons.Outlined.Person,
                        label = "Name",
                        value = userData.name
                    )

                    InfoItem(
                        icon = Icons.Outlined.Person,
                        label = "Surname",
                        value = userData.surname
                    )

                    InfoItem(
                        icon = Icons.Outlined.Person,
                        label = "Username",
                        value = userData.username
                    )

                    InfoItem(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = userData.email
                    )

                    InfoItem(
                        icon = Icons.Outlined.CalendarToday,
                        label = "Birthday",
                        value = userData.birthday
                    )

                    InfoItem(
                        icon = Icons.Default.LocationOn,
                        label = "Country",
                        value = userData.country,
                        isLast = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preferences Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Travel Preferences",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    InfoItem(
                        icon = Icons.Outlined.Explore,
                        label = "Preferred Travel Types",
                        value = userData.travelPreferences.joinToString(", ")
                    )

                    InfoItem(
                        icon = Icons.Outlined.Public,
                        label = "Desired Destinations",
                        value = userData.desiredDestinations.joinToString(", "),
                        isLast = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Confirm Registration Button
            Button(
                onClick = onConfirmRegistration,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6B46C1)
                )
            ) {
                Text(
                    text = "Confirm Registration",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Go back to edit button
            TextButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Go back to edit",
                    fontSize = 14.sp,
                    color = Color(0xFF6B46C1)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp),
            tint = Color(0xFF6B46C1)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF888888),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF1A1A1A),
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=412dp,height=892dp")
@Composable
fun RegistrationSummaryScreenPreview() {
    MaterialTheme {
        RegistrationSummaryScreen()
    }
}