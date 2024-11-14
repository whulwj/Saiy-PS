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

package ai.saiy.android.partial;

import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;

import ai.saiy.android.command.alexa.AlexaPartial;
import ai.saiy.android.command.cancel.CancelPartial;
import ai.saiy.android.command.translate.TranslatePartial;
import ai.saiy.android.command.translate.provider.TranslationProvider;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Class to detect commands within the partial voice results from the recognition provider. We can
 * use any early detection to initialise and/or prefetch resources, so should the full command be
 * resolved later in the final results, it should, in theory, execute more quickly.
 * <p/>
 * Created by benrandall76@gmail.com on 23/04/2016.
 */
public class PartialHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = PartialHelper.class.getSimpleName();

    private static final long THREADS_TIMEOUT = 100L;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final List<Callable<Pair<Boolean, Integer>>> callableList = new ArrayList<>();
    private final CancelPartial cancelPartial;
    private volatile TranslatePartial translatePartial;
    private volatile AlexaPartial alexaPartial;
    private final IPartial iPartial;

    /**
     * Constructor
     * <p/>
     * Initialises the Strings used to analyse the partial results for triggers we need to react to.
     * The {@link SaiyResources} are released here.
     *
     * @param mContext the application context
     * @param sl       the {@link SupportedLanguage}
     * @param iPartial the {@link IPartial} listener
     */
    public PartialHelper(@NonNull final Context mContext, @NonNull final SupportedLanguage sl,
                         @NonNull final IPartial iPartial) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "Constructor");
        }

        this.iPartial = iPartial;

        final SaiyResources sr = new SaiyResources(mContext, sl);
        cancelPartial = new CancelPartial(sr, sl);
        callableList.add(cancelPartial);

        switch (SPH.getDefaultTranslationProvider(mContext)) {
            case TranslationProvider.TRANSLATION_PROVIDER_BING:
                translatePartial = new TranslatePartial(sr, sl);
                callableList.add(translatePartial);
                break;
        }
        if (ai.saiy.android.amazon.TokenHelper.hasToken(mContext)) {
            this.alexaPartial = new AlexaPartial(sr, sl);
            callableList.add(alexaPartial);
        }

        sr.reset();
    }

    /**
     * Utility method to detect the phrase during a recognition loop. Handling the initialisation
     * of localised resources can be slow, so we need to do this only once.
     *
     * @param partialResults the bundle of partial results
     */
    public void isPartial(@NonNull final Bundle partialResults) {

        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

                final long then = System.nanoTime();
                cancelPartial.setPartialData(partialResults);

                if (translatePartial != null) {
                    translatePartial.setPartialData(partialResults);
                }
                if (alexaPartial != null) {
                    alexaPartial.setPartialData(partialResults);
                }

                final List<Single<Pair<Boolean, Integer>>> singleList = new ArrayList<>(callableList.size());
                for (Callable<Pair<Boolean, Integer>> callable : callableList) {
                    singleList.add(Single.fromCallable(callable));
                }
                final Consumer<Throwable> onError =  new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        if (!DEBUG) {
                            return;
                        }
                        if (throwable instanceof ExecutionException) {
                            MyLog.w(CLS_NAME, "call: ExecutionException");
                        } else if (throwable instanceof CancellationException) {
                            MyLog.w(CLS_NAME, "call: CancellationException");
                        } else if (throwable instanceof InterruptedException) {
                            MyLog.w(CLS_NAME, "call: InterruptedException");
                        } else {
                            MyLog.w(CLS_NAME, "call: " + throwable.getClass().getSimpleName() + ", " + throwable.getMessage());
                        }
                    }
                };
                final Disposable disposable = Flowable.fromIterable(singleList)
                        .parallel().runOn(Schedulers.computation())
                        .map(new Function<Single<Pair<Boolean, Integer>>, Pair<Boolean, Integer>>() {
                            @Override
                            public Pair<Boolean, Integer> apply(@NonNull Single<Pair<Boolean, Integer>> single) {
                                return single.doOnError(onError).blockingGet();
                            }
                        })
                        .collect(Collector.of(ArrayList::new, ArrayList::add, new BinaryOperator<ArrayList<Pair<Boolean, Integer>>>() {
                            @Override
                            public ArrayList<Pair<Boolean, Integer>> apply(ArrayList<Pair<Boolean, Integer>> left, ArrayList<Pair<Boolean, Integer>> right) {
                                final ArrayList<Pair<Boolean, Integer>> resultList = new ArrayList<>(left.size() + right.size());
                                resultList.addAll(left);
                                resultList.addAll(right);
                                return resultList;
                            }
                        }))
                        .timeout(THREADS_TIMEOUT, TimeUnit.MILLISECONDS, Schedulers.computation())
                        .subscribe(new Consumer<ArrayList<Pair<Boolean, Integer>>>() {
                            @Override
                            public void accept(ArrayList<Pair<Boolean, Integer>> resultList) {
                                for (Pair<Boolean, Integer> result : resultList) {
                                    if (result.first) {
                                        switch (result.second) {
                                            case Partial.CANCEL:
                                                iPartial.onCancelDetected();
                                                break;
                                            case Partial.TRANSLATE:
                                                iPartial.onTranslateDetected();
                                                break;
                                            case Partial.ALEXA:
                                                iPartial.onAlexaDetected();
                                                break;
                                        }
                                    }
                                }
                            }
                        }, onError, new Action() {
                            @Override
                            public void run() {
                                if (DEBUG) {
                                    MyLog.getElapsed(CLS_NAME, then);
                                }
                            }
                        }, compositeDisposable);
            }
        });
    }

    public void shutdown() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "shutdown");
        }

        try {

            if (!compositeDisposable.isDisposed()) {
                compositeDisposable.dispose();
            }
        } catch (final CancellationException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "shutdownNow: CancellationException");
                e.printStackTrace();
            }
        } finally {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "shutdown: complete");
            }
        }
    }

    public boolean isShutdown() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "isShutdown");
        }
        return compositeDisposable.isDisposed();
    }
}
