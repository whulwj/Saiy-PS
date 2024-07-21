package ai.saiy.android.localisation;

import java.util.Locale;

import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsLocale;

public class SaiyWebHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = SaiyWebHelper.class.getSimpleName();
    private static final String ENGLISH_UK = "en_GB";
    private static final String ENGLISH_NZ = "en_NZ";
    private static final String ENGLISH_AU = "en_AU";
    private static final String CANADA = "ca";
    private static final String UNITED_KINGDOM = "uk";
    private static final String NEW_ZEALAND = "nz";
    private static final String AUSTRALIA = "au";

    public static final int GOOGLE = 1;
    public static final int YAHOO = 2;

    public static String extension(int searchEngine, SupportedLanguage supportedLanguage) {
        Locale locale = Locale.getDefault();
        if (DEBUG) {
            MyLog.d(CLS_NAME, "defaultLocale: " + locale);
        }
        switch (searchEngine) {
            case GOOGLE:
                if (SupportedLanguage.ENGLISH_US == supportedLanguage) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "extension: com");
                    }
                    return "com";
                } else if (UtilsLocale.localesMatch(Locale.CANADA, locale)) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "extension: ca");
                    }
                    return CANADA;
                } else if (UtilsLocale.localesMatch(new Locale(ENGLISH_NZ), locale)) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "extension: co.nz");
                    }
                    return "co." + NEW_ZEALAND;
                } else if (UtilsLocale.localesMatch(new Locale(ENGLISH_AU), locale)) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "extension: com.au");
                    }
                    return "com." + AUSTRALIA;
                } else if (UtilsLocale.localesMatch(new Locale(ENGLISH_UK), locale)) {
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "extension: co.uk");
                    }
                    return "co." + UNITED_KINGDOM;
                }
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "default: extension: com");
                }
                return "com";
            case YAHOO:
                if (SupportedLanguage.ENGLISH_US == supportedLanguage) {
                    return "search.yahoo.com";
                } else if (UtilsLocale.localesMatch(Locale.CANADA, locale)) {
                    return CANADA + ".search.yahoo.com";
                } else if (UtilsLocale.localesMatch(new Locale(ENGLISH_NZ), locale)) {
                    return NEW_ZEALAND + ".search.yahoo.com";
                } else if (UtilsLocale.localesMatch(new Locale(ENGLISH_AU), locale)) {
                    return AUSTRALIA + ".search.yahoo.com";
                } else if (UtilsLocale.localesMatch(new Locale(ENGLISH_UK), locale)) {
                    return UNITED_KINGDOM + ".search.yahoo.com";
                }
                return "search.yahoo.com";
            case 3:
                if (SupportedLanguage.ENGLISH_US == supportedLanguage) {
                    return "";
                } else if (UtilsLocale.localesMatch(Locale.CANADA, locale)) {
                    return "&cc=" + CANADA;
                } else if (UtilsLocale.localesMatch(new Locale(ENGLISH_NZ), locale)) {
                    return "&cc=" + NEW_ZEALAND;
                } else if (UtilsLocale.localesMatch(new Locale(ENGLISH_AU), locale)) {
                    return "&cc=" + AUSTRALIA;
                } else if (UtilsLocale.localesMatch(new Locale(ENGLISH_UK), locale)) {
                    return "&cc=gb";
                }
                return "";
            default:
                return null;
        }
    }

    public static String yahooImage(SupportedLanguage supportedLanguage) {
        Locale locale = Locale.getDefault();
        if (SupportedLanguage.ENGLISH_US == supportedLanguage) {
            return "images.search.yahoo.com/search/images;_ylu=?p=";
        } else if (UtilsLocale.localesMatch(Locale.CANADA, locale)) {
            return CANADA + ".images.search.yahoo.com/search/images;_ylu=?p=";
        } else if (UtilsLocale.localesMatch(new Locale(ENGLISH_NZ), locale)) {
            return NEW_ZEALAND + ".images.search.yahoo.com/search/images;_ylu=?p=";
        } else if (UtilsLocale.localesMatch(new Locale(ENGLISH_AU), locale)) {
            return AUSTRALIA + ".images.search.yahoo.com/search/images;_ylu=?p=";
        } else if (UtilsLocale.localesMatch(new Locale(ENGLISH_UK), locale)) {
            return UNITED_KINGDOM + ".images.search.yahoo.com/search/images;_ylu=?p=";
        }
        return "images.search.yahoo.com/search/images;_ylu=?p=";
    }

    public static String yahooVideo(SupportedLanguage supportedLanguage) {
        Locale locale = Locale.getDefault();
        if (SupportedLanguage.ENGLISH_US == supportedLanguage) {
            return "video.search.yahoo.com/search/video;_ylu=?p=";
        } else if (UtilsLocale.localesMatch(Locale.CANADA, locale)) {
            return CANADA + ".video.search.yahoo.com/search/video;_ylu=?p=";
        } else if (UtilsLocale.localesMatch(new Locale(ENGLISH_NZ), locale)) {
            return NEW_ZEALAND + ".video.search.yahoo.com/search/video;_ylu=?p=";
        } else if (UtilsLocale.localesMatch(new Locale(ENGLISH_AU), locale)) {
            return AUSTRALIA + ".video.search.yahoo.com/search/video;_ylu=?p=";
        } else if (UtilsLocale.localesMatch(new Locale(ENGLISH_UK), locale)) {
            return UNITED_KINGDOM + ".video.search.yahoo.com/search/video;_ylu=?p=";
        }
        return "video.search.yahoo.com/search/video;_ylu=?p=";
    }
}
