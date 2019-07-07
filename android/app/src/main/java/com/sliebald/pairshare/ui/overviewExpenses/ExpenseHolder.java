package com.sliebald.pairshare.ui.overviewExpenses;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sliebald.pairshare.MyApplication;
import com.sliebald.pairshare.R;
import com.sliebald.pairshare.data.models.Expense;
import com.sliebald.pairshare.utils.GlideApp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * {@link androidx.recyclerview.widget.RecyclerView.ViewHolder} class representing the
 * {@link View} of a single Expense
 */
public class ExpenseHolder extends RecyclerView.ViewHolder {

    private final TextView mComment;
    private final TextView mExpenseDate;
    private final TextView mExpenseAmount;
    private final CardView mCardView;
    private final ImageView mImageView;

    /**
     * Create the {@link ExpenseHolder} for an Expense.
     *
     * @param itemView The layout view the item should be bound to.
     */
    public ExpenseHolder(@NonNull View itemView) {
        super(itemView);
        mComment = itemView.findViewById(R.id.tv_label_comment);
        mExpenseAmount = itemView.findViewById(R.id.tv_label_amount);
        mExpenseDate = itemView.findViewById(R.id.tv_label_date_name);
        mCardView = itemView.findViewById(R.id.cv_expense_item);
        mImageView = itemView.findViewById(R.id.iv_thumbnail);
    }

    /**
     * Binds the relevant fields of the {@link Expense} to the layout views.
     *
     * @param expense The {@link Expense} to bind.
     */
    public void bind(@NonNull Expense expense) {

        String myId = FirebaseAuth.getInstance().getUid();

        // own expenses shifted to the right, others to the left.
        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) mCardView.getLayoutParams();
        if (expense.getUserID().equals(myId)) {
            mCardView.setCardBackgroundColor(MyApplication.Companion.getContext()
                    .getResources().getColor(R.color.balance_slight_positive, null));
            params.setMarginEnd(15);
            params.setMarginStart(120);
        } else {
            mCardView.setCardBackgroundColor(MyApplication.Companion.getContext()
                    .getResources().getColor(R.color.balance_slight_negative, null));
            params.setMarginStart(15);
            params.setMarginEnd(120);
        }
        mCardView.requestLayout();
        // Set the comment.
        mComment.setText(expense.getComment());

        // Set the date
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.GERMAN);
        mExpenseDate.setText(String.format("%s - %s",
                dateFormat.format(expense.getTimeOfExpense()), expense.getUserName()));
        // Set expense amount
        mExpenseAmount.setText(String.format(Locale.GERMAN, "%.2f€", expense.getAmount()));
        // Load thumbnail preview from firestore if a image is attached.
        if (expense.getThumbnailPath() != null) {
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference thumbnailRef =
                    firebaseStorage.getReference().child(expense.getThumbnailPath());
            CircularProgressDrawable progressDrawable =
                    new CircularProgressDrawable(MyApplication.Companion.getContext());
            progressDrawable.setStrokeWidth(5f);
            progressDrawable.setCenterRadius(30f);
            progressDrawable.start();
            GlideApp.with(MyApplication.Companion.getContext())
                    .load(thumbnailRef)
                    .placeholder(progressDrawable)
                    .into(mImageView);
        } else {
            mImageView.setVisibility(View.GONE);
        }
    }


}
