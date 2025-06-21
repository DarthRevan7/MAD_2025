package com.example.voyago.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.model.Article
import com.example.voyago.model.User
import com.example.voyago.viewmodel.ArticleViewModel
import com.example.voyago.viewmodel.UserViewModel

@Composable
fun ArticleSearchScreen(
    navController: NavController,
    articleViewModel: ArticleViewModel,
    userViewModel: UserViewModel
) {
    // 获取搜索相关的状态
    val searchQuery by articleViewModel.searchQuery.collectAsState()
    val searchResults by articleViewModel.searchResults.collectAsState(initial = emptyList())

    // 🔥 获取所有文章来进行分类
    val allArticles by articleViewModel.articleList.collectAsState()

    // 🔥 分类文章：最热门的3篇 vs 其余文章
    val mostPopularArticles = allArticles
        .sortedWith(
            compareByDescending<Article> { it.viewCount }
                .thenByDescending { it.date }
        )
        .take(3) // 🔥 取前3篇最热门的文章

    val recommendedArticles = allArticles
        .sortedWith(
            compareByDescending<Article> { it.viewCount }
                .thenByDescending { it.date }
        )
        .drop(3) // 🔥 跳过最热门的前3篇，显示其余文章

    // 🔥 推荐文章的显示状态
    var showAllRecommended by remember { mutableStateOf(false) }

    // 决定显示哪些文章
    val articlesToShow = when {
        searchQuery.isNotEmpty() -> searchResults
        showAllRecommended -> recommendedArticles
        else -> recommendedArticles.take(5) // 默认显示前5篇
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("create_article")
                },
                containerColor = Color(0xFF2E2E2E),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F5F5))
        ) {
            // Search Section
            SearchSection(
                searchQuery = searchQuery,
                onSearchQueryChange = { articleViewModel.updateSearchQuery(it) },
                onClearSearch = {
                    articleViewModel.clearSearch()
                    showAllRecommended = false // 重置显示状态
                }
            )

            if (searchQuery.isNotEmpty() && searchResults.isEmpty()) {
                // 显示无结果提示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No articles found for \"$searchQuery\"",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                // 🔥 Most Popular Articles Section - 显示3篇最热门的
                if (mostPopularArticles.isNotEmpty() && searchQuery.isEmpty()) {
                    MostPopularSection(
                        articles = mostPopularArticles, // 传递3篇文章
                        onArticleClick = { selectedArticle ->
                            navController.navigate("article_detail/${selectedArticle.id}")
                        },
                        userViewModel = userViewModel,
                        searchQuery = searchQuery
                    )
                }

                // 🔥 Recommended Section - 显示其余文章
                if (articlesToShow.isNotEmpty()) {
                    val sectionTitle = if (searchQuery.isNotEmpty()) {
                        "Search Results (${searchResults.size})"
                    } else {
                        "Recommended For You"
                    }

                    RecommendedSection(
                        title = sectionTitle,
                        articles = articlesToShow,
                        onArticleClick = { selectedArticle ->
                            navController.navigate("article_detail/${selectedArticle.id}")
                        },
                        onViewAllClick = {
                            // 🔥 切换显示全部推荐文章
                            if (!showAllRecommended) {
                                showAllRecommended = true
                            }
                        },
                        showViewAll = searchQuery.isEmpty() && !showAllRecommended && recommendedArticles.size > 5,
                        userViewModel = userViewModel,
                        searchQuery = searchQuery
                    )
                }
            }
        }
    }
}


