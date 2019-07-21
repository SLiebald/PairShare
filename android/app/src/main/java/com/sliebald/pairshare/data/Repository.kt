package com.sliebald.pairshare.data

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.sliebald.pairshare.data.models.Expense
import com.sliebald.pairshare.data.models.ExpenseList
import com.sliebald.pairshare.data.models.ExpenseSummary
import com.sliebald.pairshare.data.models.User
import com.sliebald.pairshare.utils.PreferenceUtils
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * Main Repository Class for Pairshare. Responsible for accessing firebase (especially firestore).
 */
object Repository {


    /**
     * Interface for reporting results back to the caller. -1= error, 0=success
     */
    interface ResultCallback {
        fun reportResult(resultCode: Int)
    }

    /**
     * Firestore key for the expense_lists collection. Collection holds [ExpenseList]
     * Documents.
     */
    private const val COLLECTION_KEY_EXPENSE_LISTS = "expense_lists"

    /**
     * Firestore key for the users collection. Collection holds [User] Documents.
     */
    private const val COLLECTION_KEY_USERS = "users"

    /**
     * Firestore key for the expense collections. Sub-collection of a [ExpenseList]
     * Document in the expense_lists collection. Holds [Expense] Documents.
     */
    private const val COLLECTION_KEY_EXPENSE = "expenses"


    // ExpenseList field names
    private const val DOC_EXPENSE_LIST_SHARERS = "sharers"
    private const val DOC_EXPENSE_LIST_SHARER_INFO = "sharerInfo"
    private const val DOC_EXPENSE_LIST_SHARER_INFO_NUM_EXPENSES = "numExpenses"
    private const val DOC_EXPENSE_LIST_SHARER_INFO_SUM_EXPENSES = "sumExpenses"
    private const val DOC_EXPENSE_LIST_MODIFIED = "modified"

    // User field names
    private const val DOC_USER_MAIL = "mail"

    // General fields (can occur in multiple objects
    private const val DOC_CREATED = "created"


    /**
     * Tag for logging.
     */
    private val TAG = Repository::class.java.simpleName


    /**
     * The currently logged in firebase user.
     */
    private val fbUser: FirebaseUser?
        get() {
            return FirebaseAuth.getInstance().currentUser
        }


    /**
     * The currently logged in firebase user.
     */
    private val mDb: FirebaseFirestore
        get() {
            return Firebase.firestore
        }


    /**
     * Livedata for the currently selected ExpenseList.
     */
    private val activeExpenseList: MutableLiveData<ExpenseList> = MutableLiveData()


