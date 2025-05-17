package com.example.voyago.model.data

import com.example.voyago.model.domain.Article
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Calendar


@IgnoreExtraProperties
data class ArticleDto(
    val id: Long = 0,
    val title: String = "",
    val text: String = "",
    val authorId: Long = 0,
    val date: Timestamp? = null,
    val photo: String = "",
    val contentUrl: String = "",
    val tags: List<String> = emptyList()
)


// Mapper extension
fun ArticleDto.toDomainModel(): Article {
    val ts = this.date ?: Timestamp.now()
    // 1. 生成 java.util.Date
    val dateObj = ts.toDate()
    // 2. 把它放进 Calendar
    val cal = Calendar.getInstance().apply { time = dateObj }

    return Article(
        id        = this.id.toInt(),
        title     = this.title,
        text      = this.text,
        authorId  = this.authorId.toInt(),
        date      = cal,         // Calendar
        photo     = this.photo,
        contentUrl= this.contentUrl,
        tags      = this.tags
    )
}

// Mappers.kt
fun Article.toDto(): ArticleDto {
    val millis = this.date.timeInMillis
    val ts = Timestamp(millis / 1000, ((millis % 1000) * 1_000_000).toInt())

    return ArticleDto(
        id        = this.id.toLong(),
        title     = this.title,
        text      = this.text,
        authorId  = this.authorId.toLong(),
        date      = ts,
        photo     = this.photo,
        contentUrl= this.contentUrl,
        tags      = this.tags
    )
}


// ArticleRepository.kt
class ArticleRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {

    /** 把一组 Article 写入 Firestore 的 "articles" 集合 */
    suspend fun saveAll(articles: List<Article>) {
        val col = firestore.collection("articles")
        // 并发写法（可选）
        coroutineScope {
            articles.map { article ->
                async {
                    val dto = article.toDto()
                    col.document(dto.id.toString())
                        .set(dto)
                        .await()
                }
            }.awaitAll()
        }
    }
    // 批量读取
    suspend fun loadAllArticles(): List<Article> {
        val snap = Firebase.firestore
            .collection("articles")
            .get()
            .await()
        return snap.documents.mapNotNull { it.toObject(ArticleDto::class.java) }
            .map { it.toDomainModel() }
    }
    suspend fun saveArticle(article: Article) {
        val dto = article.toDto()
        Firebase.firestore
            .collection("articles")
            .document(dto.id.toString())
            .set(dto)    // 自动把字段映为 JSON
            .await()
    }

    // 单篇读取
    suspend fun loadArticle(id: Int): Article? {
        val snap = Firebase.firestore
            .collection("articles")
            .document(id.toString())
            .get()
            .await()
        // toObject 拿到 DTO，再转回业务模型
        val dto = snap.toObject(ArticleDto::class.java) ?: return null
        return dto.toDomainModel()
    }

    /** 新增：根据 ID 单篇读取 */
    suspend fun fetchById(id: Int): Article? {
        val doc = firestore.collection("articles")
            .document(id.toString())
            .get()
            .await()
        // 如果文档存在，就 toObject→toDomainModel，否则返回 null
        return doc.toObject(ArticleDto::class.java)
            ?.toDomainModel()
    }

    /** 持续地把本地 + 远端的最新文章列表当作 Flow 暴露 */
    fun streamAllArticles(): Flow<List<Article>> = flow {
        // 先发射本地缓存（如果有），然后再去拉远端、写缓存、再发射
        emit(loadAllArticles())          // 简单示例：先发一下
        saveAll(sampleArticles())        // 如果你想在空时写入
        emit(loadAllArticles())          // 再发一次「最新」
    }
}

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
        val cal = Calendar.getInstance().apply {
            set(2025, Calendar.MAY, 1 + index)
            set(Calendar.HOUR_OF_DAY, 9 + (index % 3)*3)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        Article(
            id = index + 1,
            title = titles[index],
            text = descriptions[index],
            photo = imageNames[index],
            authorId = index+1,
            date = cal,
            contentUrl = "https://example.com/articles/${titles[index].lowercase().replace("""[^a-z0-9]+""".toRegex(), "-")}",
            tags = tagsList[index]
        )
    }
}
