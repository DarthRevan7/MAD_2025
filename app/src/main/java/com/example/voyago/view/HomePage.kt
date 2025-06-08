package com.example.voyago.view

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.voyago.model.Article
import com.example.voyago.model.Trip
import com.example.voyago.viewmodel.ArticleViewModel
import com.example.voyago.viewmodel.TripViewModel
import java.util.Calendar

@Composable
fun HomePageScreen(
    navController: NavHostController,   // 如果你需要从首页再导航出去
    vm1: TripViewModel,                  // 或者别的 ViewModel
    onTripClick: (Trip) -> Unit = {},
    vm2: ArticleViewModel,
) {


//    val tripLists by vm1.tripList.collectAsState()
    val articles by vm2.articleList.collectAsState(initial = emptyList())
    var displayCount by remember { mutableIntStateOf(5) }
    val scrollState = rememberScrollState()
    // 1. 先拿到"现在"的时间点
    val now = remember { Calendar.getInstance() }
    // 2. 按条件分组
    vm1.resetFilters()

    val completedTrips = vm1.getCompletedTripsList()
        .collectAsState(initial = emptyList()).value.filter { trip -> trip.published }

    val upcomingTrips = vm1.getUpcomingTripsList()
        .collectAsState(initial = emptyList()).value.filter { trip -> trip.published }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(
                start = 22.dp,
                top = 21.dp,
                end = 24.dp,
                bottom = 12.dp
            ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        SectionTag(
            text = "Popular Trips", modifier = Modifier
                .width(117.dp)
                .height(32.dp)

        )

        PopularTravel(
            popularTrips = upcomingTrips,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),

            onTripClick = { trip ->
                vm1.setSelectedTrip(trip)
                vm1.userAction = TripViewModel.UserAction.VIEW_TRIP
                navController.navigate("trip_details")
            }
        )
        SectionTag(
            text = "Popular Completed Trips", modifier = Modifier
                .width(190.dp)
                .height(32.dp)

        )

        PopularTravel(
            popularTrips = completedTrips,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),

            onTripClick = { trip ->
                vm1.setSelectedTrip(trip)
                navController.navigate("trip_details")
            }
        )
        // 在 Column 中的 Article 部分，替换为：
        SectionTag(
            text = "Article", modifier = Modifier
                .width(82.dp)
                .height(32.dp)
        )

        val toDisplay = articles.take(displayCount)

       // 🔄 修改这部分：传递整个 article 对象
        toDisplay.forEach { article ->
            ArticleShow(
                article = article,  // 传递整个 article 对象
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        if (displayCount < articles.size) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Show more…",
                    color = Color(0xFF377FFF),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clickable {
                            // 每次增加5条，最多到文章总数
                            displayCount = (displayCount + 5).coerceAtMost(articles.size)
                        }
                        .padding(4.dp)
                )
            }
        }


    }
}


@Composable
fun SectionTag(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFCCC2DC),
    contentAlignment: Alignment = Alignment.Center,    // 直接全中
    paddingValues: PaddingValues = PaddingValues(horizontal = 22.dp, vertical = 0.dp),
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    borderColor: Color = Color.Black,
    borderWidth: Dp = 1.dp,
    textColor: Color = Color.Black,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Black,
    fontFamily: FontFamily = FontFamily.SansSerif,
    textAlign: TextAlign = TextAlign.Center          // 新增：文字对齐
) {
    Box(
        modifier = modifier
            .fillMaxWidth()                            // 宽度撑满
            .clip(shape)
            .border(borderWidth, borderColor, shape)
            .background(backgroundColor, shape),
        contentAlignment = contentAlignment            // Box 居中子元素
    ) {
        Text(
            text = text,
            color = textColor,
            textAlign = textAlign,                     // 应用对齐
            modifier = Modifier.fillMaxWidth(),        // 让 Text 自身也撑满，这样 textAlign 生效
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = fontWeight,
                fontFamily = fontFamily
            )
        )
    }
}

@Composable
fun PopularTravel(
    popularTrips: List<Trip>,
    modifier: Modifier = Modifier,
    onTripClick: (Trip) -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState { popularTrips.size }
    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        contentPadding = PaddingValues(
            start = 0.dp,
            end = 64.dp
        ), // 左右各留 32dp
        pageSpacing = 8.dp                                   // 卡片间距
    ) { page ->
        val proposal = popularTrips[page]
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium)
                .clickable { onTripClick(proposal) }

        ) {
            // 你自己的卡片实现，比如：
            TripCard(
                proposal = proposal,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@SuppressLint("DiscouragedApi")
@Composable
private fun TripCard(
    proposal: Trip,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var imageUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(proposal.photo) {
        imageUrl = proposal.getPhoto()
    }
    Box(
        modifier = Modifier
            .size(width = 280.dp, height = 160.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        GlideImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                        startY = 80f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = proposal.destination,
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = proposal.title,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                maxLines = 1
            )
        }
    }
}


// 修改 HomePage.kt 中的 ArticleShow 组件：

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArticleShow(
    article: Article,  // 🔄 改为接收整个 Article 对象而不是单独的字段
    modifier: Modifier = Modifier
) {
    var imageUrl by remember { mutableStateOf<String?>(null) }

    // 🔄 使用 LaunchedEffect 异步获取 Firebase Storage URL
    LaunchedEffect(article.photo) {
        imageUrl = article.getPhoto()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 🔄 使用 GlideImage 和 Firebase Storage URL
        when {
            imageUrl != null -> {
                GlideImage(
                    model = imageUrl,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            // 备用方案：如果 imageUrl 为空，尝试本地资源
            !article.photo.isNullOrEmpty() -> {
                val context = LocalContext.current
                val resId = remember(article.photo) {
                    context.resources.getIdentifier(article.photo, "drawable", context.packageName)
                }

                if (resId != 0) {
                    AsyncImage(
                        model = resId,
                        contentDescription = article.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    // 占位图
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Image", color = Color.Gray)
                    }
                }
            }
            // 默认占位图
            else -> {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = article.title ?: "No Title",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = article.text ?: "No Description",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}