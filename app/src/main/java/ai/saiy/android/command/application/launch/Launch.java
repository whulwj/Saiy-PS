package ai.saiy.android.command.application.launch;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Launch implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object launch;

    public Launch(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Launch(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.launch = new Launch_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.launch = new Launch_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.launch = new Launch_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Launch_en) launch).detectCallable();
            case ENGLISH_US:
                return ((Launch_en) launch).detectCallable();
            default:
                return ((Launch_en) launch).detectCallable();
        }
    }

    public ArrayList<String> detectLaunch(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Launch_en.detectLaunch(context, voiceData, sl);
            case ENGLISH_US:
                return Launch_en.detectLaunch(context, voiceData, sl);
            default:
                return Launch_en.detectLaunch(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
