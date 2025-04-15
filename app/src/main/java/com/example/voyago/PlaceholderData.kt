package com.example.voyago

import android.net.Uri
import com.example.voyago.model.*
import java.util.Calendar


fun TripFilter(dbList: List<Trip>, destination: String, minPrice:Float, maxPrice:Float, minDays:Int, maxDays:Int, minSize:Int, maxSize:Int, vararg typesTravel:TypeTravel, searchForCompleted:Boolean, minAvailableSeats:Int):List<Trip>
{
    var dbFiltrableList = dbList

    if(destination != "")
    {
        dbFiltrableList = FilterByDestination(dbFiltrableList, destination)
    }

    dbFiltrableList = FilterByPriceRange(dbFiltrableList, minPrice, maxPrice)
    dbFiltrableList = FilterByDuration(dbFiltrableList, minDays, maxDays)
    dbFiltrableList = FilterByGroupSize(dbFiltrableList, minSize, maxSize)
    dbFiltrableList = FilterByTripType(dbFiltrableList, *typesTravel)

    if(searchForCompleted)
        dbFiltrableList = FilterByCompletion(dbFiltrableList)

    if(minAvailableSeats > 0)
    {
        dbFiltrableList = FilterByAvailableSeats(dbFiltrableList, minAvailableSeats)
    }

    return dbFiltrableList



}
fun FilterByDestination(dbList:List<Trip>, destination:String): List<Trip>
{
    val filteredList:MutableList<Trip> = mutableListOf<Trip>()

    dbList.forEach {
            item ->
        if(item.destination == destination && destination != "")
        {
            filteredList.add(item)
        }
    }

    val trueFilteredList = filteredList as List<Trip>

    return trueFilteredList
}

fun FilterByPriceRange(dbList:List<Trip>, minPrice:Float, maxPrice:Float): List<Trip>
{
    val filteredList:MutableList<Trip> = mutableListOf<Trip>()

    dbList.forEach {
            item ->
        if(item.estimatedPrice in minPrice..maxPrice)
        {
            filteredList.add(item)
        }
    }

    val trueFilteredList = filteredList as List<Trip>

    return trueFilteredList
}

fun FilterByDuration(dbList:List<Trip>, minDays:Int, maxDays:Int): List<Trip>
{
    val filteredList:MutableList<Trip> = mutableListOf<Trip>()

    dbList.forEach {
            item ->
        if(item.tripDuration() in minDays.. maxDays)
        {
            filteredList.add(item)
        }
    }

    val trueFilteredList = filteredList as List<Trip>

    return trueFilteredList
}

fun FilterByGroupSize(dbList:List<Trip>, minSize:Int, maxSize:Int): List<Trip>
{
    val filteredList:MutableList<Trip> = mutableListOf<Trip>()

    dbList.forEach {
            item ->
        if(item.tripDuration() in minSize.. maxSize)
        {
            filteredList.add(item)
        }
    }

    val trueFilteredList = filteredList as List<Trip>

    return trueFilteredList
}

fun FilterByTripType(dbList:List<Trip>, vararg tripTypes:TypeTravel): List<Trip>
{
    val filteredList:MutableList<Trip> = mutableListOf<Trip>()

    if(tripTypes.isNotEmpty())
    {
        dbList.forEach {
                item ->
            tripTypes.forEach {
                    element ->
                if(item.typeTravel.contains(element))
                {
                    filteredList.add(item)
                }
            }
        }
    }

    val trueFilteredList = filteredList as List<Trip>

    return trueFilteredList
}

fun FilterByCompletion(dbList:List<Trip>): List<Trip>
{
    val filteredList:MutableList<Trip> = mutableListOf<Trip>()

    dbList.forEach {
            item ->
        if(item.isCompleted)
        {
            filteredList.add(item)
        }
    }

    val trueFilteredList = filteredList as List<Trip>

    return trueFilteredList
}

fun FilterByAvailableSeats(dbList:List<Trip>, minSeats:Int): List<Trip>
{
    val filteredList:MutableList<Trip> = mutableListOf<Trip>()

    dbList.forEach {
            item ->
        if(item.availableSpots() >= minSeats && minSeats != 0)
        {
            filteredList.add(item)
        }
    }

    val trueFilteredList = filteredList as List<Trip>

    return trueFilteredList
}



// Trip 1
val trip1StartDate = Calendar.getInstance().apply { set(2025, Calendar.MAY, 1) }
val trip1EndDate = Calendar.getInstance().apply { set(2025, Calendar.MAY, 5) }
val trip1Activities = mapOf(
    trip1StartDate to listOf(
        Trip.Activity(1, trip1StartDate.apply { set(2025, Calendar.MAY, 1, 10, 0) }, "10:00", true, "Arrival and welcome briefing"),
        Trip.Activity(2, trip1StartDate.apply { set(2025, Calendar.MAY, 1, 13, 0) }, "13:00", false, "City tour"),
        Trip.Activity(3, trip1StartDate.apply { set(2025, Calendar.MAY, 1, 16, 0) }, "16:00", true, "Dinner at local restaurant")
    ),
    trip1StartDate.apply { add(Calendar.DATE, 1) } to listOf(
        Trip.Activity(4, trip1StartDate.apply { set(2025, Calendar.MAY, 2, 9, 0) }, "09:00", true, "Museum visit"),
        Trip.Activity(5, trip1StartDate.apply { set(2025, Calendar.MAY, 2, 14, 0) }, "14:00", false, "Cooking class"),
        Trip.Activity(6, trip1StartDate.apply { set(2025, Calendar.MAY, 2, 19, 0) }, "19:00", true, "Nightlife exploration")
    ),
    trip1EndDate to listOf(
        Trip.Activity(7, trip1EndDate.apply { set(2025, Calendar.MAY, 5, 11, 0) }, "11:00", false, "Relaxing breakfast"),
        Trip.Activity(8, trip1EndDate.apply { set(2025, Calendar.MAY, 5, 15, 0) }, "15:00", true, "Farewell activity and greetings")
    )
)


