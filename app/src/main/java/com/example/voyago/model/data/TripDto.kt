package com.example.voyago.model.data




import com.example.voyago.model.domain.Trip
import com.example.voyago.model.domain.Trip.Activity
import com.example.voyago.model.domain.Trip.TripStatus
import com.example.voyago.model.domain.TypeTravel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@IgnoreExtraProperties
data class TripDto(
    val id: Long = 0L,
    val photo: String = "",
    val title: String = "",
    val destination: String = "",
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val estimatedPrice: Double = 0.0,
    val groupSize: Long = 0L,
    val participants: List<Long> = emptyList(),
    /** 用 yyyy-MM-dd 格式的 key */
    val activities: Map<String, List<ActivityDto>> = emptyMap(),
    /** 用枚举的 name 存 */
    val status: String = "",
    val typeTravel: List<String> = emptyList(),
    val creatorId: Long = 0L,
    val appliedUsers: List<Long> = emptyList(),
    val rejectedUsers: List<Long> = emptyList(),
    val published: Boolean = false,
    /** 存 reviewId 列表 */
    val reviews: List<Long> = emptyList()
) {
    @IgnoreExtraProperties
    data class ActivityDto(
        val id: Long = 0L,
        val date: String = "",           // "yyyy-MM-dd"
        val time: String = "",
        val isGroupActivity: Boolean = false,
        val description: String = ""
    )
}

private fun tsToCal(ts: Timestamp?): Calendar {
    return Calendar.getInstance().apply {
        timeInMillis = (ts?.seconds ?: 0L) * 1000
    }
}
private val DATE_FMT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

/** Trip → TripDto */
fun Trip.toDto(): TripDto {
    // Calendar → Timestamp
    fun calToTs(c: Calendar): Timestamp {
        val ms = c.timeInMillis
        return Timestamp(ms / 1000, ((ms % 1000) * 1_000_000).toInt())
    }

    // activities: Calendar-keyed map → String-keyed
    val actDto: Map<String, List<TripDto.ActivityDto>> = activities.mapKeys { (c, _) ->
        DATE_FMT.format(c.time)
    }.mapValues { (_, list) ->
        list.map {
            TripDto.ActivityDto(
                id = it.id.toLong(),
                date = DATE_FMT.format(it.date.time),
                time = it.time,
                isGroupActivity = it.isGroupActivity,
                description = it.description
            )
        }
    }

    return TripDto(
        id = id.toLong(),
        photo = photo,
        title = title,
        destination = destination,
        startDate = calToTs(startDate),
        endDate   = calToTs(endDate),
        estimatedPrice = estimatedPrice,
        groupSize = groupSize.toLong(),
        participants = participants.map { it.toLong() },
        activities  = actDto,
        status      = status.name,
        typeTravel  = typeTravel.map { it.name },
        creatorId   = creatorId.toLong(),
        appliedUsers  = appliedUsers.map { it.toLong() },
        rejectedUsers = rejectedUsers.map { it.toLong() },
        published  = published,
        reviews    = reviews.map { it.reviewId.toLong() }
    )
}

/** TripDto → Trip */
fun TripDto.toDomain(): Trip {
    // Timestamp → Calendar
    fun tsToCal(ts: Timestamp?): Calendar {
        return Calendar.getInstance().apply {
            timeInMillis = (ts?.seconds ?: 0L) * 1000
        }
    }

    // activities back to Calendar-keyed map
    val actDomain: Map<Calendar, List<Activity>> = activities.mapKeys { (dateStr, _) ->
        val d = DATE_FMT.parse(dateStr)!!
        Calendar.getInstance().apply { time = d }
    }.mapValues { (_, dtoList) ->
        dtoList.map {
            Activity(
                id = it.id.toInt(),
                date = DATE_FMT.parse(it.date).let { dt -> Calendar.getInstance().apply { time = dt } },
                time = it.time,
                isGroupActivity = it.isGroupActivity,
                description = it.description
            )
        }
    }


    return Trip(
        id = id.toInt(),
        photo = photo,
        title = title,
        destination = destination,
        startDate = tsToCal(startDate),
        endDate   = tsToCal(endDate),
        estimatedPrice = estimatedPrice,
        groupSize = groupSize.toInt(),
        participants = participants.map { it.toInt() },
        activities  = actDomain,
        status      = TripStatus.valueOf(status),
        typeTravel  = typeTravel.map { TypeTravel.valueOf(it) },
        creatorId   = creatorId.toInt(),
        appliedUsers  = appliedUsers.map { it.toInt() },
        rejectedUsers = rejectedUsers.map { it.toInt() },
        published  = published,
        reviews    = emptyList()
    )
}


class TripRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val reviewRepo: ReviewRepository = ReviewRepository(firestore)
) {

    /** 按 ID 拉一条完整的 Trip，包括真正的 Review 对象列表 */
    suspend fun fetchById(id: Int): Trip? = coroutineScope {
        // 1) 读出 DTO
        val snap = firestore.collection("trips")
            .document(id.toString())
            .get()
            .await()
        val dto = snap.toObject(TripDto::class.java) ?: return@coroutineScope null

        // 2) 基础映射（reviews 先留空）
        val baseTrip = dto.toDomain()

        // 3) 并发请求：把每个 reviewId → Review 对象
        val reviewsDeferred = dto.reviews.map { reviewId ->
            async {
                reviewRepo.fetchById(reviewId.toInt())
            }
        }

        // 4) 等待并过滤掉取不到的（null）
        val fullReviews = reviewsDeferred.awaitAll().filterNotNull()

        // 5) 把真正的评论列表填回去
        return@coroutineScope baseTrip.copy(reviews = fullReviews)
    }

    suspend fun save(trip: Trip) {
        firestore.collection("trips")
            .document(trip.id.toString())
            .set(trip.toDto())
            .await()
    }


    suspend fun fetchAll(): List<Trip> {
        val snaps = firestore.collection("trips")
            .get().await()
        return snaps.documents.mapNotNull {
            it.toObject(TripDto::class.java)?.toDomain()
        }
    }

    /** ④ 拉取原始的 QuerySnapshot，用于检查集合是否空 */
    suspend fun fetchAllRawSnapshot(): QuerySnapshot {
        return firestore
            .collection("trips")
            .get()
            .await()
    }

    /** ⑤ 批量写入一组 Trip（例如写入 sampleTrips()） */
    suspend fun saveAll(trips: List<Trip>) {
        // 用 Firestore batch 更高效地一次提交
        val batch = firestore.batch()
        trips.forEach { trip ->
            val docRef = firestore
                .collection("trips")
                .document(trip.id.toString())
            batch.set(docRef, trip.toDto())
        }
        batch.commit().await()
    }
    suspend fun delete(id: Int) {
        firestore.collection("trips")
            .document(id.toString())
            .delete()
            .await()
    }
    suspend fun delete(trip: Trip) = delete(trip.id)
}


