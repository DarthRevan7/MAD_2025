package com.example.voyago.model

import com.example.voyago.model.Trip.Activity
import com.example.voyago.model.Trip.TripStatus
import java.util.Calendar



val users = listOf(
    LazyUser(1, "Alice", "Johnson"),
    LazyUser(2, "Bob", "Smith"),
    LazyUser(3, "Charlie", "Lee"),
    LazyUser(4, "Diana", "Martinez"),
    LazyUser(5, "Ethan", "Brown"),
    LazyUser(6, "Fiona", "White")
)

var tripList = mutableListOf<Trip>(
    Trip(
        id = 101,
        photo = 0,
        title = "Cultural Wonders of Spain",
        destination = "Barcelona",
        startDate = Calendar.getInstance().apply { set(2025, 6, 10) },
        endDate = Calendar.getInstance().apply { set(2025, 6, 11) },
        estimatedPrice = 950.0,
        groupSize = 4,
        creatorId = 1,
        participants = listOf(1, 2, 3, 4),
        appliedUsers = listOf(5, 6),
        published = true,
        typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.RELAX),
        status = TripStatus.COMPLETED,
        activities = mapOf(
            Calendar.getInstance().apply { set(2025, 6, 10) } to listOf(
                Activity(1, Calendar.getInstance().apply { set(2025, 6, 10) }, "09:00", true, "City tour"),
                Activity(2, Calendar.getInstance().apply { set(2025, 6, 10) }, "12:00", false, "Lunch at market"),
                Activity(3, Calendar.getInstance().apply { set(2025, 6, 10) }, "15:00", true, "Museum visit"),
                Activity(4, Calendar.getInstance().apply { set(2025, 6, 10) }, "20:00", true, "Welcome dinner")
            ),
            Calendar.getInstance().apply { set(2025, 6, 11) } to listOf(
                Activity(5, Calendar.getInstance().apply { set(2025, 6, 11) }, "08:00", true, "Hiking trip"),
                Activity(6, Calendar.getInstance().apply { set(2025, 6, 11) }, "13:00", true, "Mountain picnic"),
                Activity(7, Calendar.getInstance().apply { set(2025, 6, 11) }, "17:00", false, "Free time"),
                Activity(8, Calendar.getInstance().apply { set(2025, 6, 11) }, "21:00", true, "Campfire stories")
            )
        ),
        reviews = listOf(
            Review(
                1, users[1], "Amazing trip!",
                "This trip was absolutely incredible from start to finish. The guided city tour was informative and fun, the food was delicious, and the museum visit was a highlight for me. Everything was well-organized and the group dynamic was awesome. I would recommend this experience to anyone wanting a deep cultural immersion.",
                9, emptyList()
            ),
            Review(
                2, users[2], "Great experience",
                "Barcelona was a dream destination and this trip made it even better. I loved how the itinerary was balanced with both group activities and personal time. The hike on day two was a bit challenging but totally worth it for the views. I came back with great memories and new friends.",
                8, emptyList()
            ),
            Review(
                3, users[3], "Would go again",
                "I’m really impressed by how well this trip was planned. Every activity had a purpose, and even the free time was suggested with local tips. The welcome dinner was a beautiful introduction to Spanish culture, and the entire experience felt authentic and enriching. 10/10 would do it again.",
                10, emptyList()
            )
        )
    ),

    Trip(
        id = 102, photo = 1, title = "Beach Escape in Thailand", destination = "Phuket, Thailand",
        startDate = Calendar.getInstance().apply { set(2025, 7, 20) },
        endDate = Calendar.getInstance().apply { set(2025, 7, 21) },
        estimatedPrice = 1200.0, groupSize = 4, creatorId = 2,
        participants = listOf(2, 3, 5, 6), appliedUsers = listOf(1, 4), published = true,
        typeTravel = listOf(TypeTravel.PARTY, TypeTravel.RELAX), status = TripStatus.COMPLETED,
        activities = mapOf(
            Calendar.getInstance().apply { set(2025, 7, 20) } to listOf(
                Activity(9, Calendar.getInstance().apply { set(2025, 7, 20) }, "10:00", true, "Snorkeling tour"),
                Activity(10, Calendar.getInstance().apply { set(2025, 7, 20) }, "13:00", true, "Beach lunch"),
                Activity(11, Calendar.getInstance().apply { set(2025, 7, 20) }, "16:00", false, "Massage session"),
                Activity(12, Calendar.getInstance().apply { set(2025, 7, 20) }, "19:00", true, "Sunset cruise")
            ),
            Calendar.getInstance().apply { set(2025, 7, 21) } to listOf(
                Activity(13, Calendar.getInstance().apply { set(2025, 7, 21) }, "09:00", true, "Island hopping"),
                Activity(14, Calendar.getInstance().apply { set(2025, 7, 21) }, "12:00", true, "Cuisine tasting"),
                Activity(15, Calendar.getInstance().apply { set(2025, 7, 21) }, "15:00", false, "Shopping"),
                Activity(16, Calendar.getInstance().apply { set(2025, 7, 21) }, "20:00", true, "Beach party")
            )
        ),
        reviews = listOf(
            Review(
                4, users[2], "Loved it!",
                "This was the ultimate beach escape. The snorkeling tour showed us some of the most stunning coral reefs I’ve ever seen. The food was delicious, and the beach party was an unforgettable night with music, dancing, and laughter. It struck the perfect balance between adventure and relaxation.",
                10, emptyList()
            ),
            Review(
                5, users[4], "Relaxing trip",
                "I needed a break from work, and this trip delivered. From the moment we arrived, everything was taken care of. The massage session was heavenly, and the sunsets over the ocean were something out of a movie. I left feeling refreshed and truly happy. Would highly recommend for anyone seeking peace.",
                9, emptyList()
            ),
            Review(
                6, users[5], "Beautiful place",
                "Thailand was everything I imagined and more. The island hopping day was packed with activities, yet never felt rushed. The local cuisine tasting opened my eyes to so many flavors, and I even brought some recipes home. A wonderful way to experience the culture while soaking up the sun.",
                8, emptyList()
            )
        )
    )
)

