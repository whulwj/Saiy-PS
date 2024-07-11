package ai.saiy.android.command.alarm;

import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Alarm implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object alarm;

    public Alarm(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.alarm = new Alarm_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.alarm = new Alarm_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.alarm = new Alarm_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (this.sl) {
            case ENGLISH:
                return ((Alarm_en) this.alarm).detectCallable();
            case ENGLISH_US:
                return ((Alarm_en) this.alarm).detectCallable();
            default:
                return ((Alarm_en) this.alarm).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
