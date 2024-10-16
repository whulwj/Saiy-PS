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
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import ai.saiy.android.command.definition.Define;
import ai.saiy.android.command.dice.Dice;
import ai.saiy.android.command.donate.Donate;
import ai.saiy.android.command.driving.Driving;
import ai.saiy.android.command.easter_egg.EasterEgg;
import ai.saiy.android.command.emotion.Emotion;
import ai.saiy.android.command.facebook.Facebook;
import ai.saiy.android.command.financial.StockQuote;
import ai.saiy.android.command.foursquare.Foursquare;
import ai.saiy.android.command.hardware.Hardware;
import ai.saiy.android.command.help.Help;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.command.horoscope.Horoscope;
import ai.saiy.android.command.hotword.Hotword;
import ai.saiy.android.command.location.address.Location;
import ai.saiy.android.command.location.vehicle.locate.LocateVehicle;
import ai.saiy.android.command.location.vehicle.parked.ParkedVehicle;
import ai.saiy.android.command.maintenance.Restart;
import ai.saiy.android.command.maintenance.Shutdown;
import ai.saiy.android.command.music.Music;
import ai.saiy.android.command.navigation.Navigation;
import ai.saiy.android.command.note.Note;
import ai.saiy.android.command.orientation.Orientation;
import ai.saiy.android.command.pardon.Pardon;
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

/**
 * The beginnings of a local and very deliberate language model to detect commands. This won't scale.
 * <p/>
 * We loop through every speech occurrence as just relying on the first may not be sufficient,
 * regardless of any associated confidence score.
 * <p/>
 * A thread pool is used so we can concurrently analyse the results with each of our local 'language
 * models'. Each of these return an ArrayList, which for every detection will contain a Pair with
 * the corresponding command constant {@link CC} and confidence score.
 * <p/>
 * Once all threads have finished, the results are combined into a single Array List and ordered by
 * their associated confidence score.
 * <p/>
 * This array list is then examined in {@link FrequencyAnalysis} for frequency occurrences and outliers
 * in an attempt to establish the command and weighted by both frequency and confidence, in order to
 * establish the most likely command.
 * <p/>
 * Created by benrandall76@gmail.com on 09/02/2016.
 */
public final class Resolve {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Resolve.class.getSimpleName();

    private static final long THREADS_TIMEOUT = 1000L;

    private final ArrayList<String> voiceData;
    private final List<Callable<ArrayList<Pair<CC, Float>>>> callableList;

    /**
     * Constructor
     *
     * @param mContext   the application context
     * @param voiceData  ArrayList<String> containing the voice data
     * @param confidence float array of confidence scores
     * @param sl         the {@link SupportedLanguage} we are using to analyse the voice data.
     *                   This is not necessarily the Locale of the device, as the user may be
     *                   multi-lingual and have set a custom recognition language in a launcher short-cut.
     */
    public Resolve(@NonNull final Context mContext, @NonNull final ArrayList<String> voiceData,
                   @NonNull final float[] confidence, @NonNull final SupportedLanguage sl) {
        this.voiceData = voiceData;

        final SaiyResources sr = new SaiyResources(mContext, sl);
        callableList = new ArrayList<>();
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
        callableList.add(new Time(sr, sl, voiceData, confidence));
        callableList.add(new Hardware(sr, sl, voiceData, confidence));
        callableList.add(new Toast(sr, sl, voiceData, confidence));
        callableList.add(new Clipboard(sr, sl, voiceData, confidence));
        callableList.add(new Settings(sr, sl, voiceData, confidence));
        callableList.add(new Music(sr, sl, voiceData, confidence));
        callableList.add(new Somersault(sr, sl, voiceData, confidence));
        callableList.add(new Orientation(sr, sl, voiceData, confidence));
        callableList.add(new Redial(sr, sl, voiceData, confidence));
        callableList.add(new CallBack(sr, sl, voiceData, confidence));
        callableList.add(new Shutdown(sr, sl, voiceData, confidence));
        callableList.add(new Restart(sr, sl, voiceData, confidence));
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
        callableList.add(new Help(sr, sl, voiceData, confidence));
        callableList.add(new Show(sr, sl, voiceData, confidence));
        callableList.add(new ChatBot(sr, sl, voiceData, confidence));
        callableList.add(new Driving(sr, sl, voiceData, confidence));
        callableList.add(new Facebook(sr, sl, voiceData, confidence));
        callableList.add(new Twitter(sr, sl, voiceData, confidence));
        callableList.add(new Foursquare(sr, sl, voiceData, confidence));
        callableList.add(new Taxi(sr, sl, voiceData, confidence));
        callableList.add(new Dice(sr, sl, voiceData, confidence));
        callableList.add(new Card(sr, sl, voiceData, confidence));
        callableList.add(new Coin(sr, sl, voiceData, confidence));
        callableList.add(new Donate(sr, sl, voiceData, confidence));
        callableList.add(new Alexa(sr, sl, voiceData, confidence));
        sr.reset();
    }

