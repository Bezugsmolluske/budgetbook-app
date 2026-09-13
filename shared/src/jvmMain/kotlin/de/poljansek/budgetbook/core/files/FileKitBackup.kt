package de.poljansek.budgetbook.core.files

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write

internal suspend fun saveBackupFileWithFileKit(
    bytes: ByteArray,
    suggestedName: String,
    extension: String,
) {
    val file = FileKit.openFileSaver(
        suggestedName = suggestedName,
        defaultExtension = extension,
    )
    file?.write(bytes)
}

internal suspend fun pickBackupFileWithFileKit(extension: String): PickedFile? {
    val file = FileKit.openFilePicker(type = FileKitType.File(extensions = listOf(extension)))
        ?: return null
    return PickedFile(name = file.name, bytes = file.readBytes())
}
