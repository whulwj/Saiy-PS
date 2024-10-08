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

package ai.saiy.android.command.battery;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;

/**
 * Helper class to direct any voice data to the correct localisation to resolve the command.
 * <p>
 * Performance is key, so initialising localised resource Strings needs to be done as few times as
 * possible, whenever possible.
 * <p>
 * Created by benrandall76@gmail.com on 17/04/2016.
 */
public class Battery implements Callable<ArrayList<Pair<CC, Float>>> {

    private final SupportedLanguage sl;
    private Object battery;

    /**
     * Constructor
     * <p>
     * Used by the {@link Callable} to construct the data ready for {@link Callable#call()}
     *
     * @param sr         the {@link SaiyResources}
     * @param sl         the {@link SupportedLanguage}
     * @param voiceData  the array of voice data
     * @param confidence the array of confidence scores
     */
    public Battery(@NonNull final SaiyResources sr, @NonNull final SupportedLanguage sl,
                   @NonNull final ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = sl;

        switch (sl) {

            case ENGLISH:
                battery = new Battery_en(sr, sl, voiceData, confidence);
                break;
            case ENGLISH_US:
                battery = new Battery_en(sr, sl, voiceData, confidence);
                break;
            default:
                battery = new Battery_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    /**
     * Constructor (used during a command)
     * <p>
     * Used when we will be constructing and managing our own {@link SaiyResources} object
     *
     * @param sl the {@link SupportedLanguage}
     */
    public Battery(@NonNull final SupportedLanguage sl) {
        this.sl = sl;
    }

    /**
     * Method to identify the required battery information
     *
     * @param ctx       the application context
     * @param voiceData ArrayList<String> of voice data
     * @return a {@link CommandBatteryValues} object containing the required parameters
     */
    public CommandBatteryValues fetch(@NonNull final Context ctx, @NonNull final ArrayList<String> voiceData) {

        switch (sl) {

            case ENGLISH:
                return Battery_en.sortBattery(ctx, voiceData, sl);
            case ENGLISH_US:
                return Battery_en.sortBattery(ctx, voiceData, sl);
            default:
                return Battery_en.sortBattery(ctx, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    /**
     * Used by the {@link Callable} in {@link Callable#call()}
     *
     * @return an array list containing {@link Pair} of {@link CC} and {@link Float} confidence scores
     */
    public ArrayList<Pair<CC, Float>> detectCallable() {

        switch (sl) {

            case ENGLISH:
                return ((Battery_en) battery).detectCallable();
            case ENGLISH_US:
                return ((Battery_en) battery).detectCallable();
            default:
                return ((Battery_en) battery).detectCallable();
        }
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     */
    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
