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

@Composable
fun ArticleSearchScreen(
    navController: NavController,
    articleViewModel: ArticleViewModel
) {
    // Observe the current search query and search results from the ViewModel
    val searchQuery by articleViewModel.searchQuery.collectAsState()
    val searchResults by articleViewModel.searchResults.collectAsState(initial = emptyList())

    // Get the full list of articles to be used for categorization
    val allArticles by articleViewModel.articleList.collectAsState()

    // Determine the 3 most popular articles by views (then fallback to recent ones)
    val mostPopularArticles = allArticles
        .sortedWith(
            compareByDescending<Article> { it.viewCount }
                .thenByDescending { it.date }
        )
        .take(3)

    // Remaining articles to recommend after removing the top 3
    val recommendedArticles = allArticles
        .sortedWith(
            compareByDescending<Article> { it.viewCount }
                .thenByDescending { it.date }
        )
        .drop(3) // 🔥 跳过最热门的前3篇，显示其余文章

    // Track whether to show all recommended articles or just a subset
    var showAllRecommended by remember { mutableStateOf(false) }

    // Decide what articles to display based on search state and toggle
    val articlesToShow = when {
        searchQuery.isNotEmpty() -> searchResults   // Show search results
        showAllRecommended -> recommendedArticles   // Show all recommendations
        else -> recommendedArticles.take(5)         // Default: show top 5
    }

    // Screen layout with floating action button to create new article
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navigate to create screen
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
            // Search bar UI
            SearchSection(
                searchQuery = searchQuery,
                onSearchQueryChange = { articleViewModel.updateSearchQuery(it) },
                onClearSearch = {
                    articleViewModel.clearSearch()
                    showAllRecommended = false // Reset recommended toggle
                }
            )

            if (searchQuery.isNotEmpty() && searchResults.isEmpty()) {
                // No results message when search yields nothing
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
                // Show "Most Popular" section only if there's no active search
                if (mostPopularArticles.isNotEmpty() && searchQuery.isEmpty()) {
                    MostPopularSection(
                        articles = mostPopularArticles,
                        onArticleClick = { selectedArticle ->
                            navController.navigate("article_detail/${selectedArticle.id}")
                        },
                        searchQuery = searchQuery
                    )
                }

                // Show either search results or recommended articles
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
                            // Reveal full recommended list when "View All" is clicked
                            if (!showAllRecommended) {
                                showAllRecommended = true
                            }
                        },
                        showViewAll = searchQuery.isEmpty() && !showAllRecommended && recommendedArticles.size > 5,
                        searchQuery = searchQuery
                    )
                }
            }
        }
    }
}


// HighlightedText composable: highlights matching query text inside a larger string
@Composable
fun HighlightedText(
    text: String,                       // Full text to display
    searchQuery: String,                // Substring to highlight within the text
    fontSize: TextUnit,                 // Size of the displayed text
    fontWeight: FontWeight? = null,     // Optional font weight for non-highlighted text
    color: Color = Color.Black,         // Color for non-highlighted text
    maxLines: Int = Int.MAX_VALUE       // Max lines to display before truncating
) {
    // Build styled text that includes highlights for matching query
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val lowerText = text.lowercase()
        val lowerQuery = searchQuery.lowercase()

        while (currentIndex < text.length) {
            // Find next match of the search query in the text
            val index = lowerText.indexOf(lowerQuery, currentIndex)

            if (index >= currentIndex) {
                // Add the text before the match in normal style
                if (index > currentIndex) {
                    withStyle(SpanStyle(color = color, fontWeight = fontWeight)) {
                        append(text.substring(currentIndex, index))
                    }
                }

                // Add the matched substring with highlight (blue color, bold, background)
                withStyle(
                    SpanStyle(
                        color = Color(0xFF2196F3),      // Blue text
                        fontWeight = FontWeight.Bold,   // Bold
                        background = Color(0x332196F3)  // Light blue background
                    )
                ) {
                    append(text.substring(index, index + searchQuery.length))
                }
                // Continue searching after this match
                currentIndex = index + searchQuery.length
            } else {
                // Append the remaining text if no more matches are found
                withStyle(SpanStyle(color = color, fontWeight = fontWeight)) {
                    append(text.substring(currentIndex))
                }
                break
            }
        }
    }

    // Render the final styled text in a Text composable
    Text(
        text = annotatedString,
        fontSize = fontSize,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis    // Use ellipsis if content overflows
    )
}

