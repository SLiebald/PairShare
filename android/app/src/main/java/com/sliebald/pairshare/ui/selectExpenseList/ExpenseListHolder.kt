package com.sliebald.pairshare.ui.selectExpenseList

import android.content.SharedPreferences
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.sliebald.pairshare.MyApplication
import com.sliebald.pairshare.R
import com.sliebald.pairshare.data.models.ExpenseList
import com.sliebald.pairshare.utils.ExpenseListUtils
import com.sliebald.pairshare.utils.PreferenceUtils
import java.util.*

/**
 * [RecyclerView.ViewHolder] that holds required data for the [ExpenseList] [RecyclerView].
 */
class ExpenseListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    /**
     * Name of the [ExpenseList] that added the expense.
     */
    private val mName: TextView = itemView.findViewById(R.id.label_expense_list_name)

    /**
     * Current balance of this [ExpenseList] (from the logged in users perspective).
     */
    private val mBalance: TextView = itemView.findViewById(R.id.tv_list_balance)

    /**
     * [CardView] layout that will display the [ExpenseList] information.
     */
    private val mCardView: CardView = itemView.findViewById(R.id.cv_expense_list_item)

    /**
     * ID of the list.
     */
    private lateinit var mListID: String

    /**
     * Bind the [ExpenseList] to the view.
     *
     *  @param expenseList The [ExpenseList] to display.
     *  @param listID the ID of the given [ExpenseList]
     */
    fun bind(expenseList: ExpenseList, listID: String) {
        mListID = listID
        setListName(expenseList.listName)
        setBalance(expenseList)
        mCardView.setOnClickListener { PreferenceUtils.selectedSharedExpenseListID = mListID }
        // Change the background of all shown lists depending on which one is selected.
        PreferenceUtils.registerActiveListChangedListener(SharedPreferences
                .OnSharedPreferenceChangeListener { sharedPreferences, key ->
                    if (key != null && key == PreferenceUtils.PREFERENCE_KEY_SELECTED_EXPENSE) {
                        val changedList = sharedPreferences.getString(key, "")
                        if (changedList != null && changedList != mListID) {
                            setBackgroundColor(R.color.white)
                        } else {
                            setBackgroundColor(R.color.colorPrimaryLight)
                        }
                    }
                })
        val selectedList = PreferenceUtils.selectedSharedExpenseListID
        if (selectedList == mListID) {
            setBackgroundColor(R.color.colorPrimaryLight)
        }
    }

    /**
     * Set the background color of this [CardView] to the given color.
     * @param color Resource id of the desired color
     */
    private fun setBackgroundColor(color: Int) {
        mCardView.setCardBackgroundColor(MyApplication.context
                .resources.getColor(color, null))
    }

    /**
     * Set the name of the [ExpenseList] in the view.
     *
     * @param listName The [ExpenseList]s name.
     */
    private fun setListName(listName: String?) {
        mName.text = listName
    }

    /**
     * Set the balance of the [ExpenseList] in the view. Shows the balance from the perspective
     * of the logged in user.
     *
     * @param expenseList the [ExpenseList] to calculate the value from.
     */
    private fun setBalance(expenseList: ExpenseList) {
        val myId = FirebaseAuth.getInstance().uid
        //use int for rounding
        val difference = ExpenseListUtils.getExpenseDifferenceFor(myId!!, expenseList)
        mBalance.setTextColor(ExpenseListUtils.getExpenseDifferenceColor(difference))
        mBalance.text = String.format(Locale.GERMAN, "%.2f€", difference)
    }
}