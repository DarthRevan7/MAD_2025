package com.example.voyago

import android.graphics.Paint
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelProposalScreen() {

    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(false)
        }
    ) { innerPadding ->
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            item{
                Spacer(modifier = Modifier.height(3.dp))
            }

            item {
                Hero()
            }

            item{
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp)
                ) {
                    Text(
                        text = "March 16th - March 20th, 2025 \n" +
                                "4 people (2 spots left)",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "750 €",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            item{
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                ItineraryTitleBox()
            }

            item {
                ItineraryText(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 16.dp, end = 20.dp)
                )
            }
        }
    }
}


@Composable
fun ItineraryTitleBox() {
    Box(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp), clip = false)
            .background(
                color = Color(0xFFF4F4F4))
    ) {
        Text(
            text = "My Itinerary",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )
    }
}

@Composable
fun ItineraryText(modifier: Modifier = Modifier) {
    Text(
        text = "Day 1 - Arrival & Ancient Rome\n" +
                "09:00 Arrival in Rome\n" +
                "10:30 Hotel check-in and brief rest (Independent)\n" +
                "12:00 Lunch near the Colosseum (Group)\n" +
                "13:30 Visit the Colosseum and Roman Forum – Guided Tour (Group)\n" +
                "17:00 Walk up Palatine Hill – Scenic Views (Independent)\n" +
                "19:30 Dinner at a traditional Roman trattoria (Group) \n\n" +

                "Day 2 - Vatican City & Art\n" +
                "08:00 Breakfast at hotel (Independent)\n" +
                "09:00 Visit Vatican Museums & Sistine Chapel – Skip-the-line ticket (Group)\n" +
                "12:30 Lunch near St. Peter’s Square (Independent)\n" +
                "14:00 Climb to the top of St. Peter’s Basilica Dome (Independent)\n" +
                "16:00 Explore Castel Sant’Angelo and its bridge (Group)\n" +
                "20:00 Aperitivo in Trastevere + Dinner (Group)\n\n" +

                "Day 3 - Baroque Rome & Hidden Gems\n" +
                "08:30 Morning coffee & cornetto at Piazza Navona (Independent)\n" +
                "10:00 Walking tour: Pantheon, Trevi Fountain, Spanish Steps (Group)\n" +
                "13:00 Lunch in Campo de’ Fiori area (Independent)\n" +
                "15:00 Time for shopping or gelato hunt around Via del Corso (Independent)\n" +
                "18:00 Sunset view from Pincian Hill (Group)\n" +
                "20:30 Optional night walk or open-air opera (Group)\n\n" +

                "Day 4 - Roman Lifestyle & Nature\n" +
                "08:00 Breakfast in a local café (Independent)\n" +
                "09:00 Visit Borghese Gallery and gardens – Pre-booked ticket (Group)\n" +
                "12:30 Picnic in Villa Borghese Park (Group)\n" +
                "14:30 Free afternoon to explore Monti or Testaccio neighborhoods (Independent)\n" +
                "17:30 Cooking class: Make fresh pasta & tiramisù (Group)\n" +
                "20:00 Enjoy self-cooked dinner with wine (Group)\n\n" +

                "Day 5 - Departure & Last Explorations\n" +
                "08:00 Early walk at Tiber River banks or local market (Independent)\n" +
                "09:30 Farewell group brunch (Group)\n" +
                "11:00 Quick stop at any missed sites or souvenir shopping (Independent)\n" +
                "13:00 Return to hotel & check-out (Independent)\n" +
                "15:00 Departure from Rome",

        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier

    )
}

@Composable
fun Hero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.rome_photo),
            contentDescription = "Trip Hero photo",
            modifier = Modifier
                .fillMaxSize()
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(vertical = 30.dp, horizontal = 10.dp)
                .background(
                    color = Color(0xAA444444), // semi-transparent dark grey
                    shape = MaterialTheme.shapes.small
                )

        ) {
            Text(
                text = "ROME \n" +
                        "Discover Rome in 5 days",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )

        }
    }
}


@Preview
@Composable
fun TravelProposalScreenPreview() {
    TravelProposalScreen()
}
