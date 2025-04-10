package com.example.voyago.view

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar
import com.example.voyago.model.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import com.example.voyago.model.TypeTravel


//Edit this
var userRepository:UserRepository = UserRepository()
var userData = userRepository.fetchUserData(true)

@Composable
fun EditProfileScreen()
{
    //Delete later
    var textList = remember { mutableStateListOf(userData.firstname,
        userData.surname, userData.username, userData.email,
        userData.country,
        userData.userDescription,)}
    val fieldNames = listOf("First Name", "Surname", "Username", "Email address", "Country", "User Description")//, "Destination")


    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(4)
        },
    ) {
        innerPadding ->


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
                    ProfilePhotoEditing(
                        userData.firstname, userData.surname,
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
                //Editing Fields
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RectangleShape)
                        .fillMaxWidth()
                ) {

                    //TextFields with various info
                    textList.forEach {
                            item ->
                            //var index = 0
                        TextField(
                            value = item,
                            onValueChange = { val itemIndex:Int = textList.indexOf(item); textList[itemIndex] = it },
                            label = { val itemIndex:Int = textList.indexOf(item); Text(text = fieldNames[itemIndex]) },
                            maxLines = 2,//Text("Campo #$index") },
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                            //index++
                    }

                    Text(text = "Preferences about the type of travel",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 10.dp),
                        fontSize = 14.sp
                    )

                    //Selected trip type
                    val selectedTypeTrip = remember { mutableStateListOf<TypeTravel?>(null) }
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 10.dp)
                    ) {
                        TypeTravel.entries.forEach { type ->
                            FilterChip(
                                selected = type in selectedTypeTrip,
                                onClick = {
                                    if (type in selectedTypeTrip) {
                                        selectedTypeTrip.remove(type)
                                    } else {
                                        selectedTypeTrip.add(type)
                                    }
                                },
                                label = { Text(type.toString().lowercase())},
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }

                    Text(text = "Most Desired destination",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 15.dp),
                        fontSize = 14.sp
                    )

                    SearchBarWithResults(LocalContext.current)


                    //Update datas
                    Button(
                        onClick = {
                            userData.changeUserData(textList)
                    },
                        modifier = Modifier
                            .align( Alignment.CenterHorizontally )
                            .padding(5.dp)

                    ) {
                        Text("Update Profile Info")
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
//Passare diretto desired destinations e la desired destination dell'user.
@Composable
fun SearchBarWithResults(context:Context)
{
    //SearchView(context)
    TextField(
        value = "Dombass",
        onValueChange = {  },
        label = { Text(text ="Desired Destinations" ) },
        maxLines = 1,//Text("Campo #$index") },
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    )

}
