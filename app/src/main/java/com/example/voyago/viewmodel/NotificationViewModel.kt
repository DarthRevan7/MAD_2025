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
import com.example.voyago.R
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

    private val _notifications = mutableStateListOf<String>()
    val notifications: List<String> = _notifications

    fun receiveNewNotification(message: String) {
        _hasNewNotification.value = true
        _notifications.add(message)
    }

    fun markNotificationsRead() {
        _hasNewNotification.value = false
    }

    // Inside NotificationViewModel.kt
    fun sendNotification(title: String, body: String, token: String) {
        val data = mapOf(
            "title" to title,
            "body" to body,
            "token" to token
        )

        Firebase.functions
            .getHttpsCallable("sendAndStoreNotification")
            .call(data)
            .addOnSuccessListener {
                Log.d("FCM", "Notification sent successfully.")
            }
            .addOnFailureListener {
                Log.e("FCM", "Error sending notification", it)
            }
    }


    fun sendNotificationToUser(recipientId: String, title: String, body: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(recipientId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Store in user's subcollection
                    db.collection("users")
                        .document(recipientId)
                        .collection("notifications")
                        .add(
                            mapOf(
                                "title" to title,
                                "body" to body,
                                "timestamp" to FieldValue.serverTimestamp(),
                                "read" to false
                            )
                        )
                        .addOnSuccessListener {
                            Log.d("FCM", "Notification stored in user's subcollection")
                        }
                        .addOnFailureListener {
                            Log.e("FCM", "Error storing notification", it)
                        }
                } else {
                    Log.e("FCM", "User not found in Firestore")
                }
            }
            .addOnFailureListener {
                Log.e("FCM", "Failed to fetch recipient document", it)
            }
    }



    fun loadNotificationsForUser(userId: String) {
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
                    for (doc in snapshots) {
                        val title = doc.getString("title") ?: ""
                        val body = doc.getString("body") ?: ""
                        _notifications.add("$title: $body")
                    }
                    _hasNewNotification.value = _notifications.isNotEmpty()
                }
            }
    }

    fun sendGlobalNotification(title: String, body: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("notifications")
            .add(
                mapOf(
                    "title" to title,
                    "body" to body
                )
            )
            .addOnSuccessListener {
                Log.d("Notification", "Notification stored successfully")
            }
            .addOnFailureListener {
                Log.e("Notification", "Error storing notification", it)
            }
    }

    fun loadAllNotifications() {
        val db = FirebaseFirestore.getInstance()
        db.collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Notification", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    _notifications.clear()
                    for (doc in snapshots) {
                        val title = doc.getString("title") ?: ""
                        val body = doc.getString("body") ?: ""
                        _notifications.add("$title: $body")
                    }
                    _hasNewNotification.value = _notifications.isNotEmpty()
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
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your icon if needed
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



