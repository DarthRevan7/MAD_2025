package com.example.voyago

import androidx.compose.*
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.ui.*
import androidx.compose.material3.*
//Warning x non importare entrambe le due librerie
//import androidx.compose.material.*
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.style.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherProfile() {

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Top app bar")
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Bottom app bar",
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text =
                    """
                    This is an example of a scaffold. It uses the Scaffold composable's parameters to create a screen with a simple top app bar, bottom app bar, and floating action button.

                    It also contains some basic inner content, such as this text.
                """.trimIndent(),
            )
        }
    }
    
    /*
    //Column for all page
    Column(modifier =
    Modifier
        .width(412.dp)
        .height(892.dp)
        .background(Color(255,255,255,25), shape = RectangleShape))
    {
        //Column for the upper part, where you see hour, battery and phone datas

        Column(modifier = Modifier.width(412.dp).height(22.dp).background(Color.Blue, shape = RectangleShape))
        {

        }

        Column(modifier = Modifier.width(412.dp).height(62.dp).background(Color(0xE6,0xE0,0xE9,255), shape = RectangleShape))
        {

        }

        Column(modifier = Modifier.width(412.dp).height(245.dp).background(Color(0xC1,0xA5,0xC3,255), shape = RectangleShape))
        {

        }

        Column(modifier = Modifier.width(412.dp).height(25.dp).background(Color.White, shape = RectangleShape))
        {

        }
        Column(modifier = Modifier.width(412.dp).height(50.dp).background(Color(0xfe,0xf7,0xff,255), shape = RectangleShape))
        {

        }
        Column(modifier = Modifier.width(412.dp).height(150.dp).background(Color.White, shape = RectangleShape))
        {

        }
        Column(modifier = Modifier.width(412.dp).height(60.dp).background(Color(0xF3,0xED,0xF7,255), shape = RectangleShape))
        {

        }
        Column(modifier = Modifier.width(412.dp).height(13.dp).background(Color.White, shape = RectangleShape))
        {

        }
        
     

    }


*/

}