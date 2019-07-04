package com.sliebald.pairshare.ui.overviewExpenses;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.sliebald.pairshare.R;
import com.sliebald.pairshare.data.Repository;
import com.sliebald.pairshare.data.models.Expense;
import com.sliebald.pairshare.data.models.ExpenseList;
import com.sliebald.pairshare.databinding.FragmentOverviewExpenseBinding;

/**
 * Fragment for managing the overview of {@link ExpenseList}s the current user is involved with.
 */
public class OverviewExpenseFragment extends Fragment {

    /**
     * Tag for logging.
     */
    private static final String TAG = OverviewExpenseFragment.class.getSimpleName();

    /**
     * Paging adapter used for displaying (the latest) expenses with a recyclerview.
     */
    private FirestorePagingAdapter expenseAdapter;

    private FragmentOverviewExpenseBinding mBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout
                .fragment_overview_expense, container, false);
        return mBinding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<Expense> options = new FirestorePagingOptions.Builder<Expense>()
                .setQuery(Repository.INSTANCE.getExpensesForActiveListQuery(), config,
                        Expense.class)
                .setLifecycleOwner(this)
                .build();
        expenseAdapter = new FirestorePagingAdapter<Expense,
                ExpenseHolder>(options) {
            @Override
            public void onBindViewHolder(@NonNull ExpenseHolder holder, int position,
                                         @NonNull Expense expense) {
                holder.bind(expense);
                Log.d(TAG, "Binding Expense: " + expense.getComment());
            }

            @NonNull
            @Override
            public ExpenseHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.recycler_item_expense, group, false);
                return new ExpenseHolder(view);
            }
        };

        mBinding.rvLastExpenses.setAdapter(expenseAdapter);
        mBinding.rvLastExpenses.setLayoutManager(new LinearLayoutManager(getContext()));

    }

}
