package com.example.voyago.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.voyago.Collections
import com.example.voyago.model.ChatRoom
import com.example.voyago.model.FirebaseChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms

    private val _messages = MutableStateFlow<List<FirebaseChatMessage>>(emptyList())
    val messages: StateFlow<List<FirebaseChatMessage>> = _messages

    fun fetchChatRoomsForUser(userId: Int) {
        Log.d("ChatDebug", "Fetching chat rooms for userId: $userId")

        db.collection("chatRooms")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatDebug", "Error fetching chatRooms: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w("ChatDebug", "Snapshot is null")
                    return@addSnapshotListener
                }

                Log.d("ChatDebug", "Found ${snapshot.documents.size} chat room documents")

                val rooms = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null

                    val participants: List<Int> = when (val raw = data["participants"]) {
                        is List<*> -> {
                            Log.d("ChatDebug", "Raw participants is List: $raw")
                            raw.mapNotNull {
                                when (it) {
                                    is Number -> it.toInt()
                                    is String -> it.toIntOrNull()
                                    else -> null
                                }
                            }
                        }
                        is Map<*, *> -> {
                            Log.d("ChatDebug", "Raw participants is Map: $raw")
                            raw.values.mapNotNull { inner ->
                                when (inner) {
                                    is Map<*, *> -> {
                                        inner.values.firstOrNull()?.let {
                                            when (it) {
                                                is Number -> it.toInt()
                                                is String -> it.toIntOrNull()
                                                else -> null
                                            }
                                        }
                                    }
                                    else -> null
                                }
                            }
                        }
                        else -> {
                            Log.w("ChatDebug", "Invalid participants field format in document ${doc.id}")
                            emptyList()
                        }
                    }

                    Log.d("ChatDebug", "Document ${doc.id} participants: $participants")

                    if (participants.contains(userId)) {
                        Log.d("ChatDebug", "User $userId is a participant in room ${doc.id}")
                        ChatRoom(
                            id = doc.id,
                            type = data["type"] as? String ?: "private",
                            participants = participants,
                            name = data["name"] as? String ?: "",
                            lastMessage = data["lastMessage"] as? String ?: "",
                            usersNotRead = (data["usersNotRead"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        )
                    } else {
                        Log.d("ChatDebug", "User $userId is NOT a participant in room ${doc.id}")
                        null
                    }
                }

                Log.d("ChatDebug", "Total chat rooms for user $userId: ${rooms.size}")
                _chatRooms.value = rooms
            }
    }

    fun fetchMessagesForRoom(roomId: String) {
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
                _messages.value = msgs
                msgs.forEach { message ->
                    if (message.senderId != "") {
                        fetchSenderName(message.senderId)
                    }
                }
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

        // Fetch participants before sending message
        chatRoomRef.get().addOnSuccessListener { roomSnapshot ->
            val roomData = roomSnapshot.data
            val participants = when (val raw = roomData?.get("participants")) {
                is List<*> -> raw.mapNotNull {
                    when (it) {
                        is Number -> it.toInt().toString()
                        is String -> it
                        else -> null
                    }
                }
                else -> emptyList()
            }

            // Filter out the sender
            val usersNotRead = participants.filter { it != message.senderId }

            // Add the message to the messages subcollection
            chatRoomRef.collection("messages").add(messageMap).addOnSuccessListener {
                // Update lastMessage and usersNotRead
                chatRoomRef.update(
                    mapOf(
                        "lastMessage" to message.content,
                        "lastMessageTimestamp" to message.timestamp,
                        "usersNotRead" to usersNotRead
                    )
                )
            }
        }
    }


    private val _senderNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val senderNames: StateFlow<Map<String, String>> = _senderNames

    fun fetchSenderName(senderId: String) {
        if (_senderNames.value.containsKey(senderId)) return // Already fetched

        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("id", senderId.toIntOrNull())
            .get()
            .addOnSuccessListener { snapshot ->
                val userDoc = snapshot.documents.firstOrNull()
                val displayName = if (userDoc != null) {
                    val firstname = userDoc.getString("firstname") ?: ""
                    val surname = userDoc.getString("surname") ?: ""
                    "$firstname $surname".trim()
                } else "Unknown User"

                _senderNames.value = _senderNames.value.toMutableMap().apply {
                    put(senderId, displayName)
                }
            }
    }

    private val _chatRoomNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val chatRoomNames: StateFlow<Map<String, String>> = _chatRoomNames

    private val _chatRoomTypes = MutableStateFlow<Map<String, String>>(emptyMap())
    val chatRoomTypes: StateFlow<Map<String, String>> = _chatRoomTypes


    fun fetchChatRoomName(roomId: String, currentUserId: Int) {
        if (_chatRoomNames.value.containsKey(roomId)) return

        db.collection("chatRooms")
            .document(roomId)
            .get()
            .addOnSuccessListener { doc ->
                val data = doc.data
                val type = data?.get("type") as? String ?: "private"
                val nameFromRoom = data?.get("name") as? String ?: "Chat"

                // Track type regardless of private/group
                _chatRoomTypes.value = _chatRoomTypes.value.toMutableMap().apply {
                    put(roomId, type)
                }

                val participantsRaw = data?.get("participants")
                val participants: List<Int> = when (participantsRaw) {
                    is List<*> -> participantsRaw.mapNotNull {
                        when (it) {
                            is Number -> it.toInt()
                            is String -> it.toIntOrNull()
                            else -> null
                        }
                    }
                    is Map<*, *> -> participantsRaw.values.mapNotNull { inner ->
                        when (inner) {
                            is Map<*, *> -> inner.values.firstOrNull()?.let {
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

                if (type == "private" && participants.size == 2) {
                    val otherParticipantId = participants.firstOrNull { it != currentUserId }
                    if (otherParticipantId != null) {
                        db.collection("users")
                            .whereEqualTo("id", otherParticipantId)
                            .get()
                            .addOnSuccessListener { userSnapshot ->
                                val userDoc = userSnapshot.documents.firstOrNull()
                                val displayName = if (userDoc != null) {
                                    val firstname = userDoc.getString("firstname") ?: ""
                                    val surname = userDoc.getString("surname") ?: ""
                                    "$firstname $surname"
                                } else {
                                    "Unknown User"
                                }

                                _chatRoomNames.value = _chatRoomNames.value.toMutableMap().apply {
                                    put(roomId, displayName)
                                }
                            }
                            .addOnFailureListener {
                                _chatRoomNames.value = _chatRoomNames.value.toMutableMap().apply {
                                    put(roomId, "Unknown User")
                                }
                            }
                    }
                } else {
                    _chatRoomNames.value = _chatRoomNames.value.toMutableMap().apply {
                        put(roomId, nameFromRoom)
                    }
                }
            }
            .addOnFailureListener {
                _chatRoomNames.value = _chatRoomNames.value.toMutableMap().apply {
                    put(roomId, "Unknown Chat")
                }
            }
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
                    // Create a new private room
                    val newRoom = hashMapOf(
                        "type" to "private",
                        "participants" to listOf(currentUserId, otherUserId),
                        "name" to "", // optional
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

    fun createGroupIfNotExists(groupName: String, creatorId: Int, onComplete: (created: Boolean) -> Unit) {
        db.collection("chatRooms")
            .whereEqualTo("type", "group")
            .whereEqualTo("name", groupName)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    // Group doesn't exist, create it
                    val newGroup = hashMapOf(
                        "type" to "group",
                        "name" to groupName,
                        "participants" to listOf(creatorId),
                        "lastMessage" to "",
                        "usersNotRead" to emptyList<String>()
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
                    // Group exists, do nothing
                    onComplete(false)
                }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun addParticipantToGroup(participantId: Int, groupName: String) {
        db.collection("chatRooms")
            .whereEqualTo("type", "group")
            .whereEqualTo("name", groupName)
            .get()
            .addOnSuccessListener { result ->
                val groupDoc = result.documents.firstOrNull()
                if (groupDoc == null) {
                    Log.w("ChatDebug", "Group $groupName not found")

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

                    return@addOnSuccessListener
                }

                val updatedParticipants = currentParticipants + participantId

                groupRef.update("participants", updatedParticipants)
                    .addOnSuccessListener {
                        Log.d("ChatDebug", "Added participant $participantId to group $groupName")

                    }
                    .addOnFailureListener { e ->
                        Log.e("ChatDebug", "Failed to add participant: ${e.message}")

                    }
            }
            .addOnFailureListener { e ->
                Log.e("ChatDebug", "Failed to query group $groupName: ${e.message}")

            }
    }

    fun removeUserFromUsersNotRead(
        roomId: String,
        userId: String,
    ) {
        val db = FirebaseFirestore.getInstance()
        val roomRef = db.collection("chatRooms").document(roomId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(roomRef)
            val usersNotRead = snapshot.get("usersNotRead") as? List<String> ?: emptyList()

            if (userId in usersNotRead) {
                val updatedList = usersNotRead.filter { it != userId }
                transaction.update(roomRef, "usersNotRead", updatedList)
            }
        }
    }

    private val _chatRoom = MutableStateFlow<ChatRoom?>(null)
    val chatRoom: StateFlow<ChatRoom?> = _chatRoom

    fun getChatRoomFromId(chatId: String) {
        // Start a Coroutine
        viewModelScope.launch {
            try {
                // Wait for Firebase operation result
                val snapshot = Collections.chatRooms.document(chatId).get().await()

                if (snapshot.exists()) {
                    val loadedChatRoom = snapshot.toObject(ChatRoom::class.java)
                    //Update stateflow and UI
                    _chatRoom.value = loadedChatRoom
                    Log.d("CH1", "ChatRoom caricata e stato aggiornato: $loadedChatRoom")
                } else {
                    // If the chatroom is not found then set to null
                    _chatRoom.value = null
                    Log.d("CH1", "ChatRoom con ID '$chatId' non trovata.")
                }
            } catch (e: Exception) {
                _chatRoom.value = null
                Log.e("CH1", "Errore nel recupero della ChatRoom con ID '$chatId': ${e.message}", e)
            }
        }
    }

    fun fetchChatRoomIdByName(
        roomName: String,
        onResult: (roomId: String?) -> Unit
    ) {
        db.collection("chatRooms")
            .whereEqualTo("name", roomName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val doc = querySnapshot.documents.firstOrNull()
                if (doc != null) {
                    onResult(doc.id)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun removeUserFromChatByName(roomName: String, userId: Int) {
        fetchChatRoomIdByName(roomName) { roomId ->
            if (roomId != null) {
                removeUserFromChat(roomId, userId)
            } else {
                Log.e("ChatVM", "Chat room with name '$roomName' not found.")
            }
        }
    }

    fun removeUserFromChat(roomId: String, userId: Int) {
        val roomRef = db.collection("chatRooms").document(roomId)

        roomRef.update("participants.$userId", FieldValue.delete())
            .addOnSuccessListener {
                Log.d("ChatVM", "User $userId removed from chat room $roomId")
            }
            .addOnFailureListener {
                Log.e("ChatVM", "Failed to remove user $userId from chat room $roomId", it)
            }
    }



}

object ChatFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}







//package com.example.voyago.viewmodel
//
//import android.util.Log
//import androidx.compose.runtime.MutableState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.compose.runtime.snapshotFlow
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.createSavedStateHandle
//import androidx.lifecycle.viewModelScope
//import androidx.lifecycle.viewmodel.CreationExtras
//import com.example.voyago.model.ChatGroup
//import com.example.voyago.model.ChatModel
//import com.example.voyago.model.PrivateChat
//import com.example.voyago.model.ReviewModel
//import com.example.voyago.model.TripModel
//import com.example.voyago.model.UserModel
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.combine
//import kotlinx.coroutines.launch
//
//class ChatViewModel(
//    val chatModel: ChatModel = ChatModel(), // Default to a new instance for testing
//    val userModel: UserModel = UserModel(),       //Need this for user IDs
//    val tripModel: TripModel = TripModel()      //Need this for trip IDs
//) : ViewModel() {
//
//
//    val privateChat: MutableStateFlow<PrivateChat> = MutableStateFlow(PrivateChat())
//
//    fun LoadChats() {
//        viewModelScope.launch {
//            chatModel.getPrivateChat(1, 2)
//            privateChat.value = chatModel.privateChat.value
//            Log.d("ChatViewModel", privateChat.value.toString())
//        }
//    }
//
//    // ----- Group Chats -----
//
//    // GROUP CHATS
//    private val _chatGroups = MutableStateFlow<List<ChatGroup>>(emptyList())
//    val chatGroups: StateFlow<List<ChatGroup>> = _chatGroups
//    private val _filteredChatGroups = mutableStateOf<List<ChatGroup>>(emptyList())
//    val filteredChatGroups: MutableState<List<ChatGroup>> = _filteredChatGroups
//
//    // PRIVATE CHATS
//    private val _privateChats = MutableStateFlow<List<PrivateChat>>(emptyList())
//    val privateChats: StateFlow<List<PrivateChat>> = _privateChats
//    private val _filteredPrivateChats = mutableStateOf<List<PrivateChat>>(emptyList())
//    val filteredPrivateChats: MutableState<List<PrivateChat>> = _filteredPrivateChats
//
//    // SEARCH
//    var searchQuery by mutableStateOf("")
//        private set
//
//    fun updateSearch(query: String) {
//        searchQuery = query
//
//        // Filter group chats
//        _filteredChatGroups.value = _chatGroups.value.filter {
//            it.title.contains(query, ignoreCase = true)
//        }
//
//        // Filter private chats
//        _filteredPrivateChats.value = _privateChats.value.filter {
//            it.username.contains(query, ignoreCase = true)
//        }
//    }
//
//    fun loadAllChatGroups() {
//        FirebaseFirestore.getInstance()
//            .collection("chatRooms")
//            .orderBy("timestamp", Query.Direction.DESCENDING)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null || snapshot == null) {
//                    Log.e("ChatViewModel", "Failed to load chat groups", error)
//                    return@addSnapshotListener
//                }
//
//                val groups = snapshot.documents.mapNotNull { doc ->
//                    val title = doc.getString("title") ?: return@mapNotNull null
//                    val lastMessage = doc.getString("lastMessage") ?: ""
//                    val timestamp = doc.getLong("timestamp") ?: 0L
//                    val unreadCount = (doc.getLong("unreadCount") ?: 0L).toInt()
//
//                    ChatGroup(
//                        id = doc.id,
//                        title = title,
//                        lastMessage = lastMessage,
//                        timestamp = timestamp,
//                        unreadCount = unreadCount
//                    )
//                }
//
//                _chatGroups.value = groups
//                _filteredChatGroups.value = groups.filter {
//                    it.title.contains(searchQuery, ignoreCase = true)
//                }
//            }
//    }
//
//
//    fun loadPrivateChatsForUser(userId: String) {
//        val loadedPrivateChats = listOf(
//            PrivateChat("u1", "Alice", "Hey!", unreadCount = 1),
//            PrivateChat("u2", "Sofia", "Let's plan!"),
//            PrivateChat("u3", "Liam", "See you soon.")
//        )
//        _privateChats.value = loadedPrivateChats
//        _filteredPrivateChats.value = loadedPrivateChats
//    }
//
//    init {
//        viewModelScope.launch {
//            combine(_privateChats, snapshotFlow { searchQuery }) { privates, query ->
//                if (query.isBlank()) privates else privates.filter {
//                    it.username.contains(query, ignoreCase = true)
//                }
//            }.collect {
//                _filteredPrivateChats.value = it
//            }
//        }
//    }
//
//
//}
//
//object ChatFactory : ViewModelProvider.Factory {
//    private val chatModel = ChatModel()
//
//    private val tripModel: TripModel = TripModel()
//    private val userModel: UserModel = UserModel()
//    private val reviewModel: ReviewModel = ReviewModel()
//
//    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
//
//        val savedStateHandle = extras.createSavedStateHandle()
//
//        @Suppress("UNCHECKED_CAST")
//        return when {
//            modelClass.isAssignableFrom(ChatViewModel::class.java) ->
//                ChatViewModel(chatModel, userModel, tripModel) as T
//
//            modelClass.isAssignableFrom(UserViewModel::class.java) ->
//                UserViewModel(userModel) as T
//
//            modelClass.isAssignableFrom(TripViewModel::class.java) ->
//                TripViewModel(tripModel, userModel, reviewModel, savedStateHandle) as T
//
//            else -> throw IllegalArgumentException("Unknown ViewModel")
//        }
//    }
//
//
//}