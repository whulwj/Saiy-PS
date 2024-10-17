package ai.saiy.android.command.float_command;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public final class FloatCommands implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object floatCommands;

    public FloatCommands(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.floatCommands = new FloatCommands_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.floatCommands = new FloatCommands_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.floatCommands = new FloatCommands_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((FloatCommands_en) floatCommands).detectCallable();
            case ENGLISH_US:
                return ((FloatCommands_en) floatCommands).detectCallable();
            default:
                return ((FloatCommands_en) floatCommands).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
