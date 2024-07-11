package ai.saiy.android.command.navigation;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class Navigation_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Navigation_en.class.getSimpleName();
    private static String navigate;
    private static String contact;
    private static String meeting;
    private static String appointment;
    private static String calendar;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Navigation_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (navigate == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static CommandNavigationValues sortNavigation(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
        if (navigate == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        final String the = sr.getString(R.string.the);
        final String address = sr.getString(R.string.address);
        final String destination = sr.getString(R.string.destination);
        final String location = sr.getString(R.string.location);
        final String to = sr.getString(R.string.to);
        final String closest = sr.getString(R.string.closest);
        final String nearest = sr.getString(R.string.nearest);
        sr.reset();
        final CommandNavigationValues commandNavigationValues = new CommandNavigationValues();
        commandNavigationValues.setType(CommandNavigationValues.Type.UNKNOWN);
        commandNavigationValues.setAction("");
        String vdLower;
        for (String voiceDatum : voiceData) {
            vdLower = voiceDatum.toLowerCase(locale).trim();
            if (vdLower.startsWith(navigate) && !vdLower.contains(contact) && !vdLower.contains(meeting) && !vdLower.contains(appointment) && !vdLower.contains(calendar)) {
                String target = vdLower.replaceAll("\\b" + navigate + "\\b\\s*", "").trim().replaceAll("\\b" + to + "\\b\\s*", "").trim().replaceAll("\\b" + the + "\\b\\s*", "").trim().replaceAll("\\b" + address + "\\b\\s*", "").trim().replaceAll("\\b" + destination + "\\b\\s*", "").trim().replaceAll("\\b" + location + "\\b\\s*", "").trim();
                if (target.startsWith(closest) || target.startsWith(nearest)) {
                    target = target.replaceAll("\\b" + closest + "\\b\\s*", "").trim().replaceAll("\\b" + nearest + "\\b\\s*", "").trim();
                }
                commandNavigationValues.setAction(navigate);
                commandNavigationValues.setType(CommandNavigationValues.Type.DESTINATION);
                commandNavigationValues.setAddress(target);
            } else if (vdLower.startsWith(navigate) && !vdLower.contains(contact) && (vdLower.contains(meeting) || vdLower.contains(appointment) || vdLower.contains(calendar))) {
                commandNavigationValues.setAction(appointment);
                commandNavigationValues.setType(CommandNavigationValues.Type.APPOINTMENT);
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return commandNavigationValues;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        navigate = sr.getString(R.string.navigate);
        contact = sr.getString(R.string.contact);
        meeting = sr.getString(R.string.meeting);
        appointment = sr.getString(R.string.appointment);
        calendar = sr.getString(R.string.calendar);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(voiceData) && ai.saiy.android.utils.UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            String vdLower;
            for (int i = 0; i < size; i++) {
                vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.startsWith(navigate) && !vdLower.contains(contact)) {
                    toReturn.add(new Pair<>(CC.COMMAND_NAVIGATION, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "navigate: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
