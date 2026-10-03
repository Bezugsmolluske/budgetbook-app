package de.poljansek.budgetbook.feature.importing

import de.poljansek.budgetbook.core.money.amountInputToCents
import de.poljansek.budgetbook.core.network.dto.BookType

data class SignedAmountInput(
    val negative: Boolean,
    val digits: String,
) {
    fun bookType(): BookType? {
        val cents = amountInputToCents(digits) ?: return null
        if (cents == 0L) return null
        return if (negative) BookType.EXPENSE else BookType.INCOME
    }
}
