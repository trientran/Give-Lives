package net.givelives.givelives.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class OrganDonor extends Donor {

    //public String donorName;
    public Long commitDate;

    public OrganDonor() {
        // Default constructor required for calls to DataSnapshot.getValue(BloodPost.class)
    }

    public OrganDonor(String postTitle,
                      String donorBloodType,
                      String donorPhone,
                      String donorMessage) {
        super(postTitle, donorBloodType, donorPhone, donorMessage);
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("postTitle", postTitle);
        result.put("commitDate",  ServerValue.TIMESTAMP);
        result.put("donorBloodType", donorBloodType);
        result.put("donorPhone", donorPhone);
        result.put("donorMessage", donorMessage);
        result.put("status", status);

        return result;
    }
    // [END post_to_map]

}
// [END post_class]
