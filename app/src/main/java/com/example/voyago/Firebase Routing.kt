package com.example.voyago

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.firestore
import com.google.firebase.Timestamp

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

fun Any?.toEpochMillisOrZero(): Long {
    return when (this) {
        is Long -> this
        is Timestamp -> this.toDate().time
        else -> 0L
    }
}
