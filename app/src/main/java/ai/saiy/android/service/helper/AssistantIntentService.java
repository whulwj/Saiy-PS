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

package ai.saiy.android.service.helper;

import android.app.IntentService;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.Set;

import ai.saiy.android.R;
import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.command.driving.CommandDriving;
import ai.saiy.android.command.driving.DrivingProfileHelper;
import ai.saiy.android.command.settings.SettingsIntent;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.localisation.SaiyResourcesHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityHelper;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

/**
 * Class to handle the various available voice interaction intents:
 * <p>
 * {@link RecognizerIntent#ACTION_VOICE_SEARCH_HANDS_FREE}
 * {@link Intent#ACTION_VOICE_COMMAND}
 * {@link Intent#ACTION_SEARCH_LONG_PRESS}
 * {@link Intent#ACTION_VOICE_COMMAND}
 * {@link Intent#ACTION_ASSIST}
 * <p>
 * Created by benrandall76@gmail.com on 15/08/2016.
 */

public class AssistantIntentService extends IntentService {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = AssistantIntentService.class.getSimpleName();

    private static final String EXTRA_ASSIST_CONTEXT = "android.intent.extra.ASSIST_CONTEXT";
    private static final String ACTION_ASSIST_SEARCH = "com.google.android.gms.actions.SEARCH_ACTION";
    public static final String ACTION_WIDGET_ASSIST = "ai.saiy.android.action.WIDGET_ASSIST";
    private static final String ACTION_QL_ASSIST = "ai.saiy.android.action.QL_ASSIST";
    private static final String ACTION_ALEXA = "ai.saiy.android.action.ALEXA";
    private static final String ACTION_HOTWORD = "ai.saiy.android.HOTWORD";
    private static final String ACTION_DRIVING ="ai.saiy.android.DRIVING";
    private static final String ACTION_NOTIFICATIONS = "ai.saiy.android.NOTIFICATIONS";

    private long then;

    public AssistantIntentService() {
        super(AssistantIntentService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }

        then = System.nanoTime();
        super.onCreate();
    }

