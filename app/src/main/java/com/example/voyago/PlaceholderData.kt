package com.example.voyago

import android.net.Uri
import com.example.voyago.model.*




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
