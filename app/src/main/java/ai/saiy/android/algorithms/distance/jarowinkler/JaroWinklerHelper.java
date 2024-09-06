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

package ai.saiy.android.algorithms.distance.jarowinkler;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import ai.saiy.android.algorithms.Algorithm;
import ai.saiy.android.algorithms.Resolvable;
import ai.saiy.android.algorithms.contact.ContactNameHelper;
import ai.saiy.android.contacts.Contact;
import ai.saiy.android.custom.CustomCommand;
import ai.saiy.android.custom.CustomCommandContainer;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.AlgorithmicContainer;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;

/**
 * Class to apply the Jaro-Winkler algorithm.
 * <p/>
 * Created by benrandall76@gmail.com on 21/04/2016.
 */
public class JaroWinklerHelper<T> implements Resolvable {
    private static final double CONTACT_DEFAULT_THRESHOLD = 0.77;
    private static final double CONTACT_LEGACY_THRESHOLD = 0.7;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = JaroWinklerHelper.class.getSimpleName();

    private final Context mContext;
    private final ArrayList<String> inputData;
    private final Locale loc;
    private final ArrayList<T> genericData;
    private final ContactNameHelper contactNameHelper;


    /**
     * Constructor
     *
     * @param mContext    the application context
     * @param genericData an array containing generic data
     * @param inputData   an array of Strings containing the input comparison data
     * @param loc         the {@link Locale} extracted from the {@link SupportedLanguage}
     * @param helper      the helper for {@link Contact}
     */
    public JaroWinklerHelper(@NonNull final Context mContext, @NonNull final ArrayList<T> genericData,
                             @NonNull final ArrayList<String> inputData, @NonNull final Locale loc,
                             @Nullable ContactNameHelper helper) {
        this.mContext = mContext;
        this.genericData = genericData;
        this.inputData = inputData;
        this.loc = loc;
        this.contactNameHelper = helper;
    }

