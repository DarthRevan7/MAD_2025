package com.example.voyago

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val navController = rememberNavController()
            NavHost(navController= navController, startDestination= "main_page", builder= {
                composable("main_page"){
                    MainPage(navController)
                }
                composable("user_profile") {
                    UserProfileScreen()
                }
                composable("my_profile") {
                    MyProfileScreen()
                }
                composable("travel_proposal") {
                    TravelProposalScreen()
                }
            })
        }
    }
}

@Composable
fun MainPage(navController: NavController) {
    Column (
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            navController.navigate("my_profile")
        }) {
            Text("Go To Own Profile")
        }
        Button(onClick = {
            navController.navigate("user_profile")
        }) {
            Text("Go To Other Profile")
        }
        Button(onClick = {
            navController.navigate("travel_proposal")
        }) {
            Text("View Travel Proposal")
        }
    }
}
