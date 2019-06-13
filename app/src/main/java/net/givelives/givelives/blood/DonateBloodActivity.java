package net.givelives.givelives.blood;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import net.givelives.givelives.R;
import net.givelives.givelives.models.BloodDonor;
import net.givelives.givelives.models.BloodPost;
import net.givelives.givelives.models.User;
import net.givelives.givelives.utility.FirebaseUtils;

import java.util.HashMap;
import java.util.Map;

import static net.givelives.givelives.R.id.action_done;
import static net.givelives.givelives.blood.BloodRequestDetailActivity.EXTRA_POST_BLOODTYPE;
import static net.givelives.givelives.blood.BloodRequestDetailActivity.EXTRA_POST_KEY;
import static net.givelives.givelives.blood.BloodRequestDetailActivity.EXTRA_POST_PLACE_ID;
import static net.givelives.givelives.blood.BloodRequestDetailActivity.EXTRA_POST_UID;

/**
 * Created by MainAcc on 24/09/2017.
 */

public class DonateBloodActivity extends AppCompatActivity {

    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    private DatabaseReference mBloodPostsRef;
    // [END declare_database_ref]

    private TextView mInstructionTextView;
    //private TextInputEditText mDonorName;
    private TextInputEditText mDonorPhone;
    private TextInputEditText mDonorDonatedAmount;
    private TextInputEditText mDonorMessage;

    private Spinner mDonorBloodTypeSpinner;

