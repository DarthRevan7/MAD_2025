package com.example.voyago

import android.media.Image
import java.util.Calendar

data class Article(
    val id: Int,
    var title: String,
    var text: String,
    var authorId: Int,
    var date: Calendar,
    var photos: List<Image>?,
    var views: Int
)
