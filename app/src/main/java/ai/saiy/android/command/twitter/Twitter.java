package ai.saiy.android.command.twitter;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Twitter implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object twitter;

    public Twitter(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Twitter(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.twitter = new Twitter_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.twitter = new Twitter_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.twitter = new Twitter_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public CommandTwitterValues sort(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Twitter_en.sortTwitter(context, voiceData, sl);
            case ENGLISH_US:
                return Twitter_en.sortTwitter(context, voiceData, sl);
            default:
                return Twitter_en.sortTwitter(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Twitter_en) twitter).detectCallable();
            case ENGLISH_US:
                return ((Twitter_en) twitter).detectCallable();
            default:
                return ((Twitter_en) twitter).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
