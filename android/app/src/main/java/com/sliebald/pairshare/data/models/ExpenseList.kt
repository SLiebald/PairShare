package com.sliebald.pairshare.data.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

/**
 * Data class for a list of [Expense]s shared between [User]s.
 *
 * @param listName Name of the [ExpenseList].
 * @param sharerInfo [Map] of sharers/[User]s of this list. Key is the firebase Auth user id, Value
 * the [ExpenseSummary] for the according user.
 * @param sharers List of [User]s involved in this [ExpenseList]. Needed as firestore
 * currently cannot check the [sharerInfo] map, where the keys contain the same info.
 * @param created  Time the [ExpenseList] was created.
 * @param modified Time the [ExpenseList] was last modified (e.g. changed the sharerInfo).
 * @constructor Creates a new [ExpenseList]
 */
// TODO: Firebase requires an empty constructor, is there a way to make name/sharers/info nonNull?
data class ExpenseList(
        val listName: String? = null,
        val sharers: List<String>? = null,
        val sharerInfo: Map<String, ExpenseSummary>? = null,
        @ServerTimestamp val created: Date? = null,
        @ServerTimestamp var modified: Date? = null
)
