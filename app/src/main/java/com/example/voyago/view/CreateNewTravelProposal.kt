package com.example.voyago.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voyago.activities.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTravelProposal(navController: NavController) {

    var tripName by rememberSaveable {mutableStateOf("")}
    var destination by rememberSaveable {mutableStateOf("")}
    var groupSize by rememberSaveable {mutableStateOf("")}

    var price by rememberSaveable {mutableStateOf("")}
    var priceError by rememberSaveable {mutableStateOf(false)}
    var priceErrorMessage by rememberSaveable {mutableStateOf("")}

    val typeTravel = listOf("Party", "Adventure", "Culture", "Relax")
    val selected = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        mutableStateListOf<String>()
    }



    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(1)
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


                OutlinedTextField(
                    value =  tripName,
                    onValueChange = { tripName = it },
                    label = { Text("Trip name") },
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }

            item{
                Spacer(modifier = Modifier.height(10.dp))
            }

            item{


                OutlinedTextField(
                    value =  destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination") },
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }

            item{
                Spacer(modifier = Modifier.height(10.dp))
            }

            item{

                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        priceError = false
                    },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    isError = priceError,
                    supportingText = {
                        if (priceError) {
                            Text(
                                text = priceErrorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                )
            }


            item{


                OutlinedTextField(
                    value =  groupSize,
                    onValueChange = { groupSize = it },
                    label = { Text("Group Size") },
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }

            item{
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Text(
                        text = "Trip type",
                        modifier = Modifier
                            .align(Alignment.Center),
                        fontSize = 14.sp
                    )
                }
            }

            item{
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                ) {
                    typeTravel.forEach { type ->
                        FilterChip(
                            selected = type in selected,
                            onClick = {
                                if (type in selected) {
                                    selected.remove(type)
                                } else {
                                    selected.add(type)
                                }
                            },
                            label = { Text(type.lowercase()) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }


            item {
                Button(
                    onClick = {
                        if (!validatePrice(price)) {
                            priceError = true
                            priceErrorMessage = "Price must be a number greater than 1"
                        } else {
                            priceError = false
                            priceErrorMessage = ""
                            navController.navigate("main_page")

                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Next")
                }
            }
        }
    }
}


fun validatePrice(price: String): Boolean {
    return price.toDoubleOrNull()?.let { it > 1.0 } ?: false
}




/*@Composable
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

    // TAB with About, Trips & Articles, Reviews
    val tabs = listOf("About", "Trips & Articles", "Reviews")

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
                                    item -> UITripArticle(item.title,item.strDate, item.photo)
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
                    ) {}
                } else {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .size(30.dp)
                            //.background(Color.White ,shape = CircleShape)
                    ) {
                        Image(painter = painterResource(image), "photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip( shape = CircleShape)
                                .border(0.dp, Color.White, CircleShape),
                            contentScale = ContentScale.Crop
                        )
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
                modifier = Modifier
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically)
            {
                Icon(Icons.Default.StarBorder, "star")
                Spacer(modifier = Modifier.width(5.dp))
                Text(rating.toString())
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

 */