// Trip 2
val trip2StartDate = Calendar.getInstance().apply { set(2025, Calendar.JUNE, 1) }
val trip2EndDate = Calendar.getInstance().apply { set(2025, Calendar.JUNE, 4) }
val trip2Activities = mapOf(
    trip2StartDate to listOf(
        Trip.Activity(9, trip2StartDate.apply { set(2025, Calendar.JUNE, 1, 10, 0) }, "10:00", true, "Welcome and introduction"),
        Trip.Activity(10, trip2StartDate.apply { set(2025, Calendar.JUNE, 1, 13, 0) }, "13:00", false, "Beach party"),
        Trip.Activity(11, trip2StartDate.apply { set(2025, Calendar.JUNE, 1, 17, 0) }, "17:00", true, "Barbecue dinner")
    ),
    trip2StartDate.apply { add(Calendar.DATE, 1) } to listOf(
        Trip.Activity(12, trip2StartDate.apply { set(2025, Calendar.JUNE, 2, 9, 0) }, "09:00", false, "Morning yoga session"),
        Trip.Activity(13, trip2StartDate.apply { set(2025, Calendar.JUNE, 2, 14, 0) }, "14:00", true, "Snorkeling adventure"),
        Trip.Activity(14, trip2StartDate.apply { set(2025, Calendar.JUNE, 2, 18, 0) }, "18:00", true, "Campfire and storytelling")
    ),
    trip2EndDate to listOf(
        Trip.Activity(15, trip2EndDate.apply { set(2025, Calendar.JUNE, 4, 10, 0) }, "10:00", false, "Sunrise walk"),
        Trip.Activity(16, trip2EndDate.apply { set(2025, Calendar.JUNE, 4, 15, 0) }, "15:00", true, "Farewell ceremony and final greetings")
    )
)

// Trip 3
val trip3StartDate = Calendar.getInstance().apply { set(2025, Calendar.JULY, 1) }
val trip3EndDate = Calendar.getInstance().apply { set(2025, Calendar.JULY, 6) }
val trip3Activities = mapOf(
    trip3StartDate to listOf(
        Trip.Activity(17, trip3StartDate.apply { set(2025, Calendar.JULY, 1, 10, 0) }, "10:00", true, "Welcome breakfast"),
        Trip.Activity(18, trip3StartDate.apply { set(2025, Calendar.JULY, 1, 13, 0) }, "13:00", true, "Hiking tour"),
        Trip.Activity(19, trip3StartDate.apply { set(2025, Calendar.JULY, 1, 18, 0) }, "18:00", false, "Campfire and stargazing")
    ),
    trip3StartDate.apply { add(Calendar.DATE, 1) } to listOf(
        Trip.Activity(20, trip3StartDate.apply { set(2025, Calendar.JULY, 2, 9, 0) }, "09:00", false, "Morning jog and stretch"),
        Trip.Activity(21, trip3StartDate.apply { set(2025, Calendar.JULY, 2, 14, 0) }, "14:00", true, "Water rafting"),
        Trip.Activity(22, trip3StartDate.apply { set(2025, Calendar.JULY, 2, 19, 0) }, "19:00", true, "Outdoor dinner")
    ),
    trip3EndDate to listOf(
        Trip.Activity(23, trip3EndDate.apply { set(2025, Calendar.JULY, 6, 9, 0) }, "09:00", false, "Morning yoga session"),
        Trip.Activity(24, trip3EndDate.apply { set(2025, Calendar.JULY, 6, 15, 0) }, "15:00", true, "Final group photo and goodbyes")
    )
)

// Trip 4
val trip4StartDate = Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 1) }
val trip4EndDate = Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 5) }
val trip4Activities = mapOf(
    trip4StartDate to listOf(
        Trip.Activity(25, trip4StartDate.apply { set(2025, Calendar.AUGUST, 1, 10, 0) }, "10:00", true, "Opening ceremony and icebreaker games"),
        Trip.Activity(26, trip4StartDate.apply { set(2025, Calendar.AUGUST, 1, 14, 0) }, "14:00", false, "Local art gallery visit"),
        Trip.Activity(27, trip4StartDate.apply { set(2025, Calendar.AUGUST, 1, 19, 0) }, "19:00", true, "Outdoor picnic under the stars")
    ),
    trip4StartDate.apply { add(Calendar.DATE, 1) } to listOf(
        Trip.Activity(28, trip4StartDate.apply { set(2025, Calendar.AUGUST, 2, 8, 0) }, "08:00", true, "Morning hike"),
        Trip.Activity(29, trip4StartDate.apply { set(2025, Calendar.AUGUST, 2, 13, 0) }, "13:00", false, "Cooking class"),
        Trip.Activity(30, trip4StartDate.apply { set(2025, Calendar.AUGUST, 2, 17, 0) }, "17:00", true, "Cultural dance performance")
    ),
    trip4EndDate to listOf(
        Trip.Activity(31, trip4EndDate.apply { set(2025, Calendar.AUGUST, 5, 9, 0) }, "09:00", false, "Final group meditation"),
        Trip.Activity(32, trip4EndDate.apply { set(2025, Calendar.AUGUST, 5, 14, 0) }, "14:00", true, "Goodbye and group photo session")
    )
)

