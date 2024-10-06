package ai.saiy.android.command.agenda;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Agenda implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object agenda;

    public Agenda(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.agenda = new Agenda_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.agenda = new Agenda_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.agenda = new Agenda_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Agenda_en) agenda).detectCallable();
            case ENGLISH_US:
                return ((Agenda_en) agenda).detectCallable();
            default:
                return ((Agenda_en) agenda).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
