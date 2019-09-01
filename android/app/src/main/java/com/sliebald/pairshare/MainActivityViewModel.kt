package com.sliebald.pairshare

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

import com.sliebald.pairshare.data.Repository
import com.sliebald.pairshare.data.models.ExpenseList
import com.sliebald.pairshare.data.models.User

/**
 * [ViewModel] of the [MainActivity]. Keeps track of data relevant for multiple fragments.
 */
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
    val user: LiveData<User> = Repository.currentUser

    /**
     * Calls the repository to create the user if it doesn't exist.
     */
    internal fun userLoggedIn() {
        Repository.checkNewUser()
    }
}
