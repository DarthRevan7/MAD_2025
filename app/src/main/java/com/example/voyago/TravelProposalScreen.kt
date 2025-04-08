package com.example.voyago

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

        }
    }
}
