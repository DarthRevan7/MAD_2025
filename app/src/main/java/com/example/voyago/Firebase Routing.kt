package com.example.voyago

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.firestore
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import kotlinx.coroutines.tasks.await

object Collections{
    private const val C_USERS = "users"
    private const val C_TRIPS = "trips"
    private const val C_REVIEWS = "reviews"
    private const val C_ARTICLES = "articles"

    private val db: FirebaseFirestore
        get() = Firebase.firestore
    

    init {
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) //false to Disable LocalChaching
            .build()
    }

    val users = db.collection(C_USERS)
    val trips = db.collection(C_TRIPS)
    val reviews = db.collection(C_REVIEWS)
    val articles = db.collection(C_ARTICLES)
}

object StorageHelper {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadImageToStorage(uri: Uri, path: String): Pair<Boolean, String?> {
        val ref = storage.reference.child(path)
        return try {
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Pair(true, downloadUrl)
        } catch (e: Exception) {
            Pair(false, null)
        }
    }

    fun deleteImageFromStorage(path: String, onResult: (Boolean) -> Unit) {
        val ref = storage.reference.child(path)
        ref.delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    suspend fun getImageDownloadUrl(path: String): String? {
        val ref = storage.reference.child(path)
        return try {
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }
}