package de.poljansek.budgetbook.core.files

actual suspend fun saveBackupFile(bytes: ByteArray, suggestedName: String, extension: String) {
    saveBackupFileWithFileKit(bytes, suggestedName, extension)
}

actual suspend fun pickBackupFile(extension: String): PickedFile? =
    pickBackupFileWithFileKit(extension)
