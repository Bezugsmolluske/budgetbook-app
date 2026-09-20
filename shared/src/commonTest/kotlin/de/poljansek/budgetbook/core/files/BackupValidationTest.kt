package de.poljansek.budgetbook.core.files

import kotlin.test.Test
import kotlin.test.assertFailsWith

class BackupValidationTest {
    @Test
    fun rejectsEmptyBytes() {
        assertFailsWith<IllegalStateException> { requireValidJsonBackup(ByteArray(0)) }
    }

    @Test
    fun rejectsEmptyBackupObject() {
        assertFailsWith<IllegalStateException> {
            requireValidJsonBackup("""{"schemaVersion":3,"categories":[],"transactions":[]}""".encodeToByteArray())
        }
    }

    @Test
    fun acceptsBackupWithCategories() {
        requireValidJsonBackup(
            """{"schemaVersion":3,"categories":[{"name":"Auto"}],"transactions":[]}""".encodeToByteArray()
        )
    }
}
