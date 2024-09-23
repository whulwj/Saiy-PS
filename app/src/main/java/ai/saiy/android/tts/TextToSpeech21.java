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

package ai.saiy.android.tts;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IllformedLocaleException;
import java.util.ListIterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.regex.Pattern;

import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.audio.AudioCompression;
import ai.saiy.android.audio.SaiyAudioTrack;
import ai.saiy.android.cache.speech.SpeechCacheResult;
import ai.saiy.android.database.DBSpeech;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.SelfAwareCache;
import ai.saiy.android.service.helper.SelfAwareConditions;
import ai.saiy.android.service.helper.SelfAwareParameters;
import ai.saiy.android.tts.attributes.Gender;
import ai.saiy.android.tts.helper.SaiyVoice;
import ai.saiy.android.tts.helper.TTSDefaults;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsParcelable;
import ai.saiy.android.utils.UtilsString;

/**
 * Due to misbehaving voice engines, it's necessary to subclass the TTS object here and handle extra
 * eventualities that have caused many crashes along the way.
 * <p/>
 * Additionally, handling the try/catch inside the methods keeps other classes tidy and more readable.
 * <p/>
 * Created by benrandall76@gmail.com on 13/03/2016.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class TextToSpeech21 extends SaiyTextToSpeech {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = TextToSpeech21.class.getSimpleName();

    private volatile SaiyAudioTrack audioTrack;

    private volatile SaiyProgressListener listener;
    private volatile Set<SaiyVoice> saiyVoiceSet;
    private volatile Set<Voice> defaultVoiceSet;

    /**
     * Constructor
     *
     * @param context the application context
     * @param listener the {@link OnInitListener} to monitor the tts engine
     */
    public TextToSpeech21(final Context context, final OnInitListener listener) {
        super(context, listener);
        init();
    }

    /**
     * Constructor
     *
     * @param context the application context
     * @param listener the {@link OnInitListener} to monitor the tts engine
     * @param engine   package name of a requested engine
     */
    public TextToSpeech21(final Context context, final OnInitListener listener, final String engine) {
        super(context, listener, engine);
        init();
    }

    /**
     * Set post constructor stuff up
     */
    private void init() {
        initialiseAudioTrack();
        setAttributes();
    }

    /**
     * Set the standard audio attributes for the Text to Speech stream
     */
    private void setAttributes() {
        setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC).build());
    }

    /**
     * Initialise our {@link SaiyAudioTrack} object, which will be used for streaming stored utterances.
     */
    private void initialiseAudioTrack() {
        audioTrack = getAudioTrack();
    }

    /**
     * Helper method to double check the returned {@link SaiyAudioTrack} object hasn't been released
     * elsewhere.
     *
     * @return the {@link SaiyAudioTrack} object, or null it the creation process failed.
     */
    private SaiyAudioTrack getAudioTrack() {
        if (audioTrack == null || audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            audioTrack = SaiyAudioTrack.getSaiyAudioTrack();
            audioTrack.setListener(listener);
        }
        return audioTrack;
    }

    @Override
    protected Gender getGender() {
        final SaiyVoice voice = getBoundSaiyVoice();
        if (voice != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "speak: have sound effect: voice.getGender(): " + voice.getGender().name());
            }
            return voice.getGender();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "speak: have sound effect: voice null");
            }
        }
        return Gender.UNDEFINED;
    }

    /**
     * Method to check if synthesis exists in the {@link DBSpeech} for the pending utterance.
     *
     * @param utterance the pending utterance
     * @param voiceName the name of the current Text to Speech Voice
     * @return true if the audio data is available to stream. False otherwise.
     */
    private boolean synthesisAvailable(@NonNull final String utterance, @NonNull final String voiceName) {
        final DBSpeech dbSpeech = new DBSpeech(mContext);
        final int rate = getSpeechRate();
        final int pitch = getPitch();
        if (rate != SPH.getTTSRate(mContext) || pitch != SPH.getTTSPitch(mContext)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "synthesisAvailable: user defaults have changed");
            }
            SPH.setTTSRate(mContext, rate);
            SPH.setTTSPitch(mContext, pitch);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                    dbSpeech.deleteTable();
                }
            });
            return false;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "synthesisAvailable: user defaults unchanged");
        }

        final SpeechCacheResult speechCacheResult = dbSpeech.getBytes(getInitialisedEngine(),
                voiceName, utterance);

        if (speechCacheResult.isSuccess()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "synthesisAvailable: true");
            }
            return true;
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "synthesisAvailable: getBytes failed or speech does not exist");
            }

            if (speechCacheResult.getRowId() > -1) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "synthesisAvailable: speech does not exist");
                }
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                        dbSpeech.deleteEntry(speechCacheResult.getRowId());
                    }
                });
            }
        }

        return false;
    }

    /**
     * Method to check if synthesis exists in the {@link DBSpeech} for the pending utterance. If so,
     * the byte[] is pulled from the database and streamed using the {@link AudioTrack}, rather
     * than than via the Text to Speech engine.
     *
     * @param utterance the pending utterance
     * @param params    the {@link SelfAwareParameters}
     * @return true if the audio data is available to stream. False otherwise.
     */
    @Override
    protected boolean canSynthesise(@NonNull final String utterance, @NonNull final SelfAwareParameters params) {
        if (getAudioTrack() == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "canSynthesise: false: audioTrack null");
            }
            return false;
        }
        if (!SPH.isCacheSpeech(mContext)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "canSynthesise: false: user declined");
            }
            return false;
        }
        if (!TTSDefaults.isApprovedVoice(getInitialisedEngine())) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "canSynthesise: false: not approved engine");
            }
            return false;
        }
        if (utterance.length() >= SelfAwareCache.MAX_UTTERANCE_CHARS) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "canSynthesise: false: length > MAX_UTTERANCE_CHARS");
            }
            return false;
        }
        if (utterance.matches(SaiyRequestParams.SILENCE)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "canSynthesise: false: silence");
            }
            return false;
        }
        if (!UtilsString.notNaked(getInitialisedEngine())) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "canSynthesise: false: getBytes engine naked");
            }
            return false;
        }

        final SaiyVoice voice = getBoundSaiyVoice();
        if (voice == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "canSynthesise: false: saiyVoice null");
            }
            return false;
        }
        final DBSpeech dbSpeech = new DBSpeech(mContext);
        final int rate = getSpeechRate();
        final int pitch = getPitch();
        if (rate != SPH.getTTSRate(mContext) || pitch != SPH.getTTSPitch(mContext)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "canSynthesise: user defaults have changed");
            }
            SPH.setTTSRate(mContext, rate);
            SPH.setTTSPitch(mContext, pitch);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                    dbSpeech.deleteTable();
                }
            });
            return false;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "canSynthesise: user defaults unchanged");
        }

        final SpeechCacheResult speechCacheResult = dbSpeech.getBytes(getInitialisedEngine(),
                voice.getName(), utterance);

        if (speechCacheResult.isSuccess()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "canSynthesise: true");
            }

            final byte[] uncompressedBytes = AudioCompression.decompressBytes(mContext,
                    speechCacheResult.getCompressedBytes(), speechCacheResult.getRowId());

            if (UtilsList.notNaked(uncompressedBytes)) {
                startSynthesis(params, uncompressedBytes);
                return true;
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "canSynthesise: getBytes empty or null");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "canSynthesise: getBytes failed or speech does not exist");
            }

            if (speechCacheResult.getRowId() > -1) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                        dbSpeech.deleteEntry(speechCacheResult.getRowId());
                    }
                });
            }
        }

        return false;
    }

    /**
     * Begin streaming the byte[] of pcm audio data via the {@link AudioTrack} object
     *
     * @param uncompressedBytes to stream
     * @param params            the {@link SelfAwareParameters}
     */
    private void startSynthesis(@NonNull final SelfAwareParameters params,
                                @NonNull final byte[] uncompressedBytes) {
        audioTrack.setListener(listener);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                audioTrack.setVolume(params.getVolume());
                audioTrack.enqueue(uncompressedBytes, params.getUtteranceId());
            }
        });
    }

    private SaiyVoice getUserDefaultSaiyVoice() {
        final String userDefaultSaiyVoiceString = SPH.getDefaultTTSVoice(mContext);
        if (UtilsString.notNaked(userDefaultSaiyVoiceString)) {
            return UtilsParcelable.unmarshall(userDefaultSaiyVoiceString, SaiyVoice.CREATOR);
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "userDefaultSaiyVoiceString: naked");
            }
        }

        return null;
    }

    private SaiyVoice getEngineDefaultSaiyVoice() {

        final Voice voice = getDefaultVoice();

        if (voice != null) {
            final SaiyVoice saiyVoice = new SaiyVoice(voice);
            saiyVoice.setEngine(getInitialisedEngine());
            saiyVoice.setGender(saiyVoice.getName());

            if (DEBUG) {
                MyLog.i(CLS_NAME, "getEngineDefaultSaiyVoice: setting Gender: " + saiyVoice.getGender().name());
            }

            return saiyVoice;
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getEngineDefaultSaiyVoice: voice null");
            }
            return null;
        }
    }

    @Override
    public Voice getVoice() {
        try {
            return super.getVoice();
        } catch (Throwable t) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getVoice:" + t.getClass().getSimpleName() + ", " + t.getMessage());
            }
        }
        return null;
    }

    private SaiyVoice getBoundSaiyVoice() {
        final Voice voice = getVoice();

        if (voice != null) {
            final SaiyVoice saiyVoice = new SaiyVoice(voice);
            saiyVoice.setEngine(getInitialisedEngine());
            saiyVoice.setGender(saiyVoice.getName());
            return saiyVoice;
        } else {
            return null;
        }
    }

    @Override
    public Set<Voice> getVoices() {
        final long then = System.nanoTime();

        if (defaultVoiceSet == null || defaultVoiceSet.isEmpty()) {
            defaultVoiceSet = super.getVoices();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getVoices: already prepared");
            }
        }

        if (DEBUG) {
            MyLog.getElapsed("getVoices", then);
        }

        return defaultVoiceSet;
    }

    private Set<SaiyVoice> getSaiyVoices() {
        final long then = System.nanoTime();
        final Set<Voice> voiceSet = getVoices();

        if (saiyVoiceSet == null || saiyVoiceSet.size() != voiceSet.size()) {
            saiyVoiceSet = SaiyVoice.getSaiyVoices(voiceSet, getInitialisedEngine());
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getSaiyVoices: already prepared");
            }
        }

        if (DEBUG) {
            MyLog.getElapsed("getSaiyVoices", then);
        }
        return saiyVoiceSet;
    }

    private int getPitch() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getPitch");
        }
        final int pitch = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.TTS_DEFAULT_PITCH, 100);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "pitch: " + pitch);
        }
        return pitch;
    }

    private int getSpeechRate() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getSpeechRate");
        }
        final int rate = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.TTS_DEFAULT_RATE, 100);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "pitch: " + rate);
        }
        return rate;
    }

    @Override
    public boolean isSpeaking() {

        if (audioTrack != null && audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isSpeaking: audioTrack STATE_INITIALIZED");
            }

            if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING
                    || audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "isSpeaking: audioTrack PLAYSTATE_PLAYING/PLAYSTATE_PAUSED");
                }
                return true;
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "isSpeaking: audioTrack not playing");
                }
            }
        }

        return super.isSpeaking();
    }

    @Override
    public int setOnUtteranceProgressListener(final UtteranceProgressListener listener) {
        this.listener = (SaiyProgressListener) listener;
        return super.setOnUtteranceProgressListener(listener);
    }

    /**
     * Automatically select the user's default voice.
     *
     * @param language   the {@link Locale} language
     * @param region     the {@link Locale} region
     * @param conditions the {@link SelfAwareConditions}
     * @param params     the {@link SelfAwareParameters}
     */
    private void setDefaultVoice(@NonNull final String language, @NonNull final String region,
                                 @NonNull final SelfAwareConditions conditions,
                                 @NonNull final SelfAwareParameters params) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setDefaultVoice");
        }

        final Pair<SaiyVoice, Locale> voicePair = new TTSVoice(mContext, this, language, region, conditions, params, null)
                .buildVoice();

        if (voicePair.first != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "setDefaultVoice: Setting Voice: " + voicePair.first);
            }

            final SaiyVoice saiyVoice = new SaiyVoice(voicePair.first);
            saiyVoice.setEngine(getInitialisedEngine());
            saiyVoice.setGender(saiyVoice.getName());

            final String base64String = UtilsParcelable.parcelable2String(saiyVoice);

            if (DEBUG) {
                MyLog.i(CLS_NAME, "setDefaultVoice base64String: " + base64String);
            }

            SPH.setDefaultTTSVoice(mContext, base64String);

        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "setDefaultVoice: Unable to establish a default voice");
            }
        }
    }

    /**
     * Set the voice of the TTS object.
     *
     * @param language   the {@link Locale} language
     * @param region     the {@link Locale} region
     * @param conditions the {@link SelfAwareConditions}
     * @param params     the {@link SelfAwareParameters}
     * @return one of {@link #SUCCESS} or {@link #ERROR}
     */
    @Override
    protected int setVoice21(@NonNull final String language, @NonNull final String region,
                           @NonNull final SelfAwareConditions conditions, @NonNull final SelfAwareParameters params) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setVoice21");
        }

        final boolean isNetworkAvailable = params.shouldNetwork();

        SaiyVoice userDefaultSaiyVoice = null;

        try {

            if (Condition.CONDITION_TRANSLATION == conditions.getCondition()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoice21: CONDITION_TRANSLATION");
                }
            } else {
                userDefaultSaiyVoice = getUserDefaultSaiyVoice();

                if (userDefaultSaiyVoice == null) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            setDefaultVoice(language, region, conditions, params);
                        }
                    });
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "setVoice21: userDefaultSaiyVoice: " + userDefaultSaiyVoice);
                    }

                    boolean exists = false;
                    for (final SaiyVoice voice : getSaiyVoices()) {
                        if (userDefaultSaiyVoice.getName().matches("(?i)" + Pattern.quote(voice.getName()))) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "setVoice21: defaultSaiyVoice: no longer exists");
                        }
                        userDefaultSaiyVoice = null;
                        SPH.setDefaultTTSVoice(mContext, null);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                setDefaultVoice(language, region, conditions, params);
                            }
                        });
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "setVoice21: defaultSaiyVoice: exists");
                        }
                    }
                }
            }

            SaiyVoice boundSaiyVoice = getBoundSaiyVoice();

            if (boundSaiyVoice == null) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoice21: boundSaiyVoice null");
                }
                return setVoiceDeprecated(language, region, conditions);
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoice21: boundSaiyVoice: " + boundSaiyVoice);
                    MyLog.i(CLS_NAME, "setVoice21: boundSaiyVoice matches default: " + boundSaiyVoice.equals(userDefaultSaiyVoice));
                    MyLog.i(CLS_NAME, "setVoice21: boundSaiyVoice Locale: " + boundSaiyVoice.getLocale().toString());
                    MyLog.i(CLS_NAME, "setVoice21: Required Locale: " + language + " ~ " + region);
                }
            }

            if (Condition.CONDITION_TRANSLATION == conditions.getCondition()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoice21: CONDITION_TRANSLATION");
                }
            } else {
                if (userDefaultSaiyVoice != null) {
                    if (!boundSaiyVoice.equals(userDefaultSaiyVoice)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "setVoice21: userDefaultSaiyVoice engine: " + userDefaultSaiyVoice.getEngine());
                            MyLog.i(CLS_NAME, "setVoice21: boundSaiyVoice engine: " + boundSaiyVoice.getEngine());
                        }

                        if (userDefaultSaiyVoice.getEngine().matches(boundSaiyVoice.getEngine())) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "setVoice21: userDefaultSaiyVoice & boundSaiyVoice engines match");
                            }

                            if (isNetworkAvailable) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "setVoice21: engines match: network available: setting default");
                                }
                                setVoice(userDefaultSaiyVoice);
                                boundSaiyVoice = getBoundSaiyVoice();
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "setVoice21: engines match: no network");
                                }

                                if (!userDefaultSaiyVoice.isNetworkConnectionRequired()) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "setVoice21: engines match: userDefaultSaiyVoice network not needed");
                                    }
                                    setVoice(userDefaultSaiyVoice);
                                    boundSaiyVoice = getBoundSaiyVoice();
                                } else {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "setVoice21: engines match: userDefaultSaiyVoice requires network");
                                    }

                                    final String utterance = conditions.getUtterance();

                                    if (UtilsString.notNaked(utterance) && !utterance.matches(SaiyRequestParams.SILENCE)) {

                                        if (synthesisAvailable(conditions.getUtterance(), userDefaultSaiyVoice.getName())) {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "setVoice21: synthesis cached: SUCCESS");
                                            }
                                            setVoice(userDefaultSaiyVoice);
                                            return SUCCESS;
                                        } else {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "setVoice21: no synthesis cache");
                                            }
                                        }
                                    } else {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "setVoice21: engine warm up only");
                                        }
                                        return SUCCESS;
                                    }
                                }
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "setVoice21: userDefaultSaiyVoice & boundSaiyVoice engines don't match.");
                            }

                            saiyVoiceSet = null;
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "setVoice21: boundSaiyVoice and userDefaultSaiyVoice match");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "setVoice21: defaultSaiyVoice: null");
                    }
                    SPH.setDefaultTTSVoice(mContext, null);
                }
            }

            if (!UtilsLocale.localesLanguageMatch(boundSaiyVoice.getLocale(), new Locale(language, region))) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoice21: locales don't match");
                }
                return resolveVoice(language, region, conditions, params, boundSaiyVoice);
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoice21: locales match");
                }

                if (boundSaiyVoice.isNetworkConnectionRequired()) {
                    if (isNetworkAvailable) {
                        if (SPH.getNetworkSynthesis(mContext)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "setVoice21: network required: SUCCESS");
                            }
                            return SUCCESS;
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "setVoice21: user no network");
                            }
                            return resolveVoice(language, region, conditions, params, boundSaiyVoice);
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "setVoice21: network unavailable");
                        }

                        final String utterance = conditions.getUtterance();

                        if (UtilsString.notNaked(utterance) && !utterance.matches(SaiyRequestParams.SILENCE)) {

                            if (synthesisAvailable(conditions.getUtterance(), boundSaiyVoice.getName())) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "setVoice21: synthesis cached: SUCCESS");
                                }
                                return SUCCESS;
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "setVoice21: no synthesis cache");
                                }
                                return resolveVoice(language, region, conditions, params, boundSaiyVoice);
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "setVoice21: engine warm up only");
                            }
                            return SUCCESS;
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "setVoice21: no network required: SUCCESS");
                    }
                    return SUCCESS;
                }
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "setVoice21 NullPointerException");
                e.printStackTrace();
            }
        } catch (final MissingResourceException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "setVoice21 MissingResourceException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "setVoice21 Exception");
                e.printStackTrace();
            }
            if (e instanceof IllformedLocaleException) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "setVoice21 IllformedLocaleException");
                }
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "setVoice21: falling back to setVoiceDeprecated");
        }

        return setVoiceDeprecated(language, region, conditions);
    }

    /**
     * Attempt to resolve the voice that most suits the conditions and the user's preferences.
     *
     * @param language   the {@link Locale} language
     * @param region     the {@link Locale} region
     * @param conditions the {@link SelfAwareConditions}
     * @param params     the {@link SelfAwareParameters}
     * @return one of {@link #SUCCESS} or {@link #ERROR}
     */
    private int resolveVoice(@NonNull final String language, @NonNull final String region,
                             @NonNull final SelfAwareConditions conditions, @NonNull final SelfAwareParameters params,
                             @Nullable final SaiyVoice currentVoice) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resolveVoice");
        }

        final Pair<SaiyVoice, Locale> voicePair = new TTSVoice(mContext, this, language, region, conditions, params,
                currentVoice).buildVoice();

        if (voicePair.first != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "resolveVoice: Setting Voice: " + voicePair.first);
                MyLog.i(CLS_NAME, "resolveVoice: Setting Voice loc: " + voicePair.first.getLocale());
                try {
                    MyLog.i(CLS_NAME, "resolveVoice: Setting Voice: isLanguageAvailable: "
                            + resolveSuccess(isLanguageAvailable(new Locale(voicePair.first.getLocale().getLanguage(),
                            voicePair.first.getLocale().getCountry()))));
                } catch (final MissingResourceException e) {
                    MyLog.w(CLS_NAME, "MissingResourceException: isLanguageAvailable failed");
                    e.printStackTrace();
                }
            }

            return super.setVoice(voicePair.first);
        } else {
            if (voicePair.second != null) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "resolveVoice: Setting Locale deprecated");
                }
                return setVoiceDeprecated(voicePair.second.getLanguage(), voicePair.second.getCountry(), conditions);
            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "resolveVoice: voicePair.second null: falling back");
                }
                return resolveSuccess(super.setLanguage(new Locale(language, region)));
            }
        }
    }

    @Override
    public void shutdown() {
        if (audioTrack != null) {
            switch (audioTrack.getPlayState()) {
                case AudioTrack.PLAYSTATE_PLAYING:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stop: PLAYSTATE_PLAYING");
                    }
                    audioTrack.stop(true);
                    audioTrack.flush();
                    audioTrack.release();
                case AudioTrack.PLAYSTATE_PAUSED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stop: PLAYSTATE_PAUSED");
                    }
                    audioTrack.stop(true);
                    audioTrack.flush();
                    audioTrack.release();
                case AudioTrack.PLAYSTATE_STOPPED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stop: PLAYSTATE_STOPPED");
                    }
                    audioTrack.flush();
                    audioTrack.release();
                    break;
            }
        }

        super.shutdown();
    }

    @Override
    public int stop() {
        if (audioTrack != null) {
            switch (audioTrack.getPlayState()) {
                case AudioTrack.PLAYSTATE_PLAYING:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stop: PLAYSTATE_PLAYING");
                    }
                    audioTrack.stop(true);
                    return SUCCESS;
                case AudioTrack.PLAYSTATE_PAUSED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stop: PLAYSTATE_PAUSED");
                    }
                    audioTrack.stop(true);
                    return SUCCESS;
                case AudioTrack.PLAYSTATE_STOPPED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stop: PLAYSTATE_STOPPED");
                    }
                    break;
            }
        }

        try {
            return super.stop();
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stop: NullPointerException");
                e.printStackTrace();
            }
        }

        return ERROR;
    }

    /**
     * Store the initialised Text to Speech Engine
     */
    @Override
    public void initialised() {
        super.initialised();

/*        if (DEBUG) {
            getInfo();
        }*/
    }


    /**
     * Examine TTS objects in an overly verbose way. Debugging only.
     */
    private void getInfo() {
        MyLog.i(CLS_NAME, "getQuickInfo");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    MyLog.v(CLS_NAME, "getEngineDefaultSaiyVoice: " + getEngineDefaultSaiyVoice());
                    MyLog.v(CLS_NAME, "getBoundSaiyVoice: " + getBoundSaiyVoice());

                    final SaiyVoice userDefaultSaiyVoice = getUserDefaultSaiyVoice();

                    if (userDefaultSaiyVoice != null) {
                        MyLog.v(CLS_NAME, "userDefaultSaiyVoice: " + userDefaultSaiyVoice);
                    }

                } catch (final NullPointerException e) {
                    MyLog.w(CLS_NAME, "NullPointerException");
                    e.printStackTrace();
                } catch (final Exception e) {
                    MyLog.w(CLS_NAME, "Exception");
                    e.printStackTrace();
                } finally {

                    try {
                        final Locale defaultLanguage = getDefaultLanguage();
                        MyLog.v(CLS_NAME, "defaultLanguage toString: " + defaultLanguage.toString());
                    } catch (final NullPointerException e) {
                        MyLog.w(CLS_NAME, "NullPointerException");
                        e.printStackTrace();
                    } catch (final Exception e) {
                        MyLog.w(CLS_NAME, "Exception");
                        e.printStackTrace();
                    } finally {
                        try {
                            final Locale languageLocale = getLanguage();
                            MyLog.v(CLS_NAME, "languageLocale toString: " + languageLocale.toString());
                        } catch (final NullPointerException e) {
                            MyLog.w(CLS_NAME, "NullPointerException");
                            e.printStackTrace();
                        } catch (final Exception e) {
                            MyLog.w(CLS_NAME, "Exception");
                            e.printStackTrace();
                        }
                    }
                }

                getVerboseInfo();
            }
        });
    }

    /**
     * More debugging info
     */
    @Override
    protected void getVerboseInfo() {
        super.getVerboseInfo();
        try {
            for (final SaiyVoice v : getSaiyVoices()) {
                MyLog.v(CLS_NAME, "v : " + v.toString());
            }

        } catch (final NullPointerException e) {
            MyLog.w(CLS_NAME, "NullPointerException");
            e.printStackTrace();
        } catch (final Exception e) {
            MyLog.w(CLS_NAME, "Exception");
            e.printStackTrace();
        }

        try {
            for (final Locale loc : getAvailableLanguages()) {
                MyLog.v(CLS_NAME, "loc: " + loc.toString());
            }
        } catch (final NullPointerException e) {
            MyLog.w(CLS_NAME, "NullPointerException");
            e.printStackTrace();
        } catch (final Exception e) {
            MyLog.w(CLS_NAME, "Exception");
            e.printStackTrace();
        }
    }

    /**
     * Attempt to select a voice object based on the user's preference or required locale. The
     * omission of parameters that let us know if the engine is correctly installed is a major
     * frustration here, along with no providers distinguishing between the gender of their voices.
     * <p/>
     * I could rant further....
     * <p/>
     * Created by benrandall76@gmail.com on 13/03/2016.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private static class TTSVoice {
        private static final boolean DEBUG = MyLog.DEBUG;
        private final String CLS_NAME = TTSVoice.class.getSimpleName();

        private final Context mContext;
        private final String language;
        private final String region;
        private final SelfAwareConditions conditions;
        private final boolean isNetworkAllowed;
        private final boolean isNetworkAvailable;
        private final TextToSpeech21 mTextToSpeech;
        private final SaiyVoice currentVoice;

        /**
         * Constructor
         *
         * @param language   the {@link Locale} language
         * @param region     the {@link Locale} region
         * @param conditions the {@link SelfAwareConditions}
         * @param params     the {@link SelfAwareParameters}
         */
        private TTSVoice(@NonNull Context context, @NonNull TextToSpeech21 textToSpeech,
                         @NonNull final String language, @NonNull final String region,
                         @NonNull final SelfAwareConditions conditions,
                         @NonNull final SelfAwareParameters params, @Nullable final SaiyVoice currentVoice) {
            this.mContext = context;
            this.mTextToSpeech = textToSpeech;
            this.language = language;
            this.region = region;
            this.conditions = conditions;
            this.currentVoice = currentVoice;

            isNetworkAllowed = params.isNetworkAllowed();
            isNetworkAvailable = params.shouldNetwork();

        }

        private Pair<SaiyVoice, Locale> buildVoice() {

            Locale requiredLocale = null;
            SaiyVoice voice = null;

            try {

                final Set<SaiyVoice> voices = mTextToSpeech.getSaiyVoices();
                requiredLocale = new Locale(language, region);

                if (conditions.getCondition() != Condition.CONDITION_TRANSLATION) {

                    if (currentVoice != null && mTextToSpeech.getInitialisedEngine().startsWith(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Have a current google voice");
                        }

                        final TTSDefaults.Google parentGoogle = TTSDefaults.Google.getGoogle(currentVoice.getName());

                        if (parentGoogle != null) {

                            SaiyVoice associatedVoice = null;
                            TTSDefaults.Google associatedGoogle = null;

                            for (final SaiyVoice v : voices) {
                                if (parentGoogle.getVoiceName().matches("(?i)" + Pattern.quote(v.getName()))) {

                                    if (v.isNetworkConnectionRequired()) {
                                        associatedGoogle = TTSDefaults.Google.getAssociatedVoice(parentGoogle,
                                                TTSDefaults.TYPE_LOCAL);
                                    } else {
                                        associatedGoogle = TTSDefaults.Google.getAssociatedVoice(parentGoogle,
                                                TTSDefaults.TYPE_NETWORK);
                                    }

                                    break;
                                }
                            }

                            if (associatedGoogle != null) {
                                for (final SaiyVoice v : voices) {
                                    if (associatedGoogle.getVoiceName().matches("(?i)" + Pattern.quote(v.getName()))) {
                                        associatedVoice = v;
                                    }
                                }

                                if (associatedVoice != null) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "returning associated: " + associatedVoice.getName());
                                    }
                                    return new Pair<>(associatedVoice, requiredLocale);
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "associated google null");
                                }
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "parent google null");
                            }
                        }
                    }

                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "buildVoice: CONDITION_TRANSLATION");
                    }

                    final ArrayList<SaiyVoice> voiceArray = new ArrayList<>();
                    final ArrayList<SaiyVoice> voiceExactArray = new ArrayList<>();

                    if (!voices.isEmpty()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Have " + voices.size() + " starting voices");
                        }

                        for (final SaiyVoice v : voices) {
                            if (UtilsLocale.localesMatch(v.getLocale(), requiredLocale)) {
                                if (DEBUG) {
                                    MyLog.v(CLS_NAME, "v : " + v);
                                }
                                voiceArray.add(v);
                            }
                        }

                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Have " + voiceArray.size() + " potential voices");
                        }

                        if (voiceArray.isEmpty()) {
                            if (DEBUG) {
                                MyLog.v(CLS_NAME, "Checking ISO ");
                            }

                            for (final SaiyVoice v : voices) {
                                if (v.getLocale().getISO3Language().matches(requiredLocale.getISO3Language())) {
                                    if (DEBUG) {
                                        MyLog.v(CLS_NAME, "vISO language: " + v.getLocale().getISO3Language());
                                    }
                                    if (v.getLocale().getISO3Country().matches(requiredLocale.getISO3Country())) {
                                        if (DEBUG) {
                                            MyLog.v(CLS_NAME, "vISO country: " + v.getLocale().getISO3Country());
                                        }
                                        voiceArray.add(0, v);
                                    } else {
                                        voiceArray.add(v);
                                    }
                                }
                            }
                        }

                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Have " + voiceArray.size() + " potential voices");
                        }

                        if (voiceArray.isEmpty()) {
                            if (DEBUG) {
                                MyLog.v(CLS_NAME, "Checking loose ISO ");
                            }

                            for (final SaiyVoice v : voices) {
                                if (v.getLocale().getISO3Language().matches(requiredLocale.getLanguage())) {
                                    if (DEBUG) {
                                        MyLog.v(CLS_NAME, "vISO language: " + v.getLocale().getLanguage());
                                    }
                                    if (v.getLocale().getISO3Country().matches(requiredLocale.getCountry())) {
                                        if (DEBUG) {
                                            MyLog.v(CLS_NAME, "vISO country: " + v.getLocale().getCountry());
                                        }
                                        voiceArray.add(0, v);
                                    } else {
                                        voiceArray.add(v);
                                    }
                                }
                            }
                        }

                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "Have " + voiceArray.size() + " potential voices");
                        }

                        if (!voiceArray.isEmpty()) {

                            if (!isNetworkAllowed || !isNetworkAvailable) {
                                if (DEBUG) {
                                    MyLog.v(CLS_NAME, "Removing networked voices");
                                }

                                final ListIterator<SaiyVoice> itr = voiceArray.listIterator();

                                SaiyVoice v;
                                while (itr.hasNext()) {
                                    v = itr.next();
                                    if ((v.isNetworkConnectionRequired()
                                            && !v.getFeatures().contains(TTSDefaults.EMBEDDED_TTS_FIELD))
                                            || v.getFeatures().contains(Engine.KEY_FEATURE_NOT_INSTALLED)) {
                                        if (DEBUG) {
                                            MyLog.v(CLS_NAME, "Removing networked voice: " + v);
                                        }
                                        itr.remove();
                                    }
                                }
                            }

                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "Have " + voiceArray.size() + " potential voices");
                            }

                            if (!voiceArray.isEmpty()) {

                                for (final SaiyVoice v : voiceArray) {
                                    if (isNetworkAllowed) {
                                        if (v.isNetworkConnectionRequired()) {
                                            voiceExactArray.add(0, v);
                                        }
                                    } else {
                                        voiceExactArray.add(v);
                                    }
                                }

                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "Have " + voiceExactArray.size() + " exact voices");
                                }

                                if (!voiceExactArray.isEmpty()) {
                                    if (DEBUG) {
                                        for (final SaiyVoice v : voiceExactArray) {
                                            MyLog.v(CLS_NAME, "Exact Voice " + v.toString());
                                        }
                                    }

                                    voice = getVoiceDetailed(voiceExactArray);

                                } else {
                                    if (DEBUG) {
                                        for (final SaiyVoice v : voiceArray) {
                                            MyLog.v(CLS_NAME, "Settled Voice " + v.toString());
                                        }
                                    }

                                    voice = getVoiceDetailed(voiceArray);
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "Only networked voices available");
                                }
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "Could not match engine Locale");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "Engine has no voices?");
                        }
                    }
                }
            } catch (final MissingResourceException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "MissingResourceException");
                    e.printStackTrace();
                }
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "NullPointerException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "Exception");
                    e.printStackTrace();
                }
                if (e instanceof IllformedLocaleException) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "IllformedLocaleException");
                    }
                }
            }

            if (voice != null) {
                voice.setEngine(mTextToSpeech.getInitialisedEngine());
                voice.setGender(voice.getName());
                return new Pair<>(voice, requiredLocale);
            } else {
                return new Pair<>(null, requiredLocale);
            }
        }

        private SaiyVoice getVoiceDetailed(@NonNull final ArrayList<SaiyVoice> voiceArray) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getVoiceDetailed");
            }

            if (voiceArray.size() > 1) {
                return filterGender(voiceArray).get(0);
            } else {
                return voiceArray.get(0);
            }
        }

        private ArrayList<SaiyVoice> filterGender(@NonNull final ArrayList<SaiyVoice> voiceArray) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "filterGender");
            }

            final Gender preferredGender = SPH.getDefaultTTSGender(mContext);
            final ArrayList<SaiyVoice> voiceArrayCopy = new ArrayList<>(voiceArray);

            final ListIterator<SaiyVoice> itr = voiceArrayCopy.listIterator();

            SaiyVoice v;
            while (itr.hasNext()) {
                v = itr.next();
                if (v.getGender() != preferredGender) {
                    itr.remove();
                }
            }

            if (voiceArrayCopy.isEmpty()) {
                return filterLegacy(voiceArray);
            } else {
                return filterLegacy(voiceArrayCopy);
            }
        }

        private ArrayList<SaiyVoice> filterLegacy(@NonNull final ArrayList<SaiyVoice> voiceArray) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "filterLegacy");
            }

            if (isNetworkAllowed) {

                final ArrayList<SaiyVoice> voiceArrayCopy = new ArrayList<>(voiceArray);
                final ListIterator<SaiyVoice> itr = voiceArrayCopy.listIterator();

                SaiyVoice v;
                while (itr.hasNext()) {
                    v = itr.next();
                    if (v.getFeatures().contains(TTSDefaults.LEGACY_ENGINE_FIELD)) {
                        itr.remove();
                    }
                }

                if (voiceArrayCopy.isEmpty()) {
                    return filterQuality(voiceArray);
                } else {
                    return filterQuality(voiceArrayCopy);
                }
            }

            return filterQuality(voiceArray);
        }

        private ArrayList<SaiyVoice> filterQuality(@NonNull final ArrayList<SaiyVoice> voiceArray) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "filterQuality");
            }

            Collections.sort(voiceArray, new SaiyVoice.SaiyVoiceComparator());
            return voiceArray;
        }
    }
}
