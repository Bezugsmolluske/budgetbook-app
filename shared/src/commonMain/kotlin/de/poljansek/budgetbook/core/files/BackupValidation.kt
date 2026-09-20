package de.poljansek.budgetbook.core.files

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

fun requireNonEmptyBackupBytes(bytes: ByteArray) {
    if (bytes.isEmpty()) {
        error("Die ausgewählte Datei ist leer.")
    }
}

fun requireValidJsonBackup(bytes: ByteArray) {
    requireNonEmptyBackupBytes(bytes)
    val root = runCatching { Json.parseToJsonElement(bytes.decodeToString()) }.getOrElse {
        error("Die Datei ist kein gültiges JSON-Backup.")
    }
    val obj = root as? JsonObject ?: error("Die Datei ist kein BudgetBook-Backup.")
    val categories = obj["categories"] as? JsonArray
    val transactions = obj["transactions"] as? JsonArray
    if (categories == null && transactions == null) {
        error("Die Datei ist kein BudgetBook-Backup.")
    }
    if (categories.isNullOrEmpty() && transactions.isNullOrEmpty()) {
        error("Backup enthält keine Kategorien oder Buchungen.")
    }
}
