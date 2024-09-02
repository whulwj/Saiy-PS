package ai.saiy.android.custom;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import ai.saiy.android.database.DBCustomNickname;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class CustomNicknameHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = CustomNicknameHelper.class.getSimpleName();

    private static final Object lock = new Object();

    /**
     * Insert a new {@link CustomNickname} in the {@link DBCustomNickname} synchronising with a basic
     * lock object in a vain attempt to prevent concurrency issues.
     *
     * @param context           the application context
     * @param customNickname to be set
     * @param rowId          the row id of the command to be replaced
     * @return a {@link Pair}, which #first field will be true if the insertion was successful
     */
    public static Pair<Boolean, Long> setNickname(@NonNull Context context, @NonNull CustomNickname customNickname, @Nullable SupportedLanguage supportedLanguage, long rowId) {
        synchronized (lock) {
            final String gsonString = new com.google.gson.GsonBuilder().disableHtmlEscaping().create().toJson(customNickname);
            final DBCustomNickname dbCustomNickname = new DBCustomNickname(context);
            final Pair<Boolean, Long> duplicatePair = rowId > -1 ? new Pair<>(true, rowId) : nickNameExists(context, dbCustomNickname, customNickname, supportedLanguage);

            return dbCustomNickname.insertPopulatedRow(customNickname.getNickname(), customNickname.getContactName(), gsonString, duplicatePair.first, duplicatePair.second);
        }
    }

    private static Pair<Boolean, Long> nickNameExists(@NonNull Context context, @NonNull DBCustomNickname dbCustomNickname, @NonNull CustomNickname customNickname, @Nullable SupportedLanguage supportedLanguage) {
        if (dbCustomNickname.databaseExists()) {
            final ArrayList<CustomNickname> customNicknameArray = dbCustomNickname.getNicknames();
            if (ai.saiy.android.utils.UtilsList.notNaked(customNicknameArray)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "have showContactPicker");
                }
                if (supportedLanguage == null) {
                    supportedLanguage = SupportedLanguage.getSupportedLanguage(ai.saiy.android.utils.SPH.getVRLocale(context));
                }
                final Locale locale = supportedLanguage.getLocale();
                for (CustomNickname cn : customNicknameArray) {
                    if (cn.getNickname().toLowerCase(locale).trim().matches(customNickname.getNickname().toLowerCase(locale).trim())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "nickname matched: " + cn.getNickname() + " ~ " + customNickname.getNickname());
                        }
                        return new Pair<>(true, cn.getRowId());
                    }
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "nickname is not a duplicate");
                }
            } else if (DEBUG) {
                MyLog.i(CLS_NAME, "no showContactPicker");
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "databaseExists: false");
        }
        return new Pair<>(false, -1L);
    }

    public static void deleteCustomNickname(Context context, long rowId) {
        synchronized (lock) {
            new ai.saiy.android.database.DBCustomNickname(context).deleteRow(rowId);
        }
    }

    /**
     * Extract all of the user's {@link CustomNickname} from {@link DBCustomNickname}
     *
     * @param context the application context
     * @return a list of {@link CustomNickname}
     */
    public ArrayList<CustomNickname> getCustomNicknames(Context context) {
        ArrayList<CustomNickname> customNicknameArray;
        synchronized (lock) {
            DBCustomNickname dbCustomNickname = new DBCustomNickname(context);
            if (dbCustomNickname.databaseExists()) {
                customNicknameArray = dbCustomNickname.getNicknames();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "databaseExists: true: with " + customNicknameArray.size() + " showContactPicker");
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "databaseExists: false");
                }
                customNicknameArray = new ArrayList<>();
            }
        }
        return customNicknameArray;
    }

    public @NonNull ArrayList<String> replace(@NonNull ArrayList<String> voiceData, ArrayList<CustomNickname> customNicknameArray) {
        final long then = System.nanoTime();
        if (ai.saiy.android.utils.UtilsList.notNaked(customNicknameArray)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "have showContactPicker: " + customNicknameArray.size());
            }
            for (int i = 0; i < voiceData.size(); ++i) {
                for (CustomNickname customNickname : customNicknameArray) {
                    voiceData.set(i, voiceData.get(i).replaceAll("(?i)" + Pattern.quote(customNickname.getNickname()), customNickname.getContactName()));
                }
            }
            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, then);
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "no custom showContactPicker");
            MyLog.getElapsed(CLS_NAME, then);
        }
        return voiceData;
    }
}
