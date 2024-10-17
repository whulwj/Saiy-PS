package ai.saiy.android.command.notification;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public final class Notifications implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object notifications;

    public Notifications(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.notifications = new Notifications_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.notifications = new Notifications_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.notifications = new Notifications_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Notifications_en) notifications).detectCallable();
            case ENGLISH_US:
                return ((Notifications_en) notifications).detectCallable();
            default:
                return ((Notifications_en) notifications).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
