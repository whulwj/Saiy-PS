package ai.saiy.android.command.audio;

import android.content.Context;
import android.media.AudioManager;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;

public class CommandAudio {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandAudio.class.getSimpleName();

    public @NonNull Outcome getResponse(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage, @NonNull ai.saiy.android.command.helper.CommandRequest cr) {
        CommandAudioValues commandAudioValues;
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        final long then = System.nanoTime();
        final Outcome outcome = new Outcome();
        if (!ai.saiy.android.permissions.PermissionHelper.checkNotificationPolicyPermission(context)) {
            ai.saiy.android.intent.ExecuteIntent.settingsIntent(context, IntentConstants.NOTIFICATION_POLICY_ACCESS_SETTINGS);
            outcome.setUtterance(ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.app_speech_notification_policy));
            outcome.setOutcome(Outcome.FAILURE);
            return outcome;
        }
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
            commandAudioValues = (CommandAudioValues) cr.getVariableData();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            commandAudioValues = new Audio(supportedLanguage).sortAudio(context, voiceData);
        }
        if (commandAudioValues != null) {
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            switch (commandAudioValues.getType()) {
                case SILENT:
                    if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                        outcome.setUtterance(PersonalityResponse.getAudioAlready(context, supportedLanguage, commandAudioValues.getDescription()));
                        outcome.setOutcome(Outcome.SUCCESS);
                    } else {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                            outcome.setUtterance(PersonalityResponse.getAudioError(context, supportedLanguage, commandAudioValues.getDescription()));
                            outcome.setOutcome(Outcome.FAILURE);
                        } else {
                            outcome.setUtterance(context.getString(R.string.okay));
                            outcome.setOutcome(Outcome.SUCCESS);
                        }
                    }
                    break;
                case VIBRATE:
                    if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                        outcome.setUtterance(PersonalityResponse.getAudioAlready(context, supportedLanguage, commandAudioValues.getDescription()));
                        outcome.setOutcome(Outcome.SUCCESS);
                    } else {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE) {
                            outcome.setUtterance(PersonalityResponse.getAudioError(context, supportedLanguage, commandAudioValues.getDescription()));
                            outcome.setOutcome(Outcome.FAILURE);
                        } else {
                            outcome.setUtterance(context.getString(R.string.okay));
                            outcome.setOutcome(Outcome.SUCCESS);
                        }
                    }
                    break;
                case NORMAL:
                    if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                        outcome.setUtterance(PersonalityResponse.getAudioAlready(context, supportedLanguage, commandAudioValues.getDescription()));
                        outcome.setOutcome(Outcome.SUCCESS);
                    } else {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                            outcome.setUtterance(PersonalityResponse.getAudioError(context, supportedLanguage, commandAudioValues.getDescription()));
                            outcome.setOutcome(Outcome.FAILURE);
                        } else {
                            outcome.setUtterance(PersonalityResponse.getAudioConfirm(context, supportedLanguage, commandAudioValues.getDescription()));
                            outcome.setOutcome(Outcome.SUCCESS);
                        }
                    }
                    break;
                default:
                    outcome.setUtterance(PersonalityResponse.getAudioUnknownError(context, supportedLanguage));
                    outcome.setOutcome(Outcome.FAILURE);
                    break;
            }
        } else {
            outcome.setUtterance(PersonalityResponse.getAudioUnknownError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }
}