// Trip 5
val trip5StartDate = Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 1) }
val trip5EndDate = Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 6) }
val trip5Activities = mapOf(
    trip5StartDate to listOf(
        Trip.Activity(33, trip5StartDate.apply { set(2025, Calendar.SEPTEMBER, 1, 10, 0) }, "10:00", true, "Welcome brunch"),
        Trip.Activity(34, trip5StartDate.apply { set(2025, Calendar.SEPTEMBER, 1, 14, 0) }, "14:00", true, "Safari tour"),
        Trip.Activity(35, trip5StartDate.apply { set(2025, Calendar.SEPTEMBER, 1, 18, 0) }, "18:00", false, "Sunset photography")
    ),
    trip5StartDate.apply { add(Calendar.DATE, 1) } to listOf(
        Trip.Activity(36, trip5StartDate.apply { set(2025, Calendar.SEPTEMBER, 2, 8, 0) }, "08:00", true, "Morning wildlife observation"),
        Trip.Activity(37, trip5StartDate.apply { set(2025, Calendar.SEPTEMBER, 2, 13, 0) }, "13:00", false, "Nature walk"),
        Trip.Activity(38, trip5StartDate.apply { set(2025, Calendar.SEPTEMBER, 2, 19, 0) }, "19:00", true, "Outdoor barbecue dinner")
    ),
    trip5EndDate to listOf(
        Trip.Activity(39, trip5EndDate.apply { set(2025, Calendar.SEPTEMBER, 6, 9, 0) }, "09:00", false, "Morning meditation session"),
        Trip.Activity(40, trip5EndDate.apply { set(2025, Calendar.SEPTEMBER, 6, 14, 0) }, "14:00", true, "Group photo and farewell")
    )
)

// Trip 6
val trip6StartDate = Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 1) }
val trip6EndDate = Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 5) }
val trip6Activities = mapOf(
    trip6StartDate to listOf(
        Trip.Activity(41, trip6StartDate.apply { set(2025, Calendar.OCTOBER, 1, 10, 0) }, "10:00", true, "Yoga and mindfulness session"),
        Trip.Activity(42, trip6StartDate.apply { set(2025, Calendar.OCTOBER, 1, 13, 0) }, "13:00", false, "Vineyard tour and wine tasting"),
        Trip.Activity(43, trip6StartDate.apply { set(2025, Calendar.OCTOBER, 1, 18, 0) }, "18:00", true, "Dinner at a local winery")
    ),
    trip6StartDate.apply { add(Calendar.DATE, 1) } to listOf(
        Trip.Activity(44, trip6StartDate.apply { set(2025, Calendar.OCTOBER, 2, 9, 0) }, "09:00", true, "Meditation and nature walk"),
        Trip.Activity(45, trip6StartDate.apply { set(2025, Calendar.OCTOBER, 2, 14, 0) }, "14:00", true, "Olive oil tasting and cooking class"),
        Trip.Activity(46, trip6StartDate.apply { set(2025, Calendar.OCTOBER, 2, 19, 0) }, "19:00", true, "Traditional Greek feast")
    ),
    trip6EndDate to listOf(
        Trip.Activity(47, trip6EndDate.apply { set(2025, Calendar.OCTOBER, 5, 9, 0) }, "09:00", false, "Group reflection"),
        Trip.Activity(48, trip6EndDate.apply { set(2025, Calendar.OCTOBER, 5, 14, 0) }, "14:00", true, "Goodbye and group photo")
    )
)

// Trip 7
val trip7StartDate = Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 10) }
val trip7EndDate = Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 14) }
val trip7Activities = mapOf(
    trip7StartDate to listOf(
        Trip.Activity(49, trip7StartDate.apply { set(2025, Calendar.NOVEMBER, 10, 10, 0) }, "10:00", true, "Arrival and introduction"),
        Trip.Activity(50, trip7StartDate.apply { set(2025, Calendar.NOVEMBER, 10, 14, 0) }, "14:00", true, "Rock climbing experience"),
        Trip.Activity(51, trip7StartDate.apply { set(2025, Calendar.NOVEMBER, 10, 18, 0) }, "18:00", true, "Campfire dinner")
    ),
    trip7StartDate.apply { add(Calendar.DATE, 1) } to listOf(
        Trip.Activity(52, trip7StartDate.apply { set(2025, Calendar.NOVEMBER, 11, 9, 0) }, "09:00", false, "Breakfast hike"),
        Trip.Activity(53, trip7StartDate.apply { set(2025, Calendar.NOVEMBER, 11, 14, 0) }, "14:00", true, "Waterfall exploration"),
        Trip.Activity(54, trip7StartDate.apply { set(2025, Calendar.NOVEMBER, 11, 19, 0) }, "19:00", false, "Evening meditation")
    ),
    trip7EndDate to listOf(
        Trip.Activity(55, trip7EndDate.apply { set(2025, Calendar.NOVEMBER, 14, 9, 0) }, "09:00", true, "Final hike"),
        Trip.Activity(56, trip7EndDate.apply { set(2025, Calendar.NOVEMBER, 14, 14, 0) }, "14:00", true, "Final group photo and farewell")
    )
)

// Trip 8
val trip8StartDate = Calendar.getInstance().apply { set(2025, Calendar.DECEMBER, 5) }
val trip8EndDate = Calendar.getInstance().apply { set(2025, Calendar.DECEMBER, 9) }
val trip8Activities = mapOf(
    trip8StartDate to listOf(
        Trip.Activity(57, trip8StartDate.apply { set(2025, Calendar.DECEMBER, 5, 10, 0) }, "10:00", true, "Guided city tour"),
        Trip.Activity(58, trip8StartDate.apply { set(2025, Calendar.DECEMBER, 5, 14, 0) }, "14:00", false, "Cultural cooking class"),
        Trip.Activity(59, trip8StartDate.apply { set(2025, Calendar.DECEMBER, 5, 19, 0) }, "19:00", true, "Authentic cultural dinner")
    ),
    trip8StartDate.apply { add(Calendar.DATE, 1) } to listOf(
        Trip.Activity(60, trip8StartDate.apply { set(2025, Calendar.DECEMBER, 6, 9, 0) }, "09:00", true, "Morning photography walk"),
        Trip.Activity(61, trip8StartDate.apply { set(2025, Calendar.DECEMBER, 6, 13, 0) }, "13:00", false, "Visit local craftsman shops"),
        Trip.Activity(62, trip8StartDate.apply { set(2025, Calendar.DECEMBER, 6, 18, 0) }, "18:00", true, "Farewell celebration dinner")
    ),
    trip8EndDate to listOf(
        Trip.Activity(63, trip8EndDate.apply { set(2025, Calendar.DECEMBER, 9, 9, 0) }, "09:00", false, "Reflection and group feedback"),
        Trip.Activity(64, trip8EndDate.apply { set(2025, Calendar.DECEMBER, 9, 14, 0) }, "14:00", true, "Goodbye and final group photo")
    )
)

