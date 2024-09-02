package ai.saiy.android.custom;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import ai.saiy.android.database.DBCustomReplacement;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class CustomReplacementHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = CustomReplacementHelper.class.getSimpleName();

    private static final Object lock = new Object();

    /**
     * Insert a new {@link CustomReplacement} in the {@link DBCustomReplacement} synchronising with a basic
     * lock object in a vain attempt to prevent concurrency issues.
     *
     * @param context           the application context
     * @param customReplacement to be set
     * @param rowId             the row id of the command to be replaced
     * @return a {@link Pair}, which #first field will be if the insertion was successful
     */
    public static Pair<Boolean, Long> setReplacement(Context context, CustomReplacement customReplacement, SupportedLanguage supportedLanguage, long rowId) {
        synchronized (lock) {
            final String gsonString = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().toJson(customReplacement);
            final DBCustomReplacement dbCustomReplacement = new DBCustomReplacement(context);
            final Pair<Boolean, Long> duplicatePair = rowId > -1 ? new Pair<>(true, rowId) : replacementExists(context, dbCustomReplacement, customReplacement, supportedLanguage);

            return dbCustomReplacement.insertPopulatedRow(customReplacement.getKeyphrase(), customReplacement.getReplacement(), gsonString, duplicatePair.first, duplicatePair.second);
        }
    }

    private static Pair<Boolean, Long> replacementExists(Context context, DBCustomReplacement dbCustomReplacement, CustomReplacement customReplacement, SupportedLanguage supportedLanguage) {
        if (dbCustomReplacement.databaseExists()) {
            final ArrayList<CustomReplacement> customReplacementArray = dbCustomReplacement.getReplacements();
            if (ai.saiy.android.utils.UtilsList.notNaked(customReplacementArray)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "have replacements");
                }
                if (supportedLanguage == null) {
                    supportedLanguage = SupportedLanguage.getSupportedLanguage(ai.saiy.android.utils.SPH.getVRLocale(context));
                }
                final Locale locale = supportedLanguage.getLocale();
                for (CustomReplacement cr : customReplacementArray) {
                    if (cr.getKeyphrase().toLowerCase(locale).trim().matches(customReplacement.getKeyphrase().toLowerCase(locale).trim())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "keyphrase matched: " + cr.getKeyphrase() + " ~ " + customReplacement.getKeyphrase());
                        }
                        return new Pair<>(true, cr.getRowId());
                    }
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "replacement is not a duplicate");
                }
            } else if (DEBUG) {
                MyLog.i(CLS_NAME, "no replacements");
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "databaseExists: false");
        }
        return new Pair<>(false, -1L);
    }

    public static void deleteCustomReplacement(Context context, long rowId) {
        synchronized (lock) {
            new ai.saiy.android.database.DBCustomReplacement(context).deleteRow(rowId);
        }
    }

    public ArrayList<CustomReplacement> getCustomReplacements(Context context) {
        ArrayList<CustomReplacement> customReplacementArray;
        synchronized (lock) {
            final DBCustomReplacement dbCustomReplacement = new DBCustomReplacement(context);
            if (dbCustomReplacement.databaseExists()) {
                customReplacementArray = dbCustomReplacement.getReplacements();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "databaseExists: true: with " + customReplacementArray.size() + " replacements.");
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "databaseExists: false");
                }
                customReplacementArray = new ArrayList<>();
            }
        }
        return customReplacementArray;
    }

    public @NonNull ArrayList<String> replace(@NonNull ArrayList<String> voiceData, ArrayList<CustomReplacement> customReplacementArray) {
        final long then = System.nanoTime();
        if (ai.saiy.android.utils.UtilsList.notNaked(customReplacementArray)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "have replacements: " + customReplacementArray.size());
            }
            for (int i = 0; i < voiceData.size(); ++i) {
                for (CustomReplacement customReplacement : customReplacementArray) {
                    voiceData.set(i, voiceData.get(i).replaceAll("(?i)" + Pattern.quote(customReplacement.getKeyphrase()), customReplacement.getReplacement()));
                }
            }
            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, then);
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "no custom replacements");
            MyLog.getElapsed(CLS_NAME, then);
        }
        return voiceData;
    }
}
