package de.poljansek.budgetbook.core.ui

val germanMonths: List<String> = listOf(
    "Januar", "Februar", "März", "April", "Mai", "Juni",
    "Juli", "August", "September", "Oktober", "November", "Dezember",
)

fun monthLabel(month: Int): String = germanMonths.getOrElse(month - 1) { month.toString() }

fun formatIsoDate(iso: String): String {
    val parts = iso.take(10).split("-")
    if (parts.size != 3) return iso
    return "${parts[2]}.${parts[1]}.${parts[0]}"
}

fun yearChoices(currentYear: Int = 2026): List<Int> = (currentYear downTo 2018).toList()

fun monthStart(year: Int, month: Int): String = "${year.toPadded(4)}-${month.toPadded(2)}-01"

fun monthEnd(year: Int, month: Int): String {
    val last = when (month) {
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    return "${year.toPadded(4)}-${month.toPadded(2)}-${last.toPadded(2)}"
}

private fun Int.toPadded(width: Int): String = toString().padStart(width, '0')
