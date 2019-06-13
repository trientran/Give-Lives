package net.givelives.givelives.organ;

import android.content.res.Resources;
import android.os.Bundle;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.givelives.givelives.models.OrganPost;
import net.givelives.givelives.R;
import net.givelives.givelives.models.DonationPlace;
import net.givelives.givelives.models.User;
import net.givelives.givelives.utility.FirebaseUtils;

import java.util.HashMap;
import java.util.Map;

import static net.givelives.givelives.R.id.action_done;

/**
 * Created by MainAcc on 24/09/2017.
 */

public class AddOrganRequestActivity extends AppCompatActivity implements PlaceSelectionListener{

    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    private TextInputEditText mRecipientField;
    private TextInputEditText mPhoneField;
    private TextInputEditText mMessageField;

    private Spinner bloodTypeSpinner;
    private Spinner organSpinner;

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

        // [START initialize_database_ref]
        mDatabase = FirebaseUtils.getDatabaseRef();
        // [END initialize_database_ref]

        mRecipientField = (TextInputEditText) findViewById(R.id.organ_hint_name);
        mTransplantPlace = null;
        mPhoneField = (TextInputEditText) findViewById(R.id.organ_hint_phone);

        mMessageField = (TextInputEditText) findViewById(R.id.organ_hint_msg);

        // All about Blood Type Spinner and Organ Spinner
        bloodTypeSpinner = (Spinner) findViewById(R.id.blood_type_spinner);
        organSpinner = (Spinner) findViewById(R.id.organ_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> bloodTypeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.blood_group_array, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> organSpinnerAdapter = ArrayAdapter.createFromResource(this,
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

    /**
     * Callback invoked when a place has been selected from the PlaceAutocompleteFragment.
     */
    @Override
    public void onPlaceSelected(Place place) {
        Log.i(TAG, "Place Selected: " + place.getName());

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
        final String bloodType = bloodTypeSpinner.getSelectedItem().toString();
        final String organ = organSpinner.getSelectedItem().toString();

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
                            Toast.makeText(AddOrganRequestActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writeNewPost(
                                    userId,
                                    recipient,
                                    mTransplantPlaceId,
                                    phone,
                                    bloodType,
                                    organ,
                                    message);

                            writeNewPlace(
                                    mTransplantPlaceId,
                                    mTransplantPlaceName,
                                    mTransplantPlaceAddress);
                        }

                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                        finish(); // to finish current activity and get back to the previous one
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
        // [END single_value_read]
    }

    private void setEditingEnabled(boolean enabled) {
        mRecipientField.setEnabled(enabled);
        mPhoneField.setEnabled(enabled);
        bloodTypeSpinner.setEnabled(enabled);
        organSpinner.setEnabled(enabled);
        mMessageField.setEnabled(enabled);
    }

    // [START write_fan_out]
    private void writeNewPost(String uid,
                              String recipient,
                              String placeId,
                              String phone,
                              String bloodType,
                              String organ,
                              String message) {
        // Create new post at /user-organposts/$userid/$postid and at
        // /organposts/$postid simultaneously
        String key = mDatabase.child("organposts").push().getKey();
        OrganPost post = new OrganPost(
                uid,
                recipient,
                placeId,
                phone,
                bloodType,
                organ,
                message);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/organposts/" + key, postValues);
        childUpdates.put("/user-organposts/" + uid + "/" + key, postValues);
        childUpdates.put("/place-organposts/" + placeId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }
    // [END write_fan_out]

    // [START basic_write]
    private void writeNewPlace(String placeId, String placeName, String placeAddress) {
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
                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it (this includes method handling up action/button).
                return super.onOptionsItemSelected(item);

        }
    }
}
