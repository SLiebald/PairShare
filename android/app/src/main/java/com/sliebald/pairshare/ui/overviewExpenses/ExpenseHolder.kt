package com.sliebald.pairshare.ui.overviewExpenses

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.sliebald.pairshare.MyApplication.Companion.context
import com.sliebald.pairshare.R
import com.sliebald.pairshare.data.models.Expense
import com.sliebald.pairshare.ui.imagePopup.ImagePopup
import com.sliebald.pairshare.utils.GlideApp
import java.text.SimpleDateFormat
import java.util.*


/**
 * [androidx.recyclerview.widget.RecyclerView.ViewHolder] class representing the
 * [View] of a single Expense
 *
 * Create the [ExpenseHolder] for an Expense.
 *
 * @param itemView The layout view the item should be bound to.
 */
class ExpenseHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val mComment: TextView = itemView.findViewById(R.id.tv_label_comment)
    private val mExpenseDate: TextView = itemView.findViewById(R.id.tv_label_date_name)
    private val mExpenseAmount: TextView = itemView.findViewById(R.id.tv_label_amount)
    private val mCardView: CardView = itemView.findViewById(R.id.cv_expense_item)
    private val mImageView: ImageView = itemView.findViewById(R.id.iv_thumbnail)

    /**
     * Binds the relevant fields of the [Expense] to the layout views.
     *
     * @param expense The [Expense] to bind.
     */
    fun bind(expense: Expense) {

        val myId = FirebaseAuth.getInstance().uid

        // own expenses shifted to the right, others to the left.
        val params = mCardView.layoutParams as ViewGroup.MarginLayoutParams
        if (expense.userID == myId) {
            mCardView.setCardBackgroundColor(context
                    .resources.getColor(R.color.balance_slight_positive, null))
            params.marginEnd = 15
            params.marginStart = 120
        } else {
            mCardView.setCardBackgroundColor(context
                    .resources.getColor(R.color.balance_slight_negative, null))
            params.marginStart = 15
            params.marginEnd = 120
        }
        mCardView.requestLayout()
        // Set the comment.
        mComment.text = expense.comment

        // Set the date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN)
        mExpenseDate.text = String.format("%s - %s",
                dateFormat.format(expense.timeOfExpense), expense.userName)
        // Set expense amount
        mExpenseAmount.text = String.format(Locale.GERMAN, "%.2f€", expense.amount)
        // Load thumbnail preview from firestore if a image is attached.
        if (expense.thumbnailPath != null) {
            val firebaseStorage = FirebaseStorage.getInstance()
            val thumbnailRef = firebaseStorage.reference.child(expense.thumbnailPath)
            val progressDrawable = CircularProgressDrawable(context)
            progressDrawable.strokeWidth = 5f
            progressDrawable.centerRadius = 30f
            progressDrawable.start()
            GlideApp.with(context)
                    .load(thumbnailRef)
                    .placeholder(progressDrawable)
                    .into(mImageView)
        } else {
            mImageView.visibility = View.GONE
        }

        if (expense.imagePath != null) {
            mCardView.setOnClickListener {
                ImagePopup(context, R.layout.popup_image, it, FirebaseStorage.getInstance().reference.child(expense.imagePath), null)
            }
        }
    }
}