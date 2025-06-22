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

@Composable
fun HomePageScreen(
    navController: NavHostController,
    vm1: TripViewModel,
    vm2: ArticleViewModel,
    onTripClick: (Trip) -> Unit = {}
) {

    // Collect the latest list of articles from the article view model as state
    val articles by vm2.articleList.collectAsState()

    // Number of articles currently visible on the screen (starts at 5)
    var displayCount by remember { mutableIntStateOf(5) }

    // Keeps track of the scroll position for the entire page
    val scrollState = rememberScrollState()

    // Reset any filters applied to trip data
    vm1.resetFilters()

    // Get list of completed trips (from a Flow), and filter to only published ones
    val completedTrips = vm1.getCompletedTripsList()
        .collectAsState(initial = emptyList()).value.filter { trip -> trip.published }

    // Get list of upcoming trips (from a Flow), and filter to only published ones
    val upcomingTrips = vm1.getUpcomingTripsList()
        .collectAsState(initial = emptyList()).value.filter { trip -> trip.published }

    // Column is the root container for the homepage; vertical layout with padding and scroll
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
        verticalArrangement = Arrangement.spacedBy(24.dp)   // Spacing between child elements
    ) {

        // Section upcoming trips
        SectionTag(
            text = "Popular Trips", modifier = Modifier
                .width(117.dp)
                .height(32.dp)

        )

        // Horizontally scrollable list of popular upcoming trips
        PopularTravel(
            popularTrips = upcomingTrips,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),

            onTripClick = { trip ->
                vm1.setSelectedTrip(trip)   // Store selected trip in ViewModel
                vm1.userAction = TripViewModel.UserAction.VIEW_TRIP     // Set action type
                navController.navigate("trip_details")      // Navigate to trip details screen
            }
        )

        //Completed trips section
        SectionTag(
            text = "Popular Completed Trips", modifier = Modifier
                .width(190.dp)
                .height(32.dp)

        )

        // Horizontally scrollable list of popular completed trips
        PopularTravel(
            popularTrips = completedTrips,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),

            onTripClick = { trip ->
                vm1.setSelectedTrip(trip)   // Store selected trip
                navController.navigate("trip_details")  // Navigate to trip detail page
            }
        )

        //Article section
        SectionTag(
            text = "Article", modifier = Modifier
                .width(82.dp)
                .height(32.dp)
        )

        // Determine which articles to show (based on display count)
        val toDisplay = articles.take(displayCount)

        // Loop through and show each article using the ArticleShow composable
        toDisplay.forEach { article ->
            ArticleShow(
                article = article,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        // Navigate to article detail page when clicked
                        navController.navigate("article_detail/${article.id}")
                    }
            )
        }

        //Load More Button for Articles
        if (displayCount < articles.size) {
            // Show a clickable "Show more…" if there are more articles to display
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
                            // Load 5 more articles, but not more than available
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
    contentAlignment: Alignment = Alignment.Center,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    borderColor: Color = Color.Black,
    borderWidth: Dp = 1.dp,
    textColor: Color = Color.Black,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Black,
    fontFamily: FontFamily = FontFamily.SansSerif,
    textAlign: TextAlign = TextAlign.Center
) {
    // A Box layout is used here to encapsulate the styling and alignment logic
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(borderWidth, borderColor, shape)
            .background(backgroundColor, shape),
        contentAlignment = contentAlignment
    ) {
        // The actual Text displayed in the tag
        Text(
            text = text,
            color = textColor,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
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

    // Create and remember the pager state for the horizontal pager
    // The pager will have as many pages as there are trips
    val pagerState = rememberPagerState { popularTrips.size }

    // HorizontalPager is used to create a horizontally scrolling carousel of trip cards
    HorizontalPager(
        state = pagerState,     // Connects the pager state to control and observe scrolling
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        contentPadding = PaddingValues(
            start = 0.dp,
            end = 64.dp
        ),
        pageSpacing = 8.dp      // Space between each card (visually separates items)
    ) { page ->
        // Get the corresponding trip for this page index
        val proposal = popularTrips[page]

        // Each page displays a Box that holds a clickable TripCard
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium)      // Applies medium shape clipping
                .clickable { onTripClick(proposal) }

        ) {
            // Custom composable that displays the trip's information
            TripCard(
                proposal = proposal
            )
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@SuppressLint("DiscouragedApi")
@Composable
private fun TripCard(
    proposal: Trip      // Represents a single trip proposal containing destination, title, and photo info
) {
    // Mutable state to hold the URL of the image to be displayed
    var imageUrl by remember { mutableStateOf<String?>(null) }

    // Side effect to load the image URL when the composable enters the composition or the photo changes
    LaunchedEffect(proposal.photo) {
        // Suspends if needed and fetches the image URL from the Trip model
        imageUrl = proposal.getPhoto()
    }

    // Root container for the trip card
    Box(
        modifier = Modifier
            .size(width = 280.dp, height = 160.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Loads and displays the trip image using Glide
        GlideImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // Overlay with vertical gradient to improve text readability over the image
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent, // Transparent at the top
                            Color.Black.copy(alpha = 0.6f)  // Dark fade at the bottom
                        ),
                        startY = 80f        // Start the gradient fade from halfway down
                    )
                )
        )

        // Text content shown at the bottom-left of the card
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            // Destination name
            Text(
                text = proposal.destination,
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Short trip title
            Text(
                text = proposal.title,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                maxLines = 1
            )
        }
    }
}

// Display an article
@SuppressLint("DiscouragedApi")
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArticleShow(
    article: Article,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    // A state variable to store the image URL from Firebase or any remote source
    var imageUrl by remember { mutableStateOf<String?>(null) }

    // Launch side-effect to asynchronously fetch the image URL whenever the article's photo field changes
    LaunchedEffect(article.photo) {
        // Fetch image
        imageUrl = article.getPhoto()
    }

    // Main container laid out horizontally: image on the left, text on the right
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }    // Add clickable modifier if an onClick is provided
                } else {
                    Modifier    // Otherwise, do nothing
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // IMAGE DISPLAY SECTION
        when {
            imageUrl != null -> {
                // If image URL was successfully fetched, load it using GlideImage
                GlideImage(
                    model = imageUrl,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            article.photo.isNotEmpty() -> {
                // If imageUrl is null but article.photo has a local image name, try loading from drawable
                val context = LocalContext.current
                val resId = remember(article.photo) {
                    context.resources.getIdentifier(
                        article.photo.toString(),   // Name of the local drawable resource
                        "drawable",
                        context.packageName
                    )
                }

                if (resId != 0) {
                    // If valid resource ID is found, load it using AsyncImage
                    AsyncImage(
                        model = resId,
                        contentDescription = article.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    // If the drawable resource does not exist, show a placeholder
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

            else -> {
                // Fallback if no image URL or local photo is available
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

        // Spacing between image and text
        Spacer(modifier = Modifier.width(16.dp))

        // TEXT CONTENT SECTION
        Column(modifier = Modifier.weight(1f)) {
            // Article title
            Text(
                text = article.title ?: "No Title", // Fallback if title is null
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,   // Limit to 2 lines
                overflow = TextOverflow.Ellipsis    // Truncate with ellipsis if too long
            )

            Spacer(Modifier.height(4.dp))

            // Article body preview/description
            Text(
                text = article.text ?: "No Description",    // Fallback if body text is null
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5,   // Show only the first 5 lines
                overflow = TextOverflow.Ellipsis    // Ellipsis for overflow
            )
        }
    }
}