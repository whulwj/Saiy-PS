/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.algorithms.regex;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

import ai.saiy.android.algorithms.Algorithm;
import ai.saiy.android.custom.CustomCommand;
import ai.saiy.android.custom.CustomCommandContainer;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

/**
 * Created by benrandall76@gmail.com on 03/10/2016.
 */

public class ContainsHelper implements Callable<CustomCommand> {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ContainsHelper.class.getSimpleName();

    private final ArrayList<String> inputData;
    private final Locale loc;
    private final ArrayList<CustomCommandContainer> genericData;

    /**
     * Constructor
     *
     * @param genericData an array containing generic data
     * @param inputData   an array of Strings containing the input comparison data
     * @param loc         the {@link Locale} extracted from the {@link SupportedLanguage}
     */
    public ContainsHelper(@NonNull final ArrayList<CustomCommandContainer> genericData,
                          @NonNull final ArrayList<String> inputData, @NonNull final Locale loc) {
        this.genericData = genericData;
        this.inputData = inputData;
        this.loc = loc;
    }

    /**
     * Method to iterate through the voice data and attempt to match the user's custom commands
     * using that are marked as {@link Algorithm#REGEX}
     * specifically {@link ai.saiy.android.api.request.Regex#CONTAINS}
     *
     * @return a {@link CustomCommand} should the regular express be successful, otherwise null
     */
    public CustomCommand executeCustomCommand() {

        long then = System.nanoTime();

        CustomCommand customCommand = null;

        String phrase;
        CustomCommandContainer container;
        int size = genericData.size();

        outer:
        for (int i = 0; i < size; i++) {
            container = genericData.get(i);
            phrase = container.getKeyphrase().toLowerCase(loc).trim();

            for (String vd : inputData) {
                vd = vd.toLowerCase(loc).trim();

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "contains: " + vd + " ~ " + phrase);
                }

                if (vd.contains(phrase)) {

                    final CustomCommandContainer ccc = SerializationUtils.clone(container);
                    final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                    customCommand = gson.fromJson(ccc.getSerialised(), CustomCommand.class);
                    customCommand.setExactMatch(true);
                    customCommand.setUtterance(vd);
                    customCommand.setScore(1.0);
                    customCommand.setAlgorithm(Algorithm.REGEX);

                    break outer;
                }
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return customCommand;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     */
    @Override
    public CustomCommand call() {
        if (UtilsList.notNaked(genericData)) {
            return executeCustomCommand();
        }

        return null;
    }
}