// Trip 9
val trip9StartDate = Calendar.getInstance().apply { set(2025, Calendar.JANUARY, 15) }
val trip9EndDate = Calendar.getInstance().apply { set(2025, Calendar.JANUARY, 20) }
val trip9Activities = mapOf(
    trip9StartDate to listOf(
        Trip.Activity(65, trip9StartDate.apply { set(2025, Calendar.JANUARY, 15, 10, 0) }, "10:00", true, "Arrival and orientation"),
        Trip.Activity(66, trip9StartDate.apply { set(2025, Calendar.JANUARY, 15, 13, 0) }, "13:00", true, "Helicopter tour of the city"),
        Trip.Activity(67, trip9StartDate.apply { set(2025, Calendar.JANUARY, 15, 18, 0) }, "18:00", false, "Luxury dinner cruise")
    ),
    trip9StartDate.apply { add(Calendar.DATE, 1) } to listOf(
        Trip.Activity(68, trip9StartDate.apply { set(2025, Calendar.JANUARY, 16, 9, 0) }, "09:00", true, "Private museum tour"),
        Trip.Activity(69, trip9StartDate.apply { set(2025, Calendar.JANUARY, 16, 14, 0) }, "14:00", true, "Paragliding over the cliffs"),
        Trip.Activity(70, trip9StartDate.apply { set(2025, Calendar.JANUARY, 16, 19, 0) }, "19:00", false, "Live music and dance performance")
    ),
    trip9EndDate to listOf(
        Trip.Activity(71, trip9EndDate.apply { set(2025, Calendar.JANUARY, 20, 9, 0) }, "09:00", false, "Morning reflection and gratitude circle"),
        Trip.Activity(72, trip9EndDate.apply { set(2025, Calendar.JANUARY, 20, 14, 0) }, "14:00", true, "Final group photo and farewell")
    )
)

// Trip 10
val trip10StartDate = Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 10) }
val trip10EndDate = Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 15) }
val trip10Activities = mapOf(
    trip10StartDate to listOf(
        Trip.Activity(73, trip10StartDate.apply { set(2025, Calendar.FEBRUARY, 10, 10, 0) }, "10:00", true, "Welcome breakfast and introductions"),
        Trip.Activity(74, trip10StartDate.apply { set(2025, Calendar.FEBRUARY, 10, 13, 0) }, "13:00", true, "Horseback riding across the beach"),
        Trip.Activity(75, trip10StartDate.apply { set(2025, Calendar.FEBRUARY, 10, 18, 0) }, "18:00", true, "Sunset cocktail party")
    ),
    trip10StartDate.apply { add(Calendar.DATE, 1) } to listOf(
        Trip.Activity(76, trip10StartDate.apply { set(2025, Calendar.FEBRUARY, 11, 9, 0) }, "09:00", true, "Morning yoga session"),
        Trip.Activity(77, trip10StartDate.apply { set(2025, Calendar.FEBRUARY, 11, 14, 0) }, "14:00", false, "Local cultural tour and market visit"),
        Trip.Activity(78, trip10StartDate.apply { set(2025, Calendar.FEBRUARY, 11, 19, 0) }, "19:00", true, "Beachfront dinner under the stars")
    ),
    trip10EndDate to listOf(
        Trip.Activity(79, trip10EndDate.apply { set(2025, Calendar.FEBRUARY, 15, 9, 0) }, "09:00", false, "Final morning meditation"),
        Trip.Activity(80, trip10EndDate.apply { set(2025, Calendar.FEBRUARY, 15, 14, 0) }, "14:00", true, "Final group photo and farewell")
    )
)

// Trip 11
val trip11StartDate = Calendar.getInstance().apply { set(2025, Calendar.MARCH, 10) }
val trip11EndDate = Calendar.getInstance().apply { set(2025, Calendar.MARCH, 15) }
val trip11Activities = mapOf(
    trip11StartDate to listOf(
        Trip.Activity(81, trip11StartDate.apply { set(2025, Calendar.MARCH, 10, 10, 0) }, "10:00", true, "Welcome breakfast and briefing"),
        Trip.Activity(82, trip11StartDate.apply { set(2025, Calendar.MARCH, 10, 14, 0) }, "14:00", true, "Explore the ancient ruins"),
        Trip.Activity(83, trip11StartDate.apply { set(2025, Calendar.MARCH, 10, 18, 0) }, "18:00", false, "Traditional local dance performance")
    ),
    trip11StartDate.apply { add(Calendar.DATE, 1) } to listOf(
        Trip.Activity(84, trip11StartDate.apply { set(2025, Calendar.MARCH, 11, 8, 0) }, "08:00", true, "Hiking through the forest"),
        Trip.Activity(85, trip11StartDate.apply { set(2025, Calendar.MARCH, 11, 13, 0) }, "13:00", false, "Visit the local market"),
        Trip.Activity(86, trip11StartDate.apply { set(2025, Calendar.MARCH, 11, 19, 0) }, "19:00", true, "Group dinner at a local restaurant")
    ),
    trip11EndDate to listOf(
        Trip.Activity(87, trip11EndDate.apply { set(2025, Calendar.MARCH, 15, 9, 0) }, "09:00", false, "Final group reflection"),
        Trip.Activity(88, trip11EndDate.apply { set(2025, Calendar.MARCH, 15, 14, 0) }, "14:00", true, "Farewell group photo and departure")
    )
)


