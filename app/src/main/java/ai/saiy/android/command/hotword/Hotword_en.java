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

package ai.saiy.android.command.hotword;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

/**
 * Helper class to resolve hotword commands.
 * <p/>
 * Created by benrandall76@gmail.com on 06/04/2016.
 */
public class Hotword_en {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Hotword_en.class.getSimpleName();

    private static String start;
    private static String stop;
    private static String listening;
    private static String hotword;
    private static String hot_word;
    private static String toggle;
    private static String turn;
    private static String on;
    private static String off;
    private static String word_switch;
    private static String enable;
    private static String disable;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Hotword_en(@NonNull final SaiyResources sr, @NonNull final SupportedLanguage sl,
                      @NonNull final ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = sl;
        this.voiceData = voiceData;
        this.confidence = confidence;

        if (listening == null) {
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
        listening = sr.getString(R.string.listening);
        hotword = sr.getString(R.string.hotword);
        hot_word = sr.getString(R.string.hot_word);
        enable = sr.getString(R.string.enable);
        disable = sr.getString(R.string.disable);
        start = sr.getString(R.string.start);
        stop = sr.getString(R.string.stop);
        toggle = sr.getString(R.string.toggle);
        turn = sr.getString(R.string.turn);
        on = sr.getString(R.string.on);
        off = sr.getString(R.string.off);
        word_switch = sr.getString(R.string.word_switch);
    }

    /**
     * Iterate through the voice data array to see if we can match the command.
     * <p/>
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

                if (vdLower.contains(hot_word) || vdLower.contains(hotword) || vdLower.contains(listening)) {

                    wordsList = vdLower.trim().split("\\s+");

                    if (wordsList.length > 6) {

                        for (int j = 0; j < 7; j++) {
                            word = wordsList[j];
                            if (word.contains(start) || word.contains(stop)
                                    || word.contains(enable) || word.contains(disable)
                                    || word.contains(toggle) || word.contains(turn)
                                    || word.contains(on) || word.contains(off)
                                    || word.contains(word_switch)) {

                                toReturn.add(new Pair<>(CC.COMMAND_HOTWORD, confidence[i]));
                                break;
                            }
                        }

                    } else {
                        toReturn.add(new Pair<>(CC.COMMAND_HOTWORD, confidence[i]));
                    }
                }
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "hotword: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }

        return toReturn;
    }
}
