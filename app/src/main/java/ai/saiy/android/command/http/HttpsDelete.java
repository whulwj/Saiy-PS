package ai.saiy.android.command.http;

import android.net.ParseException;
import android.util.Pair;

import org.checkerframework.checker.lock.qual.NewObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class HttpsDelete {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = HttpsDelete.class.getSimpleName();

    private HttpsURLConnection httpsURLConnection;
    private Object response = null;

    private void disconnect() {
        if (httpsURLConnection != null) {
            try {
                httpsURLConnection.disconnect();
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "Exception, " + e.getMessage());
                }
            }
        }
    }

    public @NewObject Pair<Boolean, Object> process(@NewObject CustomHttp customHttp) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "process");
            MyLog.i(CLS_NAME, "url:" + customHttp.getUrlString());
        }
        final long then = System.nanoTime();
        boolean isSuccessful = false;
        try {
            this.httpsURLConnection = (HttpsURLConnection) new URL(customHttp.getUrlString()).openConnection();
            httpsURLConnection.setRequestMethod(Constants.HTTP_DELETE);
            httpsURLConnection.setInstanceFollowRedirects(true);
            httpsURLConnection.setConnectTimeout(7000);
            httpsURLConnection.setReadTimeout(7000);
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.connect();
            int responseCode = httpsURLConnection.getResponseCode();
            if (DEBUG) {
                MyLog.d(CLS_NAME, "responseCode: " + responseCode);
            }
            if (responseCode == HttpsURLConnection.HTTP_OK || responseCode == HttpsURLConnection.HTTP_MOVED_PERM) {
                switch (customHttp.getOutputType()) {
                    case CustomHttp.OUTPUT_TYPE_STRING:
                        this.response = UtilsString.streamToString(httpsURLConnection.getInputStream());
                        break;
                    case CustomHttp.OUTPUT_TYPE_BYTE_ARRAY:
                        this.response = com.google.common.io.ByteStreams.toByteArray(httpsURLConnection.getInputStream());
                        break;
                    default:
                        this.response = null;
                        break;
                }
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "response: " + response);
                }
                isSuccessful = true;
            } else {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "error stream: " + UtilsString.streamToString(httpsURLConnection.getErrorStream()));
                }
                this.response = UtilsString.streamToString(httpsURLConnection.getErrorStream());
            }
        } catch (MalformedURLException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "MalformedURLException, " + e.getMessage());
            }
        } catch (UnsupportedEncodingException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "UnsupportedEncodingException, " + e.getMessage());
            }
        } catch (ParseException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ParseException, " + e.getMessage());
            }
        } catch (UnknownHostException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "UnknownHostException, " + e.getMessage());
            }
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "IOException, " + e.getMessage());
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "IllegalStateException, " + e.getMessage());
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException, " + e.getMessage());
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception, " + e.getMessage());
            }
        } finally {
            disconnect();
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return new Pair<>(isSuccessful, response);
    }
}
