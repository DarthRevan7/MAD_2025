import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.rememberNavController
import com.example.voyago.R
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar
import com.example.voyago.model.UserModel
import com.example.voyago.viewmodel.NotificationViewModel
import com.example.voyago.viewmodel.UserViewModel
import com.google.firebase.annotations.concurrent.Background


@Preview(showBackground = true)
@Composable
fun GroupChatInfoScreenPreview() {

    val navController = rememberNavController()
    val notificationViewModel = NotificationViewModel()
    val userViewModel = UserViewModel(UserModel())


    MaterialTheme {
        Scaffold(
            topBar = {
                TopBar(
                    nvm = notificationViewModel,
                    navController = navController,
                    uvm = userViewModel
                )
            },
            bottomBar = { BottomBar(navController) }
        ) { innerPadding ->
            GroupChatInfoScreen(Modifier.padding(innerPadding))
        }
    }
}


@Composable
fun GroupChatInfoScreen(modifier: Modifier = Modifier) {

    var popupVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier

    ) {
        Row(modifier = Modifier.align(Alignment.CenterHorizontally))
        {
            Image(
                painter = painterResource(id = R.drawable.bali),
                contentDescription = "Trip Pattern",
                modifier = Modifier.size(100.dp)
                    .clip(shape = CircleShape),
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.align(Alignment.CenterHorizontally))
        {
            Text(
                text = "Rome Trip",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.align(Alignment.CenterHorizontally))
        {
            Text(
                text = "Start date - End date",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        //Trip information
        Row(modifier = Modifier.align(Alignment.CenterHorizontally))
        {
            Button(
                onClick = { /* TODO: Handle click */ },
                colors = ButtonDefaults.buttonColors(Color(0xFF673AB7)), // Purple color
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text(text = "See trip information", color = Color.White, fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.align(Alignment.CenterHorizontally))
        {
            // Members Section
            Text(
                text = "Members (4/5)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Member List
        Column(modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .verticalScroll(rememberScrollState())) {
            MemberItem(
                avatarResId = R.drawable.brazil, // Drawable placeholder
                name = "You",
                rating = "4.2",
                isCreator = false,
                isDarkBackground = false
            )
            Spacer(modifier = Modifier.height(8.dp))
            MemberItem(
                avatarResId = R.drawable.brazil, // Drawable placeholder
                name = "You",
                rating = "4.2",
                isCreator = false,
                isDarkBackground = false
            )
            Spacer(modifier = Modifier.height(8.dp))
            MemberItem(
                avatarResId = R.drawable.brazil, // Drawable placeholder
                name = "You",
                rating = "4.2",
                isCreator = false,
                isDarkBackground = false
            )
            Spacer(modifier = Modifier.height(8.dp))
            MemberItem(
                avatarResId = R.drawable.brazil, // Drawable placeholder
                name = "You",
                rating = "4.2",
                isCreator = false,
                isDarkBackground = false
            )
            Spacer(modifier = Modifier.height(8.dp))
            MemberItem(
                avatarResId = R.drawable.brazil, // Drawable placeholder
                name = "You",
                rating = "4.2",
                isCreator = false,
                isDarkBackground = false
            )
            Spacer(modifier = Modifier.height(8.dp))
        }


        Spacer(modifier = Modifier.weight(1f))

        // Leave Trip Button
        Button(
            onClick = { popupVisible = true },
            colors = ButtonDefaults.buttonColors(Color(0xFFC62828)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text(text = "Leave Trip", color = Color.White, fontSize = 18.sp)
        }
    }
    if(popupVisible) {
        ConfirmationDialog(
            onDismissRequest = { popupVisible = false },
            onCancelClick = { popupVisible = false },
            onConfirmClick = { popupVisible = false })
    }
}

// Element for the Trip Member List
@Composable
fun MemberItem(
    avatarResId: Int,
    name: String,
    rating: String,
    isCreator: Boolean,
    isDarkBackground: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = avatarResId),
                    contentDescription = "$name's Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Gray, CircleShape), // Drawable placeholder
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkBackground) Color.White else Color.Black
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star Rating",
                    tint = Color(0xFFFFD700), // Golden color for the star
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = rating,
                    fontSize = 16.sp,
                    color = if (isDarkBackground) Color.White else Color.Black
                )
                if (isCreator) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Creator",
                        fontSize = 14.sp,
                        color = Color.Gray // Maybe a distint color
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit, // What happens when the dialog is dismissed
    onCancelClick: () -> Unit, // Action for click on "Cancel" button
    onConfirmClick: () -> Unit // Action for click on "Confirm" button
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(8.dp)),
        content = {
            Column(modifier = Modifier
                .height(136.dp),
                )
            {
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .height(20.dp)
                        .align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Do you want to relinquish the trip?",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }


                Spacer(Modifier.height(16.dp))


                Row(
                    modifier = Modifier.fillMaxWidth()
                        .height(20.dp)
                        .align(Alignment.CenterHorizontally),
                ) {
                    Text(text = "This will DECREASE your RELIABILITY SCORE!!",
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row (modifier = Modifier.fillMaxWidth()
                    .height(36.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = onCancelClick,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.background( color = Color(0xFFE0E0E0))
                            .height(36.dp)) {
                        Text("Cancel")
                    }

                    Button(onClick = onConfirmClick,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.background( color = Color(0xFF673AB7))
                            .height(36.dp)) {
                        Text("Confirm")
                    }
                }
            }


            /*
            Spacer(Modifier.height(16.dp))

            Row (modifier = Modifier.fillMaxWidth()
                .height(24.dp)) {
                Button(onClick = onCancelClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.background( color = Color(0xFFE0E0E0))
                        //.weight(1f)
                        .height(48.dp)) {
                    Text("Cancel")
                }
                Color(0xFF673AB7)
                Button(onClick = onConfirmClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.background( color = Color(0xFF673AB7))
                        //.weight(1f)
                        .height(24.dp)) {
                    Text("Confirm")
                }
            }

             */
        }
    )
}
