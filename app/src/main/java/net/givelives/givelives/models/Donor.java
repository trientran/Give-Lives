package net.givelives.givelives.models;

/**
 * Created by MainAcc on 14/12/2017.
 */

public class Donor {
    public String postTitle;
    public String donorBloodType;
    public String donorPhone;
    public String donorMessage;
    public Boolean status;

    public Donor() {
    }

    public Donor(String postTitle,
                 String donorBloodType,
                 String donorPhone,
                 String donorMessage) {
        this.postTitle = postTitle;
        this.donorBloodType = donorBloodType;
        this.donorPhone = donorPhone;
        this.donorMessage = donorMessage;
        this.status = true;
    }
}
