/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.givelives.givelives;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import net.givelives.givelives.auth.AuthUiActivity;
import net.givelives.givelives.blood.AddBloodRequestActivity;
import net.givelives.givelives.journey.JourneyActivity;
import net.givelives.givelives.models.User;
import net.givelives.givelives.navmenu.FeedbackActivity;
import net.givelives.givelives.utility.AboutUsFragment;
import net.givelives.givelives.utility.FirebaseUtils;
import net.givelives.givelives.utility.GlideApp;
import net.givelives.givelives.wellbeing.CardContentFragmentHappier;
import net.givelives.givelives.blood.fragment.MyBloodPostsFragment;
import net.givelives.givelives.blood.fragment.BloodPostsFromAPlaceFragment;
import net.givelives.givelives.blood.fragment.RecentBloodPostsFragment;

import net.givelives.givelives.organ.AddOrganRequestActivity;
import net.givelives.givelives.organ.fragment.MyOrganPostsFragment;
import net.givelives.givelives.organ.fragment.OrganPostsFromAPlaceFragment;
import net.givelives.givelives.organ.fragment.RecentOrganPostsFragment;

import java.util.ArrayList;

import java.util.List;

import static net.givelives.givelives.blood.fragment.BloodPostListFragment.getUid;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // The request code for AllRequestsOnMapActivity
    public static final int BLOOD_ON_MAP_REQUEST = 100;
    public static final int ORGAN_ON_MAP_REQUEST = 200;


    // Bundle that store placeId fetched from AllRequestsOnMapActivity
    Bundle mBundle = new Bundle();

    private Adapter adapter;
    private ArrayList<Fragment> mFragmentList = new ArrayList<>();
    private List<String> mFragmentTitleList = new ArrayList<>();
    public FragmentManager fmr = getSupportFragmentManager();

    private ViewPager mViewPager;
    private TabLayout tabLayout;

    private DatabaseReference mUserRef;

    private DrawerLayout mDrawerLayout;
    private ImageView mProfilePhotoImageView;
    private TextView mFullNameTextView;

    private FloatingActionMenu floatingBloodActionButton;
    private FloatingActionMenu floatingOrganActionButton;

    public static final int INDEX_OF_TAB_WITH_FAB_0 = 0;
    public static final int INDEX_OF_TAB_WITH_FAB_1 = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // temp1
        mBundle.putString("placeIndex", "ChIJfxMx8dPOsGoRv1vOEL-iqac");

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mUserRef = FirebaseUtils.getDatabaseRef().child("users")
                .child(getUid());

        // write the current device's FCM token to realtime database
        String token = FirebaseInstanceId.getInstance().getToken();
        mUserRef.child("notificationTokens").child(token).setValue(true);
        Log.d("tokenT", token);

        // Create Navigation drawer and inlfate layout
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        // Adding menu icon to Toolbar
        // Get a support ActionBar corresponding to this toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            VectorDrawableCompat indicator
                    = VectorDrawableCompat.create(getResources(), R.drawable.ic_menu, getTheme());
            indicator.setTint(ResourcesCompat.getColor(getResources(), R.color.white, getTheme()));
            supportActionBar.setHomeAsUpIndicator(indicator);
            // Enable the Up button
            supportActionBar.setDisplayHomeAsUpEnabled(true);

        }

        View headerView =  navigationView.getHeaderView(0);
        mFullNameTextView = headerView.findViewById(R.id.profile_full_name);

        mProfilePhotoImageView  = headerView.findViewById(R.id.profile_photo);

        // Set behavior of Navigation drawer
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    // This method will trigger on item Click of navigation menu
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        // Set item in checked state
                       // menuItem.setChecked(true);
                        // Closing drawer on item click
                        mDrawerLayout.closeDrawers();

                        // TODO: handle navigation
                        /*if (menuItem.getItemId() == R.id.view_requests) {
                            Intent intent = new Intent(getBaseContext(), AddBloodRequestActivity.class);
                            startActivity(intent);
                            return true;
                        }

                        return true;*/
                        switch (menuItem.getItemId()) {

                            case R.id.view_journeys:
                                Intent iJourney = new Intent(getBaseContext(), JourneyActivity.class);
                                startActivity(iJourney);
                                return true;

                            case R.id.invite_friends:
                                String sentText = getString(R.string.app_link);
                                Intent iInvite = new Intent();
                                iInvite.setAction(Intent.ACTION_SEND);
                                iInvite.putExtra(Intent.EXTRA_TEXT, sentText);
                                iInvite.setType("text/plain");
                                startActivity(Intent.createChooser(iInvite, getResources().getText(R.string.send_to)));
                                return true;

                            case R.id.send_feedback:
                                Intent iFeedback = new Intent(getBaseContext(), FeedbackActivity.class);
                                startActivity(iFeedback);
                                return true;

                            case R.id.like_facebook:
                                Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                                String facebookUrl = getFacebookPageURL(getBaseContext());
                                facebookIntent.setData(Uri.parse(facebookUrl));
                                startActivity(facebookIntent);
                                return true;

                            case R.id.learn_organ_transplantation:
                                Uri uriOrgan = Uri.parse(getString(R.string.donateLifeUrl));
                                Intent iLearnOrgan = new Intent(Intent.ACTION_VIEW, uriOrgan);
                                startActivity(iLearnOrgan);
                                return true;

                            case R.id.learn_blood_transfusion:
                                Uri uriBlood = Uri.parse(getString(R.string.redCrossUrl));
                                Intent iLearnBlood = new Intent(Intent.ACTION_VIEW, uriBlood);
                                startActivity(iLearnBlood);
                                return true;

                            case R.id.about_us:
                                DialogFragment aboutUsFragment = new AboutUsFragment();
                                aboutUsFragment.show(getSupportFragmentManager(), "about_us");
                                return true;

                            default:
                                // If we got here, the user's action was not recognized. Just return true
                                return true;
                        }
                    }
                });

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        adapter = new Adapter(fmr, mFragmentList, mFragmentTitleList);
        mViewPager.setAdapter(adapter);
        addFragment(new RecentBloodPostsFragment(), getString(R.string.heading_blood));
        addFragment(new RecentOrganPostsFragment(), getString(R.string.heading_organ));
        addFragment(new CardContentFragmentHappier(), getString(R.string.heading_happier));
        adapter.notifyDataSetChanged();
        //setupViewPager(mViewPager, new RecentOrganPostsFragment());

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Add Floating Button Menu
        floatingBloodMenuBtnSetup();
        floatingOrganMenuBtnSetup();
        floatingBloodActionButton.setVisibility(View.VISIBLE);
        floatingOrganActionButton.setVisibility(View.GONE);

        // open tab 2 when users want to see their own requests
       /* int defaultValue = 0;
        int page = getIntent().getIntExtra(MainActivity.EXTRA_TAB_2, defaultValue);
        mViewPager.setCurrentItem(page);*/

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position,
                                       float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                // set FAB visible according to page number.
                switch (position) {
                    case INDEX_OF_TAB_WITH_FAB_0:
                        floatingBloodActionButton.setVisibility(View.VISIBLE);
                        floatingOrganActionButton.setVisibility(View.GONE);
                        break;

                    case INDEX_OF_TAB_WITH_FAB_1:
                        floatingOrganActionButton.setVisibility(View.VISIBLE);
                        floatingBloodActionButton.setVisibility(View.GONE);
                        break;

                    default:
                        floatingBloodActionButton.setVisibility(View.GONE);
                        floatingOrganActionButton.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        final ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                User user = dataSnapshot.getValue(User.class);
                if (user.photoUrl != null) {

                    GlideApp
                            .with(getBaseContext())
                            .load(user.photoUrl)
                            .circleCrop()
                            .override(150)
                            .into(mProfilePhotoImageView);
                }

                if (user.fullName != null) {
                    mFullNameTextView.setText(user.fullName);
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
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case BLOOD_ON_MAP_REQUEST:

                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mBundle = data.getBundleExtra("placeIdBundle");

                        mFragmentList.remove(0);
                        mFragmentList.add(0, BloodPostsFromAPlaceFragment
                                .newInstance(mBundle.getString("placeIdSelected")));

                        System.out.println("xxx" + mBundle.getString("placeIdSelected"));
                        adapter.notifyDataSetChanged();

                        break;
                    case Activity.RESULT_CANCELED:
                        System.out.println("xxxy" + mBundle.getString("placeIdSelected"));
                        break;
                }
                break;

            case ORGAN_ON_MAP_REQUEST:

                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mBundle = data.getBundleExtra("placeIdBundle");

                        mFragmentList.remove(1);
                        mFragmentList.add(1, OrganPostsFromAPlaceFragment
                                .newInstance(mBundle.getString("placeIdSelected")));

                        System.out.println("xxx" + mBundle.getString("placeIdSelected"));
                        adapter.notifyDataSetChanged();

                        break;
                    case Activity.RESULT_CANCELED:
                        System.out.println("xxxy" + mBundle.getString("placeIdSelected"));
                        break;
                }
                break;
        }
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    public class Adapter extends FragmentStatePagerAdapter {
        private ArrayList<Fragment> mFragmentList;
        private List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager,
                       ArrayList<Fragment> fragments,
                       List<String> mFragmentTitleList) {
            super(manager);
            this.mFragmentList = fragments;
            this.mFragmentTitleList = mFragmentTitleList;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public int getItemPosition(Object object) {

            // POSITION_NONE makes it possible to reload the PagerAdapter
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    // set up FloatingBloodActionMenu
    private void floatingBloodMenuBtnSetup() {
        floatingBloodActionButton = (FloatingActionMenu) findViewById(R.id.fab_blood);
        floatingBloodActionButton.setClosedOnTouchOutside(true);

        FloatingActionButton fabMyBloodRequest = (FloatingActionButton) findViewById(R.id.fabMyBloodRequest);
        FloatingActionButton fabRecentBloodRequest = (FloatingActionButton) findViewById(R.id.fabRecentBloodRequest);
        FloatingActionButton fabBloodMap = (FloatingActionButton) findViewById(R.id.fabBloodMap);
        fabMyBloodRequest.setOnClickListener(clickListenerBloodFAB);
        fabRecentBloodRequest.setOnClickListener(clickListenerBloodFAB);
        fabBloodMap.setOnClickListener(clickListenerBloodFAB);
    }


    private View.OnClickListener clickListenerBloodFAB = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // TODO code fab menu
                case R.id.fabMyBloodRequest:
                    floatingBloodActionButton.close(true);
                    mFragmentList.remove(0);
                    mFragmentList.add(0, new MyBloodPostsFragment());
                    adapter.notifyDataSetChanged();
                    break;

                case R.id.fabRecentBloodRequest:
                    floatingBloodActionButton.close(true);
                    mFragmentList.remove(0);
                    mFragmentList.add(0, new RecentBloodPostsFragment());
                    adapter.notifyDataSetChanged();
                    break;

                case R.id.fabBloodMap:
                    floatingBloodActionButton.close(true);
                    Intent i = new Intent(getApplicationContext(), AllRequestsOnMapActivity.class);
                    startActivityForResult(i, BLOOD_ON_MAP_REQUEST);
                    break;
            }
        }
    };

    // set up FloatingOrganActionMenu
    private void floatingOrganMenuBtnSetup() {
        floatingOrganActionButton = (FloatingActionMenu) findViewById(R.id.fab_organ);
        floatingOrganActionButton.setClosedOnTouchOutside(true);

        FloatingActionButton fabMyOrganRequest = (FloatingActionButton) findViewById(R.id.fabMyOrganRequest);
        FloatingActionButton fabRecentOrganRequest = (FloatingActionButton) findViewById(R.id.fabRecentOrganRequest);
        FloatingActionButton fabOrganMap = (FloatingActionButton) findViewById(R.id.fabOrganMap);
        fabMyOrganRequest.setOnClickListener(clickListenerOrganFAB);
        fabRecentOrganRequest.setOnClickListener(clickListenerOrganFAB);
        fabOrganMap.setOnClickListener(clickListenerOrganFAB);
    }

    private View.OnClickListener clickListenerOrganFAB = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // TODO code fab menu
                case R.id.fabMyOrganRequest:
                    floatingOrganActionButton.close(true);
                    mFragmentList.remove(1);
                    mFragmentList.add(1, new MyOrganPostsFragment());
                    adapter.notifyDataSetChanged();
                    break;

                case R.id.fabRecentOrganRequest:
                    floatingOrganActionButton.close(true);
                    mFragmentList.remove(1);
                    mFragmentList.add(1, new RecentOrganPostsFragment());
                    adapter.notifyDataSetChanged();
                    break;

                case R.id.fabOrganMap:
                    floatingOrganActionButton.close(true);
                    Intent i = new Intent(getApplicationContext(), AllRequestsOnMapActivity.class);
                    startActivityForResult(i, ORGAN_ON_MAP_REQUEST);
                    break;
            }
        }
    };

    //method to get the right URL to use in the intent
    public String getFacebookPageURL(Context context) {

        String FACEBOOK_URL = getString(R.string.giveLivesFacebookUrl);
        String FACEBOOK_PAGE_ID = getString(R.string.giveLivesFacebookId);

        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://facewebmodal/f?href=" + FACEBOOK_URL;
            } else { //older versions of fb app
                return "fb://page/" + FACEBOOK_PAGE_ID;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return FACEBOOK_URL; //normal web url
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            /*case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;*/

            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            case R.id.action_add_blood_request:
                Intent intent = new Intent(getBaseContext(), AddBloodRequestActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_add_organ_request:
                Intent intent2 = new Intent(getBaseContext(), AddOrganRequestActivity.class);
                startActivity(intent2);
                return true;

            case R.id.action_settings:
                return true;

            case R.id.action_sign_out:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    startActivity(AuthUiActivity.createIntentBackToAuth(MainActivity.this));
                                    finish();
                                }
                            }
                        });
                return true;

         /*   case action_new_request:
                startActivity(new Intent(MainActivity.this, AddBloodRequestActivity.class));
                return true;*/

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it (this includes method handling up action/button).
                return super.onOptionsItemSelected(item);
        }
    }
}
