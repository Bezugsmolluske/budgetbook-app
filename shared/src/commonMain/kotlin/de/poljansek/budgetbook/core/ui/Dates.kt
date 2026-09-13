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
