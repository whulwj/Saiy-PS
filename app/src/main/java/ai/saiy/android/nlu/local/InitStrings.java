/*
 * Copyright (c) 2017. Saiy® Ltd. All Rights Reserved.
 *
 * Unauthorised copying of this file, via any medium is strictly prohibited. Proprietary and confidential
 */

package ai.saiy.android.nlu.local;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import ai.saiy.android.R;
import ai.saiy.android.command.agenda.Agenda;
import ai.saiy.android.command.alarm.Alarm;
import ai.saiy.android.command.alexa.Alexa;
import ai.saiy.android.command.application.foreground.Foreground;
import ai.saiy.android.command.application.kill.Kill;
import ai.saiy.android.command.application.launch.Launch;
import ai.saiy.android.command.audio.Audio;
import ai.saiy.android.command.battery.Battery;
import ai.saiy.android.command.calculate.Calculate;
import ai.saiy.android.command.calendar.Calendar;
import ai.saiy.android.command.call.CallBack;
import ai.saiy.android.command.call.Redial;
import ai.saiy.android.command.cancel.Cancel;
import ai.saiy.android.command.card.Card;
import ai.saiy.android.command.chatbot.ChatBot;
import ai.saiy.android.command.clipboard.Clipboard;
import ai.saiy.android.command.coin.Coin;
import ai.saiy.android.command.contact.Contact;
import ai.saiy.android.command.date.Date;
import ai.saiy.android.command.definition.Define;
import ai.saiy.android.command.dice.Dice;
import ai.saiy.android.command.donate.Donate;
import ai.saiy.android.command.driving.Driving;
import ai.saiy.android.command.easter_egg.EasterEgg;
import ai.saiy.android.command.emotion.Emotion;
import ai.saiy.android.command.facebook.Facebook;
import ai.saiy.android.command.financial.StockQuote;
import ai.saiy.android.command.float_command.FloatCommands;
import ai.saiy.android.command.foursquare.Foursquare;
import ai.saiy.android.command.hardware.Hardware;
import ai.saiy.android.command.help.Help;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.command.home.Home;
import ai.saiy.android.command.horoscope.Horoscope;
import ai.saiy.android.command.hotword.Hotword;
import ai.saiy.android.command.joke.Joke;
import ai.saiy.android.command.location.address.Location;
import ai.saiy.android.command.location.vehicle.locate.LocateVehicle;
import ai.saiy.android.command.location.vehicle.parked.ParkedVehicle;
import ai.saiy.android.command.maintenance.Restart;
import ai.saiy.android.command.maintenance.Shutdown;
import ai.saiy.android.command.music.Music;
import ai.saiy.android.command.navigation.Navigation;
import ai.saiy.android.command.note.Note;
import ai.saiy.android.command.notification.Notifications;
import ai.saiy.android.command.orientation.Orientation;
import ai.saiy.android.command.pardon.Pardon;
import ai.saiy.android.command.remember.Remember;
import ai.saiy.android.command.search.Search;
import ai.saiy.android.command.settings.application.ApplicationSettings;
import ai.saiy.android.command.settings.system.Settings;
import ai.saiy.android.command.show.Show;
import ai.saiy.android.command.sms.Sms;
import ai.saiy.android.command.somersault.Somersault;
import ai.saiy.android.command.songrecognition.SongRecognition;
import ai.saiy.android.command.spell.Spell;
import ai.saiy.android.command.superuser.Superuser;
import ai.saiy.android.command.tasker.Tasker;
import ai.saiy.android.command.taxi.Taxi;
import ai.saiy.android.command.time.Time;
import ai.saiy.android.command.timer.Timer;
import ai.saiy.android.command.toast.Toast;
import ai.saiy.android.command.translate.Translate;
import ai.saiy.android.command.twitter.Twitter;
import ai.saiy.android.command.uninstall.Uninstall;
import ai.saiy.android.command.username.UserName;
import ai.saiy.android.command.vocalrecognition.VocalRecognition;
import ai.saiy.android.command.weather.Weather;
import ai.saiy.android.command.web.Web;
import ai.saiy.android.command.wolframalpha.WolframAlpha;
import ai.saiy.android.localisation.SaiyResources;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.schedulers.Schedulers;

public final class InitStrings {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = InitStrings.class.getSimpleName();

    private static final long THREADS_TIMEOUT = 1000L;

    private final List<Callable<ArrayList<Pair<CC, Float>>>> callableList;

    private static String testString;

