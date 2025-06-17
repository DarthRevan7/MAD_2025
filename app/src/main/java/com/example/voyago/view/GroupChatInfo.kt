import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.voyago.R
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar
import com.example.voyago.model.UserModel
import com.example.voyago.viewmodel.NotificationViewModel
import com.example.voyago.viewmodel.UserViewModel


@Preview(showBackground = true)
@Composable
fun GroupChatInfoScreenPreview() {

    val navController = rememberNavController()
    val notificationViewModel = NotificationViewModel()
    val userViewModel = UserViewModel(UserModel())
    val userVerified by userViewModel.userVerified.collectAsState()

    MaterialTheme {
        //GroupChatInfoScreen()
        Scaffold(
            topBar = {
                TopBar(
                    nvm = notificationViewModel,
                    navController = navController,
                    uvm = UserViewModel(UserModel())
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
    Column(
        modifier = modifier

    ) {
        Row(modifier = Modifier.align(Alignment.CenterHorizontally))
        {
            Image(
                painter = painterResource(id = R.drawable.bali),
                contentDescription = "Trip Pattern",
                modifier = Modifier.size(100.dp),
                alignment = Alignment.Center
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

        Row(modifier = Modifier.align(Alignment.CenterHorizontally))
        {
            Button(
                onClick = { /* TODO: Handle click */ },
                colors = ButtonDefaults.buttonColors(Color(0xFF673AB7)), // Colore viola
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Riduci la larghezza
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
        Column(modifier = Modifier.fillMaxWidth()
            .height(280.dp)
            .verticalScroll(rememberScrollState())) {
            MemberItem(
                avatarResId = R.drawable.brazil, // Sostituisci con il tuo drawable
                name = "You",
                rating = "4.2",
                isCreator = false,
                isDarkBackground = false // Il tuo background della card è chiaro
            )
            Spacer(modifier = Modifier.height(8.dp))
            MemberItem(
                avatarResId = R.drawable.brazil, // Sostituisci con il tuo drawable
                name = "You",
                rating = "4.2",
                isCreator = false,
                isDarkBackground = false // Il tuo background della card è chiaro
            )
            Spacer(modifier = Modifier.height(8.dp))
            MemberItem(
                avatarResId = R.drawable.brazil, // Sostituisci con il tuo drawable
                name = "You",
                rating = "4.2",
                isCreator = false,
                isDarkBackground = false // Il tuo background della card è chiaro
            )
            Spacer(modifier = Modifier.height(8.dp))
            MemberItem(
                avatarResId = R.drawable.brazil, // Sostituisci con il tuo drawable
                name = "You",
                rating = "4.2",
                isCreator = false,
                isDarkBackground = false // Il tuo background della card è chiaro
            )
            Spacer(modifier = Modifier.height(8.dp))
            MemberItem(
                avatarResId = R.drawable.brazil, // Sostituisci con il tuo drawable
                name = "You",
                rating = "4.2",
                isCreator = false,
                isDarkBackground = false // Il tuo background della card è chiaro
            )
            Spacer(modifier = Modifier.height(8.dp))
        }


        Spacer(modifier = Modifier.weight(1f)) // Spinge il pulsante in basso

        // Leave Trip Button
        Button(
            onClick = { /* TODO: Handle click */ },
            colors = ButtonDefaults.buttonColors(Color(0xFFC62828)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp) // Riduci il padding interno del pulsante
        ) {
            Text(text = "Leave Trip", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
fun MemberItem(
    avatarResId: Int,
    name: String,
    rating: String,
    isCreator: Boolean,
    isDarkBackground: Boolean // Indica se la card è su sfondo scuro (per cambiare colore testo)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp), // Altezza fissa per ogni elemento
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
                        .background(Color.Gray, CircleShape), // Placeholder per il background se l'immagine non riempie
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
                    tint = Color(0xFFFFD700), // Colore oro per la stella
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
                        color = Color.Gray // O un colore che si distingua
                    )
                }
            }
        }
    }
}

