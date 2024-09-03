package ai.saiy.android.database.callable;

import android.content.Context;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.custom.CustomNickname;
import ai.saiy.android.custom.CustomNicknameHelper;

public class DBCustomNicknameCallable implements Callable<ArrayList<CustomNickname>> {

    private final Context mContext;

    public DBCustomNicknameCallable(Context context) {
        this.mContext = context;
    }

    @Override
    public ArrayList<CustomNickname> call() {
        return new CustomNicknameHelper().getCustomNicknames(mContext);
    }
}
