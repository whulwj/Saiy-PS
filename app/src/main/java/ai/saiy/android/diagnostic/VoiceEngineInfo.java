package ai.saiy.android.diagnostic;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class VoiceEngineInfo {
    private String packageName;
    private String applicationName;
    private List<Locale> localeArray = Collections.emptyList();
    private boolean textToSpeechDone;

    public String getApplicationName() {
        return this.applicationName;
    }

    public void setApplicationName(String str) {
        this.applicationName = str;
    }

    public void setLocaleArray(List<Locale> list) {
        this.localeArray = list;
    }

    public void setTextToSpeechDone(boolean isDone) {
        this.textToSpeechDone = isDone;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    public boolean hasSupportedLanguage() {
        for (Locale locale : this.localeArray) {
            if (locale.toString().startsWith(Locale.ENGLISH.getLanguage()) || locale.toString().startsWith(Locale.ENGLISH.getLanguage().toUpperCase(ai.saiy.android.utils.UtilsLocale.getDefaultLocale()))) {
                return true;
            }
        }
        return false;
    }

    public String getPackageName() {
        return this.packageName != null ? this.packageName : "";
    }

    public boolean isTextToSpeechDone() {
        return this.textToSpeechDone;
    }

    public List<Locale> getLocaleArray() {
        return this.localeArray;
    }
}
