package com.example.voyago.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.voyago.viewmodel.NotificationViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NotificationView(nvm: NotificationViewModel) {
    val userId = "1"
    LaunchedEffect(userId) {
        nvm.loadNotificationsForUser(userId)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Notifications", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        nvm.notifications.forEach {
            Text("🔔 $it", modifier = Modifier.padding(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        val context = LocalContext.current
        Button(
            onClick = {
                val title = "Hello"
                val body = "Message for user 1!"

                nvm.sendNotificationToUser(userId, title, body)
                nvm.receiveNewNotification("$title: $body") // Local badge trigger
                nvm.showLocalNotification(context, title, body)

            }

        ) {
            Text("Send")
        }

    }


}


