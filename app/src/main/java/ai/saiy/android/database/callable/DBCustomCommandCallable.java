/*
 * Copyright (c) 2017. Saiy® Ltd. All Rights Reserved.
 *
 * Unauthorised copying of this file, via any medium is strictly prohibited. Proprietary and confidential
 */

package ai.saiy.android.database.callable;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.custom.CustomCommandContainer;
import ai.saiy.android.custom.CustomCommandHelper;

/**
 * Created by benrandall76@gmail.com on 27/01/2017.
 */

public class DBCustomCommandCallable implements Callable<ArrayList<CustomCommandContainer>> {

    private final Context mContext;

    public DBCustomCommandCallable(@NonNull final Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public ArrayList<CustomCommandContainer> call() {
        return new CustomCommandHelper().getCustomCommands(mContext);
    }
}
