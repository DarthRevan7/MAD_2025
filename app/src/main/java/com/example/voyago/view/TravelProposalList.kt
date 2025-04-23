@file:JvmName("TravelProposalScreenKt")

package com.example.voyago.view

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voyago.R
import com.example.voyago.activities.*
import com.example.voyago.*
import com.example.voyago.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelProposalList(navController: NavController) {

    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(0)
        }
    ) { innerPadding ->

        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item{

                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f) // Ensure the Row takes up the full width
                        .padding(top = 15.dp), // Padding for the Row
                    horizontalArrangement = Arrangement.Start // Align items to the left in the Row
                ) {
                    Button(
                        onClick = {
                            // Click functionality
                        },
                        border = BorderStroke(1.dp, Color.Black),
                        shape = RoundedCornerShape(10.dp),// Border with rounded corne
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(193, 165, 195), // Change background color
                            contentColor = Color.Black // Change text color
                        )
                    ) {
                        Text("Filters")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 15.dp)
                        .height(200.dp)
                        .clip(RoundedCornerShape(30.dp)) // round corners for the whole box
                ) {
                    // uncomment the follow lines of code for the image.
//                    Image(
//                        painter = painterResource(id = R.drawable.your_image), // replace with your image
//                        contentDescription = "Background image",
//                        modifier = Modifier
//                            .fillMaxSize(),
//                        contentScale = ContentScale.Crop // makes the image fill the box nicely
//                    )

                    // semi-transparent overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x55000000)) // optional overlay for better text visibility
                    )

                    // Content (text, icons, etc.) goes here
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "Title or Info",
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "More details here",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Price",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Date - Days",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Participants",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
