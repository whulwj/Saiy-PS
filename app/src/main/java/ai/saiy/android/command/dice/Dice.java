package ai.saiy.android.command.dice;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Dice implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object dice;

    public Dice(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Dice(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.dice = new Dice_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.dice = new Dice_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.dice = new Dice_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public long sortDice(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Dice_en.sortDice(context, voiceData, sl);
            case ENGLISH_US:
                return Dice_en.sortDice(context, voiceData, sl);
            default:
                return Dice_en.sortDice(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Dice_en) dice).detectCallable();
            case ENGLISH_US:
                return ((Dice_en) dice).detectCallable();
            default:
                return ((Dice_en) dice).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
