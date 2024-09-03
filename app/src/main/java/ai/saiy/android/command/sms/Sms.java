package ai.saiy.android.command.sms;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Sms implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object sms;

    public Sms(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Sms(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.sms = new Sms_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.sms = new Sms_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.sms = new Sms_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (this.sl) {
            case ENGLISH:
                return ((Sms_en) sms).detectCallable();
            case ENGLISH_US:
                return ((Sms_en) sms).detectCallable();
            default:
                return ((Sms_en) sms).detectCallable();
        }
    }

    public ArrayList<String> sort(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Sms_en.sortSms(context, voiceData, sl);
            case ENGLISH_US:
                return Sms_en.sortSms(context, voiceData, sl);
            default:
                return Sms_en.sortSms(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
