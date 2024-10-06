package ai.saiy.android.command.financial;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class StockQuote implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object stockQuote;

    public StockQuote(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public StockQuote(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.stockQuote = new StockQuote_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.stockQuote = new StockQuote_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.stockQuote = new StockQuote_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((StockQuote_en) stockQuote).detectCallable();
            case ENGLISH_US:
                return ((StockQuote_en) stockQuote).detectCallable();
            default:
                return ((StockQuote_en) stockQuote).detectCallable();
        }
    }

    public @NonNull ArrayList<String> sortQuery(@NonNull Context context, @NonNull ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return StockQuote_en.sortQuery(context, voiceData, sl);
            case ENGLISH_US:
                return StockQuote_en.sortQuery(context, voiceData, sl);
            default:
                return StockQuote_en.sortQuery(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
