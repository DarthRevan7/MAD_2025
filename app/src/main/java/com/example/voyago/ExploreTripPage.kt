package com.example.voyago

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ExploreTripPage() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Placeholder rettangolare per l'immagine
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Gray, shape = RectangleShape)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Rome", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Discover Rome in 5 days", fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text("March 16th - March 20th", fontSize = 14.sp, color = Color.Gray)
            Text("4 people (2 seats left)", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text("750 €", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { /* TODO: Handle copy */ }) {
                    Text("Create a copy")
                }
                Button(onClick = { /* TODO: Handle join */ }) { //Insert button color
                    Text("Ask to Join", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Itinerary", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Day 1 – Arrival & Ancient Rome\n• 09:00 Arrival in Rome\n• 14:00 Colosseum + Forum (group tour)\n• 17:30 Free roam Monti\n• 20:00 Group dinner", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Day 2 – Vatican & Views\n• 09:00 Vatican Museums (group tour)\n• 14:30 Free roam Borgo & Prati\n• 18:30 Sunset at Pincian Terrace\n• 20:00 Optional group dinner", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Day 3 – Baroque & Cooking\n• 09:00 Trevi → Pantheon walk (group)\n• 14:00 Pasta cooking class", fontSize = 14.sp)
            }
        }
    }
}
