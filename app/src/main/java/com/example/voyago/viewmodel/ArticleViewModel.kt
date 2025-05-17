package com.example.voyago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.voyago.model.domain.Article
import com.example.voyago.model.data.ArticleRepository
import com.example.voyago.model.data.sampleArticles
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ArticleViewModel(
    private val repo: ArticleRepository
) : ViewModel() {

    // 把 repo.streamAllArticles() 转成一个 StateFlow，
    // Compose 端 collectAsState() 时就能拿到最新列表
    val articleList: StateFlow<List<Article>> = repo
        .streamAllArticles()
        .stateIn(
            scope = viewModelScope,                   // 绑定到 ViewModel 生命周期
            started = SharingStarted.Lazily,          // UI 第一次 collect 时才启动
            initialValue = emptyList()                // 默认给一个空列表
        )
}

class ArticleViewModelFactory(
    private val repo: ArticleRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArticleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArticleViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

