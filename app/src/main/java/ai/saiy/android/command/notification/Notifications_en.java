package ai.saiy.android.command.notification;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.OnOff;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public final class Notifications_en {
    private static String notification;
    private static String announc_sic;
    private static OnOff onOff;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Notifications_en.class.getSimpleName();
    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Notifications_en(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (onOff == null || notification == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    private static void initStrings(@NonNull ai.saiy.android.localisation.SaiyResources sr) {
        notification = sr.getString(R.string.notification);
        announc_sic = sr.getString(R.string.announc_sic);
        onOff = new OnOff(sr);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final OnOff.Result result = onOff.resolve(voiceData, sl);
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.contains(notification) && (vdLower.contains(announc_sic) || result == OnOff.Result.ON || result == OnOff.Result.OFF || result == OnOff.Result.TOGGLE)) {
                    toReturn.add(new Pair<>(CC.COMMAND_NOTIFICATIONS, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "notifications: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
