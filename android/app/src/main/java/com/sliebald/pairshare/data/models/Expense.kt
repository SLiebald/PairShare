package com.sliebald.pairshare.data.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

/**
 * Single [Expense] that is related to an [ExpenseList] collection in firestore.
 */
data class Expense(
        val userID: String? = null,
        val userName: String? = null,
        val amount: Double = 0.toDouble(),
        val comment: String? = null,
        val timeOfExpense: Date? = null,
        val imagePath: String? = null,
        val thumbnailPath: String? = null
) {
    @ServerTimestamp
    val created: Date? = null
}
