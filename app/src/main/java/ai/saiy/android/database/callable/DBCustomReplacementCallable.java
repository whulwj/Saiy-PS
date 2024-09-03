package ai.saiy.android.database.callable;

import android.content.Context;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.custom.CustomReplacement;
import ai.saiy.android.custom.CustomReplacementHelper;

public class DBCustomReplacementCallable implements Callable<ArrayList<CustomReplacement>> {
    private final Context mContext;

    public DBCustomReplacementCallable(Context context) {
        this.mContext = context;
    }

    @Override
    public ArrayList<CustomReplacement> call() {
        return new CustomReplacementHelper().getCustomReplacements(mContext);
    }
}
