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

package ai.saiy.android.processing;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.command.alexa.CommandAlexa;
import ai.saiy.android.command.battery.CommandBattery;
import ai.saiy.android.command.clipboard.ClipboardHelper;
import ai.saiy.android.command.custom.CommandCustom;
import ai.saiy.android.command.emotion.CommandEmotion;
import ai.saiy.android.command.foursquare.CommandFoursquare;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.command.note.CommandNote;
import ai.saiy.android.command.songrecognition.CommandSongRecognition;
import ai.saiy.android.command.spell.CommandSpell;
import ai.saiy.android.command.tasker.CommandTasker;
import ai.saiy.android.command.translate.CommandTranslate;
import ai.saiy.android.command.twitter.CommandTwitter;
import ai.saiy.android.command.unknown.Unknown;
import ai.saiy.android.command.username.CommandUserName;
import ai.saiy.android.command.vocalrecognition.CommandVocalRecognition;
import ai.saiy.android.command.wolframalpha.CommandWolframAlpha;
import ai.saiy.android.custom.CustomResolver;
import ai.saiy.android.defaults.songrecognition.SongRecognitionChooser;
import ai.saiy.android.device.UtilsDevice;
import ai.saiy.android.firebase.helper.UtilsAnalytic;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.localisation.SaiyResourcesHelper;
import ai.saiy.android.localisation.SaiyWebHelper;
import ai.saiy.android.memory.Memory;
import ai.saiy.android.memory.MemoryHelper;
import ai.saiy.android.nlu.local.FrequencyAnalysis;
import ai.saiy.android.nlu.local.Resolve;
import ai.saiy.android.permissions.PermissionHelper;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.helper.QuantumHelper;
import ai.saiy.android.recognition.SaiyRecognitionListener;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.service.helper.SelfAwareHelper;
import ai.saiy.android.thirdparty.tasker.TaskerHelper;
import ai.saiy.android.ui.activity.ActivityChooserDialog;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.notification.NotificationHelper;
import ai.saiy.android.utils.Conditions.Network;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsBundle;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

/**
 * This is the main class for resolving and actioning a spoken command. Regardless or not as to whether
 * the request has been processed locally or remotely, there is still no guarantee that the request is
 * sensical, to say that any given parameters can be resolved specifically to the destination command,
 * or perhaps the current context of the user.
 * <p>
 * Therefore, before we attempt to set the device brightness to 1984, create a reminder to get milk for
 * a week last Wednesday, translate 'hello' into helicopter, or call a contact with no number, we may
 * need to intervene....
 * <p>
 * Additionally, the request many contain multiple commands of different types, that are weighted by
 * confidence - either of purely the spoken word, or in the case of a remote service, the entity itself.
 * <p>
 * Dealing with these multiple eventualities, within a timescale that the user considers 'more efficient
 * by voice' and the bounds of the processing power of the device, is not easy.
 * <p>
 * In order to get as close as possible to achieving this, it's necessary to use some quantum probability
 * calculations, whereby multiple instances of weighted outcomes are assigned to a superposition. Each of
 * these multiple instances are 'entangled', and at anytime (a reduction in probability relative to its
 * equal, but opposite) the instance can be 'observed as unlikely', allowing other instances to continue
 * processing with this 'knowledge', effectively zero cancelling any resource used thus far.
 * <p>
 * Obviously, in the future, when this can be achieved at binary processing level, there will be no need
 * to consider probability - only definitive outcomes that that were 'observed' not to occur and therefore,
 * via entanglement, were never considered in the first place - but until then, you can read in more depth
 * about how this is achieved in the super class {@link Tunnelling}
 * <p>
 * Created by benrandall76@gmail.com on 08/02/2016.
 */
public class Quantum extends Tunnelling {
    private static final String COMMAND_CONSTANT = "command_constant";
    private static final String IS_CUSTOM_COMMAND = "is_custom_command";
    private static final String LANGUAGE_MODEL = "language_model";

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Quantum.class.getSimpleName();

    /**
     * Constructor
     *
     * @param mContext the application context
     */
    public Quantum(@NonNull final Context mContext) {
        super(mContext);
    }

