package com.sliebald.pairshare.data.models

/**
 * Data class for holding the summary of all [Expense]s of a user as part of a [ExpenseList].
 *
 * @param numExpenses Number of [Expense]s that a [User] added to the current [ExpenseList].
 * @param sumExpenses Sum of [Expense]s that a [User] added to the current  [ExpenseList].
 * @constructor Creates a new [ExpenseSummary].
 */
data class ExpenseSummary(val numExpenses: Int = 0, val sumExpenses: Double = 0.toDouble())
