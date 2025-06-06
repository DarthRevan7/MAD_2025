package com.example.voyago.model

import com.example.voyago.model.Trip.Activity
import com.example.voyago.model.Trip.Participant
import com.example.voyago.model.Trip.TripStatus
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Calendar

fun Calendar.stripTime(): Calendar {
    this.set(Calendar.HOUR_OF_DAY, 0)
    this.set(Calendar.MINUTE, 0)
    this.set(Calendar.SECOND, 0)
    this.set(Calendar.MILLISECOND, 0)
    return this
}

val reviewModel = ReviewModel()

val userModel = UserModel()

//USER LIST
var privateUsers = MutableStateFlow<List<UserData>>(
    listOf(
        UserData(
            id = 1,
            firstname = "Alice",
            surname = "Walker",
            username = "alice_w",
            dateOfBirth = Calendar.getInstance().apply { set(1995, 6, 10); stripTime() },
            country = "USA",
            email = "alice@example.com",
            password = "securePassword123",
            userDescription = "Globetrotter and sunset chaser. Always looking for the next quiet beach or bustling city",
            profilePicture = null,
            typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.ADVENTURE),
            desiredDestination = listOf("Greece", "Italy", "Japan"),
            rating = 5.0f,
            reliability = 90
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
            userDescription = "Spa days or city lights—I do both. Always chasing good vibes, bold flavors, and unforgettable nights. Let’s dance, then unwind in style.",
            profilePicture = null,
            typeTravel = listOf(TypeTravel.RELAX, TypeTravel.PARTY),
            desiredDestination = listOf("Romania", "USA", "South Korea"),
            rating = 5.0f,
            reliability = 55
        ),

        UserData(
            id = 3,
            firstname = "Liam",
            surname = "Hunter",
            username = "globetrotliam",
            dateOfBirth = Calendar.getInstance().apply { set(1990, 5, 21); stripTime() },
            country = "Canada",
            email = "liam.hunter@example.com",
            password = "canuckTravels123",
            userDescription = "Adventurer and culture seeker",
            profilePicture = null,
            typeTravel = listOf(TypeTravel.ADVENTURE, TypeTravel.CULTURE),
            desiredDestination = listOf("Peru", "Nepal", "Morocco"),
            rating = 5.0f,
            reliability = 80
        ),

        UserData(
            id = 4,
            firstname = "Sofia",
            surname = "Mendoza",
            username = "sofi_explorer",
            dateOfBirth = Calendar.getInstance().apply { set(1993, 2, 14); stripTime() },
            country = "Argentina",
            email = "sofia.mendoza@example.com",
            password = "andesLover2021",
            userDescription = "Love hiking and photographing landscapes",
            profilePicture = null,
            typeTravel = listOf(TypeTravel.ADVENTURE),
            desiredDestination = listOf("New Zealand", "Iceland", "Japan"),
            rating = 5.0f,
            reliability = 73
        ),

        UserData(
            id = 5,
            firstname = "Ethan",
            surname = "Nguyen",
            username = "ethan_nomad",
            dateOfBirth = Calendar.getInstance().apply { set(1988, 7, 9); stripTime() },
            country = "USA",
            email = "ethan.nguyen@example.com",
            password = "secureWay888",
            userDescription = "Foodie with a passion for street markets",
            profilePicture = null,
            typeTravel = listOf(TypeTravel.CULTURE),
            desiredDestination = listOf("Thailand", "Italy", "Vietnam"),
            rating = 5.0f,
            reliability = 67
        ),

        UserData(
            id = 6,
            firstname = "Aisha",
            surname = "Khan",
            username = "aishatravels",
            dateOfBirth = Calendar.getInstance().apply { set(1995, 12, 3); stripTime() },
            country = "Pakistan",
            email = "aisha.khan@example.com",
            password = "safeJourney999",
            userDescription = "Solo traveler sharing stories from around the world",
            profilePicture = null,
            typeTravel = listOf(TypeTravel.RELAX, TypeTravel.CULTURE),
            desiredDestination = listOf("Turkey", "Spain", "Malaysia"),
            rating = 5.0f,
            reliability = 90
        )
    )
)


