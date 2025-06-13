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
    var photo: String? = null,
    val contentUrl: String? = null,
    val tags: List<String> = emptyList(),
    var viewCount: Int = 0
) {
    suspend fun getPhoto(): String? {
        return try {
            if (photo.isNullOrEmpty()) {
                return com.example.voyago.StorageHelper.getImageDownloadUrl("articles/placeholder.jpg")
            }

            when {
                photo!!.startsWith("http") -> photo
                photo!!.contains("/") -> {
                    val storageRef = Firebase.storage.reference.child(photo!!)
                    storageRef.downloadUrl.await().toString()
                }
                else -> {
                    val storageRef = Firebase.storage.reference.child("articles/$photo")
                    storageRef.downloadUrl.await().toString()
                }
            }
        } catch (e: Exception) {
            Log.e("Article", "Failed to get photo URL for $photo", e)
            com.example.voyago.StorageHelper.getImageDownloadUrl("articles/placeholder.jpg")
        }
    }

    constructor() : this(null, null, null, null, null, null, null, emptyList())
}

fun parseArticles(snapshot: QuerySnapshot): List<Article> {
    Log.d("parseArticles", "🔥 Starting to parse ${snapshot.documents.size} documents")

    return snapshot.documents.mapNotNull { doc ->
        try {
            Log.d("parseArticles", "🔥 Parsing document: ${doc.id}")
            Log.d("parseArticles", "🔥 Document data: ${doc.data}")

            val id = doc.getLong("id")?.toInt()
            val title = doc.getString("title")
            val text = doc.getString("text")
            val authorId = doc.getLong("authorId")?.toInt()
            val ts = doc.getTimestamp("date")
            val dateMs = ts?.toDate()?.time
            val photo = doc.getString("photo")
            val contentUrl = doc.getString("contentUrl")
            val tags = doc.get("tags") as? List<String> ?: emptyList()
            val viewCount = doc.getLong("viewCount")?.toInt() ?: 0

            val article = Article(
                id = id,
                title = title,
                text = text,
                authorId = authorId,
                date = dateMs,
                photo = photo,
                contentUrl = contentUrl,
                tags = tags,
                viewCount = viewCount
            )

            Log.d("parseArticles", "🔥 Successfully parsed article: id=${article.id}, title=${article.title}")
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

        // 🔥 直接使用 Collections.articles，但添加更多调试信息
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

                        // 🔥 记录文档详情
                        snapshot.documents.forEachIndexed { index, doc ->
                            Log.d("TheArticlesModel", "🔥 Document $index: ID=${doc.id}, exists=${doc.exists()}")
                        }

                        val articles = parseArticles(snapshot)
                        Log.d("TheArticlesModel", "🔥 Parsed ${articles.size} articles successfully")

                        // 🔥 发送结果
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