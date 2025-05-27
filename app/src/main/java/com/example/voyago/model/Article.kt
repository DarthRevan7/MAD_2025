package com.example.voyago.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Calendar
import java.util.Collections

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
    private const val C_Articles = "articles"

    private val db: FirebaseFirestore
        get() = Firebase.firestore

    init {
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) //false to Disable LocalChaching
            .build()
    }



    val articles = db.collection(C_Articles)

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
