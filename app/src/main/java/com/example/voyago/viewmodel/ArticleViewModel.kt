package com.example.voyago.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.voyago.model.Article
import com.example.voyago.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class ArticleViewModel(private val model: TheArticlesModel) : ViewModel() {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        Log.d("ArticleViewModel", "🔥 ArticleViewModel initialized")
    }

    val articleList = model.getArticles()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        .also { stateFlow ->
            // 🔥 添加调试，监听 articleList 的变化
            viewModelScope.launch {
                stateFlow.collect { articles ->
                    Log.d("ArticleViewModel", "🔥 ArticleList updated: ${articles.size} articles")
                    articles.forEachIndexed { index, article ->
                        Log.d("ArticleViewModel", "🔥 Article $index: ${article.title}")
                    }
                }
            }
        }

    // 搜索关键词的状态
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // 搜索结果 - 结合文章列表和搜索关键词
    val searchResults: Flow<List<Article>> = combine(
        articleList,
        searchQuery
    ) { articles, query ->
        Log.d("ArticleViewModel", "🔥 Search filtering: ${articles.size} articles, query='$query'")
        if (query.isEmpty()) {
            articles
        } else {
            articles.filter { article ->
                article.title?.contains(query, ignoreCase = true) == true ||
                        article.text?.contains(query, ignoreCase = true) == true ||
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

    fun getArticleById(articleId: Int): Article? {
        return articleList.value.firstOrNull { it.id == articleId }
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
            Log.d("ArticleViewModel", "🔥 Getting recent articles: ${articles.size} total, limit=$limit")
            articles
                .sortedByDescending { it.date }
                .take(limit)
        }
    }

    // 获取热门文章（可以根据实际需求定义"热门"的逻辑）
    fun getPopularArticles(limit: Int = 5): Flow<List<Article>> {
        return articleList.map { articles ->
            // 按浏览次数降序排序，如果浏览次数相同则按日期排序
            articles
                .sortedWith(
                    compareByDescending<Article> { it.viewCount }
                        .thenByDescending { it.date }
                )
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

    // 🔥 按顺序添加文章：使用连续的数字 ID
    suspend fun publishArticle(article: Article):Article {
        try {
            val db = FirebaseFirestore.getInstance()
            val articlesCollection = db.collection("articles")

            // 🔥 方法1：找到当前最大的数字 ID，然后 +1
            val nextId = findNextAvailableId(articlesCollection)

            val articleWithId = article.copy(id = nextId)

            // 🔥 使用数字 ID 作为文档 ID
            articlesCollection.document(nextId.toString()).set(articleWithId).await()

            // 强制刷新
            viewModelScope.launch {
                delay(500)
                try {
                    model.forceRefresh()
                } catch (e: Exception) {
                    // 忽略刷新错误
                }
            }

            return articleWithId

        } catch (e: Exception) {
            throw e
        }
    }

    // 🔥 辅助方法：找到下一个可用的 ID
    private suspend fun findNextAvailableId(articlesCollection: CollectionReference): Int {
        return try {
            // 获取所有文档
            val snapshot = articlesCollection.get().await()

            // 找到所有数字文档 ID 中的最大值
            val maxNumericId = snapshot.documents
                .mapNotNull { doc ->
                    // 尝试将文档 ID 转换为数字
                    doc.id.toIntOrNull()
                }
                .maxOrNull() ?: 0

            // 返回最大 ID + 1
            maxNumericId + 1

        } catch (e: Exception) {
            // 如果出错，默认从 1 开始
            1
        }
    }
    // 🔥 新增：手动强制刷新方法
    fun forceRefreshArticles() {
        viewModelScope.launch {
            try {
                model.forceRefresh()
            } catch (e: Exception) {
                // 处理刷新错误
            }
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