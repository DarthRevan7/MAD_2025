package com.example.voyago.view

import android.app.Activity.RESULT_OK
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.voyago.model.TypeTravel
import com.example.voyago.model.User
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await

@Composable
fun EditProfileScreen(
    navController: NavController,
    context: Context,
    vm: TripViewModel,
    uvm: UserViewModel
) {

    val user by uvm.loggedUser.collectAsState()

    var profileImageUri = uvm.profileImageUri.value

    // Use rememberSaveable for dialog visibility
    var showPopup by rememberSaveable { mutableStateOf(false) }

    // Launcher for taking photo with camera
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data // Get the URI from the result Intent
            if (uri != null) {
                profileImageUri = uri // Update the rememberSaveable state
                user.profilePictureUrl = uri.toString() // Also update the user object
                showPopup = false // Dismiss the popup
            }
        }
    }

    val fieldValues = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )) {
        mutableStateListOf(
            user.firstname,
            user.surname,
            user.username,
            user.email,
            user.country,
            user.userDescription
        )
    }

    val fieldNames = listOf(
        "First Name", "Surname",
        "Username", "Email address", "Country",
        "User Description"
    )

    var errors =
        remember { mutableStateListOf<Boolean>().apply { addAll(List(fieldValues.size) { false }) } }


    val selected = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )) {
        user.typeTravel.toMutableStateList()
    }


    //TODO: temporary, to be changed once database is fully implemented
    val availableDestinations = listOf(
        "Argentina", "Armenia", "Australia", "Austria", "Azerbaijan",
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
        mutableStateListOf<String>().apply { addAll(user.desiredDestination) }
    }

    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            //Box with profile image and initials
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp)
            ) {
                // Pass the profileImageUri state and the showPopup state/lambda
                ProfilePhotoEditing(
                    firstname = user.firstname,
                    surname = user.surname,
                    profileImageUri = profileImageUri,
                    onCameraIconClick = { showPopup = true },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        item {
            //Text with Edit Profile
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
            {
                Text(
                    text = "Edit Profile",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(bottom = 15.dp),
                    fontSize = 20.sp
                )
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
                fieldValues.forEachIndexed { index, item ->
                    //This is TextField with email
                    if (index == 3) {
                        val emailHasErrors by derivedStateOf {
                            if (item.isNotEmpty()) {
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(item).matches()
                            } else {
                                false
                            }
                        }

                        errors[index] = emailHasErrors

                        ValidatingInputEmailField(item, { fieldValues[index] = it }, emailHasErrors)
                    }
                    //These are other text fields
                    else {
                        val validatorHasErrors by derivedStateOf {
                            item.isBlank()
                        }

                        errors[index] = validatorHasErrors

                        ValidatingInputTextField(
                            item,
                            { fieldValues[index] = it },
                            validatorHasErrors,
                            fieldNames[fieldValues.indexOf(item)]
                        )
                    }
                }

                Text(
                    text = "Preferences about the type of travel",
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
                            label = { Text(type.toString().lowercase()) },
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }

                Text(
                    text = "Most Desired destination",
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

                // 在 EditProfileScreen 中修改更新按钮的逻辑：

                //Update datas
                Button(
                    onClick = {
                        if (!errors.any { it }) {

                            if (profileImageUri == null) {
                                profileImageUri = user.profilePictureUrl?.toUri()
                            }

                            val updatedUser = User(
                                id = user.id,
                                uid = user.uid,
                                firstname = fieldValues[0],
                                surname = fieldValues[1],
                                username = fieldValues[2],
                                country = fieldValues[4],
                                email = fieldValues[3],
                                userDescription = fieldValues[5],
                                dateOfBirth = user.dateOfBirth,
                                password = user.password,
                                profilePictureUrl = user.profilePictureUrl,
                                typeTravel = selected,
                                desiredDestination = selectedDestinations.toList(),
                                rating = user.rating,
                                reliability = user.reliability
                            )


                            /*
                                                        uvm.editUserData(updatedUser)

                                                        navController.navigate("profile_overview") {
                                                            popUpTo("edit_profile") {
                                                                inclusive = true
                                                            } // Removes edit from back stack
                                                            launchSingleTop = true
                                                        }
                            */
                            uvm.editUserData(updatedUser)
                            uvm.updateUserWithProfileImage(
                                updatedUser = updatedUser,
                                newImageUri = profileImageUri
                            ) { success ->
                                if (success) {
                                    navController.navigate("profile_overview") {
                                        popUpTo("edit_profile") {
                                            inclusive = true
                                        } // Removes edit from back stack
                                        launchSingleTop = true
                                    }
                                } else {
                                    // 处理错误，可以显示 Toast 或错误消息
                                    Log.e("EditProfile", "Failed to update profile")
                                }
                            }
                            /*

                                                        // 🔄 使用 UserViewModel 的新方法
                                                        uvm.updateUserWithProfileImage(
                                                            updatedUser = updatedUser,
                                                            newImageUri = profileImageUri
                                                        ) { success ->
                                                            if (success) {
                                                                navController.navigate("profile_overview")
                                                            } else {
                                                                // 处理错误，可以显示 Toast 或错误消息
                                                                Log.e("EditProfile", "Failed to update profile")
                                                            }
                                                        }
                            */
//End of XI Part
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


// Show the popup when showPopup is true
    if (showPopup) {
        CameraPopup(
            onDismissRequest = { showPopup = false },
            onImageSelectedFromGallery = { uri ->
                profileImageUri = uri
                uvm.setProfileImageUri(uri)
                //user.profilePicture = uri
                showPopup = false
            },
            onTakePhotoClick = {
                // Launch the camera activity
                showPopup = false
                navController.navigate("camera")
                //profileImageUri = vm.profileImageUri.value
                //val intent = Intent(context, MainActivity::class.java)
                //takePhotoLauncher.launch(intent)
            }
        )
    }


}

// 在 EditProfile.kt 中修改 ProfilePhotoEditing 组件：

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfilePhotoEditing(
    firstname: String,
    surname: String,
    profileImageUri: Uri?,
    onCameraIconClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initials = "${firstname.firstOrNull() ?: ""}${surname.firstOrNull() ?: ""}"
    var firebaseImageUrl by remember { mutableStateOf<String?>(null) }

    // 如果 profileImageUri 是 Firebase Storage 路径，获取实际 URL
    LaunchedEffect(profileImageUri) {
        if (profileImageUri != null && !profileImageUri.toString().startsWith("content://")) {
            try {
                // 如果是 Firebase Storage 路径，获取下载 URL
                val path = profileImageUri.toString()
                if (path.contains("/") && !path.startsWith("http")) {
                    val storageRef = com.google.firebase.Firebase.storage.reference.child(path)
                    firebaseImageUrl = storageRef.downloadUrl.await().toString()
                }
            } catch (e: Exception) {
                Log.e("ProfilePhotoEditing", "Failed to get Firebase URL", e)
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(130.dp)
            .background(Color.Blue, shape = CircleShape)
    ) {
        when {
            // 优先显示本地选择的图片（编辑状态）
            profileImageUri != null && profileImageUri.toString().startsWith("content://") -> {
                AsyncImage(
                    model = profileImageUri,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(0.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            // 显示 Firebase Storage 中的图片
            firebaseImageUrl != null -> {
                GlideImage(
                    model = firebaseImageUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(0.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            // 显示首字母
            else -> {
                Text(
                    text = initials,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Icon(
            Icons.Default.CameraAlt,
            "camera",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clickable { onCameraIconClick() }
        )
    }
}

@Composable
fun CameraPopup(
    onDismissRequest: () -> Unit, onImageSelectedFromGallery: (Uri) -> Unit,
    onTakePhotoClick: () -> Unit
) {

    val pickMedia = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onImageSelectedFromGallery(uri)
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
fun ValidatingInputEmailField(
    email: String,
    updateState: (String) -> Unit,
    validatorHasErrors: Boolean
) {
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

