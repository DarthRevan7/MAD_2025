package com.example.voyago.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar

@Composable
fun OwnedTravelProposalList() {
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

                   /*var myPublishedTrips = listOf<Trips>()
                    myPublishedTrips.forEach { item ->
                        Card() {

                        }
                    }*/
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