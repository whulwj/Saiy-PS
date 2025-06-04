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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.media.AudioTrack;
import android.os.Build;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import ai.saiy.android.R;
import ai.saiy.android.database.DBSpeech;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.service.helper.SelfAwareConditions;
import ai.saiy.android.service.helper.SelfAwareHelper;
import ai.saiy.android.service.helper.SelfAwareParameters;
import ai.saiy.android.tts.attributes.Gender;
import ai.saiy.android.tts.helper.TTSDefaults;
import ai.saiy.android.tts.sound.SoundEffect;
import ai.saiy.android.tts.sound.SoundEffectHelper;
import ai.saiy.android.tts.sound.SoundEffectItem;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsReflection;
import ai.saiy.android.utils.UtilsString;

/**
 * Due to misbehaving voice engines, it's necessary to subclass the TTS object here and handle extra
 * eventualities that have caused many crashes along the way.
 * <p/>
 * Additionally, handling the try/catch inside the methods keeps other classes tidy and more readable.
 * <p/>
 * Created by benrandall76@gmail.com on 13/03/2016.
 */
public abstract class SaiyTextToSpeech extends TextToSpeech {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = SaiyTextToSpeech.class.getSimpleName();

    public static final int MAX_UTTERANCE_LENGTH = 500;
    public static final int MIN_UTTERANCE_LENGTH = 200;

    protected static final String ARRAY = "array";
    public static final String ARRAY_FIRST = "array_first";
    public static final String ARRAY_INTERIM = "array_interim";
    public static final String ARRAY_LAST = "array_last";
    public static final String ARRAY_SINGLE = "array_single";
    public static final String ARRAY_DELIMITER = "~~";

    public static final String ALEXA_EAR_CON = "alex_temp";

    protected volatile String initEngine;
    protected final Context mContext;

    /**
     * Constructor
     *
     * @param context the application context
     * @param listener the {@link OnInitListener} to monitor the tts engine
     */
    public SaiyTextToSpeech(final Context context, final OnInitListener listener) {
        super(context, listener);
        this.mContext = context;
    }

    /**
     * Constructor
     *
     * @param context the application context
     * @param listener the {@link OnInitListener} to monitor the tts engine
     * @param engine   package name of a requested engine
     */
    public SaiyTextToSpeech(final Context context, final OnInitListener listener, final String engine) {
        super(context, listener, engine);
        this.mContext = context;
    }

