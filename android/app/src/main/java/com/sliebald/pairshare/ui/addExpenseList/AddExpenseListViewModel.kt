package com.sliebald.pairshare.ui.addExpenseList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.sliebald.pairshare.data.Repository

/**
 * Viewmodel for the [AddExpenseListFragment]. Offers an interface to the [Repository] and
 * returns whether adding a list was successful.
 */
class AddExpenseListViewModel : ViewModel(), Repository.ResultCallback {
    //TODO: externalize Strings

    /**
     * Livedata exposing error messages to the view in case an error occurred.
     *
     * @return Error message as String.
     */
    val errorMessage: MutableLiveData<String> = MutableLiveData()

    /**
     * [Boolean] [LiveData] to indicate an operation was successful.
     *
     * @return
     */

    val operationSuccessful: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * Create a new ExpenseList. Checks that obligatory values are not empty
     *
     * @param listName The name of the list that should be created.
     * @param invite The mailaddress of the user with whom the list should be shared.
     */
    internal fun createExpenseList(listName: String, invite: String) {
        when {
            listName.isEmpty() -> errorMessage.postValue("The lists name cannot be empty!")
            invite.isEmpty() -> errorMessage.postValue("Enter the email of the person you want to invite.")
            else -> Repository.createNewExpenseList(listName, invite, this)
        }
    }

    /**
     * Callback for the [Repository] to report whether the operation was successful or not.
     */
    override fun reportResult(resultCode: Int) {
        when (resultCode) {
            0 -> operationSuccessful.postValue(true)
            else -> errorMessage.postValue("The user you want to share with doesn't exist.")
        }
    }

    companion object {
        private val TAG = AddExpenseListViewModel::class.java.simpleName
    }
}