package com.example.voyago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.voyago.model.Article
import com.example.voyago.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class ArticleViewModel(model: TheArticlesModel) : ViewModel() {


    val articleList: Flow<List<Article>> = model.getArticles()

    fun articlesByUserId(userId: Int): Flow<List<Article>> {
        return articleList.map { articles ->
            articles.filter { it.authorId == userId }


        }
    }
}


object ArticleFactory: ViewModelProvider.Factory{
    private val model = TheArticlesModel()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ArticleViewModel::class.java) -> ArticleViewModel(model) as T
            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}