// Trip 12
val trip12StartDate = Calendar.getInstance().apply { set(2025, Calendar.APRIL, 5) }
val trip12EndDate = Calendar.getInstance().apply { set(2025, Calendar.APRIL, 10) }
val trip12Activities = mapOf(
    trip12StartDate to listOf(
        Trip.Activity(89, trip12StartDate.apply { set(2025, Calendar.APRIL, 5, 10, 0) }, "10:00", true, "Private boat ride"),
        Trip.Activity(90, trip12StartDate.apply { set(2025, Calendar.APRIL, 5, 14, 0) }, "14:00", true, "Snorkeling tour"),
        Trip.Activity(91, trip12StartDate.apply { set(2025, Calendar.APRIL, 5, 18, 0) }, "18:00", false, "Sunset viewing at the beach")
    ),
    trip12StartDate.apply { add(Calendar.DATE, 1) } to listOf(
        Trip.Activity(92, trip12StartDate.apply { set(2025, Calendar.APRIL, 6, 8, 0) }, "08:00", true, "Morning yoga on the beach"),
        Trip.Activity(93, trip12StartDate.apply { set(2025, Calendar.APRIL, 6, 14, 0) }, "14:00", false, "Island excursion"),
        Trip.Activity(94, trip12StartDate.apply { set(2025, Calendar.APRIL, 6, 19, 0) }, "19:00", true, "Beachfront BBQ dinner")
    ),
    trip12EndDate to listOf(
        Trip.Activity(95, trip12EndDate.apply { set(2025, Calendar.APRIL, 10, 9, 0) }, "09:00", false, "Closing meditation session"),
        Trip.Activity(96, trip12EndDate.apply { set(2025, Calendar.APRIL, 10, 14, 0) }, "14:00", true, "Farewell group photo")
    )
)

// Trip 13
val trip13StartDate = Calendar.getInstance().apply { set(2025, Calendar.MAY, 20) }
val trip13EndDate = Calendar.getInstance().apply { set(2025, Calendar.MAY, 25) }
val trip13Activities = mapOf(
    trip13StartDate to listOf(
        Trip.Activity(97, trip13StartDate.apply { set(2025, Calendar.MAY, 20, 10, 0) }, "10:00", true, "Morning hike and nature walk"),
        Trip.Activity(98, trip13StartDate.apply { set(2025, Calendar.MAY, 20, 14, 0) }, "14:00", true, "Canoeing in the river"),
        Trip.Activity(99, trip13StartDate.apply { set(2025, Calendar.MAY, 20, 18, 0) }, "18:00", false, "Traditional dinner with the locals")
    ),
    trip13StartDate.apply { add(Calendar.DATE, 1) } to listOf(
        Trip.Activity(100, trip13StartDate.apply { set(2025, Calendar.MAY, 21, 9, 0) }, "09:00", true, "Birdwatching expedition"),
        Trip.Activity(101, trip13StartDate.apply { set(2025, Calendar.MAY, 21, 13, 0) }, "13:00", false, "Cultural exchange with indigenous people"),
        Trip.Activity(102, trip13StartDate.apply { set(2025, Calendar.MAY, 21, 19, 0) }, "19:00", true, "Night safari experience")
    ),
    trip13EndDate to listOf(
        Trip.Activity(103, trip13EndDate.apply { set(2025, Calendar.MAY, 25, 9, 0) }, "09:00", true, "Closing group reflection"),
        Trip.Activity(104, trip13EndDate.apply { set(2025, Calendar.MAY, 25, 14, 0) }, "14:00", true, "Final group photo and farewell")
    )
)

//Trip 14
val trip14StartDate = Calendar.getInstance().apply { set(2025, Calendar.JUNE, 1) }
val trip14EndDate = Calendar.getInstance().apply { set(2025, Calendar.JUNE, 6) }
val trip14Activities = mapOf(
    trip14StartDate to listOf(
        Trip.Activity(105, trip14StartDate.apply { set(2025, Calendar.JUNE, 1, 10, 0) }, "10:00", true, "Exploring the desert dunes"),
        Trip.Activity(106, trip14StartDate.apply { set(2025, Calendar.JUNE, 1, 14, 0) }, "14:00", true, "Sandboarding experience"),
        Trip.Activity(107, trip14StartDate.apply { set(2025, Calendar.JUNE, 1, 18, 0) }, "18:00", false, "Campfire and stargazing")
    ),
    trip14StartDate.apply { add(Calendar.DATE, 1) } to listOf(
        Trip.Activity(108, trip14StartDate.apply { set(2025, Calendar.JUNE, 2, 9, 0) }, "09:00", true, "Morning camel ride"),
        Trip.Activity(109, trip14StartDate.apply { set(2025, Calendar.JUNE, 2, 13, 0) }, "13:00", false, "Visit a Bedouin village"),
        Trip.Activity(110, trip14StartDate.apply { set(2025, Calendar.JUNE, 2, 19, 0) }, "19:00", true, "Traditional desert dinner")
    ),
    trip14EndDate to listOf(
        Trip.Activity(111, trip14EndDate.apply { set(2025, Calendar.JUNE, 6, 9, 0) }, "09:00", true, "Closing group discussion"),
        Trip.Activity(112, trip14EndDate.apply { set(2025, Calendar.JUNE, 6, 14, 0) }, "14:00", true, "Farewell group photo and departure")
    )
)