    public void showToast(final String text, final int duration) {
        if (UtilsString.notNaked(text)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    ai.saiy.android.utils.UtilsToast.showToast(AssistantIntentService.this.getApplicationContext(), text, duration);
                }
            });
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "showToast: naked String: ignoring");
        }
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onHandleIntent");
            examineIntent(intent);
        }

        final Bundle actionBundle = new Bundle();
        actionBundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_LISTEN);

        if (intent != null) {
            final Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if (bundle.containsKey(LocalRequest.EXTRA_RECOGNITION_LANGUAGE)) {

                    final String vrLocale = bundle.getString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE);

                    if (UtilsString.notNaked(vrLocale)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onHandleIntent: EXTRA_RECOGNITION_LANGUAGE: " + vrLocale);
                        }
                        actionBundle.putString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE, vrLocale);
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onHandleIntent: EXTRA_RECOGNITION_LANGUAGE naked");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onHandleIntent: no EXTRA_RECOGNITION_LANGUAGE");
                    }
                }

                if (bundle.containsKey(LocalRequest.EXTRA_TTS_LANGUAGE)) {

                    final String ttsLocale = bundle.getString(LocalRequest.EXTRA_TTS_LANGUAGE);

                    if (UtilsString.notNaked(ttsLocale)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onHandleIntent: EXTRA_TTS_LANGUAGE: " + ttsLocale);
                        }
                        actionBundle.putString(LocalRequest.EXTRA_TTS_LANGUAGE, ttsLocale);
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onHandleIntent: EXTRA_TTS_LANGUAGE naked");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onHandleIntent: no EXTRA_TTS_LANGUAGE");
                    }
                }

                if (bundle.containsKey(LocalRequest.EXTRA_SUPPORTED_LANGUAGE)) {

                    final SupportedLanguage supportedLanguage = (SupportedLanguage) bundle.getSerializable(
                            LocalRequest.EXTRA_SUPPORTED_LANGUAGE);

                    if (supportedLanguage != null) {
                        actionBundle.putSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE, supportedLanguage);
                        actionBundle.putString(LocalRequest.EXTRA_UTTERANCE,
                                PersonalityHelper.getIntro(getApplicationContext(), supportedLanguage));
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onHandleIntent: EXTRA_SUPPORTED_LANGUAGE null");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onHandleIntent: no EXTRA_SUPPORTED_LANGUAGE");
                    }
                }

                if (bundle.containsKey(RecognizerIntent.EXTRA_SECURE)) {

                    final Object secureObject = bundle.get(RecognizerIntent.EXTRA_SECURE);

                    if (secureObject != null) {

                        boolean secure = false;

                        if (secureObject instanceof Boolean) {
                            secure = (boolean) secureObject;
                        } else if (secureObject instanceof String) {
                            secure = Boolean.parseBoolean((String) secureObject);
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onHandleIntent: EXTRA_SECURE of unknown type ignoring");
                            }
                        }

                        if (secure) {
                            actionBundle.putInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_SECURE);
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onHandleIntent: EXTRA_SECURE false ignoring");
                            }
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onHandleIntent: secureObject null");
                        }
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onHandleIntent: no EXTRA_SECURE");
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onHandleIntent: bundle null ignoring");
                }
            }

            final String action = intent.getAction();
            if (UtilsString.notNaked(action)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onHandleIntent: action: " + action);
                }
                if (!actionBundle.containsKey(LocalRequest.EXTRA_RECOGNITION_LANGUAGE)) {
                    actionBundle.putString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE, SPH.getVRLocale(getApplicationContext()).toString());
                }
                final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(
                        UtilsLocale.stringToLocale(actionBundle.getString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE)));
                actionBundle.putSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE, sl);
                if (action.equals(Intent.ACTION_ASSIST) || action.equals(Intent.ACTION_MAIN) || action.equals(Intent.ACTION_VOICE_COMMAND)
                        || action.equals(RecognizerIntent.ACTION_WEB_SEARCH) || action.equals(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE)
                        || action.equals(ACTION_WIDGET_ASSIST) || action.equals(ACTION_QL_ASSIST)) {
                    if (DEBUG) {
                        if (intent.hasExtra(EXTRA_ASSIST_CONTEXT)) {
                            final Bundle assistBundle = intent.getBundleExtra(EXTRA_ASSIST_CONTEXT);

                            MyLog.i(CLS_NAME, "onHandleIntent checking assistBundle");
                            examineBundle(assistBundle);
                        }
                    }
                    actionBundle.putString(LocalRequest.EXTRA_UTTERANCE, PersonalityHelper.getIntro(getApplicationContext(), sl));
                } else if (action.equals(ACTION_ASSIST_SEARCH)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onHandleIntent: action: EXTRA_ASSIST_SEARCH");
                    }
                    if (intent.hasExtra(SearchManager.QUERY)) {
                        String stringExtra = intent.getStringExtra(SearchManager.QUERY);
                        if (!UtilsString.notNaked(stringExtra) || stringExtra.trim().matches("intro")) {
                            actionBundle.putString(LocalRequest.EXTRA_UTTERANCE, PersonalityHelper.getIntro(getApplicationContext(), sl));
                        } else {
                            ArrayList<String> arrayList = new ArrayList<>(1);
                            arrayList.add(stringExtra);
                            actionBundle.putBoolean(LocalRequest.EXTRA_RESOLVED, true);
                            actionBundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayList);
                            actionBundle.putFloatArray(SpeechRecognizer.CONFIDENCE_SCORES, new float[]{0.9f});
                        }
                    } else {
                        actionBundle.putString(LocalRequest.EXTRA_UTTERANCE, PersonalityHelper.getIntro(getApplicationContext(), sl));
                    }
                } else if (intent.getAction().equals(ACTION_ALEXA)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onHandleIntent: action: Intent.ACTION_ALEXA");
                    }
                    if (!ai.saiy.android.amazon.TokenHelper.hasToken(getApplicationContext())) {
                        actionBundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_ONLY);
                        actionBundle.putInt(ActivityHome.FRAGMENT_INDEX, ActivityHome.INDEX_FRAGMENT_SUPPORTED_APPS);
                        actionBundle.putString(LocalRequest.EXTRA_UTTERANCE, SaiyResourcesHelper.getStringResource(getApplicationContext(), sl, R.string.amazon_notification_auth_request));
                        ai.saiy.android.intent.ExecuteIntent.saiyActivity(getApplicationContext(), ActivityHome.class, bundle, true);
                    } else if (!ai.saiy.android.utils.conditions.Network.isNetworkAvailable(getApplicationContext())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onHandleIntent: ACTION_ALEXA: no network");
                        }
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                ai.saiy.android.utils.UtilsToast.showToast(getApplicationContext(), R.string.error_network, Toast.LENGTH_SHORT);
                            }
                        });
                        return;
                    } else {
                        actionBundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_LISTEN);
                        actionBundle.putString(LocalRequest.EXTRA_UTTERANCE, PersonalityHelper.getIntro(getApplicationContext(), sl));
                        actionBundle.putSerializable(LocalRequest.EXTRA_RECOGNITION_PROVIDER, SaiyDefaults.VR.ALEXA);
                    }
                } else if (intent.getAction().equals(ACTION_HOTWORD)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onHandleIntent: action: Intent.ACTION_HOTWORD");
                    }
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || ai.saiy.android.permissions.PermissionHelper.checkUsageStatsPermission(getApplicationContext()) || SPH.getHotwordStats(getApplicationContext())) {
                        actionBundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_TOGGLE_HOTWORD);
                    } else {
                        SPH.markHotwordStats(getApplicationContext());
                        actionBundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_ONLY);
                        if (ai.saiy.android.intent.ExecuteIntent.settingsIntent(getApplicationContext(), IntentConstants.SETTINGS_USAGE_STATS)) {
                            actionBundle.putString(LocalRequest.EXTRA_UTTERANCE, SaiyResourcesHelper.getStringResource(getApplicationContext(), sl, R.string.app_speech_usage_stats));
                        } else {
                            actionBundle.putString(LocalRequest.EXTRA_UTTERANCE, SaiyResourcesHelper.getStringResource(getApplicationContext(), sl, R.string.issue_usage_stats_bug));
                        }
                    }
                } else if (intent.getAction().equals(ACTION_DRIVING)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onHandleIntent: action: Intent.ACTION_DRIVING");
                    }
                    if (CommandDriving.haveDrivingProfilePermissions(getApplicationContext(), DrivingProfileHelper.getDrivingProfile(getApplicationContext()))) {
                        actionBundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_TOGGLE_DRIVING_PROFILE);
                    } else {
                        actionBundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_ONLY);
                        actionBundle.putString(LocalRequest.EXTRA_UTTERANCE, SaiyResourcesHelper.getStringResource(getApplicationContext(), sl, R.string.driving_profile_missing_permissions));
                        CommandDriving.openAdvancedSettings(getApplicationContext());
                    }
                } else if (intent.getAction().equals(ACTION_NOTIFICATIONS)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onHandleIntent: action: Intent.ACTION_NOTIFICATIONS");
                    }
                    actionBundle.putInt(LocalRequest.EXTRA_ACTION, LocalRequest.ACTION_SPEAK_ONLY);
                    if (SPH.getAnnounceNotifications(getApplicationContext())) {
                        SPH.setAnnounceNotifications(getApplicationContext(), false);
                        showToast(getString(R.string.disabled), Toast.LENGTH_SHORT);
                        return;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        boolean notificationEnabled = false;
                        for (String s : NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext())) {
                            if (s.equals(getApplicationContext().getPackageName())) {
                                notificationEnabled = true;
                                break;
                            }
                        }
                        if (notificationEnabled) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "notification listener service running");
                            }
                            SPH.setAnnounceNotifications(getApplicationContext(), true);
                            if (ai.saiy.android.quiet.QuietTimeHelper.canProceed(getApplicationContext())) {
                                showToast(getString(R.string.enabled), Toast.LENGTH_SHORT);
                                return;
                            }
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "notification listener service running: quiet times active");
                            }
                            showToast(getString(R.string.enabled) + " - " + getString(R.string.title_quiet_time_active), Toast.LENGTH_SHORT);
                            return;
                        }
                        if (SettingsIntent.settingsIntent(getApplicationContext(), SettingsIntent.Type.NOTIFICATION_ACCESS)) {
                            actionBundle.putString(LocalRequest.EXTRA_UTTERANCE, SaiyResourcesHelper.getStringResource(getApplicationContext(), sl, R.string.notifications_enable));
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "notification listener: settings location unknown");
                            }
                            actionBundle.putString(LocalRequest.EXTRA_UTTERANCE, String.format(SaiyResourcesHelper.getStringResource(getApplicationContext(), sl, R.string.settings_missing), ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(getApplicationContext(), sl, R.string.notification_access)));
                        }
                    } else if (SelfAwareHelper.saiyAccessibilityRunning(getApplicationContext())) {
                        SelfAwareHelper.startAccessibilityService(getApplicationContext());
                        showToast(getString(R.string.enabled), Toast.LENGTH_SHORT);
                        return;
                    } else {
                        ai.saiy.android.intent.ExecuteIntent.settingsIntent(getApplicationContext(), IntentConstants.SETTINGS_ACCESSIBILITY);
                        actionBundle.putString(LocalRequest.EXTRA_UTTERANCE, SaiyResourcesHelper.getStringResource(getApplicationContext(), sl, R.string.accessibility_enable));
                    }
                } else if (DEBUG) {
                    MyLog.i(CLS_NAME, "onHandleIntent: action naked");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onHandleIntent: intent null ignoring");
            }
        }

        if (!actionBundle.containsKey(LocalRequest.EXTRA_RECOGNITION_LANGUAGE)) {
            actionBundle.putString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE, SPH.getVRLocale(getApplicationContext()).toString());
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onHandleIntent: actionBundle: auto adding EXTRA_RECOGNITION_LANGUAGE");
            }
        }

        if (!actionBundle.containsKey(LocalRequest.EXTRA_TTS_LANGUAGE)) {
            actionBundle.putString(LocalRequest.EXTRA_TTS_LANGUAGE, SPH.getTTSLocale(getApplicationContext()).toString());
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onHandleIntent: actionBundle: auto adding EXTRA_TTS_LANGUAGE");
            }
        }

        if (!actionBundle.containsKey(LocalRequest.EXTRA_SUPPORTED_LANGUAGE)) {
            @SuppressWarnings("ConstantConditions") final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(
                    UtilsLocale.stringToLocale(actionBundle.getString(LocalRequest.EXTRA_RECOGNITION_LANGUAGE)));
            actionBundle.putSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE, sl);
            actionBundle.putString(LocalRequest.EXTRA_UTTERANCE, PersonalityHelper.getIntro(getApplicationContext(), sl));
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onHandleIntent: actionBundle: auto adding EXTRA_SUPPORTED_LANGUAGE");
            }
        }

        new LocalRequest(getApplicationContext(), actionBundle).execute();
    }

    /**
     * For debugging the intent extras
     *
     * @param intent containing potential extras
     */
    private void examineIntent(@Nullable final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "examineIntent");
        }

        if (intent != null) {
            final Bundle bundle = intent.getExtras();
            if (bundle != null) {
                final Set<String> keys = bundle.keySet();
                for (final String key : keys) {
                    if (DEBUG) {
                        MyLog.v(CLS_NAME, "examineIntent: " + key + " ~ " + bundle.get(key));
                    }
                }
            }
        }
    }

    /**
     * For debugging the intent extras
     *
     * @param bundle containing potential extras
     */
    private void examineBundle(@Nullable final Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "examineBundle");
        }

        if (bundle != null) {
            final Set<String> keys = bundle.keySet();
            for (final String key : keys) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "examineBundle: " + key + " ~ " + bundle.get(key));
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
            MyLog.getElapsed(CLS_NAME, then);
        }
    }
}
