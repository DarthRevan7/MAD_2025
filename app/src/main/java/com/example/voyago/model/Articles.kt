package com.example.voyago.model

import java.time.LocalDateTime

data class Article(
    val id: Long,
    val title: String,               // 标题
    val description: String,             // 摘要／简介
    val imageUrl: String,            // 文章封面图（URL 或者本地 Uri）
    val author: String,              // 作者
    val publishedAt: LocalDateTime,  // 发布时间
    val contentUrl: String,          // 点击后跳转的链接
    val tags: List<String> = emptyList()  // 可选：标签列表
)

