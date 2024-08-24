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

package ai.saiy.android.nlu.apiai;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.auth.oauth2.AccessToken;
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;

import ai.saiy.android.api.language.nlu.NLULanguageAPIAI;
import ai.saiy.android.configuration.GoogleConfiguration;
import ai.saiy.android.utils.MyLog;

/**
 * Created by benrandall76@gmail.com on 03/06/2016.
 */
public class RemoteAPIAI {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RemoteAPIAI.class.getSimpleName();

    private final String utterance;
    private final AccessToken accessToken;
    private final NLULanguageAPIAI vrLocale;
    private final ApiRequest apiRequest;

    /**
     * Constructor
     *
     * @param context the application context
     * @param utterance the user utterance
     * @param accessToken the Google cloud access token
     * @param vrLocale the {@link NLULanguageAPIAI}
     */
    public RemoteAPIAI(@NonNull final Context context,
                       @NonNull final String utterance,
                       @NonNull final AccessToken accessToken,
                       @NonNull final NLULanguageAPIAI vrLocale) {
        this.utterance = utterance;
        this.accessToken = accessToken;
        this.vrLocale = vrLocale;

        this.apiRequest = new ApiRequest(context);
    }

    public Pair<Boolean, DetectIntentResponse> fetch() {
        try {
            DetectIntentResponse response;
            // check if the token is received and expiry time is received and not expired
            if (!TextUtils.isEmpty(accessToken.getTokenValue()) && accessToken.getExpirationTime().getTime() > System.currentTimeMillis()) {
                response = apiRequest.callAPI(accessToken.getTokenValue(), accessToken.getExpirationTime(), utterance, vrLocale, null, true, true, true);
            } else {
                response = null;
                // get new token if expired or not received
                GoogleConfiguration.refreshAccessToken();
            }

            if (response != null) {
                return new Pair<>(true, response);
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "response null");
                }
            }

        } catch (Throwable t) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "DetectIntentResponse " + t.getClass().getSimpleName() + ", " + t.getMessage());
            }
        }

        return new Pair<>(false, null);
    }
}
