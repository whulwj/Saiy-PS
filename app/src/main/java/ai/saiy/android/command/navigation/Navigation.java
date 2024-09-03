package ai.saiy.android.command.navigation;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Navigation implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object navigation;

    public Navigation(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Navigation(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.navigation = new Navigation_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.navigation = new Navigation_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.navigation = new Navigation_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public CommandNavigationValues sort(Context context, ArrayList<String> voiceData) {
        switch (this.sl) {
            case ENGLISH:
                return Navigation_en.sortNavigation(context, voiceData, sl);
            case ENGLISH_US:
                return Navigation_en.sortNavigation(context, voiceData, sl);
            default:
                return Navigation_en.sortNavigation(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (this.sl) {
            case ENGLISH:
                return ((Navigation_en) this.navigation).detectCallable();
            case ENGLISH_US:
                return ((Navigation_en) this.navigation).detectCallable();
            default:
                return ((Navigation_en) this.navigation).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
