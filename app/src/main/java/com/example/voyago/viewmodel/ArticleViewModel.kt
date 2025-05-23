package com.example.voyago.viewmodel

import androidx.lifecycle.ViewModel
import com.example.voyago.model.Article
import com.example.voyago.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter


class ArticleViewModel() : ViewModel() {
    private val _articleList = MutableStateFlow<List<Article>>(emptyList())
    val articleList: StateFlow<List<Article>> = _articleList

    fun articlesByUserId(userId: Int): List<Article> {
        return _articleList.value.filter { it.authorId == userId }
    }

    init {
        _articleList.value = sampleArticles()
    }
}

