package de.poljansek.budgetbook.core.money

import kotlin.math.abs

data class Money(
    val amountInCents: Long,
    val currency: String = DEFAULT_CURRENCY,
) {
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

private const val MAX_AMOUNT_DIGITS = 12

/** Digits stored for the amount field. "428" means 4,28 €. Empty means 0. */
fun centsToAmountInput(amountInCents: Long): String {
    val absValue = abs(amountInCents)
    return if (absValue == 0L) "" else absValue.toString()
}

fun amountInputToCents(digits: String): Long? {
    val normalized = digits.filter(Char::isDigit).trimStart('0')
    if (normalized.isEmpty()) return null
    return normalized.toLongOrNull()
}

/** What the amount field shows. Empty input is "0,00" so the next digit lands in the cents place. */
fun formatAmountInput(digits: String): String = centsToInput(amountInputToCents(digits) ?: 0L)

/**
 * Each new digit becomes the cents place and shifts the previous digits left.
 * "4" then "2" then "8" is 4,28 €. Backspace removes the last digit.
 */
fun applyAmountInput(currentDigits: String, proposedFieldText: String): String {
    val current = currentDigits.filter(Char::isDigit).trimStart('0')
    val oldDigits = formatAmountInput(current).filter(Char::isDigit)
    val newDigits = proposedFieldText.filter(Char::isDigit)
    val added = addedDigits(oldDigits, newDigits)
    val next = when {
        added.isNotEmpty() && newDigits.length >= oldDigits.length -> current + added
        added.isNotEmpty() -> added
        newDigits.length < oldDigits.length -> current.dropLast(oldDigits.length - newDigits.length)
        else -> current
    }
    return next.trimStart('0').take(MAX_AMOUNT_DIGITS)
}

private fun addedDigits(oldDigits: String, newDigits: String): String {
    val extra = StringBuilder()
    var index = 0
    for (char in newDigits) {
        if (index < oldDigits.length && char == oldDigits[index]) index++ else extra.append(char)
    }
    return extra.toString()
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
