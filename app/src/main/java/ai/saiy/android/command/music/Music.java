package ai.saiy.android.command.music;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Music implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object music;

    public Music(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Music(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.music = new Music_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.music = new Music_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.music = new Music_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public CommandMusicValues sortMusic(@NonNull Context context, @NonNull ArrayList<String> voiceData) {
        switch (this.sl) {
            case ENGLISH:
                return Music_en.sortMusic(context, voiceData, this.sl);
            case ENGLISH_US:
                return Music_en.sortMusic(context, voiceData, this.sl);
            default:
                return Music_en.sortMusic(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (this.sl) {
            case ENGLISH:
                return ((Music_en) this.music).detectCallable();
            case ENGLISH_US:
                return ((Music_en) this.music).detectCallable();
            default:
                return ((Music_en) this.music).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
