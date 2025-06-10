package com.example.voyago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.voyago.model.ChatRepository
import com.example.voyago.model.ChatRoomState
import com.example.voyago.model.User
import com.example.voyago.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class ChatRoomViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatRoomState())
    val uiState: StateFlow<ChatRoomState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
        observeOnlineUsers()
        observeChatMessages()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            repository.getCurrentUser()
                .collect { user ->
                    _uiState.update { it.copy(currentUser = user) }
                    user?.let { updateOnlineStatus(it, true) }
                }
        }
    }

    private fun observeOnlineUsers() {
        viewModelScope.launch {
            repository.getOnlineUsers()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { users ->
                    _uiState.update { it.copy(onlineUsers = users) }
                }
        }
    }

    private fun observeChatMessages() {
        viewModelScope.launch {
            repository.getChatMessages()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { messages ->
                    _uiState.update { it.copy(messages = messages) }
                }
        }
    }

    fun sendMessage() {
        val currentMessage = _uiState.value.currentMessage.trim()
        val currentUser = _uiState.value.currentUser

        if (currentMessage.isEmpty() || currentUser == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.sendMessage(currentMessage, currentUser)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            currentMessage = "",
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun updateMessage(message: String) {
        _uiState.update { it.copy(currentMessage = message) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun updateOnlineStatus(user: User, isOnline: Boolean) {
        viewModelScope.launch {
            repository.updateOnlineStatus(user, isOnline)
        }
    }

    override fun onCleared() {
        super.onCleared()
        _uiState.value.currentUser?.let { user ->
            updateOnlineStatus(user, false)
        }
    }
}

object ChatRoomFactory : ViewModelProvider.Factory {
    private val userModel = UserModel()
    private val repository = ChatRepository(userModel)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ChatRoomViewModel::class.java) -> ChatRoomViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}