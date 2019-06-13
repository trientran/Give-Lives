package net.givelives.givelives.models;

import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public String fullName;
    public String photoUrl;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String fullName, String photoUrl) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.photoUrl = photoUrl;
    }

}
// [END blog_user_class]
