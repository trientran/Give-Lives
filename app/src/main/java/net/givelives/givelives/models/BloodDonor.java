package net.givelives.givelives.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class BloodDonor extends Donor {

    //public String donorName;
    public String transfusionDate;
    public int donatedAmount;

    public BloodDonor() {
        // Default constructor required for calls to DataSnapshot.getValue(BloodPost.class)
    }

    public BloodDonor(String postTitle,
                      String donorBloodType,
                      String donorPhone,
                      String donorMessage, String transfusionDate, int donatedAmount) {
        super(postTitle, donorBloodType, donorPhone, donorMessage);
        this.transfusionDate = transfusionDate;
        this.donatedAmount = donatedAmount;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("postTitle", postTitle);
        result.put("transfusionDate", transfusionDate);
        result.put("donorBloodType", donorBloodType);
        result.put("donorPhone", donorPhone);
        result.put("donatedAmount", donatedAmount);
        result.put("donorMessage", donorMessage);
        result.put("status", status);

        return result;
    }
    // [END post_to_map]

}
// [END post_class]
