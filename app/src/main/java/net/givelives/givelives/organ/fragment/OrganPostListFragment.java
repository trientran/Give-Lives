package net.givelives.givelives.organ.fragment;

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

import net.givelives.givelives.R;
import net.givelives.givelives.organ.EditOrganRequestActivity;
import net.givelives.givelives.utility.FirebaseUtils;
import net.givelives.givelives.utility.DonationAlertFragment;
import net.givelives.givelives.organ.OrganRequestDetailActivity;
import net.givelives.givelives.organ.DonateOrganActivity;
import net.givelives.givelives.organ.viewholder.PostViewHolder;
import net.givelives.givelives.models.OrganPost;

import java.util.HashMap;
import java.util.Map;

public abstract class OrganPostListFragment extends Fragment {

    private static final String TAG = "PostListFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    DatabaseReference organPostsRef;
    DatabaseReference userOrganPostsRef;
    DatabaseReference placeOrganPostsRef;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<OrganPost, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public OrganPostListFragment() {}

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
        mAdapter = new FirebaseRecyclerAdapter<OrganPost, PostViewHolder>(OrganPost.class,
                R.layout.organ_item_organ_post,
                PostViewHolder.class,
                postsQuery) {
            @Override
            protected void populateViewHolder(final PostViewHolder viewHolder,
                                              final OrganPost post,
                                              final int position) {
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                final String postKey = postRef.getKey();
                //postRef.getDatabase();
                organPostsRef = mDatabase.child("organposts")
                        .child(postRef.getKey());
                userOrganPostsRef = mDatabase.child("user-organposts")
                        .child(post.uid)
                        .child(postRef.getKey());
                placeOrganPostsRef = mDatabase.child("place-organposts")
                        .child(post.placeId)
                        .child(postRef.getKey());
                Log.d("Ref", String.valueOf(postRef));
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch OrganRequestDetailActivity
                        Intent intent = new Intent(getActivity(), OrganRequestDetailActivity.class);
                        intent.putExtra(OrganRequestDetailActivity.EXTRA_POST_KEY, postKey);
                        intent.putExtra(OrganRequestDetailActivity.EXTRA_POST_UID, post.uid);
                        intent.putExtra(OrganRequestDetailActivity.EXTRA_POST_PLACE_ID,
                                post.placeId);

                        startActivity(intent);
                    }
                });

                // Determine if the current user has liked this post and set UI accordingly
                setLikeView(viewHolder.mLikeButton, post);
                setDonateNowBtn(viewHolder.mDonateNowBtn, post);
                setEditDeleteBtn(viewHolder.mDeleteButton, viewHolder.mEditButton, post);

                calTotalDonors(organPostsRef, userOrganPostsRef, placeOrganPostsRef);

                // Bind OrganPost to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(OrganPostListFragment.this, post, position
                        // OnClickListener for Like button
                        , new View.OnClickListener() {
                            @Override
                            public void onClick(View likeView) {
                                // Need to write to 3 places the post is stored. All DatabaseReferences
                                // below have to be re-defined as Method level variables because the
                                // postIds might get messed up with adapterPosition.
                                DatabaseReference organPostsRef = mDatabase.child("organposts")
                                        .child(postRef.getKey());
                                DatabaseReference userOrganPostsRef = mDatabase.child("user-organposts")
                                        .child(post.uid)
                                        .child(postRef.getKey());
                                DatabaseReference placeOrganPostsRef = mDatabase.child("place-organposts")
                                        .child(post.placeId)
                                        .child(postRef.getKey());
                                // Run 3 transactions
                                onLikeClicked(placeOrganPostsRef);
                                onLikeClicked(organPostsRef);
                                onLikeClicked(userOrganPostsRef);

                                Log.d("reftu", String.valueOf(userOrganPostsRef));
                                Log.d("reftu", String.valueOf(organPostsRef));

                            }
                        }
                        // OnClickListener for Donate Now button
                        , new View.OnClickListener() {

                            @Override
                            public void onClick(View donateNowView) {
                                onDonateNowBtnClicked(postKey, post);
                            }
                        }

                        // OnClickListener for Delete button
                        , new View.OnClickListener() {

                            @Override
                            public void onClick(View donateNowView) {
                                onDeleteBtnClicked(postKey, post);
                            }
                        }
                        // OnClickListener for Edit button
                        , new View.OnClickListener() {

                            @Override
                            public void onClick(View donateNowView) {
                                onEditBtnClicked(postKey, post);

                            }
                        }

                        // OnClickListener for Share button
                        , new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                onShareBtnClicked(post.toString());
                            }
                        }

                );
            }
        };
        mRecycler.setAdapter(mAdapter);

    }

    private void onEditBtnClicked(String postKey, OrganPost post) {
        // Launch EditOrganRequestActivity
        Intent intent = new Intent(getActivity(), EditOrganRequestActivity.class);
        intent.putExtra(OrganRequestDetailActivity.EXTRA_POST_KEY, postKey);
        intent.putExtra(OrganRequestDetailActivity.EXTRA_POST_UID, post.uid);
        intent.putExtra(OrganRequestDetailActivity.EXTRA_POST_PLACE_ID,
                post.placeId);

        startActivity(intent);
    }

    private void onShareBtnClicked(String sentText) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sentText);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
    }

    private void onDeleteBtnClicked(String mPostKey, OrganPost post) {

        if (post.uid.equals(getUid())) {
            DonationAlertFragment newFragment = DonationAlertFragment.newInstance(
                    mPostKey,
                    post.uid,
                    post.placeId,
                    getUid(),
                    DonationAlertFragment.DELETE_ORGAN_POST);
            newFragment.show(getFragmentManager(), "delete_post_dialog");
        } else {
            Toast.makeText(getContext(), "You are not the author of this post", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    // [START write_fan_out]
    public static void deleteOrganPost(String postId, String currentUId, String placeId,
                                       DatabaseReference mDatabase) {
        // Create new donation records at 4 nodes simultaneously
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/organposts/" + postId, null);
        childUpdates.put("/user-organposts/" + currentUId + "/" + postId, null);
        childUpdates.put("/place-organposts/" + placeId + "/" + postId, null);

        mDatabase.updateChildren(childUpdates);
    }
    // [END write_fan_out]

    // Determine if the current user has liked this post and set UI accordingly
    public static void setLikeView(ImageButton likeView, OrganPost model) {
        // Determine if the current user has liked this post and set UI accordingly
        if (model.likes.containsKey(getUid())) {
            likeView.setImageResource(R.drawable.ic_like_red);
        } else {
            likeView.setImageResource(R.drawable.ic_like_grey);

        }
    }

    // Determine if the current user has clicked Donate Now and set UI accordingly
    public static void setDonateNowBtn(TextView donateNowBtn, OrganPost model) {
        // Determine if the current user has said yes to donate and set UI accordingly
        if (model.donorList.containsKey(getUid())) {
            donateNowBtn.setText(R.string.reverse_donation);
        } else {
            donateNowBtn.setText(R.string.donate_now);

        }
    }

    // Determine if the posts are created by the current user, then set up edit/delete buttons accordingly
    public static void setEditDeleteBtn(ImageButton deleteBtn,
                                        ImageButton editBtn,
                                        OrganPost model) {
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
                OrganPost p = mutableData.getValue(OrganPost.class);
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

    private void onDonateNowBtnClicked(String mPostKey, OrganPost post) {
        // Launch OrganRequestDetailActivity
        Intent intent = new Intent(getContext(), DonateOrganActivity.class);
        intent.putExtra(OrganRequestDetailActivity.EXTRA_POST_KEY, mPostKey);
        intent.putExtra(OrganRequestDetailActivity.EXTRA_POST_UID, post.uid);
        intent.putExtra(OrganRequestDetailActivity.EXTRA_POST_PLACE_ID,
                post.placeId);
        intent.putExtra(OrganRequestDetailActivity.EXTRA_POST_BLOODTYPE, post.recipientBloodType);
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
                    DonationAlertFragment.REVERSE_ORGAN_DONATION);
            newFragment.show(getFragmentManager(), "donation_canceled_dialog");

        }
    }

    public static void calTotalDonors(final DatabaseReference postRef1,
                                      final DatabaseReference postRef2,
                                      final DatabaseReference postRef3) {

        ValueEventListener donorListListener;
        donorListListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int totalDonors;
                totalDonors = (int) dataSnapshot.getChildrenCount();

                doAllDonationCount(postRef1, totalDonors);
                doAllDonationCount(postRef2, totalDonors);
                doAllDonationCount(postRef3, totalDonors);
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //handle databaseError
            }
        };

        postRef1.child("donorList").addListenerForSingleValueEvent(donorListListener);
    }

    private static void doAllDonationCount(DatabaseReference postRef, final int donorCount) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                OrganPost post = mutableData.getValue(OrganPost.class);

                if (post == null) {
                    return Transaction.success(mutableData);
                }

                post.donorCount = donorCount;

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

    // [END post_likes_transaction]
// [START write_fan_out]
    public static void reverseOrganDonation(String postId,
                                            String currentUId,
                                            String postUId,
                                            String placeId,
                                            DatabaseReference mDatabase) {
        // Create new donation records at 4 nodes simultaneously

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/organposts/" + postId + "/donorList/" + currentUId, null);
        childUpdates.put("/user-organposts/" + postUId + "/" + postId + "/donorList/" + currentUId,
                null);
        childUpdates.put("/place-organposts/" + placeId + "/" + postId + "/donorList/" + currentUId,
                null);

        mDatabase.child("users")
                .child(currentUId)
                .child("organJourney")
                .child(postId)
                .child("status")
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
