package com.example.voyago.model

import android.util.Log
import com.example.voyago.Collections
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
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
    var photo: List<String> = emptyList(), // Support multiple pictures
    val contentUrl: String? = null,
    val tags: List<String> = emptyList(),
    var viewCount: Int = 0
) {
    // Suspended function that retrieves the first photo (at index 0)
    // Returns the photo as a String, or null if unavailable
    suspend fun getPhoto(): String? {
        return getPhotoAt(0)
    }


    // Suspended function that retrieves the photo at the specified index
    // Returns a String if successful, or a placeholder if any error occurs
    suspend fun getPhotoAt(index: Int): String? {
        return try {
            // If the photo list is empty or the index is out of bounds, return placeholder image URL
            if (photo.isEmpty() || index >= photo.size) {
                return com.example.voyago.StorageHelper.getImageDownloadUrl("articles/placeholder.jpg")
            }

            // Get the photo path string at the given index
            val photoPath = photo[index]

            when {
                // Case 1: If the path already starts with "http", it is assumed to be a complete URL.
                // No need to fetch from Firebase; return it directly.
                photoPath.startsWith("http") -> photoPath

                // Case 2: If the path includes a "/", treat it as a full Firebase Storage path.
                photoPath.contains("/") -> {
                    // Create a reference
                    val storageRef = Firebase.storage.reference.child(photoPath)
                    // Retrieve the download URL
                    storageRef.downloadUrl.await().toString()
                }

                // Case 3: If the path does not contain "/", assume it's a filename under the "articles/" folder.
                else -> {
                    // Prepend "articles/" to the filename
                    val storageRef = Firebase.storage.reference.child("articles/$photoPath")
                    // Retrieve the download URL from Firebase.
                    storageRef.downloadUrl.await().toString()
                }
            }
        } catch (e: Exception) {
            // If any exception occurs during the process
            // log the error with the failed photo path
            Log.e("Article", "Failed to get photo URL for ${photo.getOrNull(index)}", e)
            // Return the placeholder image URL as a fallback.
            com.example.voyago.StorageHelper.getImageDownloadUrl("articles/placeholder.jpg")
        }
    }


    // Suspended function that retrieves all photos in the `photo` list.
    suspend fun getAllPhotos(): List<String> {
        return photo.indices.mapNotNull { index ->
            try {
                // Attempt to get the photo URL at the current index
                getPhotoAt(index)
            } catch (e: Exception) {
                // Log the error if the retrieval fails and skip this photo by returning null
                Log.e("Article", "Failed to get photo at index $index", e)
                null
            }
        }
    }
}

// Parses a Firestore QuerySnapshot into a list of Article objects
fun parseArticles(snapshot: QuerySnapshot): List<Article> {
    // Iterate over each document and attempt to parse it into an Article
    return snapshot.documents.mapNotNull { doc ->
        try {
            // Parse required fields from the document
            val id = doc.getLong("id")?.toInt()
            val title = doc.getString("title")
            val text = doc.getString("text")
            val authorId = doc.getLong("authorId")?.toInt()

            // Attempt to parse the date field in multiple fallback formats
            val dateMs = try {
                // Try to parse the date field in a timestamp
                val ts = doc.getTimestamp("date")
                ts?.toDate()?.time
            } catch (_: Exception) {
                try {
                    // Try to parse the date field in a long
                    doc.getLong("date")
                } catch (_: Exception) {
                    try {
                        // Try to parse the date field in a double
                        doc.getDouble("date")?.toLong()
                    } catch (_: Exception) {
                        // If all parsing fails, log warning and use current system time
                        Log.w(
                            "parseArticles",
                            "🔥 Could not parse date for doc ${doc.id}, using current time"
                        )
                        System.currentTimeMillis()
                    }
                }
            }

            // Attempt to parse the photo field, which may be a list or a single string
            val photos = try {
                // Try to parse the photo list as a list of string
                val photoList = doc.get("photo") as? List<String>
                photoList ?: emptyList()
            } catch (_: Exception) {
                try {
                    // Try to parse the photo list as a single string
                    val singlePhoto = doc.getString("photo")
                    if (singlePhoto != null) listOf(singlePhoto) else emptyList()
                } catch (_: Exception) {
                    // If photo parsing fails, log a warning and fall back to an empty list
                    Log.w("parseArticles", "🔥 Could not parse photo for doc ${doc.id}")
                    emptyList()
                }
            }

            // Parse optional fields with safe defaults
            val contentUrl = doc.getString("contentUrl")
            val tags = doc.get("tags") as? List<String> ?: emptyList()
            val viewCount = doc.getLong("viewCount")?.toInt() ?: 0

            // Construct the Article object using the parsed values
            val article = Article(
                id = id,
                title = title,
                text = text,
                authorId = authorId,
                date = dateMs,
                photo = photos,
                contentUrl = contentUrl,
                tags = tags,
                viewCount = viewCount
            )

            //Return the article
            article

        } catch (e: Exception) {
            // Log and skip any document that fails to parse completely
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
                        Log.d(
                            "TheArticlesModel",
                            "🔥 Snapshot received with ${snapshot.documents.size} documents"
                        )
                        Log.d(
                            "TheArticlesModel",
                            "🔥 Snapshot metadata: fromCache=${snapshot.metadata.isFromCache}"
                        )

                        snapshot.documents.forEachIndexed { index, doc ->
                            Log.d(
                                "TheArticlesModel",
                                "🔥 Document $index: ID=${doc.id}, exists=${doc.exists()}"
                            )
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