//TRIP LIST
var privateTripList = MutableStateFlow<List<Trip>>(
    listOf(
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
            participants = mapOf(
                6 to Trip.JoinRequest(userId = 6, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                2 to Trip.JoinRequest(userId = 2, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                3 to Trip.JoinRequest(userId = 3, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                1 to Trip.JoinRequest(userId = 1, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList())
            ),

            appliedUsers = emptyMap(),
            rejectedUsers = mapOf(
                5 to Trip.JoinRequest(userId = 5, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                4 to Trip.JoinRequest(userId = 4, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
            ),
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
            )
        ),

        Trip(
            id = 2,
            photo = "phuket",
            title = "Beach Escape in Thailand",
            destination = "Phuket",
            startDate = Calendar.getInstance().apply { set(2024, 5, 20); stripTime() },
            endDate = Calendar.getInstance().apply { set(2024, 5, 21); stripTime() },
            estimatedPrice = 1200.0,
            groupSize = 4,
            creatorId = 2,
            participants = mapOf(
                2 to Trip.JoinRequest(userId = 2, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                3 to Trip.JoinRequest(userId = 3, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                5 to Trip.JoinRequest(userId = 5, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                1 to Trip.JoinRequest(userId = 1, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList())
            ),
            appliedUsers = emptyMap(),
            rejectedUsers = mapOf(
                6 to Trip.JoinRequest(userId = 6, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                4 to Trip.JoinRequest(userId = 4, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
            ),
            published = true,
            typeTravel = listOf(TypeTravel.PARTY, TypeTravel.RELAX),
            status = TripStatus.COMPLETED,
            activities = mapOf(
                Calendar.getInstance().apply { set(2024, 5, 20); stripTime() } to listOf(
                    Activity(
                        9,
                        Calendar.getInstance().apply { set(2024, 5, 20); stripTime() },
                        "10:00 AM",
                        true,
                        "Snorkeling tour"
                    ),
                    Activity(
                        10,
                        Calendar.getInstance().apply { set(2024, 5, 20); stripTime() },
                        "01:00 PM",
                        true,
                        "Beach lunch"
                    ),
                    Activity(
                        11,
                        Calendar.getInstance().apply { set(2024, 5, 20); stripTime() },
                        "04:00 PM",
                        false,
                        "Massage session"
                    ),
                    Activity(
                        12,
                        Calendar.getInstance().apply { set(2024, 5, 20); stripTime() },
                        "07:00 PM",
                        true,
                        "Sunset cruise"
                    )
                ),
                Calendar.getInstance().apply { set(2024, 5, 21); stripTime() } to listOf(
                    Activity(
                        13,
                        Calendar.getInstance().apply { set(2024, 5, 21); stripTime() },
                        "09:00 AM",
                        true,
                        "Island hopping"
                    ),
                    Activity(
                        14,
                        Calendar.getInstance().apply { set(2024, 5, 21); stripTime() },
                        "02:00 PM",
                        true,
                        "Cuisine tasting"
                    ),
                    Activity(
                        15,
                        Calendar.getInstance().apply { set(2024, 5, 21); stripTime() },
                        "03:00 PM",
                        false,
                        "Shopping"
                    ),
                    Activity(
                        16,
                        Calendar.getInstance().apply { set(2024, 5, 21); stripTime() },
                        "08:00 PM",
                        true,
                        "Beach party"
                    )
                )
            )
        ),


        Trip(
            id = 3,
            photo = "peru",
            title = "Adventures in Peru",
            destination = "Cusco",
            startDate = Calendar.getInstance().apply { set(2025, 9, 22); stripTime() },
            endDate = Calendar.getInstance().apply { set(2025, 9, 6); stripTime() },
            estimatedPrice = 1100.0,
            groupSize = 4,
            creatorId = 3,
            participants = mapOf(
                3 to Trip.JoinRequest(userId = 3, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                1 to Trip.JoinRequest(userId = 1, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                4 to Trip.JoinRequest(userId = 4, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                5 to Trip.JoinRequest(userId = 5, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList())
            ),
            appliedUsers = emptyMap(),
            rejectedUsers = mapOf(
                6 to Trip.JoinRequest(userId = 6, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                2 to Trip.JoinRequest(userId = 2, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
            ),
            published = true,
            typeTravel = listOf(TypeTravel.ADVENTURE, TypeTravel.CULTURE),
            status = TripStatus.NOT_STARTED,
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
            )
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
            participants = mapOf(
                1 to Trip.JoinRequest(userId = 1, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList())
            ),
            appliedUsers = emptyMap(),
            rejectedUsers = emptyMap(),
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
            )
        ),

        Trip(
            id = 101,
            photo = "kyoto",
            title = "Kyoto Zen Retreat",
            destination = "Kyoto",
            startDate = Calendar.getInstance().apply { set(2025, 10, 1); stripTime() },
            endDate = Calendar.getInstance().apply { set(2025, 10, 4); stripTime() },
            estimatedPrice = 1650.0,
            groupSize = 5,
            creatorId = 1,
            participants = mapOf(
                1 to Trip.JoinRequest(userId = 1, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                2 to Trip.JoinRequest(userId = 2, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList())
            ),
            appliedUsers = mapOf(
                4 to Trip.JoinRequest(userId = 4, requestedSpots = 3, unregisteredParticipants = listOf(Participant(name = "Greta", surname = "Williams", email = "gretawilliams@gmail.com")), registeredParticipants = listOf(6)),
                5 to Trip.JoinRequest(userId = 5, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList())
            ),
            rejectedUsers = mapOf(
                3 to Trip.JoinRequest(userId = 3, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList())

            ),
            published = true,
            typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.RELAX),
            status = TripStatus.NOT_STARTED,
            activities = mapOf(
                Calendar.getInstance().apply { set(2025, 10, 1); stripTime() } to listOf(
                    Activity(65, Calendar.getInstance().apply { set(2025, 10, 1); stripTime() }, "08:00 AM", true, "Zen meditation at Ryōan-ji"),
                    Activity(66, Calendar.getInstance().apply { set(2025, 10, 1); stripTime() }, "11:00 AM", true, "Stroll through Philosopher’s Path"),
                    Activity(67, Calendar.getInstance().apply { set(2025, 10, 1); stripTime() }, "02:00 PM", false, "Matcha tasting"),
                    Activity(68, Calendar.getInstance().apply { set(2025, 10, 1); stripTime() }, "06:00 PM", true, "Kaiseki dinner at local ryokan")
                ),
                Calendar.getInstance().apply { set(2025, 10, 2); stripTime() } to listOf(
                    Activity(69, Calendar.getInstance().apply { set(2025, 10, 2); stripTime() }, "09:00 AM", true, "Visit Fushimi Inari shrine"),
                    Activity(70, Calendar.getInstance().apply { set(2025, 10, 2); stripTime() }, "02:00 PM", true, "Tofu lunch in Gion"),
                    Activity(71, Calendar.getInstance().apply { set(2025, 10, 2); stripTime() }, "03:00 PM", false, "Tea ceremony workshop"),
                    Activity(72, Calendar.getInstance().apply { set(2025, 10, 2); stripTime() }, "07:00 PM", true, "Night walk in Gion district")
                ),
                Calendar.getInstance().apply { set(2025, 10, 3); stripTime() } to listOf(
                    Activity(73, Calendar.getInstance().apply { set(2025, 10, 3); stripTime() }, "08:00 AM", true, "Arashiyama Bamboo Grove walk"),
                    Activity(74, Calendar.getInstance().apply { set(2025, 10, 3); stripTime() }, "11:00 AM", true, "River boat ride"),
                    Activity(75, Calendar.getInstance().apply { set(2025, 10, 3); stripTime() }, "02:00 PM", false, "Zen garden journaling"),
                    Activity(76, Calendar.getInstance().apply { set(2025, 10, 3); stripTime() }, "06:00 PM", true, "Dinner & sake tasting")
                ),
                Calendar.getInstance().apply { set(2025, 10, 4); stripTime() } to listOf(
                    Activity(77, Calendar.getInstance().apply { set(2025, 10, 4); stripTime() }, "09:00 AM", true, "Kinkaku-ji visit"),
                    Activity(78, Calendar.getInstance().apply { set(2025, 10, 4); stripTime() }, "02:00 PM", true, "Farewell sushi lunch"),
                    Activity(79, Calendar.getInstance().apply { set(2025, 10, 4); stripTime() }, "03:00 PM", false, "Free time & shopping"),
                    Activity(80, Calendar.getInstance().apply { set(2025, 10, 4); stripTime() }, "06:00 PM", true, "Closing circle reflection")
                )
            )
        ),

        Trip(
            id = 6,
            photo = "sydney",
            title = "Sydney Coastal Explorer",
            destination = "Sydney",
            startDate = Calendar.getInstance().apply { set(2025, 11, 10); stripTime() },
            endDate = Calendar.getInstance().apply { set(2025, 11, 13); stripTime() },
            estimatedPrice = 1900.0,
            groupSize = 6,
            creatorId = 4,
            participants = mapOf(
                4 to Trip.JoinRequest(userId = 4, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                2 to Trip.JoinRequest(userId = 2, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                5 to Trip.JoinRequest(userId = 5, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                6 to Trip.JoinRequest(userId = 6, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList())
            ),
            appliedUsers = mapOf(
                1 to Trip.JoinRequest(userId = 1, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                3 to Trip.JoinRequest(userId = 3, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList())
            ),
            rejectedUsers = emptyMap(),
            published = true,
            typeTravel = listOf(TypeTravel.ADVENTURE, TypeTravel.PARTY),
            status = TripStatus.NOT_STARTED,
            activities = mapOf(
                Calendar.getInstance().apply { set(2025, 11, 10) } to listOf(
                    Activity(81, Calendar.getInstance().apply { set(2025, 11, 10); stripTime() }, "09:00 AM", true, "Bondi to Coogee coastal walk"),
                    Activity(82, Calendar.getInstance().apply { set(2025, 11, 10); stripTime() }, "02:00 PM", true, "Beachside BBQ lunch"),
                    Activity(83, Calendar.getInstance().apply { set(2025, 11, 10); stripTime() }, "03:00 PM", false, "Surfing intro class"),
                    Activity(84, Calendar.getInstance().apply { set(2025, 11, 10); stripTime() }, "08:00 PM", true, "Rooftop drinks downtown")
                ),
                Calendar.getInstance().apply { set(2025, 11, 11) } to listOf(
                    Activity(85, Calendar.getInstance().apply { set(2025, 11, 11); stripTime() }, "08:00 AM", true, "Harbour Bridge climb"),
                    Activity(86, Calendar.getInstance().apply { set(2025, 11, 11); stripTime() }, "11:00 AM", true, "Opera House guided tour"),
                    Activity(87, Calendar.getInstance().apply { set(2025, 11, 11); stripTime() }, "02:00 PM", false, "Museum of Contemporary Art visit"),
                    Activity(88, Calendar.getInstance().apply { set(2025, 11, 11); stripTime() }, "07:00 PM", true, "Sunset dinner cruise")
                ),
                Calendar.getInstance().apply { set(2025, 11, 12) } to listOf(
                    Activity(89, Calendar.getInstance().apply { set(2025, 11, 12); stripTime() }, "10:00 AM", true, "Ferry to Manly Beach"),
                    Activity(90, Calendar.getInstance().apply { set(2025, 11, 12); stripTime() }, "01:00 PM", true, "Seafood lunch at the wharf"),
                    Activity(91, Calendar.getInstance().apply { set(2025, 11, 12); stripTime() }, "04:00 PM", false, "Relax on the sand"),
                    Activity(92, Calendar.getInstance().apply { set(2025, 11, 12); stripTime() }, "09:00 PM", true, "Beach party")
                ),
                Calendar.getInstance().apply { set(2025, 11, 13) } to listOf(
                    Activity(93, Calendar.getInstance().apply { set(2025, 11, 13); stripTime() }, "08:00 AM", true, "Morning yoga by the bay"),
                    Activity(94, Calendar.getInstance().apply { set(2025, 11, 13); stripTime() }, "11:00 AM", true, "Brunch & recap session"),
                    Activity(95, Calendar.getInstance().apply { set(2025, 11, 13); stripTime() }, "02:00 PM", false, "Last-minute shopping"),
                    Activity(96, Calendar.getInstance().apply { set(2025, 11, 13); stripTime() }, "05:00 PM", true, "Farewell drinks")
                )
            )
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
            participants = mapOf(
                2 to Trip.JoinRequest(userId = 2, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                3 to Trip.JoinRequest(userId = 3, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                5 to Trip.JoinRequest(userId = 5, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList())
            ),
            appliedUsers = mapOf(
                4 to Trip.JoinRequest(userId = 4, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                6 to Trip.JoinRequest(userId = 6, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList())
            ),
            rejectedUsers = emptyMap(),
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
            )
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
            participants = mapOf(
                2 to Trip.JoinRequest(userId = 2, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                4 to Trip.JoinRequest(userId = 4, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList()),
                6 to Trip.JoinRequest(userId = 6, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList())
            ),
            appliedUsers = mapOf(
                5 to Trip.JoinRequest(userId = 5, requestedSpots = 1, unregisteredParticipants = emptyList(), registeredParticipants = emptyList())
            ),
            rejectedUsers = emptyMap(),
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
            )
        )
    )
)

//Review list
var privateReviews = MutableStateFlow<List<Review>>(
    listOf(
        Review(
            reviewId = 1,
            tripId = 1,
            isTripReview = true,
            reviewerId = 1,
            reviewedUserId = -1,
            title = "Amazing trip!",
            comment = "This trip was absolutely incredible from start to finish. The guided city tour was informative and fun, the food was delicious, and the museum visit was a highlight for me. Everything was well-organized and the group dynamic was awesome. I would recommend this experience to anyone wanting a deep cultural immersion.",
            score = 9,
            photos = listOf("gaudi", "barcelona", "mountrocky"),
            date = Calendar.getInstance().apply { set(2026, 4, 10); stripTime() }
        ),
        Review(
            reviewId = 2,
            tripId = 1,
            isTripReview = true,
            reviewerId = 2,
            reviewedUserId = -1,
            title = "Great experience",
            comment = "Barcelona was a dream destination and this trip made it even better. I loved how the itinerary was balanced with both group activities and personal time. The hike on day two was a bit challenging but totally worth it for the views. I came back with great memories and new friends.",
            score = 8,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2026, 4, 10); stripTime() }
        ),
        Review(
            reviewId = 3,
            tripId = 1,
            isTripReview = true,
            reviewerId = 3,
            reviewedUserId = -1,
            title = "Would go again",
            comment = "I’m really impressed by how well this trip was planned. Every activity had a purpose, and even the free time was suggested with local tips. The welcome dinner was a beautiful introduction to Spanish culture, and the entire experience felt authentic and enriching. 10/10 would do it again.",
            score = 10,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2026, 4, 10); stripTime() }
        ),
        Review(
            reviewId = 4,
            tripId = 1,
            isTripReview = true,
            reviewerId = 6,
            reviewedUserId = -1,
            title = "A Deep Dive into Barcelona’s Culture",
            comment = "This trip was an unforgettable experience! The balance between group activities and personal time was just right. From the rich history of the Gothic Quarter to the vibrant energy of the local markets, I felt immersed in the heart of Catalonia. The museum visit was a standout moment, and the hike followed by the mountain picnic on the second day was so refreshing. Our guide was knowledgeable, friendly, and kept the group engaged. I especially appreciated the campfire stories to end the trip — it gave everyone a chance to bond and reflect. Highly recommend this trip to anyone who enjoys a mix of culture, nature, and great company.",
            score = 9,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2024, 6, 15) }
        ),
        Review(
            reviewId = 5,
            tripId = 1,
            isTripReview = false,
            reviewerId = 6,
            reviewedUserId = -1,
            title = "Thoughtful and adventurous companion",
            comment = "Traveling with Alice was such a joy. She’s incredibly respectful of group dynamics and always made insightful observations about the places we visited. Whether we were exploring museums or hiking together, she brought a calm energy that was deeply appreciated. I especially loved our conversations during free time — she has a way of turning every small moment into something memorable. Would definitely travel with her again!",
            score = 10,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2024, 6, 14) }
        ),
        Review(
            reviewId = 6,
            tripId = 1,
            isTripReview = false,
            reviewerId = 6,
            reviewedUserId = 2,
            title = "Fun and high-spirited",
            comment = "Bella brought a lot of energy and spontaneity to the group. She was always up for trying new things and encouraged others to loosen up and enjoy the moment. I appreciated her bold spirit during our group dinner and how she kept conversations light and joyful. While she sometimes did her own thing, it never felt disruptive — in fact, her independence added flavor to the trip. Great to have her in a travel crew.",
            score = 8,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2024, 6, 14) }
        ),
        Review(
            reviewId = 7,
            tripId = 1,
            isTripReview = false,
            reviewerId = 6,
            reviewedUserId = 3,
            title = "Reliable and engaging traveler",
            comment = "Liam was a solid and dependable member of the trip. Always on time, always helpful, and always curious about the culture we were immersed in. I admired how he engaged with the local guides and asked thoughtful questions that enriched the experience for all of us. He’s the kind of travel partner who makes planning smoother and shared moments more meaningful. A true team player with a curious mind.",
            score = 9,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2024, 6, 14) }
        ),
        Review(
            reviewId = 8,
            tripId = 1,
            isTripReview = false,
            reviewerId = 1,
            reviewedUserId = 6,
            title = "Graceful and grounded",
            comment = "Aisha has a wonderfully calming presence and was such a delight to travel with. She brought depth to our group chats and was always genuinely interested in everyone’s perspectives. Whether we were exploring local markets or sharing stories around the campfire, she listened with intent and offered thoughtful reflections. She’s a solo traveler by heart, but she made our group feel like a close-knit circle.",
            score = 10,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2024, 6, 14) }
        ),
        Review(
            reviewId = 9,
            tripId = 1,
            isTripReview = false,
            reviewerId = 1,
            reviewedUserId = 2,
            title = "Vibrant and social",
            comment = "Bella added an exciting spark to the trip! She was always the first to start a conversation and bring some humor into the mix. Her energy helped us all feel more relaxed, and she never shied away from suggesting fun detours or photo ops. Even if we had slightly different travel styles, I appreciated her spontaneity and joyfulness.",
            score = 8,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2024, 6, 14) }
        ),
        Review(
            reviewId = 10,
            tripId = 1,
            isTripReview = false,
            reviewerId = 1,
            reviewedUserId = 3,
            title = "Passionate and thoughtful traveler",
            comment = "Liam has a true explorer’s heart. He brought so much knowledge and enthusiasm to every activity, especially during the museum visit and the hike. He was always courteous, helped others when needed, and sparked interesting conversations about history and culture. I really valued having him in the group — he made every shared experience richer.",
            score = 9,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2024, 6, 14) }
        ),
        Review(
            reviewId = 11,
            tripId = 1,
            isTripReview = false,
            reviewerId = 2,
            reviewedUserId = 6,
            title = "Kind and composed travel partner",
            comment = "Aisha was super sweet and always had something meaningful to say. She brought a lot of calm energy to the group and was really open to sharing stories. Loved having her around.",
            score = 9,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2024, 6, 14) }
        ),
        Review(
            reviewId = 12,
            tripId = 1,
            isTripReview = false,
            reviewerId = 2,
            reviewedUserId = 1,
            title = "Cool and observant",
            comment = "Alice was more quiet but always super friendly. She had great taste in spots to explore and was really easy to get along with. Would definitely hang out with her again on another trip.",
            score = 8,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2024, 6, 14) }
        ),
        Review(
            reviewId = 13,
            tripId = 1,
            isTripReview = false,
            reviewerId = 2,
            reviewedUserId = 3,
            title = "Adventurous and smart",
            comment = "Liam was super into the cultural parts of the trip and always asked cool questions. You could tell he really cared about learning and connecting with the places we visited. Great vibe.",
            score = 9,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2024, 6, 14) }
        ),
        Review(
            reviewId = 14,
            tripId = 1,
            isTripReview = false,
            reviewerId = 3,
            reviewedUserId = 6,
            title = "Inspiring travel companion",
            comment = "Aisha was calm, reflective, and brought a meaningful perspective to every moment of the trip. She was someone I found myself looking forward to chatting with at the end of each day. Truly one of the most insightful people I’ve met while traveling.",
            score = 9,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2024, 6, 14) }
        ),
        Review(
            reviewId = 15,
            tripId = 1,
            isTripReview = false,
            reviewerId = 3,
            reviewedUserId = 1,
            title = "Adventurous and dependable",
            comment = "Alice was organized, easygoing, and always ready to explore. She kept the group on track during our hike and shared great insights during our city walk. A reliable and fun person to travel with.",
            score = 10,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2024, 6, 14) }
        ),
        Review(
            reviewId = 16,
            tripId = 1,
            isTripReview = false,
            reviewerId = 3,
            reviewedUserId = 2,
            title = "Lively and social",
            comment = "Bella kept the energy high and made sure no one felt left out. She was full of creative ideas and always ready to enjoy the moment. If you're looking for someone who brings positive vibes to a group, she's it.",
            score = 8,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2024, 6, 14) }
        ),
        Review(
            reviewId = 17,
            tripId = 2,
            isTripReview = true,
            reviewerId = 2,
            reviewedUserId = -1,
            title = "Loved it!",
            comment = "This was the ultimate beach escape. The snorkeling tour showed us some of the most stunning coral reefs I’ve ever seen. The food was delicious, and the beach party was an unforgettable night with music, dancing, and laughter. It struck the perfect balance between adventure and relaxation.",
            score = 10,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2026, 4, 10); stripTime() }
        ),
        Review(
            reviewId = 18,
            tripId = 2,
            isTripReview = true,
            reviewerId = 3,
            reviewedUserId = -1,
            title = "Relaxing trip",
            comment = "I needed a break from work, and this trip delivered. From the moment we arrived, everything was taken care of. The massage session was heavenly, and the sunsets over the ocean were something out of a movie. I left feeling refreshed and truly happy. Would highly recommend for anyone seeking peace.",
            score = 9,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2026, 4, 10); stripTime() }
        ),
        Review(
            reviewId = 19,
            tripId = 2,
            isTripReview = true,
            reviewerId = 5,
            reviewedUserId = -1,
            title = "Beautiful place",
            comment = "Thailand was everything I imagined and more. The island hopping day was packed with activities, yet never felt rushed. The local cuisine tasting opened my eyes to so many flavors, and I even brought some recipes home. A wonderful way to experience the culture while soaking up the sun.",
            score = 8,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2026, 4, 10); stripTime() }
        ),
        Review(
            reviewId = 20,
            tripId = 2,
            isTripReview = false,
            reviewerId = 5,
            reviewedUserId = 1,
            title = "Awesome and funny",
            comment = "Good travel buddy",
            score = 8,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2026, 4, 10); stripTime() }
        ),
        Review(
            reviewId = 21,
            tripId = 2,
            isTripReview = false,
            reviewerId = 5,
            reviewedUserId = 2,
            title = "Beautiful place",
            comment = "Thailand was everything I imagined and more. The island hopping day was packed with activities, yet never felt rushed. The local cuisine tasting opened my eyes to so many flavors, and I even brought some recipes home. A wonderful way to experience the culture while soaking up the sun.",
            score = 8,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2026, 4, 10); stripTime() }
        ),
        Review(
            reviewId = 22,
            tripId = 2,
            isTripReview = false,
            reviewerId = 2,
            reviewedUserId = 1,
            title = "Reliable and energetic",
            comment = "Alice brought a nice balance to the group. She was always on time, enthusiastic, and had a real curiosity for the local culture. Definitely someone I’d travel with again.",
            score = 9,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2025, 5, 22) }
        ),
        Review(
            reviewId = 23,
            tripId = 2,
            isTripReview = false,
            reviewerId = 2,
            reviewedUserId = 3,
            title = "Adventurous spirit",
            comment = "Liam was up for anything—snorkeling, island hopping, you name it. His energy made the trip more exciting. Great vibe and very respectful of everyone’s pace.",
            score = 8,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2025, 5, 22) }
        ),
        Review(
            reviewId = 24,
            tripId = 2,
            isTripReview = false,
            reviewerId = 2,
            reviewedUserId = 5,
            title = "Quiet but curious",
            comment = "Ethan was observant and kind. He had great recommendations for food stops and local snacks. While he was more reserved, he always participated meaningfully.",
            score = 7,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2025, 5, 22) }
        ),
        Review(
            reviewId = 25,
            tripId = 2,
            isTripReview = false,
            reviewerId = 3,
            reviewedUserId = 1,
            title = "Uplifting and organized",
            comment = "Alice had a clear plan in mind and was always prepared, which helped the group move smoothly. Her positivity made the beach party even better. A great teammate.",
            score = 10,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2025, 5, 22) }
        ),
        Review(
            reviewId = 26,
            tripId = 2,
            isTripReview = false,
            reviewerId = 3,
            reviewedUserId = 2,
            title = "Fun and free-spirited",
            comment = "Bella brought all the fun to the Phuket nights. She danced, laughed, and made sure the group was enjoying themselves. Always up for a party, but never overstepped.",
            score = 9,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2025, 5, 22) }
        ),
        Review(
            reviewId = 27,
            tripId = 2,
            isTripReview = false,
            reviewerId = 3,
            reviewedUserId = 5,
            title = "Excellent food scout",
            comment = "Ethan was quiet but had an incredible sense for finding good food. He introduced us to amazing dishes during the cuisine tasting and was very thoughtful throughout.",
            score = 8,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2025, 5, 22) }
        ),
        Review(
            reviewId = 28,
            tripId = 2,
            isTripReview = false,
            reviewerId = 5,
            reviewedUserId = 1,
            title = "Supportive and attentive",
            comment = "Alice always checked in on everyone and made sure things were going well. She made the group feel connected and supported. I appreciated her calm attitude.",
            score = 9,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2025, 5, 22) }
        ),
        Review(
            reviewId = 29,
            tripId = 2,
            isTripReview = false,
            reviewerId = 5,
            reviewedUserId = 2,
            title = "Lively and fearless",
            comment = "Bella had a wild but enjoyable energy. From leading the dancing to cracking jokes during the cruise, she was a strong presence. Definitely made things more fun.",
            score = 8,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2025, 5, 22) }
        ),
        Review(
            reviewId = 30,
            tripId = 2,
            isTripReview = false,
            reviewerId = 5,
            reviewedUserId = 3,
            title = "Chill and reliable",
            comment = "Liam was super friendly and approachable. He was always open to conversations and helped organize our snorkeling gear when things got messy. Would travel with him again.",
            score = 9,
            photos = emptyList(),
            date = Calendar.getInstance().apply { set(2025, 5, 22) }
        )
    )
)

