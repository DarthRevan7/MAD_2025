package com.example.voyago.view

import android.graphics.Paint
import android.provider.ContactsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.voyago.TravelProposalScreen
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.MainPage
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.activities.TopBar
import com.example.voyago.model.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import com.example.voyago.tripList

@Composable
fun EditProfileScreen()
{
    //Delete later
    var textList = remember { mutableStateListOf("Serios", "White", "", "", "","","","","","","","","","","","","") }



    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(4)
        },
    ) {
        innerPadding ->

        /*
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

Image(
    painter = painterResource(R.drawable.rome_photo),
    contentDescription = "Image before texts",
    modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .padding(bottom = 16.dp)
)
}

item {

Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp)
) {
    Text(
        text = "Left Aligned Text",
        modifier = Modifier.align(Alignment.CenterVertically)
    )
    Spacer(modifier = Modifier.weight(1f))
    Text(
        text = "Right Aligned Text",
        modifier = Modifier.align(Alignment.CenterVertically)
    )
}
}

item {
ItineraryTitleBox()
}

items(3) { index ->
ItineraryText(
    modifier = Modifier
        .padding(start = 24.dp, top = 16.dp)
)
}
}
*/
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                //Box with profile image and initials
                Box(modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )//.background(Color(0xdf, 0xd1, 0xe0, 255), shape = RectangleShape))
                {
                    ProfilePhotoEditing("Serious", "Game",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            item {
                //Text with Edit Profile
                Box( modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight())
                {
                    Text(text = "Edit Profile",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.TopCenter).padding(bottom = 15.dp),
                        fontSize = 20.sp)
                }
            }

            item {
                //Stuff to edit
                /*LazyColumn (verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RectangleShape)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ){

                    items(textList.size) { index ->
                        TextField(
                            value = textList[index],
                            onValueChange = { textList[index] = it },
                            label = { },//Text("Campo #$index") },
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                    }
                }*/
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RectangleShape)
                        .fillMaxWidth()
                ) {
                    var index = 0
                    textList.forEach {
                            item, ->
                            TextField(
                                value = item,
                                onValueChange = { textList[index] = it },
                                label = { },//Text("Campo #$index") },
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            )
                            index++
                    }
                }
            }
        }

    }
}

@Composable
fun ProfilePhotoEditing(firstname: String, surname: String, modifier: Modifier = Modifier) {
    val initials = "${firstname.first()}"+"${surname.first()}"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(130.dp)
            .background(Color.Blue, shape = CircleShape)
    ) {
        Text(
            text = initials,
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
    }
}