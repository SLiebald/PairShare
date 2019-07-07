package com.sliebald.pairshare.utils

import android.graphics.Color
import com.sliebald.pairshare.MyApplication
import com.sliebald.pairshare.R
import com.sliebald.pairshare.data.models.ExpenseList

/**
 * Utilityclass for managing [com.sliebald.pairshare.data.models.ExpenseList].
 */
object ExpenseListUtils {

    /**
     * Returns the total difference of the sum of all expenses of the given userId minus the sum
     * of expenses of other users.
     *
     * @param userId      User to count positively
     * @param expenseList Expenselist to check.
     * @return Difference as double. Positive values mean the given userId paid more, negative
     * values mean he paid less than the other user(s) in that list.
     */
    fun getExpenseDifferenceFor(userId: String, expenseList: ExpenseList): Double {
        var difference = 0.0

        for ((key, value) in expenseList.sharerInfo!!) {
            if (key == userId)
                difference += value.sumExpenses
            else
                difference -= value.sumExpenses
        }
        return difference
    }

    /**
     * Return the [Color] matching to the difference in spending between the current and
     * other users. Positive values --> Green, Negative --> Red.
     *
     * @param difference Difference in spending between current and other users.
     * @return Color based on the difference.
     */
    fun getExpenseDifferenceColor(difference: Double): Int {

        return if (difference >= 100)
            MyApplication.context
                    .resources.getColor(R.color.balance_positive, null)
        else if (difference > 0)
            MyApplication.context
                    .resources.getColor(R.color.balance_slight_positive, null)
        else if (difference < 0 && difference > -100)
            MyApplication.context
                    .resources.getColor(R.color.balance_slight_negative, null)
        else if (difference <= -100)
            MyApplication.context
                    .resources.getColor(R.color.balance_negative, null)
        else
            Color.BLACK
    }

}
