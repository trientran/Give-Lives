// Copyright 2014 Google Inc. All Rights Reserved.

package net.givelives.givelives.wellbeing;

import android.content.Context;

import net.givelives.givelives.R;

/**
 * Static container class for holding a reference to your YouTube Developer Key.
 */
public class DeveloperKey {

    /**
     * Please replace this with a valid API key which is enabled for the
     * YouTube Data API v3 service. Go to the
     * <a href="https://console.developers.google.com/">Google Developers Console</a>
     * to register a new developer key.
     */

    public static String getApiKey(Context context) {
        String apiKey;
        apiKey = context.getResources().getString(R.string.api_key);
        return apiKey;
    }

}
