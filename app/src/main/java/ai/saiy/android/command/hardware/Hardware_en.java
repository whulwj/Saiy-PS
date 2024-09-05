package ai.saiy.android.command.hardware;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.OnOff;
import ai.saiy.android.utils.MyLog;

public class Hardware_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Hardware_en.class.getSimpleName();

    private static String wifi;
    private static String wi_fi;
    private static String data;
    private static String bluetooth;
    private static String gps;
    private static String g_p_s;
    private static String nfc;
    private static String n_f_c;
    private static String hotspot;
    private static String hot_spot;
    private static String airplane;
    private static String aeroplane;
    private static String flight;
    private static String torch;
    private static String flashlight;
    private static String flash_light;

    private final SupportedLanguage sl;
    private final ai.saiy.android.localisation.SaiyResources sr;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Hardware_en(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        this.sr = sr;
        if (bluetooth == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static Pair<HardwareType, OnOff.Result> sortHardware(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
        if (bluetooth == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        final OnOff onOff = new OnOff(sr);
        final OnOff.Result result = onOff.resolve(voiceData, supportedLanguage, sr);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "result: " + result.name());
            MyLog.i(CLS_NAME, "confidenceScore: " + onOff.getConfidence());
        }
        sr.reset();
        for (String voiceDatum : voiceData) {
            String vdLower = voiceDatum.toLowerCase(locale).trim();
            if (vdLower.contains(bluetooth)) {
                return new Pair<>(HardwareType.BLUETOOTH, result);
            }
            if (vdLower.contains(data)) {
                return new Pair<>(HardwareType.DATA, result);
            }
            if (vdLower.contains(wifi) || vdLower.contains(wi_fi)) {
                return new Pair<>(HardwareType.WIFI, result);
            }
            if (vdLower.contains(gps) || vdLower.contains(g_p_s)) {
                return new Pair<>(HardwareType.GPS, result);
            }
            if (vdLower.contains(nfc) || vdLower.contains(n_f_c)) {
                return new Pair<>(HardwareType.NFC, result);
            }
            if (vdLower.contains(hot_spot) || vdLower.contains(hotspot)) {
                return new Pair<>(HardwareType.HOTSPOT, result);
            }
            if (vdLower.contains(airplane) || vdLower.contains(aeroplane) || vdLower.contains(flight)) {
                return new Pair<>(HardwareType.AEROPLANE, result);
            }
            if (vdLower.contains(torch) || vdLower.contains(flash_light) || vdLower.contains(flashlight)) {
                return new Pair<>(HardwareType.FLASHLIGHT, result);
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return new Pair<>(HardwareType.UNRESOLVED, OnOff.Result.UNRESOLVED);
    }

    private static void initStrings(@NonNull ai.saiy.android.localisation.SaiyResources sr) {
        bluetooth = sr.getString(R.string.bluetooth);
        data = sr.getString(R.string.data);
        wifi = sr.getString(R.string.wifi);
        wi_fi = sr.getString(R.string.wi_fi);
        gps = sr.getString(R.string.gps);
        g_p_s = sr.getString(R.string.g_p_s);
        nfc = sr.getString(R.string.nfc);
        n_f_c = sr.getString(R.string.n_f_c);
        hotspot = sr.getString(R.string.hotspot);
        hot_spot = sr.getString(R.string.hot_spot);
        airplane = sr.getString(R.string.airplane);
        aeroplane = sr.getString(R.string.aeroplane);
        flight = sr.getString(R.string.flight);
        torch = sr.getString(R.string.torch);
        flashlight = sr.getString(R.string.flashlight);
        flash_light = sr.getString(R.string.flash_light);
    }

    public @NonNull ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(voiceData) && ai.saiy.android.utils.UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final OnOff onOff = new OnOff(sr);
            final OnOff.Result result = onOff.resolve(voiceData, sl, sr);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "result: " + result.name());
                MyLog.i(CLS_NAME, "confidenceScore: " + onOff.getConfidence());
            }
            switch (result) {
                case UNRESOLVED:
                case CANCEL:
                    return toReturn;
                default:
                    final Locale locale = sl.getLocale();
                    final int size = voiceData.size();
                    for (int i = 0; i < size; i++) {
                        String vdLower = voiceData.get(i).toLowerCase(locale).trim();
                        if (vdLower.contains(wifi) || vdLower.contains(wi_fi) || vdLower.contains(Hardware_en.data) || vdLower.contains(bluetooth) || vdLower.contains(gps) || vdLower.contains(g_p_s) || vdLower.contains(nfc) || vdLower.contains(n_f_c) || vdLower.contains(hotspot) || vdLower.contains(hot_spot) || vdLower.contains(airplane) || vdLower.contains(aeroplane) || vdLower.contains(flight) || vdLower.contains(torch) || vdLower.contains(flashlight) || vdLower.contains(flash_light)) {
                            toReturn.add(new Pair<>(CC.COMMAND_HARDWARE, confidence[i]));
                        }
                    }
                    break;
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "Hardware: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
