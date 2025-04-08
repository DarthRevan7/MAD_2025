package com.example.voyago.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.voyago.view.MyProfileScreen
import com.example.voyago.viewmodel.MyProfileViewModel



class MyProfileActivity : ComponentActivity() {
    private val viewModel: MyProfileViewModel by viewModels()       //What is this?? Check it!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyProfileScreen(viewModel)
        }
    }
}




