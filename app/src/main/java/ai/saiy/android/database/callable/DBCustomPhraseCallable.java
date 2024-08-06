package ai.saiy.android.database.callable;

import android.content.Context;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.custom.CustomPhraseHelper;

public class DBCustomPhraseCallable implements Callable<ArrayList<Object>> {
    private final Context mContext;

    public DBCustomPhraseCallable(Context context) {
        this.mContext = context;
    }

    @Override
    public ArrayList<Object> call() throws Exception {
        return (ArrayList) new CustomPhraseHelper().getCustomPhrases(mContext);
    }
}
