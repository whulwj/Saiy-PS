package ai.saiy.android.command.search;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import android.content.Context;
import android.util.Pair;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class Search implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object search;

    public Search(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Search(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.search = new Search_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.search = new Search_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.search = new Search_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public CommandSearchValues sortSearch(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Search_en.sortSearch(context, voiceData, sl);
            case ENGLISH_US:
                return Search_en.sortSearch(context, voiceData, sl);
            default:
                return Search_en.sortSearch(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Search_en) search).detectCallable();
            case ENGLISH_US:
                return ((Search_en) search).detectCallable();
            default:
                return ((Search_en) search).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
