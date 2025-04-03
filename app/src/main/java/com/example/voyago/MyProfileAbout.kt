package com.example.voyago

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MyProfile() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1E9F4))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Gray))
            Row {
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Gray))
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Gray))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        // Profile picture
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "User Name", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = "97% reliable", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoCard(text = "4.4 approval")
            InfoCard(text = "97% reliable")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs (Mocked, Not Functional)
        TabRow(selectedTabIndex = 1) {
            Tab(selected = false, onClick = {}) { Text("About") }
            Tab(selected = true, onClick = {}) { Text("My trips") }
            Tab(selected = false, onClick = {}) { Text("Reviews") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Section(title = "My Trips") {
            TripItem(city = "Milan", date = "March, 2024")
            TripItem(city = "Paris", date = "August, 2022")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Section(title = "My Articles") {
            TripItem(city = "Top 10 museums in Milan", date = "March, 2024")
            TripItem(city = "Is Paris worth it?", date = "August, 2022")
        }
    }
}

@Composable
fun InfoCard(text: String) {
    Card(
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Gray))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text)
        }
    }
}

@Composable
fun Section(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
fun TripItem(city: String, date: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = city, fontSize = 16.sp)
        Text(text = date, fontSize = 14.sp, color = Color.Gray)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    MyProfile()
}
