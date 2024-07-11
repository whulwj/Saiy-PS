package ai.saiy.android.command.contact;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Contact implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object contact;

    public Contact(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Contact(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.contact = new Contact_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.contact = new Contact_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.contact = new Contact_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public CommandContactValues sort(Context context, ArrayList<String> voiceData) {
        switch (this.sl) {
            case ENGLISH:
                return Contact_en.sortContact(context, voiceData, sl);
            case ENGLISH_US:
                return Contact_en.sortContact(context, voiceData, sl);
            default:
                return Contact_en.sortContact(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Contact_en) contact).detectCallable();
            case ENGLISH_US:
                return ((Contact_en) contact).detectCallable();
            default:
                return ((Contact_en) contact).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
