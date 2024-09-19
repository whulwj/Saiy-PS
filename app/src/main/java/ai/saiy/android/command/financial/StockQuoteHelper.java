package ai.saiy.android.command.financial;

import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import android.net.ParseException;
import android.util.Pair;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class StockQuoteHelper {
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String JSON_HEADER_ACCEPT = "accept";
    private static final String JSON_HEADER_VALUE_ACCEPT = "application/json";
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = StockQuoteHelper.class.getSimpleName();

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

    public Pair<String, String> getStockQuoteIEX(String name, String symbol) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getStockQuoteIEX");
        }
        final long then = System.nanoTime();
        this.response = null;
        try {
            this.httpURLConnection = (HttpsURLConnection) new URL("https://api.iextrading.com/1.0/stock/" + symbol + "/price").openConnection();
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
                this.response = UtilsString.streamToString(httpURLConnection.getInputStream());
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getStockQuoteIEX: response: " + response);
                }
            } else if (DEBUG) {
                MyLog.e(CLS_NAME, "error stream: " + UtilsString.streamToString(httpURLConnection.getErrorStream()));
            }
        } catch (MalformedURLException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "getStockQuoteIEX: MalformedURLException");
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getStockQuoteIEX: UnsupportedEncodingException");
                e.printStackTrace();
            }
        } catch (ParseException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getStockQuoteIEX: ParseException");
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getStockQuoteIEX: UnknownHostException");
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "getStockQuoteIEX: IOException");
                e.printStackTrace();
            }
        } catch (IllegalStateException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "getStockQuoteIEX: IllegalStateException");
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getStockQuoteIEX: NullPointerException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getStockQuoteIEX: Exception");
                e.printStackTrace();
            }
        } finally {
            disconnect();
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        if (UtilsString.notNaked(response)) {
            return new Pair<>(name, response);
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "response: failed");
        }
        return new Pair<>(null, null);
    }

    public Pair<String, String> getSymbol(ArrayList<String> urls) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getSymbol");
        }
        final long then = System.nanoTime();
        for (int i = 0; i < urls.size();) {
            try {
                this.httpURLConnection = (HttpURLConnection) new URL(urls.get(i)).openConnection();
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
                    this.response = org.apache.commons.lang3.StringUtils.removeEnd(UtilsString.streamToString(httpURLConnection.getInputStream()).replaceFirst("YAHOO.Finance.SymbolSuggest.ssCallback\\(", "").trim(), "\\(\\;").trim();
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "response: " + new JSONObject(response).toString(4));
                    }
                    break;
                } else {
                    if (DEBUG) {
                        MyLog.e(CLS_NAME, "error stream: " + UtilsString.streamToString(httpURLConnection.getErrorStream()));
                    }
                    i++;
                }
            } catch (MalformedURLException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "MalformedURLException");
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                MyLog.e(CLS_NAME, "JSONException");
                e.printStackTrace();
            } catch (UnsupportedEncodingException e3) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "UnsupportedEncodingException");
                    e3.printStackTrace();
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
            return a(response);
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "response: failed");
        }
        return new Pair<>(null, null);
    }

    private Pair<String, String> a(String str) {
        final JsonReader jsonReader = new JsonReader(new StringReader(str));
        jsonReader.setLenient(true);
        final StockQuoteResponse stockQuoteResponse = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().fromJson(jsonReader, StockQuoteResponse.class);
        if (stockQuoteResponse != null) {
            try {
                final Company company = stockQuoteResponse.getResultSet().getResult().get(0);
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "getCompany: " + company.getName());
                    MyLog.d(CLS_NAME, "getSymbol: " + company.getSymbol());
                }
                return new Pair<>(company.getName(), company.getSymbol());
            } catch (JsonIOException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "JsonIOException");
                    e.printStackTrace();
                }
            } catch (JsonSyntaxException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "JsonSyntaxException");
                    e.printStackTrace();
                }
            } catch (IndexOutOfBoundsException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "IndexOutOfBoundsException");
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
        }
        return new Pair<>(null, null);
    }
}