    /**
     * Constructor
     *
     * @param mContext the application context
     */
    public InitStrings(@NonNull final Context mContext) {

        callableList = new ArrayList<>();

        if (testString != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "test string initialised. Returning");
            }
            return;
        }

        final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(SPH.getVRLocale(mContext));
        final SaiyResources sr = new SaiyResources(mContext, sl);

        testString = sr.getString(R.string.test_string);

        final ArrayList<String> voiceData = new ArrayList<>();
        final float[] confidence = new float[]{};

        callableList.add(new Cancel(sr, sl, voiceData, confidence));
        callableList.add(new Spell(sr, sl, voiceData, confidence));
        callableList.add(new Translate(sr, sl, voiceData, confidence));
        callableList.add(new Pardon(sr, sl, voiceData, confidence));
        callableList.add(new UserName(sr, sl, voiceData, confidence));
        callableList.add(new SongRecognition(sr, sl, voiceData, confidence));
        callableList.add(new Battery(sr, sl, voiceData, confidence));
        callableList.add(new WolframAlpha(sr, sl, voiceData, confidence));
        callableList.add(new Tasker(sr, sl, voiceData, confidence));
        callableList.add(new Emotion(sr, sl, voiceData, confidence));
        callableList.add(new Hotword(sr, sl, voiceData, confidence));
        callableList.add(new VocalRecognition(sr, sl, voiceData, confidence));
        callableList.add(new Contact(sr, sl, voiceData, confidence));
        callableList.add(new Navigation(sr, sl, voiceData, confidence));
        callableList.add(new Date(sr, sl, voiceData, confidence));
        callableList.add(new Time(sr, sl, voiceData, confidence));
        callableList.add(new Hardware(sr, sl, voiceData, confidence));
        callableList.add(new Toast(sr, sl, voiceData, confidence));
        callableList.add(new Clipboard(sr, sl, voiceData, confidence));
        callableList.add(new Settings(sr, sl, voiceData, confidence));
        callableList.add(new Music(sr, sl, voiceData, confidence));
        callableList.add(new Home(sr, sl, voiceData, confidence));
        callableList.add(new Somersault(sr, sl, voiceData, confidence));
        callableList.add(new Orientation(sr, sl, voiceData, confidence));
        callableList.add(new Redial(sr, sl, voiceData, confidence));
        callableList.add(new CallBack(sr, sl, voiceData, confidence));
        callableList.add(new Shutdown(sr, sl, voiceData, confidence));
        callableList.add(new Restart(sr, sl, voiceData, confidence));
        callableList.add(new Remember(sr, sl, voiceData, confidence));
        callableList.add(new Uninstall(sr, sl, voiceData, confidence));
        callableList.add(new Define(sr, sl, voiceData, confidence));
        callableList.add(new ApplicationSettings(sr, sl, voiceData, confidence));
        callableList.add(new Calculate(sr, sl, voiceData, confidence));
        callableList.add(new Kill(sr, sl, voiceData, confidence));
        callableList.add(new Launch(sr, sl, voiceData, confidence));
        callableList.add(new Location(sr, sl, voiceData, confidence));
        callableList.add(new LocateVehicle(sr, sl, voiceData, confidence));
        callableList.add(new ParkedVehicle(sr, sl, voiceData, confidence));
        callableList.add(new Foreground(sr, sl, voiceData, confidence));
        callableList.add(new Audio(sr, sl, voiceData, confidence));
        callableList.add(new Horoscope(sr, sl, voiceData, confidence));
        callableList.add(new Agenda(sr, sl, voiceData, confidence));
        callableList.add(new Weather(sr, sl, voiceData, confidence));
        callableList.add(new StockQuote(sr, sl, voiceData, confidence));
        callableList.add(new Note(sr, sl, voiceData, confidence));
        callableList.add(new Search(sr, sl, voiceData, confidence));
        callableList.add(new Alarm(sr, sl, voiceData, confidence));
        callableList.add(new Calendar(sr, sl, voiceData, confidence));
        callableList.add(new Timer(sr, sl, voiceData, confidence));
        callableList.add(new Sms(sr, sl, voiceData, confidence));
        callableList.add(new Superuser(sr, sl, voiceData, confidence));
        callableList.add(new Web(sr, sl, voiceData, confidence));
        callableList.add(new EasterEgg(sr, sl, voiceData, confidence));
        callableList.add(new Joke(sr, sl, voiceData, confidence));
        callableList.add(new Help(sr, sl, voiceData, confidence));
        callableList.add(new Show(sr, sl, voiceData, confidence));
        callableList.add(new ChatBot(sr, sl, voiceData, confidence));
        callableList.add(new Notifications(sr, sl, voiceData, confidence));
        callableList.add(new Driving(sr, sl, voiceData, confidence));
        callableList.add(new Facebook(sr, sl, voiceData, confidence));
        callableList.add(new Twitter(sr, sl, voiceData, confidence));
        callableList.add(new Foursquare(sr, sl, voiceData, confidence));
        callableList.add(new Taxi(sr, sl, voiceData, confidence));
        callableList.add(new FloatCommands(sr, sl, voiceData, confidence));
        callableList.add(new Dice(sr, sl, voiceData, confidence));
        callableList.add(new Card(sr, sl, voiceData, confidence));
        callableList.add(new Coin(sr, sl, voiceData, confidence));
        callableList.add(new Donate(sr, sl, voiceData, confidence));
        callableList.add(new Alexa(sr, sl, voiceData, confidence));
        sr.reset();
    }

    public void init() {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "init");
        }

        final long then = System.nanoTime();
        final int size = callableList.size();
        final List<Single<ArrayList<Pair<CC, Float>>>> singleList = new ArrayList<>(size);
        for (Callable<ArrayList<Pair<CC, Float>>> callable : callableList) {
            singleList.add(Single.fromCallable(callable));
        }
        final Consumer<Throwable> onError = new Consumer<Throwable>() {
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
                .map(new Function<Single<ArrayList<Pair<CC, Float>>>, Byte>() {
                    @Override
                    public Byte apply(@NonNull Single<ArrayList<Pair<CC, Float>>> single) {
                        return (byte) (single.doOnError(onError).blockingGet() != null ? 1 : 0);
                    }
                })
                .reduce(new BiFunction<Byte, Byte, Byte>() {
                    @Override
                    public Byte apply(Byte left, Byte right) throws Throwable {
                        return (byte) (left + right);
                    }
                })
                .timeout(THREADS_TIMEOUT, TimeUnit.MILLISECONDS, Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .subscribe(Functions.emptyConsumer(), onError, new Action() {
                    @Override
                    public void run() {
                        if (DEBUG) {
                            MyLog.getElapsed(CLS_NAME, then);
                        }
                    }
                });
    }
}
