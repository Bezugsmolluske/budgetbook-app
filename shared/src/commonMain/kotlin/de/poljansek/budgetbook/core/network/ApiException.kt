package de.poljansek.budgetbook.core.network

open class ApiException(
    val statusCode: Int,
    override val message: String,
) : Exception(message)

class ConflictException(
    message: String = "Die Daten wurden inzwischen geändert.",
) : ApiException(409, message)
