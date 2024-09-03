package ai.saiy.android.command.superuser;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Superuser implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object superuser;

    public Superuser(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Superuser(ai.saiy.android.localisation.SaiyResources aVar, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.superuser = new Superuser_en(aVar, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.superuser = new Superuser_en(aVar, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.superuser = new Superuser_en(aVar, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public CommandSuperuserValues sort(Context context, ArrayList<String> voiceData) {
        switch (this.sl) {
            case ENGLISH:
                return Superuser_en.sortSuperuser(context, voiceData, this.sl);
            case ENGLISH_US:
                return Superuser_en.sortSuperuser(context, voiceData, this.sl);
            default:
                return Superuser_en.sortSuperuser(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (this.sl) {
            case ENGLISH:
                return ((Superuser_en) this.superuser).detectCallable();
            case ENGLISH_US:
                return ((Superuser_en) this.superuser).detectCallable();
            default:
                return ((Superuser_en) this.superuser).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
