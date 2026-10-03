package de.poljansek.budgetbook.feature.importing

import de.poljansek.budgetbook.core.network.dto.BookType
import de.poljansek.budgetbook.core.network.dto.CategoryDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MlpCsvTest {
    @Test
    fun parsesCardPaymentCreditAndSkipsZero() {
        val csv = """
            Umsatzanzeige
            $HEADER
            KonsumKonto;DE00;BIC;MLP;30.09.2026;01.10.2026;Seipenbusch Einzelhandels;DE01;BIC2;Kartenzahlung girocard;$LONG_PURPOSE;-8,06;EUR;10,00;;;;
            KonsumKonto;DE00;BIC;MLP;29.09.2026;28.09.2026;Lea Poljansek;DE02;BIC2;GUTSCHRIFT;;1400,00;EUR;20,00;;;;
            KonsumKonto;DE00;BIC;MLP;01.01.2026;02.01.2026;Niemand;DE03;BIC2;GUTSCHRIFT;;0,00;EUR;0,00;;;;
        """.trimIndent()

        val rows = parseMlpCsv(csv.encodeToByteArray())

        assertEquals(2, rows.size)
        val card = rows[0]
        assertEquals("2026-09-30", card.date)
        assertEquals(-806L, card.signedCents)
        assertEquals(BookType.EXPENSE, card.type)
        assertEquals("EUR", card.currency)
        assertEquals("Seipenbusch Einzelhandels — $LONG_PURPOSE", card.description)
        assertFalse(card.description.contains("Kartenzahlung"))

        val credit = rows[1]
        assertEquals("2026-09-29", credit.date)
        assertEquals(140_000L, credit.signedCents)
        assertEquals(BookType.INCOME, credit.type)
        assertEquals("Lea Poljansek", credit.description)
        assertFalse(credit.description.contains("GUTSCHRIFT"))
    }

    @Test
    fun usesPurchaseDateFromDescription() {
        val header = "Buchungstag;Name Zahlungsbeteiligter;Verwendungszweck;Betrag"
        val full = "$header\n02.10.2026;REWE;Einkauf 25.10.2026;-8,06"
        val parsed = parseMlpCsv(full.encodeToByteArray()).single()
        assertEquals("2026-10-25", parsed.date)
        assertEquals(
            "Buchungstag 02.10.2026 · Betrag -8,06\nREWE\nEinkauf 25.10.2026",
            parsed.source,
        )

        val short = "$header\n02.11.2026;REWE;Einkauf 25.10.;-8,06"
        assertEquals("2026-10-25", parseMlpCsv(short.encodeToByteArray()).single().date)

        val withoutYear = "$header\n03.01.2027;REWE;Einkauf 25.12;-8,06"
        assertEquals("2026-12-25", parseMlpCsv(withoutYear.encodeToByteArray()).single().date)

        val booked = "$header\n02.10.2026;REWE;Einkauf;-8,06"
        assertEquals("2026-10-02", parseMlpCsv(booked.encodeToByteArray()).single().date)
    }

    @Test
    fun parsesThousandsSeparator() {
        val csv = "Buchungstag;Betrag\n02.10.2026;-1.234,56\n"
        val row = parseMlpCsv(csv.encodeToByteArray()).single()
        assertEquals(-123_456L, row.signedCents)
        assertEquals(BookType.EXPENSE, row.type)
    }

    @Test
    fun readsLatin1WhenBytesAreNotUtf8() {
        val prefix = "Buchungstag;Name Zahlungsbeteiligter;Verwendungszweck;Betrag\n02.10.2026;M"
        val suffix = "ller;;-1,00\n"
        val bytes = prefix.encodeToByteArray() + byteArrayOf(0xFC.toByte()) + suffix.encodeToByteArray()
        assertEquals("Müller", parseMlpCsv(bytes).single().description)
    }

    @Test
    fun readsUtf8Umlauts() {
        val csv = "Buchungstag;Name Zahlungsbeteiligter;Verwendungszweck;Betrag\n02.10.2026;Müller;;-1,00\n"
        assertEquals("Müller", parseMlpCsv(csv.encodeToByteArray()).single().description)
    }

    @Test
    fun duplicateMatchesSameDateCentsAndTypeOnly() {
        val existing = listOf(
            ExistingBooking("2026-09-30", 806, BookType.EXPENSE, "REWE", "Lebensmittel", "EUR"),
            ExistingBooking("2026-09-30", 806, BookType.INCOME, "Gehalt", "Lohn", "EUR"),
        )

        assertEquals(
            listOf(existing[0]),
            matchingDuplicates("2026-09-30", 806, BookType.EXPENSE, existing),
        )
        assertEquals(
            listOf(existing[1]),
            matchingDuplicates("2026-09-30", 806, BookType.INCOME, existing),
        )
        assertEquals(
            emptyList(),
            matchingDuplicates("2026-09-30", 807, BookType.EXPENSE, existing),
        )
    }

    @Test
    fun changingTypeClearsCategoryThatNoLongerMatches() {
        val food = CategoryDto(id = "food", name = "Lebensmittel", type = BookType.EXPENSE)
        val expense = ImportRow(
            key = 0,
            date = "2026-10-02",
            amount = SignedAmountInput(negative = true, digits = "806"),
            heldType = BookType.EXPENSE,
            currency = "EUR",
            description = "REWE",
            source = "Buchungstag 02.10.2026 · Betrag -8,06 EUR",
            categoryId = "food",
        )
        val income = expense.withType(BookType.INCOME, listOf(food))
        assertEquals(BookType.INCOME, income.type)
        assertEquals("", income.categoryId)

        val edited = expense.editedAmount("8,061")
        assertEquals(BookType.EXPENSE, edited.type)
        assertEquals("food", edited.categoryId)
    }

    private companion object {
        const val HEADER =
            "Bezeichnung Auftragskonto;IBAN Auftragskonto;BIC Auftragskonto;Bankname Auftragskonto;" +
                "Buchungstag;Valutadatum;Name Zahlungsbeteiligter;IBAN Zahlungsbeteiligter;" +
                "BIC (SWIFT-Code) Zahlungsbeteiligter;Buchungstext;Verwendungszweck;Betrag;Waehrung;" +
                "Saldo nach Buchung;Bemerkung;Gekennzeichneter Umsatz;Glaeubiger ID;Mandatsreferenz"
        const val LONG_PURPOSE =
            "REWE SAGT DANKE. EUR 8,06 Karte 1234 Filiale Koeln Hbf Terminal 42 Beleg 998877"
    }
}
