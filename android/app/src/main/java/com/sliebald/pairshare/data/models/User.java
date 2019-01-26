package com.sliebald.pairshare.data.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * PoJo Object for Users stored in firestore.
 */
public class User {

    private String mail;

    @ServerTimestamp
    private Date created;
    @ServerTimestamp
    private Date modified;


    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }
}