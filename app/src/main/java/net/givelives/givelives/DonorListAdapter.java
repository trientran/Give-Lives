package net.givelives.givelives;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import net.givelives.givelives.models.BloodDonor;
import net.givelives.givelives.models.OrganDonor;
import net.givelives.givelives.models.User;
import net.givelives.givelives.utility.FirebaseUtils;
import net.givelives.givelives.utility.GlideApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MainAcc on 13/12/2017.
 */

public class DonorListAdapter extends RecyclerView.Adapter<DonorListAdapter.DonorViewHolder> {

    private static final String TAG = "donor_list";
    public static final String BLOOD_IDENTIFIER = "blood";
    public static final String ORGAN_IDENTIFIER = "organ";
    private Context mContext;
    private String mIdentifier;

    private DatabaseReference mDonorListReference;
    private DatabaseReference mDatabase;
    private DatabaseReference mdonorURef;
    private ChildEventListener mChildEventListener;

    private List<String> mDonorIds = new ArrayList<>();
    private List<BloodDonor> mBloodDonors = new ArrayList<>();
    private List<OrganDonor> mOrganDonors = new ArrayList<>();

    public DonorListAdapter(final Context context, DatabaseReference donorListReference, String identifier) {
        mContext = context;
        mDonorListReference = donorListReference;
        mDatabase = FirebaseUtils.getDatabaseRef();
        ChildEventListener childEventListener = null;
        this.mIdentifier = identifier;

        if (identifier.equals(BLOOD_IDENTIFIER)) {
            // Create child event listener
            // [START child_event_listener_recycler]
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new donor has been added, add it to the displayed list
                    BloodDonor donor = dataSnapshot.getValue(BloodDonor.class);

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    mDonorIds.add(dataSnapshot.getKey());
                    mBloodDonors.add(donor);
                    notifyItemInserted(mBloodDonors.size() - 1);
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A donor has changed, use the key to determine if we are displaying this
                    // donor and if so displayed the changed donor.
                    BloodDonor newDonor = dataSnapshot.getValue(BloodDonor.class);
                    String donorId = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int donorIndex = mDonorIds.indexOf(donorId);
                    if (donorIndex > -1) {
                        // Replace with the new data
                        mBloodDonors.set(donorIndex, newDonor);

                        // Update the RecyclerView
                        notifyItemChanged(donorIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + donorId);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A donor has changed, use the key to determine if we are displaying this
                    // donor and if so remove it.
                    String donorId = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int donorIndex = mDonorIds.indexOf(donorId);
                    if (donorIndex > -1) {
                        // Remove data from the list
                        mDonorIds.remove(donorIndex);
                        mBloodDonors.remove(donorIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(donorIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + donorId);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A donor has changed position, use the key to determine if we are
                    // displaying this donor and if so move it.
                    BloodDonor movedDonor = dataSnapshot.getValue(BloodDonor.class);
                    String donorId = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "donorLists:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load donors.",
                            Toast.LENGTH_SHORT).show();
                }
            };
        }
        else if(identifier.equals(ORGAN_IDENTIFIER)) {
            // Create child event listener
            // [START child_event_listener_recycler]
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new donor has been added, add it to the displayed list
                    OrganDonor donor = dataSnapshot.getValue(OrganDonor.class);

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    mDonorIds.add(dataSnapshot.getKey());
                    mOrganDonors.add(donor);
                    notifyItemInserted(mOrganDonors.size() - 1);
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A donor has changed, use the key to determine if we are displaying this
                    // donor and if so displayed the changed donor.
                    OrganDonor newDonor = dataSnapshot.getValue(OrganDonor.class);
                    String donorId = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int donorIndex = mDonorIds.indexOf(donorId);
                    if (donorIndex > -1) {
                        // Replace with the new data
                        mOrganDonors.set(donorIndex, newDonor);

                        // Update the RecyclerView
                        notifyItemChanged(donorIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + donorId);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A donor has changed, use the key to determine if we are displaying this
                    // donor and if so remove it.
                    String donorId = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int donorIndex = mDonorIds.indexOf(donorId);
                    if (donorIndex > -1) {
                        // Remove data from the list
                        mDonorIds.remove(donorIndex);
                        mOrganDonors.remove(donorIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(donorIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + donorId);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A donor has changed position, use the key to determine if we are
                    // displaying this donor and if so move it.
                    OrganDonor movedDonor = dataSnapshot.getValue(OrganDonor.class);
                    String donorId = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "donorLists:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load donors.",
                            Toast.LENGTH_SHORT).show();
                }
            };
        }

        donorListReference.addChildEventListener(childEventListener);
        // [END child_event_listener_recycler]

        // Store reference to listener so it can be removed on app stop
        mChildEventListener = childEventListener;
    }

