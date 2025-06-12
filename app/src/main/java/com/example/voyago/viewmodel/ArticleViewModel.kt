package com.example.voyago.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.voyago.model.Article
import com.example.voyago.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map


class ArticleViewModel(model: TheArticlesModel) : ViewModel() {

    val articleList: Flow<List<Article>> = model.getArticles()

    // 搜索关键词的状态
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // 搜索结果 - 结合文章列表和搜索关键词
    val searchResults: Flow<List<Article>> = combine(
        articleList,
        searchQuery
    ) { articles, query ->
        if (query.isEmpty()) {
            articles
        } else {
            articles.filter { article ->
                // 搜索标题
                article.title?.contains(query, ignoreCase = true) == true ||
                        // 搜索内容
                        article.text?.contains(query, ignoreCase = true) == true ||
                        // 搜索标签
                        article.tags.any { tag ->
                            tag.contains(query, ignoreCase = true)
                        }
            }
        }
    }

    // 按用户ID筛选文章
    fun articlesByUserId(userId: Int): Flow<List<Article>> {
        return articleList.map { articles ->
            articles.filter { it.authorId == userId }
        }
    }

    // 更新搜索关键词
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // 清空搜索
    fun clearSearch() {
        _searchQuery.value = ""
    }

    // 按标签搜索
    fun searchByTag(tag: String): Flow<List<Article>> {
        return articleList.map { articles ->
            articles.filter { article ->
                article.tags.contains(tag)
            }
        }
    }

    // 按日期范围搜索
    fun searchByDateRange(startDate: Long, endDate: Long): Flow<List<Article>> {
        return articleList.map { articles ->
            articles.filter { article ->
                article.date != null &&
                        article.date!! >= startDate &&
                        article.date!! <= endDate
            }
        }
    }

    // 获取最近的文章（按日期排序）
    fun getRecentArticles(limit: Int = 5): Flow<List<Article>> {
        return articleList.map { articles ->
            articles
                .sortedByDescending { it.date }
                .take(limit)
        }
    }

    // 获取热门文章（可以根据实际需求定义"热门"的逻辑）
    fun getPopularArticles(limit: Int = 5): Flow<List<Article>> {
        return articleList.map { articles ->
            // 这里可以根据实际的热度指标排序
            // 比如：阅读量、点赞数等
            // 暂时用日期模拟，最新的作为"热门"
            articles
                .sortedByDescending { it.date }
                .take(limit)
        }
    }

    // 获取特定作者的最新文章
    fun getRecentArticlesByAuthor(authorId: Int, limit: Int = 5): Flow<List<Article>> {
        return articleList.map { articles ->
            articles
                .filter { it.authorId == authorId }
                .sortedByDescending { it.date }
                .take(limit)
        }
    }

    // 获取包含特定标签的文章
    fun getArticlesByTags(tags: List<String>): Flow<List<Article>> {
        return articleList.map { articles ->
            articles.filter { article ->
                article.tags.any { tag -> tags.contains(tag) }
            }
        }
    }

    // 高级搜索：组合多个条件
    fun advancedSearch(
        query: String? = null,
        authorId: Int? = null,
        tags: List<String>? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<List<Article>> {
        return articleList.map { articles ->
            articles.filter { article ->
                // 关键词搜索
                val matchesQuery = query?.let {
                    article.title?.contains(it, ignoreCase = true) == true ||
                            article.text?.contains(it, ignoreCase = true) == true
                } ?: true

                // 作者筛选
                val matchesAuthor = authorId?.let {
                    article.authorId == it
                } ?: true

                // 标签筛选
                val matchesTags = tags?.let {
                    article.tags.any { tag -> it.contains(tag) }
                } ?: true

                // 日期范围筛选
                val matchesDateRange = if (startDate != null && endDate != null && article.date != null) {
                    article.date!! in startDate..endDate
                } else true

                // 所有条件都满足才返回
                matchesQuery && matchesAuthor && matchesTags && matchesDateRange
            }
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