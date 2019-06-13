package net.givelives.givelives.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class BloodPost {

    public String uid;
    public String title;
    public Long postDateTime; // this is used to map time of posting. do not delete this
    public String recipientName;
    public String placeId;
    public String recipientPhone;
    public String transfusionDate;
    public String transfusionTime;
    public String recipientBloodType;
    public int amountRequired;
    public String recipientMessage;

    public int likeCount = 0;
    public Map<String, Boolean> likes = new HashMap<>();

    public int donorCount = 0;
    public Map<String, Object> donorList = new HashMap<>();
    public int remainingBlood;

    public BloodPost() {
        // Default constructor required for calls to DataSnapshot.getValue(BloodPost.class)
    }

    public BloodPost(String uid, String recipientName, String placeId, String phone,
                     String date, String time, String bloodType, int requiredAmount, String message) {
        this.uid = uid;
        this.title = requiredAmount + "ml blood for " + recipientName;
        this.recipientName = recipientName;
        this.placeId = placeId;
        this.recipientPhone = phone;
        this.transfusionDate = date;
        this.transfusionTime = time;
        this.recipientBloodType = bloodType;
        this.amountRequired = requiredAmount;
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
        result.put("transfusionDate", transfusionDate);
        result.put("transfusionTime", transfusionTime);
        result.put("recipientBloodType", recipientBloodType);
        result.put("amountRequired", amountRequired);
        result.put("recipientMessage", recipientMessage);
        result.put("likeCount", likeCount);
        result.put("likes", likes);
        result.put("donorCount", donorCount);
        result.put("donorList", donorList);
        result.put("remainingBlood", amountRequired);

        return result;
    }
    // [END post_to_map]


    @Override
    public String toString() {
        return  title + '\'' +
                ", phone:'" + recipientPhone + '\'' +
                ", transfusion date:'" + transfusionDate + '\'' +
                ", transfusion time:'" + transfusionTime + '\'' +
                ", blood type:'" + recipientBloodType + '\'' +
                ", amount required:" + amountRequired +
                ", message:'" + recipientMessage + '\'' +
                '.';
    }
}
// [END post_class]