// SearchSection composable: displays a search bar that can toggle between inactive and active states
@Composable
fun SearchSection(
    searchQuery: String,                    // Current search input value
    onSearchQueryChange: (String) -> Unit,  // Callback when user types in the search bar
    onClearSearch: () -> Unit               // Callback when the search is cleared
) {
    // Local state to track if search input is active
    var isSearchActive by remember { mutableStateOf(false) }

    // Background container for the search section
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))    // Rounded corners
            .background(Color(0xFFD8C7E8))      // Light purple background
            .padding(20.dp)                     // Inner padding
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Section title
            Text(
                text = "Article Research",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f)  // Takes up equal space with the search field
            )

            // Search Field (Takes more space than the title)
            Surface(
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White                 // White background for the search bar
            ) {
                if (isSearchActive) {
                    // Active state: show editable text field with icons
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
                            // Show clear icon when query is not empty
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    onClearSearch()
                                    isSearchActive = false  // Reset active state
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
                    // Inactive state: show placeholder-style row
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { isSearchActive = true }    // Activate on click
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

// Displays a styled card containing a list of the top 3 most popular articles
@Composable
fun MostPopularSection(
    articles: List<Article>,            // List of top articles to display (ideally 3)
    onArticleClick: (Article) -> Unit,  // Callback when an article is clicked
    searchQuery: String = ""            // Optional query for highlighting matches
) {
    // Container Card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),  // Outer padding
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Section Title
            Text(
                text = "Most Popular Articles",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Space below title
            Spacer(modifier = Modifier.height(16.dp))

            // Display each article in the list
            articles.forEachIndexed { index, article ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(16.dp))   // Space between items
                }

                // Individual article item with click and optional highlighting
                ArticleSearchItem(
                    article = article,
                    onClick = { onArticleClick(article) },
                    searchQuery = searchQuery
                )
            }

            // Show fallback message if no articles are available
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

// Displays a section of recommended articles, optionally with a "View All" button and search highlights
@Composable
fun RecommendedSection(
    title: String = "Recommended For You",  // Section header text
    articles: List<Article>,                // List of articles to display
    onArticleClick: (Article) -> Unit,      // Callback when an article is clicked
    onViewAllClick: () -> Unit,             // Callback when "View All" is clicked
    showViewAll: Boolean = true,            // Controls visibility of the "View All" button
    searchQuery: String = ""                // Used for keyword highlighting
) {

    // Card container for visual grouping
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            // Title row with optional "View All" button
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Section title
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Show "View All" button if enabled
                if (showViewAll) {
                    TextButton(onClick = onViewAllClick) {
                        Text(
                            text = "View All",
                            color = Color(0xFF6B5B95)   // Soft purple tone
                        )
                    }
                }
            }

            // Display each article with optional highlighting
            articles.forEachIndexed { index, article ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(16.dp))    // Spacing between items
                }
                ArticleSearchItem(
                    article = article,
                    onClick = { onArticleClick(article) },
                    searchQuery = searchQuery
                )
            }

            // Fallback message if article list is empty
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

// Displays a single article row with optional highlighting for search terms, author info, and tags.
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArticleSearchItem(
    article: Article,
    onClick: () -> Unit,
    searchQuery: String = ""  // Optional keyword for highlighting
) {
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var author by remember { mutableStateOf<User?>(null) }

    // Load image and author data asynchronously
    LaunchedEffect(article.photo, article.authorId) {
        imageUrl = article.getPhoto()

        // Load author data from Firestore if available
        article.authorId?.let { authorId ->
            author = getUserFromFirestore(authorId)
        }
    }

    // Layout: Horizontal row with image and content
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // Article Image Section
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
                // Placeholder for missing image
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

        // Textual Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Author info row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author avatar
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

                // Author name and role
                Column {
                    Text(
                        text = author?.let { "${it.firstname} ${it.surname}" } ?: "Loading...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Travel Writer", // Static subtitle for simplicity
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title (highlighted if searchQuery matches)
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

            // Article Summary (with optional highlighting)
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

            // Tags (up to 3 shown)
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