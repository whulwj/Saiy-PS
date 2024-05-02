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

package ai.saiy.android.command.tasker;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

/**
 * Helper class to resolve Tasker commands.
 * <p>
 * Created by benrandall76@gmail.com on 06/04/2016.
 */
public class Tasker_en {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Tasker_en.class.getSimpleName();

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    private static String tasker;
    private static String task_;

    /**
     * Constructor
     * <p>
     * Used by a {@link Callable} to prepare everything that is need when
     * {@link Callable#call()} is executed with the {@link SaiyResources} managed elsewhere.
     *
     * @param sr         the {@link SaiyResources}
     * @param sl         the {@link SupportedLanguage}
     * @param voiceData  the array of voice data
     * @param confidence the array of confidence scores
     */
    public Tasker_en(@NonNull final SaiyResources sr, @NonNull final SupportedLanguage sl,
                     @NonNull final ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = sl;
        this.voiceData = voiceData;
        this.confidence = confidence;

        if (tasker == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "strings initialised");
            }
        }
    }

    private static void initStrings(@NonNull final SaiyResources sr) {
        tasker = sr.getString(R.string.tasker);
        task_ = sr.getString(R.string.task_);
    }

    /**
     * Iterate through the voice data array to see if we can match the command.
     * <p>
     * Note - As the speech array will never contain more than ten entries, to consider the static
     * nature and performance issues here, perhaps implementing a matcher, would probably be overkill.
     *
     * @return an Array list of Pairs containing the {@link CC} and float confidence
     */
    public ArrayList<Pair<CC, Float>> detectCallable() {

        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();

        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence)
                && voiceData.size() == confidence.length) {

            String word;
            String[] wordsList;
            final Locale loc = sl.getLocale();

            String vdLower;
            int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                vdLower = voiceData.get(i).toLowerCase(loc).trim();

                if (vdLower.contains(task_) && vdLower.contains(tasker)) {

                    wordsList = vdLower.trim().split("\\s+");

                    if (wordsList.length > 4) {

                        for (int j = 0; j < 5; j++) {
                            word = wordsList[j];
                            if (word.contains(task_) || word.contains(tasker)) {
                                toReturn.add(new Pair<>(CC.COMMAND_TASKER, confidence[i]));
                                break;
                            }
                        }

                    } else {
                        toReturn.add(new Pair<>(CC.COMMAND_TASKER, confidence[i]));
                    }
                }
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "Tasker: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }

        return toReturn;
    }

    /**
     * Static method.
     * <p>
     * Iterate through the voice data array to return only the voice data associated with the
     * required command.
     *
     * @param ctx       the application context
     * @param voiceData ArrayList<String> containing the voice data
     * @param sl        the {@link SupportedLanguage}
     * @return an array list containing only the required data
     */
    public static ArrayList<String> sortTasker(@NonNull final Context ctx, @NonNull final ArrayList<String> voiceData,
                                               @NonNull final SupportedLanguage sl) {

        final long then = System.nanoTime();
        final Locale loc = sl.getLocale();
        final ArrayList<String> toReturn = new ArrayList<>();

        final SaiyResources sr = new SaiyResources(ctx, sl);

        if (task_ == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "strings initialised");
            }
        }

        final String called_ = sr.getString(R.string.called_);
        final String named_ = sr.getString(R.string.named_);
        sr.reset();

        String vdLower;
        String[] separated;
        String separatedString;
        for (final String vd : voiceData) {
            vdLower = vd.toLowerCase(loc).trim();

            if (vdLower.contains(task_) && vdLower.contains(tasker)) {
                separated = vdLower.split(task_);

                if (separated.length > 1) {

                    separatedString = separated[1].trim();

                    if (separatedString.startsWith(called_)) {

                        separated = separatedString.split(called_);

                        if (separated.length > 1) {
                            toReturn.add(separated[1].trim());
                        } else {
                            toReturn.add(separatedString);
                        }

                    } else if (separatedString.startsWith(named_)) {

                        separated = separatedString.split(named_);

                        if (separated.length > 1) {
                            toReturn.add(separated[1].trim());
                        } else {
                            toReturn.add(separatedString);
                        }

                    } else {
                        toReturn.add(separatedString);
                    }
                }
            }
        }

        if (!toReturn.isEmpty()) {
            final Set<String> deduplicated = new LinkedHashSet<>(toReturn);
            toReturn.clear();
            toReturn.addAll(deduplicated);
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return toReturn;
    }
}
