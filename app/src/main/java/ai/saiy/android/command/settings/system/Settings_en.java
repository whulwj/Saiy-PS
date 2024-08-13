package ai.saiy.android.command.settings.system;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.command.settings.SettingsIntent;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public class Settings_en {
    private static String TTS_SETTINGS;
    private static String DATE_SETTINGS;
    private static String TIME_SETTINGS;
    private static String DATA_SETTINGS;
    private static String BLUETOOTH_SETTINGS;
    private static String WIFI_SETTINGS;
    private static String DICTIONARY_SETTINGS;
    private static String SINK_SETTINGS;
    private static String SYNC_SETTINGS;
    private static String VOLUME_SETTINGS;
    private static String SOUND_SETTINGS;
    private static String SOUNDS_SETTINGS;
    private static String SETTINGS;
    private static String SEARCH_SETTINGS;
    private static String QUICK_LAUNCH_SETTINGS;
    private static String PRIVACY_SETTINGS;
    private static String N_F_C_SETTINGS;
    private static String LOCATION_SETTINGS;
    private static String BATTERY_SETTINGS;
    private static String SECURITY_SETTINGS;
    private static String NFC_SETTING;
    private static String NETWORK_OPERATOR_SETTING;
    private static String S_D_CARD_SETTING;
    private static String SD_CARD_SETTING;
    private static String STORAGE_CARD_SETTING;
    private static String STORAGE_SETTING;
    private static String SEARCH_SETTING;
    private static String QUICK_LAUNCH_SETTING;
    private static String PRIVACY_SETTING;
    private static String N_F_C_SETTING;
    private static String LOCATION_SETTING;
    private static String BATTERY_SETTING;
    private static String SECURITY_SETTING;
    private static String CLOCK_SETTING;
    private static String CLOCK_SETTINGS;
    private static String KEYBOARD_SETTING;
    private static String KEYBOARD_SETTINGS;
    private static String WI_FI_SETTING;
    private static String WI_FI_SETTINGS;
    private static String MEMORY_CARD_SETTING;
    private static String LOCALE_SETTING;
    private static String INPUT_SETTING;
    private static String DISPLAY_SETTING;
    private static String APPLICATION_SETTING;
    private static String A_P_N_SETTING;
    private static String APN_SETTING;
    private static String ACCESSIBILITY_SETTING;
    private static String DEVICE_SETTING;
    private static String ABOUT_SETTING;
    private static String TEXT_TO_SPEECH_SETTING;
    private static String T_T_S_SETTING;
    private static String VOICE_SEARCH_SETTING;
    private static String TTS_SETTING;
    private static String DATE_SETTING;
    private static String TIME_SETTING;
    private static String DATA_SETTING;
    private static String BLUETOOTH_SETTING;
    private static String WIFI_SETTING;
    private static String DICTIONARY_SETTING;
    private static String SINK_SETTING;
    private static String SYNC_SETTING;
    private static String VOLUME_SETTING;
    private static String SOUND_SETTING;
    private static String SOUNDS_SETTING;
    private static String SETTING;
    private static String NFC_SETTINGS;
    private static String NETWORK_OPERATOR_SETTINGS;
    private static String S_D_CARD_SETTINGS;
    private static String SD_CARD_SETTINGS;
    private static String STORAGE_CARD_SETTINGS;
    private static String STORAGE_SETTINGS;
    private static String MEMORY_CARD_SETTINGS;
    private static String MEMORY_SETTING;
    private static String MEMORY_SETTINGS;
    private static String LOCALE_SETTINGS;
    private static String INPUT_SETTINGS;
    private static String DISPLAY_SETTINGS;
    private static String APPLICATION_SETTINGS;
    private static String A_P_N_SETTINGS;
    private static String APN_SETTINGS;
    private static String ACCESSIBILITY_SETTINGS;
    private static String DEVICE_SETTINGS;
    private static String ABOUT_SETTINGS;
    private static String TEXT_TO_SPEECH_SETTINGS;
    private static String T_T_S_SETTINGS;
    private static String VOICE_SEARCH_SETTINGS;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Settings_en.class.getSimpleName();

    public Settings_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (NFC_SETTINGS == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static @NonNull SettingsIntent detectSettingsIntent(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final SettingsIntent settingsIntent = new SettingsIntent();
        settingsIntent.setType(SettingsIntent.Type.UNKNOWN);
        if (NFC_SETTINGS == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
            sr.reset();
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        String vdLower;
        for (String voiceDatum : voiceData) {
            vdLower = voiceDatum.toLowerCase(locale).trim();
            if (vdLower.endsWith(NFC_SETTING) || vdLower.endsWith(NFC_SETTINGS) || vdLower.endsWith(N_F_C_SETTING) || vdLower.endsWith(N_F_C_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.NFC);
                settingsIntent.setCommand(NFC_SETTINGS);
                break;
            }
            if (vdLower.endsWith(SECURITY_SETTING) || vdLower.endsWith(SECURITY_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.SECURITY);
                settingsIntent.setCommand(SECURITY_SETTINGS);
                break;
            }
            if (vdLower.endsWith(BATTERY_SETTING) || vdLower.endsWith(BATTERY_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.BATTERY);
                settingsIntent.setCommand(BATTERY_SETTINGS);
                break;
            }
            if (vdLower.endsWith(LOCATION_SETTING) || vdLower.endsWith(LOCATION_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.LOCATION);
                settingsIntent.setCommand(LOCATION_SETTINGS);
                break;
            }
            if (vdLower.endsWith(QUICK_LAUNCH_SETTING) || vdLower.endsWith(QUICK_LAUNCH_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.QUICK_LAUNCH);
                settingsIntent.setCommand(QUICK_LAUNCH_SETTINGS);
                break;
            }
            if (vdLower.endsWith(PRIVACY_SETTING) || vdLower.endsWith(PRIVACY_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.PRIVACY);
                settingsIntent.setCommand(PRIVACY_SETTINGS);
                break;
            }
            if (vdLower.endsWith(VOICE_SEARCH_SETTING) || vdLower.endsWith(VOICE_SEARCH_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.VOICE_SEARCH);
                settingsIntent.setCommand(VOICE_SEARCH_SETTINGS);
                break;
            }
            if (vdLower.endsWith(SEARCH_SETTING) || vdLower.endsWith(SEARCH_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.SEARCH);
                settingsIntent.setCommand(SEARCH_SETTINGS);
                break;
            }
            if (vdLower.endsWith(SOUNDS_SETTING) || vdLower.endsWith(SOUNDS_SETTINGS) || vdLower.endsWith(SOUND_SETTING) || vdLower.endsWith(SOUND_SETTINGS) || vdLower.endsWith(VOLUME_SETTING) || vdLower.endsWith(VOLUME_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.SOUND);
                settingsIntent.setCommand(SOUNDS_SETTINGS);
                break;
            }
            if (vdLower.endsWith(SYNC_SETTING) || vdLower.endsWith(SYNC_SETTINGS) || vdLower.endsWith(SINK_SETTING) || vdLower.endsWith(SINK_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.SYNC);
                settingsIntent.setCommand(SYNC_SETTINGS);
                break;
            }
            if (vdLower.endsWith(DICTIONARY_SETTING) || vdLower.endsWith(DICTIONARY_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.DICTIONARY);
                settingsIntent.setCommand(DICTIONARY_SETTINGS);
                break;
            }
            if (vdLower.endsWith(WIFI_SETTING) || vdLower.endsWith(WIFI_SETTINGS) || vdLower.endsWith(WI_FI_SETTING) || vdLower.endsWith(WI_FI_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.WIFI);
                settingsIntent.setCommand(WIFI_SETTINGS);
                break;
            }
            if (vdLower.endsWith(BLUETOOTH_SETTING) || vdLower.endsWith(BLUETOOTH_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.BLUETOOTH);
                settingsIntent.setCommand(BLUETOOTH_SETTINGS);
                break;
            }
            if (vdLower.endsWith(DATA_SETTING) || vdLower.endsWith(DATA_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.DATA);
                settingsIntent.setCommand(DATA_SETTINGS);
                break;
            }
            if (vdLower.endsWith(TTS_SETTING) || vdLower.endsWith(TTS_SETTINGS) || vdLower.endsWith(T_T_S_SETTING) || vdLower.endsWith(T_T_S_SETTINGS) || vdLower.endsWith(TEXT_TO_SPEECH_SETTING) || vdLower.endsWith(TEXT_TO_SPEECH_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.TTS);
                settingsIntent.setCommand(TEXT_TO_SPEECH_SETTINGS);
                break;
            }
            if (vdLower.endsWith(DATE_SETTING) || vdLower.endsWith(DATE_SETTINGS) || vdLower.endsWith(TIME_SETTING) || vdLower.endsWith(TIME_SETTINGS) || vdLower.endsWith(CLOCK_SETTING) || vdLower.endsWith(CLOCK_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.DATE);
                settingsIntent.setCommand(DATE_SETTINGS);
                break;
            }
            if (vdLower.endsWith(DEVICE_SETTING) || vdLower.endsWith(DEVICE_SETTINGS) || vdLower.endsWith(ABOUT_SETTING) || vdLower.endsWith(ABOUT_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.DEVICE);
                settingsIntent.setCommand(DEVICE_SETTINGS);
                break;
            }
            if (vdLower.endsWith(ACCESSIBILITY_SETTING) || vdLower.endsWith(ACCESSIBILITY_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.ACCESSIBILITY);
                settingsIntent.setCommand(ACCESSIBILITY_SETTINGS);
                break;
            }
            if (vdLower.endsWith(APN_SETTING) || vdLower.endsWith(APN_SETTINGS) || vdLower.endsWith(A_P_N_SETTING) || vdLower.endsWith(A_P_N_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.APN);
                settingsIntent.setCommand(APN_SETTINGS);
                break;
            }
            if (vdLower.endsWith(APPLICATION_SETTING) || vdLower.endsWith(APPLICATION_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.APPLICATION);
                settingsIntent.setCommand(APPLICATION_SETTINGS);
                break;
            }
            if (vdLower.endsWith(DISPLAY_SETTING) || vdLower.endsWith(DISPLAY_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.DISPLAY);
                settingsIntent.setCommand(DISPLAY_SETTINGS);
                break;
            }
            if (vdLower.endsWith(INPUT_SETTING) || vdLower.endsWith(INPUT_SETTINGS) || vdLower.endsWith(KEYBOARD_SETTING) || vdLower.endsWith(KEYBOARD_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.INPUT);
                settingsIntent.setCommand(INPUT_SETTINGS);
                break;
            }
            if (vdLower.endsWith(LOCALE_SETTING) || vdLower.endsWith(LOCALE_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.LOCALE);
                settingsIntent.setCommand(LOCALE_SETTINGS);
                break;
            }
            if (vdLower.endsWith(MEMORY_SETTING) || vdLower.endsWith(MEMORY_SETTINGS) || vdLower.endsWith(STORAGE_SETTING) || vdLower.endsWith(STORAGE_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.MEMORY_INTERNAL);
                settingsIntent.setCommand(MEMORY_SETTINGS);
                break;
            }
            if (vdLower.endsWith(MEMORY_CARD_SETTING) || vdLower.endsWith(MEMORY_CARD_SETTINGS) || vdLower.endsWith(S_D_CARD_SETTING) || vdLower.endsWith(S_D_CARD_SETTINGS) || vdLower.endsWith(SD_CARD_SETTING) || vdLower.endsWith(SD_CARD_SETTINGS) || vdLower.endsWith(STORAGE_CARD_SETTING) || vdLower.endsWith(STORAGE_CARD_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.MEMORY_EXTERNAL);
                settingsIntent.setCommand(MEMORY_CARD_SETTINGS);
                break;
            }
            if (vdLower.endsWith(NETWORK_OPERATOR_SETTING) || vdLower.endsWith(NETWORK_OPERATOR_SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.NETWORK);
                settingsIntent.setCommand(NETWORK_OPERATOR_SETTINGS);
                break;
            }
            if (vdLower.endsWith(SETTING) || vdLower.endsWith(SETTINGS)) {
                settingsIntent.setType(SettingsIntent.Type.SETTINGS);
                settingsIntent.setCommand(SETTINGS);
                break;
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return settingsIntent;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        NFC_SETTINGS = sr.getString(R.string.NFC_SETTINGS);
        NETWORK_OPERATOR_SETTINGS = sr.getString(R.string.NETWORK_OPERATOR_SETTINGS);
        S_D_CARD_SETTINGS = sr.getString(R.string.S_D_CARD_SETTINGS);
        SD_CARD_SETTINGS = sr.getString(R.string.SD_CARD_SETTINGS);
        STORAGE_CARD_SETTINGS = sr.getString(R.string.STORAGE_CARD_SETTINGS);
        STORAGE_SETTINGS = sr.getString(R.string.STORAGE_SETTINGS);
        MEMORY_CARD_SETTINGS = sr.getString(R.string.MEMORY_CARD_SETTINGS);
        MEMORY_SETTING = sr.getString(R.string.MEMORY_SETTING);
        MEMORY_SETTINGS = sr.getString(R.string.MEMORY_SETTINGS);
        LOCALE_SETTINGS = sr.getString(R.string.LOCALE_SETTINGS);
        INPUT_SETTINGS = sr.getString(R.string.INPUT_SETTINGS);
        DISPLAY_SETTINGS = sr.getString(R.string.DISPLAY_SETTINGS);
        APPLICATION_SETTINGS = sr.getString(R.string.APPLICATION_SETTINGS);
        A_P_N_SETTINGS = sr.getString(R.string.A_P_N_SETTINGS);
        APN_SETTINGS = sr.getString(R.string.APN_SETTINGS);
        ACCESSIBILITY_SETTINGS = sr.getString(R.string.ACCESSIBILITY_SETTINGS);
        DEVICE_SETTINGS = sr.getString(R.string.DEVICE_SETTINGS);
        ABOUT_SETTINGS = sr.getString(R.string.ABOUT_SETTINGS);
        TEXT_TO_SPEECH_SETTINGS = sr.getString(R.string.TEXT_TO_SPEECH_SETTINGS);
        T_T_S_SETTINGS = sr.getString(R.string.T_T_S_SETTINGS);
        VOICE_SEARCH_SETTINGS = sr.getString(R.string.VOICE_SEARCH_SETTINGS);
        TTS_SETTINGS = sr.getString(R.string.TTS_SETTINGS);
        DATE_SETTINGS = sr.getString(R.string.DATE_SETTINGS);
        TIME_SETTINGS = sr.getString(R.string.TIME_SETTINGS);
        DATA_SETTINGS = sr.getString(R.string.DATA_SETTINGS);
        BLUETOOTH_SETTINGS = sr.getString(R.string.BLUETOOTH_SETTINGS);
        WIFI_SETTINGS = sr.getString(R.string.WIFI_SETTINGS);
        DICTIONARY_SETTINGS = sr.getString(R.string.DICTIONARY_SETTINGS);
        SINK_SETTINGS = sr.getString(R.string.SINK_SETTINGS);
        SYNC_SETTINGS = sr.getString(R.string.SYNC_SETTINGS);
        VOLUME_SETTINGS = sr.getString(R.string.VOLUME_SETTINGS);
        SOUND_SETTINGS = sr.getString(R.string.SOUND_SETTINGS);
        SOUNDS_SETTINGS = sr.getString(R.string.SOUNDS_SETTINGS);
        SETTINGS = sr.getString(R.string.SETTINGS);
        SEARCH_SETTINGS = sr.getString(R.string.SEARCH_SETTINGS);
        QUICK_LAUNCH_SETTINGS = sr.getString(R.string.QUICK_LAUNCH_SETTINGS);
        PRIVACY_SETTINGS = sr.getString(R.string.PRIVACY_SETTINGS);
        N_F_C_SETTINGS = sr.getString(R.string.N_F_C_SETTINGS);
        LOCATION_SETTINGS = sr.getString(R.string.LOCATION_SETTINGS);
        BATTERY_SETTINGS = sr.getString(R.string.BATTERY_SETTINGS);
        SECURITY_SETTINGS = sr.getString(R.string.SECURITY_SETTINGS);
        NFC_SETTING = sr.getString(R.string.NFC_SETTING);
        NETWORK_OPERATOR_SETTING = sr.getString(R.string.NETWORK_OPERATOR_SETTING);
        S_D_CARD_SETTING = sr.getString(R.string.S_D_CARD_SETTING);
        SD_CARD_SETTING = sr.getString(R.string.SD_CARD_SETTING);
        STORAGE_CARD_SETTING = sr.getString(R.string.STORAGE_CARD_SETTING);
        STORAGE_SETTING = sr.getString(R.string.STORAGE_SETTING);
        MEMORY_CARD_SETTING = sr.getString(R.string.MEMORY_CARD_SETTING);
        LOCALE_SETTING = sr.getString(R.string.LOCALE_SETTING);
        INPUT_SETTING = sr.getString(R.string.INPUT_SETTING);
        DISPLAY_SETTING = sr.getString(R.string.DISPLAY_SETTING);
        APPLICATION_SETTING = sr.getString(R.string.APPLICATION_SETTING);
        A_P_N_SETTING = sr.getString(R.string.A_P_N_SETTING);
        APN_SETTING = sr.getString(R.string.APN_SETTING);
        ACCESSIBILITY_SETTING = sr.getString(R.string.ACCESSIBILITY_SETTING);
        DEVICE_SETTING = sr.getString(R.string.DEVICE_SETTING);
        ABOUT_SETTING = sr.getString(R.string.ABOUT_SETTING);
        TEXT_TO_SPEECH_SETTING = sr.getString(R.string.TEXT_TO_SPEECH_SETTING);
        T_T_S_SETTING = sr.getString(R.string.T_T_S_SETTING);
        VOICE_SEARCH_SETTING = sr.getString(R.string.VOICE_SEARCH_SETTING);
        TTS_SETTING = sr.getString(R.string.TTS_SETTING);
        DATE_SETTING = sr.getString(R.string.DATE_SETTING);
        TIME_SETTING = sr.getString(R.string.TIME_SETTING);
        DATA_SETTING = sr.getString(R.string.DATA_SETTING);
        BLUETOOTH_SETTING = sr.getString(R.string.BLUETOOTH_SETTING);
        WIFI_SETTING = sr.getString(R.string.WIFI_SETTING);
        DICTIONARY_SETTING = sr.getString(R.string.DICTIONARY_SETTING);
        SINK_SETTING = sr.getString(R.string.SINK_SETTING);
        SYNC_SETTING = sr.getString(R.string.SYNC_SETTING);
        VOLUME_SETTING = sr.getString(R.string.VOLUME_SETTING);
        SOUND_SETTING = sr.getString(R.string.SOUND_SETTING);
        SOUNDS_SETTING = sr.getString(R.string.SOUNDS_SETTING);
        SETTING = sr.getString(R.string.SETTING);
        SEARCH_SETTING = sr.getString(R.string.SEARCH_SETTING);
        QUICK_LAUNCH_SETTING = sr.getString(R.string.QUICK_LAUNCH_SETTING);
        PRIVACY_SETTING = sr.getString(R.string.PRIVACY_SETTING);
        N_F_C_SETTING = sr.getString(R.string.N_F_C_SETTING);
        LOCATION_SETTING = sr.getString(R.string.LOCATION_SETTING);
        BATTERY_SETTING = sr.getString(R.string.BATTERY_SETTING);
        SECURITY_SETTING = sr.getString(R.string.SECURITY_SETTING);
        CLOCK_SETTING = sr.getString(R.string.CLOCK_SETTING);
        CLOCK_SETTINGS = sr.getString(R.string.CLOCK_SETTINGS);
        KEYBOARD_SETTING = sr.getString(R.string.KEYBOARD_SETTING);
        KEYBOARD_SETTINGS = sr.getString(R.string.KEYBOARD_SETTINGS);
        WI_FI_SETTING = sr.getString(R.string.WI_FI_SETTING);
        WI_FI_SETTINGS = sr.getString(R.string.WI_FI_SETTINGS);
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
                if (vdLower.endsWith(SECURITY_SETTING) || vdLower.endsWith(NFC_SETTINGS) || vdLower.endsWith(APPLICATION_SETTINGS) || vdLower.endsWith(DISPLAY_SETTINGS) || vdLower.endsWith(INPUT_SETTINGS) || vdLower.endsWith(LOCALE_SETTINGS) || vdLower.endsWith(MEMORY_SETTING) || vdLower.endsWith(MEMORY_SETTINGS) || vdLower.endsWith(MEMORY_CARD_SETTINGS) || vdLower.endsWith(STORAGE_SETTINGS) || vdLower.endsWith(STORAGE_CARD_SETTINGS) || vdLower.endsWith(SD_CARD_SETTINGS) || vdLower.endsWith(S_D_CARD_SETTINGS) || vdLower.endsWith(NETWORK_OPERATOR_SETTINGS) || vdLower.endsWith(QUICK_LAUNCH_SETTINGS) || vdLower.endsWith(SEARCH_SETTINGS) || vdLower.endsWith(SETTINGS) || vdLower.endsWith(SOUNDS_SETTINGS) || vdLower.endsWith(SOUND_SETTINGS) || vdLower.endsWith(VOLUME_SETTINGS) || vdLower.endsWith(SYNC_SETTINGS) || vdLower.endsWith(SINK_SETTINGS) || vdLower.endsWith(DICTIONARY_SETTINGS) || vdLower.endsWith(WIFI_SETTINGS) || vdLower.endsWith(BLUETOOTH_SETTINGS) || vdLower.endsWith(DATA_SETTINGS) || vdLower.endsWith(TIME_SETTINGS) || vdLower.endsWith(CLOCK_SETTING) || vdLower.endsWith(CLOCK_SETTINGS) || vdLower.endsWith(DATE_SETTINGS) || vdLower.endsWith(TTS_SETTINGS) || vdLower.endsWith(VOICE_SEARCH_SETTINGS) || vdLower.endsWith(T_T_S_SETTINGS) || vdLower.endsWith(TEXT_TO_SPEECH_SETTINGS) || vdLower.endsWith(DEVICE_SETTINGS) || vdLower.endsWith(ABOUT_SETTINGS) || vdLower.endsWith(ACCESSIBILITY_SETTINGS) || vdLower.endsWith(APN_SETTINGS) || vdLower.endsWith(A_P_N_SETTINGS) || vdLower.endsWith(DISPLAY_SETTING) || vdLower.endsWith(INPUT_SETTING) || vdLower.endsWith(LOCALE_SETTING) || vdLower.endsWith(MEMORY_CARD_SETTING) || vdLower.endsWith(STORAGE_SETTING) || vdLower.endsWith(STORAGE_CARD_SETTING) || vdLower.endsWith(SD_CARD_SETTING) || vdLower.endsWith(S_D_CARD_SETTING) || vdLower.endsWith(NETWORK_OPERATOR_SETTING) || vdLower.endsWith(NFC_SETTING) || vdLower.endsWith(SECURITY_SETTINGS) || vdLower.endsWith(BATTERY_SETTINGS) || vdLower.endsWith(LOCATION_SETTINGS) || vdLower.endsWith(N_F_C_SETTINGS) || vdLower.endsWith(PRIVACY_SETTINGS) || vdLower.endsWith(BATTERY_SETTING) || vdLower.endsWith(LOCATION_SETTING) || vdLower.endsWith(N_F_C_SETTING) || vdLower.endsWith(PRIVACY_SETTING) || vdLower.endsWith(QUICK_LAUNCH_SETTING) || vdLower.endsWith(SEARCH_SETTING) || vdLower.endsWith(SETTING) || vdLower.endsWith(SOUNDS_SETTING) || vdLower.endsWith(SOUND_SETTING) || vdLower.endsWith(VOLUME_SETTING) || vdLower.endsWith(SYNC_SETTING) || vdLower.endsWith(SINK_SETTING) || vdLower.endsWith(DICTIONARY_SETTING) || vdLower.endsWith(WIFI_SETTING) || vdLower.endsWith(BLUETOOTH_SETTING) || vdLower.endsWith(DATA_SETTING) || vdLower.endsWith(TIME_SETTING) || vdLower.endsWith(DATE_SETTING) || vdLower.endsWith(TTS_SETTING) || vdLower.endsWith(VOICE_SEARCH_SETTING) || vdLower.endsWith(T_T_S_SETTING) || vdLower.endsWith(TEXT_TO_SPEECH_SETTING) || vdLower.endsWith(DEVICE_SETTING) || vdLower.endsWith(ACCESSIBILITY_SETTING) || vdLower.endsWith(APN_SETTING) || vdLower.endsWith(A_P_N_SETTING) || vdLower.endsWith(APPLICATION_SETTING) || vdLower.endsWith(KEYBOARD_SETTING) || vdLower.endsWith(KEYBOARD_SETTINGS) || vdLower.endsWith(WI_FI_SETTING) || vdLower.endsWith(WI_FI_SETTINGS)) {
                    toReturn.add(new Pair<>(CC.COMMAND_SETTINGS, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "settings device: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
