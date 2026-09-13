package de.poljansek.budgetbook.core.money

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MoneyTest {
    @Test
    fun formatsGermanEuros() {
        assertEquals("42,50 €", formatMoney(4250))
        assertEquals("1.234,00 €", formatMoney(123400))
        assertEquals("−0,99 €", formatMoney(-99))
    }

    @Test
    fun parsesGermanAndDotInput() {
        assertEquals(4250, parseMoneyToCents("42,50"))
        assertEquals(4250, parseMoneyToCents("42.50"))
        assertEquals(123400, parseMoneyToCents("1.234,00"))
        assertEquals(-100, parseMoneyToCents("-1,00"))
        assertNull(parseMoneyToCents("abc"))
    }

    @Test
    fun roundTripsInput() {
        assertEquals("12,05", centsToInput(1205))
        assertEquals(1205, parseMoneyToCents(centsToInput(1205)))
    }
}
