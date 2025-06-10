package com.example.voyago.model

import com.example.voyago.Collections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.flowOf


data class ChatMessage(
    val id: String = "",
    val senderId: Int = 0,           // 使用你的User模型的id (Int类型)
    val senderName: String = "",
    val senderProfileUrl: String? = null,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT
)

enum class MessageType {
    TEXT,
    SYSTEM // 用于系统消息，如"用户加入"等
}

data class ChatRoomState(
    val messages: List<ChatMessage> = emptyList(),
    val onlineUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentMessage: String = "",
    val currentUser: User? = null
)


class ChatRepository(
    private val userModel: UserModel = UserModel()
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val messagesCollection = firestore.collection("chat_messages")

    // 获取在线用户 - 使用你现有的User模型
    fun getOnlineUsers(): Flow<List<User>> = callbackFlow {
        val listener = Collections.users
            .whereEqualTo("isOnline", true)
            .orderBy("username")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                } ?: emptyList()

                trySend(users)
            }

        awaitClose { listener.remove() }
    }

    // 获取聊天消息
    fun getChatMessages(): Flow<List<ChatMessage>> = callbackFlow {
        val listener = messagesCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                }?.reversed() ?: emptyList()

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    // 发送消息 - 使用你的User模型获取当前用户信息
    suspend fun sendMessage(message: String, currentUser: User): Result<Unit> {
        return try {
            val chatMessage = ChatMessage(
                senderId = currentUser.id,
                senderName = currentUser.username,
                senderProfileUrl = currentUser.profilePictureUrl,
                message = message,
                timestamp = System.currentTimeMillis()
            )

            messagesCollection.add(chatMessage).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 更新用户在线状态 - 使用你现有的editUserData方法
    suspend fun updateOnlineStatus(user: User, isOnline: Boolean): Result<Unit> {
        return try {
            val updatedUser = user.copy(isOnline = isOnline)
            userModel.editUserData(updatedUser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 获取当前用户信息
    fun getCurrentUser(): Flow<User?> {
        val currentFirebaseUser = auth.currentUser
        return if (currentFirebaseUser != null) {
            userModel.getUserByUid(currentFirebaseUser.uid)
        } else {
            flowOf(null)
        }
    }

    // 发送系统消息
    suspend fun sendSystemMessage(message: String) {
        val systemMessage = ChatMessage(
            senderId = -1, // 系统消息使用特殊ID
            senderName = "System",
            message = message,
            type = MessageType.SYSTEM
        )
        messagesCollection.add(systemMessage)
    }
}