fun sampleTrips(): List<Trip> {
    return listOf(
        Trip(
            id = 1,
            photo = "barcelona",
            title = "Cultural Wonders of Spain",
            destination = "Barcelona",
            startDate = Calendar.getInstance().apply { set(2025, 6, 10); stripTime() },
            endDate = Calendar.getInstance().apply { set(2025, 6, 11); stripTime() },
            estimatedPrice = 950.0,
            groupSize = 4,
            creatorId = 6,
            participants = listOf(1, 2, 3, 4),
            appliedUsers = listOf(5, 6),
            rejectedUsers = emptyList(),
            published = true,
            typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.RELAX),
            status = TripStatus.COMPLETED,
            activities = mapOf(
                Calendar.getInstance().apply { set(2025, 6, 10); stripTime() } to listOf(
                    Activity(
                        1,
                        Calendar.getInstance().apply { set(2025, 6, 10); stripTime() },
                        "09:00 AM",
                        true,
                        "City tour"
                    ),
                    Activity(
                        2,
                        Calendar.getInstance().apply { set(2025, 6, 10); stripTime() },
                        "02:00 PM",
                        false,
                        "Lunch at market"
                    ),
                    Activity(
                        3,
                        Calendar.getInstance().apply { set(2025, 6, 10); stripTime() },
                        "03:00 PM",
                        true,
                        "Museum visit"
                    ),
                    Activity(
                        4,
                        Calendar.getInstance().apply { set(2025, 6, 10); stripTime() },
                        "08:00 PM",
                        true,
                        "Welcome dinner"
                    )
                ),
                Calendar.getInstance().apply { set(2025, 6, 11); stripTime() } to listOf(
                    Activity(
                        5,
                        Calendar.getInstance().apply { set(2025, 6, 11); stripTime() },
                        "08:00 AM",
                        true,
                        "Hiking trip"
                    ),
                    Activity(
                        6,
                        Calendar.getInstance().apply { set(2025, 6, 11); stripTime() },
                        "01:00 PM",
                        true,
                        "Mountain picnic"
                    ),
                    Activity(
                        7,
                        Calendar.getInstance().apply { set(2025, 6, 11); stripTime() },
                        "05:00 PM",
                        false,
                        "Free time"
                    ),
                    Activity(
                        8,
                        Calendar.getInstance().apply { set(2025, 6, 11); stripTime() },
                        "09:00 PM",
                        true,
                        "Campfire stories"
                    )
                )
            ),
            reviews = emptyList()

        ),

        Trip(
            id = 2,
            photo = "phuket",
            title = "Beach Escape in Thailand",
            destination = "Phuket",
            startDate = Calendar.getInstance().apply { set(2025, 7, 20); stripTime() },
            endDate = Calendar.getInstance().apply { set(2025, 7, 21); stripTime() },
            estimatedPrice = 1200.0,
            groupSize = 4,
            creatorId = 2,
            participants = listOf(2, 3, 5, 6),
            appliedUsers = listOf(1, 4),
            rejectedUsers = emptyList(),
            published = true,
            typeTravel = listOf(TypeTravel.PARTY, TypeTravel.RELAX),
            status = TripStatus.COMPLETED,
            activities = mapOf(
                Calendar.getInstance().apply { set(2025, 7, 20); stripTime() } to listOf(
                    Activity(
                        9,
                        Calendar.getInstance().apply { set(2025, 7, 20); stripTime() },
                        "10:00 AM",
                        true,
                        "Snorkeling tour"
                    ),
                    Activity(
                        10,
                        Calendar.getInstance().apply { set(2025, 7, 20); stripTime() },
                        "01:00 PM",
                        true,
                        "Beach lunch"
                    ),
                    Activity(
                        11,
                        Calendar.getInstance().apply { set(2025, 7, 20); stripTime() },
                        "04:00 PM",
                        false,
                        "Massage session"
                    ),
                    Activity(
                        12,
                        Calendar.getInstance().apply { set(2025, 7, 20); stripTime() },
                        "07:00 PM",
                        true,
                        "Sunset cruise"
                    )
                ),
                Calendar.getInstance().apply { set(2025, 7, 21); stripTime() } to listOf(
                    Activity(
                        13,
                        Calendar.getInstance().apply { set(2025, 7, 21); stripTime() },
                        "09:00 AM",
                        true,
                        "Island hopping"
                    ),
                    Activity(
                        14,
                        Calendar.getInstance().apply { set(2025, 7, 21); stripTime() },
                        "02:00 PM",
                        true,
                        "Cuisine tasting"
                    ),
                    Activity(
                        15,
                        Calendar.getInstance().apply { set(2025, 7, 21); stripTime() },
                        "03:00 PM",
                        false,
                        "Shopping"
                    ),
                    Activity(
                        16,
                        Calendar.getInstance().apply { set(2025, 7, 21); stripTime() },
                        "08:00 PM",
                        true,
                        "Beach party"
                    )
                )
            ),
            reviews = emptyList()
        ),


        Trip(
            id = 3,
            photo = "peru",
            title = "Adventures in Peru",
            destination = "Cusco",
            startDate = Calendar.getInstance().apply { set(2025, 9, 5); stripTime() },
            endDate = Calendar.getInstance().apply { set(2025, 9, 6); stripTime() },
            estimatedPrice = 1100.0,
            groupSize = 4,
            creatorId = 3,
            participants = listOf(1, 3, 4, 5),
            appliedUsers = listOf(2, 6),
            rejectedUsers = emptyList(),
            published = true,
            typeTravel = listOf(TypeTravel.ADVENTURE, TypeTravel.CULTURE),
            status = TripStatus.IN_PROGRESS,
            activities = mapOf(
                Calendar.getInstance().apply { set(2025, 9, 5); stripTime() } to listOf(
                    Activity(17, Calendar.getInstance().apply { set(2025, 9, 5); stripTime() }, "08:00 AM", true, "Guided trek through Sacred Valley"),
                    Activity(18, Calendar.getInstance().apply { set(2025, 9, 5); stripTime() }, "02:00 PM", true, "Traditional Andean lunch"),
                    Activity(19, Calendar.getInstance().apply { set(2025, 9, 5); stripTime() }, "03:00 PM", false, "Free exploration time"),
                    Activity(20, Calendar.getInstance().apply { set(2025, 9, 5); stripTime() }, "07:00 PM", true, "Local dance show")
                ),
                Calendar.getInstance().apply { set(2025, 9, 6) } to listOf(
                    Activity(21, Calendar.getInstance().apply { set(2025, 9, 6); stripTime() }, "06:00 AM", true, "Visit to Machu Picchu"),
                    Activity(22, Calendar.getInstance().apply { set(2025, 9, 6); stripTime() }, "11:00 AM", false, "Photography session"),
                    Activity(23, Calendar.getInstance().apply { set(2025, 9, 6); stripTime() }, "02:00 PM", true, "Lunch with a view"),
                    Activity(24, Calendar.getInstance().apply { set(2025, 9, 6); stripTime()}, "06:00 PM", true, "Group reflection session")
                )
            ),
            reviews = emptyList()
        ),

        Trip(
            id = 4,
            photo = "japan",
            title = "Japanese Autumn Journey",
            destination = "Japan",
            startDate = Calendar.getInstance().apply { set(2025, 11, 5); stripTime() },
            endDate = Calendar.getInstance().apply { set(2025, 11, 8); stripTime() },
            estimatedPrice = 1800.0,
            groupSize = 4,
            creatorId = 1,
            participants = listOf(1),
            appliedUsers = emptyList(),
            rejectedUsers = emptyList(),
            published = false,
            typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.RELAX),
            status = TripStatus.NOT_STARTED,
            activities = mapOf(
                Calendar.getInstance().apply { set(2025, 11, 5); stripTime() } to listOf(
                    Activity(49, Calendar.getInstance().apply { set(2025, 11, 5); stripTime() }, "09:00 AM", true, "Fushimi Inari shrine walk"),
                    Activity(50, Calendar.getInstance().apply { set(2025, 11, 5); stripTime() }, "02:00 PM", true, "Ramen tasting lunch"),
                    Activity(51, Calendar.getInstance().apply { set(2025, 11, 5); stripTime() }, "03:00 PM", false, "Kimono fitting"),
                    Activity(52, Calendar.getInstance().apply { set(2025, 11, 5); stripTime() }, "06:00 PM", true, "Tea ceremony experience")
                ),
                Calendar.getInstance().apply { set(2025, 11, 6); stripTime() } to listOf(
                    Activity(53, Calendar.getInstance().apply { set(2025, 11, 6); stripTime() }, "08:00 AM", true, "Visit Arashiyama Bamboo Grove"),
                    Activity(54, Calendar.getInstance().apply { set(2025, 11, 6); stripTime() }, "11:00 AM", true, "River boat ride"),
                    Activity(55, Calendar.getInstance().apply { set(2025, 11, 6); stripTime() }, "02:00 PM", false, "Visit monkey park"),
                    Activity(56, Calendar.getInstance().apply { set(2025, 11, 6); stripTime() }, "08:00 PM", true, "Night street food tour")
                ),
                Calendar.getInstance().apply { set(2025, 11, 7); stripTime() } to listOf(
                    Activity(57, Calendar.getInstance().apply { set(2025, 11, 7); stripTime() }, "09:00 AM", true, "Day trip to Nara"),
                    Activity(58, Calendar.getInstance().apply { set(2025, 11, 7); stripTime() }, "02:00 PM", true, "Deer park picnic"),
                    Activity(59, Calendar.getInstance().apply { set(2025, 11, 7); stripTime() }, "03:00 PM", false, "Visit Todai-ji temple"),
                    Activity(60, Calendar.getInstance().apply { set(2025, 11, 7); stripTime() }, "07:00 PM", true, "Traditional dinner")
                ),
                Calendar.getInstance().apply { set(2025, 11, 8); stripTime() } to listOf(
                    Activity(61, Calendar.getInstance().apply { set(2025, 11, 8); stripTime() }, "08:00 AM", true, "Morning garden stroll"),
                    Activity(62, Calendar.getInstance().apply { set(2025, 11, 8); stripTime() }, "11:00 AM", false, "Souvenir shopping"),
                    Activity(63, Calendar.getInstance().apply { set(2025, 11, 8); stripTime() }, "02:00 PM", true, "Cooking class"),
                    Activity(64, Calendar.getInstance().apply { set(2025, 11, 8); stripTime() }, "06:00 PM", true, "Closing group dinner")
                )
            ),
            reviews = emptyList()
        ),

        Trip(
            id = 5,
            photo = "kyoto",
            title = "Kyoto Zen Retreat",
            destination = "Kyoto",
            startDate = Calendar.getInstance().apply { set(2025, 12, 1); stripTime() },
            endDate = Calendar.getInstance().apply { set(2025, 12, 4); stripTime() },
            estimatedPrice = 1650.0,
            groupSize = 5,
            creatorId = 1,
            participants = listOf(1, 2),
            appliedUsers = listOf(4, 5),
            rejectedUsers = emptyList(),
            published = true,
            typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.RELAX),
            status = TripStatus.NOT_STARTED,
            activities = mapOf(
                Calendar.getInstance().apply { set(2025, 12, 1); stripTime() } to listOf(
                    Activity(65, Calendar.getInstance().apply { set(2025, 12, 1); stripTime() }, "08:00 AM", true, "Zen meditation at Ryōan-ji"),
                    Activity(66, Calendar.getInstance().apply { set(2025, 12, 1); stripTime() }, "11:00 AM", true, "Stroll through Philosopher’s Path"),
                    Activity(67, Calendar.getInstance().apply { set(2025, 12, 1); stripTime() }, "02:00 PM", false, "Matcha tasting"),
                    Activity(68, Calendar.getInstance().apply { set(2025, 12, 1); stripTime() }, "06:00 PM", true, "Kaiseki dinner at local ryokan")
                ),
                Calendar.getInstance().apply { set(2025, 12, 2); stripTime() } to listOf(
                    Activity(69, Calendar.getInstance().apply { set(2025, 12, 2); stripTime() }, "09:00 AM", true, "Visit Fushimi Inari shrine"),
                    Activity(70, Calendar.getInstance().apply { set(2025, 12, 2); stripTime() }, "02:00 PM", true, "Tofu lunch in Gion"),
                    Activity(71, Calendar.getInstance().apply { set(2025, 12, 2); stripTime() }, "03:00 PM", false, "Tea ceremony workshop"),
                    Activity(72, Calendar.getInstance().apply { set(2025, 12, 2); stripTime() }, "07:00 PM", true, "Night walk in Gion district")
                ),
                Calendar.getInstance().apply { set(2025, 12, 3); stripTime() } to listOf(
                    Activity(73, Calendar.getInstance().apply { set(2025, 12, 3); stripTime() }, "08:00 AM", true, "Arashiyama Bamboo Grove walk"),
                    Activity(74, Calendar.getInstance().apply { set(2025, 12, 3); stripTime() }, "11:00 AM", true, "River boat ride"),
                    Activity(75, Calendar.getInstance().apply { set(2025, 12, 3); stripTime() }, "02:00 PM", false, "Zen garden journaling"),
                    Activity(76, Calendar.getInstance().apply { set(2025, 12, 3); stripTime() }, "06:00 PM", true, "Dinner & sake tasting")
                ),
                Calendar.getInstance().apply { set(2025, 12, 4); stripTime() } to listOf(
                    Activity(77, Calendar.getInstance().apply { set(2025, 12, 4); stripTime() }, "09:00 AM", true, "Kinkaku-ji visit"),
                    Activity(78, Calendar.getInstance().apply { set(2025, 12, 4); stripTime() }, "02:00 PM", true, "Farewell sushi lunch"),
                    Activity(79, Calendar.getInstance().apply { set(2025, 12, 4); stripTime() }, "03:00 PM", false, "Free time & shopping"),
                    Activity(80, Calendar.getInstance().apply { set(2025, 12, 4); stripTime() }, "06:00 PM", true, "Closing circle reflection")
                )
            ),
            reviews = emptyList()
        ),


        Trip(
            id = 6,
            photo = "sydney",
            title = "Sydney Coastal Explorer",
            destination = "Sydney",
            startDate = Calendar.getInstance().apply { set(2025, 12, 10); stripTime() },
            endDate = Calendar.getInstance().apply { set(2025, 12, 13); stripTime() },
            estimatedPrice = 1900.0,
            groupSize = 6,
            creatorId = 4,
            participants = listOf(2, 4, 5, 6),
            appliedUsers = listOf(1, 3),
            rejectedUsers = emptyList(),
            published = true,
            typeTravel = listOf(TypeTravel.ADVENTURE, TypeTravel.PARTY),
            status = TripStatus.NOT_STARTED,
            activities = mapOf(
                Calendar.getInstance().apply { set(2025, 12, 10) } to listOf(
                    Activity(81, Calendar.getInstance().apply { set(2025, 12, 10); stripTime() }, "09:00 AM", true, "Bondi to Coogee coastal walk"),
                    Activity(82, Calendar.getInstance().apply { set(2025, 12, 10); stripTime() }, "02:00 PM", true, "Beachside BBQ lunch"),
                    Activity(83, Calendar.getInstance().apply { set(2025, 12, 10); stripTime() }, "03:00 PM", false, "Surfing intro class"),
                    Activity(84, Calendar.getInstance().apply { set(2025, 12, 10); stripTime() }, "08:00 PM", true, "Rooftop drinks downtown")
                ),
                Calendar.getInstance().apply { set(2025, 12, 11) } to listOf(
                    Activity(85, Calendar.getInstance().apply { set(2025, 12, 11); stripTime() }, "08:00 AM", true, "Harbour Bridge climb"),
                    Activity(86, Calendar.getInstance().apply { set(2025, 12, 11); stripTime() }, "11:00 AM", true, "Opera House guided tour"),
                    Activity(87, Calendar.getInstance().apply { set(2025, 12, 11); stripTime() }, "02:00 PM", false, "Museum of Contemporary Art visit"),
                    Activity(88, Calendar.getInstance().apply { set(2025, 12, 11); stripTime() }, "07:00 PM", true, "Sunset dinner cruise")
                ),
                Calendar.getInstance().apply { set(2025, 12, 12) } to listOf(
                    Activity(89, Calendar.getInstance().apply { set(2025, 12, 12); stripTime() }, "10:00 AM", true, "Ferry to Manly Beach"),
                    Activity(90, Calendar.getInstance().apply { set(2025, 12, 12); stripTime() }, "01:00 PM", true, "Seafood lunch at the wharf"),
                    Activity(91, Calendar.getInstance().apply { set(2025, 12, 12); stripTime() }, "04:00 PM", false, "Relax on the sand"),
                    Activity(92, Calendar.getInstance().apply { set(2025, 12, 12); stripTime() }, "09:00 PM", true, "Beach party")
                ),
                Calendar.getInstance().apply { set(2025, 12, 13) } to listOf(
                    Activity(93, Calendar.getInstance().apply { set(2025, 12, 13); stripTime() }, "08:00 AM", true, "Morning yoga by the bay"),
                    Activity(94, Calendar.getInstance().apply { set(2025, 12, 13); stripTime() }, "11:00 AM", true, "Brunch & recap session"),
                    Activity(95, Calendar.getInstance().apply { set(2025, 12, 13); stripTime() }, "02:00 PM", false, "Last-minute shopping"),
                    Activity(96, Calendar.getInstance().apply { set(2025, 12, 13); stripTime() }, "05:00 PM", true, "Farewell drinks")
                )
            ),
            reviews = emptyList()
        ),

        Trip(
            id = 7,
            photo = "rio",
            title = "Rio Rhythms & Rainforests",
            destination = "Rio de Janeiro",
            startDate = Calendar.getInstance().apply { set(2026, 0, 10); stripTime() },
            endDate = Calendar.getInstance().apply { set(2026, 0, 16); stripTime() },
            estimatedPrice = 2100.0,
            groupSize = 6,
            creatorId = 2,
            participants = listOf(1, 2, 3, 5),
            appliedUsers = listOf(4, 6),
            rejectedUsers = emptyList(),
            published = true,
            typeTravel = listOf(TypeTravel.ADVENTURE, TypeTravel.PARTY),
            status = TripStatus.NOT_STARTED,
            activities = mapOf(
                Calendar.getInstance().apply { set(2026, 0, 10); stripTime() } to listOf(
                    Activity(
                        100,
                        Calendar.getInstance().apply { set(2026, 0, 10); stripTime() },
                        "08:00 AM",
                        true,
                        "Tijuca Forest hike"
                    ),
                    Activity(
                        101,
                        Calendar.getInstance().apply { set(2026, 0, 10); stripTime() },
                        "02:00 PM",
                        true,
                        "Street food tour"
                    ),
                    Activity(
                        102,
                        Calendar.getInstance().apply { set(2026, 0, 10); stripTime() },
                        "03:00 PM",
                        false,
                        "Beach volleyball at Copacabana"
                    ),
                    Activity(
                        103,
                        Calendar.getInstance().apply { set(2026, 0, 10); stripTime() },
                        "08:00 PM",
                        true,
                        "Samba night in Lapa"
                    )
                ),
                Calendar.getInstance().apply { set(2026, 0, 11); stripTime() } to listOf(
                    Activity(
                        104,
                        Calendar.getInstance().apply { set(2026, 0, 11); stripTime() },
                        "09:00 AM",
                        true,
                        "Sugarloaf Mountain cable car"
                    ),
                    Activity(
                        105,
                        Calendar.getInstance().apply { set(2026, 0, 11); stripTime() },
                        "01:00 PM",
                        true,
                        "Traditional feijoada lunch"
                    ),
                    Activity(
                        106,
                        Calendar.getInstance().apply { set(2026, 0, 11); stripTime() },
                        "04:00 PM",
                        false,
                        "Ipanema shopping stroll"
                    ),
                    Activity(
                        107,
                        Calendar.getInstance().apply { set(2026, 0, 11); stripTime() },
                        "09:00 PM",
                        true,
                        "Live bossa nova show"
                    )
                ),
                Calendar.getInstance().apply { set(2026, 0, 12); stripTime() } to listOf(
                    Activity(
                        108,
                        Calendar.getInstance().apply { set(2026, 0, 12); stripTime() },
                        "08:30 AM",
                        true,
                        "Christ the Redeemer visit"
                    ),
                    Activity(
                        109,
                        Calendar.getInstance().apply { set(2026, 0, 12); stripTime() },
                        "12:30 PM",
                        true,
                        "Lunch with a view at Santa Teresa"
                    ),
                    Activity(
                        110,
                        Calendar.getInstance().apply { set(2026, 0, 12); stripTime() },
                        "03:30 PM",
                        false,
                        "Art museum visit"
                    ),
                    Activity(
                        111,
                        Calendar.getInstance().apply { set(2026, 0, 12); stripTime() },
                        "07:00 PM",
                        true,
                        "Samba dance workshop"
                    )
                ),
                Calendar.getInstance().apply { set(2026, 0, 13); stripTime() } to listOf(
                    Activity(
                        112,
                        Calendar.getInstance().apply { set(2026, 0, 13); stripTime() },
                        "09:00 AM",
                        true,
                        "Favela walking tour"
                    ),
                    Activity(
                        113,
                        Calendar.getInstance().apply { set(2026, 0, 13); stripTime() },
                        "01:00 PM",
                        true,
                        "Local market tasting"
                    ),
                    Activity(
                        114,
                        Calendar.getInstance().apply { set(2026, 0, 13); stripTime() },
                        "04:00 PM",
                        false,
                        "Chill at Leblon beach"
                    ),
                    Activity(
                        115,
                        Calendar.getInstance().apply { set(2026, 0, 13); stripTime() },
                        "08:00 PM",
                        true,
                        "Evening cruise on Guanabara Bay"
                    )
                ),
                Calendar.getInstance().apply { set(2026, 0, 14); stripTime() } to listOf(
                    Activity(
                        116,
                        Calendar.getInstance().apply { set(2026, 0, 14); stripTime() },
                        "10:00 AM",
                        true,
                        "Hang gliding experience"
                    ),
                    Activity(
                        117,
                        Calendar.getInstance().apply { set(2026, 0, 14); stripTime() },
                        "01:00 PM",
                        true,
                        "Fresh seafood lunch"
                    ),
                    Activity(
                        118,
                        Calendar.getInstance().apply { set(2026, 0, 14); stripTime() },
                        "04:00 PM",
                        false,
                        "Free exploration time"
                    ),
                    Activity(
                        119,
                        Calendar.getInstance().apply { set(2026, 0, 14); stripTime() },
                        "10:00 PM",
                        true,
                        "Rio nightlife crawl"
                    )
                ),
                Calendar.getInstance().apply { set(2026, 0, 15); stripTime() } to listOf(
                    Activity(
                        120,
                        Calendar.getInstance().apply { set(2026, 0, 15); stripTime() },
                        "08:00 AM",
                        true,
                        "Morning yoga by the beach"
                    ),
                    Activity(
                        121,
                        Calendar.getInstance().apply { set(2026, 0, 15); stripTime() },
                        "11:00 AM",
                        true,
                        "Street art tour"
                    ),
                    Activity(
                        122,
                        Calendar.getInstance().apply { set(2026, 0, 15); stripTime() },
                        "03:00 PM",
                        false,
                        "Sunset photo session"
                    ),
                    Activity(
                        123,
                        Calendar.getInstance().apply { set(2026, 0, 15); stripTime() },
                        "07:00 PM",
                        true,
                        "Dinner & storytelling night"
                    )
                ),
                Calendar.getInstance().apply { set(2026, 0, 16); stripTime() } to listOf(
                    Activity(
                        124,
                        Calendar.getInstance().apply { set(2026, 0, 16); stripTime() },
                        "09:00 AM",
                        true,
                        "Botanical garden tour"
                    ),
                    Activity(
                        125,
                        Calendar.getInstance().apply { set(2026, 0, 16); stripTime() },
                        "02:00 PM",
                        true,
                        "Farewell brunch"
                    ),
                    Activity(
                        126,
                        Calendar.getInstance().apply { set(2026, 0, 16); stripTime() },
                        "02:00 PM",
                        false,
                        "Last-minute shopping"
                    ),
                    Activity(
                        127,
                        Calendar.getInstance().apply { set(2026, 0, 16); stripTime() },
                        "05:00 PM",
                        true,
                        "Trip recap & goodbye circle"
                    )
                )
            ),
            reviews = emptyList()
        ),

        Trip(
            id = 8,
            photo = "monaco",
            title = "Monaco Luxe Experience",
            destination = "Monaco",
            startDate = Calendar.getInstance().apply { set(2026, 4, 5); stripTime() },
            endDate = Calendar.getInstance().apply { set(2026, 4, 11); stripTime() },
            estimatedPrice = 4600.0,
            groupSize = 4,
            creatorId = 6,
            participants = listOf(2, 4, 6),
            appliedUsers = listOf(1, 5),
            rejectedUsers = emptyList(),
            published = true,
            typeTravel = listOf(TypeTravel.RELAX, TypeTravel.CULTURE),
            status = TripStatus.NOT_STARTED,
            activities = mapOf(
                Calendar.getInstance().apply { set(2026, 4, 5); stripTime() } to listOf(
                    Activity(
                        130,
                        Calendar.getInstance().apply { set(2026, 4, 5); stripTime() },
                        "09:00 AM",
                        true,
                        "Breakfast at Café de Paris"
                    ),
                    Activity(
                        131,
                        Calendar.getInstance().apply { set(2026, 4, 5); stripTime() },
                        "11:00 AM",
                        true,
                        "Tour of the Prince’s Palace"
                    ),
                    Activity(
                        132,
                        Calendar.getInstance().apply { set(2026, 4, 5); stripTime() },
                        "03:00 PM",
                        false,
                        "Free time in Monte Carlo"
                    ),
                    Activity(
                        133,
                        Calendar.getInstance().apply { set(2026, 4, 5); stripTime() },
                        "08:00 PM",
                        true,
                        "Gala dinner at Hôtel de Paris"
                    )
                ),
                Calendar.getInstance().apply { set(2026, 4, 6); stripTime() } to listOf(
                    Activity(
                        134,
                        Calendar.getInstance().apply { set(2026, 4, 6); stripTime() },
                        "10:00 AM",
                        true,
                        "Oceanographic Museum visit"
                    ),
                    Activity(
                        135,
                        Calendar.getInstance().apply { set(2026, 4, 6); stripTime() },
                        "01:00 PM",
                        true,
                        "Harbor-side lunch"
                    ),
                    Activity(
                        136,
                        Calendar.getInstance().apply { set(2026, 4, 6); stripTime() },
                        "04:00 PM",
                        false,
                        "Shopping at Boulevard des Moulins"
                    ),
                    Activity(
                        137,
                        Calendar.getInstance().apply { set(2026, 4, 6); stripTime() },
                        "07:00 PM",
                        true,
                        "Casino de Monte-Carlo night"
                    )
                ),
                Calendar.getInstance().apply { set(2026, 4, 7); stripTime() } to listOf(
                    Activity(
                        138,
                        Calendar.getInstance().apply { set(2026, 4, 7); stripTime() },
                        "09:00 AM",
                        true,
                        "Luxury yacht cruise"
                    ),
                    Activity(
                        139,
                        Calendar.getInstance().apply { set(2026, 4, 7); stripTime() },
                        "02:00 PM",
                        true,
                        "Private art gallery viewing"
                    ),
                    Activity(
                        140,
                        Calendar.getInstance().apply { set(2026, 4, 7); stripTime() },
                        "04:00 PM",
                        false,
                        "Spa and wellness retreat"
                    ),
                    Activity(
                        141,
                        Calendar.getInstance().apply { set(2026, 4, 7); stripTime() },
                        "08:00 PM",
                        true,
                        "Michelin-starred dinner"
                    )
                ),
                Calendar.getInstance().apply { set(2026, 4, 8); stripTime() } to listOf(
                    Activity(
                        142,
                        Calendar.getInstance().apply { set(2026, 4, 8); stripTime() },
                        "10:00 AM",
                        true,
                        "Matisse Museum exploration"
                    ),
                    Activity(
                        143,
                        Calendar.getInstance().apply { set(2026, 4, 8); stripTime() },
                        "01:00 PM",
                        true,
                        "Coastal drive to Eze"
                    ),
                    Activity(
                        144,
                        Calendar.getInstance().apply { set(2026, 4, 8); stripTime() },
                        "04:00 PM",
                        false,
                        "Free exploration of Eze"
                    ),
                    Activity(
                        145,
                        Calendar.getInstance().apply { set(2026, 4, 8); stripTime() },
                        "08:00 PM",
                        true,
                        "Evening at the Monte Carlo Opera House"
                    )
                ),
                Calendar.getInstance().apply { set(2026, 4, 9); stripTime() } to listOf(
                    Activity(
                        146,
                        Calendar.getInstance().apply { set(2026, 4, 9); stripTime() },
                        "09:00 AM",
                        true,
                        "Visit the Exotic Garden"
                    ),
                    Activity(
                        147,
                        Calendar.getInstance().apply { set(2026, 4, 9); stripTime() },
                        "02:00 PM",
                        true,
                        "Lunch in the old town"
                    ),
                    Activity(
                        148,
                        Calendar.getInstance().apply { set(2026, 4, 9); stripTime() },
                        "03:00 PM",
                        false,
                        "Shopping at La Condamine Market"
                    ),
                    Activity(
                        149,
                        Calendar.getInstance().apply { set(2026, 4, 9); stripTime() },
                        "07:00 PM",
                        true,
                        "Monaco Yacht Club party"
                    )
                ),
                Calendar.getInstance().apply { set(2026, 4, 10); stripTime() } to listOf(
                    Activity(
                        150,
                        Calendar.getInstance().apply { set(2026, 4, 10); stripTime() },
                        "09:00 AM",
                        true,
                        "Helicopter tour of Monaco"
                    ),
                    Activity(
                        151,
                        Calendar.getInstance().apply { set(2026, 4, 10); stripTime() },
                        "01:00 PM",
                        true,
                        "Luxury shopping spree"
                    ),
                    Activity(
                        152,
                        Calendar.getInstance().apply { set(2026, 4, 10); stripTime() },
                        "04:00 PM",
                        false,
                        "Relaxation at the beach"
                    ),
                    Activity(
                        153,
                        Calendar.getInstance().apply { set(2026, 4, 10); stripTime() },
                        "08:00 PM",
                        true,
                        "Farewell gala"
                    )
                )
            ),
            reviews = emptyList()
        )

    )

}