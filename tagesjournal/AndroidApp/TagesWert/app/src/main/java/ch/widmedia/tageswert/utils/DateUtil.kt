package ch.widmedia.tageswert.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtil {
    val ISO_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val DISPLAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    fun lokalDatum(isoDate: String): String {
        return try {
            LocalDate.parse(isoDate, ISO_FORMAT).format(DISPLAY_FORMAT)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun lokalDatumMitWochentag(isoDate: String): String {
        return try {
            val date = LocalDate.parse(isoDate, ISO_FORMAT)
            "${wochentag(date)}, ${date.format(DISPLAY_FORMAT)}"
        } catch (_: Exception) {
            isoDate
        }
    }

    fun lokalDatumMitWochentagLang(isoDate: String): String {
        return try {
            val date = LocalDate.parse(isoDate, ISO_FORMAT)
            "${wochentagLang(date)}, ${date.format(DISPLAY_FORMAT)}"
        } catch (_: Exception) {
            isoDate
        }
    }

    fun kalenderWochen(): List<LocalDate> {
        val today = LocalDate.now()
        val diff = today.dayOfWeek.value - 1
        val currentMonday = today.minusDays(diff.toLong())
        val lastMonday = currentMonday.minusDays(7)
        return (0..13).map { lastMonday.plusDays(it.toLong()) }
    }

    fun toIso(date: LocalDate): String = date.format(ISO_FORMAT)

    fun monthTitle(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.getDefault())
        val formatted = date.format(formatter)
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
    }

    fun daysInMonth(date: LocalDate): List<LocalDate?> {
        val firstOfMonth = date.withDayOfMonth(1)
        val lastOfMonth = date.withDayOfMonth(date.lengthOfMonth())
        
        val firstDayOfWeek = firstOfMonth.dayOfWeek.value // 1 (Mon) to 7 (Sun)
        val daysBefore = firstDayOfWeek - 1
        
        val days = mutableListOf<LocalDate?>()
        
        // Add empty slots for days of previous month
        repeat(daysBefore) {
            days.add(null)
        }
        
        // Add days of current month
        for (day in 1..date.lengthOfMonth()) {
            days.add(firstOfMonth.plusDays((day - 1).toLong()))
        }
        
        // Fill up to full weeks if needed
        while (days.size % 7 != 0) {
            days.add(null)
        }
        
        return days
    }

    fun wochentag(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("E", java.util.Locale.getDefault())
        val formatted = date.format(formatter)
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
    }

    fun wochentagLang(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern(if (java.util.Locale.getDefault().language == "de") "EEEE" else "EEEE", java.util.Locale.getDefault())
        val formatted = date.format(formatter)
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
    }
}
