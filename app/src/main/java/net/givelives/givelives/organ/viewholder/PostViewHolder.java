package net.givelives.givelives.organ.viewholder;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.givelives.givelives.R;
import net.givelives.givelives.models.DonationPlace;
import net.givelives.givelives.models.OrganPost;
import net.givelives.givelives.models.User;
import net.givelives.givelives.utility.GlideApp;

public class PostViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "PostListFragment";

    private DatabaseReference mUserRef;
    private DatabaseReference mPlaceRef;

    public ImageView backgroundImage;

    private TextView mAuthorView;
    private ImageView mAuthorPhoto;

    private TextView mTitleView;
    private TextView mBloodTypeView;
    private TextView mTransplantPlaceView;
    private TextView mAddressView;
    private TextView mPhoneView;
    private TextView mMessageView;

    public ImageButton mLikeButton;
    private TextView mLikeCountView;

    private TextView mDonorCountView;
    public TextView mDonateNowBtn;

    public ImageButton mDeleteButton;
    public ImageButton mEditButton;
    public ImageButton mShareButton;
    public PostViewHolder(View itemView) {
        super(itemView);

        backgroundImage = (ImageView) itemView.findViewById(R.id.card_image);

        mAuthorView = (TextView) itemView.findViewById(R.id.post_author);
        mAuthorPhoto = itemView.findViewById(R.id.post_author_photo);

        mTitleView = (TextView) itemView.findViewById(R.id.post_title);
        mBloodTypeView = (TextView) itemView.findViewById(R.id.blood_type);
        mTransplantPlaceView = (TextView) itemView.findViewById(R.id.place_of_transplantation);
        mAddressView = (TextView) itemView.findViewById(R.id.address);
        mPhoneView = (TextView) itemView.findViewById(R.id.phone);
        mMessageView = (TextView) itemView.findViewById(R.id.message);

        mLikeButton = itemView.findViewById(R.id.like_button);
        mLikeCountView = (TextView) itemView.findViewById(R.id.like_count);
        mDonorCountView = (TextView) itemView.findViewById(R.id.donor_count);
        mDonateNowBtn = (TextView) itemView.findViewById(R.id.donate_now_btn);

        mDeleteButton = itemView.findViewById(R.id.delete_button);
        mEditButton = itemView.findViewById(R.id.edit_button);
        mShareButton = itemView.findViewById(R.id.share_button);
    }

    public void bindToPost(final Fragment fragment,
                           final OrganPost post,
                           final int position,
                           final View.OnClickListener starClickListener,
                           final View.OnClickListener donateNowClickListener,
                           final View.OnClickListener deleteClickListener,
                           final View.OnClickListener editClickListener,
                           final View.OnClickListener shareClickListener) {

        // post.commentUid is to get userID of whom the post belong to
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(post.uid);
        mPlaceRef = FirebaseDatabase.getInstance().getReference().child("donationplaces")
                .child(post.placeId);
        final ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                if (fragment == null) {
                    return;
                }
                User user = dataSnapshot.getValue(User.class);
                if (user.photoUrl != null) {

                    GlideApp
                            .with(fragment)
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

        final ValueEventListener placeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                DonationPlace donationPlace = dataSnapshot.getValue(DonationPlace.class);
                if (donationPlace!= null) {
                    mTransplantPlaceView.setText("Proposed hospital: " + donationPlace.placeName);
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

        mUserRef.addValueEventListener(userListener);
        mPlaceRef.addValueEventListener(placeListener);

        Drawable[] mOrganCardImages;
        TypedArray a = fragment.getResources().obtainTypedArray(R.array.organ_post_background);
        mOrganCardImages = new Drawable[a.length()];
        for (int i = 0; i < mOrganCardImages.length; i++) {
            mOrganCardImages[i] = a.getDrawable(i);
        }
        a.recycle();
        backgroundImage.setImageDrawable(mOrganCardImages[position % mOrganCardImages.length]);

        mTitleView.setText(post.title);
        mBloodTypeView.setText("Blood type: " + post.recipientBloodType);
        if (TextUtils.isEmpty(post.recipientPhone)) {
            mPhoneView.setText("Phone: None");
        }
        else {
            mPhoneView.setText("Phone: " + post.recipientPhone);
        }

        mMessageView.setText(post.recipientMessage);

        mLikeButton.setOnClickListener(starClickListener);
        mLikeCountView.setText(String.valueOf(post.likeCount + " Likes"));

        mDonorCountView.setText(String.valueOf(post.donorCount + " donors so far"));
        mDonateNowBtn.setOnClickListener(donateNowClickListener);

        mDeleteButton.setOnClickListener(deleteClickListener);
        mEditButton.setOnClickListener(editClickListener);
        mShareButton.setOnClickListener(shareClickListener);
    }
}
