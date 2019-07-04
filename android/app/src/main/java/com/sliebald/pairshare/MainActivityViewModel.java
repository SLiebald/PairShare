package com.sliebald.pairshare;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.sliebald.pairshare.data.Repository;
import com.sliebald.pairshare.data.models.ExpenseList;
import com.sliebald.pairshare.data.models.User;

public class MainActivityViewModel extends ViewModel {

    /**
     * The currently selected {@link ExpenseList} as {@link LiveData} to observe
     */
    private LiveData<ExpenseList> currentExpenseList;

    /**
     * The currently logged in User;
     */
    private LiveData<User> user;


    public MainActivityViewModel() {
        this.currentExpenseList = Repository.INSTANCE.getActiveExpenseList();
    }

    /**
     * Get the currently selected {@link ExpenseList} as {@link LiveData} to observe
     *
     * @return ExpenseList as LiveData
     */
    LiveData<ExpenseList> getActiveExpenseList() {
        return currentExpenseList;
    }


    /**
     * Calls the repository to create the user if it doesn't exist.
     */
    void userLoggedIn() {
        Repository.INSTANCE.checkNewUser();
    }

    /**
     * Get the currently logged in user.
     *
     * @return The {@link User} currently logged in.
     */
    public LiveData<User> getUser() {
        if (user == null)
            user = Repository.INSTANCE.getCurrentUser();
        return user;
    }
}
