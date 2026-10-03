package de.poljansek.budgetbook.core.files

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class BackupFileNameTest {
    @Test
    fun includesCurrentDateBeforeTheExtension() {
        assertEquals("backup_2026-10-02", suggestedBackupName(LocalDate(2026, 10, 2)))
    }
}
