package com.example.voyago.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.voyago.view.*
import com.example.voyago.viewmodel.*

class MyProfileActivity : ComponentActivity() {
    private val viewModel: ProfileViewModel by viewModels()       //What is this?? Check it!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {



            //NAV CONTROLLER
            val navController = rememberNavController()
            val context = LocalContext.current

            MyProfileScreen(viewModel, true, navController, context)
            NavHost(navController= navController, startDestination= "my_profile", builder= {
                composable("my_profile"){

                    MyProfileScreen(viewModel, true, navController, context)
                }
                composable("edit_profile") {
                    EditProfileScreen()
                }
            })

        }
    }
}