    /**
     * Process a Text to Speech utterance request, considering API versions, sound effects, silence
     * and other possibilities.
     *
     * @param text        the utterance
     * @param queueMode   one of {@link #QUEUE_ADD} or {@link #QUEUE_FLUSH}
     * @param params      the {@link SelfAwareParameters} object
     * @param utteranceId the utterance id
     * @return one of {@link #SUCCESS} or {@link Error}
     */
    public int speak(@NonNull CharSequence text, final int queueMode,
                     @NonNull final SelfAwareParameters params, @NonNull final String utteranceId) {
        if (ai.saiy.android.thirdparty.tasker.TaskerHelper.pTask.matcher(text).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "speak: have tasker variables");
            }
            text = ai.saiy.android.thirdparty.tasker.TaskerHelper.replaceTask(mContext, text);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "speak: variable replace: " + text);
            }
        }

        if (SoundEffectHelper.pSOUND_EFFECT.matcher(text).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "speak: have sound effect");
            }

            final Gender gender = getGender();
            final SoundEffectHelper helper = new SoundEffectHelper(text, utteranceId, gender);
            helper.sort();
            final ArrayList<SoundEffectItem> items = helper.getArray();
            final ArrayList<String> addedItems = SoundEffectHelper.getAddedItems();

            int result = ERROR;
            for (final SoundEffectItem item : items) {

                params.setUtteranceId(item.getUtteranceId());

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "item getText: " + item.getText());
                    MyLog.i(CLS_NAME, "item getItemType: " + item.getItemType());
                    MyLog.i(CLS_NAME, "item getUtteranceId: " + item.getUtteranceId());
                }

                switch (item.getItemType()) {

                    case SoundEffectItem.SOUND:

                        if (addedItems.contains(item.getText())) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                result = playEarcon(item.getText(), QUEUE_ADD, params.getBundle(), item.getUtteranceId());
                            } else {
                                result = playEarcon(item.getText(), QUEUE_ADD, params);
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "No valid sound effect: " + item.getText());
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                result = speak21(mContext.getString(R.string.error_sound_effect), QUEUE_ADD, params, item.getUtteranceId());
                            } else {
                                result = speak(mContext.getString(R.string.error_sound_effect), QUEUE_ADD, params);
                            }
                        }

                        break;
                    case SoundEffectItem.SPEECH:

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            result = speak21(item.getText(), QUEUE_ADD, params, item.getUtteranceId());
                        } else {
                            result = speak(item.getText(), QUEUE_ADD, params);
                        }
                        break;
                    case SoundEffectItem.SILENCE:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            result = playSilentUtterance(SoundEffect.SILENCE_DURATION, QUEUE_ADD,
                                    item.getUtteranceId());
                        } else {
                            result = playSilence(SoundEffect.SILENCE_DURATION, QUEUE_ADD, params);
                        }
                        break;
                }
            }

            return result;
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (params.containsKey(LocalRequest.EXTRA_ALEXA_FILE_PATH)) {
                    return playEarcon(ALEXA_EAR_CON, TextToSpeech.QUEUE_FLUSH, params.getBundle(), params.getUtteranceId());
                }
                return speak21(text, queueMode, params, utteranceId);
            } else {
                if (params.containsKey(LocalRequest.EXTRA_ALEXA_FILE_PATH)) {
                    return playEarcon(ALEXA_EAR_CON, TextToSpeech.QUEUE_FLUSH, params);
                }
                return speak(text.toString(), queueMode, params);
            }
        }
    }

    protected abstract Gender getGender();

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private int speak21(@NonNull final CharSequence text, final int queueMode,
                        @NonNull final SelfAwareParameters params, @NonNull final String utteranceId) {

        if (queueMode != QUEUE_ADD && !params.getUtteranceId().startsWith(ARRAY)
                && canSynthesise(text.toString(), params)) {
            return SUCCESS;
        } else {
            if (text.length() > getMaxUtteranceLength()) {

                final ArrayList<String> splitUtterances = SelfAwareHelper.splitUtteranceRegex(
                        text.toString(), getMaxUtteranceLength());

                final int splitUtterancesSize = splitUtterances.size();

                if (splitUtterancesSize > 1) {

                    final boolean overrideId = utteranceId.contains(ARRAY_DELIMITER);

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "speak21: SU: " + utteranceId);
                        MyLog.i(CLS_NAME, "speak21: SU override: " + overrideId);
                    }

                    for (int i = 0; i < splitUtterancesSize; i++) {

                        final String resolvedUtteranceId = resolveUtteranceId(utteranceId, splitUtterancesSize,
                                i, overrideId);

                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "speak21: SU resolvedUtteranceId: " + resolvedUtteranceId);
                        }

                        params.setUtteranceId(resolvedUtteranceId);

                        if (i == (splitUtterancesSize - 1)) {
                            return super.speak(splitUtterances.get(i), QUEUE_ADD, params.getBundle(),
                                    params.getUtteranceId());
                        } else {
                            super.speak(splitUtterances.get(i), QUEUE_ADD, params.getBundle(),
                                    params.getUtteranceId());
                        }
                    }

                } else {
                    return super.speak(text, queueMode, params.getBundle(), utteranceId);
                }
            } else {
                return super.speak(text, queueMode, params.getBundle(), utteranceId);
            }
        }

        return super.speak(text, queueMode, params.getBundle(), utteranceId);
    }

    @Override
    public int speak(final String text, final int queueMode, final HashMap<String, String> map) {
        if (text.length() > getMaxUtteranceLength()) {
            final String utteranceId = map.get(Engine.KEY_PARAM_UTTERANCE_ID);
            if (TextUtils.isEmpty(utteranceId)) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "speak: " + utteranceId);
                }
                return super.speak(text, queueMode, map);
            }

            final ArrayList<String> splitUtterances = SelfAwareHelper.splitUtteranceRegex(text,
                    getMaxUtteranceLength());

            final int splitUtterancesSize = splitUtterances.size();

            if (splitUtterancesSize > 1) {
                final boolean overrideId = utteranceId.contains(ARRAY_DELIMITER);

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "speak: SU: " + utteranceId);
                    MyLog.i(CLS_NAME, "speak: SU override: " + overrideId);
                }

                for (int i = 0; i < splitUtterancesSize; i++) {

                    final String resolvedUtteranceId = resolveUtteranceId(utteranceId, splitUtterancesSize,
                            i, overrideId);

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "speak: SU resolvedUtteranceId: " + resolvedUtteranceId);
                    }

                    map.put(Engine.KEY_PARAM_UTTERANCE_ID, resolvedUtteranceId);

                    if (i == (splitUtterancesSize - 1)) {
                        return super.speak(splitUtterances.get(i), QUEUE_ADD, map);
                    } else {
                        super.speak(splitUtterances.get(i), QUEUE_ADD, map);
                    }
                }

            } else {
                return super.speak(text, queueMode, map);
            }
        } else {
            return super.speak(text, queueMode, map);
        }

        return super.speak(text, queueMode, map);
    }

    private String resolveUtteranceId(@NonNull final String utteranceId, final int arraySize, final int position,
                                      final boolean overrideId) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resolveUtteranceId: utteranceId: " + utteranceId);
            MyLog.i(CLS_NAME, "resolveUtteranceId: arraySize: " + arraySize);
            MyLog.i(CLS_NAME, "resolveUtteranceId: position: " + position);
            MyLog.i(CLS_NAME, "resolveUtteranceId: overrideId: " + overrideId);
        }

        if (position == (arraySize - 1)) {

            if (overrideId) {

                if (utteranceId.startsWith(ARRAY_INTERIM) || utteranceId.startsWith(ARRAY_LAST)) {
                    return utteranceId;
                } else {
                    return ARRAY_INTERIM + ARRAY_DELIMITER + TextUtils.split(utteranceId, ARRAY_DELIMITER)[1];
                }

            } else {
                return ARRAY_LAST + ARRAY_DELIMITER + utteranceId;
            }

        } else if (position == 0) {
            if (overrideId) {

                if (utteranceId.startsWith(ARRAY_FIRST) || utteranceId.startsWith(ARRAY_INTERIM)) {
                    return utteranceId;
                } else {
                    return ARRAY_INTERIM + ARRAY_DELIMITER + TextUtils.split(utteranceId, ARRAY_DELIMITER)[1];
                }

            } else {
                return ARRAY_FIRST + ARRAY_DELIMITER + utteranceId;
            }
        }

        if (overrideId) {

            if (utteranceId.startsWith(ARRAY_INTERIM)) {
                return utteranceId;
            } else {
                return ARRAY_INTERIM + ARRAY_DELIMITER + TextUtils.split(utteranceId, ARRAY_DELIMITER)[1];
            }

        } else {
            return ARRAY_INTERIM + ARRAY_DELIMITER + utteranceId;
        }
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
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected boolean canSynthesise(@NonNull final String utterance, @NonNull final SelfAwareParameters params) {
        return false;
    }

    /**
     * Add the inbuilt array of sound effect that can be played by the engine on demand
     */
    private void addSoundEffects() {

        final ArrayList<String> addedItems = new ArrayList<>();
        final TypedArray typedArray = mContext.getResources().obtainTypedArray(R.array.array_se);

        int temp;
        String fileName;
        for (int i = 0; i < typedArray.length(); i++) {

            temp = typedArray.getResourceId(i, -1);

            if (temp > 0) {

                try {

                    fileName = mContext.getResources().getResourceEntryName(temp);

                    if (DEBUG) {
                        MyLog.v(CLS_NAME, "addSoundEffects: fileName: " + fileName);
                    }

                    switch (addEarcon(fileName, mContext.getPackageName(), temp)) {

                        case SUCCESS:
                            if (DEBUG) {
                                MyLog.v(CLS_NAME, "addSoundEffects: fileName: SUCCESS: " + fileName);
                            }
                            addedItems.add(fileName);
                            break;
                        case ERROR:
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "addSoundEffects: fileName: ERROR: " + fileName);
                            }
                            break;
                    }

                } catch (final Resources.NotFoundException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "addSoundEffects: Resources.NotFoundException: " + temp);
                        e.printStackTrace();
                    }
                }
            }
        }

        typedArray.recycle();
        for (File file : SoundEffectHelper.getUserSoundEffects(mContext)) {
            String userFileName = org.apache.commons.io.FilenameUtils.getBaseName(file.getName());
            switch (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? addEarcon(userFileName, file) : addEarcon(userFileName, file.getPath())) {
                case ERROR:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "addSoundEffects: userFileName: ERROR: " + userFileName);
                    }
                    break;
                case SUCCESS:
                    if (DEBUG) {
                        MyLog.v(CLS_NAME, "addSoundEffects: userFileName: SUCCESS: " + userFileName);
                    }
                    addedItems.add(userFileName);
                    break;
            }
        }
        SoundEffectHelper.setAddedItems(addedItems);
    }

    @Override
    public final String getDefaultEngine() {

        String packageName = "";

        try {
            packageName = super.getDefaultEngine();
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getDefaultEngine: NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getDefaultEngine: Exception");
                e.printStackTrace();
            }
        }

        return packageName;
    }

    /**
     * for some TTS Engines, the {@link #getDefaultEngine()} is very slow - here we jump straight
     * to the Secure Settings to access it. If an exception is thrown, we revert to the latter.
     *
     * @return the user default TTS Engine
     */

    private String getDefaultEngineSecure() {

        try {

            final String packageName = Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.TTS_DEFAULT_SYNTH);

            if (packageName != null) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "getDefaultEngineSecure: Secure: " + packageName);
                }

                return packageName;
            }

        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getDefaultEngineSecure: NullPointerException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getDefaultEngineSecure: Exception");
                e.printStackTrace();
            }
        }

        return getDefaultEngine();
    }

    /**
     * Compare the current initialised TTS Engine with the user's default selection. If this has
     * changed since initialisation occurred, we need to restart the Engine, to bind to the new
     * default choice.
     *
     * @param packageName supplied if we want to compare a specific Engine
     * @return true if the default Engine does not match the initialised Engine
     */
    public final boolean shouldReinitialise(final String packageName) {

        final String initialisedEngine = getInitialisedEngine();

        if (!UtilsString.notNaked(initialisedEngine)) {
            return true;
        }

        if (packageName != null) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "shouldReinitialise: comparing " + initialisedEngine
                        + " ~ " + packageName);
            }
            return !packageName.matches(initialisedEngine);
        } else {

            final String defaultEngine = getDefaultEngineSecure();

            if (DEBUG) {
                MyLog.v(CLS_NAME, "shouldReinitialise: comparing " + defaultEngine
                        + " ~ " + initialisedEngine);
            }

            return !UtilsString.notNaked(defaultEngine) || !defaultEngine.matches(initialisedEngine);

        }
    }

    /**
     * Limit of length of input string passed to speak and synthesizeToFile.
     *
     * @see #speak
     * @see #synthesizeToFile
     */
    protected final int getMaxUtteranceLength() {
        if (getInitialisedEngine().startsWith(TTSDefaults.TTS_PKG_NAME_GOOGLE)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getMaxUtteranceLength: TTS_PKG_NAME_GOOGLE");
            }
        } else if (getInitialisedEngine().startsWith(TTSDefaults.TTS_PKG_NAME_IVONA)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getMaxUtteranceLength: TTS_PKG_NAME_IVONA");
            }
        } else if (getInitialisedEngine().startsWith(TTSDefaults.TTS_PKG_NAME_CEREPROC)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getMaxUtteranceLength: TTS_PKG_NAME_CEREPROC");
            }
        } else if (getInitialisedEngine().startsWith(TTSDefaults.TTS_PKG_NAME_PICO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getMaxUtteranceLength: TTS_PKG_NAME_PICO");
            }
        } else if (getInitialisedEngine().startsWith(TTSDefaults.TTS_PKG_NAME_SVOX)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getMaxUtteranceLength: TTS_PKG_NAME_SVOX");
            }
        }

        return MAX_UTTERANCE_LENGTH;
    }

    @Override
    public final int isLanguageAvailable(final Locale loc) {
        int result = LANG_NOT_SUPPORTED;
        try {
            result = super.isLanguageAvailable(loc);
        } catch (final IllegalArgumentException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "isLanguageAvailable: IllegalArgumentException: "
                        + loc.toString());
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public boolean isSpeaking() {
        final boolean speakingSuper = super.isSpeaking();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "isSpeaking: speakingSuper " + speakingSuper);
        }

        return speakingSuper;
    }

    /**
     * Set the voice of the TTS object. API levels are handled here.
     *
     * @param language   the {@link Locale} language
     * @param region     the {@link Locale} region
     * @param conditions the {@link SelfAwareConditions}
     * @param params     the {@link SelfAwareParameters}
     * @return one of {@link #SUCCESS} or {@link #ERROR}
     */
    public int setVoice(@NonNull final String language, @NonNull final String region,
                        @NonNull final SelfAwareConditions conditions, @NonNull final SelfAwareParameters params) {

        int result;

        if (DEBUG) {
            final String initialisedEngine = getInitialisedEngine();
            MyLog.i(CLS_NAME, "setVoice: initialisedEngine: " + initialisedEngine);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && TTSDefaults.isApprovedVoice(getInitialisedEngine())) {

            result = setVoice21(language, region, conditions, params);

            if (DEBUG) {
                switch (result) {
                    case SUCCESS:
                        MyLog.i(CLS_NAME, "setVoice21: SUCCESS");
                        break;
                    case ERROR:
                        MyLog.w(CLS_NAME, "setVoice21: ERROR");
                        break;
                    default:
                        MyLog.w(CLS_NAME, "setVoice21: default");
                        break;
                }
            }

            if (result != SUCCESS) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "setVoice21: FAIL: notifying user");
                }
                conditions.showToast(mContext.getString(R.string.error_tts_voice), Toast.LENGTH_SHORT);
            }

            return result;
        } else {

            result = setVoiceDeprecated(language, region, conditions);

            if (DEBUG) {
                switch (result) {
                    case SUCCESS:
                        MyLog.i(CLS_NAME, "setVoiceDeprecated: SUCCESS");
                        break;
                    case ERROR:
                        MyLog.w(CLS_NAME, "setVoiceDeprecated: ERROR");
                        break;
                    default:
                        MyLog.w(CLS_NAME, "setVoiceDeprecated: default");
                        break;
                }
            }

            if (result != SUCCESS) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "setVoiceDeprecated: FAIL: notifying user");
                }
                conditions.showToast(mContext.getString(R.string.error_tts_voice), Toast.LENGTH_SHORT);
            }

            return result;
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
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected int setVoice21(@NonNull final String language, @NonNull final String region,
                           @NonNull final SelfAwareConditions conditions, @NonNull final SelfAwareParameters params) {
        return ERROR;
    }

    /**
     * Set the voice/language of the TTS object for lower API levels, or if the current engine does
     * not support the latest APIs.
     *
     * @param language the {@link Locale} language
     * @param region   the {@link Locale} region
     * @param conditions the {@link SelfAwareConditions}
     * @return one of {@link #SUCCESS} or {@link #ERROR}
     */
    protected final int setVoiceDeprecated(@NonNull final String language, @NonNull final String region, @NonNull SelfAwareConditions conditions) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setVoiceDeprecated: Setting Locale");
        }

        try {
            Locale locale = new Locale(language, region);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoiceDeprecated: comparing current: " + getLanguage() + " with " + locale);
                    MyLog.i(CLS_NAME, "setVoiceDeprecated: getLanguage: " + getLanguage());
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoiceDeprecated: comparing current: " + getDefaultLanguage() + " with " + locale);
                    MyLog.i(CLS_NAME, "setVoiceDeprecated: getDefaultLanguage: " + getDefaultLanguage() + " getLanguage: " + getLanguage());
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || conditions.getCondition() == Condition.CONDITION_TRANSLATION) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "setVoiceDeprecated: CONDITION_TRANSLATION: setting request Locale");
                    }
                }
                if (!UtilsLocale.localesMatch(getLanguage(), locale)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        MyLog.i(CLS_NAME, "setVoiceDeprecated: CONDITION_TRANSLATION: locales not matched");
                    }
                    try {
                        locale.getCountry();
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "setVoiceDeprecated: isLanguageAvailable: "
                                    + resolveSuccess(super.isLanguageAvailable(new Locale(locale.getLanguage(),
                                    locale.getCountry()))));
                        }
                    } catch (final MissingResourceException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "MissingResourceException: Just using language");
                            e.printStackTrace();
                        }
                        locale = new Locale(locale.getLanguage());
                    }

                    return resolveSuccess(super.setLanguage(locale));
                } else {
                    if (DEBUG) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            MyLog.i(CLS_NAME, "setVoiceDeprecated: CONDITION_TRANSLATION: Current matches");
                        } else {
                            MyLog.i(CLS_NAME, "setVoiceDeprecated: Current matches");
                        }
                    }
                    return SUCCESS;
                }
            }

            if (DEBUG) {
                MyLog.i(CLS_NAME, "setVoiceDeprecated: checking standard match");
            }
            final @Nullable Locale defaultLanguage = getDefaultLanguage();
            if (defaultLanguage != null) {
                locale = SupportedLanguage.getSupportedLanguage(defaultLanguage).getLocale();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoiceDeprecated: SupportedLanguage: defaultEngineLocale: " + locale);
                }
            } else {
                locale = SupportedLanguage.getSupportedLanguage(locale).getLocale();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoiceDeprecated: SupportedLanguage: locale: " + locale);
                }
            }
            if (!UtilsLocale.localesMatch(getLanguage(), locale)) {
                MyLog.i(CLS_NAME, "setVoiceDeprecated: locales not matched");
                try {
                    locale.getCountry();
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "setVoiceDeprecated: isLanguageAvailable: "
                                + resolveSuccess(super.isLanguageAvailable(new Locale(locale.getLanguage(),
                                locale.getCountry()))));
                    }
                } catch (final MissingResourceException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "MissingResourceException: Just using language");
                        e.printStackTrace();
                    }
                    locale = new Locale(locale.getLanguage());
                }

                return resolveSuccess(super.setLanguage(locale));
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setVoiceDeprecated: Current matches");
                }
                return SUCCESS;
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
        } catch (final MissingResourceException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "MissingResourceException");
                e.printStackTrace();
            }
        } catch (final Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }

        return ERROR;
    }

    /**
     * Get the previously initialised TTS Engine package name. If it doesn't match the user's default
     * we need to reinitialise the Engine.
     *
     * @return the package name of the currently initialised TTS Engine.
     */
    public final String getInitialisedEngine() {
        if (initEngine == null) {
            return "";
        } else {
            return this.initEngine;
        }
    }

    /**
     * Store the initialised engine package name, so to check in the future if the user has changed
     * their default TTS Engine in the Android Settings. There is no Broadcast exposed to handle
     * this elsewhere, so we need to use reflection to access the variable in the super class.
     *
     * @param initEngine the user's default Text to Speech Engine in the Android Application Settings
     */
    private void setInitialisedEngine(@NonNull final String initEngine) {

        String reflectEngine;

        try {
            final Object object = UtilsReflection.invokeMethod(this, TextToSpeech.class, TTSDefaults.BOUND_ENGINE_METHOD);
            reflectEngine = (object instanceof String)? (String) object : null;

            if (UtilsString.notNaked(reflectEngine)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Method reflect: reflectEngine: " + reflectEngine);
                }

                this.initEngine = reflectEngine;
                return;
            }
        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
        }

        try {
            final Object object = UtilsReflection.getFieldObj(this, TextToSpeech.class, TTSDefaults.BOUND_ENGINE_FIELD, null);
            reflectEngine = (object instanceof String)? (String) object : null;

            if (UtilsString.notNaked(reflectEngine)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Field reflect: reflectEngine: " + reflectEngine);
                }

                this.initEngine = reflectEngine;
                return;
            }

        } catch (final NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
        }

        if (UtilsString.notNaked(initEngine)) {
            this.initEngine = initEngine;
        } else {
            this.initEngine = "";
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "initEngine: " + this.initEngine);
        }
    }

    /**
     * Store the initialised Text to Speech Engine
     */
    @CallSuper
    public void initialised() {
        try {

            final String packageName = getDefaultEngineSecure();
            setInitialisedEngine(packageName);

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
        }

        if (DEBUG) {
            getInfo();
        }
        addSoundEffects();
    }

    /**
     * Examine TTS objects in an overly verbose way. Debugging only.
     */
    @CallSuper
    protected void getInfo() {
        MyLog.i(CLS_NAME, "getQuickInfo");
    }

    /**
     * More debugging info
     */
    @CallSuper
    protected void getVerboseInfo() {
        MyLog.i(CLS_NAME, "getVerboseInfo");

        try {
            final List<EngineInfo> engines = getEngines();

            for (int i = 0; i < engines.size(); i++) {
                MyLog.v(CLS_NAME, "inf label: " + engines.get(i).label);
                MyLog.v(CLS_NAME, "inf name: " + engines.get(i).name);
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
     * Convert the responses of {@link #LANG_COUNTRY_AVAILABLE} {@link #LANG_AVAILABLE}
     * {@link #LANG_COUNTRY_VAR_AVAILABLE} {@link #LANG_MISSING_DATA}
     * {@link #LANG_NOT_SUPPORTED} to {@link #SUCCESS} {@link #ERROR}
     *
     * @param result the result of attempting to set a voice/language {@link Locale}
     * @return one of {@link #SUCCESS} {@link #ERROR}
     */
    protected final int resolveSuccess(final int result) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resolveSuccess");
        }

        switch (result) {
            case LANG_AVAILABLE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "resolveSuccess: LANG_AVAILABLE");
                }
                return SUCCESS;
            case LANG_COUNTRY_AVAILABLE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "resolveSuccess: LANG_COUNTRY_AVAILABLE");
                }
                return SUCCESS;
            case LANG_COUNTRY_VAR_AVAILABLE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "resolveSuccess: LANG_COUNTRY_VAR_AVAILABLE");
                }
                return SUCCESS;
            case LANG_MISSING_DATA:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "resolveSuccess: LANG_MISSING_DATA");
                }
                return ERROR;
            case LANG_NOT_SUPPORTED:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "resolveSuccess: LANG_NOT_SUPPORTED");
                }
                return ERROR;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "resolveSuccess: default");
                }
                return ERROR;
        }
    }
}
