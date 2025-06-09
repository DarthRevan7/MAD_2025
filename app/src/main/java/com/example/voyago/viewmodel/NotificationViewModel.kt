package com.example.voyago.viewmodel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.voyago.R
import com.example.voyago.model.NotificationItem
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.functions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

class NotificationViewModel : ViewModel() {
    private val _hasNewNotification = mutableStateOf(false)
    val hasNewNotification: State<Boolean> = _hasNewNotification

    private val _notifications = mutableStateListOf<NotificationItem>()
    val notifications: List<NotificationItem> = _notifications


    fun markNotificationsRead(userId: String) {
        _hasNewNotification.value = false
        markAllNotificationsRead(userId)
    }

    fun sendNotificationToUser(
        recipientId: String,
        title: String,
        body: String,
        notificationType: String,
        idLink: Int
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(recipientId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    db.collection("users")
                        .document(recipientId)
                        .collection("notifications")
                        .add(
                            mapOf(
                                "title" to title,
                                "body" to body,
                                "timestamp" to FieldValue.serverTimestamp(),
                                "read" to false,
                                "type" to notificationType,
                                "idLink" to idLink
                            )
                        )
                }
            }
    }

    fun loadNotificationsForUser(context: Context, userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("FCM", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    _notifications.clear()
                    var hasUnread = false
                    for (doc in snapshots) {
                        val notification = NotificationItem(
                            title = doc.getString("title") ?: "",
                            body = doc.getString("body") ?: "",
                            type = doc.getString("type") ?: "",
                            idLink = doc.getLong("idLink")?.toInt() ?: -1,
                            read = doc.getBoolean("read") ?: false
                        )
                        if (!notification.read) hasUnread = true
                        _notifications.add(notification)
                    }

                    // Detect if new unread notifications appeared
                    val previousValue = _hasNewNotification.value
                    _hasNewNotification.value = hasUnread
                    if (hasUnread && !previousValue) {
                        showLocalNotification(context, "New Voyago notification!", "Check out what's new.")
                    }
                }
            }
    }

    fun showLocalNotification(context: Context, title: String, body: String) {
        val channelId = "default_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }

    fun markAllNotificationsRead(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userNotifications = db.collection("users")
            .document(userId)
            .collection("notifications")

        userNotifications
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    userNotifications.document(doc.id).update("read", true)
                }
            }
    }
}

object NotificationFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> {
                NotificationViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}



