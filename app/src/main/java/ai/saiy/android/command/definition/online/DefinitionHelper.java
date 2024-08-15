package ai.saiy.android.command.definition.online;

import android.content.Context;
import android.net.ParseException;
import android.util.Pair;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import ai.saiy.android.firebase.database.reference.DefinitionReference;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class DefinitionHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = DefinitionHelper.class.getSimpleName();
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String JSON_HEADER_ACCEPT = "accept";
    private static final String JSON_HEADER_VALUE_ACCEPT = "application/json";

    private String response;
    private HttpURLConnection httpURLConnection;

    private void disconnect() {
        if (httpURLConnection != null) {
            try {
                httpURLConnection.disconnect();
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Pair<Boolean, DefinitionResponse> execute(Context context, String str) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "execute");
        }
        final long then = System.nanoTime();
        final Pair<Boolean, String> authPair = new DefinitionReference().getAPIKey(context);
        try {
            if (authPair.first) {
                this.httpURLConnection = (HttpURLConnection) new URL(("http://api.wordnik.com/v4/word.json/" + str + "/definitions?limit=1&includeRelated=false&useCanonical=true&includeTags=false&api_key=" + authPair.second).replaceAll("\\s", "%20")).openConnection();
                httpURLConnection.setRequestMethod(Constants.HTTP_GET);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestProperty(CONTENT_TYPE, JSON_HEADER_VALUE_ACCEPT);
                httpURLConnection.setRequestProperty(JSON_HEADER_ACCEPT, JSON_HEADER_VALUE_ACCEPT);
                httpURLConnection.connect();
                int responseCode = httpURLConnection.getResponseCode();
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "responseCode: " + responseCode);
                }
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    this.response = UtilsString.streamToString(httpURLConnection.getInputStream());
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "response: " + response);
                    }
                } else if (DEBUG) {
                    MyLog.e(CLS_NAME, "error stream: " + UtilsString.streamToString(httpURLConnection.getErrorStream()));
                }
            } else if (DEBUG) {
                MyLog.w(CLS_NAME, "authPair: error");
            }
        } catch (MalformedURLException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "MalformedURLException");
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "UnsupportedEncodingException");
                e.printStackTrace();
            }
        } catch (ParseException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ParseException");
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "UnknownHostException");
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "IOException");
                e.printStackTrace();
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "IllegalStateException");
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        } finally {
            disconnect();
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        if (UtilsString.notNaked(response)) {
            final DefinitionResponse[] definitionResponses = new GsonBuilder().disableHtmlEscaping().create().fromJson(response, DefinitionResponse[].class);
            return (definitionResponses == null || definitionResponses.length == 0) ? new Pair<>(false, null) : new Pair<>(true, definitionResponses[0]);
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "response: failed");
        }
        return new Pair<>(false, null);
    }
}