// Trip 15
val trip15StartDate = Calendar.getInstance().apply { set(2025, Calendar.JULY, 5) }
val trip15EndDate = Calendar.getInstance().apply { set(2025, Calendar.JULY, 10) }
val trip15Activities = mapOf(
    trip15StartDate to listOf(
        Trip.Activity(113, trip15StartDate.apply { set(2025, Calendar.JULY, 5, 10, 0) }, "10:00", true, "Morning wildlife safari"),
        Trip.Activity(114, trip15StartDate.apply { set(2025, Calendar.JULY, 5, 14, 0) }, "14:00", true, "Visit to a local village"),
        Trip.Activity(115, trip15StartDate.apply { set(2025, Calendar.JULY, 5, 18, 0) }, "18:00", false, "Cultural performance and dinner")
    ),
    trip15StartDate.apply { add(Calendar.DATE, 1) } to listOf(
        Trip.Activity(116, trip15StartDate.apply { set(2025, Calendar.JULY, 6, 9, 0) }, "09:00", true, "Morning bird watching"),
        Trip.Activity(117, trip15StartDate.apply { set(2025, Calendar.JULY, 6, 13, 0) }, "13:00", false, "Cooking class with local chefs"),
        Trip.Activity(118, trip15StartDate.apply { set(2025, Calendar.JULY, 6, 19, 0) }, "19:00", true, "Dinner under the stars")
    ),
    trip15EndDate to listOf(
        Trip.Activity(119, trip15EndDate.apply { set(2025, Calendar.JULY, 10, 9, 0) }, "09:00", false, "Closing reflection and gratitude session"),
        Trip.Activity(120, trip15EndDate.apply { set(2025, Calendar.JULY, 10, 14, 0) }, "14:00", true, "Final group photo and departure")
    )
)

val trips = listOf<Trip>(
    Trip(
        id = 1,
        photo = R.drawable.paris,
        title = "Cultural Exploration in Paris",
        destination = "Paris, France",
        startDate = trip1StartDate,
        endDate = trip1EndDate,
        estimatedPrice = 1500.0,
        groupSize = 10,
        participants = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
        activities = trip1Activities,
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.RELAX),
        creatorId = 1,
        appliedUsers = listOf(11, 12, 13),
        published = true,
        isCompleted = false
    ),
    Trip(
        id = 2,
        photo = R.drawable.maldives,
        title = "Tropical Island Party",
        destination = "Maldives",
        startDate = trip2StartDate,
        endDate = trip2EndDate,
        estimatedPrice = 2000.0,
        groupSize = 15,
        participants = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
        activities = trip2Activities,
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.PARTY, TypeTravel.RELAX),
        creatorId = 2,
        appliedUsers = listOf(16, 17, 18),
        published = true,
        isCompleted = false
    ),
    Trip(
        id = 3,
        photo = R.drawable.mountrocky,
        title = "Adventure in the Rockies",
        destination = "Rocky Mountains, USA",
        startDate = trip3StartDate,
        endDate = trip3EndDate,
        estimatedPrice = 1800.0,
        groupSize = 12,
        participants = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
        activities = trip3Activities,
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.ADVENTURE),
        creatorId = 3,
        appliedUsers = listOf(19, 20, 21),
        published = true,
        isCompleted = false
    ),
    Trip(
        id = 4,
        photo = R.drawable.santorini,
        title = "Creative Arts Retreat",
        destination = "Santorini, Greece",
        startDate = trip4StartDate,
        endDate = trip4EndDate,
        estimatedPrice = 2200.0,
        groupSize = 8,
        participants = listOf(1, 2, 3, 4, 5, 6, 7, 8),
        activities = trip4Activities,
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.RELAX),
        creatorId = 4,
        appliedUsers = listOf(22, 23, 24),
        published = true,
        isCompleted = false
    ),
    Trip(
        id = 5,
        photo = R.drawable.serengetis,
        title = "African Safari Adventure",
        destination = "Serengeti, Tanzania",
        startDate = trip5StartDate,
        endDate = trip5EndDate,
        estimatedPrice = 2500.0,
        groupSize = 10,
        participants = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
        activities = trip5Activities,
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.ADVENTURE),
        creatorId = 5,
        appliedUsers = listOf(25, 26, 27),
        published = true,
        isCompleted = false
    ),
    Trip(
        id = 6,
        photo = R.drawable.tuscany,
        title = "Vineyard Retreat in Tuscany",
        destination = "Tuscany, Italy",
        startDate = trip6StartDate,
        endDate = trip6EndDate,
        estimatedPrice = 2200.0,
        groupSize = 8,
        participants = listOf(1, 2, 3, 4, 5, 6, 7, 8),
        activities = trip6Activities,
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.RELAX, TypeTravel.CULTURE),
        creatorId = 6,
        appliedUsers = listOf(28, 29, 30),
        published = true,
        isCompleted = false
    ),
    Trip(
        id = 7,
        photo = R.drawable.colorado,
        title = "Mountain Adventure in Colorado",
        destination = "Colorado, USA",
        startDate = trip7StartDate,
        endDate = trip7EndDate,
        estimatedPrice = 2800.0,
        groupSize = 10,
        participants = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
        activities = trip7Activities,
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.ADVENTURE),
        creatorId = 7,
        appliedUsers = listOf(31, 32, 33),
        published = true,
        isCompleted = false
    ),
    Trip(
        id = 8,
        photo = R.drawable.kyoto,
        title = "Cultural Immersion in Kyoto",
        destination = "Kyoto, Japan",
        startDate = trip8StartDate,
        endDate = trip8EndDate,
        estimatedPrice = 2300.0,
        groupSize = 12,
        participants = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
        activities = trip8Activities,
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.CULTURE),
        creatorId = 8,
        appliedUsers = listOf(34, 35, 36),
        published = true,
        isCompleted = false
    ),
    Trip(
        id = 9,
        photo = R.drawable.monaco,
        title = "Exclusive City Experience",
        destination = "Monaco",
        startDate = trip9StartDate,
        endDate = trip9EndDate,
        estimatedPrice = 3500.0,
        groupSize = 6,
        participants = listOf(1, 2, 3, 4, 5, 6),
        activities = trip9Activities,
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.RELAX),
        creatorId = 9,
        appliedUsers = listOf(37, 38, 39),
        published = true,
        isCompleted = false
    ),
    Trip(
        id = R.drawable.tulum,
        photo = 110,
        title = "Beachside Bliss in Tulum",
        destination = "Tulum, Mexico",
        startDate = trip10StartDate,
        endDate = trip10EndDate,
        estimatedPrice = 3000.0,
        groupSize = 8,
        participants = listOf(1, 2, 3, 4, 5, 6, 7, 8),
        activities = trip10Activities,
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.RELAX, TypeTravel.ADVENTURE),
        creatorId = 10,
        appliedUsers = listOf(40, 41, 42),
        published = true,
        isCompleted = false
    ),
    Trip(
        id = 11,
        photo = R.drawable.peru,
        title = "Ancient Civilizations Exploration",
        destination = "Peru",
        startDate = trip11StartDate,
        endDate = trip11EndDate,
        estimatedPrice = 1800.0,
        groupSize = 10,
        participants = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
        activities = trip11Activities,
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.CULTURE, TypeTravel.ADVENTURE),
        creatorId = 11,
        appliedUsers = listOf(43, 44, 45),
        published = true,
        isCompleted = false
    ),
    Trip(
        id = 12,
        photo = R.drawable.fiji,
        title = "Island Getaway in Fiji",
        destination = "Fiji",
        startDate = trip12StartDate,
        endDate = trip12EndDate,
        estimatedPrice = 2700.0,
        groupSize = 12,
        participants = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
        activities = trip12Activities,
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.RELAX, TypeTravel.ADVENTURE),
        creatorId = 12,
        appliedUsers = listOf(46, 47, 48),
        published = true,
        isCompleted = false
    ),
    Trip(
        id = R.drawable.brazil,
        photo = 113,
        title = "Jungle Adventure in the Amazon",
        destination = "Amazon Rainforest, Brazil",
        startDate = trip13StartDate,
        endDate = trip13EndDate,
        estimatedPrice = 3200.0,
        groupSize = 8,
        participants = listOf(1, 2, 3, 4, 5, 6, 7, 8),
        activities = trip13Activities,
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.ADVENTURE),
        creatorId = 13,
        appliedUsers = listOf(49, 50, 51),
        published = true,
        isCompleted = false
    ),
    Trip(
        id = 14,
        photo = R.drawable.dubai,
        title = "Desert Safari Adventure",
        destination = "Dubai, UAE",
        startDate = trip14StartDate,
        endDate = trip14EndDate,
        estimatedPrice = 2200.0,
        groupSize = 10,
        participants = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
        activities = trip14Activities,
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.ADVENTURE),
        creatorId = 14,
        appliedUsers = listOf(52, 53, 54),
        published = true,
        isCompleted = false
    ),
    Trip(
        id = 15,
        photo = R.drawable.kenya,
        title = "Safari Adventure in Kenya",
        destination = "Kenya",
        startDate = trip15StartDate,
        endDate = trip15EndDate,
        estimatedPrice = 2500.0,
        groupSize = 12,
        participants = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
        activities = trip15Activities,
        status = Trip.TripStatus.NOT_STARTED,
        typeTravel = listOf(TypeTravel.ADVENTURE),
        creatorId = 15,
        appliedUsers = listOf(55, 56, 57),
        published = true,
        isCompleted = false
    )
)





