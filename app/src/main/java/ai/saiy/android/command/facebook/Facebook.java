package ai.saiy.android.command.facebook;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Facebook implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object facebook;

    public Facebook(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Facebook(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.facebook = new Facebook_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.facebook = new Facebook_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.facebook = new Facebook_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public CommandFacebookValues sort(Context context, ArrayList<String> arrayList) {
        switch (sl) {
            case ENGLISH:
                return Facebook_en.sortFacebook(context, arrayList, sl);
            case ENGLISH_US:
                return Facebook_en.sortFacebook(context, arrayList, sl);
            default:
                return Facebook_en.sortFacebook(context, arrayList, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Facebook_en) facebook).detectCallable();
            case ENGLISH_US:
                return ((Facebook_en) facebook).detectCallable();
            default:
                return ((Facebook_en) facebook).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
