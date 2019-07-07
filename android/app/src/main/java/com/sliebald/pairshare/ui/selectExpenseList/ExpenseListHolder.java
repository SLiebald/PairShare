package com.sliebald.pairshare.ui.selectExpenseList;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.sliebald.pairshare.MyApplication;
import com.sliebald.pairshare.R;
import com.sliebald.pairshare.data.models.ExpenseList;
import com.sliebald.pairshare.utils.ExpenseListUtils;
import com.sliebald.pairshare.utils.PreferenceUtils;

import java.util.Locale;

public class ExpenseListHolder extends RecyclerView.ViewHolder {


    private final TextView mName;
    private final TextView mBalance;
    private final CardView mCardView;

    private String mListID;


    public ExpenseListHolder(@NonNull View itemView) {
        super(itemView);
        mName = itemView.findViewById(R.id.label_expense_list_name);
        mCardView = itemView.findViewById(R.id.cv_expense_list_item);
        mBalance = itemView.findViewById(R.id.tv_list_balance);
    }

    public void bind(@NonNull ExpenseList expenseList, String listID) {
        mListID = listID;
        setListName(expenseList.getListName());
        setBalance(expenseList);
        mCardView.setOnClickListener(view -> {
            PreferenceUtils.Companion.setSelectedSharedExpenseListID(mListID);
        });
        PreferenceUtils.Companion.registerActiveListChangedListener((sharedPreferences,
                                                                     key) -> {
            if (key != null && key.equals(PreferenceUtils.PREFERENCE_KEY_SELECTED_EXPENSE)) {
                String changedList =
                        sharedPreferences.getString(PreferenceUtils.PREFERENCE_KEY_SELECTED_EXPENSE, "");
                if (changedList != null && !changedList.equals(mListID)) {
                    setBackgroundColor(R.color.white);
                } else {
                    setBackgroundColor(R.color.colorPrimaryLight);
                }
            }
        });
        String selectedList =
                PreferenceUtils.Companion.getSelectedSharedExpenseListID();
        if (selectedList.equals(mListID)) {
            setBackgroundColor(R.color.colorPrimaryLight);
        }

    }

    private void setBackgroundColor(int color) {
        mCardView.setCardBackgroundColor(MyApplication.getContext()
                .getResources().getColor(color, null));
    }


    private void setListName(@Nullable String listName) {
        mName.setText(listName);
    }

    private void setBalance(ExpenseList expenseList) {
        String myId = FirebaseAuth.getInstance().getUid();
        //use int for rounding
        double difference = ExpenseListUtils.getExpenseDifferenceFor(myId, expenseList);
        mBalance.setTextColor(ExpenseListUtils.getExpenseDifferenceColor(difference));
        mBalance.setText(String.format(Locale.GERMAN, "%.2f€", difference));

    }

}
