package ai.saiy.android.command.calendar;

import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Calendar implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object calendar;

    public Calendar(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> arrayList, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.calendar = new Calendar_en(sr, supportedLanguage, arrayList, confidence);
                break;
            case ENGLISH_US:
                this.calendar = new Calendar_en(sr, supportedLanguage, arrayList, confidence);
                break;
            default:
                this.calendar = new Calendar_en(sr, SupportedLanguage.ENGLISH, arrayList, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (this.sl) {
            case ENGLISH:
                return ((Calendar_en) this.calendar).detectCallable();
            case ENGLISH_US:
                return ((Calendar_en) this.calendar).detectCallable();
            default:
                return ((Calendar_en) this.calendar).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
