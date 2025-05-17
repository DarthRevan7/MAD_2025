package com.example.voyago.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voyago.model.domain.Article
import com.example.voyago.model.domain.UserData
import com.example.voyago.model.data.ArticleRepository
import com.example.voyago.model.data.sampleArticles
import com.example.voyago.model.data.sampleUserData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    // 暴露给 UI 的用户列表
    private val _users = MutableStateFlow<List<UserData>>(emptyList())
    val users: StateFlow<List<UserData>> = _users

    //User Business Logic
    fun getUsers(ids: List<Int>): List<UserData> {
        return _users.value.filter { it.id in ids }
    }

    fun getUserDataById(id: Int): UserData {
        return _users.value.find { it.id == id }
            ?: throw NoSuchElementException("User with ID $id not found")
    }


    // 可选：提供一个 loading flag
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init {
        viewModelScope.launch {
            _loading.value = true
            try {
                // 1) 看一下集合是不是空
                val col = firestore.collection("users")
                val snap = col.get().await()
                if (snap.isEmpty) {
                    // 2) 如果空，则批量写入 sampleUserData()
                    val data = sampleUserData()
                    coroutineScope {
                        data.map { user ->
                            async {
                                // 假设用 id 作为 document key
                                col.document(user.id.toString())
                                    .set(user)  // UserData 必须是 POJO / data class，可被 Firestore 自动序列化
                                    .await()
                            }
                        }.awaitAll()
                    }
                }
                // 3) 不管之前是不是空，都再一次把最新数据拉回来
                val freshSnap = col.get().await()
                val list = freshSnap.documents.mapNotNull {
                    it.toObject(UserData::class.java)
                }
                _users.value = list

            } catch (e: Exception) {
                Log.e("UserViewModel", "初始化用户列表失败", e)
            } finally {
                _loading.value = false
            }
        }
    }
}
