package de.poljansek.budgetbook.core.money

import kotlin.math.abs

data class Money(
    val amountInCents: Long,
    val currency: String = DEFAULT_CURRENCY,
) {
    fun formatted(): String = formatMoney(amountInCents, currency)

    companion object {
        const val DEFAULT_CURRENCY: String = "EUR"
    }
}

fun formatMoney(amountInCents: Long, currency: String = Money.DEFAULT_CURRENCY): String {
    val negative = amountInCents < 0
    val absValue = abs(amountInCents)
    val major = absValue / 100
    val minor = absValue % 100
    val grouped = groupThousands(major)
    val symbol = if (currency == "EUR") "€" else currency
    val formatted = "$grouped,${minor.toString().padStart(2, '0')} $symbol"
    return if (negative) "−$formatted" else formatted
}

fun parseMoneyToCents(input: String): Long? {
    val trimmed = input.trim().replace(" ", "").replace("€", "").replace("EUR", "", ignoreCase = true)
    if (trimmed.isEmpty()) return null
    val negative = trimmed.startsWith("-") || trimmed.startsWith("−")
    val unsigned = trimmed.removePrefix("-").removePrefix("−")
    val lastComma = unsigned.lastIndexOf(',')
    val lastDot = unsigned.lastIndexOf('.')
    val normalized = when {
        lastComma >= 0 && lastDot >= 0 -> {
            if (lastComma > lastDot) {
                unsigned.replace(".", "").replace(",", ".")
            } else {
                unsigned.replace(",", "")
            }
        }
        lastComma >= 0 -> unsigned.replace(",", ".")
        else -> unsigned
    }
    val parts = normalized.split('.')
    val major = parts[0].toLongOrNull() ?: return null
    val minor = when {
        parts.size == 1 -> 0L
        parts.size == 2 -> parts[1].padEnd(2, '0').take(2).toLongOrNull() ?: return null
        else -> return null
    }
    val cents = major * 100 + minor
    return if (negative) -cents else cents
}

fun centsToInput(amountInCents: Long): String {
    val negative = amountInCents < 0
    val absValue = abs(amountInCents)
    val text = "${absValue / 100},${(absValue % 100).toString().padStart(2, '0')}"
    return if (negative) "-$text" else text
}

private fun groupThousands(value: Long): String {
    val digits = value.toString()
    val builder = StringBuilder()
    digits.reversed().forEachIndexed { index, char ->
        if (index > 0 && index % 3 == 0) builder.append('.')
        builder.append(char)
    }
    return builder.reverse().toString()
}
