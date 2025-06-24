package com.example.voyago


import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Locale


fun formatMessageTimestamp(timestamp: Timestamp): String {
    val stringMessageTimestamp =
//        toCalendar(timestamp).get(Calendar.DAY_OF_MONTH).toString() +
//                "/" +
//                toCalendar(timestamp).get(Calendar.MONTH).toString().trimEnd() +
//                " " +
        toCalendar(timestamp).get(Calendar.HOUR).toString() +
                ":" +
                toCalendar(timestamp).get(Calendar.MINUTE).toString()
    return stringMessageTimestamp
}

fun toCalendar(timeDate: Timestamp): Calendar {
    var calendarDate = Calendar.getInstance()
    calendarDate.time = timeDate.toDate()
    return calendarDate
}

fun parseAndSetTime(calendar: Calendar, timeString: String): Calendar {
    val parts = timeString.split(" ", ":")
    var calendarToReturn = calendar

    if (parts.size != 3) {
        throw IllegalArgumentException("Formato orario non valido: $timeString. Deve essere 'HH:mm AM/PM'.")
    }

    var ore = parts[0].toIntOrNull() ?: throw IllegalArgumentException("Ore non valide: ${parts[0]}")
    val minuti = parts[1].toIntOrNull() ?: throw IllegalArgumentException("Minuti non validi: ${parts[1]}")
    val amPmIndicator = parts[2].uppercase()

    if (ore !in 1..12 || minuti !in 0..59) {
        throw IllegalArgumentException("Ora o minuti fuori intervallo valido: $timeString")
    }

    when (amPmIndicator) {
        "PM" -> {
            if (ore != 12) {
                ore += 12
            }
        }
        "AM" -> {
            if (ore == 12) {
                ore = 0
            }
        }
        else -> throw IllegalArgumentException("Indicatore AM/PM non valido: ${parts[2]}")
    }

    calendar.set(Calendar.HOUR_OF_DAY, ore)
    calendar.set(Calendar.MINUTE, minuti)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    calendarToReturn = calendar
    return calendarToReturn
}

fun Calendar.toStringDate(): String {

    val year = get(Calendar.YEAR)
    val month = get(Calendar.MONTH) + 1
    val day = get(Calendar.DAY_OF_MONTH)

    // Format month and day with a 0 if needed
    val formattedDate = String.format(Locale.US, "$day/$month/$year")

    return formattedDate
}

fun String.toCalendar(): Calendar {
    val parts: List<String>

    if (this.contains("-")) {
        parts = this.split("-")
        if (parts.size != 3) {
            throw IllegalArgumentException("Format 'yyyy-MM-dd' invalid: $this")
        }
        val anno = parts[0].toInt()
        val mese = parts[1].toInt() - 1
        val giorno = parts[2].toInt()

        return Calendar.getInstance().apply {
            set(anno, mese, giorno)
        }
    } else if (this.contains("/")) {
        parts = this.split("/")
        if (parts.size != 3) {
            throw IllegalArgumentException("Format 'dd/MM/yyyy' invalid: $this")
        }
        val giorno = parts[0].toInt()
        val mese = parts[1].toInt() - 1
        val anno = parts[2].toInt()

        return Calendar.getInstance().apply {
            set(anno, mese, giorno)
        }
    } else {
        throw IllegalArgumentException("Invalid format: $this. Use 'yyyy-MM-dd' or 'dd/MM/yyyy'.")
    }
}