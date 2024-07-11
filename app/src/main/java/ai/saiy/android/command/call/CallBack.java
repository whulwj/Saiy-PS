package ai.saiy.android.command.call;

import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class CallBack implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object callBack;

    public CallBack(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.callBack = new CallBack_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.callBack = new CallBack_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.callBack = new CallBack_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((CallBack_en) callBack).detectCallable();
            case ENGLISH_US:
                return ((CallBack_en) callBack).detectCallable();
            default:
                return ((CallBack_en) callBack).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
