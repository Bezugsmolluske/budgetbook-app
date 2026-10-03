package de.poljansek.budgetbook.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DateFieldsTest {
    @Test
    fun isoDateRoundTripsThroughPickerMillis() {
        val millis = isoToUtcMillis("2026-02-28")
        assertEquals(1_772_236_800_000L, millis)
        assertEquals("2026-02-28", utcMillisToIso(millis!!))
    }

    @Test
    fun invalidIsoDateHasNoSelection() {
        assertNull(isoToUtcMillis("28.02.2026"))
    }
}
