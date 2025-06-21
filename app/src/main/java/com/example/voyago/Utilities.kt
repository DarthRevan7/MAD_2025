package com.example.voyago


import java.util.Calendar
import java.util.Locale


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