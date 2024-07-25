package ai.saiy.android.command.http;

import android.net.ParseException;
import android.util.Pair;

import org.checkerframework.checker.lock.qual.NewObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class HttpDelete {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = HttpDelete.class.getSimpleName();

    private HttpURLConnection httpURLConnection;
    private Object response = null;

    private void disconnect() {
        if (httpURLConnection != null) {
            try {
                httpURLConnection.disconnect();
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
            this.httpURLConnection = (HttpURLConnection) new URL(customHttp.getUrlString()).openConnection();
            httpURLConnection.setRequestMethod(Constants.HTTP_DELETE);
            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.setConnectTimeout(7000);
            httpURLConnection.setReadTimeout(7000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            int responseCode = httpURLConnection.getResponseCode();
            if (DEBUG) {
                MyLog.d(CLS_NAME, "responseCode: " + responseCode);
            }
            if (responseCode == HttpsURLConnection.HTTP_OK || responseCode == HttpsURLConnection.HTTP_MOVED_PERM) {
                switch (customHttp.getOutputType()) {
                    case CustomHttp.OUTPUT_TYPE_STRING:
                        this.response = UtilsString.streamToString(httpURLConnection.getInputStream());
                        break;
                    case CustomHttp.OUTPUT_TYPE_BYTE_ARRAY:
                        this.response = com.google.common.io.ByteStreams.toByteArray(httpURLConnection.getInputStream());
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
                    MyLog.e(CLS_NAME, "error stream: " + UtilsString.streamToString(httpURLConnection.getErrorStream()));
                }
                this.response = UtilsString.streamToString(httpURLConnection.getErrorStream());
            }
            disconnect();
        } catch (MalformedURLException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "MalformedURLException, " + e.getMessage());
            }
            disconnect();
        } catch (UnsupportedEncodingException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "UnsupportedEncodingException, " + e.getMessage());
            }
            disconnect();
        } catch (ParseException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ParseException, " + e.getMessage());
            }
            disconnect();
        } catch (UnknownHostException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "UnknownHostException, " + e.getMessage());
            }
            disconnect();
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "IOException, " + e.getMessage());
            }
            disconnect();
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "IllegalStateException, " + e.getMessage());
            }
            disconnect();
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException, " + e.getMessage());
            }
            disconnect();
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception, " + e.getMessage());
            }
            disconnect();
        } catch (Throwable th) {
            disconnect();
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return new Pair<>(isSuccessful, response);
    }
}
