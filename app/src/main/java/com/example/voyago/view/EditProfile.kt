package com.example.voyago.view

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.voyago.viewmodel.UserViewModel
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await

// Screen that permits to edit the profile
@Composable
fun EditProfileScreen(
    navController: NavController,
    uvm: UserViewModel
) {
    // Obtain the current Android context
    val context = LocalContext.current

    // Observe the current logged-in user from the ViewModel
    val user by uvm.loggedUser.collectAsState()

    // Store the current URI of the profile image (nullable)
    var profileImageUri = uvm.profileImageUri.value

    // State variable to control the visibility of the image source selection popup
    var showPopup by rememberSaveable { mutableStateOf(false) }

    // List of editable text field values (first name, surname, etc.)
    // These are initialized from the user object and preserved across recompositions
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

    // Labels for the input fields, used for UI descriptions
    val fieldNames = listOf(
        "First Name", "Surname",
        "Username", "Email address", "Country",
        "User Description"
    )

    // Stores validation states (true if a field has an error) for each input field
    var errors = remember {
        mutableStateListOf<Boolean>().apply {
            addAll(List(fieldValues.size) { false })
        }
    }

    // Stores selected travel types
    val selected = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )) {
        user.typeTravel.toMutableStateList()
    }


    //List of available destinations
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

    // Stores the user's selected desired destinations
    val selectedDestinations = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        mutableStateListOf<String>().apply { addAll(user.desiredDestination) }
    }

    // State object to control the scroll position of the LazyColumn
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Profile photo and camera icon, centered inside a box
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
                    onCameraIconClick = { showPopup = true },   // Open camera/gallery popup
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        item {
            //Title Text
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
            // Column containing all editable fields and preferences
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .background(Color(0xdf, 0xd1, 0xe0, 255), shape = RectangleShape)
                    .fillMaxWidth()
            ) {

                // Input Fields
                fieldValues.forEachIndexed { index, item ->
                    // Name and Surname fields
                    if (index == 0 || index == 1) {
                        isValidName(item)
                    }
                    // Username field
                    else if (index == 2) {
                        val emailHasErrors by derivedStateOf {
                            isValidUsername(item)
                        }

                        errors[index] = emailHasErrors

                        ValidatingInputEmailField(item, { fieldValues[index] = it }, emailHasErrors)

                    }
                    // Email field
                    else if (index == 3) {
                        val emailHasErrors by derivedStateOf {
                            isValidEmail(item)
                        }

                        errors[index] = emailHasErrors

                        ValidatingInputEmailField(item, { fieldValues[index] = it }, emailHasErrors)

                    }
                    // Country field
                    else if (index == 4) {
                        isValidCountry(item)
                    } else {
                        // General validation for other fields: must not be blank
                        val validatorHasErrors by derivedStateOf {
                            isValidUserDescription(item)
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

                // Travel Type Preferences (Filter Chips)
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

                // Desired Destination Section
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
                        // Checkboxes for selecting destinations
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

                // Button to update the user's profile
                Button(
                    onClick = {
                        // Only proceed if all inputs are valid
                        if (!errors.any { it }) {
                            // Use current profile picture if new one isn't selected
                            if (profileImageUri == null) {
                                profileImageUri = user.profilePictureUrl?.toUri()
                            }

                            // Create a new updated User object
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
                            // Save user changes via ViewModel
                            uvm.editUserData(updatedUser)

                            // Also update the profile image
                            uvm.updateUserWithProfileImage(
                                updatedUser = updatedUser,
                                newImageUri = profileImageUri
                            ) { success ->
                                if (success) {
                                    // Navigate to the profile overview and clear back stack
                                    navController.navigate("profile_overview") {
                                        popUpTo("edit_profile") {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                } else {
                                    // Show error toast
                                    Toast.makeText(
                                        context,
                                        "Failed to update profile. Please try again.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

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


    // Show camera/gallery selection popup if toggled
    if (showPopup) {
        CameraPopup(
            onDismissRequest = { showPopup = false },
            onImageSelectedFromGallery = { uri ->
                profileImageUri = uri
                uvm.setProfileImageUri(uri)
                showPopup = false
            },
            onTakePhotoClick = {
                showPopup = false
                navController.navigate("camera")
            }
        )
    }


}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfilePhotoEditing(
    firstname: String,
    surname: String,
    profileImageUri: Uri?,
    onCameraIconClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    // Combine first letters of first name and surname for fallback initials display
    val initials = "${firstname.firstOrNull() ?: ""}${surname.firstOrNull() ?: ""}"

    // Mutable state to hold the actual Firebase Storage image download URL if applicable
    var firebaseImageUrl by remember { mutableStateOf<String?>(null) }

    // Launch side-effect to resolve Firebase Storage URL if profileImageUri is not a local content URI
    LaunchedEffect(profileImageUri) {
        if (profileImageUri != null && !profileImageUri.toString().startsWith("content://")) {
            try {
                // Check if it's a Firebase path and not an already downloaded HTTP URL
                val path = profileImageUri.toString()
                if (path.contains("/") && !path.startsWith("http")) {
                    val storageRef = com.google.firebase.Firebase.storage.reference.child(path)
                    // Asynchronously fetch the download URL using Kotlin coroutines
                    firebaseImageUrl = storageRef.downloadUrl.await().toString()
                }
            } catch (e: Exception) {
                // Log any failure during download URL resolution
                Log.e("ProfilePhotoEditing", "Failed to get Firebase URL", e)
            }
        }
    }

    // Outer circular container for the profile picture or initials
    Box(
        contentAlignment = Alignment.Center,    // Center all content inside the Box
        modifier = modifier
            .size(130.dp)   // Fixed size for profile photo box
            .background(Color.Blue, shape = CircleShape)    // Blue background in a circular shape
    ) {
        when {
            // Case 1: Display a local image selected from the device (typically content:// URI)
            profileImageUri != null && profileImageUri.toString().startsWith("content://") -> {
                AsyncImage(
                    model = profileImageUri,    // Local URI to load
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)      // Make sure image stays in circular bounds
                        .border(0.dp, Color.White, CircleShape),    // White border
                    contentScale = ContentScale.Crop    // Crop image to fill the circle
                )
            }

            // Case 2: Display image from Firebase Storage using resolved HTTP URL
            firebaseImageUrl != null -> {
                GlideImage(
                    model = firebaseImageUrl,   // Firebase image URL to load
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(0.dp, Color.White, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            // Case 3: If no image available, show the user's initials
            else -> {
                Text(
                    text = initials,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Always show the camera icon in the bottom-right for editing the profile image
        Icon(
            Icons.Default.CameraAlt,
            "camera",                                   // Accessibility description
            modifier = Modifier
                .align(Alignment.BottomEnd)             // Align to bottom-end corner
                .clickable { onCameraIconClick() }      // Trigger passed-in lambda on click
        )
    }
}

@Composable
fun CameraPopup(
    onDismissRequest: () -> Unit,                   // Callback to handle dismissal of the popup
    onImageSelectedFromGallery: (Uri) -> Unit,      // Callback when an image is picked from the gallery
    onTakePhotoClick: () -> Unit                    // Callback to trigger taking a photo with the camera
) {

    // ActivityResultLauncher to launch the system picker for selecting an image
    val pickMedia = rememberLauncherForActivityResult(
        // Defines contract to pick visual media (images/videos)
        contract = PickVisualMedia()
    ) { uri ->  // This lambda gets called once an image is selected
        if (uri != null) {
            // Send the selected URI back to the caller
            onImageSelectedFromGallery(uri)
        }
    }

    // Material Dialog used to show the popup UI on top of other content
    Dialog(onDismissRequest = onDismissRequest) {
        // Card container for the two selection buttons
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            // Button to take a new picture using the device's camera
            Button(
                onClick = {
                    // Trigger external function to navigate to camera screen
                    onTakePhotoClick()
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(5.dp)

            ) {
                Text("Picture from Camera")
            }

            // Button to pick an existing picture from the gallery
            Button(
                onClick = {
                    // Launch system picker limited to image files
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
    email: String,                      // Current value of the email input
    updateState: (String) -> Unit,      // Callback to update the email state when user types
    validatorHasErrors: Boolean         // Flag indicating if the current input is invalid
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        value = email,                     // Binds the current email value to the text field
        onValueChange = updateState,       // Updates the state on text change via provided callback
        label = { Text("Email") },         // Displays "Email" as the label inside the field
        isError = validatorHasErrors,      // Highlights the field in red if input is invalid
        supportingText = {
            // If there is a validation error, show helper text below the input
            if (validatorHasErrors) {
                Text("Incorrect email format")  // Error message shown when email is not valid
            }
        },
        // Shows an email-optimized keyboard on mobile
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
}

fun isValidUserDescription(description: String): Boolean {
    val trimmed = description.trim()

    // Length validation
    if (trimmed.length < 10 || trimmed.length > 300) return false

    // Restrict special characters (only allow letters, digits, basic punctuation)
    val regex = "^[a-zA-Z0-9\\s.,!?()'\"-]{10,300}$".toRegex()
    return trimmed.matches(regex)
}
