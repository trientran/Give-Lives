package net.givelives.givelives.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class OrganPost {

    public String uid;
    public String title;
    public Long postDateTime; // this is used to map time of posting. do not delete this
    public String recipientName;
    public String placeId;
    public String recipientPhone;
    public String recipientBloodType;
    public String organ;
    public String recipientMessage;

    public int likeCount = 0;
    public Map<String, Boolean> likes = new HashMap<>();

    public int donorCount = 0;
    public Map<String, Object> donorList = new HashMap<>();

    public OrganPost() {
        // Default constructor required for calls to DataSnapshot.getValue(OrganPost.class)
    }

    public OrganPost(String uid, String recipient, String placeId, String phone,
                     String bloodType, String organ, String message) {
        this.uid = uid;
        this.title = organ + " for " + recipient;
        this.recipientName = recipient;
        this.placeId = placeId;
        this.recipientPhone = phone;
        this.recipientBloodType = bloodType;
        this.organ = organ;
        this.recipientMessage = message;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("title", title);
        result.put("postDateTime", ServerValue.TIMESTAMP);
        result.put("recipientName", recipientName);
        result.put("placeId", placeId);
        result.put("recipientPhone", recipientPhone);
        result.put("recipientBloodType", recipientBloodType);
        result.put("organ", organ);
        result.put("recipientMessage", recipientMessage);
        result.put("likeCount", likeCount);
        result.put("likes", likes);
        result.put("donorCount", donorCount);
        result.put("donorList", donorList);

        return result;
    }
    // [END post_to_map]


    @Override
    public String toString() {
        String sb = title + '\'' +
                ", phone:'" + recipientPhone + '\'' +
                ", blood type:'" + recipientBloodType + '\'' +
                ", message:'" + recipientMessage + '\'' +
                '.';
        return sb;
    }
}
// [END post_class]
