package net.givelives.givelives.blood.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import net.givelives.givelives.blood.EditBloodRequestActivity;
import net.givelives.givelives.models.BloodDonor;
import net.givelives.givelives.utility.FirebaseUtils;
import net.givelives.givelives.utility.DonationAlertFragment;
import net.givelives.givelives.blood.BloodRequestDetailActivity;
import net.givelives.givelives.R;
import net.givelives.givelives.blood.DonateBloodActivity;
import net.givelives.givelives.models.BloodPost;
import net.givelives.givelives.blood.viewholder.PostViewHolder;

import java.util.HashMap;
import java.util.Map;

public abstract class BloodPostListFragment extends Fragment {

    private static final String TAG = "PostListFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    DatabaseReference bloodPostsRef;
    DatabaseReference userBloodPostsRef;
    DatabaseReference placeBloodPostsRef;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<BloodPost, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public BloodPostListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.mainact_fragment_all_requests, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseUtils.getDatabaseRef();
        // [END create_database_reference]

        mRecycler = (RecyclerView) rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<BloodPost, PostViewHolder>(BloodPost.class,
                R.layout.blood_item_blood_post,
                PostViewHolder.class,
                postsQuery) {
            @Override
            protected void populateViewHolder(final PostViewHolder viewHolder,
                                              final BloodPost post,
                                              final int position) {
                final DatabaseReference postRef = getRef(position);
               // final DatabaseReference postRef = getRef(viewHolder.getAdapterPosition());
                // Set click listener for the whole post view
                final String postKey = postRef.getKey();
                //postRef.getDatabase();
                bloodPostsRef = mDatabase.child("bloodposts")
                        .child(postRef.getKey());
                userBloodPostsRef = mDatabase.child("user-bloodposts")
                        .child(post.uid)
                        .child(postRef.getKey());
                placeBloodPostsRef = mDatabase.child("place-bloodposts")
                        .child(post.placeId)
                        .child(postRef.getKey());
                Log.d("PostRefff", String.valueOf(postRef));

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch BloodRequestDetailActivity
                        Intent intent = new Intent(getActivity(), BloodRequestDetailActivity.class);
                        intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_KEY, postKey);
                        intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_UID, post.uid);
                        intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_PLACE_ID, post.placeId);

                        startActivity(intent);
                    }
                });

                // Determine if the current user has liked this post and set UI accordingly
                setLikeView(viewHolder.mLikeButton, post);
                setDonateNowBtn(viewHolder.mDonateNowBtn, post);
                setEditDeleteBtn(viewHolder.mDeleteButton, viewHolder.mEditButton, post);

                calRemainingBloodAndTotalDonors(bloodPostsRef, userBloodPostsRef, placeBloodPostsRef);

                // Bind BloodPost to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(BloodPostListFragment.this, post, position
                        // OnClickListener for Like button
                        , new View.OnClickListener() {
                            @Override
                            public void onClick(View starView) {
                                // Need to write to 3 places the post is stored. All DatabaseReferences
                                // below have to be re-defined as Method level variables because the
                                // postIds might get messed up with adapterPosition.
                                DatabaseReference bloodPostsRef = mDatabase.child("bloodposts")
                                        .child(postRef.getKey());
                                DatabaseReference userBloodPostsRef = mDatabase.child("user-bloodposts")
                                        .child(post.uid)
                                        .child(postRef.getKey());
                                DatabaseReference placeBloodPostsRef = mDatabase.child("place-bloodposts")
                                        .child(post.placeId)
                                        .child(postRef.getKey());
                                // Run 3 transactions
                                onLikeClicked(bloodPostsRef);
                                onLikeClicked(userBloodPostsRef);
                                onLikeClicked(placeBloodPostsRef);

                                Log.d("reftu", String.valueOf(bloodPostsRef));

                            }
                        }
                        // OnClickListener for Donate Now button
                        , new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                onDonateNowBtnClicked(postKey, post);
                            }
                        }

                        // OnClickListener for Delete button
                        , new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                Log.d("trienDel", postKey);
                                onDeleteBtnClicked(postKey, post);
                            }
                        }
                        // OnClickListener for Edit button
                        , new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                onEditBtnClicked(postKey, post);

                            }
                        }

                        // OnClickListener for Share button
                        , new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                // Launch BloodRequestDetailActivity
                                onShareBtnClicked(post.toString());
                            }
                        }

                        );
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    private void onShareBtnClicked(String sentText) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sentText);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
    }

    private void onEditBtnClicked(String postKey, BloodPost post) {
        // Launch EditBloodRequestActivity
        Intent intent = new Intent(getActivity(), EditBloodRequestActivity.class);
        intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_KEY, postKey);
        intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_UID, post.uid);
        intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_PLACE_ID,
                post.placeId);

        startActivity(intent);
    }

    private void onDeleteBtnClicked(String mPostKey, BloodPost post) {

        if (post.uid.equals(getUid())) {
            DonationAlertFragment newFragment = DonationAlertFragment.newInstance(
                    mPostKey,
                    post.uid,
                    post.placeId,
                    getUid(),
                    DonationAlertFragment.DELETE_BLOOD_POST);
            newFragment.show(getFragmentManager(), "delete_blood_post_dialog");
        } else {
            Toast.makeText(getContext(), "You are not the author of this post", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    // [START write_fan_out]
    public static void deleteBloodPost(String postId, String currentUId, String placeId,
                                       DatabaseReference mDatabase) {
        // Create new donation records at 4 nodes simultaneously

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/bloodposts/" + postId, null);
        childUpdates.put("/user-bloodposts/" + currentUId + "/" + postId, null);
        childUpdates.put("/place-bloodposts/" + placeId + "/" + postId, null);

        mDatabase.updateChildren(childUpdates);
    }
    // [END write_fan_out]


    // Determine if the current user has liked this post and set UI accordingly
    public static void setLikeView(ImageButton likeView, BloodPost model) {
        // Determine if the current user has liked this post and set UI accordingly
        if (model.likes.containsKey(getUid())) {
            likeView.setImageResource(R.drawable.ic_like_red);
        } else {
            likeView.setImageResource(R.drawable.ic_like_grey);

        }
    }

    // Determine if the current user has clicked Donate Now and set UI accordingly
    public static void setDonateNowBtn(TextView donateNowBtn, BloodPost model) {
        // Determine if the current user has said yes to donate and set UI accordingly
        if (model.donorList.containsKey(getUid())) {
            donateNowBtn.setText(R.string.reverse_donation);
        } else {
            donateNowBtn.setText(R.string.donate_now);

       }
    }

    // Determine if the posts are created by the current user, then set up edit/delete buttons accordingly
    public static void setEditDeleteBtn(ImageButton deleteBtn, ImageButton editBtn, BloodPost model) {
        // Determine if the current user has said yes to donate and set UI accordingly
        if (model.uid.equals(getUid())) {
            deleteBtn.setVisibility(View.VISIBLE);
            editBtn.setVisibility(View.VISIBLE);
        }
    }

    // [START post_likes_transaction]
    public static void onLikeClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                BloodPost p = mutableData.getValue(BloodPost.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.likes.containsKey(getUid())) {
                    // Unlike the post and remove self from likes
                    p.likeCount = p.likeCount - 1;
                    p.likes.remove(getUid());
                } else {
                    // Like the post and add self to likes
                    p.likeCount = p.likeCount + 1;
                    p.likes.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }
    // [END post_likes_transaction]

    private void onDonateNowBtnClicked(String mPostKey, BloodPost post) {
        // Launch BloodRequestDetailActivity
        Intent intent = new Intent(getContext(), DonateBloodActivity.class);
        intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_KEY, mPostKey);
        intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_UID, post.uid);
        intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_PLACE_ID,
                post.placeId);
        intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_BLOODTYPE, post.recipientBloodType);
        // Determine if the current user has said yes to donate and set Intent
        // accordingly
        if (!post.donorList.containsKey(getUid())) {
            startActivity(intent);
        } else {
            //todo code to display dialog to confirm deletion:
            DonationAlertFragment newFragment = DonationAlertFragment.newInstance(
                    mPostKey,
                    post.uid,
                    post.placeId,
                    getUid(),
                    DonationAlertFragment.REVERSE_BLOOD_DONATION);
            newFragment.show(getFragmentManager(), "blood_donation_canceled_dialog");

        }
    }

    public static void calRemainingBloodAndTotalDonors(final DatabaseReference postRef1, final DatabaseReference postRef2
            , final DatabaseReference postRef3) {

        ValueEventListener donorListListener;
        donorListListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int accumulation = 0;
                int totalDonors;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    BloodDonor bloodDonor = child.getValue(BloodDonor.class);
                    if (bloodDonor != null) {
                        accumulation+=bloodDonor.donatedAmount;
                    }
                }
                totalDonors = (int) dataSnapshot.getChildrenCount();

                Log.d("totalAccumulation", String.valueOf(accumulation));
                Log.d("totalDonorCount", String.valueOf(dataSnapshot.getChildrenCount()));

                doAllDonationCount(postRef1, accumulation, totalDonors);
                doAllDonationCount(postRef2, accumulation, totalDonors);
                doAllDonationCount(postRef3, accumulation, totalDonors);
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //handle databaseError
            }
        };

        postRef1.child("donorList").addListenerForSingleValueEvent(donorListListener);
    }
    private static void doAllDonationCount(DatabaseReference postRef, final int totalDonatedAmount
            , final int donorCount) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                BloodPost post = mutableData.getValue(BloodPost.class);

                if (post == null) {
                    return Transaction.success(mutableData);
                }

                post.donorCount = donorCount;
                post.remainingBlood = post.amountRequired - totalDonatedAmount;

                mutableData.setValue(post);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

// [START write_fan_out]
    public static void reverseBloodDonation(String postId, String currentUId, String postUId, String placeId,
                                            DatabaseReference mDatabase) {
        // Create new donation records at 4 nodes simultaneously

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/bloodposts/" + postId + "/donorList/" + currentUId, null);
        childUpdates.put("/user-bloodposts/" + postUId + "/" + postId + "/donorList/" + currentUId, null);
        childUpdates.put("/place-bloodposts/" + placeId + "/" + postId + "/donorList/" + currentUId, null);
        //childUpdates.put("/users/" + currentUId + "/bloodJourney/" + postId, null);
        mDatabase.child("users").child(currentUId).child("bloodJourney").child(postId).child("status")
                .setValue(false);
        mDatabase.updateChildren(childUpdates);
    }
    // [END write_fan_out]

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }

    }

    public static String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);
}