// 高亮文本组件
@Composable
fun HighlightedText(
    text: String,
    searchQuery: String,
    fontSize: TextUnit,
    fontWeight: FontWeight? = null,
    color: Color = Color.Black,
    maxLines: Int = Int.MAX_VALUE
) {
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val lowerText = text.lowercase()
        val lowerQuery = searchQuery.lowercase()

        while (currentIndex < text.length) {
            val index = lowerText.indexOf(lowerQuery, currentIndex)
            if (index >= currentIndex) {
                // 添加匹配前的文本
                if (index > currentIndex) {
                    withStyle(SpanStyle(color = color, fontWeight = fontWeight)) {
                        append(text.substring(currentIndex, index))
                    }
                }
                // 添加高亮的匹配文本
                withStyle(
                    SpanStyle(
                        color = Color(0xFF2196F3), // 蓝色
                        fontWeight = FontWeight.Bold,
                        background = Color(0x332196F3) // 淡蓝色背景
                    )
                ) {
                    append(text.substring(index, index + searchQuery.length))
                }
                currentIndex = index + searchQuery.length
            } else {
                // 添加剩余的文本
                withStyle(SpanStyle(color = color, fontWeight = fontWeight)) {
                    append(text.substring(currentIndex))
                }
                break
            }
        }
    }

    Text(
        text = annotatedString,
        fontSize = fontSize,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun SearchSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFD8C7E8))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Article Research",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            // Search Field
            Surface(
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                if (isSearchActive) {
                    // 激活状态 - 显示输入框
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = { Text("Search articles...", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    onClearSearch()
                                    isSearchActive = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                } else {
                    // 非激活状态 - 显示占位符
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { isSearchActive = true }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Search here",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MostPopularSection(
    articles: List<Article>, // 🔥 改为接收文章列表（3篇）
    onArticleClick: (Article) -> Unit,
    userViewModel: UserViewModel,
    searchQuery: String = ""
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 🔥 标题改为复数形式
            Text(
                text = "Most Popular Articles", // 🔥 复数形式
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔥 显示3篇热门文章
            articles.forEachIndexed { index, article ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                ArticleSearchItem(
                    article = article,
                    onClick = { onArticleClick(article) },
                    userViewModel = userViewModel,
                    searchQuery = searchQuery
                )
            }

            // 🔥 如果文章不足3篇，显示提示
            if (articles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No popular articles available",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendedSection(
    title: String = "Recommended For You",
    articles: List<Article>,
    onArticleClick: (Article) -> Unit,
    onViewAllClick: () -> Unit,
    showViewAll: Boolean = true, // 🔥 控制是否显示 View All 按钮
    userViewModel: UserViewModel,
    searchQuery: String = ""
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // 🔥 只在特定条件下显示 View All 按钮
                if (showViewAll) {
                    TextButton(onClick = onViewAllClick) {
                        Text(
                            text = "View All",
                            color = Color(0xFF6B5B95)
                        )
                    }
                }
            }

            // 🔥 显示文章列表
            articles.forEachIndexed { index, article ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                ArticleSearchItem(
                    article = article,
                    onClick = { onArticleClick(article) },
                    userViewModel = userViewModel,
                    searchQuery = searchQuery
                )
            }

            // 🔥 如果没有文章，显示占位文本
            if (articles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) {
                            "No articles found for \"$searchQuery\""
                        } else {
                            "No recommended articles available"
                        },
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArticleSearchItem(
    article: Article,
    onClick: () -> Unit,
    userViewModel: UserViewModel,  // 添加 UserViewModel 参数
    searchQuery: String = ""  // 添加搜索关键词参数
) {
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var author by remember { mutableStateOf<User?>(null) }

    // 异步获取图片 URL 和作者信息
    LaunchedEffect(article.photo, article.authorId) {
        imageUrl = article.getPhoto()

        // 获取作者信息
        article.authorId?.let { authorId ->
            author = getUserFromFirestore(authorId)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        // Image
        when {
            imageUrl != null -> {
                GlideImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            else -> {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Author info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author avatar with ProfilePhoto
                author?.let { user ->
                    ProfilePhoto(
                        user = user,
                        small = true,
                        modifier = Modifier.size(24.dp)
                    )
                } ?: Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "?",
                        color = Color(0xFF6B5B95),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = author?.let { "${it.firstname} ${it.surname}" } ?: "Loading...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Travel Writer", // 简化为固定文本
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title with highlighted search text
            if (searchQuery.isNotEmpty() && (article.title?.contains(
                    searchQuery,
                    ignoreCase = true
                ) == true)
            ) {
                HighlightedText(
                    text = article.title ?: "Untitled",
                    searchQuery = searchQuery,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
            } else {
                Text(
                    text = article.title ?: "Untitled",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Description with highlighted search text
            if (searchQuery.isNotEmpty() && (article.text?.contains(
                    searchQuery,
                    ignoreCase = true
                ) == true)
            ) {
                HighlightedText(
                    text = article.text ?: "No description available",
                    searchQuery = searchQuery,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 3
                )
            } else {
                Text(
                    text = article.text ?: "No description available",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }

            // Tags
            if (article.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    article.tags.take(3).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE8E8E8)
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            }
        }
    }
}