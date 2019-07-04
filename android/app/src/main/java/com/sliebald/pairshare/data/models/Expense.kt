package com.sliebald.pairshare.data.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

/**
 * Single [Expense] that is related to an [ExpenseList] collection in firestore.
 *
 * @param userID The firebase ID of the [User] that created the [Expense].
 * @param userName The username of the [User] that created the [Expense].
 * @param amount The amount of money to add in the [Expense].
 * @param comment The comment describing the [Expense] (optional).
 * @param timeOfExpense Time the [Expense] took place.
 * @param imagePath Firebase storage url for the image assigned to the [Expense] (optional).
 * @param thumbnailPath Firebase storage url for the thumbnail image assigned to the [Expense]
 * (optional).
 * @param created  Time the [Expense] was added. Can be different from the [timeOfExpense].
 * @constructor Creates a new [Expense]
 */
data class Expense(
        val userID: String? = null,
        val userName: String? = null,
        val amount: Double = 0.toDouble(),
        val comment: String? = null,
        val timeOfExpense: Date? = null,
        val imagePath: String? = null,
        val thumbnailPath: String? = null,
        @ServerTimestamp
        val created: Date? = null
)
