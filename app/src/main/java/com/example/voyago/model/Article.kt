package com.example.voyago.model

import android.util.Log
import com.example.voyago.Collections
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
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
    var date: Long? = null,
    var photo: List<String> = emptyList(), // 🔥 支持多张图片
    val contentUrl: String? = null,
    val tags: List<String> = emptyList(),
    var viewCount: Int = 0
) {
    // 🔥 获取第一张图片（用于列表显示）
    suspend fun getPhoto(): String? {
        return getPhotoAt(0)
    }

    // 🔥 获取指定位置的图片
    suspend fun getPhotoAt(index: Int): String? {
        return try {
            if (photo.isEmpty() || index >= photo.size) {
                return com.example.voyago.StorageHelper.getImageDownloadUrl("articles/placeholder.jpg")
            }

            val photoPath = photo[index]
            when {
                // 如果已经是完整的 HTTP URL，直接返回
                photoPath.startsWith("http") -> photoPath

                // 如果包含路径分隔符，说明是 Firebase Storage 路径
                photoPath.contains("/") -> {
                    val storageRef = Firebase.storage.reference.child(photoPath)
                    storageRef.downloadUrl.await().toString()
                }

                // 否则假设是在 articles/ 目录下
                else -> {
                    val storageRef = Firebase.storage.reference.child("articles/$photoPath")
                    storageRef.downloadUrl.await().toString()
                }
            }
        } catch (e: Exception) {
            Log.e("Article", "Failed to get photo URL for ${photo.getOrNull(index)}", e)
            com.example.voyago.StorageHelper.getImageDownloadUrl("articles/placeholder.jpg")
        }
    }

    // 🔥 获取所有图片的URL
    suspend fun getAllPhotos(): List<String> {
        return photo.indices.mapNotNull { index ->
            try {
                getPhotoAt(index)
            } catch (e: Exception) {
                Log.e("Article", "Failed to get photo at index $index", e)
                null
            }
        }
    }

    constructor() : this(null, null, null, null, null, emptyList(), null, emptyList())
}

fun parseArticles(snapshot: QuerySnapshot): List<Article> {
    Log.d("parseArticles", "🔥 Starting to parse ${snapshot.documents.size} documents")

    return snapshot.documents.mapNotNull { doc ->
        try {
            Log.d("parseArticles", "🔥 Parsing document: ${doc.id}")

            val id = doc.getLong("id")?.toInt()
            val title = doc.getString("title")
            val text = doc.getString("text")
            val authorId = doc.getLong("authorId")?.toInt()

            // 🔥 修复日期解析：支持多种格式
            val dateMs = try {
                val ts = doc.getTimestamp("date")
                ts?.toDate()?.time
            } catch (e: Exception) {
                try {
                    doc.getLong("date")
                } catch (e2: Exception) {
                    try {
                        doc.getDouble("date")?.toLong()
                    } catch (e3: Exception) {
                        Log.w("parseArticles", "🔥 Could not parse date for doc ${doc.id}, using current time")
                        System.currentTimeMillis()
                    }
                }
            }

            // 🔥 解析图片：支持单张图片和多张图片
            val photos = try {
                // 尝试作为列表读取
                val photoList = doc.get("photo") as? List<String>
                photoList ?: emptyList()
            } catch (e: Exception) {
                try {
                    // 如果失败，尝试作为单个字符串读取（向后兼容）
                    val singlePhoto = doc.getString("photo")
                    if (singlePhoto != null) listOf(singlePhoto) else emptyList()
                } catch (e2: Exception) {
                    Log.w("parseArticles", "🔥 Could not parse photo for doc ${doc.id}")
                    emptyList()
                }
            }

            val contentUrl = doc.getString("contentUrl")
            val tags = doc.get("tags") as? List<String> ?: emptyList()
            val viewCount = doc.getLong("viewCount")?.toInt() ?: 0

            val article = Article(
                id = id,
                title = title,
                text = text,
                authorId = authorId,
                date = dateMs,
                photo = photos, // 🔥 使用图片列表
                contentUrl = contentUrl,
                tags = tags,
                viewCount = viewCount
            )

            Log.d("parseArticles", "🔥 Successfully parsed article: id=${article.id}, title=${article.title}, photos=${article.photo.size}")
            article

        } catch (e: Exception) {
            Log.e("parseArticles", "🔥 Error parsing document ${doc.id}: ${e.message}", e)
            null
        }
    }
}

class TheArticlesModel {

    fun getArticles(): Flow<List<Article>> = callbackFlow {
        Log.d("TheArticlesModel", "🔥 Starting to listen for articles...")

        val listenerRegistration = Collections.articles
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
                Log.d("TheArticlesModel", "🔥 Snapshot listener triggered")

                when {
                    exception != null -> {
                        Log.e("TheArticlesModel", "🔥 Listen error: ${exception.message}", exception)
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        Log.d("TheArticlesModel", "🔥 Snapshot received with ${snapshot.documents.size} documents")
                        Log.d("TheArticlesModel", "🔥 Snapshot metadata: fromCache=${snapshot.metadata.isFromCache}")

                        snapshot.documents.forEachIndexed { index, doc ->
                            Log.d("TheArticlesModel", "🔥 Document $index: ID=${doc.id}, exists=${doc.exists()}")
                        }

                        val articles = parseArticles(snapshot)
                        Log.d("TheArticlesModel", "🔥 Parsed ${articles.size} articles successfully")

                        val success = trySend(articles)
                        Log.d("TheArticlesModel", "🔥 trySend result: ${success.isSuccess}")

                    }
                    else -> {
                        Log.w("TheArticlesModel", "🔥 Both snapshot and exception are null")
                        trySend(emptyList())
                    }
                }
            }

        awaitClose {
            Log.d("TheArticlesModel", "🔥 Removing Firebase listener")
            listenerRegistration.remove()
        }
    }

    suspend fun forceRefresh(): List<Article> {
        return try {
            Log.d("TheArticlesModel", "🔥 Force refresh starting...")

            val snapshot = Collections.articles
                .orderBy("date", Query.Direction.DESCENDING)
                .get(Source.SERVER)
                .await()

            Log.d("TheArticlesModel", "🔥 Force refresh got ${snapshot.documents.size} documents")

            val articles = parseArticles(snapshot)
            Log.d("TheArticlesModel", "🔥 Force refresh parsed ${articles.size} articles")
            articles

        } catch (e: Exception) {
            Log.e("TheArticlesModel", "🔥 Force refresh failed: ${e.message}", e)
            emptyList()
        }
    }
}