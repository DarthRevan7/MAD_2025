package com.example.voyago.view
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.voyago.model.Trip
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.voyago.viewmodel.ArticleViewModel
import com.example.voyago.viewmodel.TripViewModel
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.filter

@Composable
fun HomePageScreen(
    navController: NavHostController,   // 如果你需要从首页再导航出去
    vm1: TripViewModel,                  // 或者别的 ViewModel
    onTripClick: (Trip) -> Unit = {} ,
    vm2: ArticleViewModel,
) {


//    val tripLists by vm1.tripList.collectAsState()
    val articles by vm2.articleList.collectAsState(initial = emptyList() )
    var displayCount by remember { mutableIntStateOf(5) }
    val scrollState = rememberScrollState()
    // 1. 先拿到“现在”的时间点
    val now = remember { Calendar.getInstance() }
    // 2. 按条件分组
    vm1.resetFilters()

    val completedTrips = vm1.getCompletedTripsList().collectAsState(initial = emptyList()).value.filter { trip -> trip.published }

    val upcomingTrips = vm1.getUpcomingTripsList().collectAsState(initial = emptyList()).value.filter { trip -> trip.published }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(   start =22.dp,
                top = 21.dp,
                end = 24.dp,
                bottom = 12.dp
            ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        SectionTag(text = "Popular Trips",  modifier = Modifier
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
        SectionTag(text = "Popular Completed Trips",  modifier = Modifier
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
        SectionTag(text = "Article",  modifier = Modifier
            .width(82.dp)
            .height(32.dp)

        )
        val toDisplay = articles.take(displayCount)

        toDisplay.forEach { article ->
            ArticleShow(
                imageUrl    = article.photo,
                title       = article.title,
                description = article.text,        // 或者拼成 “Discover • 3 days” 之类
                modifier    = Modifier
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
    val pagerState = rememberPagerState{popularTrips.size}
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




@SuppressLint("DiscouragedApi")
@Composable
private fun TripCard(

    proposal: Trip,
    modifier: Modifier = Modifier,         // ← 默认值

) {
    val context = LocalContext.current
    val resId = remember(proposal.photo) {
        context.resources.getIdentifier(proposal.photo, "drawable", context.packageName)
    }
    Box(
        modifier = Modifier
            .size(width = 280.dp, height = 160.dp)
            .clip(RoundedCornerShape(16.dp))

    ) {
        // 1. 背景图层
        if (proposal.photo.isNotEmpty()) {
            AsyncImage(
                model = resId,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            // 占位色
            Box(Modifier.matchParentSize().background(Color.LightGray))
        }

        // 2. 底部渐变叠加
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

        // 3. 文本层：标题 + 副标题
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
                text = proposal.title,  // 或者自定义“Discover • 3 days”那种格式
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                maxLines = 1
            )
        }
    }
}


@SuppressLint("DiscouragedApi")
@Composable
fun ArticleShow(
    imageUrl: String?,
    title: String?,
    description: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resId = remember(imageUrl) {
        context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = resId,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title ?: "No Title", // 处理 null,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description ?: "No Description", // 处理 null
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}