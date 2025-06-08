package com.example.voyago.model

import android.util.Log
import com.example.voyago.Collections
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class Article(
    val id: Int? = null,
    var title: String? = null,
    var text: String? = null,
    var authorId: Int? = null,
    var date: Long? = null,    // Calendar 改成 Long 时间戳
    var photo: String? = null,
    val contentUrl: String? = null,
    val tags: List<String> = emptyList()

) {
    // 添加获取 Firebase Storage 图片 URL 的方法
    suspend fun getPhoto(): String? {
        return try {
            if (photo.isNullOrEmpty()) {
                // 如果没有照片，返回默认占位图
                return com.example.voyago.StorageHelper.getImageDownloadUrl("articles/placeholder.jpg")
            }

            when {
                // 如果已经是完整的 HTTP URL，直接返回
                photo!!.startsWith("http") -> photo

                // 如果包含路径分隔符，说明是 Firebase Storage 路径
                photo!!.contains("/") -> {
                    val storageRef = Firebase.storage.reference.child(photo!!)
                    storageRef.downloadUrl.await().toString()
                }

                // 否则假设是在 articles/ 目录下
                else -> {
                    val storageRef = Firebase.storage.reference.child("articles/$photo")
                    storageRef.downloadUrl.await().toString()
                }
            }
        } catch (e: Exception) {
            Log.e("Article", "Failed to get photo URL for $photo", e)
            // 返回默认占位图
            com.example.voyago.StorageHelper.getImageDownloadUrl("articles/placeholder.jpg")
        }
    }


    // 可选：无参构造函数（其实默认参数已经自动生成了）
    constructor() : this(null, null, null, null, null, null, null, emptyList())
}

fun parseArticles(snapshot: QuerySnapshot): List<Article> {
    return snapshot.documents.mapNotNull { doc ->
        try {
            val id        = doc.getLong("id")?.toInt()
            val title     = doc.getString("title")
            val text      = doc.getString("text")
            val authorId  = doc.getLong("authorId")?.toInt()
            // 1) 先拿 Timestamp，再转 Date 再拿 time
            val ts        = doc.getTimestamp("date")
            val dateMs    = ts?.toDate()?.time
            val photo     = doc.getString("photo")
            val contentUrl= doc.getString("contentUrl")
            val tags      = doc.get("tags") as? List<String> ?: emptyList()

            Article(
                id        = id,
                title     = title,
                text      = text,
                authorId  = authorId,
                date      = dateMs,     // Long 毫秒
                photo     = photo,
                contentUrl= contentUrl,
                tags      = tags
            )
        } catch (e: Exception) {
            // 解析某条文档失败就跳过
            null
        }
    }
}


object CollectionsArticles{

    private val db: FirebaseFirestore
        get() = Firebase.firestore

    init {
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) //false to Disable LocalChaching
            .build()
    }

    val articles = Collections.articles

}

class TheArticlesModel {

    fun getArticles(): Flow<List<Article>> = callbackFlow {
        val listenerRegistration = CollectionsArticles.articles
            .addSnapshotListener { snapshot, exception ->
                when {
                    exception != null -> {
                        // 监听出错，发一个空列表过去
                        Log.e("TheArticlesModel", "listen error", exception)
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        // snapshot 不为 null，才去解析
                        val articles = parseArticles(snapshot)
                        trySend(articles)
                    }
                    else -> {
                        // snapshot 和 exception 同时为 null，极少见，但也发空列表兜底
                        trySend(emptyList())
                    }
                }
            }

        // channel 关闭时注销 listener
        awaitClose { listenerRegistration.remove() }
    }
}
