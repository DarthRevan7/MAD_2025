package com.example.voyago.model.data

import com.example.voyago.model.domain.TypeTravel
import com.example.voyago.model.domain.UserData
import java.util.Calendar



import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

@IgnoreExtraProperties
data class UserDto(
    val id: Long = 0L,
    val firstname: String = "",
    val surname: String = "",
    val username: String = "",
    val country: String = "",
    val email: String = "",
    val userDescription: String = "",
    val dateOfBirth: Timestamp? = null,
    val password: String = "",
    val profilePictureUrl: String? = null,
    val typeTravel: List<String> = emptyList(),
    val desiredDestination: List<String> = emptyList(),
    val rating: Double = 0.0,
    val reliability: Long = 0L,
    val publicTrips: List<Long> = emptyList(),              // 存 Trip.id
    val articles: List<Long> = emptyList(),                 // 存 Article.id
    val reviews: List<Long> = emptyList(),                  // 存 Review.reviewId
    val privateTrips: List<Long> = emptyList(),             // 存 Trip.id
    val tripsAppliedTo: List<Long> = emptyList(),           // 存 Trip.id
    val tripsApplicationAccepted: List<Long> = emptyList(), // 存 Trip.id
    val requestedSpots: Long = 0L
)



/** UserData → UserDto */
fun UserData.toDto(): UserDto {
    // 1) Calendar → Timestamp
    val dobTs = Timestamp(dateOfBirth.timeInMillis / 1000, (dateOfBirth.timeInMillis % 1000 * 1_000_000).toInt())

    return UserDto(
        id        = this.id.toLong(),
        firstname = this.firstname,
        surname   = this.surname,
        username  = this.username,
        country   = this.country,
        email     = this.email,
        userDescription = this.userDescription,
        dateOfBirth      = dobTs,
        password         = this.password,
        profilePictureUrl = null,                     // 或者你的 URL 字段
        typeTravel        = this.typeTravel.map { it.name },
        desiredDestination= this.desiredDestination,
        rating            = this.rating.toDouble(),
        reliability       = this.reliability.toLong(),
        publicTrips       = this.publicTrips.map { it.id.toLong() },
        articles          = this.articles.map    { it.id.toLong() },
        reviews           = this.reviews.map     { it.reviewId.toLong() },
        privateTrips      = this.privateTrips.map{ it.id.toLong() },
        tripsAppliedTo    = this.tripsAppliedTo.map{ it.id.toLong() },
        tripsApplicationAccepted = this.tripsApplicationAccepted.map{ it.id.toLong() },
        requestedSpots    = this.requestedSpots.toLong()
    )
}

fun UserDto.toDomainBase(): UserData {
    // Timestamp → Calendar
    val cal = Calendar.getInstance().apply {
        timeInMillis = (this@toDomainBase.dateOfBirth?.seconds ?: 0L) * 1000
    }

    return UserData(
        id            = this.id.toInt(),
        firstname     = this.firstname,
        surname       = this.surname,
        username      = this.username,
        country       = this.country,
        email         = this.email,
        userDescription = this.userDescription,
        dateOfBirth     = cal,
        password        = this.password,
        profilePicture  = null,     // 之后可从 URL 下载
        typeTravel      = this.typeTravel.map { TypeTravel.valueOf(it) },
        desiredDestination = this.desiredDestination,
        rating         = this.rating.toFloat(),
        reliability    = this.reliability.toInt(),
        publicTrips    = emptyList(),  // 先留空
        articles       = emptyList(),  // 先留空
        reviews        = emptyList(),  // 先留空
        privateTrips   = emptyList(),
        tripsAppliedTo = emptyList(),
        tripsApplicationAccepted = emptyList(),
        requestedSpots = this.requestedSpots.toInt()
    )
}


class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val articleRepo: ArticleRepository = ArticleRepository(firestore),
    private val tripRepo: TripRepository = TripRepository(firestore),
    private val reviewRepo: ReviewRepository = ReviewRepository(firestore)
) {
    suspend fun fetchByIds(ids: List<Int>): List<UserData> = coroutineScope {
        // 为每个 id 都启动一个 async，调用 fetchUserFull
        val deferred = ids.map { userId ->
            async { fetchUserFull(userId) }
        }
        // 等待所有结果并过滤掉那些不存在的（null）
        deferred.awaitAll().filterNotNull()
    }

    /** 读取一个完整的 UserData，包括关联的 Article/Trip/Review 列表 */
    suspend fun fetchUserFull(id: Int): UserData? = coroutineScope {
        // 1) 先拿到 DTO
        val snap = firestore.collection("users")
            .document(id.toString())
            .get()
            .await()
        val dto = snap.toObject(UserDto::class.java) ?: return@coroutineScope null

        // 2) 基础映射
        val base = dto.toDomainBase()

        // 3) 并发去拉每一个关联对象
        //    Article
        val articlesDeferred = dto.articles.map { artId ->
            async { articleRepo.fetchById(artId.toInt()) }
        }
        //    Trip
        val publicTripsDeferred = dto.publicTrips.map { tripId ->
            async { tripRepo.fetchById(tripId.toInt()) }
        }
        //    Review
        val reviewsDeferred = dto.reviews.map { revId ->
            async { reviewRepo.fetchById(revId.toInt()) }
        }
        //    （其他列表同理……）

        // 4) 等待结果、过滤 null
        val articles = articlesDeferred.awaitAll().filterNotNull()
        val publicTrips = publicTripsDeferred.awaitAll().filterNotNull()
        val reviews = reviewsDeferred.awaitAll().filterNotNull()

        // 5) 返回一个 copy 了关联字段的完整 UserData
        base.copy(
            publicTrips    = publicTrips,
            articles       = articles,
            reviews        = reviews,
            // privateTrips… tripsAppliedTo… tripsApplicationAccepted… 同理
        )
    }
}

fun sampleUserData(): List<UserData> {
     return listOf(
         UserData(
             id = 1,
             firstname = "Alice",
             surname = "Walker",
             username = "alice_w",
             dateOfBirth = Calendar.getInstance().apply { set(1995, 6, 10); stripTime() },
             country = "USA",
             email = "alice@example.com",
             password = "securePassword123",
             userDescription = "hi",
             profilePicture = null,
             typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.ADVENTURE),
             desiredDestination = listOf("Greece", "Italy", "Japan"),
             rating = 4.7f,
             reliability = 90,
             publicTrips = emptyList(),
             articles = emptyList(),
             reviews = emptyList(),
             privateTrips = emptyList(),
             tripsAppliedTo = emptyList(),
             tripsApplicationAccepted = emptyList(),
             requestedSpots = 1
         ),

         UserData(
             id = 2,
             firstname = "Bella",
             surname = "Estrange",
             username = "beauty_lest",
             dateOfBirth = Calendar.getInstance().apply { set(1985, 10, 31); stripTime() },
             country = "UK",
             email = "bellalast@example.com",
             password = "securePassword987",
             userDescription = "hi",
             profilePicture = null,
             typeTravel = listOf(TypeTravel.RELAX, TypeTravel.PARTY),
             desiredDestination = listOf("Romania", "USA", "South Korea"),
             rating = 4.3f,
             reliability = 55,
             publicTrips = emptyList(),
             articles = emptyList(),
             reviews = emptyList(),
             privateTrips = emptyList(),
             tripsAppliedTo = emptyList(),
             tripsApplicationAccepted = emptyList(),
             requestedSpots = 1
         ),
     )

}