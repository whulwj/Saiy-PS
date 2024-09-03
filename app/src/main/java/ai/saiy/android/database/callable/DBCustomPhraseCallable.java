package ai.saiy.android.database.callable;

import android.content.Context;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.custom.CustomPhrase;
import ai.saiy.android.custom.CustomPhraseHelper;

public class DBCustomPhraseCallable implements Callable<ArrayList<CustomPhrase>> {
    private final Context mContext;

    public DBCustomPhraseCallable(Context context) {
        this.mContext = context;
    }

    @Override
    public ArrayList<CustomPhrase> call() {
        return new CustomPhraseHelper().getCustomPhrases(mContext);
    }
}
