package de.poljansek.budgetbook.feature.importing

import de.poljansek.budgetbook.core.money.parseMoneyToCents
import de.poljansek.budgetbook.core.network.dto.BookType
import kotlinx.datetime.LocalDate

data class ImportedBooking(
    val date: String,
    val signedCents: Long,
    val currency: String,
    val description: String,
    val source: String,
) {
    val type: BookType
        get() = if (signedCents < 0) BookType.EXPENSE else BookType.INCOME
}

data class ExistingBooking(
    val date: String,
    val amountCents: Long,
    val type: BookType,
    val description: String,
    val categoryName: String,
    val currency: String,
    val id: String? = null,
)

fun matchingDuplicates(
    date: String,
    amountCents: Long,
    type: BookType,
    existing: List<ExistingBooking>,
): List<ExistingBooking> = existing.filter { booking ->
    booking.date == date && booking.amountCents == amountCents && booking.type == type
}

fun parseMlpCsv(bytes: ByteArray): List<ImportedBooking> {
    val records = parseSemicolonRecords(decodeBankText(bytes))
    val headerIndex = records.indexOfFirst { row ->
        val cells = row.map { it.headerCell() }
        "Buchungstag" in cells && "Betrag" in cells
    }
    if (headerIndex < 0) {
        throw IllegalArgumentException("Die Datei enthält keine MLP-Umsätze.")
    }
    val header = records[headerIndex].map { it.headerCell() }
    val dateCol = header.indexOf("Buchungstag")
    val amountCol = header.indexOf("Betrag")
    val nameCol = header.indexOf("Name Zahlungsbeteiligter")
    val purposeCol = header.indexOf("Verwendungszweck")
    val currencyCol = header.indexOf("Waehrung")
    val bookings = mutableListOf<ImportedBooking>()
    for (row in records.drop(headerIndex + 1)) {
        val amountRaw = row.cell(amountCol)
        if (amountRaw.isEmpty()) continue
        val signedCents = parseMoneyToCents(amountRaw) ?: continue
        if (signedCents == 0L) continue
        val bookedRaw = row.cell(dateCol).trim()
        val bookedOn = parseBookingDate(bookedRaw) ?: continue
        val name = row.cell(nameCol)
        val purpose = row.cell(purposeCol)
        val description = bookingDescription(name, purpose)
        val currencyRaw = row.cell(currencyCol).trim()
        bookings.add(
            ImportedBooking(
                date = dateFromDescription(description, bookedOn),
                signedCents = signedCents,
                currency = currencyRaw.ifBlank { "EUR" },
                description = description,
                source = importSource(bookedRaw, amountRaw.trim(), currencyRaw, name, purpose),
            ),
        )
    }
    if (bookings.isEmpty()) {
        throw IllegalArgumentException("Keine Buchungen in der Datei.")
    }
    return bookings
}

internal fun importSource(
    bookedOn: String,
    amount: String,
    currency: String,
    name: String,
    purpose: String,
): String {
    val head = buildString {
        append("Buchungstag ")
        append(bookedOn)
        append(" · Betrag ")
        append(amount)
        if (currency.isNotBlank()) append(" $currency")
    }
    return listOf(head, name.normalizeCell(), purpose.normalizeCell())
        .filter { it.isNotBlank() }
        .joinToString("\n")
}

internal fun bookingDescription(name: String, purpose: String): String {
    val counterparty = name.normalizeCell()
    val usage = purpose.normalizeCell()
    return when {
        counterparty.isEmpty() -> usage
        usage.isEmpty() -> counterparty
        else -> "$counterparty — $usage"
    }
}

internal fun parseBookingDate(raw: String): String? {
    val parts = raw.trim().split('.')
    if (parts.size != 3 || parts[2].length != 4) return null
    val day = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val year = parts[2].toIntOrNull() ?: return null
    return runCatching { LocalDate(year, month, day).toString() }.getOrNull()
}

internal fun dateFromDescription(description: String, bookedOn: String): String {
    val booked = runCatching { LocalDate.parse(bookedOn) }.getOrNull() ?: return bookedOn
    return findDescriptionDate(description, booked)?.toString() ?: bookedOn
}

private val descriptionDate = Regex("""\d{1,2}\.\d{1,2}(?:\.\d{4}|\.)?""")

private fun findDescriptionDate(description: String, bookedOn: LocalDate): LocalDate? {
    var found: Pair<Int, LocalDate>? = null
    for (match in descriptionDate.findAll(description)) {
        if (!match.isStandaloneDate(description)) continue
        val date = match.toDescriptionDate(bookedOn) ?: continue
        if (found == null || match.range.first < found.first) found = match.range.first to date
    }
    return found?.second
}

