package ai.saiy.android.command.calendar;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public class Calendar_en {
    private static String set;
    private static String calendar;
    private static String word_new;
    private static String appointment;
    private static String create;
    private static String event;
    private static String schedule;
    private static String meeting;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Calendar_en.class.getSimpleName();
    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Calendar_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (set == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        set = sr.getString(R.string.set);
        word_new = sr.getString(R.string.word_new);
        create = sr.getString(R.string.create);
        appointment = sr.getString(R.string.appointment);
        event = sr.getString(R.string.event);
        calendar = sr.getString(R.string.calendar);
        schedule = sr.getString(R.string.schedule);
        meeting = sr.getString(R.string.meeting);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            String vdLower;
            for (int i = 0; i < size; i++) {
                vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if ((vdLower.startsWith(set) || vdLower.startsWith(word_new) || vdLower.startsWith(create) || vdLower.startsWith(schedule)) && (vdLower.contains(appointment) || vdLower.contains(calendar) || vdLower.contains(event) || vdLower.contains(meeting))) {
                    toReturn.add(new Pair<>(CC.COMMAND_CALENDAR, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "calendar: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
