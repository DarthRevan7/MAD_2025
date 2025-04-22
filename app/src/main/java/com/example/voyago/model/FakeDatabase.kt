package com.example.voyago.model

import androidx.compose.runtime.mutableStateListOf
import com.example.voyago.model.Trip.Activity
import com.example.voyago.model.Trip.TripStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar


class Model {
    private val _users = listOf(
        LazyUser(1, "Alice", "Johnson"),
        LazyUser(2, "Bob", "Smith"),
        LazyUser(3, "Charlie", "Lee"),
        LazyUser(4, "Diana", "Martinez"),
        LazyUser(5, "Ethan", "Brown"),
        LazyUser(6, "Fiona", "White")
    )
    val users: List<LazyUser> = _users

    private var _tripList =  MutableStateFlow<List<Trip>>(
        listOf(
            Trip(
                id = 101,
                photo = "barcelona",
                title = "Cultural Wonders of Spain",
                destination = "Barcelona",
                startDate = Calendar.getInstance().apply { set(2025, 6, 10) },
                endDate = Calendar.getInstance().apply { set(2025, 6, 11) },
                estimatedPrice = 950.0,
                groupSize = 4,
                creatorId = 6,
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
                id = 102,
                photo = "phuket",
                title = "Beach Escape in Thailand",
                destination = "Phuket",
                startDate = Calendar.getInstance().apply { set(2025, 7, 20) },
                endDate = Calendar.getInstance().apply { set(2025, 7, 21) },
                estimatedPrice = 1200.0,
                groupSize = 4,
                creatorId = 2,
                participants = listOf(2, 3, 5, 6),
                appliedUsers = listOf(1, 4),
                published = true,
                typeTravel = listOf(TypeTravel.PARTY, TypeTravel.RELAX),
                status = TripStatus.COMPLETED,
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
            ),

            Trip(
                id = 103,
                photo = "peru",
                title = "Adventures in Peru",
                destination = "Cusco",
                startDate = Calendar.getInstance().apply { set(2025, 9, 5) },
                endDate = Calendar.getInstance().apply { set(2025, 9, 6) },
                estimatedPrice = 1100.0,
                groupSize = 4,
                creatorId = 3,
                participants = listOf(1, 3, 4, 5),
                appliedUsers = listOf(2, 6),
                published = true,
                typeTravel = listOf(TypeTravel.ADVENTURE, TypeTravel.CULTURE),
                status = TripStatus.IN_PROGRESS,
                activities = mapOf(
                    Calendar.getInstance().apply { set(2025, 9, 5) } to listOf(
                        Activity(17, Calendar.getInstance().apply { set(2025, 9, 5) }, "08:00", true, "Guided trek through Sacred Valley"),
                        Activity(18, Calendar.getInstance().apply { set(2025, 9, 5) }, "12:00", true, "Traditional Andean lunch"),
                        Activity(19, Calendar.getInstance().apply { set(2025, 9, 5) }, "15:00", false, "Free exploration time"),
                        Activity(20, Calendar.getInstance().apply { set(2025, 9, 5) }, "19:00", true, "Local dance show")
                    ),
                    Calendar.getInstance().apply { set(2025, 9, 6) } to listOf(
                        Activity(21, Calendar.getInstance().apply { set(2025, 9, 6) }, "06:00", true, "Visit to Machu Picchu"),
                        Activity(22, Calendar.getInstance().apply { set(2025, 9, 6) }, "11:00", false, "Photography session"),
                        Activity(23, Calendar.getInstance().apply { set(2025, 9, 6) }, "14:00", true, "Lunch with a view"),
                        Activity(24, Calendar.getInstance().apply { set(2025, 9, 6) }, "18:00", true, "Group reflection session")
                    )
                ),
                reviews = emptyList()
            ),

            Trip(
                id = 106,
                photo = "japan",
                title = "Japanese Autumn Journey",
                destination = "Japan",
                startDate = Calendar.getInstance().apply { set(2025, 11, 5) },
                endDate = Calendar.getInstance().apply { set(2025, 11, 8) },
                estimatedPrice = 1800.0,
                groupSize = 4,
                creatorId = 1,
                participants = listOf(1),
                appliedUsers = emptyList(),
                published = false,
                typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.RELAX),
                status = TripStatus.NOT_STARTED,
                activities = mapOf(
                    Calendar.getInstance().apply { set(2025, 11, 5) } to listOf(
                        Activity(49, Calendar.getInstance().apply { set(2025, 11, 5) }, "09:00", true, "Fushimi Inari shrine walk"),
                        Activity(50, Calendar.getInstance().apply { set(2025, 11, 5) }, "12:00", true, "Ramen tasting lunch"),
                        Activity(51, Calendar.getInstance().apply { set(2025, 11, 5) }, "15:00", false, "Kimono fitting"),
                        Activity(52, Calendar.getInstance().apply { set(2025, 11, 5) }, "18:00", true, "Tea ceremony experience")
                    ),
                    Calendar.getInstance().apply { set(2025, 11, 6) } to listOf(
                        Activity(53, Calendar.getInstance().apply { set(2025, 11, 6) }, "08:00", true, "Visit Arashiyama Bamboo Grove"),
                        Activity(54, Calendar.getInstance().apply { set(2025, 11, 6) }, "11:00", true, "River boat ride"),
                        Activity(55, Calendar.getInstance().apply { set(2025, 11, 6) }, "14:00", false, "Visit monkey park"),
                        Activity(56, Calendar.getInstance().apply { set(2025, 11, 6) }, "20:00", true, "Night street food tour")
                    ),
                    Calendar.getInstance().apply { set(2025, 11, 7) } to listOf(
                        Activity(57, Calendar.getInstance().apply { set(2025, 11, 7) }, "09:00", true, "Day trip to Nara"),
                        Activity(58, Calendar.getInstance().apply { set(2025, 11, 7) }, "12:00", true, "Deer park picnic"),
                        Activity(59, Calendar.getInstance().apply { set(2025, 11, 7) }, "15:00", false, "Visit Todai-ji temple"),
                        Activity(60, Calendar.getInstance().apply { set(2025, 11, 7) }, "19:00", true, "Traditional dinner")
                    ),
                    Calendar.getInstance().apply { set(2025, 11, 8) } to listOf(
                        Activity(61, Calendar.getInstance().apply { set(2025, 11, 8) }, "08:00", true, "Morning garden stroll"),
                        Activity(62, Calendar.getInstance().apply { set(2025, 11, 8) }, "11:00", false, "Souvenir shopping"),
                        Activity(63, Calendar.getInstance().apply { set(2025, 11, 8) }, "14:00", true, "Cooking class"),
                        Activity(64, Calendar.getInstance().apply { set(2025, 11, 8) }, "18:00", true, "Closing group dinner")
                    )
                ),
                reviews = emptyList()
            ),

            Trip(
                id = 107,
                photo = "kyoto",
                title = "Kyoto Zen Retreat",
                destination = "Kyoto",
                startDate = Calendar.getInstance().apply { set(2025, 12, 1) },
                endDate = Calendar.getInstance().apply { set(2025, 12, 4) },
                estimatedPrice = 1650.0,
                groupSize = 5,
                creatorId = 1,
                participants = listOf(1, 2),
                appliedUsers = listOf(4, 5),
                published = true,
                typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.RELAX),
                status = TripStatus.NOT_STARTED,
                activities = mapOf(
                    Calendar.getInstance().apply { set(2025, 12, 1) } to listOf(
                        Activity(65, Calendar.getInstance().apply { set(2025, 12, 1) }, "08:00", true, "Zen meditation at Ryōan-ji"),
                        Activity(66, Calendar.getInstance().apply { set(2025, 12, 1) }, "11:00", true, "Stroll through Philosopher’s Path"),
                        Activity(67, Calendar.getInstance().apply { set(2025, 12, 1) }, "14:00", false, "Matcha tasting"),
                        Activity(68, Calendar.getInstance().apply { set(2025, 12, 1) }, "18:00", true, "Kaiseki dinner at local ryokan")
                    ),
                    Calendar.getInstance().apply { set(2025, 12, 2) } to listOf(
                        Activity(69, Calendar.getInstance().apply { set(2025, 12, 2) }, "09:00", true, "Visit Fushimi Inari shrine"),
                        Activity(70, Calendar.getInstance().apply { set(2025, 12, 2) }, "12:00", true, "Tofu lunch in Gion"),
                        Activity(71, Calendar.getInstance().apply { set(2025, 12, 2) }, "15:00", false, "Tea ceremony workshop"),
                        Activity(72, Calendar.getInstance().apply { set(2025, 12, 2) }, "19:00", true, "Night walk in Gion district")
                    ),
                    Calendar.getInstance().apply { set(2025, 12, 3) } to listOf(
                        Activity(73, Calendar.getInstance().apply { set(2025, 12, 3) }, "08:00", true, "Arashiyama Bamboo Grove walk"),
                        Activity(74, Calendar.getInstance().apply { set(2025, 12, 3) }, "11:00", true, "River boat ride"),
                        Activity(75, Calendar.getInstance().apply { set(2025, 12, 3) }, "14:00", false, "Zen garden journaling"),
                        Activity(76, Calendar.getInstance().apply { set(2025, 12, 3) }, "18:00", true, "Dinner & sake tasting")
                    ),
                    Calendar.getInstance().apply { set(2025, 12, 4) } to listOf(
                        Activity(77, Calendar.getInstance().apply { set(2025, 12, 4) }, "09:00", true, "Kinkaku-ji visit"),
                        Activity(78, Calendar.getInstance().apply { set(2025, 12, 4) }, "12:00", true, "Farewell sushi lunch"),
                        Activity(79, Calendar.getInstance().apply { set(2025, 12, 4) }, "15:00", false, "Free time & shopping"),
                        Activity(80, Calendar.getInstance().apply { set(2025, 12, 4) }, "18:00", true, "Closing circle reflection")
                    )
                ),
                reviews = emptyList()
            ),

