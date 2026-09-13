package de.poljansek.budgetbook.core.files

data class PickedFile(
    val name: String,
    val bytes: ByteArray,
)

expect suspend fun saveBackupFile(bytes: ByteArray, suggestedName: String, extension: String)

expect suspend fun pickBackupFile(extension: String): PickedFile?
