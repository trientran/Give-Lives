package net.givelives.givelives.organ;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import net.givelives.givelives.R;
import net.givelives.givelives.models.DonationPlace;
import net.givelives.givelives.models.OrganPost;
import net.givelives.givelives.models.User;
import net.givelives.givelives.utility.FirebaseUtils;

import static net.givelives.givelives.R.id.action_done;

/**
 * Created by MainAcc on 24/09/2017.
 */

public class EditOrganRequestActivity extends AppCompatActivity implements PlaceSelectionListener{

    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";

    private String mPostKey;
    private String mPostUid;
    private String mPostPlaceId;

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    private DatabaseReference mOrganPostsRef;
    private DatabaseReference mUserPostRef;
    private DatabaseReference mPlaceOrganPostsRef;
    // [END declare_database_ref]

    protected GeoDataClient mGeoDataClient;

    private TextInputEditText mRecipientField;
    private TextInputEditText mPhoneField;
    private TextInputEditText mMessageField;

    private Spinner bloodTypeSpinner;
    ArrayAdapter<CharSequence> bloodTypeSpinnerAdapter;
    private Spinner organSpinner;
    ArrayAdapter<CharSequence> organSpinnerAdapter;

    private Place mTransplantPlace;
    private TextView mPlaceDetailsText;
    private TextView mPlaceAttribution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organ_add_request);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Get post key from intent
        mPostKey = getIntent().getStringExtra(OrganRequestDetailActivity.EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }
        // Get post key from intent
        mPostUid = getIntent().getStringExtra(OrganRequestDetailActivity.EXTRA_POST_UID);
        if (mPostUid == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_UID");
        }
        // Get post place id from intent
        mPostPlaceId = getIntent().getStringExtra(OrganRequestDetailActivity.EXTRA_POST_PLACE_ID);
        if (mPostPlaceId == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_PLACE_ID");
        }

        // [START initialize_database_ref]
        mDatabase = FirebaseUtils.getDatabaseRef();
        mOrganPostsRef = mDatabase.child("organposts").child(mPostKey);
        mPlaceOrganPostsRef = mDatabase.child("place-organposts")
                .child(mPostPlaceId)
                .child(mPostKey);
        mUserPostRef = mDatabase.child("user-organposts").child(mPostUid).child(mPostKey);
        // [END initialize_database_ref]

        mRecipientField = (TextInputEditText) findViewById(R.id.organ_hint_name);
        mTransplantPlace = null;
        mPhoneField = (TextInputEditText) findViewById(R.id.organ_hint_phone);

        mMessageField = (TextInputEditText) findViewById(R.id.organ_hint_msg);

        // All about Blood Type Spinner and Organ Spinner
        bloodTypeSpinner = (Spinner) findViewById(R.id.blood_type_spinner);
        organSpinner = (Spinner) findViewById(R.id.organ_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        bloodTypeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_group_array, android.R.layout.simple_spinner_item);
        organSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.organ_tissue_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        bloodTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        bloodTypeSpinner.setAdapter(bloodTypeSpinnerAdapter);
        organSpinner.setAdapter(organSpinnerAdapter);
        // Responding to User Selections
        SpinnerItemSelectedListener bloodTypeSpinnerSelection = new SpinnerItemSelectedListener();
        bloodTypeSpinner.setOnItemSelectedListener(bloodTypeSpinnerSelection);
        organSpinner.setOnItemSelectedListener(bloodTypeSpinnerSelection);

        // Retrieve the PlaceAutocompleteFragment.
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.organ_hint_place);

        // Register a listener to receive callbacks when a place has been selected or an error has
        // occurred.
        autocompleteFragment.setOnPlaceSelectedListener(this);

        // Retrieve the TextViews that will display details about the selected place.
        mPlaceDetailsText = (TextView) findViewById(R.id.place_details);
        mPlaceAttribution = (TextView) findViewById(R.id.place_attribution);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPostDetails();
    }

    private void loadPostDetails() {
        mGeoDataClient.getPlaceById(mPostPlaceId)
                .addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                                           @Override
                                           public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                                               if (task.isSuccessful()) {
                                                   PlaceBufferResponse places = task.getResult();
                                                   Place place = places.get(0);
                                                   Log.i(TAG, "Place found: " + place.getName());
                                                   // setup place details and attributions

                                                   Place frozen = place.freeze();

                                                   places.release();
                                                   // If you call place.getName() here, an exception is thrown,
                                                   // because the buffer has been released.
                                                   // Instead, use the frozen Place object.
                                                   setupPlaceDetails(frozen);
                                               } else {
                                                   Log.e(TAG, "Place not found.");
                                               }

                                           }
                                       }
                );

        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get OrganPost object and use the values to update the UI
                final OrganPost post = dataSnapshot.getValue(OrganPost.class);

                // [START_EXCLUDE]
                if (post != null) {
                    mRecipientField.setText(post.recipientName);
                    mPhoneField.setText(post.recipientPhone);
                    mMessageField.setText(post.recipientMessage);

                    bloodTypeSpinner.setSelection(bloodTypeSpinnerAdapter.getPosition(post.recipientBloodType));
                    //bloodTypeSpinner.setEnabled(false);
                    bloodTypeSpinner.setOnTouchListener(new View.OnTouchListener() {
                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public boolean onTouch(View view, MotionEvent event) {
                            Toast.makeText(getBaseContext(), "You cannot change " +
                                            "your blood type. If you want to, please delete this post and " +
                                            "create a new one",
                                    Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });

                    organSpinner.setSelection(organSpinnerAdapter.getPosition(post.organ));
                    //organTypeSpinner.setEnabled(false);
                    organSpinner.setOnTouchListener(new View.OnTouchListener() {
                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public boolean onTouch(View view, MotionEvent event) {
                            Toast.makeText(getBaseContext(), "You cannot change " +
                                            "the organ type. If you want to, please delete this post and " +
                                            "create a new one",
                                    Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });
                }

                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting OrganPost failed, log a recipientMessage
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(getBaseContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mOrganPostsRef.addValueEventListener(postListener);
        // [END post_value_event_listener]
    }

    /**
     * Callback invoked when a place has been selected from the PlaceAutocompleteFragment.
     */
    @Override
    public void onPlaceSelected(Place place) {
        Log.i(TAG, "Place Selected: " + place.getName());
        setupPlaceDetails(place);
    }

    private void setupPlaceDetails(Place place) {

        // Format the returned place's details and display them in the TextView.
        mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
                place.getAddress()));

        // set place of transfusion
        mTransplantPlace = place;

        CharSequence attributions = place.getAttributions();
        if (!TextUtils.isEmpty(attributions)) {
            mPlaceAttribution.setText(Html.fromHtml(attributions.toString()));
        } else {
            mPlaceAttribution.setText("");
        }
    }

    /**
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */
    @Override
    public void onError(Status status) {
        Log.e(TAG, "onError: Status = " + status.toString());

        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Helper method to format information about a place nicely.
     */
    private static Spanned formatPlaceDetails(Resources res,
                                              CharSequence name,
                                              CharSequence address) {
        Log.e(TAG, res.getString(R.string.place_details, name, address));
        return Html.fromHtml(res.getString(R.string.place_details, name, address));
    }

    public class SpinnerItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    private void submitPost() {

        final String recipient = mRecipientField.getText().toString();
        final String phone = mPhoneField.getText().toString();
        final String message = mMessageField.getText().toString();
        //final String bloodType = bloodTypeSpinner.getSelectedItem().toString();
        //final String organ = organSpinner.getSelectedItem().toString();

        final String mTransplantPlaceId;
        final String mTransplantPlaceName;
        final String mTransplantPlaceAddress;


        // Validate required fields
        if (TextUtils.isEmpty(recipient)) {
            mRecipientField.setError(REQUIRED);
            return;
        }
        if (mTransplantPlace == null) {

            Toast.makeText(this, R.string.transfusion_place_empty,
                    Toast.LENGTH_SHORT).show();
            return;
        } else {
            mTransplantPlaceId = mTransplantPlace.getId();
            mTransplantPlaceName = (String) mTransplantPlace.getName();
            mTransplantPlaceAddress = (String) mTransplantPlace.getAddress();
        }

        // recipientPhone number is not required at the moment
      /*  if (TextUtils.isEmpty(recipientPhone)) {
            mPhoneField.setError(REQUIRED);
            return;
        }*/


        // message is not required at the moment
      /*  if (TextUtils.isEmpty(message)) {
            mMessageField.setError(REQUIRED);
            return;
        }*/

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(EditOrganRequestActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writePostToDatabase(
                                    mOrganPostsRef,
                                    recipient,
                                    mTransplantPlaceId,
                                    phone,
                                    message);
                            writePostToDatabase(
                                    mUserPostRef,
                                    recipient,
                                    mTransplantPlaceId,
                                    phone,
                                    message);

                            writePlaceToDatabase(
                                    mTransplantPlaceId,
                                    mTransplantPlaceName,
                                    mTransplantPlaceAddress);
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
        mRecipientField.setEnabled(enabled);
        mPhoneField.setEnabled(enabled);
        bloodTypeSpinner.setEnabled(enabled);
        organSpinner.setEnabled(enabled);
        mMessageField.setEnabled(enabled);
    }

    private void writePostToDatabase(final DatabaseReference postRef,
                                     final String newRecipient,
                                     final String newPlaceId,
                                     final String newPhone,
                                     final String newMessage) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                OrganPost post = mutableData.getValue(OrganPost.class);

                if (post == null) {
                    return Transaction.success(mutableData);
                }
                post.title = post.organ + " for " + newRecipient;
                post.recipientName = newRecipient;
                post.placeId = newPlaceId;
                post.recipientPhone = newPhone;
                post.recipientMessage = newMessage;

                if (postRef == mOrganPostsRef){
                    writeNewPlaceOrganPost(newPlaceId, post);
                }


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

    private void writeNewPlaceOrganPost(String newPlaceId, OrganPost newPost) {
        mPlaceOrganPostsRef.setValue(null);
        mDatabase.child("place-organposts")
                .child(newPlaceId)
                .child(mPostKey)
                .setValue(newPost);
    }

    // [START basic_write]
    private void writePlaceToDatabase(String placeId, String placeName, String placeAddress) {
        DonationPlace donationPlace = new DonationPlace(placeName, placeAddress);
        mDatabase.child("donationplaces").child(placeId).setValue(donationPlace);
    }
    // [END basic_write]

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     */
    private void showSnackbar(final int mainTextStringId) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .show();
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
