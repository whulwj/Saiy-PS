package ai.saiy.android.command.orientation;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;

public class CommandOrientation {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandOrientation.class.getSimpleName();

    public @NonNull Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        final long then = System.nanoTime();
        final Outcome outcome = new Outcome();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
            if (ai.saiy.android.intent.ExecuteIntent.settingsIntent(context, IntentConstants.MANAGE_WRITE_SETTINGS)) {
                outcome.setUtterance(context.getString(R.string.permission_write_settings));
            } else {
                ai.saiy.android.applications.UtilsApplication.openApplicationSpecificSettings(context, context.getPackageName());
                outcome.setUtterance(context.getString(R.string.settings_missing, context.getString(R.string.content_modify_system_settings)));
            }
            outcome.setOutcome(Outcome.SUCCESS);
        } else if (!cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            final OrientationHelper orientationHelper = new OrientationHelper();
            final CommandOrientationValues commandOrientationValues = new Orientation(supportedLanguage).detectOrientation(context, voiceData);
            switch (commandOrientationValues.getType()) {
                case PORTRAIT:
                    if (orientationHelper.getRotation(context) != Surface.ROTATION_0) {
                        if (orientationHelper.freezeAutoRotation(context)) {
                            Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                            outcome.setOutcome(Outcome.SUCCESS);
                            outcome.setUtterance(commandOrientationValues.getDescription());
                        } else {
                            outcome.setOutcome(Outcome.FAILURE);
                            outcome.setUtterance(PersonalityResponse.getOrientationError(context, supportedLanguage));
                        }
                    } else {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(PersonalityResponse.getCurrentOrientation(context, supportedLanguage, commandOrientationValues.getDescription()));
                    }
                    break;
                case LANDSCAPE:
                    if (orientationHelper.getRotation(context) != Surface.ROTATION_90) {
                        if (orientationHelper.freezeAutoRotation(context)) {
                            Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);
                            outcome.setOutcome(Outcome.SUCCESS);
                            outcome.setUtterance(commandOrientationValues.getDescription());
                        } else {
                            outcome.setOutcome(Outcome.FAILURE);
                            outcome.setUtterance(PersonalityResponse.getOrientationError(context, supportedLanguage));
                        }
                    } else {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(PersonalityResponse.getCurrentOrientation(context, supportedLanguage, commandOrientationValues.getDescription()));
                    }
                    break;
                case REVERSE_PORTRAIT:
                    if (orientationHelper.getRotation(context) != Surface.ROTATION_180) {
                        if (orientationHelper.freezeAutoRotation(context)) {
                            Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
                            outcome.setOutcome(Outcome.SUCCESS);
                            outcome.setUtterance(commandOrientationValues.getDescription());
                        } else {
                            outcome.setOutcome(Outcome.FAILURE);
                            outcome.setUtterance(PersonalityResponse.getOrientationError(context, supportedLanguage));
                        }
                    } else {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(PersonalityResponse.getCurrentOrientation(context, supportedLanguage, commandOrientationValues.getDescription()));
                    }
                    break;
                case REVERSE_LANDSCAPE:
                    if (orientationHelper.getRotation(context) != Surface.ROTATION_270) {
                        if (orientationHelper.freezeAutoRotation(context)) {
                            Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);
                            outcome.setOutcome(Outcome.SUCCESS);
                            outcome.setUtterance(commandOrientationValues.getDescription());
                        } else {
                            outcome.setOutcome(Outcome.FAILURE);
                            outcome.setUtterance(PersonalityResponse.getOrientationError(context, supportedLanguage));
                        }
                    } else {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(PersonalityResponse.getCurrentOrientation(context, supportedLanguage, commandOrientationValues.getDescription()));
                    }
                    break;
                case LOCK:
                    if (orientationHelper.isAutoRotationFrozen(context)) {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(PersonalityResponse.getCurrentOrientationLock(context, supportedLanguage, commandOrientationValues.getDescription()));
                    } else {
                        orientationHelper.fixedToUserRotation(context);
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(PersonalityResponse.getOrientationLock(context, supportedLanguage, commandOrientationValues.getDescription()));
                    }
                    break;
                case UNLOCK:
                    if (!orientationHelper.isAutoRotationFrozen(context)) {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(PersonalityResponse.getCurrentOrientationLock(context, supportedLanguage, commandOrientationValues.getDescription()));
                    } else {
                        orientationHelper.thawAutoRotation(context);
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(PersonalityResponse.getOrientationLock(context, supportedLanguage, commandOrientationValues.getDescription()));
                    }
                    break;
                case UNKNOWN:
                case SOMERSAULT_FORWARD:
                case SOMERSAULT_BACKWARD:
                    outcome.setOutcome(Outcome.FAILURE);
                    outcome.setUtterance(PersonalityResponse.getUnknownOrientationError(context, supportedLanguage));
                    break;
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "isResolved: true");
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }
}
