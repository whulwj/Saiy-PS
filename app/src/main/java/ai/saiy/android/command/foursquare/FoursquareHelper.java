package ai.saiy.android.command.foursquare;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
import android.net.Uri;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.gson.JsonSyntaxException;
import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import ai.saiy.android.applications.Installed;
import ai.saiy.android.location.LocalLocation;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

/**
 * <a href="https://docs.foursquare.com/developer/reference/place-search"/>
 */
public class FoursquareHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = FoursquareHelper.class.getSimpleName();
    public static final String CONTENT_TYPE = "Content-Type";
    private static final String JSON_HEADER_ACCEPT = "accept";
    private static final String JSON_HEADER_VALUE_ACCEPT = "application/json";

    private String rawResponse;
    private HttpURLConnection httpURLConnection;

    @SuppressLint({"SimpleDateFormat"})
    private static String time(long timeStamp) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        calendar.setTimeInMillis(timeStamp);
        return simpleDateFormat.format(calendar.getTime());
    }

    private void destroy() {
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

    public static boolean fourSquareCheckInPage(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "fourSquareCheckInPage");
        }
        return ai.saiy.android.applications.UtilsApplication.launchAppFromPackageName(context, Installed.PACKAGE_FOUR_SQUARED);
    }

    public static boolean fourSquareCheckIn(Context context, String id) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "fourSquareCheckIn");
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.foursquare.com/venue/" + id));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "fourSquareCheckIn: ActivityNotFoundException");
                e.printStackTrace();
            }
            return false;
        }
    }

    public Pair<Boolean, VenuesResponse> searchVenues(@NonNull LocalLocation location, String token) {
        final long then = System.nanoTime();
        try {
            this.httpURLConnection = (HttpURLConnection) new URL("https://api.foursquare.com/v2/venues/search?ll=" + (location.getLatitude() + XMLResultsHandler.SEP_COMMA + location.getLongitude()) + "&oauth_token=" + token + "&v=" + time(System.currentTimeMillis())).openConnection();
            httpURLConnection.setRequestMethod(Constants.HTTP_GET);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestProperty(CONTENT_TYPE, JSON_HEADER_VALUE_ACCEPT);
            httpURLConnection.setRequestProperty(JSON_HEADER_ACCEPT, JSON_HEADER_VALUE_ACCEPT);
            httpURLConnection.connect();
            int responseCode = httpURLConnection.getResponseCode();
            if (DEBUG) {
                MyLog.d(CLS_NAME, "responseCode: " + responseCode);
            }
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                this.rawResponse = UtilsString.streamToString(httpURLConnection.getInputStream());
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "response: " + new JSONObject(rawResponse).toString(4));
                }
            } else if (DEBUG) {
                MyLog.e(CLS_NAME, "error stream: " + UtilsString.streamToString(httpURLConnection.getErrorStream()));
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
            destroy();
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        if (UtilsString.notNaked(rawResponse)) {
            try {
                final VenuesResponse venuesResponse = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().fromJson(rawResponse, VenuesResponse.class);
                return venuesResponse != null ? new Pair<>(true, venuesResponse) : new Pair<>(false, null);
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
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "response: failed");
        }
        return new Pair<>(false, null);
    }
}
