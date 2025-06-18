package com.example.voyago.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.voyago.model.ChatGroup
import com.example.voyago.model.ChatModel
import com.example.voyago.model.PrivateChat
import com.example.voyago.model.ReviewModel
import com.example.voyago.model.TripModel
import com.example.voyago.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    val chatModel: ChatModel = ChatModel(), // Default to a new instance for testing
    val userModel: UserModel = UserModel(),       //Need this for user IDs
    val tripModel: TripModel = TripModel()      //Need this for trip IDs
) : ViewModel() {


    val privateChat: MutableStateFlow<PrivateChat> = MutableStateFlow(PrivateChat())

    fun LoadChats() {
        viewModelScope.launch {
            chatModel.getPrivateChat(1, 2)
            privateChat.value = chatModel.privateChat.value
            Log.d("ChatViewModel", privateChat.value.toString())
        }
    }

    // ----- Group Chats -----

    // GROUP CHATS
    private val _chatGroups = MutableStateFlow<List<ChatGroup>>(emptyList())
    val chatGroups: StateFlow<List<ChatGroup>> = _chatGroups
    private val _filteredChatGroups = mutableStateOf<List<ChatGroup>>(emptyList())
    val filteredChatGroups: MutableState<List<ChatGroup>> = _filteredChatGroups

    // PRIVATE CHATS
    private val _privateChats = MutableStateFlow<List<PrivateChat>>(emptyList())
    val privateChats: StateFlow<List<PrivateChat>> = _privateChats
    private val _filteredPrivateChats = mutableStateOf<List<PrivateChat>>(emptyList())
    val filteredPrivateChats: MutableState<List<PrivateChat>> = _filteredPrivateChats

    // SEARCH
    var searchQuery by mutableStateOf("")
        private set

    fun updateSearch(query: String) {
        searchQuery = query

        // Filter group chats
        _filteredChatGroups.value = _chatGroups.value.filter {
            it.title.contains(query, ignoreCase = true)
        }

        // Filter private chats
        _filteredPrivateChats.value = _privateChats.value.filter {
            it.username.contains(query, ignoreCase = true)
        }
    }

    fun loadChatGroupsForUser(userId: String) {
        val loadedGroups = listOf(
            ChatGroup(
                "1",
                "Trip to Italy",
                "Ciao ragazzi!",
                1680000000000,
                unreadCount = 3
            ),
            ChatGroup("2", "Mountain Hike", "See you all at 7AM", 1680050000000),
            ChatGroup("3", "Beach Day", "Bring sunscreen!", 1680100000000)
        )
        _chatGroups.value = loadedGroups
        _filteredChatGroups.value = loadedGroups
    }

    fun loadPrivateChatsForUser(userId: String) {
        val loadedPrivateChats = listOf(
            PrivateChat("u1", "Alice", "Hey!", unreadCount = 1),
            PrivateChat("u2", "Sofia", "Let's plan!"),
            PrivateChat("u3", "Liam", "See you soon.")
        )
        _privateChats.value = loadedPrivateChats
        _filteredPrivateChats.value = loadedPrivateChats
    }

    init {
        loadChatGroupsForUser("someUserId")
        loadPrivateChatsForUser("someUserId")
    }

}

object ChatFactory : ViewModelProvider.Factory {
    private val chatModel = ChatModel()

    private val tripModel: TripModel = TripModel()
    private val userModel: UserModel = UserModel()
    private val reviewModel: ReviewModel = ReviewModel()

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {

        val savedStateHandle = extras.createSavedStateHandle()

        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(ChatViewModel::class.java) ->
                ChatViewModel(chatModel, userModel, tripModel) as T

            modelClass.isAssignableFrom(UserViewModel::class.java) ->
                UserViewModel(userModel) as T

            modelClass.isAssignableFrom(TripViewModel::class.java) ->
                TripViewModel(tripModel, userModel, reviewModel, savedStateHandle) as T

            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }


}