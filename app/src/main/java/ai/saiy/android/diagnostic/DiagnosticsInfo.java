package ai.saiy.android.diagnostic;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

public class DiagnosticsInfo {
    private final ArrayList<Locale> supportedLocales = new ArrayList<>();
    private ArrayList<String> supportedLanguages = new ArrayList<>();

    private final ArrayList<VoiceEngineInfo> voiceEngineInfos = new ArrayList<>();
    private int ASRCount;
    private int TTSCount;
    private int errorCount;
    private int passedCount;
    private String defaultTTSPackage;
    private String applicationName;
    private String packageName;

    public String getDefaultTTSPackage() {
        return this.defaultTTSPackage != null ? this.defaultTTSPackage : "";
    }

    public void setASRCount(int i) {
        this.ASRCount = i;
    }

    public void setDefaultTTSPackage(String str) {
        this.defaultTTSPackage = str;
    }

    public void setSupportedLanguages(ArrayList<String> arrayList) {
        this.supportedLanguages = arrayList;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setTTSCount(int i) {
        this.TTSCount = i;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    public String getApplicationName() {
        return this.applicationName == null ? "" : this.applicationName;
    }

    public void setErrorCount(int i) {
        this.errorCount = i;
    }

    public void setApplicationName(String str) {
        this.applicationName = str;
    }

    public String getEngineName() {
        if (this.defaultTTSPackage == null) {
            return "";
        }
        for (VoiceEngineInfo voiceEngineInfo : this.voiceEngineInfos) {
            if (voiceEngineInfo.getPackageName().matches(this.defaultTTSPackage)) {
                return voiceEngineInfo.getApplicationName();
            }
        }
        return this.defaultTTSPackage;
    }

    public void setPassedCount(int i) {
        this.passedCount = i;
    }

    public @NonNull ArrayList<VoiceEngineInfo> getVoiceEngineInfos() {
        return this.voiceEngineInfos;
    }

    public ArrayList<Locale> getSupportedLocales() {
        return this.supportedLocales;
    }

    public ArrayList<String> getSupportedLanguages() {
        return this.supportedLanguages;
    }

    public int getASRCount() {
        return this.ASRCount;
    }

    public int getTTSCount() {
        return this.TTSCount;
    }

    public int getErrorCount() {
        return this.errorCount;
    }

    public int getPassedCount() {
        return this.passedCount;
    }
}
