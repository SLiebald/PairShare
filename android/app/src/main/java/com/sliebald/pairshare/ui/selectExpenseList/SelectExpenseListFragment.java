package com.sliebald.pairshare.ui.selectExpenseList;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;
import com.sliebald.pairshare.R;
import com.sliebald.pairshare.data.Repository;
import com.sliebald.pairshare.data.models.ExpenseList;
import com.sliebald.pairshare.databinding.FragmentSelectExpenseListBinding;

/**
 * Fragment for managing the overview of {@link ExpenseList}s the current user is involved with.
 */
public class SelectExpenseListFragment extends Fragment {

    /**
     * Tag for logging.
     */
    private static final String TAG = SelectExpenseListFragment.class.getSimpleName();

    /**
     * Adapter for displaying available expenseLists.
     */
    private FirestoreRecyclerAdapter expenseListsAdapter;

    /**
     * Databinding of the corresponding fragment layout.
     */
    private FragmentSelectExpenseListBinding mBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout
                .fragment_select_expense_list, container, false);
        mBinding.fab.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_selectExpense_dest_to_addExpenseListFragment));
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Query query = Repository.INSTANCE.getExpenseListsQuery();


            FirestoreRecyclerOptions<ExpenseList> options =
                    new FirestoreRecyclerOptions.Builder<ExpenseList>()
                            .setQuery(query, ExpenseList.class)
                            .setLifecycleOwner(this)
                            .build();

            expenseListsAdapter = new FirestoreRecyclerAdapter<ExpenseList,
                    ExpenseListHolder>(options) {
                @Override
                public void onBindViewHolder(@NonNull ExpenseListHolder holder, int position,
                                             @NonNull ExpenseList expenseList) {
                    holder.bind(expenseList, getSnapshots().getSnapshot(position).getId());
                    Log.d(TAG, "Binding List: " + expenseList.getListName());
                }

                @NonNull
                @Override
                public ExpenseListHolder onCreateViewHolder(@NonNull ViewGroup group, int i) {
                    View view = LayoutInflater.from(group.getContext())
                            .inflate(R.layout.recycler_item_expense_list, group, false);
                    return new ExpenseListHolder(view);
                }
            };

            mBinding.rvActiveLists.setAdapter(expenseListsAdapter);
            mBinding.rvActiveLists.setLayoutManager(new LinearLayoutManager(getContext()));
        }
    }

}
