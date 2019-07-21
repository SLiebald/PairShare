package com.sliebald.pairshare.ui.selectExpenseList

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.sliebald.pairshare.MainActivityViewModel
import com.sliebald.pairshare.R
import com.sliebald.pairshare.data.Repository
import com.sliebald.pairshare.data.models.ExpenseList
import com.sliebald.pairshare.data.models.User
import com.sliebald.pairshare.databinding.FragmentSelectExpenseListBinding

/**
 * Fragment for managing the overview of [ExpenseList]s the current user is involved with.
 */
class SelectExpenseListFragment : Fragment() {

    /**
     * Adapter for displaying available expenseLists.
     */
    private lateinit var expenseListsAdapter: FirestoreRecyclerAdapter<*, *>

    private val mViewModelMain: MainActivityViewModel by activityViewModels()


    /**
     * Databinding of the corresponding fragment layout.
     */
    private lateinit var mBinding: FragmentSelectExpenseListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout
                .fragment_select_expense_list, container, false)
        mBinding.fab.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_selectExpense_dest_to_addExpenseListFragment))
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mBinding.rvActiveLists.layoutManager = LinearLayoutManager(context)

        mViewModelMain.user.observe(this, Observer<User> {
            Log.d(TAG, "User changed")
            initAdapter()
        })
    }

    private fun initAdapter(){
        val query = Repository.getExpenseListsQuery()

        val options = FirestoreRecyclerOptions.Builder<ExpenseList>()
                .setQuery(query, ExpenseList::class.java)
                .setLifecycleOwner(viewLifecycleOwner)
                .build()

        expenseListsAdapter = object : FirestoreRecyclerAdapter<ExpenseList, ExpenseListHolder>(options) {
            public override fun onBindViewHolder(holder: ExpenseListHolder, position: Int,
                                                 expenseList: ExpenseList) {
                holder.bind(expenseList, snapshots.getSnapshot(position).id)
                Log.d(TAG, "Binding List: ${expenseList.listName!!}")
            }

            override fun onCreateViewHolder(group: ViewGroup, i: Int): ExpenseListHolder {
                val view = LayoutInflater.from(group.context)
                        .inflate(R.layout.recycler_item_expense_list, group, false)
                return ExpenseListHolder(view)
            }
        }
        mBinding.rvActiveLists.adapter = expenseListsAdapter
        expenseListsAdapter.notifyDataSetChanged()

    }

    companion object {

        /**
         * Tag for logging.
         */
        private val TAG = SelectExpenseListFragment::class.java.simpleName
    }

}
