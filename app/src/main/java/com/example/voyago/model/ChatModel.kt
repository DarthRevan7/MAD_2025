package com.example.voyago.model

import com.example.voyago.Collections
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.coroutineScope
import android.util.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow


data class TripChat(val chatId : String = "",                // chatId = tripId.toString() + "Chat"
                    val messages : List<ChatMessage>)

data class PrivateChat(val chatId : String = "",             // chatId = userId + "_to_" + userId [1_to_2]
                       val messages : List<ChatMessage> = emptyList() )

data class ChatMessage(
    val senderId : Int,
    val message : String,
    val date : String
)

class ChatModel(){


    private val _privateChat = MutableStateFlow<PrivateChat>(PrivateChat())
    val privateChat : MutableStateFlow<PrivateChat> = _privateChat

    suspend fun getPrivateChat(userId1: Int, userId2 : Int) {

        coroutineScope {
            callbackFlow {
                val chatId = userId1.toString() + "_to_" + userId2.toString()
                val listener = Chats.chats
                    .whereEqualTo("chatId", chatId)
                    .addSnapshotListener { snapshot, error ->
                        if(snapshot != null) {
                            val objToSend = snapshot.toObjects(PrivateChat::class.java)
                            trySend(objToSend)
                        }
                        else
                        {
                            Log.e("ChatErrors", error.toString())
                            trySend(PrivateChat())
                        }
                    }
            }.collect { privateChatCollected ->
                _privateChat.value = privateChatCollected as PrivateChat
            }
        }
    }

}

//Chat singleton
object Chats{

    private const val C_CHATS = "chats"

    private val db : FirebaseFirestore
        get() = Firebase.firestore

    init {
        Chats.db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) //false to Disable LocalChaching
            .build()
    }

    val chats = db.collection(C_CHATS)
}