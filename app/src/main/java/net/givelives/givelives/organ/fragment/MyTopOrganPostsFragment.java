package net.givelives.givelives.organ.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MyTopOrganPostsFragment extends OrganPostListFragment {

    public MyTopOrganPostsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START my_top_posts_query]
        // My top posts by number of likes
        String myUserId = getUid();
        Query myTopPostsQuery = databaseReference.child("user-organposts").child(myUserId)
                .orderByChild("likeCount");
        // [END my_top_posts_query]

        return myTopPostsQuery;
    }
}
