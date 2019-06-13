package net.givelives.givelives.utility;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.google.firebase.database.DatabaseReference;

import net.givelives.givelives.R;

import static net.givelives.givelives.blood.fragment.BloodPostListFragment.calRemainingBloodAndTotalDonors;
import static net.givelives.givelives.blood.fragment.BloodPostListFragment.deleteBloodPost;
import static net.givelives.givelives.blood.fragment.BloodPostListFragment.reverseBloodDonation;
import static net.givelives.givelives.organ.fragment.OrganPostListFragment.calTotalDonors;
import static net.givelives.givelives.organ.fragment.OrganPostListFragment.deleteOrganPost;
import static net.givelives.givelives.organ.fragment.OrganPostListFragment.reverseOrganDonation;

/**
 * Created by MainAcc on 15/11/2017.
 */

public class DonationAlertFragment extends DialogFragment {
    private static final String TAG = "DonationCancelActivity";

    public static final String REVERSE_BLOOD_DONATION = "blood";
    public static final String REVERSE_ORGAN_DONATION = "organ";
    public static final String DELETE_BLOOD_POST = "delete_blood_post";
    public static final String DELETE_ORGAN_POST = "delete_organ_post";
    public static final String ABOUT_US = "about_us";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    private DatabaseReference bloodPostsRef;
    private DatabaseReference userBloodPostsRef;
    private DatabaseReference placeBloodPostsRef;
    private DatabaseReference organPostsRef;
    private DatabaseReference userOrganPostsRef;
    private DatabaseReference placeOrganPostsRef;
    // [END define_database_reference]

    private String mPostKey;
    private String mPostUid;
    private String mPostPlaceId;
    private String mCurrentUId;
    private String mActionIdentifier;

    public void setPostKey(String postKey) {
        mPostKey = postKey;
    }

    public void setPostUid(String postUid) {
        mPostUid = postUid;
    }

    public void setPostPlaceId(String postPlaceId) {
        mPostPlaceId = postPlaceId;
    }

    public void setCurrentUId(String currentUId) {
        mCurrentUId = currentUId;
    }

    public void setActionIdentifier(String actionIdentifier) {
        mActionIdentifier = actionIdentifier;
    }

    public static DonationAlertFragment newInstance(String mPostKey, String mPostUid,
                                                    String mPostPlaceId, String mCurrentUId,
                                                    String actionIdentifier) {
        DonationAlertFragment r = new DonationAlertFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("mPostKey", String.valueOf(mPostKey));
        args.putString("mPostUid", String.valueOf(mPostUid));
        args.putString("mPostPlaceId", String.valueOf(mPostPlaceId));
        args.putString("mCurrentUId", String.valueOf(mCurrentUId));
        args.putString("mActionIdentifier", String.valueOf(actionIdentifier));
        r.setArguments(args);

        return r;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPostKey(getArguments() != null ? getArguments().getString("mPostKey") : "None");
        setPostUid(getArguments() != null ? getArguments().getString("mPostUid") : "None");
        setPostPlaceId(getArguments() != null ? getArguments().getString("mPostPlaceId") : "None");
        setCurrentUId(getArguments() != null ? getArguments().getString("mCurrentUId") : "None");
        setActionIdentifier(getArguments() != null ? getArguments().getString("mActionIdentifier") : "None");
        //Log.d("trien", mCurrentUId);
        //Log.d("trien", mPostKey);
        // [START create_database_reference]
        mDatabase = FirebaseUtils.getDatabaseRef();

        bloodPostsRef = mDatabase.child("bloodposts")
                .child(mPostKey);
        userBloodPostsRef = mDatabase.child("user-bloodposts")
                .child(mPostUid)
                .child(mPostKey);
        placeBloodPostsRef = mDatabase.child("place-bloodposts")
                .child(mPostPlaceId)
                .child(mPostKey);


        organPostsRef = mDatabase.child("organposts")
                .child(mPostKey);
        userOrganPostsRef = mDatabase.child("user-organposts")
                .child(mPostUid)
                .child(mPostKey);
        placeOrganPostsRef = mDatabase.child("place-organposts")
                .child(mPostPlaceId)
                .child(mPostKey);
        // [END create_database_reference]
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        int alertMessage = 0;

        if (mActionIdentifier.equals(REVERSE_BLOOD_DONATION) || mActionIdentifier.equals(
                REVERSE_ORGAN_DONATION)) {
            alertMessage = R.string.dialog_reverse_donation;
        } else if (mActionIdentifier.equals(DELETE_BLOOD_POST) || mActionIdentifier.equals(
                DELETE_ORGAN_POST)) {
            alertMessage = R.string.dialog_delete_post;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(alertMessage);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (mActionIdentifier.equals(REVERSE_BLOOD_DONATION)) {
                    yesReverseBloodBtnClicked();
                } else if (mActionIdentifier.equals(REVERSE_ORGAN_DONATION)) {
                    yesReverseOrganBtnClicked();
                } else if (mActionIdentifier.equals(DELETE_BLOOD_POST)) {
                    yesDeleteBloodPostClicked();
                } else if (mActionIdentifier.equals(DELETE_ORGAN_POST)) {
                    yesDeleteOrganPostClicked();
                }

            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void yesReverseBloodBtnClicked() {
        // Run reverseDonationStatistic on 3 nodes
        reverseBloodDonation(mPostKey, mCurrentUId, mPostUid, mPostPlaceId, mDatabase);
        calRemainingBloodAndTotalDonors(bloodPostsRef, userBloodPostsRef, placeBloodPostsRef);
    }

    private void yesReverseOrganBtnClicked() {
        // Run reverseDonationStatistic on 3 nodes
        reverseOrganDonation(mPostKey, mCurrentUId, mPostUid, mPostPlaceId, mDatabase);
        calTotalDonors(organPostsRef, userOrganPostsRef, placeOrganPostsRef);
    }

    private void yesDeleteBloodPostClicked() {
        deleteBloodPost(mPostKey, mCurrentUId, mPostPlaceId, mDatabase);
    }

    private void yesDeleteOrganPostClicked() {
        deleteOrganPost(mPostKey, mCurrentUId, mPostPlaceId, mDatabase);
    }
}