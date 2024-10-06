package ai.saiy.android.command.audio;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Audio implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object audio;

    public Audio(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Audio(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.audio = new Audio_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.audio = new Audio_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.audio = new Audio_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public CommandAudioValues sortAudio(@NonNull Context context, @NonNull ArrayList<String> voiceData) {
        switch (this.sl) {
            case ENGLISH:
                return Audio_en.sortAudio(context, voiceData, this.sl);
            case ENGLISH_US:
                return Audio_en.sortAudio(context, voiceData, this.sl);
            default:
                return Audio_en.sortAudio(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (this.sl) {
            case ENGLISH:
                return ((Audio_en) this.audio).detectCallable();
            case ENGLISH_US:
                return ((Audio_en) this.audio).detectCallable();
            default:
                return ((Audio_en) this.audio).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
