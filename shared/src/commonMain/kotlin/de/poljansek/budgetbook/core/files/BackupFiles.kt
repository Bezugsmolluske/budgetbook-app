package de.poljansek.budgetbook.core.files

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

fun suggestedBackupName(
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
): String = "backup_$today"

data class PickedFile(
    val name: String,
    val bytes: ByteArray,
)

expect suspend fun saveBackupFile(bytes: ByteArray, suggestedName: String, extension: String)

expect suspend fun pickBackupFile(extension: String): PickedFile?
