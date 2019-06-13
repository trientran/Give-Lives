package net.givelives.givelives.blood.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MyTopBloodPostsFragment extends BloodPostListFragment {

    public MyTopBloodPostsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START my_top_posts_query]
        // My top posts by number of likes
        String myUserId = getUid();
        Query myTopPostsQuery = databaseReference.child("user-bloodposts").child(myUserId)
                .orderByChild("likeCount");
        // [END my_top_posts_query]

        return myTopPostsQuery;
    }
}
