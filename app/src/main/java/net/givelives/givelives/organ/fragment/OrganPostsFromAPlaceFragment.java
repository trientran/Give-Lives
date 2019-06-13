package net.givelives.givelives.organ.fragment;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class OrganPostsFromAPlaceFragment extends OrganPostListFragment {

    private String placeId;

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
    public OrganPostsFromAPlaceFragment() {}


    public static OrganPostsFromAPlaceFragment newInstance(String placeId) {
        OrganPostsFromAPlaceFragment f = new OrganPostsFromAPlaceFragment();

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
        return databaseReference.child("place-organposts")
                .child(getPlaceId());
        // [END posts_from_a_place_query]

    }
}
