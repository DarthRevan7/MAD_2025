package com.example.voyago
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.navigation.NavType

//Check below
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "main") {
                composable("main") { MainScreen(navController) }
                composable("userProfile/{isOwner}", arguments = listOf(navArgument("isOwner") { type = NavType.BoolType })) {
                    val isOwner = it.arguments?.getBoolean("isOwner") ?: false
                    UserProfileScreen(isOwner)
                }
                composable("travelProposal") { TravelProposalScreen() }
            }
        }
    }
}


@Composable
fun MainScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.navigate("userProfile/true") }) {
            Text("Go To Own Profile")
        }
        Button(onClick = { navController.navigate("userProfile/false") }) {
            Text("Go To Other Profile")
        }
        Button(onClick = { navController.navigate("travelProposal") }) {
            Text("View Travel Proposal")
        }
    }
}

@Composable
fun UserProfileScreen(isOwner: Boolean) {
    //Text(text = if (isOwner) "User Profile - Owner View" else "User Profile - Other View")
    if(isOwner)
        MyProfile()
    else
        OtherProfile()
}

@Composable
fun TravelProposalScreen() {
    //Text(text = "Travel Proposal View")
    //Gotta fix exploreTripPage!!
    ExploreTripPage()
}
