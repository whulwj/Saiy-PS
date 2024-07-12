package ai.saiy.android.command.alexa;

import android.os.Bundle;
import android.util.Pair;

import java.util.concurrent.Callable;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.partial.Partial;

public class AlexaPartial implements Callable<Pair<Boolean, Integer>> {
    private final SupportedLanguage sl;
    private final Object alexa;
    private Bundle results;

    public AlexaPartial(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.alexa = new Alexa_en(sr, false);
                break;
            case ENGLISH_US:
                this.alexa = new Alexa_en(sr, false);
                break;
            default:
                this.alexa = new Alexa_en(sr, false);
                break;
        }
    }

    public Pair<Boolean, Integer> detectPartial() {
        switch (sl) {
            case ENGLISH:
                return new Pair<>(((Alexa_en) alexa).detectPartial(sl.getLocale(), results), Partial.ALEXA);
            case ENGLISH_US:
                return new Pair<>(((Alexa_en) alexa).detectPartial(sl.getLocale(), results), Partial.ALEXA);
            default:
                return new Pair<>(((Alexa_en) alexa).detectPartial(SupportedLanguage.ENGLISH.getLocale(), results), Partial.ALEXA);
        }
    }

    public void setPartialData(Bundle results) {
        this.results = results;
    }

    @Override
    public Pair<Boolean, Integer> call() throws Exception {
        return detectPartial();
    }
}
