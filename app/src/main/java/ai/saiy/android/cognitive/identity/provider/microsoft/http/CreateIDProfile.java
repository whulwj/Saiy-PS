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

package ai.saiy.android.cognitive.identity.provider.microsoft.http;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.cognitive.identity.provider.microsoft.containers.EnrollmentID;
import ai.saiy.android.utils.MyLog;

/**
 * Class to get an initial enrollment id, which will be used to enroll and validate the user.
 * <p>
 * This request is always synchronous, as it is an 'on-demand' requirement.
 * <p>
 * Created by benrandall76@gmail.com on 08/06/2016.
 */
public class CreateIDProfile {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CreateIDProfile.class.getSimpleName();

    private static final String CREATE_URL = "https://westus.api.cognitive.microsoft.com/spid/v1.0/identificationProfiles";
    private static final String OCP_SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";
    private static final String ENCODING = "UTF-8";
    private static final String CHARSET = "Accept-Charset";
    private static final String JSON_HEADER_ACCEPT = "accept";
    private static final String JSON_HEADER_VALUE_ACCEPT = "application/json";
    private static final String LOCALE_PARAM = "locale";

    private static final long THREAD_TIMEOUT = 7L;

    private final Context mContext;
    private final String apiKey;
    private final Locale locale;

    /**
     * Constructor
     *
     * @param mContext the application context
     * @param apiKey   the api key
     * @param locale   the locale of the user. Currently unused as only en-us is supported
     */
    public CreateIDProfile(@NonNull final Context mContext, @NonNull final String apiKey,
                           @NonNull final Locale locale) {
        this.apiKey = apiKey;
        this.mContext = mContext.getApplicationContext();
        this.locale = locale;
    }

    /**
     * Method to get an enrollment id.
     *
     * @return an {@link Pair} of which the first parameter will denote success and the second an
     * {@link EnrollmentID} object, containing the id. If the request was unsuccessful,
     * the second parameter may be null.
     */
    public Pair<Boolean, EnrollmentID> getID() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getID");
        }

        final long then = System.nanoTime();

        final JSONObject object = new JSONObject();

        try {
            object.put(LOCALE_PARAM, "en-us");
        } catch (final JSONException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getID: JSONException");
                e.printStackTrace();
            }
        }

        final RequestFuture<JSONObject> future = RequestFuture.newFuture();
        final RequestQueue queue = Volley.newRequestQueue(mContext);
        queue.start();

        final JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, CREATE_URL, object, future,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(final VolleyError error) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onErrorResponse: " + error.toString());
                            CreateIDProfile.this.verboseError(error);
                        }
                        queue.stop();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                final Map<String, String> params = new HashMap<>();
                params.put(CHARSET, ENCODING);
                params.put(JSON_HEADER_ACCEPT, JSON_HEADER_VALUE_ACCEPT);
                params.put(OCP_SUBSCRIPTION_KEY_HEADER, apiKey);
                return params;
            }
        };

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);

        JSONObject response = null;

        try {
            response = future.get(THREAD_TIMEOUT, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getID: InterruptedException");
                e.printStackTrace();
            }
        } catch (final ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getID: ExecutionException");
                e.printStackTrace();
            }
        } catch (final TimeoutException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getID: TimeoutException");
                e.printStackTrace();
            }
        } finally {
            queue.stop();
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        if (response != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "response: " + response.toString());
            }

            final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            final EnrollmentID enrollmentID = gson.fromJson(response.toString(), EnrollmentID.class);

            if (DEBUG) {
                MyLog.i(CLS_NAME, "onResponse: getId: " + enrollmentID.getId());
            }

            return new Pair<>(true, enrollmentID);

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "response: failed");
            }
            return new Pair<>(false, null);
        }
    }

    /**
     * Used for debugging only to view verbose error information
     *
     * @param error the {@link VolleyError}
     */
    private void verboseError(@NonNull final VolleyError error) {

        final NetworkResponse response = error.networkResponse;

        if (response != null && error instanceof ServerError) {

            try {
                final String result = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                MyLog.i(CLS_NAME, "result: " + result);
            } catch (final UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}
