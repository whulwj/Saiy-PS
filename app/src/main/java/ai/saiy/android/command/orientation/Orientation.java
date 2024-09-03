package ai.saiy.android.command.orientation;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Orientation implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object orientation;

    public Orientation(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Orientation(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.orientation = new Orientation_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.orientation = new Orientation_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.orientation = new Orientation_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public @NonNull CommandOrientationValues detectOrientation(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Orientation_en.detectOrientation(context, voiceData, sl);
            case ENGLISH_US:
                return Orientation_en.detectOrientation(context, voiceData, sl);
            default:
                return Orientation_en.detectOrientation(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Orientation_en) orientation).detectCallable();
            case ENGLISH_US:
                return ((Orientation_en) orientation).detectCallable();
            default:
                return ((Orientation_en) orientation).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