    /**
     * Method to iterate through the voice data and attempt to match the user's custom commands
     * using the {@link JaroWinklerDistance} within ranges applied by the associated thresholds constants.
     *
     * @return the highest scoring {@link CustomCommand} or null if thresholds aren't satisfied
     */
    public CustomCommand executeCustomCommand() {

        long then = System.nanoTime();

        final double jwdUpperThreshold = SPH.getJaroWinklerUpper(mContext);

        CustomCommand customCommand = null;
        final ArrayList<CustomCommandContainer> toKeep = new ArrayList<>();
        final JaroWinklerDistance jwd = new JaroWinklerDistance();

        String phrase;
        CustomCommandContainer container;
        double distance;

        int size = genericData.size();

        outer:
        for (int i = 0; i < size; i++) {
            container = (CustomCommandContainer) genericData.get(i);
            phrase = container.getKeyphrase().toLowerCase(loc).trim();

            for (String vd : inputData) {
                vd = vd.toLowerCase(loc).trim();
                distance = jwd.apply(phrase, vd);

                if (distance > jwdUpperThreshold) {

                    container.setUtterance(vd);
                    container.setScore(distance);

                    if (distance == Algorithm.JWD_MAX_THRESHOLD) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Exact match " + phrase);
                        }

                        container.setExactMatch(true);
                        toKeep.add(SerializationUtils.clone(container));
                        break outer;
                    } else {
                        toKeep.add(SerializationUtils.clone(container));
                    }
                }
            }
        }

        if (UtilsList.notNaked(toKeep)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "Have " + toKeep.size() + " phrase matches");
                for (final CustomCommandContainer c : toKeep) {
                    MyLog.i(CLS_NAME, "before order: " + c.getKeyphrase() + " ~ " + c.getScore());
                }
            }

            Collections.sort(toKeep, new Comparator<CustomCommandContainer>() {
                @Override
                public int compare(final CustomCommandContainer c1, final CustomCommandContainer c2) {
                    return Double.compare(c2.getScore(), c1.getScore());
                }
            });

            if (DEBUG) {
                for (final CustomCommandContainer c : toKeep) {
                    MyLog.i(CLS_NAME, "after order: " + c.getKeyphrase() + " ~ " + c.getScore());
                }
                MyLog.i(CLS_NAME, "would select: " + toKeep.get(0).getKeyphrase());
            }

            final CustomCommandContainer ccc = toKeep.get(0);

            final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            customCommand = gson.fromJson(ccc.getSerialised(), CustomCommand.class);
            customCommand.setExactMatch(ccc.isExactMatch());
            customCommand.setUtterance(ccc.getUtterance());
            customCommand.setScore(ccc.getScore());
            customCommand.setAlgorithm(Algorithm.JARO_WINKLER);

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "no custom phrases above threshold");
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return customCommand;
    }

    /**
     * Method to iterate through the given input data and attempt to match the given String data
     * using the {@link JaroWinklerDistance} within ranges applied by the associated thresholds constants.
     *
     * @return an {@link AlgorithmicContainer} or null if thresholds aren't satisfied
     */
    public AlgorithmicContainer executeGeneric() {

        long then = System.nanoTime();

        final double jwdUpperThreshold = SPH.getJaroWinklerUpper(mContext);

        final ArrayList<AlgorithmicContainer> toKeep = new ArrayList<>();
        final JaroWinklerDistance jwd = new JaroWinklerDistance();

        String generic;
        String genericLower;
        AlgorithmicContainer container = null;
        double distance;

        int size = genericData.size();

        outer:
        for (int i = 0; i < size; i++) {
            generic = (String) genericData.get(i);
            genericLower = generic.toLowerCase(loc).trim();

            for (String vd : inputData) {
                vd = vd.toLowerCase(loc).trim();
                distance = jwd.apply(genericLower, vd);

                if (distance > jwdUpperThreshold) {

                    container = new AlgorithmicContainer();
                    container.setInput(vd);
                    container.setGenericMatch(generic);
                    container.setScore(distance);
                    container.setAlgorithm(Algorithm.JARO_WINKLER);
                    container.setParentPosition(i);

                    if (distance == Algorithm.JWD_MAX_THRESHOLD) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Exact match " + genericLower);
                        }

                        container.setExactMatch(true);
                        toKeep.add(container);
                        break outer;
                    } else {
                        container.setExactMatch(false);
                        toKeep.add(container);
                    }
                }
            }
        }

        if (UtilsList.notNaked(toKeep)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "Have " + toKeep.size() + " input matches");
                for (final AlgorithmicContainer c : toKeep) {
                    MyLog.i(CLS_NAME, "before order: " + c.getGenericMatch() + " ~ " + c.getScore());
                }
            }

            Collections.sort(toKeep, new Comparator<AlgorithmicContainer>() {
                @Override
                public int compare(final AlgorithmicContainer c1, final AlgorithmicContainer c2) {
                    return Double.compare(c2.getScore(), c1.getScore());
                }
            });

            if (DEBUG) {
                for (final AlgorithmicContainer c : toKeep) {
                    MyLog.i(CLS_NAME, "after order: " + c.getGenericMatch() + " ~ " + c.getScore());
                }
                MyLog.i(CLS_NAME, "would select: " + toKeep.get(0).getGenericMatch());
            }

            container = toKeep.get(0);

        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "no matches above threshold");
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return container;
    }

    public ArrayList<AlgorithmicContainer> executeContact() {
        long then = System.nanoTime();

        final double jwdUpperThreshold = SPH.getJwdUpperThresholdForContact(mContext)? CONTACT_DEFAULT_THRESHOLD : CONTACT_LEGACY_THRESHOLD;

        final ArrayList<AlgorithmicContainer> toKeep = new ArrayList<>();
        final JaroWinklerDistance jwd = new JaroWinklerDistance();

        String genericLower;
        AlgorithmicContainer container;
        double distance;
        Contact contact;
        double drift = ContactNameHelper.DEFAULT_DRIFT;
        ArrayList<Pair<String, String>> wordGroup = null;

        final int size = genericData.size();

        for (int i = 0; i < size; i++) {
            contact = (Contact) this.genericData.get(i);
            if (contactNameHelper != null) {
                wordGroup = null;
                switch (contact.getWordCount()) {
                    case 1:
                        wordGroup = this.contactNameHelper.getOneWordGroup();
                        drift = ContactNameHelper.DRIFT_ONE;
                        break;
                    case 2:
                        wordGroup = this.contactNameHelper.getTwoWordsGroup();
                        drift = ContactNameHelper.DRIFT_TWO;
                        break;
                    case 3:
                        wordGroup = this.contactNameHelper.getThreeWordsGroup();
                        drift = ContactNameHelper.DRIFT_THREE;
                        break;
                    case 4:
                        wordGroup = this.contactNameHelper.getFourWordsGroup();
                        drift = ContactNameHelper.DRIFT_FOUR;
                        break;
                    default:
                        break;
                }
                if (wordGroup == null || wordGroup.isEmpty()) {
                    continue;
                }
            }

            genericLower = contact.getName().toLowerCase(loc).trim();
            if (contactNameHelper == null) {
                for (String vd : inputData) {
                    vd = vd.toLowerCase(loc).trim();
                    distance = jwd.apply(genericLower, vd);

                    if (distance > jwdUpperThreshold) {
                        container = new AlgorithmicContainer();
                        container.setInput(vd);
                        container.setGenericMatch(genericLower);
                        container.setScore(distance);
                        container.setAlgorithm(Algorithm.JARO_WINKLER);
                        container.setParentPosition(i);

                        if (distance == Algorithm.JWD_MAX_THRESHOLD) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "Exact match " + genericLower);
                            }

                            container.setExactMatch(true);
                            toKeep.add(container);
                        } else {
                            container.setExactMatch(false);
                            toKeep.add(container);
                        }
                    }
                }
            } else {
                for (Pair<String, String> stringPair : wordGroup) {
                    distance = jwd.apply(genericLower, stringPair.first.toLowerCase(loc).trim());

                    if (distance > jwdUpperThreshold) {
                        container = new AlgorithmicContainer();
                        container.setInput(stringPair.second);
                        container.setGenericMatch(genericLower);
                        container.setScore(distance + drift);
                        container.setAlgorithm(Algorithm.JARO_WINKLER);
                        container.setParentPosition(i);

                        if (distance == Algorithm.JWD_MAX_THRESHOLD) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "Exact match " + genericLower);
                            }

                            container.setExactMatch(true);
                            toKeep.add(container);
                        } else {
                            container.setExactMatch(false);
                            toKeep.add(container);
                        }
                    }
                }
            }
        }

        if (UtilsList.notNaked(toKeep)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "Have " + toKeep.size() + " input matches");
                for (final AlgorithmicContainer c : toKeep) {
                    MyLog.i(CLS_NAME, "before order: " + c.getGenericMatch() + " ~ " + c.getScore());
                }
            }

            Collections.sort(toKeep, new Comparator<AlgorithmicContainer>() {
                @Override
                public int compare(final AlgorithmicContainer c1, final AlgorithmicContainer c2) {
                    return Double.compare(c2.getScore(), c1.getScore());
                }
            });

            if (DEBUG) {
                for (final AlgorithmicContainer c : toKeep) {
                    MyLog.i(CLS_NAME, "after order: " + c.getGenericMatch() + " ~ " + c.getScore());
                }
                MyLog.i(CLS_NAME, "would select: " + toKeep.get(0).getGenericMatch());
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "no matches above threshold");
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return toKeep;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     */
    @Override
    public @NonNull Callable<AlgorithmicContainer> genericCallable() {
        if (UtilsList.notNaked(genericData)) {
            final Object object = genericData.get(0);
            if (object instanceof String) {
                return new Callable<AlgorithmicContainer>() {
                    @Override
                    public AlgorithmicContainer call() {
                        return executeGeneric();
                    }
                };
            }
        }
        return new Callable<AlgorithmicContainer>() {
            @Override
            public AlgorithmicContainer call() {
                return null;
            }
        };
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     */
    @Override
    public @NonNull Callable<List<AlgorithmicContainer>> contactCallable() {
        if (UtilsList.notNaked(genericData)) {
            final Object object = genericData.get(0);
            if (object instanceof Contact) {
                return new Callable<List<AlgorithmicContainer>>() {
                    @Override
                    public List<AlgorithmicContainer> call() {
                        return executeContact();
                    }
                };
            }
        }
        return new Callable<List<AlgorithmicContainer>>() {
            @Override
            public List<AlgorithmicContainer> call() {
                return null;
            }
        };
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     */
    @Override

    public @NonNull Callable<CustomCommand> customCommandCallable() {
        if (UtilsList.notNaked(genericData)) {
            final Object object = genericData.get(0);
            if (object instanceof CustomCommandContainer) {
                return new Callable<CustomCommand>() {
                    @Override
                    public CustomCommand call() {
                        return executeCustomCommand();
                    }
                };
            }
        }
        return new Callable<CustomCommand>() {
            @Override
            public CustomCommand call() {
                return null;
            }
        };
    }
}