    /**
     * Constructor
     * <p>
     * Used only for intercepting Google Now commands, as we exclude some due to the output being identical
     *
     * @param mContext   the application context
     * @param voiceData  ArrayList<String> containing the voice data
     * @param confidence float array of confidence scores
     * @param sl         the {@link SupportedLanguage} we are using to analyse the voice data.
     *                   This is not necessarily the Locale of the device, as the user may be
     *                   multi-lingual and have set a custom recognition language in a launcher short-cut.
     * @param interim    true to only include commands with a 'finite' length, false otherwise
     */
    public Resolve(@NonNull final Context mContext, @NonNull final ArrayList<String> voiceData,
                   @NonNull final float[] confidence, @NonNull final SupportedLanguage sl, final boolean interim) {
        this.voiceData = voiceData;

        final SaiyResources sr = new SaiyResources(mContext, sl);
        callableList = new ArrayList<>();
        callableList.add(new Cancel(sr, sl, voiceData, confidence));
        callableList.add(new Pardon(sr, sl, voiceData, confidence));
        callableList.add(new SongRecognition(sr, sl, voiceData, confidence));
        callableList.add(new Battery(sr, sl, voiceData, confidence));
        callableList.add(new Emotion(sr, sl, voiceData, confidence));
        callableList.add(new Hotword(sr, sl, voiceData, confidence));
        callableList.add(new VocalRecognition(sr, sl, voiceData, confidence));

        if (!interim) {
            callableList.add(new Spell(sr, sl, voiceData, confidence));
            callableList.add(new UserName(sr, sl, voiceData, confidence));
            callableList.add(new Tasker(sr, sl, voiceData, confidence));
            callableList.add(new WolframAlpha(sr, sl, voiceData, confidence));
        }

        // callableList.add(new Translate(sr, sl, voiceData, confidence));

        sr.reset();
    }

    /**
     * Create an ArrayList<Integer> containing the frequency of commands.
     *
     * @return an ArrayList<Integer> of all recognised commands
     */
    public ArrayList<Pair<CC, Float>> resolve() {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "analyse: voiceData: " + voiceData.size() + " : " + voiceData);
            MyLog.d(CLS_NAME, "analyse: availableProcessors: " + Runtime.getRuntime().availableProcessors());
        }

        final long then = System.nanoTime();

        final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        final ArrayList<ArrayList<Pair<CC, Float>>> pairList = new ArrayList<>();

        try {

            final List<Future<ArrayList<Pair<CC, Float>>>> futures = executorService.invokeAll(callableList,
                    THREADS_TIMEOUT, TimeUnit.MILLISECONDS);

            for (final Future<ArrayList<Pair<CC, Float>>> future : futures) {
                pairList.add(future.get());
            }

        } catch (final ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "future: ExecutionException");
                e.printStackTrace();
            }
        } catch (final CancellationException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "future: CancellationException");
                e.printStackTrace();
            }
        } catch (final InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "future: InterruptedException");
                e.printStackTrace();
            }
        } finally {
            executorService.shutdown();
        }

        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();

        if (!pairList.isEmpty()) {

            for (final ArrayList<Pair<CC, Float>> pairs : pairList) {
                toReturn.addAll(pairs);
            }

            if (!toReturn.isEmpty()) {
                Collections.sort(toReturn, new Comparator<Pair<CC, Float>>() {
                    @Override
                    public int compare(final Pair<CC, Float> p1, final Pair<CC, Float> p2) {
                        return Float.compare(p2.second, p1.second);
                    }
                });
            }

            if (DEBUG) {
                for (final Pair<CC, Float> pairs : toReturn) {
                    MyLog.i(CLS_NAME, "command: " + pairs.first.name() + " ~ " + pairs.second);
                }
            }
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return toReturn;
    }
}
