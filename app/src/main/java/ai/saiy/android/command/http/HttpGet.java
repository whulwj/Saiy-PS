package ai.saiy.android.command.http;

import android.net.ParseException;
import android.util.Base64;
import android.util.Pair;

import org.checkerframework.checker.lock.qual.NewObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class HttpGet {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = HttpGet.class.getSimpleName();

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
        boolean isSuccessful = false;
        if (DEBUG) {
            MyLog.i(CLS_NAME, "process");
            MyLog.i(CLS_NAME, "url:" + customHttp.getUrlString());
        }
        final long then = System.nanoTime();
        int i = 0;
        try {
            URL url;
            URL currentUrl = new URL(customHttp.getUrlString());
            while (!isSuccessful && i <= 2) {
                this.httpURLConnection = (HttpURLConnection) currentUrl.openConnection();
                httpURLConnection.setRequestMethod(Constants.HTTP_GET);
                httpURLConnection.setConnectTimeout(7000);
                httpURLConnection.setReadTimeout(7000);
                httpURLConnection.setDoInput(true);
                String userInfo = currentUrl.getUserInfo();
                if (UtilsString.notNaked(userInfo)) {
                    String decode = URLDecoder.decode(userInfo, Constants.ENCODING_UTF8);
                    if (UtilsString.notNaked(decode)) {
                        String encodeToString = Base64.encodeToString(decode.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
                        if (UtilsString.notNaked(encodeToString)) {
                            httpURLConnection.setRequestProperty("Authorization", "Basic " + encodeToString);
                        }
                    }
                }
                httpURLConnection.connect();
                httpURLConnection.setInstanceFollowRedirects(true);
                int responseCode = httpURLConnection.getResponseCode();
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "responseCode: " + responseCode);
                }
                switch (responseCode) {
                    case HttpsURLConnection.HTTP_OK:
                        switch (customHttp.getOutputType()) {
                            case CustomHttp.OUTPUT_TYPE_STRING:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "CustomHttp.OUTPUT_TYPE_STRING");
                                }
                                this.response = UtilsString.streamToString(httpURLConnection.getInputStream());
                                break;
                            case CustomHttp.OUTPUT_TYPE_BYTE_ARRAY:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "CustomHttp.OUTPUT_TYPE_BYTE_ARRAY");
                                }
                                this.response = com.google.common.io.ByteStreams.toByteArray(httpURLConnection.getInputStream());
                                break;
                            default:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "CustomHttp.OUTPUT_TYPE_NONE");
                                }
                                this.response = null;
                                break;
                        }
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "response: " + response);
                        }
                        url = currentUrl;
                        isSuccessful = true;
                        break;
                    case HttpsURLConnection.HTTP_MOVED_PERM:
                    case HttpsURLConnection.HTTP_MOVED_TEMP:
                        String headerField = httpURLConnection.getHeaderField("Location");
                        ++i;
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "location: " + headerField);
                            MyLog.d(CLS_NAME, "redirectCount: " + i);
                        }
                        url = new URL(currentUrl, headerField);
                        break;
                    default:
                        if (DEBUG) {
                            MyLog.e(CLS_NAME, "error stream: " + UtilsString.streamToString(httpURLConnection.getErrorStream()));
                        }
                        this.response = UtilsString.streamToString(httpURLConnection.getErrorStream());
                        url = currentUrl;
                        break;
                }
                currentUrl = url;
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
            ai.saiy.android.utils.MyLog.getElapsed(CLS_NAME, then);
        }
        return new Pair<>(isSuccessful, response);
    }
}
