package com.example.voyago.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.example.voyago.R
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar
import com.example.voyago.model.romeTrip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnedTravelProposalScreen() {

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
                        text = "${romeTrip.startDate} - ${romeTrip.endDate} \n" +
                                "${romeTrip.sizeGroup} people (${romeTrip.remainingSpots()} spots left)",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${romeTrip.esteemedPrice} €",
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

    val itineraryString = romeTrip.activities.entries.joinToString("\n\n") { (day, activities) ->
        val dayHeader = "$day:\n"
        val activityDescriptions = activities.joinToString("\n") {activity ->
            val groupActivity = if (activity.isGroupActivity) "(group activity)" else ""
            "- ${activity.activityTime} → ${activity.description} $groupActivity"
        }
        dayHeader + activityDescriptions
    }

    Text(
        text = itineraryString,
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
                text = romeTrip.destination +
                        "\n" +
                        romeTrip.tripTitle,
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
    OwnedTravelProposalScreen()
}
