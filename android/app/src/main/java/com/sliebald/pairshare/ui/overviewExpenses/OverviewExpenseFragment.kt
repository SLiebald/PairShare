package com.sliebald.pairshare.ui.overviewExpenses

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager

import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.sliebald.pairshare.R
import com.sliebald.pairshare.data.Repository
import com.sliebald.pairshare.data.models.Expense
import com.sliebald.pairshare.data.models.ExpenseList
import com.sliebald.pairshare.databinding.FragmentOverviewExpenseBinding

/**
 * Fragment for managing the overview of [ExpenseList]s the current user is involved with.
 */
class OverviewExpenseFragment : Fragment() {

    companion object {
        /**
         * Tag for logging.
         */
        private val TAG = OverviewExpenseFragment::class.java.simpleName
    }


    /**
     * Paging adapter used for displaying (the latest) expenses with a recyclerview.
     */
    private lateinit var expenseAdapter: FirestorePagingAdapter<*, *>

    private lateinit var mBinding: FragmentOverviewExpenseBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout
                .fragment_overview_expense, container, false)
        return mBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val config = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build()

        val options = FirestorePagingOptions.Builder<Expense>()
                .setQuery(Repository.getExpensesForActiveListQuery(), config,
                        Expense::class.java)
                .setLifecycleOwner(this)
                .build()
        expenseAdapter = object : FirestorePagingAdapter<Expense, ExpenseHolder>(options) {
            public override fun onBindViewHolder(holder: ExpenseHolder, position: Int,
                                                 expense: Expense) {
                holder.bind(expense)
                Log.d(TAG, "Binding Expense: " + expense.comment)
            }

            override fun onCreateViewHolder(group: ViewGroup, i: Int): ExpenseHolder {
                val view = LayoutInflater.from(group.context)
                        .inflate(R.layout.recycler_item_expense, group, false)
                return ExpenseHolder(view)
            }
        }

        mBinding.rvLastExpenses.adapter = expenseAdapter
        mBinding.rvLastExpenses.layoutManager = LinearLayoutManager(context)

    }


}
