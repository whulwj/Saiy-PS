package ai.saiy.android.command.application.kill;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Kill implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object kill;

    public Kill(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Kill(ai.saiy.android.localisation.SaiyResources aVar, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.kill = new Kill_en(aVar, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.kill = new Kill_en(aVar, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.kill = new Kill_en(aVar, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (this.sl) {
            case ENGLISH:
                return ((Kill_en) this.kill).detectCallable();
            case ENGLISH_US:
                return ((Kill_en) this.kill).detectCallable();
            default:
                return ((Kill_en) this.kill).detectCallable();
        }
    }

    public ArrayList<String> detectKill(Context context, ArrayList<String> voiceData) {
        switch (this.sl) {
            case ENGLISH:
                return Kill_en.detectKill(context, voiceData, this.sl);
            case ENGLISH_US:
                return Kill_en.detectKill(context, voiceData, this.sl);
            default:
                return Kill_en.detectKill(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
