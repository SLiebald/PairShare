package com.sliebald.pairshare.ui.addExpenseList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sliebald.pairshare.data.Repository;

public class AddExpenseListViewModel extends ViewModel implements Repository.ResultCallback {
//TODO: externalize Strings

    private static final String TAG = AddExpenseListViewModel.class.getSimpleName();

    private MutableLiveData<String> errorMessage;
    private MutableLiveData<Boolean> operationSuccessful;


    /**
     * Livedata exposing error messages to the view in case an error occurred.
     *
     * @return Error message as String.
     */
    LiveData<String> getErrorMessage() {
        if (errorMessage == null) {
            errorMessage = new MutableLiveData<>();
        }
        return errorMessage;
    }

    /**
     * {@link Boolean} {@link LiveData} to indicate an operation was successful.
     *
     * @return
     */
    LiveData<Boolean> getOperationSuccessful() {
        if (operationSuccessful == null) {
            operationSuccessful = new MutableLiveData<>();
        }
        return operationSuccessful;
    }

    void createExpenseList(String listName, String invite) {
        if (listName.isEmpty()) {
            errorMessage.postValue("The lists name cannot be empty!");
        } else if (invite.isEmpty()) {
            errorMessage.postValue("Enter the email of the person you want to invite.");
        } else {
            Repository.INSTANCE.createNewExpenseList(listName, invite, this);
        }

    }

    @Override
    public void reportResult(int resultCode) {
        switch (resultCode) {
            case 0:
                operationSuccessful.postValue(true);
                break;
            default:
                errorMessage.postValue("The user you want to share with doesn't exist.");
        }
    }
}
