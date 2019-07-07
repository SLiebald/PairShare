package com.sliebald.pairshare.ui.addExpenseList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.sliebald.pairshare.R;
import com.sliebald.pairshare.databinding.FragmentAddExpenseListBinding;
import com.sliebald.pairshare.utils.KeyboardUtils;

import java.util.Objects;

public class AddExpenseListFragment extends Fragment {


    private AddExpenseListViewModel mViewModel;

    /**
     * Databinding of the corresponding fragment layout.
     */
    private FragmentAddExpenseListBinding mBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout
                .fragment_add_expense_list, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(AddExpenseListViewModel.class);

        //show error messages that occur when the viewmodel fails at creating the desired expense
        // list
        mViewModel.getErrorMessage().observe(this,
                errorMessage -> Snackbar.make(mBinding.layoutAddExpenseList, errorMessage,
                        Snackbar.LENGTH_SHORT).show());
        mBinding.button.setOnClickListener(v -> mViewModel.createExpenseList(mBinding.etAddExpenseListName.getText().toString(),
                mBinding.etAddExpenseListInvite.getText().toString()));
        NavController navController =
                Navigation.findNavController(Objects.requireNonNull(this.getView()));

        mViewModel.getOperationSuccessful().observe(this, successful -> {
            if (successful) {
                Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(R.id.main_layout),
                        "Added new List", Snackbar.LENGTH_SHORT).show();
                KeyboardUtils.INSTANCE.hideKeyboard(getContext(), getView());
                navController.navigateUp();
            }
        });

    }
}
