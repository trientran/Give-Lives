package net.givelives.givelives.blood.fragment;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class BloodPostsFromAPlaceFragment extends BloodPostListFragment {

    private String placeId;

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
    public BloodPostsFromAPlaceFragment() {}


    public static BloodPostsFromAPlaceFragment newInstance(String placeId) {
        BloodPostsFromAPlaceFragment f = new BloodPostsFromAPlaceFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("num", placeId);
        f.setArguments(args);

        return f;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPlaceId(getArguments() != null ? getArguments().getString("num") : "None");
    }


    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START posts_from_a_place_query]
        return databaseReference.child("place-bloodposts")
                .child(getPlaceId());
        // [END posts_from_a_place_query]

    }
}
