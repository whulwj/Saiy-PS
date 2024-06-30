package ai.saiy.android.command.horoscope;

import android.content.Context;
import android.net.ParseException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

public class HoroscopeHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = HoroscopeHelper.class.getSimpleName();
    private static final String DESCRIPTION = "description";

    private String description;
    private HttpURLConnection urlConnection;

    private void closeConnection() {
        if (urlConnection != null) {
            try {
                urlConnection.disconnect();
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void calculateHoroscope(Context context, int dayOfMonth, int month, int year) {
        final int calendarMonth = month + 1;
        if ((calendarMonth == 12 && dayOfMonth >= 22 && dayOfMonth <= 31) || (calendarMonth == 1 && dayOfMonth >= 1 && dayOfMonth <= 19)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Capricorn");
            }
            SPH.setHoroscope(context, dayOfMonth, calendarMonth, year, CommandHoroscopeValues.Sign.CAPRICORN);
            return;
        }
        if ((calendarMonth == 1 && dayOfMonth >= 20 && dayOfMonth <= 31) || (calendarMonth == 2 && dayOfMonth >= 1 && dayOfMonth <= 17)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Aquarius");
            }
            SPH.setHoroscope(context, dayOfMonth, calendarMonth, year, CommandHoroscopeValues.Sign.AQUARIUS);
            return;
        }
        if ((calendarMonth == 2 && dayOfMonth >= 18 && dayOfMonth <= 29) || (calendarMonth == 3 && dayOfMonth >= 1 && dayOfMonth <= 19)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Pisces");
            }
            SPH.setHoroscope(context, dayOfMonth, calendarMonth, year, CommandHoroscopeValues.Sign.PISCES);
            return;
        }
        if ((calendarMonth == 3 && dayOfMonth >= 20 && dayOfMonth <= 31) || (calendarMonth == 4 && dayOfMonth >= 1 && dayOfMonth <= 19)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Aries");
            }
            SPH.setHoroscope(context, dayOfMonth, calendarMonth, year, CommandHoroscopeValues.Sign.ARIES);
            return;
        }
        if ((calendarMonth == 4 && dayOfMonth >= 20 && dayOfMonth <= 30) || (calendarMonth == 5 && dayOfMonth >= 1 && dayOfMonth <= 20)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Taurus");
            }
            SPH.setHoroscope(context, dayOfMonth, calendarMonth, year, CommandHoroscopeValues.Sign.TAURUS);
            return;
        }
        if ((calendarMonth == 5 && dayOfMonth >= 21 && dayOfMonth <= 31) || (calendarMonth == 6 && dayOfMonth >= 1 && dayOfMonth <= 20)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Gemini");
            }
            SPH.setHoroscope(context, dayOfMonth, calendarMonth, year, CommandHoroscopeValues.Sign.GEMINI);
            return;
        }
        if ((calendarMonth == 6 && dayOfMonth >= 21 && dayOfMonth <= 30) || (calendarMonth == 7 && dayOfMonth >= 1 && dayOfMonth <= 22)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Cancer");
            }
            SPH.setHoroscope(context, dayOfMonth, calendarMonth, year, CommandHoroscopeValues.Sign.CANCER);
            return;
        }
        if ((calendarMonth == 7 && dayOfMonth >= 23 && dayOfMonth <= 31) || (calendarMonth == 8 && dayOfMonth >= 1 && dayOfMonth <= 22)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Leo");
            }
            SPH.setHoroscope(context, dayOfMonth, calendarMonth, year, CommandHoroscopeValues.Sign.LEO);
            return;
        }
        if ((calendarMonth == 8 && dayOfMonth >= 23 && dayOfMonth <= 31) || (calendarMonth == 9 && dayOfMonth >= 1 && dayOfMonth <= 22)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Virgo");
            }
            SPH.setHoroscope(context, dayOfMonth, calendarMonth, year, CommandHoroscopeValues.Sign.VIRGO);
            return;
        }
        if ((calendarMonth == 9 && dayOfMonth >= 23 && dayOfMonth <= 30) || (calendarMonth == 10 && dayOfMonth >= 1 && dayOfMonth <= 22)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Libra");
            }
            SPH.setHoroscope(context, dayOfMonth, calendarMonth, year, CommandHoroscopeValues.Sign.LIBRA);
            return;
        }
        if ((calendarMonth == 10 && dayOfMonth >= 23 && dayOfMonth <= 31) || (calendarMonth == 11 && dayOfMonth >= 1 && dayOfMonth <= 21)) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Scorpio");
            }
            SPH.setHoroscope(context, dayOfMonth, calendarMonth, year, CommandHoroscopeValues.Sign.SCORPIO);
        } else if ((calendarMonth != 11 || dayOfMonth < 22 || dayOfMonth > 30) && (calendarMonth != 12 || dayOfMonth < 1 || dayOfMonth > 21)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "unknown star sign");
            }
        } else {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "Sagittarius");
            }
            SPH.setHoroscope(context, dayOfMonth, calendarMonth, year, CommandHoroscopeValues.Sign.SAGITTARIUS);
        }
    }

    public String execute(CommandHoroscopeValues.Sign sign) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "execute");
        }
        final long startTime = System.nanoTime();
        try {
            urlConnection = (HttpURLConnection) new URL("http://www.findyourfate.com/rss/dailyhoroscope-feed.php?sign=" + sign.getName() + "&id=45").openConnection();
            urlConnection.setRequestMethod(Constants.HTTP_GET);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            if (DEBUG) {
                MyLog.d(CLS_NAME, "responseCode: " + responseCode);
            }
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                description = UtilsString.streamToString(urlConnection.getInputStream());
                Document parse = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(description)));
                parse.getDocumentElement().normalize();
                description = parse.getElementsByTagName(DESCRIPTION).item(1).getChildNodes().item(0).getNodeValue();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "response: " + description);
                }
            } else if (DEBUG) {
                MyLog.e(CLS_NAME, "error stream: " + UtilsString.streamToString(urlConnection.getErrorStream()));
            }
        } catch (ParseException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ParseException");
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "MalformedURLException");
                e.printStackTrace();
            }
        } catch (ParserConfigurationException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ParserConfigurationException");
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "UnsupportedEncodingException");
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "UnknownHostException");
                e.printStackTrace();
            }
        } catch (SAXException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "SAXException");
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
        } catch (IndexOutOfBoundsException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "IndexOutOfBoundsException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        } finally {
            closeConnection();
        }
        if (UtilsString.notNaked(description)) {
            description = description.replaceAll("`", "'");
            description = description.replaceAll("--", "");
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, startTime);
        }
        return description;
    }
}