data class LazySeriousTrip(
    val tripTitle:String,
    val destination:String,
    val startDate:String,
    val endDate:String,
    val esteemedPrice:Float,
    val sizeGroup:Int,
    val acceptedPeople:Int,
    val tripPhoto:Int,                              //Use the R.drawable which returns an integer
    val activities:Map<String,List<LazyActivity>>
) {
    //Calculates the remaining spots
    fun remainingSpots():Int
    {
        return this.sizeGroup-this.acceptedPeople
    }
}

data class LazyActivity(
    val activityDate:String,
    val activityTime:String,
    val isGroupActivity:Boolean,
    val description:String
)

data class LazyUser(
    var name:String,
    var surname:String,
    var username:String,
    var userDescription:String,
    var email:String,
    var country:String,
    var approvalRate:Float,
    var reliability:Int,
    var typeTravelPreferences:List<TypeTravel>,
    var desiredDestinations:List<String>,
    var trips:List<LazyTrip>,
    var articles:List<LazyArticle>,
    var reviews:List<LazyReview>,
    var profileImage: Uri?
) {
    fun applyStrChanges(name:String, surname:String, username:String, email:String, country:String, userDescription:String)
    {
        this.name = name
        this.surname = surname
        this.username = username
        this.userDescription = userDescription
        this.email = email
        this.country = country
    }

    fun applyTypeTravelChanges(typeTravels:List<TypeTravel>)
    {
        if(typeTravels.isNotEmpty())
            this.typeTravelPreferences = typeTravels
    }

    fun applyNewImage(uri:Uri)
    {
        this.profileImage=uri
    }
}

//LAZY DATA
data class LazyTrip(
    val destination:String,
    val strDate:String,
    val photo: Int
)

data class LazyArticle(
    val title:String,
    val strDate:String,
    val photo: Int
)

data class LazyReview(
    val name:String,
    val surname:String,
    val rating:Float,
    val strDate:String
)

val tripList2 = listOf(
    LazyTrip("Reykjavik", "October, 2024", R.drawable.reykjavik),
    LazyTrip("Rio", "February, 2025", R.drawable.rio),
    LazyTrip("Paris", "May, 2025", R.drawable.paris),
    LazyTrip("New York", "December, 2025", R.drawable.new_york),

)

val tripList1 = listOf(
    LazyTrip("Roma", "September, 2024", R.drawable.rome_photo),
    LazyTrip("Sydney", "November, 2024", R.drawable.sydney),
    LazyTrip("Tokyo", "July, 2004", R.drawable.tokyo),
    LazyTrip("Bali", "August, 2025", R.drawable.bali),
)

val articleList2 = listOf(
    LazyArticle("Chasing Iceland Lights", "October, 2024", R.drawable.reykjavik),
    LazyArticle("Dancing Through Rio", "February, 2025", R.drawable.rio),
    LazyArticle("Fall in Love in Paris", "May, 2025", R.drawable.paris),
    LazyArticle("Discover New York City", "December, 2025", R.drawable.new_york)
)

