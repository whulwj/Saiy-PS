package ai.saiy.android.command.time;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Time implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object time;

    public Time(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Time(ai.saiy.android.localisation.SaiyResources aVar, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.time = new Time_en(aVar, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.time = new Time_en(aVar, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.time = new Time_en(aVar, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Time_en) time).detectCallable();
            case ENGLISH_US:
                return ((Time_en) time).detectCallable();
            default:
                return ((Time_en) time).detectCallable();
        }
    }

    public ArrayList<String> sort(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Time_en.sortTime(context, voiceData, sl);
            case ENGLISH_US:
                return Time_en.sortTime(context, voiceData, sl);
            default:
                return Time_en.sortTime(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
