package ai.saiy.android.firebase.helper;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.cognitive.identity.provider.microsoft.containers.ProfileItem;
import ai.saiy.android.user.SaiyAccount;
import ai.saiy.android.user.SaiyAccountHelper;
import ai.saiy.android.user.SaiyAccountList;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

public class UtilsAnalytic {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UtilsAnalytic.class.getSimpleName();
    private static final String USAGE_COUNT = "usage_count";
    private static final String HAS_USER_NAME = "has_user_name";
    private static final String HAS_CUSTOMISATION = "has_customisation";
    private static final String HAS_PHRASE = "has_phrase";
    private static final String HAS_NICKNAME = "has_nickname";
    private static final String HAS_REPLACEMENT = "has_replacement";
    private static final String HAS_INTRO = "has_intro";
    private static final String HAS_RUN_DIAGNOSTICS = "has_run_diagnostics";
    private static final String HAS_CUSTOM_VOICE = "has_custom_voice";
    private static final String HAS_UNKNOWN_COMMAND_SOLUTION = "has_unknown_command_solution";
    private static final String HAS_VOICE_ID = "has_voice_id";
    private static final String WITH_ERRORS = "with_errors";
    private static final String USER_LOCALE = "user_locale";

    public static void diagnosticsStarted(Context context, FirebaseAnalytics firebaseAnalytics) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "diagnosticsStarted");
        }
        if (firebaseAnalytics == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "diagnosticsStarted: mFirebaseAnalytics null");
            }
        } else if (SPH.getAnonymousUsageStats(context)) {
            firebaseAnalytics.logEvent("diagnostics_started", new Bundle());
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "diagnosticsStarted: getAnonymousUsageStats false");
        }
    }

    public static void onCommandComplete(Context context, FirebaseAnalytics firebaseAnalytics, Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCommandComplete");
        }
        if (firebaseAnalytics == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onCommandComplete: mFirebaseAnalytics null");
            }
        } else if (SPH.getAnonymousUsageStats(context)) {
            firebaseAnalytics.logEvent("command_complete", bundle);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "onCommandComplete: getAnonymousUsageStats false");
        }
    }

    public static void diagnosticsComplete(Context context, FirebaseAnalytics firebaseAnalytics, boolean condition) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "diagnosticsComplete");
        }
        if (firebaseAnalytics == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "diagnosticsComplete: mFirebaseAnalytics null");
            }
        } else if (SPH.getAnonymousUsageStats(context)) {
            final Bundle bundle = new Bundle(1);
            bundle.putBoolean(WITH_ERRORS, condition);
            firebaseAnalytics.logEvent("diagnostics_complete", bundle);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "diagnosticsComplete: getAnonymousUsageStats false");
        }
    }

    public static boolean HasVoiceId(Context context) {
        final SaiyAccountList saiyAccountList = SaiyAccountHelper.getAccounts(context);
        if (saiyAccountList == null || saiyAccountList.size() <= 0) {
            return false;
        }
        final SaiyAccount saiyAccount = saiyAccountList.getSaiyAccountList().get(0);
        if (saiyAccount == null) {
            return false;
        }
        final ProfileItem profileItem = saiyAccount.getProfileItem();
        if (profileItem == null) {
            return false;
        }
        return UtilsString.notNaked(profileItem.getId());
    }

    public static void alexaAuthorised(Context context, FirebaseAnalytics firebaseAnalytics) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "alexaAuthorised");
        }
        if (firebaseAnalytics == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "alexaAuthorised: mFirebaseAnalytics null");
            }
            return;
        }
        if (!SPH.getAnonymousUsageStats(context)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "alexaAuthorised: getAnonymousUsageStats false");
            }
            return;
        }
        final Bundle bundle = new Bundle(11);
        bundle.putLong(USAGE_COUNT, SPH.getUsedIncrement(context));
        bundle.putBoolean(HAS_USER_NAME, SPH.getUserName(context) != null && !SPH.getUserName(context).matches(context.getString(R.string.master)));
        bundle.putBoolean(HAS_CUSTOMISATION, SPH.hasCustomisation(context));
        bundle.putBoolean(HAS_PHRASE, SPH.hasPhrase(context));
        bundle.putBoolean(HAS_NICKNAME, SPH.hasNickname(context));
        bundle.putBoolean(HAS_REPLACEMENT, SPH.hasReplacement(context));
        bundle.putBoolean(HAS_INTRO, SPH.getCustomIntro(context) != null);
        bundle.putBoolean(HAS_RUN_DIAGNOSTICS, SPH.getRunDiagnostics(context));
        bundle.putBoolean(HAS_CUSTOM_VOICE, SPH.getDefaultTTSVoice(context) != null);
        bundle.putBoolean(HAS_UNKNOWN_COMMAND_SOLUTION, SPH.getCommandUnknownAction(context) != 0);
        bundle.putBoolean(HAS_VOICE_ID, HasVoiceId(context));
        firebaseAnalytics.logEvent("alexa_authorise", bundle);
    }

    public static void tutorialComplete(Context context, FirebaseAnalytics firebaseAnalytics, boolean condition) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "tutorialComplete");
        }
        if (firebaseAnalytics == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "tutorialComplete: mFirebaseAnalytics null");
            }
        } else if (SPH.getAnonymousUsageStats(context)) {
            final Bundle bundle = new Bundle(1);
            bundle.putBoolean(WITH_ERRORS, condition);
            firebaseAnalytics.logEvent("tutorial_complete", bundle);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "tutorialComplete: getAnonymousUsageStats false");
        }
    }

    public static void alexaDeauthorised(Context context, FirebaseAnalytics firebaseAnalytics) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "alexaDeauthorised");
        }
        if (firebaseAnalytics == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "alexaDeauthorised: mFirebaseAnalytics null");
            }
            return;
        }
        if (!SPH.getAnonymousUsageStats(context)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "alexaDeauthorised: getAnonymousUsageStats false");
            }
            return;
        }
        final Bundle bundle = new Bundle(11);
        bundle.putLong(USAGE_COUNT, SPH.getUsedIncrement(context));
        bundle.putBoolean(HAS_USER_NAME, SPH.getUserName(context) != null && !SPH.getUserName(context).matches(context.getString(R.string.master)));
        bundle.putBoolean(HAS_CUSTOMISATION, SPH.hasCustomisation(context));
        bundle.putBoolean(HAS_PHRASE, SPH.hasPhrase(context));
        bundle.putBoolean(HAS_NICKNAME, SPH.hasNickname(context));
        bundle.putBoolean(HAS_REPLACEMENT, SPH.hasReplacement(context));
        bundle.putBoolean(HAS_INTRO, SPH.getCustomIntro(context) != null);
        bundle.putBoolean(HAS_RUN_DIAGNOSTICS, SPH.getRunDiagnostics(context));
        bundle.putBoolean(HAS_CUSTOM_VOICE, SPH.getDefaultTTSVoice(context) != null);
        bundle.putBoolean(HAS_UNKNOWN_COMMAND_SOLUTION, SPH.getCommandUnknownAction(context) != 0);
        bundle.putBoolean(HAS_VOICE_ID, HasVoiceId(context));
        firebaseAnalytics.logEvent("alexa_deauthorise", bundle);
    }

    public static void alexaAuthSuccess(Context context, FirebaseAnalytics firebaseAnalytics) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "alexaAuthSuccess");
        }
        if (firebaseAnalytics == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "alexaAuthSuccess: mFirebaseAnalytics null");
            }
        } else if (!SPH.getAnonymousUsageStats(context)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "alexaAuthSuccess: getAnonymousUsageStats false");
            }
        } else {
            final Bundle bundle = new Bundle(2);
            bundle.putLong(USAGE_COUNT, SPH.getUsedIncrement(context));
            bundle.putString(USER_LOCALE, Locale.getDefault().toString());
            firebaseAnalytics.logEvent("alexa_auth_success", bundle);
        }
    }

    public static void alexaAuthError(Context context, FirebaseAnalytics firebaseAnalytics) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "alexaAuthError");
        }
        if (firebaseAnalytics == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "alexaAuthError: mFirebaseAnalytics null");
            }
        } else if (!SPH.getAnonymousUsageStats(context)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "alexaAuthError: getAnonymousUsageStats false");
            }
        } else {
            final Bundle bundle = new Bundle(2);
            bundle.putLong(USAGE_COUNT, SPH.getUsedIncrement(context));
            bundle.putString(USER_LOCALE, Locale.getDefault().toString());
            firebaseAnalytics.logEvent("alexa_auth_error", bundle);
        }
    }

    public static void tutorialStarted(Context context, FirebaseAnalytics firebaseAnalytics) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "tutorialStarted");
        }
        if (firebaseAnalytics == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "tutorialStarted: mFirebaseAnalytics null");
            }
            return;
        }
        if (!SPH.getAnonymousUsageStats(context)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "tutorialStarted: getAnonymousUsageStats false");
            }
            return;
        }
        final Bundle bundle = new Bundle(11);
        bundle.putLong(USAGE_COUNT, SPH.getUsedIncrement(context));
        bundle.putBoolean(HAS_USER_NAME, SPH.getUserName(context) != null && !SPH.getUserName(context).matches(context.getString(R.string.master)));
        bundle.putBoolean(HAS_CUSTOMISATION, SPH.hasCustomisation(context));
        bundle.putBoolean(HAS_PHRASE, SPH.hasPhrase(context));
        bundle.putBoolean(HAS_NICKNAME, SPH.hasNickname(context));
        bundle.putBoolean(HAS_REPLACEMENT, SPH.hasReplacement(context));
        bundle.putBoolean(HAS_INTRO, SPH.getCustomIntro(context) != null);
        bundle.putBoolean(HAS_RUN_DIAGNOSTICS, SPH.getRunDiagnostics(context));
        bundle.putBoolean(HAS_CUSTOM_VOICE, SPH.getDefaultTTSVoice(context) != null);
        bundle.putBoolean(HAS_UNKNOWN_COMMAND_SOLUTION, SPH.getCommandUnknownAction(context) != 0);
        bundle.putBoolean(HAS_VOICE_ID, HasVoiceId(context));
        firebaseAnalytics.logEvent("tutorial_begin", bundle);
    }
}
