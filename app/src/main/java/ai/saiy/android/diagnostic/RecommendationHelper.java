package ai.saiy.android.diagnostic;

import android.content.Context;
import android.os.Build;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.tts.helper.TTSDefaults;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

public class RecommendationHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RecommendationHelper.class.getSimpleName();

    private void debug(ArrayList<VoiceEngineInfo> voiceEngineInfos) {
        if (DEBUG) {
            for (VoiceEngineInfo voiceEngineInfo : voiceEngineInfos) {
                MyLog.d(CLS_NAME, "------------------------------------------");
                MyLog.d(CLS_NAME, "getEngineName: " + voiceEngineInfo.getApplicationName());
                MyLog.d(CLS_NAME, "getPackageName: " + voiceEngineInfo.getPackageName());
                MyLog.d(CLS_NAME, "hasSupportedLanguage: " + voiceEngineInfo.hasSupportedLanguage());
                List<Locale> localeArray = voiceEngineInfo.getLocaleArray();
                MyLog.d(CLS_NAME, "localeArray size: " + localeArray.size());
                for (Locale locale : localeArray) {
                    MyLog.d(CLS_NAME, "loc: " + locale.toString());
                }
                MyLog.d(CLS_NAME, "------------------------------------------");
            }
        }
    }

    public ArrayList<String> recommend(Context context, DiagnosticsInfo diagnosticsInfo) {
        final long then = System.nanoTime();
        ArrayList<String> arrayList = new ArrayList<>();
        ArrayList<VoiceEngineInfo> voiceEngineInfos = diagnosticsInfo.getVoiceEngineInfos();
        debug(voiceEngineInfos);
        arrayList.add("************************************");
        arrayList.add("* -------RECOMMENDATIONS------- *");
        arrayList.add("************************************");
        arrayList.add("\n");
        boolean noGoogleNow = false;
        if (diagnosticsInfo.getASRCount() == 0) {
            arrayList.add("! " + context.getString(R.string.diagnostics_install_google_now));
            arrayList.add("\n");
            noGoogleNow = true;
        }
        if (diagnosticsInfo.getPackageName() != null && !diagnosticsInfo.getPackageName().matches(Installed.PACKAGE_NAME_GOOGLE_NOW)) {
            if (noGoogleNow) {
                arrayList.add("\n");
            }
            arrayList.add(context.getString(R.string.diagnostics_set_vr_google) + " ❌");
            noGoogleNow = true;
        }
        boolean newLine;
        if (noGoogleNow || Build.VERSION.SDK_INT < Build.VERSION_CODES.O || UtilsList.notNaked(diagnosticsInfo.getSupportedLocales())) {
            newLine = noGoogleNow;
        } else {
            arrayList.add(context.getString(R.string.diagnostics_no_vr_languages_bug) + " ❌");
            newLine = true;
        }

        if (diagnosticsInfo.getTTSCount() == 0) {
            if (newLine) {
                arrayList.add("\n");
            }
            arrayList.add(context.getString(R.string.diagnostics_install_tts) + " ❌");
            newLine = true;
        } else {
            boolean haveGoogleTTS = false;
            for (VoiceEngineInfo voiceEngineInfo : voiceEngineInfos) {
                if (voiceEngineInfo.getPackageName().matches(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
                    haveGoogleTTS = true;
                    break;
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (!haveGoogleTTS) {
                    if (newLine) {
                        arrayList.add("\n");
                    }
                    arrayList.add("! " + context.getString(R.string.diagnostics_install_tts_google));
                    newLine = true;
                } else if (!UtilsString.notNaked(diagnosticsInfo.getDefaultTTSPackage()) || !diagnosticsInfo.getDefaultTTSPackage().matches(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
                    if (newLine) {
                        arrayList.add("\n");
                    }
                    arrayList.add("! " + context.getString(R.string.diagnostics_set_tts_google));
                    newLine = true;
                }
            }
            for (VoiceEngineInfo voiceEngineInfo : voiceEngineInfos) {
                if (diagnosticsInfo.getDefaultTTSPackage().matches(voiceEngineInfo.getPackageName())) {
                    if (!voiceEngineInfo.hasSupportedLanguage()) {
                        if (newLine) {
                            arrayList.add("\n");
                        }
                        if (haveGoogleTTS) {
                            arrayList.add("! " + context.getString(R.string.diagnostics_supported_language_1) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.diagnostics_supported_language_2));
                        } else {
                            arrayList.add("! " + context.getString(R.string.diagnostics_supported_language_1) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.diagnostics_supported_language_3));
                        }
                        newLine = true;
                    }
                    if (!voiceEngineInfo.isTextToSpeechDone()) {
                        if (newLine) {
                            arrayList.add("\n");
                        }
                        if (voiceEngineInfo.getPackageName().matches(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
                            arrayList.add("! " + context.getString(R.string.diagnostics_supported_language_5));
                        } else if (haveGoogleTTS) {
                            arrayList.add("! " + context.getString(R.string.diagnostics_supported_language_4) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.diagnostics_supported_language_2));
                        } else {
                            arrayList.add("! " + context.getString(R.string.diagnostics_supported_language_4) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.diagnostics_supported_language_3));
                        }
                        newLine = true;
                    }
                }
            }
        }

        if (!newLine) {
            arrayList.add(context.getString(R.string.diagnostics_perfect));
            arrayList.add(context.getString(R.string.diagnostics_no_changes) + " ✔ ✔ ✔");
        }
        arrayList.add("\n");
        arrayList.add("*************************");
        arrayList.add("* ---------RESULTS--------- *");
        arrayList.add("*************************");
        arrayList.add("\n");
        if (diagnosticsInfo.getASRCount() == 0) {
            arrayList.add(context.getString(R.string.diagnostics_vr_installed) + " ❌");
        } else {
            arrayList.add(context.getString(R.string.diagnostics_vr_installed) + " ✔");
            arrayList.add(context.getString(R.string.diagnostics_default_provider_) + XMLResultsHandler.SEP_SPACE + diagnosticsInfo.getApplicationName());
        }
        if (diagnosticsInfo.getTTSCount() == 0) {
            arrayList.add(context.getString(R.string.diagnostics_tts_installed) + " ❌");
        } else {
            arrayList.add(context.getString(R.string.diagnostics_tts_installed) + " ✔");
            arrayList.add(context.getString(R.string.diagnostics_default_provider_) + XMLResultsHandler.SEP_SPACE + diagnosticsInfo.getEngineName());
        }
        for (VoiceEngineInfo voiceEngineInfo : voiceEngineInfos) {
            arrayList.add("\n");
            arrayList.add("★ " + voiceEngineInfo.getApplicationName() + " ★");
            arrayList.add("----------------------------------------");
            if (voiceEngineInfo.hasSupportedLanguage()) {
                arrayList.add(context.getString(R.string.diagnostics_has_supported_language) + " ✔");
            } else {
                arrayList.add(context.getString(R.string.diagnostics_has_supported_language) + " ❌");
            }
            if (voiceEngineInfo.isTextToSpeechDone()) {
                arrayList.add(context.getString(R.string.diagnostics_passed_test) + " ✔");
            } else {
                arrayList.add(context.getString(R.string.diagnostics_passed_test) + " ❌");
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !voiceEngineInfo.getPackageName().matches(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
                arrayList.add(context.getString(R.string.diagnostics_switch_voices) + " ❌");
            } else {
                arrayList.add(context.getString(R.string.diagnostics_switch_voices) + " ✔");
            }
            arrayList.add("----------------------------------------");
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return arrayList;
    }
}
