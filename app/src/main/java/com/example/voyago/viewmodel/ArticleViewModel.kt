package com.example.voyago.viewmodel

import androidx.lifecycle.ViewModel
import com.example.voyago.model.Article
import com.example.voyago.model.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.util.Calendar


fun sampleArticles(): List<Article> {
    val titles = listOf(
        "The Hidden Gems of Kyoto: Beyond the Tourist Trail",
        "5 Must-Visit Cafés in Paris",
        "A Foodie's Guide to Bangkok Street Eats",
        "Discovering the Fjords of Norway by Boat",
        "Exploring the Ancient Ruins of Petra",
        "Safari Adventures in the Serengeti",
        "Road Tripping the Pacific Coast Highway",
        "A Weekend in New York City: Top 10 Spots",
        "The Ultimate Guide to Iceland’s Waterfalls",
        "Wine Tasting Tours in Tuscany"
    )
    val descriptions = listOf(
        "Discover Kyoto’s best-kept secrets, from hidden temples to historic noodle shops. Escape the crowds and explore the city like a local!",
        "From the Left Bank to the Right Bank, these five cafés are sure to make your trip to Paris more stylish.",
        "Dive into the vibrant street food scene of Bangkok—pad thai, mango sticky rice, and more await!",
        "Experience Norway’s dramatic fjords on a scenic boat tour, complete with waterfalls and mountain views.",
        "Walk through the rose-red city carved into stone and learn the history of this archaeological wonder.",
        "Join us for an unforgettable journey across the plains of the Serengeti, home to the Big Five.",
        "Hit the open road along California’s stunning coastline, with stops in Malibu, Big Sur, and beyond.",
        "Pack your bags for a whirlwind weekend in NYC—Central Park, Times Square, Brooklyn Bridge and more.",
        "Chase the thundering cascades of Gullfoss, Seljalandsfoss, and others in stunning Iceland.",
        "Sip your way through rolling hills and vineyards in Tuscany’s most renowned wine regions."
    )
    val imageNames = listOf(
        "kyoto", "paris", "bali", "brazil", "dubai",
        "sydney", "rio", "colorado", "fiji", "japan"
    )
    val authors = listOf(
        "Jane Doe", "Jean Dupont", "Somchai Prasert", "Håkon Larsen",
        "Amira Khalil", "David Smith", "Emily Chen", "Luca Rossi",
        "Ólafur Jónsson", "Giulia Bianchi"
    )
    val tagsList = listOf(
        listOf("Travel", "Culture", "Japan"),
        listOf("Food", "Paris", "Cafés"),
        listOf("Food", "Thailand", "Street Food"),
        listOf("Nature", "Norway", "Cruise"),
        listOf("History", "Jordan", "Archaeology"),
        listOf("Wildlife", "Africa", "Safari"),
        listOf("Road Trip", "USA", "Scenic"),
        listOf("City", "USA", "Weekend"),
        listOf("Nature", "Iceland", "Waterfalls"),
        listOf("Wine", "Italy", "Tuscany")
    )

    return List(10) { index ->
        val calendar = Calendar.getInstance().apply {
            set(2025, Calendar.MAY, 1 + index, 9 + (index % 3) * 3, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        Article(
            id = index + 1,
            title = titles[index],
            text = descriptions[index],
            photo = imageNames[index],
            authorId = authors[index],
            date = calendar,
            contentUrl = "https://example.com/articles/${titles[index].lowercase().replace("""[^a-z0-9]+""".toRegex(), "-")}",
            tags = tagsList[index]
        )
    }
}




class ArticleViewModel : ViewModel() {


    private val _articleList = MutableStateFlow<List<Article>>(emptyList())
    val articleList: StateFlow<List<Article>> = _articleList

    // Get articles list of a user
//    fun getUserArticles(id: Int): List<Article> {
//        return _articleList.value.filter { it.authorId == id }
//    }

    init {
        _articleList.value = sampleArticles()
    }
}