    private String mPostKey;
    private String mPostUid;
    private String mPostPlaceId;
    private String mPostBloodType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blood_donate_now);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        // [START initialize_database_ref]
        mDatabase = FirebaseUtils.getDatabaseRef();
        // [END initialize_database_ref]

        mInstructionTextView = (TextView) findViewById(R.id.donate_instr_textview);
        //mDonorName = (TextInputEditText) findViewById(R.id.input_donor_name);
        mDonorPhone = (TextInputEditText) findViewById(R.id.input_phone);
        mDonorDonatedAmount = (TextInputEditText) findViewById(R.id.input_donor_amount);
        mDonorMessage = (TextInputEditText) findViewById(R.id.input_donor_msg);

        // All about Blood Type Spinner
        mDonorBloodTypeSpinner = (Spinner) findViewById(R.id.blood_type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.blood_group_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mDonorBloodTypeSpinner.setAdapter(adapter);
        // Responding to User Selections
        BloodTypeSpinnerItemSelectedListener bloodTypeSpinnerSelection =
                new BloodTypeSpinnerItemSelectedListener();
        mDonorBloodTypeSpinner.setOnItemSelectedListener(bloodTypeSpinnerSelection);

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
        if (mPostPlaceId == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_PLACE_ID");
        }
        // Get post blood type from intent
        mPostBloodType = getIntent().getStringExtra(EXTRA_POST_BLOODTYPE);
        if (mPostBloodType == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_BLOODTYPE");
        }
        // Format the returned Donation Instruction details and display them in the TextView.
        mInstructionTextView.setText(formatDonationInstructionDetails(getResources(),
                mPostBloodType));

    }

    /**
     * Helper method to format information about a place nicely.
     */
    private static Spanned formatDonationInstructionDetails(Resources res,
                                                            CharSequence bloodType) {
        Log.e(TAG, res.getString(R.string.blood_type_warning, bloodType));
        return Html.fromHtml(res.getString(R.string.blood_type_warning, bloodType));
    }

    public class BloodTypeSpinnerItemSelectedListener implements AdapterView.OnItemSelectedListener {

        // todo must code on if statement to reconcile blood types according to blood type compatibility rule
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
            String donorBloodType = (String) parent.getItemAtPosition(pos);
            Log.d("ttt", donorBloodType);
            Log.d("ttt", mPostBloodType);

            isBloodTypeCompatible(donorBloodType);

        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    private Boolean isBloodTypeCompatible(String donorBloodType) {

        Boolean bloodTypeCompat = false;

        if (mPostBloodType.equals("O-") && !donorBloodType.equals("O-")) {
            showSnackbar(R.string.incompatible_blood_type);
        } else if (mPostBloodType.equals("O+") && (!donorBloodType.equals("O-") && !donorBloodType.equals("O+"))) {
            showSnackbar(R.string.incompatible_blood_type);
        } else if (mPostBloodType.equals("B-") && (!donorBloodType.equals("O-") && !donorBloodType.equals("B-"))) {
            showSnackbar(R.string.incompatible_blood_type);
        } else if (mPostBloodType.equals("B+") && (!donorBloodType.equals("O-") && !donorBloodType.equals("O+")
                && !donorBloodType.equals("B-") && !donorBloodType.equals("B+"))) {
            showSnackbar(R.string.incompatible_blood_type);
        } else if (mPostBloodType.equals("A-") && (!donorBloodType.equals("O-") && !donorBloodType.equals("A-"))) {
            showSnackbar(R.string.incompatible_blood_type);
        } else if (mPostBloodType.equals("A+") && (!donorBloodType.equals("O-") && !donorBloodType.equals("O+")
                && donorBloodType.equals("A-") && !donorBloodType.equals("A+"))) {
            showSnackbar(R.string.incompatible_blood_type);
        } else if (mPostBloodType.equals("AB-") && (!donorBloodType.equals("O-") && !donorBloodType.equals("B-")
                && !donorBloodType.equals("A-") && !donorBloodType.equals("AB-"))) {
            showSnackbar(R.string.incompatible_blood_type);
        }

        else {
            bloodTypeCompat = true;
        }
        Log.d("ttt1", String.valueOf(mPostBloodType.equals("AB-")));
        Log.d("ttt2", String.valueOf(!donorBloodType.equals("O-")));
        Log.d("ttt3", String.valueOf(!donorBloodType.equals("B-")));
        Log.d("ttt4", String.valueOf(!donorBloodType.equals("A-")));
        Log.d("ttt5", String.valueOf(!donorBloodType.equals("AB-")));
        return bloodTypeCompat;
    }

    private void submitPost() {

        final String bloodType = mDonorBloodTypeSpinner.getSelectedItem().toString();
        //final String name = mDonorName.getText().toString();
        final String phone = mDonorPhone.getText().toString();

        final String message = mDonorMessage.getText().toString();

        // Validate required fields
        /*if (TextUtils.isEmpty(name)) {
            mDonorName.setError(REQUIRED);
            return;
        }*/

        // check blood type compatibility if users have not selected any blood type
        if (!isBloodTypeCompatible(bloodType)) {
            showSnackbar(R.string.incompatible_blood_type);
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            mDonorPhone.setError(REQUIRED);
            return;
        }

        if (TextUtils.isEmpty(String.valueOf(mDonorDonatedAmount.getText()))) {
            mDonorDonatedAmount.setError(REQUIRED);
            return;
        }
        final int amount = Integer.parseInt(String.valueOf(mDonorDonatedAmount.getText()));


        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Processing...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String currentUId = getUid();
        mDatabase.child("users").child(currentUId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + currentUId + " is unexpectedly null");
                            Toast.makeText(DonateBloodActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {


                            // Write new donation
                            writeNewDonation(
                                    mPostKey,
                                    currentUId,
                                    mPostUid,
                                    mPostPlaceId,
                                    bloodType,
                                    phone,
                                    amount,
                                    message);
                        }
                        // [END single_value_read]
                        setEditingEnabled(true);
                        // Finish this Activity, back to the stream
                        // be careful with this method as it ends the activity and stop any other
                        // method called after. Also, it should be placed here due to
                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                });




    }

    private void setEditingEnabled(boolean enabled) {
        // mDonorName.setEnabled(enabled);
        mDonorPhone.setEnabled(enabled);
        mDonorBloodTypeSpinner.setEnabled(enabled);
        mDonorDonatedAmount.setEnabled(enabled);
        mDonorMessage.setEnabled(enabled);
    }

    // [START write_fan_out]
    private void writeNewDonation(final String postId, final String currentUId, final String postUId, final String placeId,
                                  final String donorBloodType, final String donorPhone, final int donatedAmount,
                                  final String donorMessage) {
        // Create new donation records at 4 nodes simultaneously
        BloodDonor bloodDonor;

        mBloodPostsRef = mDatabase.child("bloodposts").child(postId);
        mBloodPostsRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        BloodPost post = dataSnapshot.getValue(BloodPost.class);

                        // [START_EXCLUDE]
                        if (post != null) {
                            BloodDonor bloodDonor = new BloodDonor(
                                    post.title,
                                    donorBloodType,
                                    donorPhone,
                                    donorMessage,
                                    post.transfusionDate,
                                    donatedAmount
                                    );
                            Map<String, Object> postValues = bloodDonor.toMap();

                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/bloodposts/" + postId + "/donorList/" + currentUId, postValues);
                            childUpdates.put("/user-bloodposts/" + postUId + "/" + postId + "/donorList/" + currentUId,
                                    postValues);
                            childUpdates.put("/place-bloodposts/" + placeId + "/" + postId + "/donorList/" + currentUId,
                                    postValues);
                            childUpdates.put("/users/" + currentUId + "/bloodJourney/" + postId, postValues);

                            mDatabase.updateChildren(childUpdates);
                        }

                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getPost:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                });

    }
    // [END write_fan_out]


    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     */

    private void showSnackbar(final int mainTextStringId) {

       /* Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .show();*/
        Snackbar snackbar;
        snackbar = Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.orange));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.white));
        snackbar.show();
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_receiver_add_blood_request, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case action_done:

                submitPost();
                return true;


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
