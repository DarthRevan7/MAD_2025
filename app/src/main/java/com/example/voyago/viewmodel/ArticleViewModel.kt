package com.example.voyago.viewmodel

import androidx.lifecycle.ViewModel
import com.example.voyago.model.Article
import com.example.voyago.model.Review
import com.example.voyago.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.util.Calendar


class ArticleViewModel : ViewModel() {
    private val _articleList = MutableStateFlow<List<Article>>(emptyList())
    val articleList: StateFlow<List<Article>> = _articleList

    init {
        _articleList.value = sampleArticles()
    }
}

