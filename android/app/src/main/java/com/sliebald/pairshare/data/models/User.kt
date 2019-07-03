package com.sliebald.pairshare.data.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

/**
 * Data class for Users stored in firestore.
 *
 * @param mail The mail address of the User.
 * @param username Username of the User.
 * @param fcmToken FCM Token used for cloud notifications on events.
 * @constructor creates a new User
 */
data class User(
        val mail: String? = null,
        var username: String? = null,
        var fcmToken: String? = null
) {

    /**
     * Time the [User] was created.
     */
    @ServerTimestamp
    val created: Date? = null

    /**
     * Time the [User] was modified.
     */
    @ServerTimestamp
    var modified: Date? = null

}