val articleList1 = listOf(
    LazyArticle("Journey into Rome's History", "September, 2024", R.drawable.rome_photo),
    LazyArticle("Sydney Nights & Surf Days", "November, 2024", R.drawable.sydney),
    LazyArticle("Ultimate Tokyo Adventure", "July, 2004", R.drawable.tokyo),
    LazyArticle("Ocean Meets Culture", "August, 2025", R.drawable.bali)
)

val reviewList1 = listOf(
    LazyReview("Alice", "Martin", 4.5f, "August, 2025"),
    LazyReview("Leo", "Nguyen", 5.0f, "July, 2025"),
    LazyReview("Maria", "Gonzalez", 4.2f, "June, 2025"),
    LazyReview("Thomas", "Anders", 3.8f, "September, 2025"),
    LazyReview("Lena", "Kowalski", 4.7f, "October, 2025"),
    LazyReview("David", "O'Connor", 4.9f, "May, 2025"),
    LazyReview("Sofia", "Chen", 4.0f, "November, 2025"),
    LazyReview("Max", "Dubois", 3.5f, "December, 2025")
)

val reviewList2 = listOf(
    LazyReview("Emily", "Smith", 4.6f, "April, 2025"),
    LazyReview("James", "Lee", 4.1f, "March, 2025"),
    LazyReview("Nina", "Petrova", 4.8f, "February, 2025"),
    LazyReview("Oscar", "Morales", 3.9f, "January, 2025"),
    LazyReview("Isabella", "Bianchi", 4.4f, "August, 2025"),
    LazyReview("Lukas", "Meier", 3.7f, "June, 2025"),
    LazyReview("Ava", "Johnson", 4.3f, "July, 2025"),
    LazyReview("Mateo", "Silva", 4.0f, "May, 2025")
)

val user1 = LazyUser(
    name = "James",
    surname = "Lee",
    username = "james.explorer",
    userDescription = "Adventure junkie. If it's off-road, underwater, or on a mountain, I’m in.",
    email = "jameslee@polito.it",
    country = "Liberia",
    approvalRate = 4.3f,
    reliability = 90,
    typeTravelPreferences = listOf( TypeTravel.ADVENTURE, TypeTravel.CULTURE ),
    desiredDestinations = listOf( "Australia", "Japan", "Italy"),
    trips = tripList1,
    articles = articleList1,
    reviews = reviewList1,
    profileImage = null
)

val user2 = LazyUser(
    name = "Sofia",
    surname = "Chen",
    username = "sofiatrips",
    userDescription = "Planner, packer, photographer. I bring color to every itinerary.",
    email = "sofiachen@alice.it",
    country = "Panama",
    approvalRate = 4.9f,
    reliability = 97,
    typeTravelPreferences = listOf( TypeTravel.PARTY, TypeTravel.RELAX ),
    desiredDestinations = listOf( "Iceland","France", "United States of America" ),
    trips = tripList2,
    articles = articleList2,
    reviews = reviewList2,
    profileImage = null
)

val romeTrip = LazySeriousTrip(
    tripTitle = "Roaming Through Rome",
    destination = "Rome, Italy",
    startDate = "September 15, 2025",
    endDate = "September 20, 2025",
    esteemedPrice = 1350.0f,
    sizeGroup = 10,
    acceptedPeople = 6,
    tripPhoto = R.drawable.rome_photo, // Replace with actual image resource
    activities = mapOf(
        "Day 1" to listOf(
            LazyActivity("2025-09-15", "09:00", true, "Colosseum guided tour"),
            LazyActivity("2025-09-15", "12:00", true, "Lunch near Roman Forum"),
            LazyActivity("2025-09-15", "14:00", false, "Walk through Palatine Hill"),
            LazyActivity("2025-09-15", "18:00", true, "Welcome dinner at Trastevere")
        ),
        "Day 2" to listOf(
            LazyActivity("2025-09-16", "08:30", true, "Vatican Museums and Sistine Chapel"),
            LazyActivity("2025-09-16", "12:30", true, "Lunch at Vatican area"),
            LazyActivity("2025-09-16", "15:00", false, "Explore Castel Sant'Angelo"),
            LazyActivity("2025-09-16", "20:00", true, "Tiber River sunset walk")
        ),
        "Day 3" to listOf(
            LazyActivity("2025-09-17", "09:00", true, "Pantheon visit"),
            LazyActivity("2025-09-17", "11:00", true, "Espresso tasting session"),
            LazyActivity("2025-09-17", "14:00", false, "Free time at Piazza Navona"),
            LazyActivity("2025-09-17", "19:00", true, "Group dinner and gelato tour")
        ),
        "Day 4" to listOf(
            LazyActivity("2025-09-18", "10:00", true, "Cooking class: Make your own pasta"),
            LazyActivity("2025-09-18", "13:00", true, "Eat what you cooked together"),
            LazyActivity("2025-09-18", "15:00", false, "Relax in Villa Borghese gardens"),
            LazyActivity("2025-09-18", "18:00", true, "Evening wine tasting event")
        ),
        "Day 5" to listOf(
            LazyActivity("2025-09-19", "09:30", true, "Day trip to Tivoli (Villa d'Este)"),
            LazyActivity("2025-09-19", "13:00", true, "Lunch in Tivoli"),
            LazyActivity("2025-09-19", "16:00", false, "Explore Villa Adriana"),
            LazyActivity("2025-09-19", "20:00", true, "Return to Rome and night walk")
        ),
        "Day 6" to listOf(
            LazyActivity("2025-09-20", "08:00", false, "Optional early morning photo session"),
            LazyActivity("2025-09-20", "10:00", true, "Farewell brunch"),
            LazyActivity("2025-09-20", "12:00", false, "Free time for shopping or relaxing"),
            LazyActivity("2025-09-20", "15:00", true, "Group wrap-up & feedback session")
        )
    )
)