    @Override
    protected Qubit doTunnelling(final CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "doTunnelling");
        }

        Qubit qubit = null;

        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData before: " + cr.getResultsArray().toString());
        }

        final CustomResolver customResolver = new QuantumHelper().resolve(mContext, sl, cr);
        final ArrayList<String> toResolve = customResolver.getVoiceData();
        final Bundle eventExtra;

        if (customResolver.isCustom()) {
            setAlexaTTS(cr.isAlexaTTS());
            cch = customResolver.getCustomCommandHelper();
            COMMAND = cch.getCommandConstant();
            eventExtra = (CC.COMMAND_UNKNOWN == COMMAND || CC.COMMAND_EMPTY_ARRAY == COMMAND || CC.COMMAND_SOMETHING_WEIRD == COMMAND)? null : new Bundle(5);
            if (eventExtra != null) {
                eventExtra.putString(COMMAND_CONSTANT, cr.isAlexaTTS()? CC.COMMAND_ALEXA.name() : COMMAND.name());
                eventExtra.putBoolean(IS_CUSTOM_COMMAND, true);
                eventExtra.putString(LANGUAGE_MODEL, SPH.getDefaultLanguageModel(mContext).name());
            }
        } else if (UtilsList.notNaked(toResolve)) {
            final float[] confidence = cr.getConfidenceArray();
            final SaiyDefaults.LanguageModel languageModel = SPH.getDefaultLanguageModel(mContext);
            switch (languageModel) {
                case LOCAL:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "LanguageModel LOCAL");
                    }

                    COMMAND = new FrequencyAnalysis(
                            new Resolve(mContext, toResolve, confidence, sl).resolve()).analyse();

                    break;
                default:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "LanguageModel EXTERNAL");
                    }

                    COMMAND = cr.getCC();

                    if (CommandRequest.inError(COMMAND)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "LanguageModelDefault.EXTERNAL inError: reverting to LOCAL");
                        }

                        COMMAND = new FrequencyAnalysis(
                                new Resolve(mContext, toResolve, confidence, sl).resolve()).analyse();
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "LanguageModelDefault.EXTERNAL: populated");
                        }
                    }

                    break;
            }
            eventExtra = (CC.COMMAND_UNKNOWN == COMMAND || CC.COMMAND_EMPTY_ARRAY == COMMAND || CC.COMMAND_SOMETHING_WEIRD == COMMAND)? null : new Bundle(5);
            if (eventExtra != null) {
                eventExtra.putString(COMMAND_CONSTANT, cr.isAlexaTTS()? CC.COMMAND_ALEXA.name() : COMMAND.name());
                eventExtra.putBoolean(IS_CUSTOM_COMMAND, false);
                eventExtra.putString(LANGUAGE_MODEL, languageModel.name());
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "toResolve naked");
            }

            COMMAND = CC.COMMAND_EMPTY_ARRAY;
            eventExtra = null;
        }

        final Outcome outcome;
        request.setCommand(COMMAND);

        if (Network.networkProceed(mContext, COMMAND)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "DT networkProceed passed");
                MyLog.i(CLS_NAME, "DT command secure: " + COMMAND.isSecure());
                MyLog.i(CLS_NAME, "DT command requested securely: " + cr.wasSecure());
                MyLog.i(CLS_NAME, "DT device secure: " + UtilsDevice.isDeviceLocked(this.mContext));
            }

            secure = COMMAND.isSecure() && cr.wasSecure() && UtilsDevice.isDeviceLocked(this.mContext);
            if (cr.isAlexaTTS()) {
                COMMAND = CC.COMMAND_UNKNOWN;
            }

            switch (COMMAND) {

                case COMMAND_CANCEL:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_CANCEL.name());
                    }

                    request.setUtterance(PersonalityResponse.getCancelled(mContext, sl));
                    request.setAction(LocalRequest.ACTION_SPEAK_ONLY);

                    break;
                case COMMAND_SPELL:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_SPELL.name());
                    }

                    outcome = new CommandSpell().getResponse(mContext, toResolve, sl, cr);

                    request.setUtterance(outcome.getUtterance());
                    request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                    result = outcome.getOutcome();

                    qubit = outcome.getQubit();
                    publishProgress(outcome.getEntangledPair());

                    break;
                case COMMAND_TRANSLATE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_TRANSLATE.name());
                    }

                    outcome = new CommandTranslate().getResponse(mContext, toResolve, sl, cr);

                    request.setUtterance(outcome.getUtterance());
                    request.setTTSLocale(outcome.getTTSLocale());
                    request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                    result = outcome.getOutcome();

                    if (result == Outcome.SUCCESS) {
                        request.setCondition(Condition.CONDITION_TRANSLATION);
                        publishProgress(outcome.getEntangledPair());
                        qubit = outcome.getQubit();
                    }

                    break;
                case COMMAND_PARDON:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_PARDON.name());
                    }

                    final Memory memory = MemoryHelper.getMemory(mContext);

                    if (!secure) {
                        request.setUtterance(memory.getUtterance());
                        request.setTTSLocale(UtilsLocale.stringToLocale(memory.getTTSLanguage()));
                        request.setVRLocale(UtilsLocale.stringToLocale(memory.getVRLanguage()));
                        request.setAction(memory.getAction());
                        request.setUtteranceArray(memory.getUtteranceArray());
                        result = Outcome.SUCCESS;

                        switch (memory.getCondition()) {

                            case Condition.CONDITION_TRANSLATION:
                                request.setCondition(Condition.CONDITION_TRANSLATION);
                                break;
                            case Condition.CONDITION_NONE:
                            case Condition.CONDITION_CONVERSATION:
                            case Condition.CONDITION_ROOT:
                            case Condition.CONDITION_USER_CUSTOM:
                            case Condition.CONDITION_EMOTION:
                            case Condition.CONDITION_IDENTITY:
                            case Condition.CONDITION_GOOGLE_NOW:
                            case Condition.CONDITION_IGNORE:
                            default:
                                request.setCondition(Condition.CONDITION_IGNORE);
                                break;
                        }
                    } else {
                        request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                        request.setUtterance(PersonalityResponse.getSecureErrorResponse(mContext, sl));
                        result = Outcome.SUCCESS;
                    }

                    break;
                case COMMAND_USER_NAME:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_USER_NAME.name());
                    }

                    outcome = new CommandUserName().getResponse(mContext, toResolve, sl, cr);

                    request.setUtterance(outcome.getUtterance());
                    request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                    result = outcome.getOutcome();

                    break;
                case COMMAND_BATTERY:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_BATTERY.name());
                    }

                    outcome = new CommandBattery().getResponse(mContext, toResolve, sl, cr);

                    request.setUtterance(outcome.getUtterance());
                    request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                    result = outcome.getOutcome();

                    break;
                case COMMAND_SONG_RECOGNITION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_SONG_RECOGNITION.name());
                    }

                    outcome = new CommandSongRecognition().getResponse(mContext, toResolve, sl);

                    request.setUtterance(outcome.getUtterance());
                    request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                    result = outcome.getOutcome();
                    qubit = outcome.getQubit();

                    break;
                case COMMAND_WOLFRAM_ALPHA:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_WOLFRAM_ALPHA.name());
                    }

                    outcome = new CommandWolframAlpha().getResponse(mContext, toResolve, sl, cr);

                    request.setUtterance(outcome.getUtterance());
                    request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                    result = outcome.getOutcome();

                    break;
                case COMMAND_TASKER:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_TASKER.name());
                    }

                    if (!secure) {

                        outcome = new CommandTasker().getResponse(mContext, toResolve, sl, cr);

                        request.setUtterance(outcome.getUtterance());
                        request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                        result = outcome.getOutcome();
                    } else {
                        request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                        request.setUtterance(PersonalityResponse.getSecureErrorResponse(mContext, sl));
                        result = Outcome.SUCCESS;
                    }

                    break;
                case COMMAND_EMOTION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_EMOTION.name());
                    }

                    if (!secure) {

                        outcome = new CommandEmotion().getResponse(mContext, sl);

                        request.setUtterance(outcome.getUtterance());
                        request.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                        request.setCondition(Condition.CONDITION_EMOTION);
                        result = outcome.getOutcome();
                    } else {
                        request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                        request.setUtterance(PersonalityResponse.getSecureErrorResponse(mContext, sl));
                        result = Outcome.SUCCESS;
                    }

                    break;
                case COMMAND_HOTWORD:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_HOTWORD.name());
                    }

                    //noinspection NewApi
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                            && !PermissionHelper.checkUsageStatsPermission(mContext)) {

                        if (ExecuteIntent.settingsIntent(mContext, IntentConstants.SETTINGS_USAGE_STATS)) {

                            request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                            request.setUtterance(SaiyResourcesHelper.getStringResource(mContext, sl,
                                    R.string.app_speech_usage_stats));
                        } else {

                            request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                            request.setUtterance(SaiyResourcesHelper.getStringResource(mContext, sl,
                                    R.string.issue_usage_stats_bug));
                        }
                    } else {
                        request.setAction(LocalRequest.ACTION_TOGGLE_HOTWORD);
                    }

                    result = Outcome.SUCCESS;

                    break;
                case COMMAND_VOICE_IDENTIFY:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_VOICE_IDENTIFY.name());
                    }

                    outcome = new CommandVocalRecognition().getResponse(mContext, sl);
                    result = outcome.getOutcome();
                    request.setUtterance(outcome.getUtterance());

                    if (result == Outcome.SUCCESS) {
                        request.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                        request.setCondition(Condition.CONDITION_IDENTIFY);
                        request.setIdentityProfile((String) outcome.getExtra());
                    } else {
                        qubit = outcome.getQubit();
                        request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                    }

                    break;
                case COMMAND_CONTACT:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_CONTACT.name());
                    }
                    final ai.saiy.android.command.contact.CommandContactLocal commandContact = new ai.saiy.android.command.contact.CommandContactLocal();
                    outcome = commandContact.getResponse(mContext, toResolve, sl, cr);
                    request.setUtterance(outcome.getUtterance());
                    request.setAction(outcome.getAction());
                    request.setCondition(outcome.getCondition());
                    if (outcome.getExtra() instanceof Parcelable) {
                        request.setParcelableObject((Parcelable) outcome.getExtra());
                    }
                    result = outcome.getOutcome();
                    break;
                case COMMAND_NAVIGATION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_NAVIGATION.name());
                    }
                    final ai.saiy.android.command.navigation.CommandNavigation commandNavigation = new ai.saiy.android.command.navigation.CommandNavigation();
                    outcome = commandNavigation.getResponse(mContext, toResolve, sl, cr);
                    request.setUtterance(outcome.getUtterance());
                    request.setAction(outcome.getAction());
                    result = outcome.getOutcome();
                    break;
                case COMMAND_TIME:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_TIME.name());
                    }
                    final ai.saiy.android.command.time.CommandTime commandTime = new ai.saiy.android.command.time.CommandTime();
                    outcome = commandTime.getResponse(mContext, toResolve, sl, cr);
                    request.setUtterance(outcome.getUtterance());
                    request.setAction(outcome.getAction());
                    result = outcome.getOutcome();
                    break;
                case COMMAND_REDIAL:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_REDIAL.name());
                    }
                    if (!secure) {
                        final ai.saiy.android.command.call.CommandRedial commandRedial = new ai.saiy.android.command.call.CommandRedial();
                        outcome = commandRedial.getResponse(mContext, toResolve, sl, cr);
                        request.setUtterance(outcome.getUtterance());
                        request.setAction(outcome.getAction());
                        result = outcome.getOutcome();
                    } else {
                        request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                        request.setUtterance(PersonalityResponse.getSecureErrorResponse(mContext, sl));
                        result = Outcome.SUCCESS;
                    }
                    break;
                case COMMAND_CALL_BACK:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_CALL_BACK.name());
                    }
                    if (!secure) {
                        final ai.saiy.android.command.call.CommandCallBack commandCallBack = new ai.saiy.android.command.call.CommandCallBack();
                        outcome = commandCallBack.getResponse(mContext, toResolve, sl, cr);
                        request.setUtterance(outcome.getUtterance());
                        request.setAction(outcome.getAction());
                        result = outcome.getOutcome();
                    } else {
                        request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                        request.setUtterance(PersonalityResponse.getSecureErrorResponse(mContext, sl));
                        result = Outcome.SUCCESS;
                    }
                    break;
                case COMMAND_HOROSCOPE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_HOROSCOPE.name());
                    }
                    final ai.saiy.android.command.horoscope.CommandHoroscope commandHoroscope = new ai.saiy.android.command.horoscope.CommandHoroscope();
                    outcome = commandHoroscope.getResponse(mContext, toResolve, sl, cr);
                    request.setUtterance(outcome.getUtterance());
                    request.setAction(outcome.getAction());
                    result = outcome.getOutcome();
                    break;
                case COMMAND_WEATHER:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_WEATHER.name());
                    }
                    final ai.saiy.android.command.weather.CommandWeather commandWeather = new ai.saiy.android.command.weather.CommandWeather();
                    outcome = commandWeather.getResponse(mContext, toResolve, sl, cr);
                    request.setUtterance(outcome.getUtterance());
                    request.setAction(outcome.getAction());
                    result = outcome.getOutcome();
                    break;
                case COMMAND_NOTE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_NOTE.name());
                    }
                    final CommandNote commandNote = new CommandNote();
                    outcome = commandNote.getResponse(mContext, toResolve, sl, cr);
                    request.setUtterance(outcome.getUtterance());
                    request.setAction(outcome.getAction());
                    result = outcome.getOutcome();
                    break;
                case COMMAND_ALARM:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_ALARM.name());
                    }
                    final ai.saiy.android.command.alarm.CommandAlarm commandAlarm = new ai.saiy.android.command.alarm.CommandAlarm();
                    outcome = commandAlarm.getResponse(mContext, toResolve, sl, cr);
                    request.setUtterance(outcome.getUtterance());
                    request.setAction(outcome.getAction());
                    result = outcome.getOutcome();
                    break;
                case COMMAND_CALENDAR:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_CALENDAR.name());
                    }
                    final ai.saiy.android.command.calendar.CommandCalendar commandCalendar = new ai.saiy.android.command.calendar.CommandCalendar();
                    outcome = commandCalendar.getResponse(mContext, toResolve, sl, cr);
                    request.setUtterance(outcome.getUtterance());
                    request.setAction(outcome.getAction());
                    result = outcome.getOutcome();
                    break;
                case COMMAND_SMS:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_SMS.name());
                    }
                    if (!secure) {
                        final ai.saiy.android.command.sms.CommandSms commandSms = new ai.saiy.android.command.sms.CommandSms();
                        outcome = commandSms.getResponse(mContext, toResolve, sl, cr);
                        request.setUtterance(outcome.getUtterance());
                        request.setAction(outcome.getAction());
                        result = outcome.getOutcome();
                    } else {
                        request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                        request.setUtterance(PersonalityResponse.getSecureErrorResponse(mContext, sl));
                        result = Outcome.SUCCESS;
                    }
                    break;
                case COMMAND_HELP:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_HELP.name());
                    }
                    final ai.saiy.android.command.help.CommandHelp commandHelp = new ai.saiy.android.command.help.CommandHelp();
                    outcome = commandHelp.getResponse(mContext, toResolve, sl, cr);
                    qubit = outcome.getQubit();
                    request.setUtterance(outcome.getUtterance());
                    request.setAction(outcome.getAction());
                    result = outcome.getOutcome();
                    break;
                case COMMAND_DRIVING:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_DRIVING.name());
                    }
                    if (!secure) {
                        final ai.saiy.android.command.driving.CommandDriving commandDriving = new ai.saiy.android.command.driving.CommandDriving();
                        outcome = commandDriving.getResponse(mContext, toResolve, sl, cr, vrLocale, ttsLocale);
                        request.setUtterance(outcome.getUtterance());
                        request.setAction(outcome.getAction());
                        result = outcome.getOutcome();
                    } else {
                        request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                        request.setUtterance(PersonalityResponse.getSecureErrorResponse(mContext, sl));
                        result = Outcome.SUCCESS;
                    }
                    break;
                case COMMAND_TWITTER:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_TWITTER.name());
                    }
                    if (!secure) {
                        final CommandTwitter commandTwitter = new CommandTwitter();
                        outcome = commandTwitter.getResponse(mContext, toResolve, sl, cr);
                        request.setUtterance(outcome.getUtterance());
                        request.setAction(outcome.getAction());
                        request.setCondition(outcome.getCondition());
                        if (outcome.getExtra() instanceof Parcelable) {
                            request.setParcelableObject((Parcelable) outcome.getExtra());
                        }
                        result = outcome.getOutcome();
                    } else {
                        request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                        request.setUtterance(PersonalityResponse.getSecureErrorResponse(mContext, sl));
                        result = Outcome.SUCCESS;
                    }
                    break;
                case COMMAND_FOURSQUARE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_FOURSQUARE.name());
                    }
                    if (!secure) {
                        final CommandFoursquare commandFoursquare = new CommandFoursquare();
                        outcome = commandFoursquare.getResponse(mContext, toResolve, sl, cr);
                        request.setUtterance(outcome.getUtterance());
                        request.setAction(outcome.getAction());
                        request.setCondition(outcome.getCondition());
                        if (outcome.getExtra() instanceof Parcelable) {
                            request.setParcelableObject((Parcelable) outcome.getExtra());
                        }
                        result = outcome.getOutcome();
                    } else {
                        request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                        request.setUtterance(PersonalityResponse.getSecureErrorResponse(mContext, sl));
                        result = Outcome.SUCCESS;
                    }
                    break;
                case COMMAND_FLOAT_COMMANDS:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_FLOAT_COMMANDS.name());
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(mContext)) {
                        request.setUtterance(SaiyResourcesHelper.getStringResource(mContext, sl, R.string.permission_system_alert));
                        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + mContext.getPackageName()));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    } else {
                        request.setUtterance(PersonalityResponse.getGenericAcknowledgement(mContext, sl));
                        SelfAwareHelper.startFloatingService(mContext, ai.saiy.android.tutorial.Tutorial.TUTORIAL_WINDOW_ID);
                    }
                    request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                    result = Outcome.SUCCESS;
                    break;
                case COMMAND_ALEXA:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ai.saiy.android.amazon.TokenHelper.hasToken(mContext)) {
                            final CommandAlexa commandAlexa = new CommandAlexa();
                            outcome = commandAlexa.getResponse(mContext, toResolve, sl, cr);
                            result = outcome.getOutcome();
                            if (result == Outcome.SUCCESS) {
                                request.setAction(LocalRequest.ACTION_ALEXA_TTS);
                                request.setUtteranceArray(toResolve);
                                final ArrayList<String> resultsArray = new ArrayList<>();
                                resultsArray.add(outcome.getUtterance());
                                request.getBundle().putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, resultsArray);
                                request.getBundle().putFloatArray(SpeechRecognizer.CONFIDENCE_SCORES, new float[] {0.9f});
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "DT " + CC.COMMAND_ALEXA.name() + ": have request");
                                }
                            } else {
                                request.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                                request.setUtterance(PersonalityResponse.getAlexaIntro(mContext, sl));
                                request.setRecognitionProvider(SaiyDefaults.VR.ALEXA);
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "DT " + CC.COMMAND_ALEXA.name() + ": starting request");
                                }
                            }
                        } else {
                            request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                            request.getBundle().putInt(ActivityHome.FRAGMENT_INDEX, ActivityHome.INDEX_FRAGMENT_SUPPORTED_APPS);
                            request.setUtterance(SaiyResourcesHelper.getStringResource(mContext, sl, R.string.amazon_notification_auth_request));
                            ExecuteIntent.saiyActivity(mContext, ActivityHome.class, request.getBundle(), true);
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "DT " + CC.COMMAND_ALEXA.name() + ": no auth");
                            }
                        }
                    } else {
                        request.setUtterance(ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(mContext, sl, R.string.error_alexa_marshmallow));
                        request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                        result = Outcome.SUCCESS;
                    }
                    break;
                case COMMAND_UNKNOWN:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_UNKNOWN.name());
                    }
                    final int unknownAction = cr.isAlexaTTS()? Unknown.UNKNOWN_ALEXA : SPH.getCommandUnknownAction(mContext);
                    switch (unknownAction) {
                        case Unknown.UNKNOWN_STATE:
                        case Unknown.UNKNOWN_REPEAT:
                            if (SPH.getToastUnknown(mContext)) {
                                final EntangledPair entangledPair = new EntangledPair(Position.TOAST_SHORT,
                                        CC.COMMAND_UNKNOWN);
                                entangledPair.setToastContent(toResolve.get(0) + "??");
                                publishProgress(entangledPair);
                            }
                            break;
                    }

                    request.setAction(LocalRequest.ACTION_SPEAK_ONLY);

                    switch (unknownAction) {
                        case Unknown.UNKNOWN_STATE:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "Unknown.UNKNOWN_STATE");
                            }
                            request.setUtterance(PersonalityResponse.getNoComprende(mContext, sl));
                            break;
                        case Unknown.UNKNOWN_REPEAT:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "Unknown.UNKNOWN_STATE");
                            }
                            request.setUtterance(PersonalityResponse.getRepeatCommand(mContext, sl));
                            request.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                            break;
                        case Unknown.UNKNOWN_WEB_SEARCH:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "Unknown.UNKNOWN_WEB_SEARCH");
                            }
                            if (ExecuteIntent.webSearch(mContext, "http://google." + ai.saiy.android.localisation.SaiyWebHelper.extension(SaiyWebHelper.GOOGLE, sl) + "/search?q=" + toResolve.get(0).trim().replaceAll("\\s", "%20"))) {
                                request.setUtterance(PersonalityResponse.getSearchConfirm(mContext, sl, mContext.getString(R.string.google), toResolve.get(0).trim()));
                            } else {
                                request.setUtterance(PersonalityResponse.getSearchError(mContext, sl));
                            }
                            break;
                        case Unknown.UNKNOWN_GOOGLE_ASSISTANT:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "Unknown.UNKNOWN_GOOGLE_ASSISTANT");
                            }

                            if (!ExecuteIntent.googleNow(mContext, toResolve.get(0))) {
                                request.setUtterance(PersonalityResponse.getNoComprende(mContext, sl));
                                SPH.setCommandUnknownAction(mContext, Unknown.UNKNOWN_STATE);

                                final EntangledPair entangledPair = new EntangledPair(Position.TOAST_SHORT, CC.COMMAND_UNKNOWN);
                                entangledPair.setToastContent(mContext.getString(R.string.error_google_now_broadcast));
                                publishProgress(entangledPair);
                            }
                            break;
                        case Unknown.UNKNOWN_ALEXA:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "Unknown.UNKNOWN_ALEXA");
                            }
                            if (Network.isConnected(mContext)) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "Unknown.UNKNOWN_ALEXA: isAlexaTTS: " + cr.isAlexaTTS());
                                }
                                Bundle alexaBundle = null;
                                for (int i = 0; i < 10; ++i) {
                                    alexaBundle = ai.saiy.android.utils.Global.getAlexDirectiveBundle();
                                    if (UtilsBundle.notNaked(alexaBundle)) {
                                        break;
                                    }
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "Unknown.UNKNOWN_ALEXA: sleeping");
                                    }
                                    try {
                                        java.lang.Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "Unknown.UNKNOWN_ALEXA: InterruptedException");
                                        }
                                    }
                                }
                                if (UtilsBundle.notNaked(alexaBundle)) {
                                    final int action = alexaBundle.getInt(LocalRequest.EXTRA_ACTION);
                                    request.setAction(action);
                                    if (action == LocalRequest.ACTION_SPEAK_LISTEN) {
                                        request.setRecognitionProvider(SaiyDefaults.VR.ALEXA);
                                    }
                                    if (alexaBundle.containsKey(SaiyRecognitionListener.ALEXA_DIRECTIVE)) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "Unknown.UNKNOWN_ALEXA: containsKey: ALEXA_DIRECTIVE");
                                        }
                                        ai.saiy.android.amazon.directives.DirectiveType directiveType = alexaBundle.getParcelable(SaiyRecognitionListener.ALEXA_DIRECTIVE);
                                        if (directiveType == null) {
                                            if (DEBUG) {
                                                MyLog.w(CLS_NAME, "Unknown.UNKNOWN_ALEXA: DirectiveType null");
                                            }
                                        } else {
                                            switch (directiveType) {
                                                case DIRECTIVE_CANCEL:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "Unknown.UNKNOWN_ALEXA: DIRECTIVE_CANCEL");
                                                    }
                                                    request.setUtterance(PersonalityResponse.getCancelled(mContext, sl));
                                                    break;
                                                case DIRECTIVE_ABANDON:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "Unknown.UNKNOWN_ALEXA: DIRECTIVE_ABANDON");
                                                    }
                                                    alexaBundle.putString(SaiyRecognitionListener.ALEX_FILE, null);
                                                    break;
                                                case DIRECTIVE_VOLUME:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "Unknown.UNKNOWN_ALEXA: DIRECTIVE_VOLUME");
                                                    }
                                                    request.setUtterance(PersonalityResponse.getAlexaVolumeInsert(mContext, sl));
                                                    break;
                                                case DIRECTIVE_MEDIA:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "Unknown.UNKNOWN_ALEXA: DIRECTIVE_MEDIA");
                                                    }
                                                    request.setUtterance(SaiyResourcesHelper.getStringResource(mContext, sl, R.string.alexa_media_response));
                                                    break;
                                                default:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "Unknown.UNKNOWN_ALEXA: DIRECTIVE_NONE");
                                                    }
                                                    break;
                                            }
                                        }
                                    }
                                    final String pathOfFile = alexaBundle.getString(SaiyRecognitionListener.ALEX_FILE, null);
                                    if (UtilsString.notNaked(pathOfFile)) {
                                        request.setAlexaFilePath(pathOfFile);
                                        request.setUtterance(SaiyRecognitionListener.ALEX_SPEECH);
                                    } else {
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "Unknown.UNKNOWN_ALEXA: ALEXA_FILE null");
                                        }
                                        if (!UtilsString.notNaked(request.getUtterance())) {
                                            request.setUtterance(PersonalityResponse.getNoComprendeForAlexa(mContext, sl));
                                        }
                                    }
                                } else {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "Unknown.UNKNOWN_ALEXA: alexaBundle null");
                                    }
                                    request.setUtterance(PersonalityResponse.getNoComprendeForAlexa(mContext, sl));
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "Unknown.UNKNOWN_ALEXA: no network");
                                }
                                request.setUtterance(PersonalityResponse.getAlexaNoNetwork(mContext, sl));
                            }
                            break;
                        case Unknown.UNKNOWN_MICROSOFT_CORTANA:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "Unknown.UNKNOWN_MICROSOFT_CORTANA");
                            }
                            if (!ExecuteIntent.microsoftCortana(mContext, toResolve.get(0))) {
                                request.setUtterance(PersonalityResponse.getNoComprende(mContext, sl));
                                SPH.setCommandUnknownAction(mContext, Unknown.UNKNOWN_STATE);

                                final EntangledPair entangledPair = new EntangledPair(Position.TOAST_SHORT, CC.COMMAND_UNKNOWN);
                                entangledPair.setToastContent(mContext.getString(R.string.error_microsoft_cortana_broadcast));
                                publishProgress(entangledPair);
                            }
                            break;
                        case Unknown.UNKNOWN_WOLFRAM_ALPHA:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "Unknown.UNKNOWN_WOLFRAM_ALPHA");
                            }

                            if (!ExecuteIntent.wolframAlpha(mContext, toResolve.get(0))) {
                                request.setUtterance(PersonalityResponse.getNoComprende(mContext, sl));
                                SPH.setCommandUnknownAction(mContext, Unknown.UNKNOWN_STATE);

                                final EntangledPair entangledPair = new EntangledPair(Position.TOAST_SHORT, CC.COMMAND_UNKNOWN);
                                entangledPair.setToastContent(mContext.getString(R.string.error_wolfram_alpha_broadcast));
                                publishProgress(entangledPair);
                            }
                            break;
                        case Unknown.UNKNOWN_TASKER:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "Unknown.UNKNOWN_TASKER");
                            }

                            if (!TaskerHelper.broadcastVoiceData(mContext, toResolve)) {
                                request.setUtterance(PersonalityResponse.getNoComprende(mContext, sl));
                                SPH.setCommandUnknownAction(mContext, Unknown.UNKNOWN_STATE);

                                final EntangledPair entangledPair = new EntangledPair(Position.TOAST_SHORT, CC.COMMAND_UNKNOWN);
                                entangledPair.setToastContent(mContext.getString(R.string.error_tasker_broadcast));
                                publishProgress(entangledPair);
                            }
                            break;
                    }
                    break;
                case COMMAND_EMPTY_ARRAY:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_EMPTY_ARRAY.name());
                    }

                    request.setUtterance(PersonalityResponse.getErrorProfanityFilter(mContext, sl));
                    request.setAction(LocalRequest.ACTION_SPEAK_ONLY);

                    break;
                case COMMAND_USER_CUSTOM:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_USER_CUSTOM.name());
                    }

                    if (!secure) {

                        outcome = new CommandCustom().getResponse(mContext, cch.getCommand(), sl, cr);

                        request.setUtterance(outcome.getUtterance());
                        request.setAction(outcome.getAction());
                        result = outcome.getOutcome();
                    } else {
                        request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                        request.setUtterance(PersonalityResponse.getSecureErrorResponse(mContext, sl));
                        result = Outcome.SUCCESS;
                    }

                    break;
                case COMMAND_SOMETHING_WEIRD:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "DT " + CC.COMMAND_SOMETHING_WEIRD.name());
                    }

                    request.setUtterance(PersonalityResponse.getNoComprende(mContext, sl));
                    request.setAction(LocalRequest.ACTION_SPEAK_ONLY);

                    break;
                default:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "DT DEFAULT");
                    }

                    request.setUtterance(PersonalityResponse.getNoComprende(mContext, sl));
                    request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                    request.setCommand(CC.COMMAND_UNKNOWN);

                    break;
            }
            if (eventExtra != null) {
                eventExtra.putBoolean("command_outcome_success", (Outcome.SUCCESS == result));
                eventExtra.putBoolean("secure_request", secure);
                UtilsAnalytic.onCommandComplete(mContext, FirebaseAnalytics.getInstance(mContext), eventExtra);
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "DT networkProceed failed");
            }

            request.setUtterance(PersonalityResponse.getNoNetwork(mContext, sl));
            request.setAction(LocalRequest.ACTION_SPEAK_ONLY);
            request.setCommand(CC.COMMAND_UNKNOWN);
        }

        return qubit;
    }

    @Override
    protected void onSuperposition(final Qubit qubit) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onSuperposition secure: " + secure);
        }

        if (validateQubit(qubit)) {

            switch (COMMAND) {

                case COMMAND_CANCEL:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_CANCEL.name());
                    }
                    break;
                case COMMAND_SPELL:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_SPELL.name());
                    }

                    if (result == Outcome.SUCCESS) {
                        ClipboardHelper.setClipboardContent(mContext, qubit.getSpellContent());
                    }

                    break;
                case COMMAND_TRANSLATE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_TRANSLATE.name());
                    }

                    if (result == Outcome.SUCCESS) {
                        ClipboardHelper.setClipboardContent(mContext, qubit.getTranslatedText());
                    }

                    break;
                case COMMAND_PARDON:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_PARDON.name());
                    }
                    break;
                case COMMAND_USER_NAME:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_USER_NAME.name());
                    }
                    break;
                case COMMAND_BATTERY:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_BATTERY.name());
                    }
                    break;
                case COMMAND_SONG_RECOGNITION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_SONG_RECOGNITION.name());
                    }

                    if (result == Outcome.FAILURE) {
                        Intent intent = new Intent(mContext, ActivityChooserDialog.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        final Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList(SongRecognitionChooser.PARCEL_KEY,
                                qubit.getSongRecognitionChooserList());
                        intent.putExtras(bundle);

                        mContext.startActivity(intent);
                    }

                    break;
                case COMMAND_WOLFRAM_ALPHA:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_WOLFRAM_ALPHA.name());
                    }
                    break;
                case COMMAND_TASKER:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_TASKER.name());
                    }
                    break;
                case COMMAND_EMOTION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_EMOTION.name());
                    }
                    break;
                case COMMAND_HOTWORD:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_HOTWORD.name());
                    }
                    break;
                case COMMAND_VOICE_IDENTIFY:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_VOICE_IDENTIFY.name());
                    }

                    if (result == Outcome.FAILURE) {
                        final Bundle bundle = new Bundle();
                        bundle.putInt(ActivityHome.FRAGMENT_INDEX, ActivityHome.INDEX_FRAGMENT_SUPER_USER);
                        ExecuteIntent.saiyActivity(mContext, ActivityHome.class, bundle, true);
                    }

                    break;
                case COMMAND_CONTACT:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_CONTACT.name());
                    }
                    break;
                case COMMAND_NAVIGATION:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_NAVIGATION.name());
                    }
                    break;
                case COMMAND_TIME:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_TIME.name());
                    }
                    break;
                case COMMAND_REDIAL:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_REDIAL.name());
                    }
                    break;
                case COMMAND_CALL_BACK:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_CALL_BACK.name());
                    }
                    break;
                case COMMAND_HOROSCOPE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_HOROSCOPE.name());
                    }
                    break;
                case COMMAND_WEATHER:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_WEATHER.name());
                    }
                    break;
                case COMMAND_NOTE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_NOTE.name());
                    }
                    break;
                case COMMAND_ALARM:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_ALARM.name());
                    }
                    break;
                case COMMAND_CALENDAR:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_CALENDAR.name());
                    }
                    break;
                case COMMAND_SMS:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_SMS.name());
                    }
                    break;
                case COMMAND_HELP:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_HELP.name());
                    }
                    Bundle bundle = new Bundle();
                    bundle.putInt(ActivityHome.FRAGMENT_INDEX, ActivityHome.INDEX_FRAGMENT_COMMANDS);
                    ExecuteIntent.saiyActivity(mContext, ActivityHome.class, bundle, true);
                    break;
                case COMMAND_DRIVING:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_DRIVING.name());
                    }
                    break;
                case COMMAND_TWITTER:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_TWITTER.name());
                    }
                    break;
                case COMMAND_FOURSQUARE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_FOURSQUARE.name());
                    }
                    break;
                case COMMAND_FLOAT_COMMANDS:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_FLOAT_COMMANDS.name());
                    }
                    break;
                case COMMAND_ALEXA:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_ALEXA.name());
                    }
                    break;
                case COMMAND_UNKNOWN:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_UNKNOWN.name());
                    }
                    break;
                case COMMAND_EMPTY_ARRAY:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_EMPTY_ARRAY.name());
                    }
                    break;
                case COMMAND_USER_CUSTOM:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_USER_CUSTOM.name());
                    }
                    break;
                case COMMAND_SOMETHING_WEIRD:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "OSP " + CC.COMMAND_SOMETHING_WEIRD.name());
                    }
                    break;
                default:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "OSP CC.DEFAULT");
                    }
                    break;
            }
        }

        NotificationHelper.cancelComputingNotification(mContext);

        if (DEBUG) {
            MyLog.getElapsed(Quantum.class.getSimpleName(), then);
        }

        request.execute();
    }


    @Override
    protected void onEntanglement(final EntangledPair entangledPair) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onEntanglement secure: " + secure);
        }

        if (validatePosition(entangledPair)) {

            switch (entangledPair.getPosition()) {

                case TOAST_SHORT:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Position " + entangledPair.getPosition().name());
                        MyLog.i(CLS_NAME, "Position " + entangledPair.getCC().name());
                        MyLog.i(CLS_NAME, "Position getToastContent: " + entangledPair.getToastContent());
                    }
                    Toast.makeText(mContext, entangledPair.getToastContent(), Toast.LENGTH_SHORT).show();
                    break;
                case TOAST_LONG:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Position " + entangledPair.getPosition().name());
                        MyLog.i(CLS_NAME, "Position " + entangledPair.getCC().name());
                        MyLog.i(CLS_NAME, "Position getToastContent: " + entangledPair.getToastContent());
                    }
                    Toast.makeText(mContext, entangledPair.getToastContent(), Toast.LENGTH_LONG).show();
                    break;
                case SPEAK:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Position " + entangledPair.getPosition().name());
                        MyLog.i(CLS_NAME, "Position " + entangledPair.getCC().name());
                    }

                    final LocalRequest opuRequest = new LocalRequest(mContext);
                    opuRequest.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                    opuRequest.setUtterance(entangledPair.getUtterance());
                    opuRequest.setTTSLocale(ttsLocale);
                    opuRequest.setVRLocale(vrLocale);
                    opuRequest.setSupportedLanguage(sl);
                    opuRequest.execute();

                    break;
                case CLIPBOARD:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Position " + entangledPair.getPosition().name());
                        MyLog.i(CLS_NAME, "Position " + entangledPair.getCC().name());
                    }

                    ClipboardHelper.saveClipboardContent(mContext);

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Position: CLIPBOARD content: " + ClipboardHelper.getClipboardContent());
                    }
                    break;
                case SHOW_COMPUTING:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Position " + entangledPair.getPosition().name());
                        MyLog.i(CLS_NAME, "Position " + entangledPair.getCC().name());
                        MyLog.i(CLS_NAME, "Position isAlexaTTS: " + entangledPair.isAlexaTTS());
                    }
                    if (entangledPair.isAlexaTTS()) {
                        NotificationHelper.createFetchingNotification(mContext);
                    } else {
                        NotificationHelper.createComputingNotification(mContext);
                    }
                    break;
                default:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "Position: default");
                    }
                    break;
            }
        }
    }
}
