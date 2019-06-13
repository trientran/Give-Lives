/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.givelives.givelives.blood;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.givelives.givelives.CommentAdapter;
import net.givelives.givelives.DonorListAdapter;
import net.givelives.givelives.utility.FirebaseUtils;
import net.givelives.givelives.utility.DonationAlertFragment;
import net.givelives.givelives.models.BloodPost;
import net.givelives.givelives.models.DonationPlace;
import net.givelives.givelives.models.User;
import net.givelives.givelives.models.Comment;

import static net.givelives.givelives.blood.fragment.BloodPostListFragment.getUid;
import static net.givelives.givelives.blood.fragment.BloodPostListFragment.onLikeClicked;
import static net.givelives.givelives.blood.fragment.BloodPostListFragment.setDonateNowBtn;
import static net.givelives.givelives.blood.fragment.BloodPostListFragment.setEditDeleteBtn;
import static net.givelives.givelives.blood.fragment.BloodPostListFragment.setLikeView;
import net.givelives.givelives.R;
import net.givelives.givelives.utility.GlideApp;

/**
 * Provides UI for the Detail page with Collapsing Toolbar.
 */
public class BloodRequestDetailActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String EXTRA_POSITION = "position";

    private static final String TAG = "PostDetailActivity";

    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_POST_UID = "post_uid";
    public static final String EXTRA_POST_PLACE_ID = "post_place_id";
    public static final String EXTRA_POST_BLOODTYPE = "post_blood_type";

    private DatabaseReference mDatabase;
    private DatabaseReference mUserRef;
    private DatabaseReference mPlaceRef;
    private DatabaseReference mBloodPostsRef;
    private DatabaseReference mUserPostRef;
    private DatabaseReference mPlaceBloodPostsRef;
    private DatabaseReference mCommentsReference;
    private DatabaseReference mDonorListReference;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private String mPostUid;
    private String mPostPlaceId;
    private CommentAdapter mCommentAdapter;
    private DonorListAdapter mDonorListAdapter;

    private TextView mAuthorView;
    private ImageView mAuthorPhoto;

    private TextView mTitleView;
    private TextView mPostTimeView;
    private TextView mBloodTypeView;
    private TextView mTransfusionPlaceView;
    private TextView mAddressView;
    private TextView mTransfusionTimeView;
    private TextView mPhoneView;
    private TextView mMessageView;
    private TextView mQuantityView;

    private ImageButton mLikeButton;
    private TextView mLikeCountView;
    private TextView mDonorCountView;
    private TextView mRemainingBloodView;
    private TextView mDonateNowBtn;

    public ImageButton mDeleteButton;
    public ImageButton mEditButton;
    public ImageButton mShareButton;

    private View donorListInclude;
    private RecyclerView mDonorListRecycler;

    private EditText mCommentField;
    private ImageView mCommentButton;
    private RecyclerView mCommentsRecycler;





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blood_request_detail);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Set Collapsing Toolbar layout to the screen
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        collapsingToolbar.setTitle(getString(R.string.blood_request_detail));

        int position = getIntent().getIntExtra(EXTRA_POSITION, 0);
        Resources resources = getResources();
        TypedArray placePictures = resources.obtainTypedArray(R.array.well_being_background);
        ImageView placePicutre = (ImageView) findViewById(R.id.image);
        placePicutre.setImageDrawable(placePictures.getDrawable(position % placePictures.length()));

        placePictures.recycle();

        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }
        // Get post key from intent
        mPostUid = getIntent().getStringExtra(EXTRA_POST_UID);
        if (mPostUid == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_UID");
        }
        // Get post place id from intent
        mPostPlaceId = getIntent().getStringExtra(EXTRA_POST_PLACE_ID);

        mDatabase = FirebaseUtils.getDatabaseRef();
        mUserRef = mDatabase.child("users").child(mPostUid);
        mBloodPostsRef = mDatabase.child("bloodposts").child(mPostKey);
        mUserPostRef = mDatabase.child("user-bloodposts").child(mPostUid).child(mPostKey);
        mCommentsReference = mDatabase.child("post-comments").child(mPostKey);
        mDonorListReference = mBloodPostsRef.child("donorList");
        donorListInclude = findViewById(R.id.donor_list);
        if (!getUid().equals(mPostUid)) {
            donorListInclude.setVisibility(View.GONE);
        }

        // Initialize Views
        mAuthorView = (TextView) findViewById(R.id.post_author);
        mAuthorPhoto = findViewById(R.id.post_author_photo);

        mTitleView = (TextView) findViewById(R.id.post_title);
        mPostTimeView = (TextView) findViewById(R.id.post_datetime);
        mBloodTypeView = (TextView) findViewById(R.id.blood_type);
        mTransfusionPlaceView = (TextView) findViewById(R.id.place_of_transfusion);
        mAddressView = (TextView) findViewById(R.id.address);
        mTransfusionTimeView = (TextView) findViewById(R.id.transfusion_time);
        mPhoneView = (TextView) findViewById(R.id.phone);
        mMessageView = (TextView) findViewById(R.id.message);
        mQuantityView = (TextView) findViewById(R.id.blood_quantity);

        mLikeButton = findViewById(R.id.like_button);

        mLikeCountView = (TextView) findViewById(R.id.like_count);
        mDonorCountView = (TextView) findViewById(R.id.donor_count);
        mRemainingBloodView = (TextView) findViewById(R.id.remaining_blood);
        mDonateNowBtn = (TextView) findViewById(R.id.donate_now_btn);

        mDeleteButton = findViewById(R.id.delete_button);
        mEditButton = findViewById(R.id.edit_button);
        mShareButton = findViewById(R.id.share_button);

        mCommentField = (EditText) findViewById(R.id.field_comment_text);

        mCommentButton = (ImageView) findViewById(R.id.button_post_comment);
        mCommentsRecycler = (RecyclerView) findViewById(R.id.recycler_comments);
        mDonorListRecycler = (RecyclerView) findViewById(R.id.recycler_donor_list);

        mCommentButton.setOnClickListener(this);
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mDonorListRecycler.setLayoutManager(new LinearLayoutManager(this));
       // calRemainingBloodAndTotalDonors(mBloodPostsRef, mPlaceBloodPostsRef, mUserPostRef);

        if (mPostPlaceId == null) {
            mBloodPostsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI
                    BloodPost post = dataSnapshot.getValue(BloodPost.class);

                    if (post != null) {
                        initializePlaceRefs(post.placeId);
                        populatePlaceViews();
                        setLikeButtonOnclicked();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a recipientMessage
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    // ...
                }
            });

        }
        else {
            initializePlaceRefs(mPostPlaceId);
            populatePlaceViews();
            setLikeButtonOnclicked();
        }
    }

    private void initializePlaceRefs(String placeId) {
        mPlaceRef = mDatabase.child("donationplaces").child(placeId);
        mPlaceBloodPostsRef = mDatabase.child("place-bloodposts").child(placeId).child(mPostKey);
    }

    @Override
    public void onStart() {
        super.onStart();

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                User user = dataSnapshot.getValue(User.class);
                if (user.photoUrl != null) {

                    GlideApp
                            .with(getApplicationContext())
                            .load(user.photoUrl)
                            .circleCrop()
                            .override(150)
                            .into(mAuthorPhoto);
                }
                if (user.fullName != null) {
                    mAuthorView.setText(user.fullName);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a recipientMessage
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mUserRef.addValueEventListener(userListener);



        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get BloodPost object and use the values to update the UI
                final BloodPost post = dataSnapshot.getValue(BloodPost.class);


                // [START_EXCLUDE]


                mTitleView.setText(post.title);
                // TODO time of posting
                //mPostTimeView.setText(SimpleDateFormat.getDateTimeInstance().format(post.postDateTime));
                mBloodTypeView.setText("Blood type: " + post.recipientBloodType);
                mTransfusionTimeView.setText("Transfusion Time: " + post.transfusionDate + " " + post.transfusionTime);
                mPhoneView.setText("Phone: " + post.recipientPhone);
                mMessageView.setText(post.recipientMessage);
                mQuantityView.setText(post.amountRequired + "ml required");

                setLikeView(mLikeButton, post);
                setDonateNowBtn(mDonateNowBtn, post);
                setEditDeleteBtn(mDeleteButton, mEditButton, post);


                mLikeCountView.setText(post.likeCount + " Likes");
                mDonorCountView.setText(post.donorCount + " donors so far");
                mRemainingBloodView.setText(post.remainingBlood + "ml remaining");

                mDonateNowBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        onDonateNowBtnClicked(mPostKey, post);
                    }
                });

                mDeleteButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        onDeleteBtnClicked(mPostKey, post);
                    }
                });

                mEditButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        onEditBtnClicked(mPostKey, post);
                    }
                });

                mShareButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        onShareBtnClicked(post.toString());
                    }
                });


                // [END_EXCLUDE]
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
                Intent intent = new Intent(getBaseContext(), EditBloodRequestActivity.class);
                intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_KEY, postKey);
                intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_UID, post.uid);
                intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_PLACE_ID,
                        post.placeId);

                startActivity(intent);
            }

            private void onDonateNowBtnClicked(String mPostKey, BloodPost post) {
                // Launch BloodRequestDetailActivity
                Intent intent = new Intent(getBaseContext(), DonateBloodActivity.class);
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
                    DonationAlertFragment newFragment = DonationAlertFragment.newInstance(
                            mPostKey,
                            post.uid,
                            post.placeId,
                            getUid(),
                            DonationAlertFragment.REVERSE_BLOOD_DONATION);
                    newFragment.show(getSupportFragmentManager(), "donation_canceled_dialog_detail");

                }
            }

            private void onDeleteBtnClicked(String mPostKey, BloodPost post) {

                if (post.uid.equals(getUid())) {
                    DonationAlertFragment newFragment = DonationAlertFragment.newInstance(
                            mPostKey,
                            post.uid,
                            post.placeId,
                            getUid(),
                            DonationAlertFragment.DELETE_BLOOD_POST);
                    newFragment.show(getSupportFragmentManager(), "delete_blood_post_dialog");
                } else {
                    Toast.makeText(getBaseContext(), "You are not the author of this post"
                            , Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting BloodPost failed, log a recipientMessage
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(BloodRequestDetailActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mBloodPostsRef.addValueEventListener(postListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;

        // Listen for comments
        mCommentAdapter = new CommentAdapter(getApplicationContext(), mCommentsReference);
        mCommentsRecycler.setAdapter(mCommentAdapter);

        if (getUid().equals(mPostUid)) {
            mDonorListAdapter = new DonorListAdapter(getApplicationContext(), mDonorListReference, DonorListAdapter.BLOOD_IDENTIFIER);
            mDonorListRecycler.setAdapter(mDonorListAdapter);
        }
    }

    private void setLikeButtonOnclicked() {
        mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Need to write to both places the post is stored
                // Run two transactions
                Log.d("reftu2", String.valueOf(mBloodPostsRef));
                onLikeClicked(mBloodPostsRef);
                onLikeClicked(mUserPostRef);
                onLikeClicked(mPlaceBloodPostsRef);
            }});
    }

    private void populatePlaceViews() {
        ValueEventListener placeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                DonationPlace donationPlace = dataSnapshot.getValue(DonationPlace.class);
                if (donationPlace!= null) {
                    mTransfusionPlaceView.setText("Place of Transfusion: " + donationPlace.placeName);
                    mAddressView.setText("Address: " + donationPlace.placeAddress);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a recipientMessage
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mPlaceRef.addValueEventListener(placeListener);
    }


/*
    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        SimpleDateFormat.getDateTimeInstance().format(dateInMS);;
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
*/
    // todo onActivityResult to get new post values from DonateBloodActivity, so we can refresh the displayed
    // values in post details views

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //calRemainingBloodAndTotalDonors(mBloodPostsRef, mPlaceBloodPostsRef, mUserPostRef);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mBloodPostsRef.removeEventListener(mPostListener);
        }

        // Clean up comments listener
        mCommentAdapter.cleanupListener();

        // Clean up donor list listener
        if (getUid().equals(mPostUid)) {
            mDonorListAdapter.cleanupListener();
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_post_comment) {
            postComment();
        }
    }

    private void postComment() {
        final String commentUid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(commentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.username;

                        // Create new comment object
                        String commentText = mCommentField.getText().toString();
                        Comment comment = new Comment(mPostUid, commentUid, authorName, commentText);

                        // Push the comment, it will appear in the list
                        mCommentsReference.push().setValue(comment);

                        // Clear the field
                        mCommentField.setText(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_plain, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it (this includes method handling up action/button).
                return super.onOptionsItemSelected(item);

        }
    }
}
