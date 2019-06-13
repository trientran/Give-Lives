package net.givelives.givelives.organ.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MyOrganPostsFragment extends OrganPostListFragment {

    public MyOrganPostsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All my posts
        return databaseReference.child("user-organposts")
                .child(getUid());
    }
}
