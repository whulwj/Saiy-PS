package ai.saiy.android.command.financial;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;
import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;
import java.util.ArrayList;
import java.util.Locale;

public class StockQuote_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = StockQuote_en.class.getSimpleName();

    private static String stock_price;
    private static String stock_quote;
    private static String share_price;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public StockQuote_en(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (stock_price == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static @NonNull ArrayList<String> sortQuery(@NonNull Context context, @NonNull ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final ArrayList<String> toReturn = new ArrayList<>();
        final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
        if (stock_price == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        final String word_for = sr.getString(R.string.word_for);
        final String of = sr.getString(R.string.of);
        for (String voiceDatum : voiceData) {
            String vdLower = voiceDatum.toLowerCase(locale).trim();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "examining: " + vdLower);
            }
            String[] split = null;
            if (vdLower.contains(stock_price + XMLResultsHandler.SEP_SPACE)) {
                split = vdLower.split(stock_price + XMLResultsHandler.SEP_SPACE);
            } else if (vdLower.contains(stock_quote + XMLResultsHandler.SEP_SPACE)) {
                split = vdLower.split(stock_quote + XMLResultsHandler.SEP_SPACE);
            } else if (vdLower.contains(share_price + XMLResultsHandler.SEP_SPACE)) {
                split = vdLower.split(share_price + XMLResultsHandler.SEP_SPACE);
            }

            if (split != null && split.length > 1) {
                String query = split[1].trim();
                if (query.matches(".*\\b" + word_for + "\\b.*")) {
                    split = query.split("\\b" + word_for + "\\b", 2);
                    if (split.length > 1 && UtilsString.notNaked(split[1].trim())) {
                        toReturn.add(split[1].trim());
                    }
                } else if (query.matches(".*\\b" + of + "\\b.*")) {
                    split = query.split("\\b" + of + "\\b", 2);
                    if (split.length > 1 && UtilsString.notNaked(split[1].trim())) {
                        toReturn.add(split[1].trim());
                    }
                } else {
                    toReturn.add(query);
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }

    private static void initStrings(@NonNull ai.saiy.android.localisation.SaiyResources sr) {
        stock_price = sr.getString(R.string.stock_price);
        stock_quote = sr.getString(R.string.stock_quote);
        share_price = sr.getString(R.string.share_price);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.contains(stock_price) || vdLower.contains(stock_quote) || vdLower.contains(share_price)) {
                    toReturn.add(new Pair<>(CC.COMMAND_STOCK_QUOTE, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stock quote: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
