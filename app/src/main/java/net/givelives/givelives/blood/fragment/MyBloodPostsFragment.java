package net.givelives.givelives.blood.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MyBloodPostsFragment extends BloodPostListFragment {

    public MyBloodPostsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All my posts
        return databaseReference.child("user-bloodposts")
                .child(getUid());
    }
}
