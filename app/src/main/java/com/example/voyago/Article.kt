package com.example.voyago

import android.media.Image

data class Article(
    var title: String,
    var text: String,
    var photos: List<Image>?
)
