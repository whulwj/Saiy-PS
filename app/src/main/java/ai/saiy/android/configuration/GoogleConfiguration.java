/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.configuration;

import android.content.res.Resources;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.util.Date;

import ai.saiy.android.utils.MyLog;

/**
 * Enter your Google Chromium Speech API key below. You need to register in the Google Group and
 * enable this in your API console. Without doing both, it WILL NOT WORK!
 * <p/>
 * Created by benrandall76@gmail.com on 12/02/2016.
 */
public final class GoogleConfiguration {
    private static final String CLS_NAME = GoogleConfiguration.class.getSimpleName();
    /**
     * Prevent instantiation
     */
    public GoogleConfiguration() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    public static final String GOOGLE_SPEECH_API_KEY = "_your_value_here_";
    public static final String GOOGLE_TRANSLATE_PROJECT_ID = "_your_value_here_";

    private static final String GOOGLE_SPEECH_CLOUD_API_KEY = "_your_value_here_";

    public static AccessToken sAccessToken = new AccessToken(GoogleConfiguration.GOOGLE_SPEECH_CLOUD_API_KEY,
            new Date(System.currentTimeMillis() - 3600000L));

    public static final String CLOUD_PROJECT_ID = "plucky-sound-315505";

    // To make API requests, you will need to authenticate your application. This can be done by obtaining an API key or setting up service account credentials.
    public static void authenticateExplicit() throws IOException {
        // Construct the GoogleCredentials object which obtains the default configuration from your
        // working environment.
        // GoogleCredentials.getApplicationDefault() will give you ComputeEngineCredentials
        // if you are on a GCE (or other metadata server supported environments).
        final GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        // If you are authenticating to a Cloud API, you can let the library include the default scope,
        // https://www.googleapis.com/auth/cloud-platform, because IAM is used to provide fine-grained
        // permissions for Cloud.
        // For more information on scopes to use,
        // see: https://developers.google.com/identity/protocols/oauth2/scopes

        // The credentials must be refreshed before the access token is available.
        credentials.refreshIfExpired();
        GoogleConfiguration.sAccessToken = credentials.getAccessToken();
    }

    public static void refreshAccessToken() {
        try {
            final GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

            // The credentials must be refreshed before the access token is available.
            credentials.refreshIfExpired();
            GoogleConfiguration.sAccessToken = credentials.refreshAccessToken();
        } catch (Throwable t) {
            if (MyLog.DEBUG) {
                MyLog.w(CLS_NAME, "refreshAccessToken " + t.getClass().getSimpleName() + ", " + t.getMessage());
            }
        }
    }
}
