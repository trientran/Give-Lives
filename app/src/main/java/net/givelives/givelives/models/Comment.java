package net.givelives.givelives.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by MainAcc on 14/12/2017.
 */
@IgnoreExtraProperties
public class Comment {
    public String postUid;
    public String commentUid;
    public String author;
    public String text;

    public Comment() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Comment(String postUid, String commentUid, String author, String text) {
        this.postUid = postUid;
        this.commentUid = commentUid;
        this.author = author;
        this.text = text;
    }
}