private fun MatchResult.isStandaloneDate(text: String): Boolean {
    val start = range.first
    val end = range.last + 1
    val precededByDigit = start > 0 && text[start - 1].isDigit()
    val followedByDigit = end < text.length && text[end].isDigit()
    return !precededByDigit && !followedByDigit
}

private fun MatchResult.toDescriptionDate(bookedOn: LocalDate): LocalDate? {
    val parts = value.trimEnd('.').split('.')
    val day = parts.getOrNull(0) ?: return null
    val month = parts.getOrNull(1) ?: return null
    return if (parts.size >= 3) {
        localDate(day, month, parts[2].toIntOrNull())
    } else {
        shortDate(day, month, bookedOn)
    }
}

private fun shortDate(dayText: String, monthText: String, bookedOn: LocalDate): LocalDate? {
    val sameYear = localDate(dayText, monthText, bookedOn.year) ?: return null
    if (sameYear <= bookedOn) return sameYear
    return localDate(dayText, monthText, bookedOn.year - 1)
}

private fun localDate(dayText: String, monthText: String, year: Int?): LocalDate? {
    val day = dayText.toIntOrNull() ?: return null
    val month = monthText.toIntOrNull() ?: return null
    if (year == null) return null
    return runCatching { LocalDate(year, month, day) }.getOrNull()
}

internal fun decodeBankText(bytes: ByteArray): String {
    val payload = bytes.stripUtf8Bom()
    return if (payload.isValidUtf8()) payload.decodeToString() else payload.decodeIso8859_1()
}

private fun parseSemicolonRecords(text: String): List<List<String>> {
    val records = mutableListOf<List<String>>()
    val record = mutableListOf<String>()
    val field = StringBuilder()
    var quoted = false
    var index = 0

    fun finishField() {
        record.add(field.toString())
        field.clear()
    }

    fun finishRecord() {
        val blank = record.all { it.isBlank() }
        if (record.isNotEmpty() && !blank) records.add(record.toList())
        record.clear()
    }

    while (index < text.length) {
        val char = text[index]
        if (quoted) {
            if (char == '"') {
                if (index + 1 < text.length && text[index + 1] == '"') {
                    field.append('"')
                    index += 2
                } else {
                    quoted = false
                    index++
                }
            } else {
                field.append(char)
                index++
            }
            continue
        }
        when (char) {
            '"' -> {
                quoted = true
                index++
            }
            ';' -> {
                finishField()
                index++
            }
            '\n' -> {
                finishField()
                finishRecord()
                index++
            }
            '\r' -> {
                finishField()
                finishRecord()
                index += if (index + 1 < text.length && text[index + 1] == '\n') 2 else 1
            }
            else -> {
                field.append(char)
                index++
            }
        }
    }
    if (field.isNotEmpty() || record.isNotEmpty()) {
        finishField()
        finishRecord()
    }
    return records
}

private fun List<String>.cell(index: Int): String =
    if (index < 0) "" else getOrNull(index).orEmpty()

private fun String.headerCell(): String = trim().removePrefix("\uFEFF")

private fun String.normalizeCell(): String = trim().replace(Regex("\\s+"), " ")

private fun ByteArray.stripUtf8Bom(): ByteArray {
    val hasBom = size >= 3 &&
        this[0] == 0xEF.toByte() &&
        this[1] == 0xBB.toByte() &&
        this[2] == 0xBF.toByte()
    return if (hasBom) copyOfRange(3, size) else this
}

private fun ByteArray.decodeIso8859_1(): String = buildString(size) {
    for (byte in this@decodeIso8859_1) append((byte.toInt() and 0xFF).toChar())
}

private fun ByteArray.isValidUtf8(): Boolean {
    var index = 0
    while (index < size) {
        val lead = this[index].toInt() and 0xFF
        val width = when (lead) {
            in 0x00..0x7F -> 1
            in 0xC2..0xDF -> 2
            in 0xE0..0xEF -> 3
            in 0xF0..0xF4 -> 4
            else -> return false
        }
        if (index + width > size) return false
        if (width == 3 && lead == 0xE0 && (this[index + 1].toInt() and 0xFF) < 0xA0) return false
        if (width == 3 && lead == 0xED && (this[index + 1].toInt() and 0xFF) > 0x9F) return false
        if (width == 4 && lead == 0xF0 && (this[index + 1].toInt() and 0xFF) < 0x90) return false
        if (width == 4 && lead == 0xF4 && (this[index + 1].toInt() and 0xFF) > 0x8F) return false
        for (offset in 1 until width) {
            val next = this[index + offset].toInt() and 0xFF
            if (next !in 0x80..0xBF) return false
        }
        index += width
    }
    return true
}
