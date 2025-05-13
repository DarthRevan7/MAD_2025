

package com.example.voyago.viewmodel

import androidx.lifecycle.ViewModel
import com.example.voyago.model.LazyUser
import com.example.voyago.model.Review
import com.example.voyago.model.Trip
import com.example.voyago.model.Trip.Activity
import com.example.voyago.model.Trip.TripStatus
import com.example.voyago.model.TypeTravel
import com.example.voyago.model.stripTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

private val users = listOf(
    LazyUser(1, "Alice", "Johnson", 4.2f),
    LazyUser(2, "Bob", "Smith", 4.5f),
    LazyUser(3, "Charlie", "Lee", 4.3f),
    LazyUser(4, "Diana", "Martinez", 3.9f),
    LazyUser(5, "Ethan", "Brown", 4.7f),
    LazyUser(6, "Fiona", "White", 4.6f)
)

fun sampleTrips(): List<Trip> {
    return listOf(
        Trip(
            id = 1,
            photo = "barcelona",
            title = "Cultural Wonders of Spain",
            destination = "Barcelona",
            startDate = Calendar.getInstance().apply { set(2024, 6, 10); stripTime() },
            endDate = Calendar.getInstance().apply { set(2024, 6, 11); stripTime() },
            estimatedPrice = 950.0,
            groupSize = 4,
            creatorId = 6,
            participants = listOf(1, 2, 3, 4),
            appliedUsers = listOf(5, 6),
            published = true,
            typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.RELAX),
            status = TripStatus.COMPLETED,
            activities = mapOf(
                Calendar.getInstance().apply { set(2024, 6, 10); stripTime() } to listOf(
                    Activity(
                        1,
                        Calendar.getInstance().apply { set(2024, 6, 10); stripTime() },
                        "09:00 AM",
                        true,
                        "City tour"
                    ),
                    Activity(
                        2,
                        Calendar.getInstance().apply { set(2024, 6, 10); stripTime() },
                        "02:00 PM",
                        false,
                        "Lunch at market"
                    ),
                    Activity(
                        3,
                        Calendar.getInstance().apply { set(2024, 6, 10); stripTime() },
                        "03:00 PM",
                        true,
                        "Museum visit"
                    ),
                    Activity(
                        4,
                        Calendar.getInstance().apply { set(2024, 6, 10); stripTime() },
                        "08:00 PM",
                        true,
                        "Welcome dinner"
                    )
                ),
                Calendar.getInstance().apply { set(2024, 6, 11); stripTime() } to listOf(
                    Activity(
                        5,
                        Calendar.getInstance().apply { set(2024, 6, 11); stripTime() },
                        "08:00 AM",
                        true,
                        "Hiking trip"
                    ),
                    Activity(
                        6,
                        Calendar.getInstance().apply { set(2024, 6, 11); stripTime() },
                        "01:00 PM",
                        true,
                        "Mountain picnic"
                    ),
                    Activity(
                        7,
                        Calendar.getInstance().apply { set(2024, 6, 11); stripTime() },
                        "05:00 PM",
                        false,
                        "Free time"
                    ),
                    Activity(
                        8,
                        Calendar.getInstance().apply { set(2024, 6, 11); stripTime() },
                        "09:00 PM",
                        true,
                        "Campfire stories"
                    )
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
            id = 2,
            photo = "phuket",
            title = "Beach Escape in Thailand",
            destination = "Phuket",
            startDate = Calendar.getInstance().apply { set(2024, 7, 20); stripTime() },
            endDate = Calendar.getInstance().apply { set(2024, 7, 21); stripTime() },
            estimatedPrice = 1200.0,
            groupSize = 4,
            creatorId = 2,
            participants = listOf(2, 3, 5, 6),
            appliedUsers = listOf(1, 4),
            published = true,
            typeTravel = listOf(TypeTravel.PARTY, TypeTravel.RELAX),
            status = TripStatus.COMPLETED,
            activities = mapOf(
                Calendar.getInstance().apply { set(2024, 7, 20); stripTime() } to listOf(
                    Activity(
                        9,
                        Calendar.getInstance().apply { set(2024, 7, 20); stripTime() },
                        "10:00 AM",
                        true,
                        "Snorkeling tour"
                    ),
                    Activity(
                        10,
                        Calendar.getInstance().apply { set(2024, 7, 20); stripTime() },
                        "01:00 PM",
                        true,
                        "Beach lunch"
                    ),
                    Activity(
                        11,
                        Calendar.getInstance().apply { set(2024, 7, 20); stripTime() },
                        "04:00 PM",
                        false,
                        "Massage session"
                    ),
                    Activity(
                        12,
                        Calendar.getInstance().apply { set(2024, 7, 20); stripTime() },
                        "07:00 PM",
                        true,
                        "Sunset cruise"
                    )
                ),
                Calendar.getInstance().apply { set(2024, 7, 21); stripTime() } to listOf(
                    Activity(
                        13,
                        Calendar.getInstance().apply { set(2024, 7, 21); stripTime() },
                        "09:00 AM",
                        true,
                        "Island hopping"
                    ),
                    Activity(
                        14,
                        Calendar.getInstance().apply { set(2024, 7, 21); stripTime() },
                        "02:00 PM",
                        true,
                        "Cuisine tasting"
                    ),
                    Activity(
                        15,
                        Calendar.getInstance().apply { set(2024, 7, 21); stripTime() },
                        "03:00 PM",
                        false,
                        "Shopping"
                    ),
                    Activity(
                        16,
                        Calendar.getInstance().apply { set(2024, 7, 21); stripTime() },
                        "08:00 PM",
                        true,
                        "Beach party"
                    )
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
        ),


        Trip(
            id = 3,
            photo = "peru",
            title = "Adventures in Peru",
            destination = "Cusco",
            startDate = Calendar.getInstance().apply { set(2024, 1, 5); stripTime() },
            endDate = Calendar.getInstance().apply { set(2024, 1, 6); stripTime() },
            estimatedPrice = 1100.0,
            groupSize = 4,
            creatorId = 3,
            participants = listOf(1, 3, 4, 5),
            appliedUsers = listOf(2, 6),
            published = true,
            typeTravel = listOf(TypeTravel.ADVENTURE, TypeTravel.CULTURE),
            status = TripStatus.IN_PROGRESS,
            activities = mapOf(
                Calendar.getInstance().apply { set(2024, 9, 5); stripTime() } to listOf(
                    Activity(17, Calendar.getInstance().apply { set(2024, 9, 5); stripTime() }, "08:00 AM", true, "Guided trek through Sacred Valley"),
                    Activity(18, Calendar.getInstance().apply { set(2024, 9, 5); stripTime() }, "02:00 PM", true, "Traditional Andean lunch"),
                    Activity(19, Calendar.getInstance().apply { set(2024, 9, 5); stripTime() }, "03:00 PM", false, "Free exploration time"),
                    Activity(20, Calendar.getInstance().apply { set(2024, 9, 5); stripTime() }, "07:00 PM", true, "Local dance show")
                ),
                Calendar.getInstance().apply { set(2024, 9, 6) } to listOf(
                    Activity(21, Calendar.getInstance().apply { set(2024, 9, 6); stripTime() }, "06:00 AM", true, "Visit to Machu Picchu"),
                    Activity(22, Calendar.getInstance().apply { set(2024, 9, 6); stripTime() }, "11:00 AM", false, "Photography session"),
                    Activity(23, Calendar.getInstance().apply { set(2024, 9, 6); stripTime() }, "02:00 PM", true, "Lunch with a view"),
                    Activity(24, Calendar.getInstance().apply { set(2024, 9, 6); stripTime()}, "06:00 PM", true, "Group reflection session")
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
class TripListViewModel : ViewModel() {
    private var _tripList = MutableStateFlow<List<Trip>>(emptyList())

    var tripList: StateFlow<List<Trip>> = _tripList
    init {
        _tripList.value = sampleTrips()
    }

}