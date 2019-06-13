package net.givelives.givelives.journey;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import net.givelives.givelives.R;
import net.givelives.givelives.blood.BloodRequestDetailActivity;
import net.givelives.givelives.models.BloodDonor;
import net.givelives.givelives.models.BloodPost;
import net.givelives.givelives.models.OrganDonor;
import net.givelives.givelives.models.OrganPost;
import net.givelives.givelives.organ.OrganRequestDetailActivity;
import net.givelives.givelives.utility.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class JourneyActivity extends AppCompatActivity {
    private static final String TAG = "cancelled_post_load" ;
    private List<BloodDonor> mBloodGivingList = new ArrayList<>();
    private List<OrganDonor> mOrganGivingList = new ArrayList<>();
    private RecyclerView recyclerView;
    private JourneyAdapter mAdapter;

    private DatabaseReference mDatabase;
    private DatabaseReference mBloodJourneyRef;
    private DatabaseReference mOrganJourneyRef;
    private List<DatabaseReference> mBloodPostsRefList = new ArrayList<>();
    private List<DatabaseReference> mOrganPostsRefList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journey_activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseUtils.getDatabaseRef();
        mBloodJourneyRef = mDatabase.child("users").child(getUid()).child("bloodJourney");
        mOrganJourneyRef = mDatabase.child("users").child(getUid()).child("organJourney");

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        mAdapter = new JourneyAdapter(mBloodGivingList, mOrganGivingList);

        recyclerView.setHasFixedSize(true);

        // vertical RecyclerView
        // keep movie_list_row.xml width to `match_parent`
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());

        // horizontal RecyclerView
        // keep movie_list_row.xml width to `wrap_content`
        // RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(mLayoutManager);

        // adding inbuilt divider line
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // adding custom divider line with padding 16dp
        // recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.HORIZONTAL, 16));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(mAdapter);

        // row click listener
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                if (position < (mAdapter.getItemCount() - mOrganGivingList.size())) {
                    DatabaseReference postRef = mBloodPostsRefList.get(position);

                    ValueEventListener postListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get BloodPost object and use the values to update the UI
                            final BloodPost post = dataSnapshot.getValue(BloodPost.class);

                            // [START_EXCLUDE]
                            if (post != null) {
                                // Launch BloodRequestDetailActivity
                                Intent intent = new Intent(getBaseContext(), BloodRequestDetailActivity.class);
                                intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_KEY, dataSnapshot.getKey());
                                intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_UID, post.uid);
                                intent.putExtra(BloodRequestDetailActivity.EXTRA_POST_PLACE_ID, post.placeId);

                                startActivity(intent);
                            }

                            // [END_EXCLUDE]
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    };
                    postRef.addValueEventListener(postListener);
                }
                else {
                    int arrayItemPosition = position - mBloodGivingList.size();
                    
                    DatabaseReference postRef = mOrganPostsRefList.get(arrayItemPosition);

                    ValueEventListener postListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get BloodPost object and use the values to update the UI
                            final OrganPost post = dataSnapshot.getValue(OrganPost.class);

                            // [START_EXCLUDE]
                            if (post != null) {
                                // Launch OrganRequestDetailActivity
                                Intent intent = new Intent(getBaseContext(), OrganRequestDetailActivity.class);
                                intent.putExtra(OrganRequestDetailActivity.EXTRA_POST_KEY, dataSnapshot.getKey());
                                intent.putExtra(OrganRequestDetailActivity.EXTRA_POST_UID, post.uid);
                                intent.putExtra(OrganRequestDetailActivity.EXTRA_POST_PLACE_ID, post.placeId);

                                startActivity(intent);
                            }

                            // [END_EXCLUDE]
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    };
                    postRef.addValueEventListener(postListener);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // [START post_value_event_listener]
        ValueEventListener bloodJourneyListener;
        bloodJourneyListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    BloodDonor bloodGiving = child.getValue(BloodDonor.class);
                    if (bloodGiving != null) {
                        buildBloodJourney(bloodGiving, mDatabase.child("bloodposts").child(child.getKey()));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //handle databaseError
            }
        };

        mBloodJourneyRef.addListenerForSingleValueEvent(bloodJourneyListener);

        ValueEventListener organJourneyListener;
        organJourneyListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    OrganDonor organGiving = child.getValue(OrganDonor.class);
                    if (organGiving != null) {
                        buildOrganJourney(organGiving, mDatabase.child("organposts").child(child.getKey()));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //handle databaseError
            }
        };

        mOrganJourneyRef.addListenerForSingleValueEvent(organJourneyListener);

        // notify adapter about data set changes
        // so that it will render the list with new data
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Prepares sample data to provide data set to adapter
     */
    private void buildBloodJourney(BloodDonor bloodGiving, DatabaseReference postRef) {
        mBloodGivingList.add(bloodGiving);
        mBloodPostsRefList.add(postRef);
    }

    private void buildOrganJourney(OrganDonor organGiving, DatabaseReference postRef) {
        mOrganGivingList.add(organGiving);
        mOrganPostsRefList.add(postRef);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // notify adapter about data set changes
        // so that it will render the list with new data
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBloodGivingList.clear();
        mOrganPostsRefList.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}
