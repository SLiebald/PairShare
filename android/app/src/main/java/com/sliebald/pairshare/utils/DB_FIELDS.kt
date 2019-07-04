package com.sliebald.pairshare.utils

class DB_FIELDS {
    companion object {

        // ExpenseList field names
        const val FIELD_EXPENSE_LIST_LISTNAME = "listName"
        const val FIELD_EXPENSE_LIST_SHARERS = "sharers"
        const val FIELD_EXPENSE_LIST_SHARER_INFO = "sharerInfo"
        const val FIELD_EXPENSE_LIST_SHARER_INFO_NUM_EXPENSES = "numExpenses"
        const val FIELD_EXPENSE_LIST_SHARER_INFO_SUM_EXPENSES = "sumExpenses"
        const val FIELD_EXPENSE_LIST_MODIFIED = "modified"

        // User field names
        const val FIELD_USER_MAIL = "mail"

        // General fields (can occur in multiple objects
        const val FIELD_CREATED = "created"

    }


}