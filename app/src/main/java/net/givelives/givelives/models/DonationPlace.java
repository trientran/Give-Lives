package net.givelives.givelives.models;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by MainAcc on 3/11/2017.
 */
@IgnoreExtraProperties
public class DonationPlace {

    public String placeName;
    public String placeAddress;

    public DonationPlace() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public DonationPlace(String placeName, String placeAddress) {

        this.placeName = placeName;
        this.placeAddress = placeAddress;
    }

}
// [END blog_user_class]