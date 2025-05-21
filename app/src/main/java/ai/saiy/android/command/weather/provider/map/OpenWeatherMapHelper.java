package ai.saiy.android.command.weather.provider.map;

import android.net.ParseException;
import android.util.Pair;

import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class OpenWeatherMapHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = OpenWeatherMapHelper.class.getSimpleName();
    public static final String CONTENT_TYPE = "Content-Type";
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

    public Pair<Boolean, OpenWeatherMapResponse> execute(ArrayList<String> urls) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "execute");
        }
        final long then = System.nanoTime();
        for (int i = 0; i < urls.size(); i++) {
            try {
                this.httpURLConnection = (HttpURLConnection) new URL(urls.get(i)).openConnection();
                httpURLConnection.setRequestMethod(Constants.HTTP_GET);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestProperty(CONTENT_TYPE, JSON_HEADER_VALUE_ACCEPT);
                httpURLConnection.setRequestProperty(JSON_HEADER_ACCEPT, JSON_HEADER_VALUE_ACCEPT);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.connect();
                int responseCode = httpURLConnection.getResponseCode();
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "responseCode: " + responseCode);
                }
                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "error stream: " + UtilsString.streamToString(httpURLConnection.getErrorStream()));
                    }
                } else {
                    this.response = UtilsString.streamToString(httpURLConnection.getInputStream());
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "response: " + new JSONObject(response).toString(4));
                    }
                    break;
                }
            } catch (MalformedURLException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "MalformedURLException");
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                MyLog.e(CLS_NAME, "JSONException");
                e.printStackTrace();
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
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        if (UtilsString.notNaked(response)) {
            try {
                final OpenWeatherMapResponse openWeatherMapResponse = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().fromJson(response, OpenWeatherMapResponse.class);
                return openWeatherMapResponse != null ? new Pair<>(true, openWeatherMapResponse) : new Pair<>(false, null);
            } catch (JsonSyntaxException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "JsonSyntaxException");
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
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "response: failed");
            }
        }
        return new Pair<>(false, null);
    }
}
