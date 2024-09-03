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

package ai.saiy.android.command.translate;

import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;

import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.partial.Partial;
import ai.saiy.android.partial.PartialHelper;

/**
 * Created by benrandall76@gmail.com on 26/04/2016.
 */
public class TranslatePartial implements Callable<Pair<Boolean, Integer>> {

    private final SupportedLanguage sl;
    private final Object translate;
    private Bundle results;

    /**
     * Constructor (used in the {@link PartialHelper}
     * <p>
     * Used when a {@link SaiyResources} object is being handled elsewhere
     *
     * @param sl the {@link SupportedLanguage}
     * @param sr the {@link SaiyResources}
     */
    public TranslatePartial(@NonNull final SaiyResources sr, @NonNull final SupportedLanguage sl) {
        this.sl = sl;

        switch (sl) {

            case ENGLISH:
                translate = new Translate_en(sr, false);
                break;
            case ENGLISH_US:
                translate = new Translate_en(sr, false);
                break;
            default:
                translate = new Translate_en(sr, false);
                break;
        }
    }

    /**
     * Set the partial results to analyse during a recognition loop.
     *
     * @param results the {@link Bundle} of recognition results
     */
    public void setPartialData(@NonNull final Bundle results) {
        this.results = results;
    }

    /**
     * Will loop through an array to detect the command. The initialisation of any localised resources
     * will only take place once in the constructor, which is better for performance.
     * <p>
     * The language to be used is decided by the {@link SupportedLanguage} object
     *
     * @return a {@link Pair} with the first parameter denoting detection and the second the
     * {@link Partial} constant.
     */
    public Pair<Boolean, Integer> detectPartial() {

        switch (sl) {

            case ENGLISH:
                return new Pair<>(((Translate_en) translate).detectPartial(sl.getLocale(), results),
                        Partial.TRANSLATE);
            case ENGLISH_US:
                return new Pair<>(((Translate_en) translate).detectPartial(sl.getLocale(), results),
                        Partial.TRANSLATE);
            default:
                return new Pair<>(((Translate_en) translate).detectPartial(SupportedLanguage.ENGLISH.getLocale(),
                        results), Partial.TRANSLATE);
        }
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     */
    @Override
    public Pair<Boolean, Integer> call() {
        return detectPartial();
    }
}
