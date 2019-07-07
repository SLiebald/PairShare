package com.sliebald.pairshare.ui.addExpenseList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.sliebald.pairshare.R
import com.sliebald.pairshare.databinding.FragmentAddExpenseListBinding
import com.sliebald.pairshare.utils.KeyboardUtils

class AddExpenseListFragment : Fragment() {


    private val mViewModel: AddExpenseListViewModel by viewModels()

    /**
     * Databinding of the corresponding fragment layout.
     */
    private lateinit var mBinding: FragmentAddExpenseListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout
                .fragment_add_expense_list, container, false)
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //mViewModel = ViewModelProviders.of(this).get(AddExpenseListViewModel::class.java)

        //show error messages that occur when the viewmodel fails at creating the desired expense
        // list
        mViewModel.errorMessage.observe(this, Observer { errorMessage ->
            Snackbar.make(mBinding.layoutAddExpenseList, errorMessage,
                    Snackbar.LENGTH_SHORT).show()
        })
        mBinding.button.setOnClickListener {
            mViewModel.createExpenseList(mBinding.etAddExpenseListName.text.toString(),
                    mBinding.etAddExpenseListInvite.text.toString())
        }

        mViewModel.operationSuccessful.observe(this, Observer { successful ->
            if (successful) {
                Snackbar.make(activity!!.findViewById(R.id.main_layout),
                        "Added new List", Snackbar.LENGTH_SHORT).show()
                KeyboardUtils.hideKeyboard(context!!, view!!)
                findNavController().navigateUp()
            }
        })

    }
}