    @Override
    public DonorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.mainact_post_item_donor, parent, false);
        return new DonorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DonorViewHolder holder, int position) {
        String donorId = mDonorIds.get(position);
        mdonorURef = mDatabase.child("users").child(donorId);
        ValueEventListener userListener = null;

        if(mIdentifier.equals(BLOOD_IDENTIFIER)) {
            final BloodDonor donor = mBloodDonors.get(position);
            userListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.photoUrl != null) {

                        GlideApp
                                .with(mContext)
                                .load(user.photoUrl)
                                .circleCrop()
                                .override(150)
                                .into(holder.donorPhoto);
                    }
                    if (user != null && user.fullName != null) {
                        holder.donorDetail.setText(formatBloodDonorDetails(mContext,
                                user.fullName,
                                String.valueOf(donor.donatedAmount),
                                donor.donorBloodType,
                                donor.donorPhone,
                                donor.donorMessage));
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a recipientMessage
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    // ...
                }
            };
        }
        else if (mIdentifier.equals(ORGAN_IDENTIFIER)){
            final OrganDonor donor = mOrganDonors.get(position);
            userListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null && user.photoUrl != null) {

                        GlideApp
                                .with(mContext)
                                .load(user.photoUrl)
                                .circleCrop()
                                .override(150)
                                .into(holder.donorPhoto);
                    }
                    if (user != null && user.fullName != null) {
                        holder.donorDetail.setText(formatOrganDonorDetails(mContext,
                                user.fullName,
                                donor.donorBloodType,
                                donor.donorPhone,
                                donor.donorMessage));
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a recipientMessage
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    // ...
                }
            };
        }

        mdonorURef.addValueEventListener(userListener);
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (mIdentifier.equals(BLOOD_IDENTIFIER)) {
            itemCount = mBloodDonors.size();
        }
        if (mIdentifier.equals(ORGAN_IDENTIFIER)) {
            itemCount = mOrganDonors.size();
        }
        return  itemCount;
    }

    public void cleanupListener() {
        if (mChildEventListener != null) {
            mDonorListReference.removeEventListener(mChildEventListener);
        }
    }



    /**
     * Helper method to format text nicely.
     */
    private static Spanned formatBloodDonorDetails(Context context,
                                                            CharSequence donorName,
                                                            CharSequence amount,
                                                            CharSequence bloodType,
                                                            CharSequence phone,
                                                            CharSequence message) {
        return Html.fromHtml(context.getString(R.string.blood_donor_text, donorName, amount, bloodType, phone, message));
    }
    /**
     * Helper method to format text nicely.
     */
    private static Spanned formatOrganDonorDetails(Context context,
                                                            CharSequence donorName,
                                                            CharSequence bloodType,
                                                            CharSequence phone,
                                                            CharSequence message) {
        return Html.fromHtml(context.getString(R.string.organ_donor_text, donorName, bloodType, phone, message));
    }

    static class DonorViewHolder extends RecyclerView.ViewHolder {

        ImageView donorPhoto;
        TextView donorDetail;

        DonorViewHolder(View itemView) {
            super(itemView);
            donorPhoto = (ImageView) itemView.findViewById(R.id.donor_photo);
            donorDetail = (TextView) itemView.findViewById(R.id.donor_text_view);
        }
    }
}
