package ai.saiy.android.command.weather.provider.online;

import android.net.ParseException;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import ai.saiy.android.command.time.online.model.TimeResponse;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class WeatherOnlineHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = WeatherOnlineHelper.class.getSimpleName();
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String JSON_HEADER_ACCEPT = "accept";
    private static final String JSON_HEADER_VALUE_ACCEPT = "application/json";

    private String response;
    private HttpsURLConnection httpsURLConnection;

    private void disconnect() {
        if (httpsURLConnection != null) {
            try {
                httpsURLConnection.disconnect();
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Pair<Boolean, WeatherOnlineResponse> execute(ArrayList<String> urls) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "execute");
        }
        final long then = System.nanoTime();
        for (int i = 0; i < urls.size(); i++) {
            try {
                this.httpsURLConnection = (HttpsURLConnection) new URL(urls.get(i)).openConnection();
                httpsURLConnection.setRequestMethod(Constants.HTTP_GET);
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setRequestProperty(CONTENT_TYPE, JSON_HEADER_VALUE_ACCEPT);
                httpsURLConnection.setRequestProperty(JSON_HEADER_ACCEPT, JSON_HEADER_VALUE_ACCEPT);
                httpsURLConnection.connect();
                int responseCode = httpsURLConnection.getResponseCode();
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "responseCode: " + responseCode);
                }
                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "error stream: " + UtilsString.streamToString(httpsURLConnection.getErrorStream()));
                    }
                } else {
                    this.response = UtilsString.streamToString(httpsURLConnection.getInputStream());
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
            final WeatherOnlineResponse weatherOnlineResponse = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().fromJson(response, WeatherOnlineResponse.class);
            return weatherOnlineResponse != null ? new Pair<>(true, weatherOnlineResponse) : new Pair<>(false, null);
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "response: failed");
        }
        return new Pair<>(false, null);
    }

    public Pair<Boolean, TimeResponse> getTimeResponse(ArrayList<String> urls) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "execute");
        }
        final long then = System.nanoTime();
        for (int i = 0; i < urls.size();) {
            try {
                this.httpsURLConnection = (HttpsURLConnection) new URL(urls.get(i)).openConnection();
                httpsURLConnection.setRequestMethod(Constants.HTTP_GET);
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setRequestProperty(CONTENT_TYPE, JSON_HEADER_VALUE_ACCEPT);
                httpsURLConnection.setRequestProperty(JSON_HEADER_ACCEPT, JSON_HEADER_VALUE_ACCEPT);
                httpsURLConnection.connect();
                int responseCode = httpsURLConnection.getResponseCode();
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "responseCode: " + responseCode);
                }
                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "error stream: " + UtilsString.streamToString(httpsURLConnection.getErrorStream()));
                    }
                    i++;
                } else {
                    response = UtilsString.streamToString(httpsURLConnection.getInputStream());
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "response: " + new JSONObject(response).toString(4));
                    }
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
            final TimeResponse timeResponse = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().fromJson(response, TimeResponse.class);
            return timeResponse != null ? new Pair<>(true, timeResponse) : new Pair<>(false, null);
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "response: failed");
        }
        return new Pair<>(false, null);
    }
}
