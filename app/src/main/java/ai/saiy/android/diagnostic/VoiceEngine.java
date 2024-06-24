package ai.saiy.android.diagnostic;

import android.content.Intent;

import java.util.List;

public class VoiceEngine {
    private String applicationName;
    private String packageName;

    private List<String> mAvailableStrLocales;
    private Intent checkIntent;
    private String dataRoot;

    public String getDataRoot() {
        return this.dataRoot;
    }

    public void setIntent(Intent intent) {
        this.checkIntent = intent;
    }

    public void setDataRoot(String str) {
        this.dataRoot = str;
    }

    public void setAvailableLocales(List<String> list) {
        this.mAvailableStrLocales = list;
    }

    public Intent getIntent() {
        return this.checkIntent;
    }

    public void setApplicationName(String str) {
        this.applicationName = str;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public List<String> getAvailableLocales() {
        return this.mAvailableStrLocales;
    }
}
