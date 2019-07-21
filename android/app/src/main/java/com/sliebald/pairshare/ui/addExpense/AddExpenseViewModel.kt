package com.sliebald.pairshare.ui.addExpense

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sliebald.pairshare.data.Repository
import com.sliebald.pairshare.data.models.Expense
import java.util.*

/**
 * Viewmodel for the [AddExpenseFragment]. Handling interaction with the [Repository]
 * and implements other logic
 */
class AddExpenseViewModel : ViewModel() {

    /**
     * Uri of the latest take image that should be added to the [Expense].
     */
    internal var latestImagePath: String? = null

    /**
     * Image to be added to the expense.
     */
    var image: Bitmap? = null

    /**
     * Small footprint thumbnail of the image to be added.
     */
    var thumbnail: Bitmap? = null


    /**
     * Calender used for selecting the current time. exposed as [LiveData].
     */
    val calendar: MutableLiveData<Calendar> = MutableLiveData(Calendar.getInstance())

    /**
     * Updates the calendar with the given year/month/day
     *
     * @param year       Year to set.
     * @param month      Month to set.
     * @param dayOfMonth Day to set.
     */
    internal fun setDate(year: Int, month: Int, dayOfMonth: Int) {
        val cal = calendar.value
        if (cal != null) {
            cal.set(year, month, dayOfMonth)
            calendar.value = cal
        }
    }


    /**
     * Add an expense to the currently selected list.
     *
     * @param amount  Amount of money spent.
     * @param comment Comment for the expense (e.g. reason).
     * @param username Current username.
     */
    internal fun addExpense(amount: Double?, comment: String, username: String) {
        Repository.addExpense(username, amount!!, comment, calendar.value!!.time,
                image,
                thumbnail)
        clearImage()
    }

    /**
     * Clear the imagedetails after submitting an expense
     */
    private fun clearImage() {
        this.image = null
        this.thumbnail = null
        latestImagePath = null
    }

    companion object {
        /**
         * Tag for logging.
         */
        private val TAG = AddExpenseViewModel::class.java.simpleName
    }
}