            Trip(
                id = 108,
                photo = "sydney",
                title = "Sydney Coastal Explorer",
                destination = "Sydney",
                startDate = Calendar.getInstance().apply { set(2025, 12, 10) },
                endDate = Calendar.getInstance().apply { set(2025, 12, 13) },
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
                        Activity(81, Calendar.getInstance().apply { set(2025, 12, 10) }, "09:00", true, "Bondi to Coogee coastal walk"),
                        Activity(82, Calendar.getInstance().apply { set(2025, 12, 10) }, "12:00", true, "Beachside BBQ lunch"),
                        Activity(83, Calendar.getInstance().apply { set(2025, 12, 10) }, "15:00", false, "Surfing intro class"),
                        Activity(84, Calendar.getInstance().apply { set(2025, 12, 10) }, "20:00", true, "Rooftop drinks downtown")
                    ),
                    Calendar.getInstance().apply { set(2025, 12, 11) } to listOf(
                        Activity(85, Calendar.getInstance().apply { set(2025, 12, 11) }, "08:00", true, "Harbour Bridge climb"),
                        Activity(86, Calendar.getInstance().apply { set(2025, 12, 11) }, "11:00", true, "Opera House guided tour"),
                        Activity(87, Calendar.getInstance().apply { set(2025, 12, 11) }, "14:00", false, "Museum of Contemporary Art visit"),
                        Activity(88, Calendar.getInstance().apply { set(2025, 12, 11) }, "19:00", true, "Sunset dinner cruise")
                    ),
                    Calendar.getInstance().apply { set(2025, 12, 12) } to listOf(
                        Activity(89, Calendar.getInstance().apply { set(2025, 12, 12) }, "10:00", true, "Ferry to Manly Beach"),
                        Activity(90, Calendar.getInstance().apply { set(2025, 12, 12) }, "13:00", true, "Seafood lunch at the wharf"),
                        Activity(91, Calendar.getInstance().apply { set(2025, 12, 12) }, "16:00", false, "Relax on the sand"),
                        Activity(92, Calendar.getInstance().apply { set(2025, 12, 12) }, "21:00", true, "Beach party")
                    ),
                    Calendar.getInstance().apply { set(2025, 12, 13) } to listOf(
                        Activity(93, Calendar.getInstance().apply { set(2025, 12, 13) }, "08:00", true, "Morning yoga by the bay"),
                        Activity(94, Calendar.getInstance().apply { set(2025, 12, 13) }, "11:00", true, "Brunch & recap session"),
                        Activity(95, Calendar.getInstance().apply { set(2025, 12, 13) }, "14:00", false, "Last-minute shopping"),
                        Activity(96, Calendar.getInstance().apply { set(2025, 12, 13) }, "17:00", true, "Farewell drinks")
                    )
                ),
                reviews = emptyList()
            ),

            Trip(
                id = 109,
                photo = "rio",
                title = "Rio Rhythms & Rainforests",
                destination = "Rio de Janeiro, Brazil",
                startDate = Calendar.getInstance().apply { set(2026, 0, 10) },
                endDate = Calendar.getInstance().apply { set(2026, 0, 16) },
                estimatedPrice = 2100.0,
                groupSize = 6,
                creatorId = 2,
                participants = listOf(1, 2, 3, 5),
                appliedUsers = listOf(4, 6),
                published = true,
                typeTravel = listOf(TypeTravel.ADVENTURE, TypeTravel.PARTY),
                status = TripStatus.NOT_STARTED,
                activities = mapOf(
                    Calendar.getInstance().apply { set(2026, 0, 10) } to listOf(
                        Activity(100, Calendar.getInstance().apply { set(2026, 0, 10) }, "08:00", true, "Tijuca Forest hike"),
                        Activity(101, Calendar.getInstance().apply { set(2026, 0, 10) }, "12:00", true, "Street food tour"),
                        Activity(102, Calendar.getInstance().apply { set(2026, 0, 10) }, "15:00", false, "Beach volleyball at Copacabana"),
                        Activity(103, Calendar.getInstance().apply { set(2026, 0, 10) }, "20:00", true, "Samba night in Lapa")
                    ),
                    Calendar.getInstance().apply { set(2026, 0, 11) } to listOf(
                        Activity(104, Calendar.getInstance().apply { set(2026, 0, 11) }, "09:00", true, "Sugarloaf Mountain cable car"),
                        Activity(105, Calendar.getInstance().apply { set(2026, 0, 11) }, "13:00", true, "Traditional feijoada lunch"),
                        Activity(106, Calendar.getInstance().apply { set(2026, 0, 11) }, "16:00", false, "Ipanema shopping stroll"),
                        Activity(107, Calendar.getInstance().apply { set(2026, 0, 11) }, "21:00", true, "Live bossa nova show")
                    ),
                    Calendar.getInstance().apply { set(2026, 0, 12) } to listOf(
                        Activity(108, Calendar.getInstance().apply { set(2026, 0, 12) }, "08:30", true, "Christ the Redeemer visit"),
                        Activity(109, Calendar.getInstance().apply { set(2026, 0, 12) }, "12:30", true, "Lunch with a view at Santa Teresa"),
                        Activity(110, Calendar.getInstance().apply { set(2026, 0, 12) }, "15:30", false, "Art museum visit"),
                        Activity(111, Calendar.getInstance().apply { set(2026, 0, 12) }, "19:00", true, "Samba dance workshop")
                    ),
                    Calendar.getInstance().apply { set(2026, 0, 13) } to listOf(
                        Activity(112, Calendar.getInstance().apply { set(2026, 0, 13) }, "09:00", true, "Favela walking tour"),
                        Activity(113, Calendar.getInstance().apply { set(2026, 0, 13) }, "13:00", true, "Local market tasting"),
                        Activity(114, Calendar.getInstance().apply { set(2026, 0, 13) }, "16:00", false, "Chill at Leblon beach"),
                        Activity(115, Calendar.getInstance().apply { set(2026, 0, 13) }, "20:00", true, "Evening cruise on Guanabara Bay")
                    ),
                    Calendar.getInstance().apply { set(2026, 0, 14) } to listOf(
                        Activity(116, Calendar.getInstance().apply { set(2026, 0, 14) }, "10:00", true, "Hang gliding experience"),
                        Activity(117, Calendar.getInstance().apply { set(2026, 0, 14) }, "13:00", true, "Fresh seafood lunch"),
                        Activity(118, Calendar.getInstance().apply { set(2026, 0, 14) }, "16:00", false, "Free exploration time"),
                        Activity(119, Calendar.getInstance().apply { set(2026, 0, 14) }, "22:00", true, "Rio nightlife crawl")
                    ),
                    Calendar.getInstance().apply { set(2026, 0, 15) } to listOf(
                        Activity(120, Calendar.getInstance().apply { set(2026, 0, 15) }, "08:00", true, "Morning yoga by the beach"),
                        Activity(121, Calendar.getInstance().apply { set(2026, 0, 15) }, "11:00", true, "Street art tour"),
                        Activity(122, Calendar.getInstance().apply { set(2026, 0, 15) }, "15:00", false, "Sunset photo session"),
                        Activity(123, Calendar.getInstance().apply { set(2026, 0, 15) }, "19:00", true, "Dinner & storytelling night")
                    ),
                    Calendar.getInstance().apply { set(2026, 0, 16) } to listOf(
                        Activity(124, Calendar.getInstance().apply { set(2026, 0, 16) }, "09:00", true, "Botanical garden tour"),
                        Activity(125, Calendar.getInstance().apply { set(2026, 0, 16) }, "12:00", true, "Farewell brunch"),
                        Activity(126, Calendar.getInstance().apply { set(2026, 0, 16) }, "14:00", false, "Last-minute shopping"),
                        Activity(127, Calendar.getInstance().apply { set(2026, 0, 16) }, "17:00", true, "Trip recap & goodbye circle")
                    )
                ),
                reviews = emptyList()
            ),

            Trip(
                id = 110,
                photo = "monaco",
                title = "Monaco Luxe Experience",
                destination = "Monaco",
                startDate = Calendar.getInstance().apply { set(2026, 4, 5) },
                endDate = Calendar.getInstance().apply { set(2026, 4, 11) },
                estimatedPrice = 4600.0,
                groupSize = 4,
                creatorId = 6,
                participants = listOf(2, 4, 6),
                appliedUsers = listOf(1, 5),
                published = true,
                typeTravel = listOf(TypeTravel.RELAX, TypeTravel.CULTURE),
                status = TripStatus.NOT_STARTED,
                activities = mapOf(
                    Calendar.getInstance().apply { set(2026, 4, 5) } to listOf(
                        Activity(130, Calendar.getInstance().apply { set(2026, 4, 5) }, "09:00", true, "Breakfast at Café de Paris"),
                        Activity(131, Calendar.getInstance().apply { set(2026, 4, 5) }, "11:00", true, "Tour of the Prince’s Palace"),
                        Activity(132, Calendar.getInstance().apply { set(2026, 4, 5) }, "15:00", false, "Free time in Monte Carlo"),
                        Activity(133, Calendar.getInstance().apply { set(2026, 4, 5) }, "20:00", true, "Gala dinner at Hôtel de Paris")
                    ),
                    Calendar.getInstance().apply { set(2026, 4, 6) } to listOf(
                        Activity(134, Calendar.getInstance().apply { set(2026, 4, 6) }, "10:00", true, "Oceanographic Museum visit"),
                        Activity(135, Calendar.getInstance().apply { set(2026, 4, 6) }, "13:00", true, "Harbor-side lunch"),
                        Activity(136, Calendar.getInstance().apply { set(2026, 4, 6) }, "16:00", false, "Spa & relaxation"),
                        Activity(137, Calendar.getInstance().apply { set(2026, 4, 6) }, "21:00", true, "Casino Royale night")
                    ),
                    Calendar.getInstance().apply { set(2026, 4, 7) } to listOf(
                        Activity(138, Calendar.getInstance().apply { set(2026, 4, 7) }, "09:00", true, "Guided yacht tour"),
                        Activity(139, Calendar.getInstance().apply { set(2026, 4, 7) }, "12:30", true, "Seafood tasting aboard"),
                        Activity(140, Calendar.getInstance().apply { set(2026, 4, 7) }, "15:00", false, "Relax at private beach"),
                        Activity(141, Calendar.getInstance().apply { set(2026, 4, 7) }, "19:00", true, "Dinner cruise at sunset")
                    ),
                    Calendar.getInstance().apply { set(2026, 4, 8) } to listOf(
                        Activity(142, Calendar.getInstance().apply { set(2026, 4, 8) }, "10:00", true, "Perfume workshop in Eze"),
                        Activity(143, Calendar.getInstance().apply { set(2026, 4, 8) }, "13:00", true, "Hilltop village lunch"),
                        Activity(144, Calendar.getInstance().apply { set(2026, 4, 8) }, "17:00", false, "Photography walk"),
                        Activity(145, Calendar.getInstance().apply { set(2026, 4, 8) }, "21:00", true, "Fine wine & jazz night")
                    ),
                    Calendar.getInstance().apply { set(2026, 4, 9) } to listOf(
                        Activity(146, Calendar.getInstance().apply { set(2026, 4, 9) }, "09:00", true, "Luxury shopping experience"),
                        Activity(147, Calendar.getInstance().apply { set(2026, 4, 9) }, "12:00", true, "Lunch at Le Louis XV"),
                        Activity(148, Calendar.getInstance().apply { set(2026, 4, 9) }, "15:00", false, "Relaxation by hotel pool"),
                        Activity(149, Calendar.getInstance().apply { set(2026, 4, 9) }, "19:00", true, "Cultural performance night")
                    ),
                    Calendar.getInstance().apply { set(2026, 4, 10) } to listOf(
                        Activity(150, Calendar.getInstance().apply { set(2026, 4, 10) }, "08:30", true, "Helicopter tour of Côte d'Azur"),
                        Activity(151, Calendar.getInstance().apply { set(2026, 4, 10) }, "12:00", true, "Lunch in Nice"),
                        Activity(152, Calendar.getInstance().apply { set(2026, 4, 10) }, "15:30", false, "Art gallery visit"),
                        Activity(153, Calendar.getInstance().apply { set(2026, 4, 10) }, "20:00", true, "Final night celebration")
                    ),
                    Calendar.getInstance().apply { set(2026, 4, 11) } to listOf(
                        Activity(154, Calendar.getInstance().apply { set(2026, 4, 11) }, "09:00", true, "Farewell breakfast"),
                        Activity(155, Calendar.getInstance().apply { set(2026, 4, 11) }, "11:00", true, "Wrap-up and packing"),
                        Activity(156, Calendar.getInstance().apply { set(2026, 4, 11) }, "13:00", false, "Optional solo stroll"),
                        Activity(157, Calendar.getInstance().apply { set(2026, 4, 11) }, "15:00", true, "Final group toast")
                    )
                ),
                reviews = emptyList()
            )
        )
    )
    var tripList: StateFlow<List<Trip>> = _tripList

    private val _publishedTrips = MutableStateFlow<List<Trip>>(emptyList())
    val publishedTrips: StateFlow<List<Trip>> = _publishedTrips

    private val _privateTrips = MutableStateFlow<List<Trip>>(emptyList())
    val privateTrips: StateFlow<List<Trip>> = _privateTrips

    //TripList Business Logic
    fun filterPublishedByCreator(id: Int): List<Trip> {
        _publishedTrips.value = _tripList.value.filter { it.creatorId == id && it.published }
        return _publishedTrips.value
    }

    fun filterPrivateByCreator(id: Int): List<Trip> {
        _privateTrips.value = _tripList.value.filter { it.creatorId == id && !it.published }
        return _privateTrips.value
    }
}

