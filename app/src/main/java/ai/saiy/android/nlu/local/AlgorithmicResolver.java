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

package ai.saiy.android.nlu.local;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ai.saiy.android.algorithms.Algorithm;
import ai.saiy.android.algorithms.contact.ContactNameHelper;
import ai.saiy.android.algorithms.distance.jarowinkler.JaroWinklerHelper;
import ai.saiy.android.algorithms.distance.levenshtein.LevenshteinHelper;
import ai.saiy.android.algorithms.doublemetaphone.DoubleMetaphoneHelper;
import ai.saiy.android.algorithms.fuzzy.FuzzyHelper;
import ai.saiy.android.algorithms.metaphone.MetaphoneHelper;
import ai.saiy.android.algorithms.mongeelkan.MongeElkanHelper;
import ai.saiy.android.algorithms.needlemanwunch.NeedlemanWunschHelper;
import ai.saiy.android.algorithms.soundex.SoundexHelper;
import ai.saiy.android.utils.MyLog;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Created by benrandall76@gmail.com on 11/08/2016.
 */

public class AlgorithmicResolver<T> {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = AlgorithmicResolver.class.getSimpleName();

    private final long THREADS_TIMEOUT;
    public static final long THREADS_TIMEOUT_500 = 500L;
    public static final long THREADS_TIMEOUT_2000 = 2000L;

    private final Context mContext;
    private final ArrayList<String> inputData;
    private final ArrayList<T> genericData;
    private final Algorithm[] algorithms;
    private final Locale loc;
    private AlgorithmicContainer algorithmicContainer = null;
    private final boolean precision;

    public AlgorithmicResolver(@NonNull final Context context, @NonNull final Algorithm[] algorithms,
                               @NonNull final Locale loc, @NonNull final ArrayList<String> inputData,
                               @NonNull final ArrayList<T> genericData, final long timeout, final boolean precision) {
        this.mContext = context;
        this.genericData = genericData;
        this.inputData = inputData;
        this.algorithms = algorithms;
        this.loc = loc;
        this.THREADS_TIMEOUT = timeout;
        this.precision = precision;
    }

    public AlgorithmicContainer resolve() {
        final long then = System.nanoTime();
        final List<Callable<AlgorithmicContainer>> callableList = new ArrayList<>(algorithms.length);
        for (final Algorithm algorithm : algorithms) {
            switch (algorithm) {
                case JARO_WINKLER:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: JARO_WINKLER");
                    }

                    callableList.add(new JaroWinklerHelper<>(mContext, genericData, inputData, loc, null).genericCallable());
                    break;
                case LEVENSHTEIN:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: LEVENSHTEIN");
                    }

                    callableList.add(new LevenshteinHelper<>(mContext, genericData, inputData, loc).genericCallable());
                    break;
                case SOUNDEX:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: SOUNDEX");
                    }

