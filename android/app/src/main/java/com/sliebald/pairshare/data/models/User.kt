package com.sliebald.pairshare.data.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

/**
 * Data class for Users stored in firestore.
 *
 * @param mail The mail address of the User.
 * @param username Username of the User.
 * @param fcmToken FCM Token used for cloud notifications on events.
 * @param created Time the [User] was created.
 * @param modified Time the [User] was last modified.
 * @constructor Creates a new [User]
 */
// TODO: Firebase requires an empty constructor, is there a way to make mail nonNull?
data class User(
        val mail: String? = null,
        var username: String = "unknown",
        var fcmToken: String? = null,
        @ServerTimestamp val created: Date? = null,
        @ServerTimestamp var modified: Date? = null
)