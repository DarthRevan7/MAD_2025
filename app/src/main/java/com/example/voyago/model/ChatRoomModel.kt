package com.example.voyago.model

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


data class ChatRoom(
    val id: String = "", // Firestore document ID (e.g., "1", "2", ...)
    val type: String = "private", // "private" or "group"
    val participants: List<Int> = emptyList(), // List IDs
    val name: String = "", // Optional for group chats
    val lastMessage: String = "", // Last message content
    val usersNotRead: List<String> = emptyList(), // Users who haven't read last message
    val tripId: String = "" // Trip ID for group chats
)

data class FirebaseChatMessage(
    val id: String = "", // Firestore document ID
    val senderId: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val type: String = "TEXT", // "TEXT", "IMAGE", etc.
    val imageUrl: String? = null
)

// -- ChatModel --

class ChatModel(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun listenToUserChatRooms(
        userId: Int,
        onUpdate: (List<ChatRoom>) -> Unit,
        onError: (Exception?) -> Unit = {}
    ) {
        db.collection("chatRooms")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener onError(error)

                val rooms = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val participants = extractParticipants(data["participants"])
                    if (participants.contains(userId)) {
                        ChatRoom(
                            id = doc.id,
                            type = data["type"] as? String ?: "private",
                            participants = participants,
                            name = data["name"] as? String ?: "",
                            lastMessage = data["lastMessage"] as? String ?: "",
                            usersNotRead = (data["usersNotRead"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            tripId = data["tripId"] as? String ?: ""
                        )
                    } else null
                }
                onUpdate(rooms)
            }
    }


    fun listenToMessages(roomId: String, onUpdate: (List<FirebaseChatMessage>) -> Unit) {
        db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val msgs = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    FirebaseChatMessage(
                        id = doc.id,
                        senderId = data["senderId"] as? String ?: "",
                        content = data["content"] as? String ?: "",
                        timestamp = data["timestamp"] as? Timestamp ?: Timestamp.now(),
                        type = data["type"] as? String ?: "TEXT",
                        imageUrl = data["imageUrl"] as? String
                    )
                }
                onUpdate(msgs)
            }
    }

    fun sendMessage(roomId: String, message: FirebaseChatMessage) {
        val messageMap = mapOf(
            "senderId" to message.senderId,
            "content" to message.content,
            "timestamp" to message.timestamp,
            "type" to message.type,
            "imageUrl" to message.imageUrl
        )

        val chatRoomRef = db.collection("chatRooms").document(roomId)

        chatRoomRef.get().addOnSuccessListener { roomSnapshot ->
            val participants = extractParticipants(roomSnapshot.get("participants"))
            val usersNotRead = participants.map { it.toString() }.filter { it != message.senderId }

            chatRoomRef.collection("messages").add(messageMap).addOnSuccessListener {
                chatRoomRef.update(
                    mapOf(
                        "lastMessage" to message.content,
                        "lastMessageTimestamp" to message.timestamp,
                        "usersNotRead" to usersNotRead
                    )
                ).addOnSuccessListener {}
            }
        }
    }

    fun fetchUserDisplayName(userId: Int, onResult: (String) -> Unit) {
        db.collection("users")
            .whereEqualTo("id", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val doc = snapshot.documents.firstOrNull()
                val name = doc?.let {
                    val first = it.getString("firstname") ?: ""
                    val last = it.getString("surname") ?: ""
                    "$first $last".trim()
                } ?: "Unknown User"
                onResult(name)
            }
    }

    fun fetchChatRoom(roomId: String, onResult: (Map<String, Any>?) -> Unit) {
        db.collection("chatRooms").document(roomId).get()
            .addOnSuccessListener { onResult(it.data) }
    }


    private fun extractParticipants(raw: Any?): List<Int> = when (raw) {
        is List<*> -> raw.mapNotNull {
            when (it) {
                is Number -> it.toInt()
                is String -> it.toIntOrNull()
                else -> null
            }
        }
        is Map<*, *> -> raw.values.mapNotNull { v ->
            when (v) {
                is Map<*, *> -> v.values.firstOrNull()?.let {
                    when (it) {
                        is Number -> it.toInt()
                        is String -> it.toIntOrNull()
                        else -> null
                    }
                }
                else -> null
            }
        }
        else -> emptyList()
    }

    fun createOrGetPrivateChatRoom(
        currentUserId: Int,
        otherUserId: Int,
        onRoomReady: (roomId: String) -> Unit
    ) {
        db.collection("chatRooms")
            .whereEqualTo("type", "private")
            .get()
            .addOnSuccessListener { result ->
                val existingRoom = result.documents.firstOrNull { doc ->
                    val participants = (doc["participants"] as? List<*>)?.mapNotNull {
                        when (it) {
                            is Number -> it.toInt()
                            is String -> it.toIntOrNull()
                            else -> null
                        }
                    } ?: emptyList()

                    participants.contains(currentUserId) && participants.contains(otherUserId)
                }

                if (existingRoom != null) {
                    onRoomReady(existingRoom.id)
                } else {
                    val newRoom = hashMapOf(
                        "type" to "private",
                        "participants" to listOf(currentUserId, otherUserId),
                        "name" to "",
                        "lastMessage" to "",
                        "usersNotRead" to listOf<String>()
                    )

                    db.collection("chatRooms")
                        .add(newRoom)
                        .addOnSuccessListener { docRef ->
                            onRoomReady(docRef.id)
                        }
                }
            }
    }

    fun createGroupIfNotExists(
        groupName: String,
        creatorId: Int,
        tripId: String = "",
        onComplete: (created: Boolean) -> Unit
    ) {
        db.collection("chatRooms")
            .whereEqualTo("type", "group")
            .whereEqualTo("name", groupName)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    val newGroup = hashMapOf(
                        "type" to "group",
                        "name" to groupName,
                        "participants" to listOf(creatorId),
                        "lastMessage" to "",
                        "usersNotRead" to emptyList<String>(),
                        "tripId" to tripId
                    )
                    db.collection("chatRooms")
                        .add(newGroup)
                        .addOnSuccessListener {
                            onComplete(true)
                        }
                        .addOnFailureListener {
                            onComplete(false)
                        }
                } else {
                    onComplete(false)
                }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }


    fun fetchChatIdByName(roomName: String, onResult: (roomId: String?) -> Unit) {
        db.collection("chatRooms")
            .whereEqualTo("name", roomName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()
                onResult(doc?.id)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun addParticipantToGroup(participantId: Int, groupName: String, onComplete: (Boolean) -> Unit) {
        db.collection("chatRooms")
            .whereEqualTo("type", "group")
            .whereEqualTo("name", groupName)
            .get()
            .addOnSuccessListener { result ->
                val groupDoc = result.documents.firstOrNull()
                if (groupDoc == null) {
                    Log.w("ChatDebug", "Group $groupName not found")
                    onComplete(false)
                    return@addOnSuccessListener
                }

                val groupRef = db.collection("chatRooms").document(groupDoc.id)
                val currentParticipants = (groupDoc["participants"] as? List<*>)?.mapNotNull {
                    when (it) {
                        is Number -> it.toInt()
                        is String -> it.toIntOrNull()
                        else -> null
                    }
                } ?: emptyList()

                if (currentParticipants.contains(participantId)) {
                    Log.d("ChatDebug", "Participant $participantId is already in group $groupName")
                    onComplete(true)
                    return@addOnSuccessListener
                }

                val updatedParticipants = currentParticipants + participantId

                groupRef.update("participants", updatedParticipants)
                    .addOnSuccessListener {
                        Log.d("ChatDebug", "Added participant $participantId to group $groupName")
                        onComplete(true)
                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatDebug", "Failed to add participant: ${e.message}")
                        onComplete(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ChatDebug", "Failed to query group $groupName: ${e.message}")
                onComplete(false)
            }
    }

    fun removeParticipantFromRoom(roomName: String, userIdToRemove: Int, onComplete: (Boolean) -> Unit) {
        fetchChatIdByName(roomName) { roomId ->
            if (roomId == null) {
                Log.w("ChatDebug", "Room $roomName not found")
                onComplete(false)
                return@fetchChatIdByName
            }

            val roomRef = db.collection("chatRooms").document(roomId)

            roomRef.get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.exists()) {
                        Log.w("ChatDebug", "Room $roomId does not exist")
                        onComplete(false)
                        return@addOnSuccessListener
                    }

                    val currentParticipants = (snapshot.get("participants") as? List<*>)?.mapNotNull {
                        when (it) {
                            is Number -> it.toInt()
                            is String -> it.toIntOrNull()
                            else -> null
                        }
                    } ?: emptyList()

                    if (!currentParticipants.contains(userIdToRemove)) {
                        Log.d("ChatDebug", "User $userIdToRemove is not in room $roomName")
                        onComplete(true)
                        return@addOnSuccessListener
                    }

                    val updatedParticipants = currentParticipants.filter { it != userIdToRemove }

                    roomRef.update("participants", updatedParticipants)
                        .addOnSuccessListener {
                            Log.d("ChatDebug", "User $userIdToRemove removed from room $roomName")
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            Log.e("ChatDebug", "Failed to remove user: ${e.message}")
                            onComplete(false)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("ChatDebug", "Failed to fetch chat room: ${e.message}")
                    onComplete(false)
                }
        }
    }

    fun removeUserFromUsersNotRead(roomId: String, userId: String, onComplete: (Boolean) -> Unit) {
        val roomRef = db.collection("chatRooms").document(roomId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(roomRef)
            val usersNotRead = snapshot.get("usersNotRead") as? List<String> ?: emptyList()

            if (userId in usersNotRead) {
                val updatedList = usersNotRead.filter { it != userId }
                transaction.update(roomRef, "usersNotRead", updatedList)
            }
        }.addOnSuccessListener {
            onComplete(true)
        }.addOnFailureListener { e ->
            Log.e("ChatDebug", "Failed to remove user from usersNotRead: ${e.message}")
            onComplete(false)
        }
    }

    suspend fun getChatRoomFromId(chatId: String): ChatRoom? {
        return try {
            val snapshot = db.collection("chatRooms").document(chatId).get().await()
            if (snapshot.exists()) snapshot.toObject(ChatRoom::class.java)
            else null
        } catch (e: Exception) {
            Log.e("ChatModel", "Error fetching chat room: ${e.message}", e)
            null
        }
    }

    fun fetchChatRoomIdByName(roomName: String, onResult: (roomId: String?) -> Unit) {
        db.collection("chatRooms")
            .whereEqualTo("name", roomName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()
                onResult(doc?.id)
            }
            .addOnFailureListener {
                Log.e("ChatModel", "Error fetching chat room ID: ${it.message}", it)
                onResult(null)
            }
    }

    fun blockChatRoom(roomId: String, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        db.collection("chatRooms").document(roomId)
            .update("type", "blocked")
            .addOnSuccessListener {
                Log.d("ChatModel", "Room $roomId successfully blocked.")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("ChatModel", "Failed to block room: ${e.message}", e)
                onFailure(e)
            }
    }
}






