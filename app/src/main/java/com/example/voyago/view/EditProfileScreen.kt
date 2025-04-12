package com.example.voyago.view


import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import androidx.compose.ui.window.Dialog
import com.example.voyago.LazyUser
import com.example.voyago.model.TypeTravel



//Edit this
var userRepository:UserRepository = UserRepository()
var userData = userRepository.fetchUserData(true)



@Composable
fun EditProfileScreen(user: LazyUser, navController: NavController)
{
    //Delete later
    val fieldValues = rememberSaveable(saver = listSaver(
        save = { it.toList() },
        restore = { it.toMutableStateList() }
    )) {
        mutableStateListOf(
            user.name,
            user.surname,
            user.username,
            user.email,
            user.country,
            user.userDescription
        )
    }
    val fieldNames = listOf("First Name", "Surname",
        "Username", "Email address", "Country",
        "User Description"
    )

    var errors = arrayOf(false, false, false, false, false, false)


    val selected = remember { user.typeTravelPreferences.toMutableStateList() }
    var userDestinations = user.desiredDestinations

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
                        user.name, user.surname,
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
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(bottom = 15.dp),
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
                    fieldValues.forEachIndexed() {
                            index, item ->
                            //This is TextField with email
                            if(index == 3) {
                                val emailHasErrors by derivedStateOf {
                                    if (item.isNotEmpty()) {
                                        !android.util.Patterns.EMAIL_ADDRESS.matcher(item).matches()
                                    } else {
                                        false
                                    }
                                }

                                errors[index] = emailHasErrors
                                /*TextField(
                                    value = item,
                                    onValueChange = { fieldValues[index] = it},
                                    label = { val itemIndex:Int = fieldValues.indexOf(item); Text(text = fieldNames[itemIndex]) },
                                    maxLines = 2,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )*/
                                ValidatingInputEmailField(item, {fieldValues[index] = it}, emailHasErrors)
                            }
                            //These are other text fields
                            else {
                                val validatorHasErrors by derivedStateOf {
                                    item.isBlank()
                                }

                                errors[index] = validatorHasErrors
                                /*TextField(
                                    value = item,
                                    onValueChange = { val itemIndex:Int = fieldValues.indexOf(item); fieldValues[itemIndex] = it },
                                    label = { val itemIndex:Int = fieldValues.indexOf(item); Text(text = fieldNames[itemIndex]) },
                                    maxLines = 2,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                                )*/
                                ValidatingInputTextField(item, {fieldValues[index] = it}, validatorHasErrors, fieldNames[fieldValues.indexOf(item)])
                            }

                    }

                    Text(text = "Preferences about the type of travel",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 10.dp),
                        fontSize = 14.sp
                    )

                    //Selected trip type
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 10.dp)
                    ) {
                        TypeTravel.entries.forEach { type ->
                            FilterChip(
                                selected = type in selected,
                                onClick = {
                                    if (type in selected) {
                                        selected.remove(type)
                                    } else {
                                        selected.add(type)
                                    }
                                },
                                label = { Text(type.toString().lowercase())},
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }

                    Text(text = "Most Desired destination",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 15.dp),
                        fontSize = 14.sp
                    )

                    SearchBarWithResults(LocalContext.current, user)


                    //Update datas
                    Button(
                        onClick = {
                            if(!errors.any{it}) {
                                user.applyStrChanges(fieldValues[0], fieldValues[1], fieldValues[2], fieldValues[3], fieldValues[4], fieldValues[5])
                                user.applyTypeTravelChanges(selected)
                                //user.applyDestinations()
                                navController.navigate("my_profile")
                            }
                    },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
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

    var showPopup by remember { mutableStateOf(false) }

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
        //Popup da implementare
        Icon(Icons.Default.CameraAlt,
            "camera",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clickable { showPopup = true }
        )
    }

    if(showPopup)
        CameraPopup(onDismissRequest = { showPopup = false } )

}

@Composable
fun CameraPopup(onDismissRequest: () -> Unit)
{

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                //.align(Alignment.CenterVertically)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Button(
                onClick = {

                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(5.dp)

            ) {
                Text("Picture from Camera")
            }

            Button(
                onClick = {

                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(5.dp)

            ) {
                Text("Picture from Gallery")
            }
        }
    }


    /*
    Box(modifier = Modifier.wrapContentSize())

    {
        Button(
            onClick = {

            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(5.dp)

        ) {
            Text("Picture from Camera")
        }

        Button(
            onClick = {

            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(5.dp)

        ) {
            Text("Picture from Gallery")
        }
    }

     */
}

//Passare diretto desired destinations e la desired destination dell'user.
@Composable
fun SearchBarWithResults(context:Context, user: LazyUser)
{

    var destinationList = remember { mutableStateOf<List<String>>(user.desiredDestinations) }
    var strData = destinationList.value.joinToString(", ")

    //SearchView(context)
    TextField(
        value = strData,
        onValueChange = { strData = it }, //Splittare il dato con la virgola
        label = { Text(text ="Desired Destinations" ) },
        maxLines = 1,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )

}




@Composable
fun ValidatingInputEmailField(email: String, updateState: (String) -> Unit, validatorHasErrors: Boolean) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        value = email,
        onValueChange = updateState,
        label = { Text("Email") },
        isError = validatorHasErrors,
        supportingText = {
            if (validatorHasErrors) {
                Text("Incorrect email format")
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
}

@Composable
fun ValidatingInputTextField(text:String, updateState: (String) -> Unit, validatorHasErrors: Boolean, label: String) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        value = text,
        onValueChange = updateState,
        label = { Text(label) },
        isError = validatorHasErrors,
        supportingText = {
            if (validatorHasErrors) {
                Text("This field cannot be empty")
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}