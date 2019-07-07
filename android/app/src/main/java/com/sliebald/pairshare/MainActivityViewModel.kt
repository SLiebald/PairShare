package com.sliebald.pairshare

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

import com.sliebald.pairshare.data.Repository
import com.sliebald.pairshare.data.models.ExpenseList
import com.sliebald.pairshare.data.models.User

class MainActivityViewModel : ViewModel() {

    /**
     * The currently selected [ExpenseList] as [LiveData] to observe
     */
    /**
     * Get the currently selected [ExpenseList] as [LiveData] to observe
     *
     * @return ExpenseList as LiveData
     */
    internal val activeExpenseList: LiveData<ExpenseList> = Repository.getActiveExpenseList()

    /**
     * The currently logged in User;
     */
    private var user: LiveData<User> = Repository.getCurrentUser()


    /**
     * Calls the repository to create the user if it doesn't exist.
     */
    internal fun userLoggedIn() {
        Repository.checkNewUser()
    }

    /**
     * Get the currently logged in user.
     *
     * @return The [User] currently logged in.
     */
    fun getUser(): LiveData<User> {
        return user
    }
}
