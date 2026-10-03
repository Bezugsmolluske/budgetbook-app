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

    @Test
    fun amountGrowsFromCents() {
        var digits = ""
        assertEquals("0,00", formatAmountInput(digits))
        digits = applyAmountInput(digits, "0,004")
        assertEquals("0,04", formatAmountInput(digits))
        digits = applyAmountInput(digits, "0,042")
        assertEquals("0,42", formatAmountInput(digits))
        digits = applyAmountInput(digits, "0,428")
        assertEquals("4,28", formatAmountInput(digits))
        assertEquals(428, amountInputToCents(digits))
    }

    @Test
    fun twelveCentsStayBelowOneEuro() {
        val digits = applyAmountInput(applyAmountInput("", "0,001"), "0,012")
        assertEquals("0,12", formatAmountInput(digits))
        assertEquals(12, amountInputToCents(digits))
    }

    @Test
    fun backspaceRemovesLastCent() {
        val digits = applyAmountInput("428", "4,2")
        assertEquals("42", digits)
        assertEquals("0,42", formatAmountInput(digits))
        assertEquals("", applyAmountInput("4", "0,0"))
    }
}
