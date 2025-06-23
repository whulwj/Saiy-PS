package ai.saiy.android.nlu.local;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class OnOff {
    private static Pattern on;
    private static Pattern off;
    private static Pattern toggle;
    private static Pattern enable;
    private static Pattern disable;
    private static Pattern start;
    private static Pattern stop;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = OnOff.class.getSimpleName();
    private int confidence;

    public enum Result {
        UNRESOLVED,
        ON,
        OFF,
        TOGGLE,
        CANCEL
    }

    public OnOff(ai.saiy.android.localisation.SaiyResources sr) {
        if (on == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    private int calculateConfidence(int onCount, int offCount, int toggleCount) {
        if ((onCount > 0 && offCount == 0 && toggleCount == 0) || ((offCount > 0 && onCount == 0 && toggleCount == 0) || (toggleCount > 0 && offCount == 0 && onCount == 0))) {
            return 100;
        }
        if (onCount > offCount && onCount > toggleCount) {
            return (int) Math.round(((double) ((onCount + offCount) + toggleCount) / onCount) * 100.0d);
        }
        if (offCount > onCount && offCount > toggleCount) {
            return (int) Math.round(((double) ((onCount + offCount) + toggleCount) / offCount) * 100.0d);
        }
        if (toggleCount > onCount || toggleCount > offCount) {
            return (int) Math.round(((double) ((onCount + offCount) + toggleCount) / toggleCount) * 100.0d);
        }
        return 33;
    }

    private void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        on = Pattern.compile(".*\\b" + sr.getString(R.string.on) + "\\b.*");
        off = Pattern.compile(".*\\b" + sr.getString(R.string.off) + "\\b.*");
        toggle = Pattern.compile(".*\\b" + sr.getString(R.string.toggle) + "\\b.*");
        enable = Pattern.compile(".*\\b" + sr.getString(R.string.enable) + "\\b.*");
        disable = Pattern.compile(".*\\b" + sr.getString(R.string.disable) + "\\b.*");
        start = Pattern.compile(".*\\b" + sr.getString(R.string.start) + "\\b.*");
        stop = Pattern.compile(".*\\b" + sr.getString(R.string.stop) + "\\b.*");
    }

    public int getConfidence() {
        return this.confidence;
    }

    public Result resolve(@NonNull ArrayList<String> voiceData, SupportedLanguage supportedLanguage) { //resolve
        final Locale locale = supportedLanguage.getLocale();
        int toggleCount = 0;
        int offCount = 0;
        int onCount = 0;
        String vdLower;
        for (String voiceDatum : voiceData) {
            vdLower = voiceDatum.toLowerCase(locale).trim();
            if (on.matcher(vdLower).matches() || enable.matcher(vdLower).matches() || start.matcher(vdLower).matches()) {
                onCount++;
            }
            if (off.matcher(vdLower).matches() || disable.matcher(vdLower).matches() || stop.matcher(vdLower).matches()) {
                offCount++;
            }
            if (toggle.matcher(vdLower).matches()) {
                toggleCount++;
            }
        }
        this.confidence = calculateConfidence(onCount, offCount, toggleCount);
        return (onCount <= offCount || onCount <= toggleCount) ? (offCount <= onCount || offCount <= toggleCount) ? (toggleCount <= onCount || toggleCount <= offCount) ? Result.UNRESOLVED : Result.TOGGLE : Result.OFF : Result.ON;
    }

    public Result resolve(@NonNull ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.localisation.SaiyResources sr) {
        if (new ai.saiy.android.command.cancel.Cancel(supportedLanguage, sr).detectCancel(voiceData)) {
            return Result.CANCEL;
        }
        final Locale locale = supportedLanguage.getLocale();
        int toggleCount = 0;
        int offCount = 0;
        int onCount = 0;
        String vdLower;
        for (String voiceDatum : voiceData) {
            vdLower = voiceDatum.toLowerCase(locale).trim();
            if (on.matcher(vdLower).matches() || enable.matcher(vdLower).matches() || start.matcher(vdLower).matches()) {
                onCount++;
            }
            if (off.matcher(vdLower).matches() || disable.matcher(vdLower).matches() || stop.matcher(vdLower).matches()) {
                offCount++;
            }
            if (toggle.matcher(vdLower).matches()) {
                toggleCount++;
            }
        }
        this.confidence = calculateConfidence(onCount, offCount, toggleCount);
        return (onCount <= offCount || onCount <= toggleCount) ? (offCount <= onCount || offCount <= toggleCount) ? (toggleCount <= onCount || toggleCount <= offCount) ? Result.UNRESOLVED : Result.TOGGLE : Result.OFF : Result.ON;
    }
}
