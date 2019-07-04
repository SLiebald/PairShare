package com.sliebald.pairshare.data;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sliebald.pairshare.data.models.Expense;
import com.sliebald.pairshare.data.models.ExpenseList;
import com.sliebald.pairshare.data.models.ExpenseSummary;
import com.sliebald.pairshare.data.models.User;
import com.sliebald.pairshare.utils.DB_FIELDS;
import com.sliebald.pairshare.utils.PreferenceUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main Repository Class for Pairshare. Responsible for accessing firebase (especially firestore).
 */
public class Repository {

    /**
     * Firestore key for the expense_lists collection. Collection holds {@link ExpenseList}
     * Documents.
     */
    private static final String COLLECTION_KEY_EXPENSE_LISTS = "expense_lists";

    /**
     * Firestore key for the users collection. Collection holds {@link User} Documents.
     */
    private static final String COLLECTION_KEY_USERS = "users";

    /**
     * Firestore key for the expense collections. Sub-collection of a {@link ExpenseList}
     * Document in the expense_lists collection. Holds {@link Expense} Documents.
     */
    private static final String COLLECTION_KEY_EXPENSE = "expenses";

    /**
     * Tag for logging.
     */
    private static final String TAG = Repository.class.getSimpleName();
    // Lock for Singleton instantiation
    private static final Object LOCK = new Object();

    /**
     * Singleton instance of the Repository.
     */
    @SuppressLint("StaticFieldLeak")
    private static Repository sInstance;

    /**
     * {@link FirebaseFirestore} Firestore database access.
     */
    private FirebaseFirestore mDb;

    /**
     * Livedata for the currently selected ExpenseList.
     */
    private MutableLiveData<ExpenseList> activeExpenseList;


    /**
     * private constructor used by Singleton pattern.
     */
    private Repository() {
        mDb = FirebaseFirestore.getInstance();
    }

