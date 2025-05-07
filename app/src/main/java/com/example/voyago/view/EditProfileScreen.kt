package com.example.voyago.view

import android.app.Activity.RESULT_OK
import com.example.voyago.activities.*
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import androidx.compose.ui.window.Dialog
import com.example.voyago.LazyUser
import com.example.voyago.model.TypeTravel
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun EditProfileScreen(user: LazyUser, navController: NavController, context:Context) {

    // Use rememberSaveable to persist the profile image URI across configuration changes
    var profileImageUri by rememberSaveable { mutableStateOf<Uri?>(user.profileImage) }

    // Use rememberSaveable for dialog visibility
    var showPopup by rememberSaveable { mutableStateOf(false) }

    // Launcher for taking photo with camera
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data // Get the URI from the result Intent
            if (uri != null) {
                Log.d("CameraResult", "Captured URI: $uri")
                profileImageUri = uri // Update the rememberSaveable state
                user.profileImage = uri // Also update the user object
                showPopup = false // Dismiss the popup
            } else {
                Log.d("CameraResult", "No URI received from camera")
            }
        } else {
            Log.d("CameraResult", "Camera activity cancelled or failed")
        }
    }

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

    var errors = remember { mutableStateListOf<Boolean>().apply { addAll(List(fieldValues.size) { false }) } }


    val selected = rememberSaveable(saver = listSaver(
        save = { it.toList() },
        restore = { it.toMutableStateList() }
    )) {
        user.typeTravelPreferences.toMutableStateList()
    }


    //TODO: temporary, to be changed once database is fully implemented
    val availableDestinations = listOf("Argentina", "Armenia", "Australia", "Austria", "Azerbaijan",
        "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus",
        "Belgium", "Belize", "Benin", "Bhutan", "Bolivia",
        "Bosnia and Herzegovina", "Botswana", "Brazil", "Brunei", "Bulgaria",
        "Cambodia", "Cameroon", "Canada", "Cape Verde", "Chile",
        "China", "Colombia", "Costa Rica", "Croatia", "Cuba",
        "Cyprus", "Czech Republic", "Denmark", "Dominican Republic", "Ecuador",
        "Egypt", "El Salvador", "Estonia", "Ethiopia", "Fiji",
        "Finland", "France", "Georgia", "Germany", "Ghana",
        "Greece", "Guatemala", "Honduras", "Hungary", "Iceland",
        "India", "Indonesia", "Iran", "Iraq", "Ireland",
        "Israel", "Italy", "Jamaica", "Japan", "Jordan",
        "Kazakhstan", "Kenya", "Kuwait", "Kyrgyzstan", "Laos",
        "Latvia", "Lebanon", "Lithuania", "Luxembourg", "Madagascar",
        "Malaysia", "Maldives", "Malta", "Mauritius", "Mexico",
        "Moldova", "Monaco", "Mongolia", "Montenegro", "Morocco",
        "Myanmar", "Namibia", "Nepal", "Netherlands", "New Zealand",
        "Nicaragua", "Nigeria", "North Macedonia", "Norway", "Oman",
        "Pakistan", "Panama", "Paraguay", "Peru", "Philippines",
        "Poland", "Portugal", "Qatar", "Romania", "Russia",
        "Rwanda", "Saudi Arabia", "Serbia", "Singapore", "Slovakia",
        "Slovenia", "South Africa", "South Korea", "Spain", "Sri Lanka",
        "Sweden", "Switzerland", "Taiwan", "Tanzania", "Thailand",
        "Trinidad and Tobago", "Tunisia", "Turkey", "Uganda", "Ukraine",
        "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "Vietnam"
    )

    val selectedDestinations = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        mutableStateListOf<String>().apply { addAll(user.desiredDestinations) }
    }

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
                ) {
                    // Pass the profileImageUri state and the showPopup state/lambda
                    ProfilePhotoEditing(
                        firstname = user.name,
                        surname = user.surname,
                        profileImageUri = profileImageUri,
                        onCameraIconClick = { showPopup = true },
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
                    fieldValues.forEachIndexed {
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

                            ValidatingInputEmailField(item, {fieldValues[index] = it}, emailHasErrors)
                        }
                        //These are other text fields
                        else {
                            val validatorHasErrors by derivedStateOf {
                                item.isBlank()
                            }

                            errors[index] = validatorHasErrors

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

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        val scrollState = rememberScrollState()

                        Column(
                            modifier = Modifier
                                .height(200.dp)
                                .background(Color(0xc1, 0xa5, 0xc3, 128))
                                .verticalScroll(scrollState)
                                .fillMaxWidth(0.8f)
                                .padding(horizontal = 16.dp)
                        ) {
                            availableDestinations.forEach { destination ->
                                val isChecked = destination in selectedDestinations
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            if (isChecked) {
                                                selectedDestinations.remove(destination)
                                            } else {
                                                selectedDestinations.add(destination)
                                            }
                                        }
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            if (it) selectedDestinations.add(destination)
                                            else selectedDestinations.remove(destination)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(destination)
                                }
                            }
                        }
                    }


                    //Update datas
                    Button(
                        onClick = {
                            if(!errors.any{it}) {
                                user.applyStrChanges(fieldValues[0], fieldValues[1], fieldValues[2], fieldValues[3], fieldValues[4], fieldValues[5])
                                user.applyTypeTravelChanges(selected)
                                user.desiredDestinations = selectedDestinations.toList()
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

    // Show the popup when showPopup is true
    if(showPopup) {
        CameraPopup(
            onDismissRequest = { showPopup = false },
            onImageSelectedFromGallery = { uri ->
                profileImageUri = uri
                user.profileImage = uri
                showPopup = false
            },
            onTakePhotoClick = {
                // Launch the camera activity
                val intent = Intent(context, CameraActivity::class.java)
                takePhotoLauncher.launch(intent)
            }
        )
    }
}

@Composable
fun ProfilePhotoEditing(firstname: String, surname: String, profileImageUri: Uri?,
    onCameraIconClick: () -> Unit, modifier: Modifier = Modifier) {

    val initials = "${firstname.first()}"+"${surname.first()}"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(130.dp)
            .background(Color.Blue, shape = CircleShape)
    ) {
        if(profileImageUri != null) {
            AsyncImage(
                model = profileImageUri,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip( shape = CircleShape)
                    .border(0.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initials,
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Icon(Icons.Default.CameraAlt,
            "camera",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clickable { onCameraIconClick() }
        )
    }
}

@Composable
fun CameraPopup(onDismissRequest: () -> Unit, onImageSelectedFromGallery: (Uri) -> Unit,
    onTakePhotoClick: () -> Unit) {

    val pickMedia = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            onImageSelectedFromGallery(uri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Button(
                onClick = {
                    onTakePhotoClick() // Call the lambda to launch camera
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(5.dp)

            ) {
                Text("Picture from Camera")
            }

            Button(
                onClick = {
                    pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(5.dp)
            ) {
                Text("Picture from Gallery")
            }
        }
    }
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