    init {
        PreferenceUtils.registerActiveListChangedListener(SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> updateActiveExpenseList() })
    }

    /**
     * Gets the currently logged in [User] and returns the result as [LiveData].
     *
     */
    fun getCurrentUser(): LiveData<User> {

        val user = MutableLiveData<User>()

        if (fbUser != null)
            mDb.collection(COLLECTION_KEY_USERS)
                    .document(fbUser!!.uid)
                    .get()
                    .addOnSuccessListener { documentSnapshot -> user.postValue(documentSnapshot.toObject(User::class.java)) }
        return user
    }


    /**
     * Creates a new entry for the currently logged in User in Firestore.
     * If the user already exists, nothing is changed.
     */
    fun checkNewUser() {
        if (fbUser == null) {
            Log.d(TAG, "FirebaseUser null (not authenticated)")
            return
        }

        val userName: String = if (fbUser!!.displayName != null && fbUser!!.displayName!!
                        .isNotEmpty())
            fbUser!!.displayName!!
        else if (fbUser!!.email != null)
            fbUser!!.email!!
        else
            "unknown"

        //Get the firebase cloud messaging token, then add the user or update him.
        //TODO: Token should be monitored in case it changes:
        // https://firebase.google.com/docs/cloud-messaging/android/first-message
        // #access_the_registration_token
        FirebaseInstanceId.getInstance().instanceId
                .addOnSuccessListener { instanceIdResult ->
                    val token = instanceIdResult.token
                    val user = User(fbUser!!.email!!.toLowerCase(), userName, token)

                    mDb.collection(COLLECTION_KEY_USERS).document(fbUser!!.uid).get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val document = task.result
                                    if (document != null && !document.exists()) {
                                        Firebase.firestore.collection(COLLECTION_KEY_USERS)
                                                .document(fbUser!!.uid).set(user)
                                    } else if (document != null) {
                                        // If user already exists, update him if token differs.
                                        val userUpdate = document.toObject(User::class.java)
                                        if (userUpdate != null && (userUpdate.fcmToken == null || userUpdate.fcmToken != token)) {
                                            userUpdate.fcmToken = token
                                            userUpdate.modified = Calendar.getInstance().time
                                            mDb.collection(COLLECTION_KEY_USERS).document(fbUser!!.uid).set(userUpdate)
                                        }
                                    }
                                }
                            }
                }
    }


    /**
     * Gets a [Query] for the expenseLists of the user. Can be used as input to create a
     * [com.firebase.ui.firestore.FirestoreRecyclerAdapter] for displaying the data in a
     * [androidx.recyclerview.widget.RecyclerView].
     *
     * @return The [Query] object
     */
    fun getExpenseListsQuery(): Query {
        return mDb.collection(COLLECTION_KEY_EXPENSE_LISTS)
                .whereArrayContains(DOC_EXPENSE_LIST_SHARERS, fbUser!!.uid)
                .orderBy(DOC_EXPENSE_LIST_MODIFIED)
    }

    /**
     * Gets a [Query] for the expenses of currently selected list. Can be used as input to
     * create a [com.firebase.ui.firestore.FirestoreRecyclerAdapter] or
     * [com.firebase.ui.firestore.paging.FirestorePagingAdapter] for displaying the data in a
     * [androidx.recyclerview.widget.RecyclerView].
     *
     * @return The [Query] object
     */
    fun getExpensesForActiveListQuery(): Query {
        return mDb.collection(COLLECTION_KEY_EXPENSE_LISTS)
                .document(PreferenceUtils.selectedSharedExpenseListID)
                .collection(COLLECTION_KEY_EXPENSE)
                .orderBy(DOC_CREATED, Query.Direction.DESCENDING)
    }


    /**
     * Create a new ExpenseList for the own user and the invited user based on his email.
     *
     * @param listName Name of the new [ExpenseList].
     * @param invite   Email address of the [User] that will be invited to the list.
     * @param callback Returns the result. 0 if operation went through, -1 if the other User
     * wasn't found.
     */
    fun createNewExpenseList(listName: String, invite: String, callback: ResultCallback) {
        //TODO: addOnCompleteListener only works when the phone is online. add a check and abort
        // otherwise.

        if (fbUser == null) {
            Log.d(TAG, "Firebase user not authenticated")
            return
        }
        //Get the other invited User.

        Log.d(TAG, "adding expenselist: searching for user")
        mDb.collection(COLLECTION_KEY_USERS).whereEqualTo(DOC_USER_MAIL,
                invite.toLowerCase()).get().addOnCompleteListener { task ->
            Log.d(TAG, "adding expenselist: found a user")

            // if the invited user is found, get his id and add him to the sharerinfo too.
            if (task.isSuccessful && task.result != null && task.result!!.documents.isNotEmpty()) {
                val documentSnapshot = task.result!!.documents[0]
                Log.d(TAG, "adding expenselist: got user document")

                if (documentSnapshot.id != fbUser!!.uid) {
                    val sharers = ArrayList<String>(2)
                    sharers.add(fbUser!!.uid)
                    sharers.add(documentSnapshot.id)

                    val sharerInfo = HashMap<String, ExpenseSummary>()
                    sharerInfo[fbUser!!.uid] = ExpenseSummary()
                    sharerInfo[documentSnapshot.id] = ExpenseSummary()

                    val expenseList = ExpenseList(listName, sharers, sharerInfo)

                    // Add the new expenslist to the collection and report success back.
                    Log.d(TAG, "adding expenselist: adding list")
                    mDb.collection(COLLECTION_KEY_EXPENSE_LISTS).add(expenseList)
                            .addOnSuccessListener { callback.reportResult(0) }
                }
            } else {
                callback.reportResult(-1)
            }
        }
    }


    /**
     * Upload image to firebase storage. Also create a thumbnail and also upload it.
     */
    private fun uploadImage(image: Bitmap, path: String): String {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        val imageRef = storageRef.child(path)
        val byteStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteStream)
        imageRef.putBytes(byteStream.toByteArray())
                .addOnFailureListener { e ->
                    Log.d(TAG,
                            "Error on image upload: " + e.message)
                }
                .addOnSuccessListener { Log.d(TAG, "Upload Success") }
        return imageRef.path
    }

    /**
     * Adds the given expense to the currently selected List. Adds the ID of the currently logged
     * in user to the logged expense.
     *
     * @param image The image to add to the expense
     * @param thumbnail The thumbnail to add to the expense.
     */
    fun addExpense(username: String, amount: Double, comment: String, time: Date, image:
    Bitmap? = null, thumbnail: Bitmap? = null) {

        if (fbUser == null) {
            Log.d(TAG, "Firebase user not authenticated")
            return
        }

        var imagePath: String? = null
        var thumbnailPath: String? = null

        if (image != null && thumbnail != null) {
            imagePath = uploadImage(image, "images/" + UUID.randomUUID().toString() + ".jpeg")
            thumbnailPath = uploadImage(thumbnail, "thumbnails/" + UUID.randomUUID().toString() +
                    ".jpeg")
        }

        val expense = Expense(fbUser!!.uid, username, amount, comment, time, imagePath,
                thumbnailPath)

        val userSharerInfo = DOC_EXPENSE_LIST_SHARER_INFO + "." + fbUser!!.uid
        val affectedListDocument = mDb.collection(COLLECTION_KEY_EXPENSE_LISTS)
                .document(PreferenceUtils.selectedSharedExpenseListID)
        val expenseDocument = affectedListDocument.collection(COLLECTION_KEY_EXPENSE).document()

        // add the new expense and update the counters in the parent list as batch operation
        // Using increment operation avoids inconsistencies in case of multiple users adding
        // expenses at the same time.
        val batch = mDb.batch()
        batch.set(expenseDocument, expense)
        batch.update(affectedListDocument,
                "$userSharerInfo.$DOC_EXPENSE_LIST_SHARER_INFO_SUM_EXPENSES",
                FieldValue.increment(expense.amount),
                "$userSharerInfo.$DOC_EXPENSE_LIST_SHARER_INFO_NUM_EXPENSES",
                FieldValue.increment(1))
        batch.commit()
    }


    /**
     * [android.content.SharedPreferences.OnSharedPreferenceChangeListener] for updating
     * the livedata if another list was selected. Cannot be a local variable, as it might get
     * garbage collected in that case. TODO: check if it works with kotlin
     */
    //private var onPrefChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    /**
     * Get the [ExpenseList] that is currently selected from firestore,
     *
     * @return The [ExpenseList] as [LiveData].
     */
    fun getActiveExpenseList(): LiveData<ExpenseList> {
        updateActiveExpenseList()
        return activeExpenseList
    }


    /**
     * Helpermethod to update the [LiveData] [ExpenseList] object if the user changes
     * the active list.
     */
    private fun updateActiveExpenseList() {
        Log.d(TAG, "Active List changed, updating LiveData.")
        //TODO: cleanup old snapshotlisteners required?
        if (PreferenceUtils.selectedSharedExpenseListID.isEmpty())
            return
        mDb.collection(COLLECTION_KEY_EXPENSE_LISTS)
                .document(PreferenceUtils.selectedSharedExpenseListID)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                    } else if (snapshot != null && snapshot.exists()) {
                        val list = snapshot.toObject(ExpenseList::class.java)
                        activeExpenseList.postValue(list)
                    }
                }
    }


}
