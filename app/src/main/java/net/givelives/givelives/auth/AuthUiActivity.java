/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.givelives.givelives.auth;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.AuthUI.IdpConfig;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import net.givelives.givelives.R;
import net.givelives.givelives.MainActivity;
import net.givelives.givelives.models.User;
import net.givelives.givelives.utility.FirebaseUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AuthUiActivity extends AppCompatActivity {

    // TODO change these URL values
    private static final String TERMS_OF_SERVICE_URL = "https://www.google.com/policies/terms/";
    private static final String PRIVACY_POLICY_URL = "https://www.google.com/policies/privacy/";

    private static final int RC_SIGN_IN = 100;

    private static final boolean CREDENTIAL_SELECTOR_ENABLED = true;
    private static final boolean HINT_SELECTOR_ENABLED = true;
    private static final boolean ALLOW_NEW_EMAIL_ACCOUNTS_CREATION = true;


    @BindView(R.id.root)
    View mRootView;

    // instantiate FirebaseAuth and DatabaseReference
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;

    public static Intent createIntentBackToAuth(Context context) {
        return new Intent(context, AuthUiActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_ui_layout);
        ButterKnife.bind(this);

        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseUtils.getDatabaseRef();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if (auth.getCurrentUser() != null) {
            startSignedInActivity(null, auth.getCurrentUser());
            finish();
            return;
        } else {
            signInNow();
        }
    }


    // Sign in UI derived from Firebase library
    public void signInNow() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setTheme(R.style.GreenTheme)
                        .setLogo(R.drawable.ic_main_medium)
                        .setAvailableProviders(getSelectedProviders())
                        .setTosUrl(TERMS_OF_SERVICE_URL)
                        .setPrivacyPolicyUrl(PRIVACY_POLICY_URL)
                        .setIsSmartLockEnabled(CREDENTIAL_SELECTOR_ENABLED,
                                HINT_SELECTOR_ENABLED)
                        .setAllowNewEmailAccounts(ALLOW_NEW_EMAIL_ACCOUNTS_CREATION)
                        .build(),
                RC_SIGN_IN);
    }


    @OnClick(R.id.sign_in)
    public void signInButton(View view) {
       signInNow();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
            return;
        }

        showSnackbar(R.string.unknown_response);
    }

    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        // Successfully signed in
        if (resultCode == RESULT_OK) {
            startSignedInActivity(response, auth.getCurrentUser());
            finish();
            return;
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                showSnackbar(R.string.sign_in_cancelled);
                return;
            }

            if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection);
                return;
            }

            if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                showSnackbar(R.string.unknown_error);
                return;
            }
        }

        showSnackbar(R.string.unknown_sign_in_response);
    }

    // TODO edit this method accordingly to start main activity
/*    private void startSignedInActivity(IdpResponse response) {
        startActivity(
                SignedInActivity.createIntentBackToAuth(
                        this,
                        response,
                        new SignedInActivity.SignedInConfig(
                                R.mipmap.ic_launcher,
                                R.style.GreenTheme,
                                getSelectedProviders(),
                                TERMS_OF_SERVICE_URL,
                                CREDENTIAL_SELECTOR_ENABLED,
                                HINT_SELECTOR_ENABLED)));
    }*/

    private void startSignedInActivity(IdpResponse response, FirebaseUser user) {
        String username = usernameFromEmail(user.getEmail());

        String userProfilePhotoUrl;
        Uri userPhotoUrl = user.getPhotoUrl();

        // Write new user
        if (userPhotoUrl == null) {
            userProfilePhotoUrl = "";
        }
        else {
            userProfilePhotoUrl = userPhotoUrl.toString();
        }
            writeNewUser(user.getUid(),
                    username,
                    user.getEmail(),
                    user.getDisplayName(),
                    userProfilePhotoUrl);

        // Go to MainActivity
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @MainThread
    private List<IdpConfig> getSelectedProviders() {
        List<IdpConfig> selectedProviders = new ArrayList<>();

        selectedProviders.add(
                new IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                        .setPermissions(getGooglePermissions())
                        .build());

        selectedProviders.add(
                new IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER)
                        .setPermissions(getFacebookPermissions())
                        .build());

        selectedProviders.add(
                new IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build());

        selectedProviders.add(
                new IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());

//        selectedProviders.add(
//                new IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build());

        return selectedProviders;
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    // TODO edit to include more permissions to FACEBOOK login access
    @MainThread
    private List<String> getFacebookPermissions() {
        List<String> result = new ArrayList<>();

        //   result.add("user_friends");
       // result.add("user_photos");

        return result;
    }

    // TODO edit to include more permissions to Google login access
    @MainThread
    private List<String> getGooglePermissions() {
        List<String> result = new ArrayList<>();
        // include youtube data
        //result.add("https://www.googleapis.com/auth/youtube.readonly");
        // include drive files
        //result.add(Scopes.DRIVE_FILE);

        return result;
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    // [START basic_write]
    private void writeNewUser(String userId, String username, String email, String fullName, String photoUrl) {
        // update a child without rewriting the entire object. allow users to update their profiles as follows:
        mDatabase.child("users").child(userId).child("username").setValue(username);
        mDatabase.child("users").child(userId).child("email").setValue(email);
        mDatabase.child("users").child(userId).child("fullName").setValue(fullName);
        mDatabase.child("users").child(userId).child("photoUrl").setValue(photoUrl);
    }
    // [END basic_write]
}
