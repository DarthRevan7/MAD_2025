package com.example.voyago.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar
import com.example.voyago.viewmodel.Factory
import com.example.voyago.viewmodel.TripListViewModel
import androidx.compose.runtime.collectAsState
import com.example.voyago.model.Trip
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade


@SuppressLint("DiscouragedApi")
@Composable
fun OwnedTravelProposalList(vm: TripListViewModel = viewModel(factory = Factory)) {
    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(1)
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
            item {
                Text(
                    text = "Published Trips:",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Box(
                    modifier = Modifier.wrapContentSize()
                ) {
                    val trips: List<Trip> by vm.tripList.collectAsState()

                    LaunchedEffect(Unit) {
                        vm.creatorPublishedFilter(1)
                    }

                    trips.forEach { item ->
                        Card(
                            modifier = Modifier.padding(16.dp).fillMaxSize(),
                            shape = CardDefaults.elevatedShape,
                            onClick = {}

                        ) {
                            Text(text = item.destination, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                            Text(text = item.title, modifier = Modifier.padding(16.dp))

                            val context = LocalContext.current
                            val drawableId = remember("barcelona") {
                                context.resources.getIdentifier("barcelona", "drawable", context.packageName)
                            }

                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(drawableId)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "paris",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp).offset(y = 300.dp)
                            )

                        }
                    }
                }
            }

            item {
                Text(
                    text = "Private Trips:",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Box(
                    modifier = Modifier.wrapContentSize()
                ) {
                    /*var myPrivateTrips = listOf<Trips>()
                     myPrivateTrips.forEach { item ->
                         Card() {

                         }
                     }*/
                }
            }
        }
    }
}