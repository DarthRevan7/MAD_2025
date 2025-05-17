package com.example.voyago.model.data



import android.net.Uri
import com.example.voyago.model.domain.Review
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Calendar

@IgnoreExtraProperties
data class ReviewDto(
    val reviewId: Long = 0L,
    val reviewerId: Long = 0L,
    val tripId: Long = 0L,
    val title: String = "",
    val comment: String = "",
    val score: Long = 0L,
    val photos: List<String> = emptyList(),    // URI → String
    val userId: Long? = null,
    val date: Timestamp? = null               // Calendar → Timestamp
)
fun Review.toDto(): ReviewDto {
    // 1) Calendar → Timestamp
    val millis = this.date.timeInMillis
    val seconds = millis / 1000
    val nanoseconds = ((millis % 1000) * 1_000_000).toInt()
    val ts = Timestamp(seconds, nanoseconds)

    // 2) Uri 列表 → String 列表
    val photoStrings = this.photos.map { it.toString() }

    return ReviewDto(
        reviewId   = this.reviewId.toLong(),
        reviewerId = this.reviewerId.toLong(),
        tripId     = this.tripId.toLong(),
        title      = this.title,
        comment    = this.comment,
        score      = this.score.toLong(),
        photos     = photoStrings,
        userId     = this.userId?.toLong(),
        date       = ts
    )
}

/** DTO → 领域模型，用于从 Firestore 读出后还原 */
fun ReviewDto.toDomain(): Review {
    // 1) Timestamp → Calendar
    val cal = Calendar.getInstance().apply {
        timeInMillis = (this@toDomain.date?.seconds ?: 0L) * 1000
    }

    // 2) String 列表 → Uri 列表
    val photoUris = this.photos.map { Uri.parse(it) }

    return Review(
        reviewId   = this.reviewId.toInt(),
        reviewerId = this.reviewerId.toInt(),
        tripId     = this.tripId.toInt(),
        title      = this.title,
        comment    = this.comment,
        score      = this.score.toInt(),
        photos     = photoUris,
        userId     = this.userId?.toInt(),
        date       = cal
    )
}
class ReviewRepository(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {
    /** 写一条 Review */
    suspend fun save(review: Review) {
        firestore.collection("reviews")
            .document(review.reviewId.toString())
            .set(review.toDto())
            .await()
    }

    /** 读一条 Review */
    suspend fun fetchById(id: Int): Review? {
        val snap = firestore.collection("reviews")
            .document(id.toString())
            .get()
            .await()
        return snap.toObject(ReviewDto::class.java)?.toDomain()
    }
}


fun sampleReviews(): List<Review> {
    return  listOf(
        Review(
            reviewId = 1,
            reviewerId = 1,
            tripId = 1,
            title = "Amazing trip!",
            comment = "This trip was absolutely incredible from start to finish. The guided city tour was informative and fun, the food was delicious, and the museum visit was a highlight for me. Everything was well-organized and the group dynamic was awesome. I would recommend this experience to anyone wanting a deep cultural immersion.",
            score = 9,
            photos = emptyList(),
            userId = 1,
            date = Calendar.getInstance().apply { set(2025, 6, 12); stripTime() }
        ),

        Review(
            reviewId = 2,
            reviewerId = 2,
            tripId = 1,
            title = "Loved it!",
            comment = "This was the ultimate beach escape. The snorkeling tour showed us some of the most stunning coral reefs I’ve ever seen. The food was delicious, and the beach party was an unforgettable night with music, dancing, and laughter. It struck the perfect balance between adventure and relaxation.",
            score = 10,
            photos = emptyList(),
            userId = 1,
            date = Calendar.getInstance().apply { set(2022, 4, 12); stripTime() }
        )
//
    )


}
fun Calendar.stripTime(): Calendar {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return this
}
