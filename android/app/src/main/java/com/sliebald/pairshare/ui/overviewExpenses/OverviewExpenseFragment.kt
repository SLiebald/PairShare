package com.sliebald.pairshare.ui.overviewExpenses

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.sliebald.pairshare.MainActivityViewModel
import com.sliebald.pairshare.R
import com.sliebald.pairshare.data.Repository
import com.sliebald.pairshare.data.models.Expense
import com.sliebald.pairshare.data.models.ExpenseList
import com.sliebald.pairshare.data.models.User
import com.sliebald.pairshare.databinding.FragmentOverviewExpenseBinding
import com.sliebald.pairshare.utils.ExpenseListUtils

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

    private val mViewModelMain: MainActivityViewModel by activityViewModels()


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
                .setPrefetchDistance(5)
                .setPageSize(15)
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
                Log.d(TAG, "Binding Expense: ${expense.comment}")
            }

            override fun onCreateViewHolder(group: ViewGroup, i: Int): ExpenseHolder {
                val view = LayoutInflater.from(group.context)
                        .inflate(R.layout.recycler_item_expense, group, false)
                return ExpenseHolder(view)
            }
        }

        mBinding.rvLastExpenses.adapter = expenseAdapter
        mBinding.rvLastExpenses.layoutManager = LinearLayoutManager(context)

        mViewModelMain.activeExpenseList.observe(this, Observer { list ->

            val obs = Observer<User> {
                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                val amountMe = list.sharerInfo
                        ?.get(uid)
                        ?.sumExpenses
                        ?: 0.0

                val amountOther = list.sharerInfo
                        ?.filterKeys { it != uid }
                        ?.map { it.value.sumExpenses }
                        ?.sum()
                        ?: 0.0

                mBinding.tvAmountMe.text = amountMe.toString()
                mBinding.tvAmountMe.setTextColor(ExpenseListUtils.getExpenseDifferenceColor
                (amountMe - amountOther))
                mBinding.tvAmountOther.text = amountOther.toString()
                mBinding.tvAmountOther.setTextColor(ExpenseListUtils.getExpenseDifferenceColor
                (amountOther - amountMe))
            }
            mViewModelMain.getUser().observe(this, obs)
            mViewModelMain.getUser().removeObserver(obs)

        })

    }


}
