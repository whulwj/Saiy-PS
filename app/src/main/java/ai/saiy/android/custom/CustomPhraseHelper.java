package ai.saiy.android.custom;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.database.DBCustomPhrase;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;

public class CustomPhraseHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = CustomPhraseHelper.class.getSimpleName();

    private static final Object lock = new Object();

    /**
     * Insert a new {@link CustomPhrase} in the {@link DBCustomPhrase} synchronising with a basic
     * lock object in a vain attempt to prevent concurrency issues.
     *
     * @param context      the application context
     * @param customPhrase to be set
     * @param rowId        the row id of the command to be replaced
     * @return a {@link Pair}, which #first field will be if the insertion was successful
     */
    public static Pair<Boolean, Long> setPhrase(Context context, CustomPhrase customPhrase, SupportedLanguage supportedLanguage, long rowId) {
        synchronized (lock) {
            final String gsonString = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().toJson(customPhrase);
            final DBCustomPhrase dbCustomPhrase = new DBCustomPhrase(context);
            final Pair<Boolean, Long> duplicatePair = rowId > -1 ? new Pair<>(true, rowId) : phraseExists(context, dbCustomPhrase, customPhrase, supportedLanguage);

            return dbCustomPhrase.insertPopulatedRow(customPhrase.getKeyphrase(), customPhrase.getResponse(), customPhrase.getStartVoiceRecognition(), gsonString, duplicatePair.first, duplicatePair.second);
        }
    }

    private static Pair<Boolean, Long> phraseExists(Context context, DBCustomPhrase dbCustomPhrase, CustomPhrase customPhrase, SupportedLanguage supportedLanguage) {
        if (dbCustomPhrase.databaseExists()) {
            final ArrayList<CustomPhrase> customPhraseArray = dbCustomPhrase.getPhrases();
            if (ai.saiy.android.utils.UtilsList.notNaked(customPhraseArray)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "have phrases");
                }
                if (supportedLanguage == null) {
                    supportedLanguage = SupportedLanguage.getSupportedLanguage(ai.saiy.android.utils.SPH.getVRLocale(context));
                }
                final Locale locale = supportedLanguage.getLocale();
                for (CustomPhrase cp : customPhraseArray) {
                    if (cp.getKeyphrase().toLowerCase(locale).trim().matches(customPhrase.getKeyphrase().toLowerCase(locale).trim())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "keyphrase matched: " + cp.getKeyphrase() + " ~ " + customPhrase.getKeyphrase());
                        }
                        return new Pair<>(true, cp.getRowId());
                    }
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "phrase is not a duplicate");
                }
            } else if (DEBUG) {
                MyLog.i(CLS_NAME, "no phrases");
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "databaseExists: false");
        }
        return new Pair<>(false, -1L);
    }

    public static void deleteCustomPhrase(Context context, long rowId) {
        synchronized (lock) {
            new ai.saiy.android.database.DBCustomPhrase(context).deleteRow(rowId);
        }
    }

    public Pair<Boolean, CustomCommand> haveCustomPhrase(Context context, ai.saiy.android.command.helper.CommandRequest cr, ArrayList<String> voiceData, ArrayList<CustomPhrase> customPhraseArray) {
        final long then = System.nanoTime();
        if (!ai.saiy.android.utils.UtilsList.notNaked(customPhraseArray)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "no custom phrases");
                MyLog.getElapsed(CLS_NAME, then);
            }
            return new Pair<>(false, null);
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "have phrases: " + customPhraseArray.size());
        }

        for (int i = 0; i < voiceData.size(); ++i) {
            for (CustomPhrase customPhrase : customPhraseArray) {
                if (voiceData.get(i).matches("(?i)" + Pattern.quote(customPhrase.getKeyphrase()))) {
                    return new Pair<>(true, new CustomCommand(CCC.CUSTOM_SPEECH, CC.COMMAND_USER_CUSTOM, customPhrase.getKeyphrase(), customPhrase.getResponse(), "", cr.getTTSLocale(context).toString(), cr.getVRLocale(context).toString(), customPhrase.getStartVoiceRecognition() ? LocalRequest.ACTION_SPEAK_LISTEN : LocalRequest.ACTION_SPEAK_ONLY));
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return new Pair<>(false, null);
    }

    public ArrayList<CustomPhrase> getCustomPhrases(Context context) {
        ArrayList<CustomPhrase> customPhraseArray;
        synchronized (lock) {
            final DBCustomPhrase dbCustomPhrase = new DBCustomPhrase(context);
            if (dbCustomPhrase.databaseExists()) {
                customPhraseArray = dbCustomPhrase.getPhrases();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "databaseExists: true: with " + customPhraseArray.size() + " phrases.");
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "databaseExists: false");
                }
                customPhraseArray = new ArrayList<>();
            }
        }
        return customPhraseArray;
    }
}
