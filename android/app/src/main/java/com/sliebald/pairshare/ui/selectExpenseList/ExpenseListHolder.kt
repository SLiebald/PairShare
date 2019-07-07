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

class ExpenseListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


    private val mName: TextView = itemView.findViewById(R.id.label_expense_list_name)
    private val mBalance: TextView = itemView.findViewById(R.id.tv_list_balance)
    private val mCardView: CardView = itemView.findViewById(R.id.cv_expense_list_item)

    private lateinit var mListID: String


    fun bind(expenseList: ExpenseList, listID: String) {
        mListID = listID
        setListName(expenseList.listName)
        setBalance(expenseList)
        mCardView.setOnClickListener { PreferenceUtils.selectedSharedExpenseListID = mListID }


        PreferenceUtils.registerActiveListChangedListener(SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key != null && key == PreferenceUtils.PREFERENCE_KEY_SELECTED_EXPENSE) {
                val changedList = sharedPreferences.getString(PreferenceUtils.PREFERENCE_KEY_SELECTED_EXPENSE, "")
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

    private fun setBackgroundColor(color: Int) {
        mCardView.setCardBackgroundColor(MyApplication.context
                .resources.getColor(color, null))
    }


    private fun setListName(listName: String?) {
        mName.text = listName
    }

    private fun setBalance(expenseList: ExpenseList) {
        val myId = FirebaseAuth.getInstance().uid
        //use int for rounding
        val difference = ExpenseListUtils.getExpenseDifferenceFor(myId!!, expenseList)
        mBalance.setTextColor(ExpenseListUtils.getExpenseDifferenceColor(difference))
        mBalance.text = String.format(Locale.GERMAN, "%.2f€", difference)

    }

}
