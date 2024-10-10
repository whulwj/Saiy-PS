package ai.saiy.android.command.web;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public final class Web implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object web;

    public Web(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Web(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.web = new Web_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.web = new Web_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.web = new Web_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Web_en) web).detectCallable();
            case ENGLISH_US:
                return ((Web_en) web).detectCallable();
            default:
                return ((Web_en) web).detectCallable();
        }
    }

    public ArrayList<String> getUrls(@NonNull Context context, @NonNull ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Web_en.getUrls(context, voiceData, sl);
            case ENGLISH_US:
                return Web_en.getUrls(context, voiceData, sl);
            default:
                return Web_en.getUrls(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public String getSearchTerm(@NonNull Context context, @NonNull ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Web_en.getSearchTerm(context, voiceData, sl);
            case ENGLISH_US:
                return Web_en.getSearchTerm(context, voiceData, sl);
            default:
                return Web_en.getSearchTerm(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
