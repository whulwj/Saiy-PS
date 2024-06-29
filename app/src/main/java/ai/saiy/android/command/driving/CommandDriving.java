package ai.saiy.android.command.driving;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.utils.MyLog;

public class CommandDriving {
    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandDriving.class.getSimpleName();

    private long then;

    /**
     * A single point of return to check the elapsed time for debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(@NonNull Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, this.then);
        }
        return outcome;
    }

    public static void openAdvancedSettings(Context context) {
        Bundle bundle = new Bundle();
        bundle.putInt(ActivityHome.FRAGMENT_INDEX, ActivityHome.INDEX_FRAGMENT_ADVANCED_SETTINGS);
        ai.saiy.android.intent.ExecuteIntent.saiyActivity(context, ActivityHome.class, bundle, true);
    }

    public static boolean haveDrivingProfilePermissions(Context context, DrivingProfile drivingProfile) {
        final boolean announceNotifications = drivingProfile.getAnnounceNotifications();
        if (announceNotifications && !ai.saiy.android.service.helper.SelfAwareHelper.saiyAccessibilityRunning(context)) {
            return false;
        }
        if (announceNotifications) {
            ai.saiy.android.service.helper.SelfAwareHelper.startAccessibilityService(context);
        }
        final boolean announceCallerId = drivingProfile.getAnnounceCallerId();
        if (!announceCallerId || ai.saiy.android.permissions.PermissionHelper.checkNotificationPolicyPermission(context)) {
            return !announceCallerId || ai.saiy.android.permissions.PermissionHelper.checkAnnounceCallerPermissionsNR(context);
        }
        return false;
    }

    public Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage sl, ai.saiy.android.command.helper.CommandRequest cr, Locale vrLocale, Locale ttsLocale) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        Outcome outcome = new Outcome();
        outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        outcome.setOutcome(Outcome.SUCCESS);
        final CommandDrivingValues commandDrivingValues = new Driving(sl).fetch(context, voiceData);
        final DrivingProfile drivingProfile = DrivingProfileHelper.getDrivingProfile(context);
        final boolean isEnabled = drivingProfile.isEnabled();
        ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, sl);
        final String enabled = sr.getString(R.string.enabled);
        final String disabled = sr.getString(R.string.disabled);
        final String drivingProfileResponse = sr.getString(R.string.driving_profile_response);
        final String drivingProfileAlreadyResponse = sr.getString(R.string.driving_profile_already_response);
        final String drivingProfileMissingPermissions = sr.getString(R.string.driving_profile_missing_permissions);
        sr.reset();
        boolean toggleDrivingProfile = true;
        switch (commandDrivingValues.getAction()) {
            case ENABLE:
                if (!isEnabled) {
                    if (!haveDrivingProfilePermissions(context, drivingProfile)) {
                        outcome.setUtterance(drivingProfileMissingPermissions);
                        toggleDrivingProfile = false;
                        openAdvancedSettings(context);
                    } else {
                        outcome.setUtterance(String.format(drivingProfileResponse, enabled));
                    }
                } else {
                    outcome.setUtterance(String.format(drivingProfileAlreadyResponse, enabled));
                    toggleDrivingProfile = false;
                }
                break;
            case DISABLE:
                ai.saiy.android.utils.SPH.setDrivingCooldownTime(context, System.currentTimeMillis());
                if (!isEnabled) {
                    outcome.setUtterance(String.format(drivingProfileAlreadyResponse, disabled));
                    toggleDrivingProfile = false;
                } else {
                    outcome.setUtterance(String.format(drivingProfileResponse, disabled));
                }
                break;
            default:
                if (!isEnabled) {
                    if (!haveDrivingProfilePermissions(context, drivingProfile)) {
                        outcome.setUtterance(drivingProfileMissingPermissions);
                        toggleDrivingProfile = false;
                        openAdvancedSettings(context);
                    } else {
                        outcome.setUtterance(String.format(drivingProfileResponse, enabled));
                    }
                } else {
                    outcome.setUtterance(String.format(drivingProfileResponse, disabled));
                }
                break;
        }
        if (toggleDrivingProfile) {
            LocalRequest localRequest = new LocalRequest(context);
            localRequest.prepareDefault(LocalRequest.ACTION_TOGGLE_DRIVING_PROFILE, sl, vrLocale, ttsLocale, "silence");
            localRequest.execute();
        }
        return returnOutcome(outcome);
    }
}