    /**
     * Get the currently logged in firebase user.
     *
     * @return FirebaseUser
     */
    private FirebaseUser getFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * Singleton to make sure only one {@link Repository} is used at a time.
     *
     * @return A new {@link Repository} if none exists. If already an instance exists this is
     * returned instead of creating a new one.
     */
    public synchronized static Repository getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new Repository();
                Log.d(TAG, "Made new repository");
            }
        }
        return sInstance;
    }

    /**
     * Gets the currently logged in {@link User} and returns the result as {@link LiveData}.
     *
     */
    public LiveData<User> getCurrentUser() {

        MutableLiveData<User> user = new MutableLiveData<>();
        mDb.collection(COLLECTION_KEY_USERS)
                .document(getFirebaseUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> user.postValue(documentSnapshot.toObject(User.class)));
        return user;
    }


    /**
     * Creates a new entry for the currently logged in User in Firestore.
     * If the user already exists, nothing is changed.
     */
    public void checkNewUser() {
        FirebaseUser fbUser = getFirebaseUser();
        String userName;
        if (fbUser.getDisplayName() != null && !fbUser.getDisplayName().isEmpty())
            userName = fbUser.getDisplayName();
        else
            userName = fbUser.getEmail();
        String id = fbUser.getUid();

        //Get the firebase cloud messaging token, then add the user or update him.
        //TODO: Token should be monitored in case it changes:
        // https://firebase.google.com/docs/cloud-messaging/android/first-message
        // #access_the_registration_token
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(instanceIdResult -> {
                    String token = instanceIdResult.getToken();
                    User user = new User(fbUser.getEmail().toLowerCase(), userName, token);

                    mDb.collection(COLLECTION_KEY_USERS).document(id).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && !document.exists()) {
                                mDb.collection(COLLECTION_KEY_USERS).document(id).set(user);
                            } else if (document != null) {
                                // If user already exists, update him if token differs.
                                User userUpdate = document.toObject(User.class);
                                if (userUpdate != null && (userUpdate.getFcmToken() == null
                                        || !userUpdate.getFcmToken().equals(token))) {
                                    userUpdate.setFcmToken(token);
                                    userUpdate.setModified(Calendar.getInstance().getTime());
                                    mDb.collection(COLLECTION_KEY_USERS).document(id).set(userUpdate);
                                }
                            }
                        }
                    });

                });


    }

    /**
     * Gets a {@link Query} for the expenseLists of the user. Can be used as input to create a
     * {@link com.firebase.ui.firestore.FirestoreRecyclerAdapter} for displaying the data in a
     * {@link androidx.recyclerview.widget.RecyclerView}.
     *
     * @return The {@link Query} object
     */
    public Query getExpenseListsQuery() {
        return mDb.collection(COLLECTION_KEY_EXPENSE_LISTS)
                .whereArrayContains(DB_FIELDS.FIELD_EXPENSE_LIST_SHARERS
                        , getFirebaseUser().getUid())
                .orderBy(DB_FIELDS.FIELD_EXPENSE_LIST_MODIFIED);
    }

    /**
     * Gets a {@link Query} for the expenses of currently selected list. Can be used as input to
     * create a {@link com.firebase.ui.firestore.FirestoreRecyclerAdapter} or
     * {@link com.firebase.ui.firestore.paging.FirestorePagingAdapter} for displaying the data in a
     * {@link androidx.recyclerview.widget.RecyclerView}.
     *
     * @return The {@link Query} object
     */
    public Query getExpensesForActiveListQuery() {
        return mDb.collection(COLLECTION_KEY_EXPENSE_LISTS)
                .document(PreferenceUtils.getSelectedSharedExpenseListID())
                .collection(COLLECTION_KEY_EXPENSE)
                .orderBy(DB_FIELDS.FIELD_CREATED, Query.Direction.DESCENDING);
    }


    /**
     * Create a new ExpenseList for the own user and the invited user based on his email.
     *
     * @param listName Name of the new {@link ExpenseList}.
     * @param invite   Email address of the {@link User} that will be invited to the list.
     * @param callback Returns the result. 0 if operation went through, -1 if the other User
     *                 wasn't found.
     */
    public void createNewExpenseList(String listName, String invite, ResultCallback callback) {
        //TODO: addOnCompleteListener only works when the phone is online. add a check and abort
        // otherwise. Create a new ExpenseList.

        //Get the other invited User.
        Log.d(TAG, "adding expenselist: searching for user");
        mDb.collection(COLLECTION_KEY_USERS).whereEqualTo(DB_FIELDS.FIELD_USER_MAIL,
                invite.toLowerCase()).get().addOnCompleteListener(task -> {
            Log.d(TAG, "adding expenselist: found a user");

            // if the invited user is found, get his id and add him to the sharerinfo too.
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().getDocuments().isEmpty()) {
                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                Log.d(TAG, "adding expenselist: got user document");
                FirebaseUser fbUser = getFirebaseUser();

                if (!documentSnapshot.getId().equals(fbUser.getUid())) {
                    List<String> sharers = new ArrayList<>(2);
                    sharers.add(fbUser.getUid());
                    sharers.add(documentSnapshot.getId());

                    Map<String, ExpenseSummary> sharerInfo = new HashMap<>();
                    sharerInfo.put(fbUser.getUid(), new ExpenseSummary());
                    sharerInfo.put(documentSnapshot.getId(), new ExpenseSummary());

                    ExpenseList expenseList = new ExpenseList(listName, sharers, sharerInfo);

                    // Add the new expenslist to the collection and report success back.
                    Log.d(TAG, "adding expenselist: adding list");
                    mDb.collection(COLLECTION_KEY_EXPENSE_LISTS).add(expenseList)
                            .addOnSuccessListener(documentReference -> callback.reportResult(0));
                }
            } else {
                callback.reportResult(-1);
            }
        });


    }

    private String uploadImage(Bitmap image, String path) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        StorageReference imageRef =
                storageRef.child(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        imageRef.putBytes(baos.toByteArray())
                .addOnFailureListener(e -> Log.d(TAG,
                        "Error on image upload: " + e.getMessage()))
                .addOnSuccessListener(taskSnapshot -> Log.d(TAG, "Upload Success"));
        return imageRef.getPath();
    }

    /**
     * Adds the given expense to the currently selected List. Adds the ID of the currently logged
     * in user to the logged expense.
     *
     * @param image The image to add to the expense
     * @param thumbnail The thumbnail to add to the expense.
     */
    public void addExpense(String username, Double amount, String comment, Date time, Bitmap image, Bitmap thumbnail) {
        FirebaseUser fbUser = getFirebaseUser();

        String imagePath = null;
        String thumbnailPath = null;

        if (image != null && thumbnail != null) {
            imagePath = uploadImage(image, "images/" + UUID.randomUUID().toString() + ".jpeg");
            thumbnailPath = uploadImage(thumbnail, "thumbnails/" + UUID.randomUUID().toString() +
                    ".jpeg");
        }

        Expense expense = new Expense(fbUser.getUid(), username, amount, comment, time, imagePath,
                thumbnailPath);

        String userSharerInfo = DB_FIELDS.FIELD_EXPENSE_LIST_SHARER_INFO + "." + fbUser.getUid();
        DocumentReference affectedListDocument =
                mDb.collection(COLLECTION_KEY_EXPENSE_LISTS)
                        .document(PreferenceUtils.getSelectedSharedExpenseListID());
        DocumentReference expenseDocument =
                affectedListDocument.collection(COLLECTION_KEY_EXPENSE).document();

        // add the new expense and update the counters in the parent list as batch operation
        // Using increment operation avoids inconsistencies in case of multiple users adding
        // expenses at the same time.
        WriteBatch batch = mDb.batch();
        batch.set(expenseDocument, expense);
        batch.update(affectedListDocument,
                userSharerInfo + "." + DB_FIELDS.FIELD_EXPENSE_LIST_SHARER_INFO_SUM_EXPENSES,
                FieldValue.increment(expense.getAmount()),
                userSharerInfo + "." + DB_FIELDS.FIELD_EXPENSE_LIST_SHARER_INFO_NUM_EXPENSES,
                FieldValue.increment(1));
        batch.commit();
    }

    /**
     * {@link android.content.SharedPreferences.OnSharedPreferenceChangeListener} for updating
     * the livedata if another list was selected. Cannot be a global variable, as it might get
     * garbage collected in that case.
     */
    private SharedPreferences.OnSharedPreferenceChangeListener onPrefChangeListener;

    /**
     * Get the {@link ExpenseList} that is currently selected from firestore,
     *
     * @return The {@link ExpenseList} as {@link LiveData}.
     */
    public LiveData<ExpenseList> getActiveExpenseList() {

        if (activeExpenseList == null) {
            activeExpenseList = new MutableLiveData<>();
            onPrefChangeListener = (sharedPreferences, key) -> {
                updateActiveExpenseList();
            };
            PreferenceUtils.registerActiveListChangedListener(onPrefChangeListener);
            Log.d(TAG, "Added activeListChangedListener");
        }
        updateActiveExpenseList();
        return activeExpenseList;
    }

    /**
     * Helpermethod to update the {@link LiveData} {@link ExpenseList} object if the user changes
     * the active list.
     */
    private void updateActiveExpenseList() {
        Log.d(TAG, "Active List changed, updating LiveData.");
        //TODO: cleanup old snapshotlisteners required?
        if (PreferenceUtils.getSelectedSharedExpenseListID() != null)
            mDb.collection(COLLECTION_KEY_EXPENSE_LISTS)
                    .document(PreferenceUtils.getSelectedSharedExpenseListID())
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            ExpenseList list = snapshot.toObject(ExpenseList.class);
                            activeExpenseList.postValue(list);
                        }
                    });
    }


    /**
     * Interface for reporting results back to the caller. -1= error, 0=success
     */
    public interface ResultCallback {
        void reportResult(int resultCode);
    }

}
