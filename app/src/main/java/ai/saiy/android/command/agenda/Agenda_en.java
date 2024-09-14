package ai.saiy.android.command.agenda;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class Agenda_en {
    private static String set;
    private static String word_new;
    private static String create;
    private static String open;
    private static String launch;
    private static String run;
    private static String navigate;
    private static String calendar;
    private static String appointment;
    private static String diary;
    private static String agenda;
    private static String meeting;
    private static String gender;
    private static String am_i_busy;
    private static String i_m_a_busy;
    private static String what_am_i_doing;
    private static String schedule;
    private static String event;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Agenda_en.class.getSimpleName();

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Agenda_en(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
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

    private static void initStrings(@NonNull ai.saiy.android.localisation.SaiyResources sr) {
        set = sr.getString(R.string.set);
        word_new = sr.getString(R.string.word_new);
        create = sr.getString(R.string.create);
        open = sr.getString(R.string.open);
        launch = sr.getString(R.string.launch);
        run = sr.getString(R.string.run);
        navigate = sr.getString(R.string.navigate);
        calendar = sr.getString(R.string.calendar);
        diary = sr.getString(R.string.diary);
        appointment = sr.getString(R.string.appointment);
        agenda = sr.getString(R.string.agenda);
        meeting = sr.getString(R.string.meeting);
        gender = sr.getString(R.string.gender);
        am_i_busy = sr.getString(R.string.am_i_busy);
        i_m_a_busy = sr.getString(R.string.i_m_a_busy);
        what_am_i_doing = sr.getString(R.string.what_am_i_doing);
        schedule = sr.getString(R.string.schedule);
        event = sr.getString(R.string.event);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(voiceData) && ai.saiy.android.utils.UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (!vdLower.startsWith(set) && !vdLower.startsWith(word_new) && !vdLower.startsWith(create) && !vdLower.startsWith(open) && !vdLower.startsWith(Agenda_en.navigate) && !vdLower.startsWith(run) && !vdLower.startsWith(launch) && (vdLower.contains(calendar) || vdLower.contains(diary) || vdLower.contains(appointment) || vdLower.contains(agenda) || vdLower.contains(meeting) || vdLower.contains(gender) || vdLower.startsWith(am_i_busy) || vdLower.startsWith(i_m_a_busy) || vdLower.startsWith(what_am_i_doing) || vdLower.contains(schedule) || vdLower.contains(event))) {
                    toReturn.add(new Pair<>(CC.COMMAND_AGENDA, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "agenda: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
