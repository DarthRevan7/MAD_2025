package com.example.voyago.view

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
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.Uri
import com.example.voyago.R
import com.example.voyago.activities.*
import com.example.voyago.*
import com.example.voyago.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(viewModel: ProfileViewModel, myProfile: Boolean, navController: NavController) {

    //Icons
    val painterLogout = painterResource(R.drawable.logout)
    val painterEdit = painterResource(R.drawable.edit)

    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(4)
        }
    ) { innerPadding ->
        viewModel.getUserData(myProfile)

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
                //Box with Profile Photo, Username and Logout and Edit icons
                Box(modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RectangleShape)) {

                    Image(painter = painterLogout, "logout", modifier = Modifier
                        .size(60.dp)
                        .align(alignment = Alignment.TopEnd)
                        .padding(16.dp)
                        .clickable {/*TODO*/}
                    )

                    Image(painter = painterEdit, "edit", modifier = Modifier
                        .size(60.dp)
                        .align(alignment = Alignment.BottomEnd)
                        .padding(16.dp)
                        .offset(y = (-30).dp)
                        .clickable {  navController.navigate("edit_profile") }
                    )

                    ProfilePhoto(
                        user1.name, user1.surname, false, user1.profileImage,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-50).dp)
                    )
                    Text(
                        text = user1.username,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 10.dp)
                            .offset(y = (40).dp)
                    )


                    Text(
                        text = user1.name + " " + user1.surname,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                            .offset(y = (-50).dp)
                    )
                    Spacer( Modifier.height(20.dp) )
                    Text(
                        text = user1.country,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                            .offset(y = (-20).dp)
                    )
                }
            }

            item {
                //Row with rating and reliability
                Row(
                    modifier = Modifier
                        .offset(y = (-25).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    RatingAndReliability(
                        user1.approvalRate,
                        user1.reliability
                    )
                }
            }

            item {
                //Tab About, My Trips, Review
                TabAboutTripsReview(user1)
            }
        }
    }
}



@Composable
fun RatingAndReliability(rating: Float, reliability: Int) {

    val painterStar = painterResource(R.drawable.star)
    val painterMobile = painterResource(R.drawable.mobile)

    //Box with rating
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(149.dp, 44.dp)
            .background(Color(0xc1, 0xa5, 0xc3, 255), shape = RoundedCornerShape(10.dp))
            .border(2.dp, color = Color.White, shape = RoundedCornerShape(10.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(painter = painterStar, "star", modifier = Modifier
                .size(40.dp)
            )
            Text(
                text = "$rating approval",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
    Spacer(modifier = Modifier.width(16.dp))

    //Box with reliability
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(149.dp, 44.dp)
            .background(Color(0xc1, 0xa5, 0xc3, 255), shape = RoundedCornerShape(10.dp))
            .border(2.dp, color = Color.White, shape = RoundedCornerShape(10.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(painter = painterMobile, "mobile", modifier = Modifier
                .size(30.dp)
            )
            Text(
                text = "${reliability}% reliable",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabAboutTripsReview(user: LazyUser) {

    // TAB with About, trips, Reviews
    val tabs = listOf("About", "Trips", "Reviews")

    var selectedTabIndex by remember {
        mutableIntStateOf(0)
    }

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.background(Color(0xfe, 0xf7, 0xff, 255)),
        contentColor = Color.Black
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = {selectedTabIndex = index},
                text = {
                    Text(title, color = if (index == selectedTabIndex) {
                        Color(0x65, 0x55, 0x8f, 255)
                    } else {
                        Color.Black
                    })
                }
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)
    ) {

        when(selectedTabIndex) {
            0 -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(user.userDescription)
                    Text(text = "Preferences about the type of travel:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    FlowRow(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        user.typeTravelPreferences.forEach { type ->
                            SuggestionChip(
                                onClick = {},
                                label = {Text(type.toString().lowercase())},
                                colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                    labelColor = Color(0x4f, 0x37, 0x8b, 255)
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }

                    Text(text = "Most desired destinations:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    FlowRow(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        user.desiredDestinations.forEach { destination ->
                            SuggestionChip(
                                onClick = {},
                                label = {Text(destination)},
                                colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                                    labelColor = Color(0x4f, 0x37, 0x8b, 255)
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                }
            }
            1 -> {
                Column {
                    Text(text = "Trips:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(140.dp)
                            .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .height((3*43).dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            user.trips.forEach {
                                    item -> UITripArticle(item.destination,item.strDate,item.photo)
                            }
                        }
                    }

                    Text(text = "Articles:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 10.dp)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(140.dp)
                            .wrapContentWidth()
                            .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .height((3*43).dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            user.articles.forEach {
                                    item -> UITripArticle(item.title,item.strDate, null)
                            }
                        }
                    }
                }
            }
            2 -> {
                Column {

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .wrapContentWidth()
                            .wrapContentHeight()
                            .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .height((7*43).dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            user.reviews.forEach {
                                    item -> UIReview(item.name, item.surname, item.rating,item.strDate)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UITripArticle(destination:String, strData:String, image: Int?)
{
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .background(
                Color(0xf9, 0xf6, 0xf9, 255),
                shape = RectangleShape
            )
            .padding(5.dp))
    {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize())
        {
            Row(
                modifier = Modifier
                    .weight(1f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically)
            {
                if(image == null) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.Gray, shape = CircleShape)
                    ) {


                    }
                }
                else {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .size(30.dp)
                            //.background(Color.White ,shape = CircleShape)
                    ) {
                        Image(painter = painterResource(image), "photo")

                    }
                }

                Text(destination, modifier = Modifier.padding( start = 16.dp))
            }



            Row(
                modifier = Modifier
                    .weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically)
            {
                //Date zone
                Text(strData)
            }
        }
    }
}

@Composable
fun UIReview(name:String, surname:String, rating:Float, strData:String) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            //.size(376.dp, 43.dp)
            .fillMaxWidth()
            .height(43.dp)
            .background(
                Color(0xf9, 0xf6, 0xf9, 255),
                shape = RectangleShape
            )
            .padding(5.dp))
    {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize())
        {
            Row(
                //modifier = Modifier.fillMaxWidth(),
                modifier = Modifier
                    .weight(1f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically)
            {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color.Gray, shape = CircleShape)
                ) {
                    //Image
                    ProfilePhoto(name, surname,true, null)
                }
                Text("$name $surname", modifier = Modifier.padding( start = 16.dp))
            }

            Row(
                //modifier = Modifier.fillMaxWidth(),
                modifier = Modifier
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically)
            {
                //Icon zone
                Icon(Icons.Default.StarBorder, "star")
                Spacer(modifier = Modifier.width(5.dp))
                Text(rating.toString())
            }

            Row(
                //modifier = Modifier.fillMaxWidth(),
                modifier = Modifier
                    .weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically)
            {
                //Date zone
                Text(strData)
            }
        }
    }
}