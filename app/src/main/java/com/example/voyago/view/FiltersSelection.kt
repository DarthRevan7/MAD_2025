package com.example.voyago.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar
import com.example.voyago.viewmodel.Factory
import com.example.voyago.viewmodel.TripListViewModel

@Composable
fun FilterSelection(navController: NavController, vm: TripListViewModel = viewModel(factory = Factory)) {
    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(1)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {navController.navigate("create_new_travel_proposal")}) {
                Icon(Icons.Default.Add, "Add")
            }
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
                Text("Filter selection Page")
            }
        }

    }
}