                    callableList.add(new SoundexHelper<>(mContext, genericData, inputData, loc, null).genericCallable());
                    break;
                case METAPHONE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: METAPHONE");
                    }

                    callableList.add(new MetaphoneHelper<>(mContext, genericData, inputData, loc, null).genericCallable());
                    break;
                case DOUBLE_METAPHONE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: DOUBLE_METAPHONE");
                    }

                    callableList.add(new DoubleMetaphoneHelper<>(mContext, genericData, inputData, loc, null).genericCallable());
                    break;
                case FUZZY:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: FUZZY");
                    }

                    callableList.add(new FuzzyHelper<>(mContext, genericData, inputData, loc).genericCallable());
                    break;
                case NEEDLEMAN_WUNCH:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: NEEDLEMAN_WUNCH");
                    }

                    callableList.add(new NeedlemanWunschHelper<>(mContext, genericData, inputData, loc).genericCallable());
                    break;
                case MONGE_ELKAN:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: MONGE_ELKAN");
                    }

                    callableList.add(new MongeElkanHelper<>(mContext, genericData, inputData, loc).genericCallable());
                    break;

            }
        }

        final ArrayList<AlgorithmicContainer> algorithmicContainerArray = new ArrayList<>();

        try {
            final List<Future<AlgorithmicContainer>> futures = new ArrayList<>(callableList.size());
            final long timeout = THREADS_TIMEOUT / callableList.size();
            for (Callable<AlgorithmicContainer> callable : callableList) {
                futures.add(Maybe.fromCallable(callable).timeout(timeout, TimeUnit.MILLISECONDS, Schedulers.computation()).subscribeOn(Schedulers.io()).onErrorComplete(throwable -> {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "future: " + throwable.getClass().getSimpleName() + ", " + throwable.getMessage());
                    }
                    return true;
                }).toFuture());
            }

            AlgorithmicContainer algorithmicContainer;
            for (final Future<AlgorithmicContainer> future : futures) {
                algorithmicContainer = future.get();
                if (algorithmicContainer != null) {
                    algorithmicContainerArray.add(algorithmicContainer);
                }
            }
        } catch (final ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "future: ExecutionException" + ", " + e.getMessage());
            }
        } catch (final CancellationException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "future: CancellationException" + ", " + e.getMessage());
            }
        } catch (final InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "future: InterruptedException" + ", " + e.getMessage());
            }
        }

        if (!algorithmicContainerArray.isEmpty()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "algorithms returned " + algorithmicContainerArray.size() + " matches");
                for (final AlgorithmicContainer a : algorithmicContainerArray) {
                    MyLog.i(CLS_NAME, "Potentials: " + a.getAlgorithm().name() + " ~ "
                            + a.getGenericMatch() + " ~ " + a.getInput() + " ~ "
                            + a.getScore());
                }
            }

            AlgorithmicContainer ac;
            final ListIterator<AlgorithmicContainer> itr = algorithmicContainerArray.listIterator();

            while (itr.hasNext()) {
                ac = itr.next();
                if (ac == null) {
                    itr.remove();
                } else {

                    if (ac.isExactMatch()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "exact match: " + ac.getAlgorithm().name() + " ~ "
                                    + ac.getGenericMatch() + " ~ " + ac.getInput() + " ~ "
                                    + ac.getScore());
                        }
                        algorithmicContainer = ac;
                        break;
                    }
                }
            }
        }

        if (algorithmicContainer == null && !algorithmicContainerArray.isEmpty()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "No exact match, but have " + algorithmicContainerArray.size() + " commands");
                for (final AlgorithmicContainer c : algorithmicContainerArray) {
                    MyLog.i(CLS_NAME, "before order: " + c.getGenericMatch() + " ~ " + c.getScore());
                }
            }

            Collections.sort(algorithmicContainerArray, new Comparator<AlgorithmicContainer>() {
                @Override
                public int compare(final AlgorithmicContainer a1, final AlgorithmicContainer a2) {
                    return Double.compare(a2.getScore(), a1.getScore());
                }
            });

            if (DEBUG) {
                for (final AlgorithmicContainer a : algorithmicContainerArray) {
                    MyLog.i(CLS_NAME, "after order: " + a.getGenericMatch() + " ~ " + a.getScore());
                }
            }

            algorithmicContainer = algorithmicContainerArray.get(0);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "match: " + algorithmicContainer.getAlgorithm().name() + " ~ "
                        + algorithmicContainer.getGenericMatch() + " ~ " + algorithmicContainer.getInput()
                        + " ~ " + algorithmicContainer.getScore());
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return algorithmicContainer;
    }

    public ArrayList<AlgorithmicContainer> fetch() {
        ContactNameHelper contactNameHelper;
        if (precision) {
            contactNameHelper = new ContactNameHelper();
            contactNameHelper.buildGroups(inputData);
        } else {
            contactNameHelper = null;
        }
        final long then = System.nanoTime();

        final List<Callable<List<AlgorithmicContainer>>> callableList = new ArrayList<>(algorithms.length);
        for (final Algorithm algorithm : algorithms) {
            switch (algorithm) {
                case JARO_WINKLER:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: JARO_WINKLER");
                    }

                    callableList.add(new JaroWinklerHelper<>(mContext, genericData, inputData, loc, contactNameHelper).contactCallable());
                    break;
                case LEVENSHTEIN:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: LEVENSHTEIN");
                    }

                    callableList.add(new LevenshteinHelper<>(mContext, genericData, inputData, loc).contactCallable());
                    break;
                case SOUNDEX:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: SOUNDEX");
                    }

                    callableList.add(new SoundexHelper<>(mContext, genericData, inputData, loc, contactNameHelper).contactCallable());
                    break;
                case METAPHONE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: METAPHONE");
                    }

                    callableList.add(new MetaphoneHelper<>(mContext, genericData, inputData, loc, contactNameHelper).contactCallable());
                    break;
                case DOUBLE_METAPHONE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: DOUBLE_METAPHONE");
                    }

                    callableList.add(new DoubleMetaphoneHelper<>(mContext, genericData, inputData, loc, contactNameHelper).contactCallable());
                    break;
                case FUZZY:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: FUZZY");
                    }

                    callableList.add(new FuzzyHelper<>(mContext, genericData, inputData, loc).contactCallable());
                    break;
                case NEEDLEMAN_WUNCH:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: NEEDLEMAN_WUNCH");
                    }

                    callableList.add(new NeedlemanWunschHelper<>(mContext, genericData, inputData, loc).contactCallable());
                    break;
                case MONGE_ELKAN:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Running: MONGE_ELKAN");
                    }

                    callableList.add(new MongeElkanHelper<>(mContext, genericData, inputData, loc).contactCallable());
                    break;
            }
        }

        final ArrayList<AlgorithmicContainer> algorithmicContainerArray = new ArrayList<>();
        try {
            final List<Future<List<AlgorithmicContainer>>> futures = new ArrayList<>(callableList.size());
            final long timeout = THREADS_TIMEOUT / callableList.size();
            for (Callable<List<AlgorithmicContainer>> callable : callableList) {
                futures.add(Maybe.fromCallable(callable).timeout(timeout, TimeUnit.MILLISECONDS, Schedulers.computation()).subscribeOn(Schedulers.io()).onErrorReturn(throwable -> {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "future: " + throwable.getClass().getSimpleName() + ", " + throwable.getMessage());
                    }
                    return Collections.emptyList();
                }).toFuture());
            }

            List<AlgorithmicContainer> algorithmicContainers;
            for (final Future<List<AlgorithmicContainer>> future : futures) {
                algorithmicContainers = future.get();
                if (algorithmicContainers == null || algorithmicContainers.isEmpty()) {
                    continue;
                }
                algorithmicContainerArray.addAll(algorithmicContainers);
            }
        } catch (final ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "future: ExecutionException" + ", " + e.getMessage());
            }
        } catch (final CancellationException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "future: CancellationException" + ", " + e.getMessage());
            }
        } catch (final InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "future: InterruptedException" + ", " + e.getMessage());
            }
        }

        if (!algorithmicContainerArray.isEmpty()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "Have " + algorithmicContainerArray.size() + " potentials");
                for (final AlgorithmicContainer a : algorithmicContainerArray) {
                    MyLog.i(CLS_NAME, "before order: " + a.getGenericMatch() + " ~ " + a.getScore());
                }
            }

            Collections.sort(algorithmicContainerArray, new Comparator<AlgorithmicContainer>() {
                @Override
                public int compare(final AlgorithmicContainer a1, final AlgorithmicContainer a2) {
                    return Double.compare(a2.getScore(), a1.getScore());
                }
            });

            if (DEBUG) {
                for (final AlgorithmicContainer a : algorithmicContainerArray) {
                    MyLog.i(CLS_NAME, "after order: " + a.getGenericMatch() + " ~ " + a.getScore());
                }
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return algorithmicContainerArray;
    }
}
