package ai.saiy.android.command.web;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import org.apache.commons.validator.routines.UrlValidator;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

public final class Web_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Web_en.class.getSimpleName();

    private static String go_to;
    private static Pattern pGo_to;
    private static Pattern pWww;
    private static Pattern pCom;
    private static Pattern pCo;
    private static Pattern pNet;
    private static Pattern pOrg;
    private static Pattern pOrgUk;
    private static Pattern pInfo;
    private static Pattern pMobi;
    private static Pattern pBiz;
    private static Pattern pXxx;
    private static Pattern pTv;
    private static Pattern pTheWebsite;
    private static Pattern pTheWeb_site;
    private static Pattern pWebsite;
    private static Pattern pWeb_site;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Web_en(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (go_to == null || pTv == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static ArrayList<String> getUrls(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final ArrayList<String> toReturn = new ArrayList<>();
        if (go_to == null || pTv == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
            sr.reset();
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        final UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
        for (String voiceDatum : voiceData) {
            String vdLower = voiceDatum.toLowerCase(locale).trim();
            if (pGo_to.matcher(vdLower).find()) {
                String url = pTv.matcher(pXxx.matcher(pBiz.matcher(pMobi.matcher(pInfo.matcher(pOrgUk.matcher(pOrg.matcher(pNet.matcher(pCo.matcher(pCom.matcher(pWww.matcher(pWeb_site.matcher(pWebsite.matcher(pTheWeb_site.matcher(pTheWebsite.matcher(pGo_to.matcher(vdLower).replaceFirst("")).replaceFirst("")).replaceFirst("")).replaceFirst("")).replaceFirst("")).replaceFirst("www.")).replaceFirst(".com")).replaceFirst(".co.uk")).replaceFirst(".net")).replaceFirst(".org")).replaceFirst(".org.uk")).replaceFirst(".info")).replaceFirst(".mobi")).replaceFirst(".biz")).replaceFirst(".xxx")).replaceFirst(".tv");
                if (url.endsWith(" ai")) {
                    url = UtilsString.replaceLast(url, " ai", ".ai");
                }
                if (url.endsWith(" co")) {
                    url = UtilsString.replaceLast(url, " co", ".co");
                }
                if (url.endsWith(" comm")) {
                    url = UtilsString.replaceLast(url, " comm", ".com");
                }
                if (url.endsWith(" calm")) {
                    url = UtilsString.replaceLast(url, " calm", ".com");
                }
                if (url.endsWith(" con")) {
                    url = UtilsString.replaceLast(url, " con", ".com");
                }
                if (url.startsWith("www\\.")) {
                    url = "http://" + url;
                } else if (!url.startsWith("http://www.") && !url.startsWith("https://www.")) {
                    url = "http://www." + url;
                }
                if (!url.endsWith(" a") && !url.endsWith(" i")) {
                    url = url.replaceAll("\\s", "").replaceAll("([.])+", "$1");
                    if (url.matches("http://www.say.ai")) {
                        url = "http://www.saiy.ai";
                    }
                    if (urlValidator.isValid(url)) {
                        if (!toReturn.contains(url)) {
                            toReturn.add(url);
                        }
                    } else if (DEBUG) {
                        MyLog.i(CLS_NAME, "invalid url: " + url);
                    }
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        final String website = sr.getString(R.string.website);
        final String web_site = sr.getString(R.string.web_site);
        final String the = sr.getString(R.string.the);
        go_to = sr.getString(R.string.go_to);
        pWww = Pattern.compile("^www\\s");
        pCom = Pattern.compile("\\b\\scom\\b");
        pCo = Pattern.compile("\\b\\sco\\suk\\b");
        pNet = Pattern.compile("\\b\\snet\\b");
        pOrg = Pattern.compile("\\b\\sorg\\b");
        pOrgUk = Pattern.compile("\\b\\sorg\\suk\\b");
        pInfo = Pattern.compile("\\b\\sinfo\\b");
        pMobi = Pattern.compile("\\b\\smobi\\b");
        pBiz = Pattern.compile("\\b\\sbiz\\b");
        pXxx = Pattern.compile("\\b\\sxxx\\b");
        pTv = Pattern.compile("\\b\\stv\\b");
        pTheWebsite = Pattern.compile(the + "\\s" + website);
        pTheWeb_site = Pattern.compile(the + "\\s" + web_site);
        pWebsite = Pattern.compile(website);
        pWeb_site = Pattern.compile(web_site);
        pGo_to = Pattern.compile("^" + go_to + "\\s");
    }

    public static String getSearchTerm(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        if (go_to == null || pTv == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
            sr.reset();
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        String searchTerm = null;
        for (String voiceDatum : voiceData) {
            String vdLower = voiceDatum.toLowerCase(locale).trim();
            if (pGo_to.matcher(vdLower).find()) {
                String url = pTv.matcher(pXxx.matcher(pBiz.matcher(pMobi.matcher(pInfo.matcher(pOrgUk.matcher(pOrg.matcher(pNet.matcher(pCo.matcher(pCom.matcher(pWww.matcher(pWeb_site.matcher(pWebsite.matcher(pTheWeb_site.matcher(pTheWebsite.matcher(pGo_to.matcher(vdLower).replaceFirst("")).replaceFirst("")).replaceFirst("")).replaceFirst("")).replaceFirst("")).replaceFirst("www.")).replaceFirst(".com")).replaceFirst(".co.uk")).replaceFirst(".net")).replaceFirst(".org")).replaceFirst(".org.uk")).replaceFirst(".info")).replaceFirst(".mobi")).replaceFirst(".biz")).replaceFirst(".xxx")).replaceFirst(".tv");
                if (url.endsWith(" ai")) {
                    url = UtilsString.replaceLast(url, " ai", ".ai");
                }
                if (url.endsWith(" co")) {
                    url = UtilsString.replaceLast(url, " co", ".co");
                }
                if (url.endsWith(" comm")) {
                    url = UtilsString.replaceLast(url, " comm", ".com");
                }
                if (url.endsWith(" calm")) {
                    url = UtilsString.replaceLast(url, " calm", ".com");
                }
                if (url.endsWith(" con")) {
                    url = UtilsString.replaceLast(url, " con", ".com");
                }
                if (url.startsWith("www\\.")) {
                    url = "http://" + url;
                } else if (!url.startsWith("http\\:\\/\\/www\\.")) {
                    url = "http://www." + url;
                }
                if (!url.endsWith(" a") && !url.endsWith(" i")) {
                    url = url.replaceAll("\\s", "").replaceAll("([.])+", "$1");
                    if (url.matches("http://www.say.ai")) {
                        url = "http://www.saiy.ai";
                    }
                    final String publicSuffix = UtilsString.getPublicSuffix(url);
                    url = url.replaceFirst("http://www.", "");
                    searchTerm = UtilsString.notNaked(publicSuffix) ? UtilsString.replaceLast(url, "." + publicSuffix, "") : url;
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return searchTerm;
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (pGo_to.matcher(vdLower).find()) {
                    toReturn.add(new Pair<>(CC.COMMAND_WEB, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "web address: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
