package com.example.voyago.model

import android.media.Image
import java.time.LocalDateTime
import java.util.Calendar

data class Article(
    val id: Int,
    var title: String,
    var text: String,
    var authorId: String,  //Todo change to it and vm.getUserData
    var date: LocalDateTime, //Todo change to Calendar
    var photo: String,
    val contentUrl: String,          // 点击后跳转的链接
    val tags: List<String> = emptyList()  // 可选：标签列表
)