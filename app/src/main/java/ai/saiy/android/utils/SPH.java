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

package ai.saiy.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.algorithms.Algorithm;
import ai.saiy.android.algorithms.distance.jarowinkler.JaroWinklerHelper;
import ai.saiy.android.algorithms.distance.levenshtein.LevenshteinHelper;
import ai.saiy.android.algorithms.fuzzy.FuzzyHelper;
import ai.saiy.android.algorithms.mongeelkan.MongeElkanHelper;
import ai.saiy.android.algorithms.needlemanwunch.NeedlemanWunschHelper;
import ai.saiy.android.algorithms.soundex.SoundexHelper;
import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.cognitive.emotion.provider.beyondverbal.containers.BVCredentials;
import ai.saiy.android.cognitive.motion.provider.google.Motion;
import ai.saiy.android.command.battery.BatteryInformation;
import ai.saiy.android.command.horoscope.CommandHoroscopeValues;
import ai.saiy.android.command.translate.provider.TranslationProvider;
import ai.saiy.android.command.unknown.Unknown;
import ai.saiy.android.defaults.notes.NoteProvider;
import ai.saiy.android.defaults.songrecognition.SongRecognitionProvider;
import ai.saiy.android.firebase.database.read.WeatherProvider;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.memory.Memory;
import ai.saiy.android.recognition.provider.android.RecognitionNative;
import ai.saiy.android.service.SelfAware;
import ai.saiy.android.service.helper.SelfAwareConditions;
import ai.saiy.android.tts.attributes.Gender;
import ai.saiy.android.utils.Conditions.Network;

/**
 * Created by benrandall76@gmail.com on 07/02/2016.
 * <p/>
 * A helper class (Shared Preference Helper (SPH)) to access the user's preferences and retain
 * certain interaction information.
 * <p/>
 * The static nature of these global user variables does not appear to be a performance issue
 * and provides ease of access.
 */
public class SPH {

    private static final String SAIY_PREF = "saiyPref";

    private static final int ZERO = 0;
    private static final int ONE = 1;
    public static final int TRESS = 3;

    private static final String DEFAULT_RECOGNITION = "default_recognition";
    private static final String DEFAULT_LANGUAGE_MODEL = "default_language_model";
    private static final String DEFAULT_TTS = "default_tts";
    private static final String DEFAULT_TTS_GENDER = "default_tts_gender";
    private static final String DEFAULT_TEMPERATURE_UNITS = "default_temperature_units";
    private static final String DEFAULT_RINGER = "default_ringer";
    private static final String MESSAGE_SIGNATURE = "message_signature";
    private static final String MESSAGE_SIGNATURE_CONDITION = "message_signature_condition";
    private static final String INACTIVITY_TIMEOUT = "inactivity_timeout";
    private static final String INACTIVITY_TOAST = "inactivity_toast";
    private static final String VR_LOCALE = "vr_locale";
    private static final String TTS_LOCALE = "tts_locale";
    private static final String JWD_UPPER_THRESHOLD_CONTACT = "jwd_upper_threshold_contact";
    private static final String TTS_VOLUME = "tts_volume";
    private static final String ASSUME_GLOBAL_VOLUME = "assume_global_volume";
    private static final String TOAST_VOLUME_WARNINGS = "toast_volume_warnings";
    private static final String SYSTEM_MANAGED_VOLUME = "system_managed_volume";
    private static final String CUSTOM_INTRO = "custom_intro";
    private static final String CUSTOM_INTRO_RANDOM = "custom_intro_random";
    private static final String DEFAULT_TTS_VOICE = "default_tts_voice";
    private static final String SPELL_COMMAND_VERBOSE = "spell_command_verbose";
    private static final String EMOTION_COMMAND_VERBOSE = "emotion_command_verbose";
    private static final String TRANSLATE_COMMAND_VERBOSE = "translate_command_verbose";
    private static final String EMOTION_PERMISSION = "emotion_permission";
    private static final String VIBRATE = "vibrate";
    private static final String NETWORK_SYNTHESIS = "network_synthesis";
    private static final String NETWORK_SYNTHESIS_WIFI = "network_synthesis_wifi";
    private static final String NETWORK_SYNTHESIS_4G = "network_synthesis_4g";
    private static final String FIREBASE_ANONYMOUS_UID = "firebase_anonymous_uid";
    private static final String FIREBASE_AUTH_UID = "firebase_auth_uid";
    private static final String FIREBASE_MIGRATED_UID = "firebase_migrated_uid";
    private static final String BLOCKED_NOTIFICATION_APPLICATIONS = "blocked_notification_applications";
    private static final String RESET_SPEAKER = "reset_speaker";
    private static final String DESIGN_OVERVIEW = "design_overview";
    private static final String FRAGMENT_INCREMENT = "fragment_increment";
    private static final String SHOWN_UNKNOWN = "shown_unknown";
    private static final String USE_OFFLINE = "use_offline";
    private static final String PING_CHECK = "ping_check";
    private static final String NOTE_PROVIDER_VERBOSE = "note_provider_verbose";
    private static final String COMMAND_UNKNOWN_ACTION = "command_unknown_action";
    private static final String DRIVING_COOLDOWN_TIME = "driving_cooldown_time";
    private static final String PING_TIMEOUT = "ping_timeout";
    private static final String CONNECTION_MINIMUM = "connection_minimum";
    private static final String USER_NAME = "user_name";
    private static final String USER_GENDER = "user_gender";
    private static final String USER_ACCOUNT = "user_account";
    private static final String TORCH_FIX = "torch_fix";
    private static final String UNKNOWN_CONTACT_VERBOSE = "unknown_contact_verbose";
    private static final String EMAIL_AUTO_VERBOSE = "email_auto_verbose";
    private static final String LOCATION_PROVIDER = "location_provider";
    private static final String VEHICLE_LOCATION_LAT = "vehicle_location_lat";
    private static final String VEHICLE_LOCATION_LONG = "vehicle_location_long";
    private static final String STAR_SIGN = "star_sign";
    private static final String USER_ATTENDEE_NAME = "user_attendee_name";
    private static final String LAST_CONTACT_UPDATE = "last_contact_update";
    private static final String DOB_YEAR = "dob_year";
    private static final String DOB_MONTH = "dob_month";
    private static final String DOB_DAY = "dob_day";
    private static final String SHOWN_OFFLINE_INSTALLATION = "shown_offline_installation";
    private static final String HOTWORD = "hotword";
    private static final String HOTWORD_BOOT = "hotword_boot";
    private static final String HOTWORD_DRIVING = "hotword_driving";
    private static final String HOTWORD_START_DRIVING = "hotword_start_driving";
    private static final String HOTWORD_STOP_DRIVING = "hotword_stop_driving";
    private static final String HOTWORD_WAKELOCK = "hotword_wakelock";
    private static final String HOTWORD_OKAY_GOOGLE = "hotword_okay_google";
    private static final String HOTWORD_SECURE = "hotword_secure";
    private static final String HOTWORD_USAGE_STATS = "hotword_usage_stats";
    private static final String BOOT_START = "boot_start";
    private static final String SELF_AWARE_ENABLED = "self_aware_enabled";
    private static final String CALL_CONFIRMATION = "call_confirmation";
    private static final String WEATHER_PROVIDER = "weather_provider";
    private static final String HAS_PHRASE = "has_phrase";
    private static final String HAS_NICKNAME = "has_nickname";
    private static final String HAS_REPLACEMENT = "has_replacement";
    private static final String HAS_CUSTOM = "has_custom";
    private static final String HAS_TASKER_VARIABLES = "has_tasker_variables";
    private static final String ENROLLMENT_VERBOSE = "enrollment_verbose";
    private static final String DISCLAIMER = "disclaimer";
    private static final String WHATS_NEW = "whats_new";
    private static final String DEVELOPER_NOTE = "developer_note";
    private static final String PAUSE_TIMEOUT = "pause_timeout";
    private static final String BING_TOKEN = "bing_token";
    private static final String BEYOND_VERBAL_AUTH_RESPONSE = "beyond_verbal_auth_response";
    private static final String BING_OAUTH_UPDATE = "bing_oauth_update";
    private static final String TRANSLATION_PROVIDER = "translation_provider";
    private static final String SAIY_ACCOUNTS = "saiy_accounts";
    private static final String RATE_ME = "rate_me";
    private static final String MEMORY = "memory";
    private static final String EMOTION = "emotion";
    private static final String LAST_DRIVING_TIME = "last_driving_time";
    private static final String MOTION = "motion";
    private static final String MOTION_ENABLED = "motion_enabled";
    private static final String TOAST_UNKNOWN = "toast_unknown";
    private static final String BLACKLIST = "blacklist";
    private static final String ALGORITHM = "algorithm";
    private static final String ALGORITHMS = "algorithms";
    private static final String JWD_LOWER_THRESHOLD = "jwd_lower_threshold";
    private static final String JWD_UPPER_THRESHOLD = "jwd_upper_threshold";
    private static final String LEV_UPPER_THRESHOLD = "lev_upper_threshold";
    private static final String ME_UPPER_THRESHOLD = "me_upper_threshold";
    private static final String FUZZY_MULTIPLIER = "fuzzy_multiplier";
    private static final String NW_UPPER_THRESHOLD = "nw_upper_threshold";
    private static final String SOUNDEX_UPPER_THRESHOLD = "soundex_upper_threshold";
    private static final String REMOTE_COMMAND_VERBOSE = "remote_command_verbose";
    private static final String LAST_USED = "last_used";
    private static final String PLAYLIST_VERBOSE ="playlist_verbose";
    private static final String RADIO_VERBOSE ="radio_verbose";
    private static final String FACEBOOK_COMMAND_VERBOSE = "facebook_command_verbose";
    private static final String TWITTER_TOKEN = "twitter_token";
    private static final String TWITTER_SECRET = "twitter_secret";
    private static final String USED_INCREMENT = "used_increment";
    private static final String FOURSQUARE_TOKEN = "foursquare_token";
    private static final String DEFAULT_SONG_RECOGNITION = "default_song_recognition";
    private static final String ANNOUNCE_TASKER = "announce_tasker";
    private static final String ANNOUNCE_NOTIFICATIONS = "announce_notifications";
    private static final String ANNOUNCE_NOTIFICATIONS_SECURE = "announce_notifications_secure";
    private static final String ANNOUNCE_NOTIFICATIONS_SMS = "announce_notifications_sms";
    private static final String ANNOUNCE_NOTIFICATIONS_HANGOUTS = "announce_notifications_hangouts";
    private static final String ANNOUNCE_NOTIFICATIONS_WHATSAPP = "announce_notifications_whatsapp";
    private static final String AUTO_CONNECT_HEADSET = "aut_connect_headset";
    private static final String HEADSET_SYSTEM = "headset_system";
    private static final String HEADSET_STREAM_TYPE = "headset_stream_type";
    private static final String HEADSET_CONNECTION_TYPE = "headset_connection_type";
    private static final String ACCOUNT_OVERVIEW = "account_overview";
    private static final String AD_OVERVIEW = "ad_overview";
    private static final String CREDITS_VERBOSE = "credits_verbose";
    private static final String PREMIUM_CONTENT_VERBOSE = "premium_content_verbose";
    private static final String HOROSCOPE_INCREMENT = "horoscope_increment";
    private static final String CACHE_SPEECH = "cache_speech";
    private static final String TTS_PITCH = "tts_pitch";
    private static final String TTS_RATE = "tts_rate";
    private static final String SMS_ID_FIX = "sms_id_fix";
    private static final String SMS_BODY_FIX = "sms_body_fix";
    private static final String RECOGNISER_BUSY_FIX = "recogniser_busy_fix";
    private static final String RECOGNIZER_BUSY_INCREMENT = "recognizer_busy_increment";
    private static final String ANONYMOUS_USAGE_STATS = "anonymous_usage_stats";
    private static final String IGNORE_RESTRICTED_CONTENT = "ignore_restricted_content";
    private static final String OKAY_GOOGLE_FIX = "okay_google_fix";
    private static final String DOUBLE_BEEP_FIX = "double_beep_fix";
    private static final String NEW_USER = "new_user";
    private static final String IMPORT_WARNING = "import_warning";
    private static final String EXPORT_WARNING = "export_warning";
    private static final String HEADSET_OVERVIEW_COUNT = "headset_overview_count";
    private static final String ACCESSIBILITY_CHANGE = "accessibility_change";
    private static final String DEBUG_BILLING = "debug_billing";
    private static final String DEFAULT_NOTE = "default_note";
    private static final String ALEXA_CODE_VERIFIER = "alexa_code_verifier";
    private static final String ALEXA_ACCESS_TOKEN = "alexa_access_token";
    private static final String ALEXA_REFRESH_TOKEN = "alexa_refresh_token";
    private static final String ALEXA_ACCESS_TOKEN_EXPIRY = "alexa_access_token_expiry";
    private static final String ALEXA_REGION = "alexa_region";
    private static final String UNIQUE_ID = "unique_id";
    private static final String ALEXA_NOTIFICATION_BUTTON = "alexa_notification_button";
    private static final String SHOWN_VOLUME_BUG = "shown_volume_bug";
    private static final String SHOWN_PAUSE_BUG = "shown_pause_bug";
    private static final String REINSTALLATION_PROCESS = "reinstallation_process";
    private static final String UNKNOWN_SOURCES = "unknown_sources";
    private static final String ANNOUNCE_CALLER = "announce_caller";
    private static final String DRIVING_PROFILE = "driving_profile";
    private static final String QUIET_TIMES = "quiet_times";
    private static final String OVERRIDE_SECURE = "override_secure";
    private static final String OVERRIDE_SECURE_HEADSET = "override_secure_headset";
    private static final String OVERRIDE_SECURE_DRIVING = "override_secure_driving";
    private static final String RUN_DIAGNOSTICS = "run_diagnostics";
    private static final String MIC_FIRST = "mic_first";

    /**
     * Prevent instantiation
     */
    public SPH() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    /**
     * For convenience
     *
     * @param ctx the application context
     * @return the {@link SharedPreferences} object
     */
    private static SharedPreferences getPref(@NonNull final Context ctx) {
        return ctx.getSharedPreferences(SAIY_PREF, Context.MODE_PRIVATE);
    }

    /**
     * For convenience
     *
     * @param pref {@link SharedPreferences} object
     * @return the {@link android.content.SharedPreferences.Editor} object
     */
    private static SharedPreferences.Editor getEditor(final SharedPreferences pref) {
        return pref.edit();
    }

    ////////////////////////////////////////////////////////////////////////////////
    //                           START OF METHODS                                 //
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Get the default recognition provider
     *
     * @param ctx the application context
     * @return the default recognition provider
     */
    public static SaiyDefaults.VR getDefaultRecognition(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return SaiyDefaults.getProviderVR(pref.getString(DEFAULT_RECOGNITION, SaiyDefaults.VR.NATIVE.name()));
    }

    /**
     * Set the default recognition provider
     *
     * @param ctx      the application context
     * @param provider of the recognition
     */
    public static void setDefaultRecognition(@NonNull final Context ctx, final SaiyDefaults.VR provider) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(DEFAULT_RECOGNITION, provider.name());
        edit.apply();
    }

    /**
     * Get the default Language Model
     *
     * @param ctx the application context
     * @return the default language model
     */
    public static SaiyDefaults.LanguageModel getDefaultLanguageModel(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return SaiyDefaults.getLanguageModel(pref.getString(DEFAULT_LANGUAGE_MODEL,
                SaiyDefaults.LanguageModel.LOCAL.name()));
    }

    /**
     * Set the default Language Model
     *
     * @param ctx   the application context
     * @param model to apply
     */
    public static void setDefaultLanguageModel(@NonNull final Context ctx, final SaiyDefaults.LanguageModel model) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(DEFAULT_LANGUAGE_MODEL, model.name());
        edit.apply();
    }

    /**
     * Get the amount of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     * @return the integer number of times
     */
    public static int getEmotionCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(EMOTION_COMMAND_VERBOSE, ZERO);
    }

    /**
     * Increment the number of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     */
    public static void incrementEmotionCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(EMOTION_COMMAND_VERBOSE, (getEmotionCommandVerbose(ctx) + 1));
        edit.apply();
    }

    /**
     * Set whether the Saiy should announce the name of the executed Tasker Task
     *
     * @param ctx       the application context
     * @param condition to apply
     */
    public static void setAnnounceTasker(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(ANNOUNCE_TASKER, condition);
        edit.apply();
    }

    /**
     * Get whether the Saiy should announce the name of the executed Tasker Task
     *
     * @param ctx the application context
     * @return true as the default, or false if the user has disabled this
     */
    public static boolean getAnnounceTasker(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(ANNOUNCE_TASKER, true);
    }

    /**
     * Get the user preferred Text to Speech volume level
     *
     * @param ctx the application context
     * @return the user preferred volume level
     */
    public static int getTTSVolume(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(TTS_VOLUME, ZERO);
    }

    /**
     * Set the user preferred Text to Speech volume level
     *
     * @param ctx   the application context
     * @param level of the volume
     */
    public static void setTTSVolume(@NonNull final Context ctx, final int level) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(TTS_VOLUME, level);
        edit.apply();
    }

    public static boolean getJwdUpperThresholdForContact(Context context) {
        return getPref(context).getBoolean(JWD_UPPER_THRESHOLD_CONTACT, true);
    }

    public static void setJwdUpperThresholdForContact(Context context, boolean useDefault) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(JWD_UPPER_THRESHOLD_CONTACT, useDefault);
        edit.apply();
    }

    public static boolean getSystemManagedVolume(Context context) {
        return getPref(context).getBoolean(SYSTEM_MANAGED_VOLUME, true);
    }

    public static void setSystemManagedVolume(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(SYSTEM_MANAGED_VOLUME, condition);
        edit.apply();
    }

    public static boolean getAssumeGlobalVolume(Context context) {
        return getPref(context).getBoolean(ASSUME_GLOBAL_VOLUME, true);
    }

    public static void setAssumeGlobalVolume(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(ASSUME_GLOBAL_VOLUME, condition);
        edit.apply();
    }

    public static boolean getToastVolumeWarnings(Context context) {
        return getPref(context).getBoolean(TOAST_VOLUME_WARNINGS, true);
    }

    public static void setToastVolumeWarnings(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(TOAST_VOLUME_WARNINGS, condition);
        edit.apply();
    }

    /**
     * Get the default ringer configuration
     *
     * @param ctx the application context
     * @return the default recognition provider
     */
    public static int getDefaultRinger(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(DEFAULT_RINGER, 99);
    }

    /**
     * Set the default ringer configuration
     *
     * @param ctx           the application context
     * @param ringerDefault of the recognition
     */
    public static void setDefaultRinger(@NonNull final Context ctx, final int ringerDefault) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(DEFAULT_RINGER, ringerDefault);
        edit.apply();
    }

    public static String getMessageSignature(Context context) {
        return getPref(context).getString(MESSAGE_SIGNATURE, context.getString(R.string.saiy_signature));
    }

    public static void setMessageSignature(Context context, String str) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(MESSAGE_SIGNATURE, str);
        edit.apply();
    }

    public static boolean getMessageSignatureCondition(Context context) {
        return getPref(context).getBoolean(MESSAGE_SIGNATURE_CONDITION, true);
    }

    public static void setMessageSignatureCondition(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(MESSAGE_SIGNATURE_CONDITION, condition);
        edit.commit();
    }

    /**
     * Get the user preferred temperature units
     *
     * @param ctx the application context
     * @return the default units one of {@link BatteryInformation#CELSIUS}
     * or {@link BatteryInformation#FAHRENHEIT}
     */
    public static int getDefaultTemperatureUnits(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(DEFAULT_TEMPERATURE_UNITS, UtilsLocale.getDefaultLocale() == Locale.US
                ? BatteryInformation.FAHRENHEIT : BatteryInformation.CELSIUS);
    }

    /**
     * Set the user preferred temperature units
     *
     * @param ctx   the application context
     * @param units one of {@link BatteryInformation#CELSIUS}
     *              or {@link BatteryInformation#FAHRENHEIT}
     */
    public static void setDefaultTemperatureUnits(@NonNull final Context ctx, final int units) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(DEFAULT_TEMPERATURE_UNITS, units);
        edit.apply();
    }

    /**
     * Get the user preferred song recognition application
     *
     * @param ctx the application context
     * @return one of {@link SongRecognitionProvider}
     */
    public static SongRecognitionProvider getDefaultSongRecognition(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return SongRecognitionProvider.getProvider(pref.getInt(DEFAULT_SONG_RECOGNITION,
                SongRecognitionProvider.UNKNOWN.ordinal()));
    }

    /**
     * Set the user preferred song recognition application
     *
     * @param ctx      the application context
     * @param provider {@link SongRecognitionProvider}
     */
    public static void setDefaultSongRecognition(@NonNull final Context ctx,
                                                 @NonNull final SongRecognitionProvider provider) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(DEFAULT_SONG_RECOGNITION, provider.ordinal());
        edit.apply();
    }

    /**
     * Get the last time the application was used
     *
     * @param ctx the application context
     * @return the last time the application was used
     */
    public static long getLastUsed(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getLong(LAST_USED, ONE);
    }

    /**
     * Set the last time the application was used to now.
     *
     * @param ctx the application context
     */
    public static void setLastUsed(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putLong(LAST_USED, System.currentTimeMillis());
        edit.apply();

        SPH.incrementUsed(ctx);
    }

    public static int getPlaylistVerbose(Context context) {
        return getPref(context).getInt(PLAYLIST_VERBOSE, 0);
    }

    public static void setPlaylistVerbose(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putInt(PLAYLIST_VERBOSE, getPlaylistVerbose(context) + 1);
        edit.apply();
    }

    public static int getRadioVerbose(Context context) {
        return getPref(context).getInt(RADIO_VERBOSE, 0);
    }

    public static void setRadioVerbose(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putInt(RADIO_VERBOSE, getRadioVerbose(context) + 1);
        edit.apply();
    }

    public static int getFacebookCommandVerbose(Context context) {
        return getPref(context).getInt(FACEBOOK_COMMAND_VERBOSE, 0);
    }

    public static void setFacebookCommandVerbose(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putInt(FACEBOOK_COMMAND_VERBOSE, getFacebookCommandVerbose(context) + 1);
        edit.apply();
    }

    public static String getTwitterToken(Context context) {
        return getPref(context).getString(TWITTER_TOKEN, null);
    }

    public static void setTwitterToken(Context context, String str) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(TWITTER_TOKEN, str);
        edit.apply();
    }

    public static String getTwitterSecret(Context context) {
        return getPref(context).getString(TWITTER_SECRET, null);
    }

    public static void setTwitterSecret(Context context, String str) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(TWITTER_SECRET, str);
        edit.apply();
    }

    /**
     * Get the number of speech requests that have been made, since the application data was last
     * wiped. This will be used to run various housekeeping tasks.
     *
     * @param ctx the application context
     * @return the count of application uses
     */
    public static long getUsedIncrement(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getLong(USED_INCREMENT, ONE);
    }

    /**
     * Increment the number of speech requests made
     *
     * @param ctx the application context
     */
    public static void incrementUsed(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putLong(USED_INCREMENT, (getUsedIncrement(ctx) + 1));
        edit.apply();
    }

    public static String getFoursquareToken(Context context) {
        return getPref(context).getString(FOURSQUARE_TOKEN, null);
    }

    public static void setFoursquareToken(Context context, String token) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(FOURSQUARE_TOKEN, token);
        edit.apply();
    }

    /**
     * Get the amount of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     * @return the integer number of times
     */
    public static int getRemoteCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(REMOTE_COMMAND_VERBOSE, ZERO);
    }

    /**
     * Increment the number of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     */
    public static void incrementRemoteCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(REMOTE_COMMAND_VERBOSE, (getRemoteCommandVerbose(ctx) + 1));
        edit.apply();
    }

    /**
     * Get the serialised user defined {@link Algorithm}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static String getAlgorithms(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(ALGORITHMS, null);
    }

    /**
     * Set the user defined algorithms
     *
     * @param ctx        the application context
     * @param serialised list of {@link Algorithm}
     */
    public static void setAlgorithms(@NonNull final Context ctx, @NonNull final String serialised) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(ALGORITHMS, serialised);
        edit.apply();
    }

    /**
     * Get the upper distance limit to use in {@link SoundexHelper}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static double getSoundexUpper(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Double.parseDouble(pref.getString(SOUNDEX_UPPER_THRESHOLD,
                String.valueOf(Algorithm.SOUNDEX_UPPER_THRESHOLD)));
    }

    /**
     * Set the upper distance limit to use in {@link SoundexHelper}
     *
     * @param ctx   the application context
     * @param limit to set
     */
    public static void setSoundexUpper(@NonNull final Context ctx, final double limit) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(SOUNDEX_UPPER_THRESHOLD, String.valueOf(limit));
        edit.apply();
    }

    /**
     * Get the upper distance limit to use in {@link NeedlemanWunschHelper}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static double getNeedlemanWunschUpper(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Double.parseDouble(pref.getString(NW_UPPER_THRESHOLD, String.valueOf(Algorithm.NW_UPPER_THRESHOLD)));
    }

    /**
     * Set the upper distance limit to use in {@link NeedlemanWunschHelper}
     *
     * @param ctx   the application context
     * @param limit to set
     */
    public static void setNeedlemanWunschUpper(@NonNull final Context ctx, final double limit) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(NW_UPPER_THRESHOLD, String.valueOf(limit));
        edit.apply();
    }

    /**
     * Get the fuzzy multiplier to use in {@link FuzzyHelper}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static double getFuzzyMultiplier(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Double.parseDouble(pref.getString(FUZZY_MULTIPLIER,
                String.valueOf(Algorithm.FUZZY_MULTIPLIER)));
    }

    /**
     * Set the fuzzy multiplier to use in {@link FuzzyHelper}
     *
     * @param ctx   the application context
     * @param limit to set
     */
    public static void setFuzzyMultiplier(@NonNull final Context ctx, final double limit) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(FUZZY_MULTIPLIER, String.valueOf(limit));
        edit.apply();
    }

    /**
     * Get the upper distance limit to use in {@link MongeElkanHelper}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static double getMongeElkanUpper(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Double.parseDouble(pref.getString(ME_UPPER_THRESHOLD,
                String.valueOf(Algorithm.ME_UPPER_THRESHOLD)));
    }

    /**
     * Set the upper distance limit to use in {@link MongeElkanHelper}
     *
     * @param ctx   the application context
     * @param limit to set
     */
    public static void setMongeElkanUpper(@NonNull final Context ctx, final double limit) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(ME_UPPER_THRESHOLD, String.valueOf(limit));
        edit.apply();
    }


    /**
     * Get the upper distance limit to use in {@link LevenshteinHelper}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static double getLevenshteinUpper(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Double.parseDouble(pref.getString(LEV_UPPER_THRESHOLD,
                String.valueOf(Algorithm.LEV_UPPER_THRESHOLD)));
    }

    /**
     * Set the upper distance limit to use in {@link LevenshteinHelper}
     *
     * @param ctx   the application context
     * @param limit to set
     */
    public static void setLevenshteinUpper(@NonNull final Context ctx, final double limit) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(LEV_UPPER_THRESHOLD, String.valueOf(limit));
        edit.apply();
    }

    /**
     * Get the upper distance limit to use in {@link JaroWinklerHelper}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static double getJaroWinklerUpper(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Double.parseDouble(pref.getString(JWD_UPPER_THRESHOLD,
                String.valueOf(Algorithm.JWD_UPPER_THRESHOLD)));
    }

    /**
     * Get the lower distance limit to use in {@link JaroWinklerHelper}
     *
     * @param ctx the application context
     * @return the double or default value
     */
    public static double getJaroWinklerLower(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Double.parseDouble(pref.getString(JWD_LOWER_THRESHOLD,
                String.valueOf(Algorithm.JWD_LOWER_THRESHOLD)));
    }


    /**
     * Set the upper distance limit to use in {@link JaroWinklerHelper}
     *
     * @param ctx   the application context
     * @param limit to set
     */
    public static void setJaroWinklerUpper(@NonNull final Context ctx, final double limit) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(JWD_UPPER_THRESHOLD, String.valueOf(limit));
        edit.apply();
    }

    /**
     * Set the lower distance limit to use in {@link JaroWinklerHelper}
     *
     * @param ctx   the application context
     * @param limit to set
     */
    public static void setJaroWinklerLower(@NonNull final Context ctx, final double limit) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(JWD_LOWER_THRESHOLD, String.valueOf(limit));
        edit.apply();
    }

    /**
     * Get the default algorithm provider
     *
     * @param ctx the application context
     * @return the default algorithm provider
     */
    public static Algorithm getAlgorithm(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Algorithm.valueOf(pref.getString(ALGORITHM, Algorithm.JARO_WINKLER.name()));
    }

    /**
     * Set the default algorithm provider
     *
     * @param ctx       the application context
     * @param algorithm to set as the default
     */
    public static void setAlgorithm(@NonNull final Context ctx, final @NonNull Algorithm algorithm) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(ALGORITHM, algorithm.name());
        edit.apply();
    }

    /**
     * Get the serialised string of the array of blacklisted applications which will be coerced into
     * a {@link ai.saiy.android.api.helper.BlackList} objects using {@link com.google.gson.Gson}
     *
     * @param ctx the application context
     * @return the serialised string or null
     */
    public static String getBlacklistArray(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(BLACKLIST, null);
    }

    /**
     * Set the serialised string of the blacklist array which has been coerced into
     * a {@link ai.saiy.android.api.helper.BlackList} objects using {@link com.google.gson.Gson}
     *
     * @param ctx       the application context
     * @param blacklist the serialised string
     */
    public static void setBlacklistArray(@NonNull final Context ctx, @Nullable final String blacklist) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(BLACKLIST, blacklist);
        edit.apply();
    }

    public static long getLastDrivingTime(Context context) {
        return getPref(context).getLong(LAST_DRIVING_TIME, 1L);
    }

    public static void setLastDrivingTime(Context context, long timestamp) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putLong(LAST_DRIVING_TIME, timestamp);
        edit.apply();
    }

    /**
     * Get the serialised string of the last ActivityRecognition which will be coerced into
     * a {@link Motion} object
     * using {@link com.google.gson.Gson}
     *
     * @param ctx the application context
     * @return the serialised string or null
     */
    public static String getMotion(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(MOTION, null);
    }

    /**
     * Set the serialised string of the last ActivityRecognition which has been coerced into
     * a {@link Motion} object
     * using {@link com.google.gson.Gson}
     *
     * @param ctx    the application context
     * @param motion the serialised string
     */
    public static void setMotion(@NonNull final Context ctx, @Nullable final String motion) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(MOTION, motion);
        edit.apply();
    }

    /**
     * Get the serialised string of the last emotion analysis which will be coerced into
     * a {@link ai.saiy.android.cognitive.emotion.provider.beyondverbal.AnalysisResult} object
     * using {@link com.google.gson.Gson}
     *
     * @param ctx the application context
     * @return the serialised string or null
     */
    public static String getEmotion(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(EMOTION, null);
    }

    /**
     * Set the serialised string of the last emotion analysis which has been coerced into
     * a {@link ai.saiy.android.cognitive.emotion.provider.beyondverbal.AnalysisResult} object
     * using {@link com.google.gson.Gson}
     *
     * @param ctx     the application context
     * @param emotion the serialised string
     */
    public static void setEmotion(@NonNull final Context ctx, @Nullable final String emotion) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(EMOTION, emotion);
        edit.apply();
    }

    /**
     * Get the serialised string of the Saiy accounts which will be coerced into
     * a {@link ai.saiy.android.user.SaiyAccountList} object using {@link com.google.gson.Gson}
     *
     * @param ctx the application context
     * @return the serialised string or null
     */
    public static String getSaiyAccounts(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(SAIY_ACCOUNTS, null);
    }

    /**
     * Set the serialised string of the Saiy accounts which has been coerced into
     * a {@link ai.saiy.android.user.SaiyAccountList} object using {@link com.google.gson.Gson}
     *
     * @param ctx         the application context
     * @param accountList the serialised string
     */
    public static void setSaiyAccounts(@NonNull final Context ctx, @Nullable final String accountList) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(SAIY_ACCOUNTS, accountList);
        edit.apply();
    }

    public static long getRateMe(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getLong(RATE_ME, 101L);
    }

    public static void setRateMe(@NonNull final Context ctx, final long count) {
        final SharedPreferences.Editor edit = getEditor(getPref(ctx));
        edit.putLong(RATE_ME, count);
        edit.apply();
    }

    /**
     * Get the serialised string of the last action Saiy performed which will be coerced into
     * a {@link Memory} object using {@link com.google.gson.Gson}
     *
     * @param ctx the application context
     * @return the serialised string or null
     */
    public static String getMemory(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(MEMORY, null);
    }

    /**
     * Set the serialised string of the last action Saiy performed which has been coerced into
     * a {@link Memory} object using {@link com.google.gson.Gson}
     *
     * @param ctx    the application context
     * @param memory the serialised string
     */
    public static void setMemory(@NonNull final Context ctx, @NonNull final String memory) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(MEMORY, memory);
        edit.apply();
    }

    /**
     * Get the serialised string of the user's default Text to Speech Voice which will be coerced into
     * a {@link android.speech.tts.Voice} object using {@link com.google.gson.Gson}
     *
     * @param ctx the application context
     * @return the serialised string or null
     */
    public static String getDefaultTTSVoice(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(DEFAULT_TTS_VOICE, null);
    }

    /**
     * Set the serialised string of the user's default Text to Speech Voice which has been coerced into
     * a {@link android.speech.tts.Voice} object using {@link com.google.gson.Gson}
     *
     * @param ctx   the application context
     * @param voice the serialised string
     */
    public static void setDefaultTTSVoice(@NonNull final Context ctx, @Nullable final String voice) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(DEFAULT_TTS_VOICE, voice);
        edit.apply();
    }

    /**
     * Get the default translation provider
     *
     * @param ctx the application context
     * @return the default translation provider
     */
    public static int getDefaultTranslationProvider(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(TRANSLATION_PROVIDER, TranslationProvider.TRANSLATION_PROVIDER_GOOGLE);
    }

    /**
     * Set the default translation provider
     *
     * @param ctx      the application context
     * @param provider of the translation
     */
    public static void setDefaultTranslationProvider(@NonNull final Context ctx, final int provider) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(TRANSLATION_PROVIDER, provider);
        edit.apply();
    }

    /**
     * Set the Bing refresh token timeout
     *
     * @param ctx  the application context
     * @param time the {@link System#currentTimeMillis()}
     */
    public static void setBingTokenExpiryTime(@NonNull final Context ctx, final long time) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);
        edit.putLong(BING_OAUTH_UPDATE, time);
        edit.apply();
    }

    /**
     * Get the Bing refresh token timeout
     *
     * @param ctx the application context
     * @return the time the token was last refreshed
     */
    public static long getBingTokenExpiryTime(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getLong(BING_OAUTH_UPDATE, 0);
    }

    /**
     * Get the Bing access token
     *
     * @param ctx the application context
     * @return the Bing access token or an empty String
     */
    public static String getBingToken(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(BING_TOKEN, null);
    }

    /**
     * Set the Bing access token
     *
     * @param ctx   the application context
     * @param token the Bing access token
     */
    public static void setBingToken(@NonNull final Context ctx, @NonNull final String token) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(BING_TOKEN, token);
        edit.apply();
    }

    /**
     * Get the serialised string of the most recent token credentials which will be coerced into
     * an {@link BVCredentials}
     * object using {@link com.google.gson.Gson}
     *
     * @param ctx the application context
     * @return the serialised string or null
     */
    public static String getBeyondVerbalCredentials(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(BEYOND_VERBAL_AUTH_RESPONSE, null);
    }

    /**
     * Set the serialised string of the last token credentials which has been coerced into
     * a {@link BVCredentials}
     * object using {@link com.google.gson.Gson}
     *
     * @param ctx         the application context
     * @param credentials the serialised string
     */
    public static void setBeyondVerbalCredentials(@NonNull final Context ctx, @NonNull final String credentials) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(BEYOND_VERBAL_AUTH_RESPONSE, credentials);
        edit.apply();
    }

    /**
     * Get the user assigned pause timeout
     *
     * @param ctx the application context
     * @return the pause timeout
     */
    public static long getPauseTimeout(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getLong(PAUSE_TIMEOUT, RecognitionNative.PAUSE_TIMEOUT);
    }

    /**
     * Set the user assigned pause timeout
     *
     * @param ctx     the application context
     * @param timeout to assign
     */
    public static void setPauseTimeout(@NonNull final Context ctx, final Long timeout) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putLong(PAUSE_TIMEOUT, timeout);
        edit.apply();
    }

    /**
     * Set that the user has seen the what's new note.
     *
     * @param ctx the application context
     */
    public static void setWhatsNew(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(WHATS_NEW, true);
        edit.apply();
    }

    /**
     * Get whether or not the user has seen the what's new note.
     *
     * @param ctx the application context
     * @return true if the user has seen the what's new note, false otherwise
     */
    public static boolean getWhatsNew(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(WHATS_NEW, false);
    }

    /**
     * Set that the user has already heard the verbose enrollment explanation.
     *
     * @param ctx the application context
     */
    public static void setEnrollmentVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(ENROLLMENT_VERBOSE, true);
        edit.apply();
    }

    /**
     * Get if the user has already heard the verbose enrollment explanation.
     *
     * @param ctx the application context
     * @return true if the user has heard the verbose explanation, false otherwise
     */
    public static boolean getEnrollmentVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(ENROLLMENT_VERBOSE, false);
    }

    /**
     * Set that the user has seen the developer note.
     *
     * @param ctx the application context
     */
    public static void setDeveloperNote(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(DEVELOPER_NOTE, true);
        edit.apply();
    }

    /**
     * Get whether or not the user has seen the developer note.
     *
     * @param ctx the application context
     * @return true if the user has seen the developer note, false otherwise
     */
    public static boolean getDeveloperNote(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(DEVELOPER_NOTE, false);
    }

    /**
     * Set that the user has accepted the disclaimer.
     *
     * @param ctx the application context
     */
    public static void setAcceptedDisclaimer(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(DISCLAIMER, true);
        edit.apply();
    }

    /**
     * Get whether or not the user has accepted the disclaimer.
     *
     * @param ctx the application context
     * @return true if the disclaimer has been accepted, false otherwise
     */
    public static boolean getAcceptedDisclaimer(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(DISCLAIMER, false);
    }

    /**
     * Set whether or not to toast unknown commands.
     *
     * @param ctx       the application context
     * @param condition to be applied.
     */
    public static void setToastUnknown(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(TOAST_UNKNOWN, condition);
        edit.apply();
    }

    /**
     * Get whether or not to toast unknown commands.
     *
     * @param ctx the application context
     * @return true if commands should be toasted, false otherwise
     */
    public static boolean getToastUnknown(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(TOAST_UNKNOWN, true);
    }

    /**
     * Set whether or not to track the user's motion.
     *
     * @param ctx       the application context
     * @param condition to be applied.
     */
    public static void setMotionEnabled(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(MOTION_ENABLED, condition);
        edit.apply();
    }

    /**
     * Get whether or not to track the user's motion
     *
     * @param ctx the application context
     * @return true if motion should be tracked, false otherwise
     */
    public static boolean getMotionEnabled(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(MOTION_ENABLED, true);
    }

    /**
     * Set whether or not to start the {@link SelfAware} service is enabled by the user.
     *
     * @param ctx       the application context
     * @param condition to be applied.
     */
    public static void setSelfAwareEnabled(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(SELF_AWARE_ENABLED, condition);
        edit.apply();
    }

    /**
     * Get whether or not to start the {@link SelfAware} service is enabled by the user.
     *
     * @param ctx the application context
     * @return true if the {@link SelfAware} should be enabled, false otherwise
     */
    public static boolean getSelfAwareEnabled(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(SELF_AWARE_ENABLED, true);
    }

    public static boolean getCallConfirmation(Context context) {
        return getPref(context).getBoolean(CALL_CONFIRMATION, true);
    }

    public static void setCallConfirmation(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(CALL_CONFIRMATION, condition);
        edit.apply();
    }

    public static int getWeatherProvider(Context context) {
        return getPref(context).getInt(WEATHER_PROVIDER, WeatherProvider.OPEN_WEATHER_MAP);
    }

    public static void setWeatherProvider(Context context, int provider) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putInt(WEATHER_PROVIDER, provider);
        edit.apply();
    }

    public static boolean hasPhrase(Context context) {
        return getPref(context).getBoolean(HAS_PHRASE, false);
    }

    public static void setHasPhrase(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(HAS_PHRASE, condition);
        edit.apply();
    }

    public static boolean hasNickname(Context context) {
        return getPref(context).getBoolean(HAS_NICKNAME, false);
    }

    public static void setHasNickname(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(HAS_NICKNAME, condition);
        edit.apply();
    }

    public static boolean hasReplacement(Context context) {
        return getPref(context).getBoolean(HAS_REPLACEMENT, false);
    }

    public static void setHasReplacement(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(HAS_REPLACEMENT, condition);
        edit.apply();
    }

    public static boolean hasCustomisation(Context context) {
        return getPref(context).getBoolean(HAS_CUSTOM, false);
    }

    public static void setHasCustomisation(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(HAS_CUSTOM, condition);
        edit.apply();
    }

    public static boolean hasTaskerVariables(Context context) {
        return getPref(context).getBoolean(HAS_TASKER_VARIABLES, false);
    }

    /**
     * Set whether or not to start the {@link SelfAware} service at boot.
     *
     * @param ctx       the application context
     * @param condition to be applied.
     */
    public static void setStartAtBoot(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(BOOT_START, condition);
        edit.apply();
    }

    /**
     * Check if the {@link SelfAware} should start at boot
     *
     * @param ctx the application context
     * @return true if the {@link SelfAware} should start at boot, false otherwise
     */
    public static boolean getStartAtBoot(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(BOOT_START, true);
    }

    /**
     * Get the default Text to Speech provider
     *
     * @param ctx the application context
     * @return the default Text to Speech provider
     */
    public static SaiyDefaults.TTS getDefaultTTS(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return SaiyDefaults.getProviderTTS(pref.getString(DEFAULT_TTS, SaiyDefaults.TTS.LOCAL.name()));
    }

    /**
     * Set the default Text to Speech provider
     *
     * @param ctx      the application context
     * @param provider of the Text to Speech
     */
    public static void setDefaultTTS(@NonNull final Context ctx, @NonNull final SaiyDefaults.TTS provider) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(DEFAULT_TTS, provider.name());
        edit.apply();
    }

    /**
     * Get the user preferred Text to Speech voice gender
     *
     * @param ctx the application context
     * @return one of {@link Gender#FEMALE} or {@link Gender#MALE}
     */
    public static Gender getDefaultTTSGender(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Gender.getGender(pref.getString(DEFAULT_TTS_GENDER, Gender.FEMALE.name()));
    }

    /**
     * Set the user preferred Text to Speech voice gender
     *
     * @param ctx    the application context
     * @param gender one of {@link Gender#FEMALE} or {@link Gender#MALE}
     */
    public static void setDefaultTTSGender(@NonNull final Context ctx, @NonNull final Gender gender) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(DEFAULT_TTS_GENDER, gender.name());
        edit.apply();
    }

    /**
     * Set whether the user wishes their voice data to be subject to emotion analysis
     *
     * @param ctx       the application context
     * @param condition to apply
     */
    public static void setEmotionPermission(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(EMOTION_PERMISSION, condition);
        edit.apply();
    }

    /**
     * Get whether the user wishes their voice data to be subject to emotion analysis
     *
     * @param ctx the application context
     * @return true if they require emotion analysis
     */
    public static boolean getEmotionPermission(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(EMOTION_PERMISSION, false);
    }

    /**
     * Set the default {@link Locale} of the voice recognition
     *
     * @param ctx          the application context
     * @param nativeLocale to apply as the default.
     */
    public static void setVRLocale(@NonNull final Context ctx, @NonNull final Locale nativeLocale) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(VR_LOCALE, nativeLocale.toString());
        edit.apply();
    }

    /**
     * Get the default {@link Locale} of the voice recognition
     *
     * @param ctx the application context
     * @return the default {@link Locale} of the recognition
     */
    public static Locale getVRLocale(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        String localeString = pref.getString(VR_LOCALE, UtilsLocale.getDefaultLocale().toString());
        if (UtilsString.notNaked(localeString)) {
            return UtilsLocale.stringToLocale(localeString);
        }
        final Locale locale = UtilsLocale.stringToLocale(SupportedLanguage.getGoogleNativeVRSupportedLanguageString());
        setVRLocale(ctx, locale);
        return locale;
    }

    /**
     * Set the default {@link Locale} of the Text to Speech engine
     *
     * @param ctx          the application context
     * @param nativeLocale to apply as the default.
     */
    public static void setTTSLocale(@NonNull final Context ctx, @NonNull final Locale nativeLocale) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(TTS_LOCALE, nativeLocale.toString());
        edit.apply();
    }

    /**
     * Get the default {@link Locale} of the Text to Speech engine
     *
     * @param ctx the application context
     * @return the default {@link Locale} of the engine
     */
    public static Locale getTTSLocale(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        String localeString = pref.getString(TTS_LOCALE, UtilsLocale.getDefaultLocale().toString());
        if (UtilsString.notNaked(localeString)) {
            return UtilsLocale.stringToLocale(localeString);
        }
        Locale locale = UtilsLocale.stringToLocale(SupportedLanguage.getGoogleNativeVRSupportedLanguageString());
        setTTSLocale(ctx, locale);
        return locale;
    }

    /**
     * Get the user preferred inactivity timeout
     *
     * @param ctx the application context
     * @return the value in milliseconds
     */
    public static long getInactivityTimeout(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getLong(INACTIVITY_TIMEOUT, SelfAwareConditions.DEFAULT_INACTIVITY_TIMEOUT);
    }

    /**
     * Set the user preferred inactivity timeout, to release memory resources
     *
     * @param ctx     the application context
     * @param timeout in milliseconds
     */
    public static void setInactivityTimeout(@NonNull final Context ctx, final long timeout) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putLong(INACTIVITY_TIMEOUT, timeout);
        edit.apply();
    }

    public static boolean getInactivityToast(Context context) {
        return getPref(context).getBoolean(INACTIVITY_TOAST, false);
    }

    public static void setInactivityToast(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(INACTIVITY_TOAST, condition);
        edit.apply();
    }

    /**
     * Get the amount of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     * @return the integer number of times
     */
    public static int getSpellCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(SPELL_COMMAND_VERBOSE, ZERO);
    }

    /**
     * Increment the number of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     */
    public static void incrementSpellCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(SPELL_COMMAND_VERBOSE, (getSpellCommandVerbose(ctx) + 1));
        edit.apply();
    }

    /**
     * Get the amount of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     * @return the integer number of times
     */
    public static int getTranslateCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(TRANSLATE_COMMAND_VERBOSE, ZERO);
    }

    /**
     * Increment the number of times the user has been informed of this verbose information
     *
     * @param ctx the application context
     */
    public static void incrementTranslateCommandVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(TRANSLATE_COMMAND_VERBOSE, (getTranslateCommandVerbose(ctx) + 1));
        edit.apply();
    }

    /**
     * Set the user's preferred vibration condition
     *
     * @param ctx       the application context
     * @param condition to set
     */
    public static void setVibrateCondition(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(VIBRATE, condition);
        edit.apply();
    }

    /**
     * Get the user's preferred vibration condition
     *
     * @param ctx the application context
     * @return true if the user requires haptic feedback
     */
    public static boolean getVibrateCondition(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(VIBRATE, true);
    }

    /**
     * Set the user's preference for how long a 'ping' request should be given to timeout.
     *
     * @param ctx     the application context
     * @param timeout in milliseconds to be applied
     */
    public static void setPingTimeout(@NonNull final Context ctx, final int timeout) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(PING_TIMEOUT, timeout);
        edit.apply();
    }

    /**
     * Get the user's preference for how long a 'ping' request should be given to timeout.
     *
     * @param ctx the application context
     * @return the timeout in milliseconds
     */
    public static int getPingTimeout(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(PING_TIMEOUT, Network.PING_TIMEOUT);
    }

    /**
     * Set the user's preference for announcing notifications.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setAnnounceNotifications(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(ANNOUNCE_NOTIFICATIONS, condition);
        edit.apply();
    }

    /**
     * Get the user's preference for announcing notifications.
     *
     * @param ctx the application context
     * @return true if notification content should be announced, false otherwise
     */
    public static boolean getAnnounceNotifications(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(ANNOUNCE_NOTIFICATIONS, false);
    }

    public static boolean getAnnounceNotificationsSecure(Context context) {
        return getPref(context).getBoolean(ANNOUNCE_NOTIFICATIONS_SECURE, false);
    }

    public static void setAnnounceNotificationsSecure(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(ANNOUNCE_NOTIFICATIONS_SECURE, condition);
        edit.apply();
    }

    public static boolean getAnnounceNotificationsSMS(Context context) {
        return getPref(context).getBoolean(ANNOUNCE_NOTIFICATIONS_SMS, false);
    }

    public static void setAnnounceNotificationsSMS(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(ANNOUNCE_NOTIFICATIONS_SMS, condition);
        edit.apply();
    }

    public static boolean getAnnounceNotificationsHangouts(Context context) {
        return getPref(context).getBoolean(ANNOUNCE_NOTIFICATIONS_HANGOUTS, false);
    }

    public static void setAnnounceNotificationsHangouts(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(ANNOUNCE_NOTIFICATIONS_HANGOUTS, condition);
        edit.apply();
    }

    public static boolean getAnnounceNotificationsWhatsapp(Context context) {
        return getPref(context).getBoolean(ANNOUNCE_NOTIFICATIONS_WHATSAPP, false);
    }

    public static void setAnnounceNotificationsWhatsapp(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(ANNOUNCE_NOTIFICATIONS_WHATSAPP, condition);
        edit.apply();
    }

    public static boolean isAutoConnectHeadset(Context context) {
        return getPref(context).getBoolean(AUTO_CONNECT_HEADSET, true);
    }

    public static void setAutoConnectHeadset(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(AUTO_CONNECT_HEADSET, condition);
        edit.apply();
    }

    public static @BluetoothConstants.HeadsetSystem int getHeadsetSystem(Context context) {
        return getPref(context).getInt(HEADSET_SYSTEM, BluetoothConstants.SYSTEM_ONE);
    }

    public static void setHeadsetSystem(Context context, @BluetoothConstants.HeadsetSystem int i) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putInt(HEADSET_SYSTEM, i);
        edit.apply();
    }

    public static @BluetoothConstants.HeadsetStreamType int getHeadsetStreamType(Context context) {
        return getPref(context).getInt(HEADSET_STREAM_TYPE, BluetoothConstants.STREAM_COMMUNICATION);
    }

    public static void setHeadsetStreamType(Context context, @BluetoothConstants.HeadsetStreamType int i) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putInt(HEADSET_STREAM_TYPE, i);
        edit.apply();
    }

    public static @BluetoothConstants.HeadsetConnectionType int getHeadsetConnectionType(Context context) {
        return getPref(context).getInt(HEADSET_CONNECTION_TYPE, BluetoothConstants.CONNECTION_A2DP);
    }

    public static void setHeadsetConnectionType(Context context, @BluetoothConstants.HeadsetConnectionType int i) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putInt(HEADSET_CONNECTION_TYPE, i);
        edit.apply();
    }

    /**
     * Set the user's preference for pinging the network prior to making a request.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setPingCheck(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(PING_CHECK, condition);
        edit.apply();
    }

    /**
     * Get the user's preference for pinging the network prior to making a request.
     *
     * @param ctx the application context
     * @return true if the network should be pinged, false otherwise
     */
    public static boolean getPingCheck(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(PING_CHECK, true);
    }

    /**
     * Set the user's preference for hotword wakelock.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setHotwordWakelock(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(HOTWORD_WAKELOCK, condition);
        edit.apply();
    }

    /**
     * Get the user's preference for hotword wakelock.
     *
     * @param ctx the application context
     * @return true if hotword should hold a wakelock
     */
    public static boolean getHotwordWakelock(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(HOTWORD_WAKELOCK, false);
    }

    public static boolean getHotwordOkayGoogle(Context context) {
        return getPref(context).getBoolean(HOTWORD_OKAY_GOOGLE, true);
    }

    public static void setHotwordOkayGoogle(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(HOTWORD_OKAY_GOOGLE, condition);
        edit.apply();
    }

    /**
     * Set the user's preference for hotword when driving.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setHotwordDriving(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(HOTWORD_DRIVING, condition);
        edit.apply();
    }

    /**
     * Get the user's preference for hotword when driving.
     *
     * @param ctx the application context
     * @return true if hotword should begin when driving
     */
    public static boolean getHotwordDriving(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(HOTWORD_DRIVING, false);
    }

    public static boolean getHotwordStartDriving(Context context) {
        return getPref(context).getBoolean(HOTWORD_START_DRIVING, false);
    }

    public static void setHotwordStartDriving(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(HOTWORD_START_DRIVING, condition);
        edit.apply();
    }

    public static boolean getHotwordStopDriving(Context context) {
        return getPref(context).getBoolean(HOTWORD_STOP_DRIVING, false);
    }

    public static void setHotwordStopDriving(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(HOTWORD_STOP_DRIVING, condition);
        edit.apply();
    }

    /**
     * Set the user's preference for hotword security.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setHotwordSecure(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(HOTWORD_SECURE, condition);
        edit.apply();
    }

    /**
     * Get the user's preference for hotword security.
     *
     * @param ctx the application context
     * @return true if hotword commands should be secure
     */
    public static boolean getHotwordSecure(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(HOTWORD_SECURE, true);
    }

    /**
     * Set the user's preference for hotword detection starting at boot.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setHotwordBoot(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(HOTWORD_BOOT, condition);
        edit.apply();
    }

    /**
     * Get the user's preference for hotword detection starting at boot.
     *
     * @param ctx the application context
     * @return true if hotword detection should start at boot
     */
    public static boolean getHotwordBoot(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(HOTWORD_BOOT, false);
    }

    public static boolean getHotwordStats(Context context) {
        return getPref(context).getBoolean(HOTWORD_USAGE_STATS, false);
    }

    public static void markHotwordStats(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(HOTWORD_USAGE_STATS, true);
        edit.apply();
    }

    public static boolean getResetSpeaker(Context context) {
        return getPref(context).getBoolean(RESET_SPEAKER, false);
    }

    public static void setResetSpeaker(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(RESET_SPEAKER, condition);
        edit.apply();
    }

    public static boolean isDesignOverviewShown(Context context) {
        return getPref(context).getBoolean(DESIGN_OVERVIEW, false);
    }

    public static void markDesignOverviewShown(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(DESIGN_OVERVIEW, true);
        edit.apply();
    }

    public static long getFragmentIncrement(Context context) {
        return getPref(context).getLong(FRAGMENT_INCREMENT, 1L);
    }

    public static void autoIncreaseFragment(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putLong(FRAGMENT_INCREMENT, 1 + getFragmentIncrement(context));
        edit.apply();
    }

    public static boolean isUnknownShown(Context context) {
        return getPref(context).getBoolean(SHOWN_UNKNOWN, false);
    }

    public static void markUnknownShown(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(SHOWN_UNKNOWN, true);
        edit.apply();
    }

    public static String getBlockedNotificationApplications(Context context) {
        return getPref(context).getString(BLOCKED_NOTIFICATION_APPLICATIONS, null);
    }

    public static void setBlockedNotificationApplications(Context context, String str) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(BLOCKED_NOTIFICATION_APPLICATIONS, str);
        edit.apply();
    }

    /**
     * Set the user's preference for offline voice recognition.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setUseOffline(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(USE_OFFLINE, condition);
        edit.apply();
    }

    /**
     * Get the user's preference for offline voice recognition.
     *
     * @param ctx the application context
     * @return true if offline recognition is preferred
     */
    public static boolean getUseOffline(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(USE_OFFLINE, false);
    }

    /**
     * Set the user's preference for a network synthesised voice.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setNetworkSynthesis(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(NETWORK_SYNTHESIS, condition);
        edit.apply();
    }

    public static boolean isNetworkSynthesisWifi(Context context) {
        return getPref(context).getBoolean(NETWORK_SYNTHESIS_WIFI, true);
    }

    public static void setNetworkSynthesisWifi(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(NETWORK_SYNTHESIS_WIFI, condition);
        edit.apply();
    }

    public static boolean isNetworkSynthesis4g(Context context) {
        return getPref(context).getBoolean(NETWORK_SYNTHESIS_4G, true);
    }

    public static void setNetworkSynthesis4g(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(NETWORK_SYNTHESIS_4G, condition);
        edit.apply();
    }

    public static String getFirebaseAnonymousUid(Context context) {
        return getPref(context).getString(FIREBASE_ANONYMOUS_UID, null);
    }

    public static void setFirebaseAnonymousUid(Context context, String str) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(FIREBASE_ANONYMOUS_UID, str);
        edit.apply();
    }

    public static String getFirebaseUid(Context context) {
        return getPref(context).getString(FIREBASE_AUTH_UID, null);
    }

    public static void setFirebaseUid(Context context, String str) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(FIREBASE_AUTH_UID, str);
        edit.apply();
    }

    public static String getFirebaseMigratedUid(Context context) {
        return getPref(context).getString(FIREBASE_MIGRATED_UID, null);
    }

    public static void setFirebaseMigratedUid(Context context, String str) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(FIREBASE_MIGRATED_UID, str);
        edit.apply();
    }

    /**
     * Get the user's preference for a network synthesised voice.
     *
     * @param ctx the application context
     * @return true if network synthesis is preferred
     */
    public static boolean getNetworkSynthesis(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(NETWORK_SYNTHESIS, true);
    }

    public static void markNoteProviderVerbose(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(NOTE_PROVIDER_VERBOSE, true);
        edit.apply();
    }

    public static boolean getNoteProviderVerbose(Context context) {
        return getPref(context).getBoolean(NOTE_PROVIDER_VERBOSE, false);
    }

    /**
     * Get the user default action for an unknown command
     *
     * @param ctx the application context
     * @return the action constant
     */
    public static int getCommandUnknownAction(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(COMMAND_UNKNOWN_ACTION, Unknown.UNKNOWN_STATE);
    }

    /**
     * Set the user default action for an unknown command
     *
     * @param ctx    the application context
     * @param action to set
     */
    public static void setCommandUnknownAction(@NonNull final Context ctx, final int action) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(COMMAND_UNKNOWN_ACTION, action);
        edit.apply();
    }

    public static long getDrivingCooldownTime(Context context) {
        return getPref(context).getLong(DRIVING_COOLDOWN_TIME, 1L);
    }

    public static void setDrivingCooldownTime(Context context, long timestamp) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putLong(DRIVING_COOLDOWN_TIME, timestamp);
        edit.apply();
    }

    /**
     * Get the minimum require connection level to process network events
     *
     * @param ctx the application context
     * @return the integer constant from {@link Network}
     */
    public static int getConnectionMinimum(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(CONNECTION_MINIMUM, Network.CONNECTION_TYPE_3G);
    }

    /**
     * Set the minimum require connection level to process network events
     *
     * @param ctx        the application context
     * @param connection to set as a minimum
     */
    public static void setConnectionMinimum(@NonNull final Context ctx, final int connection) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(CONNECTION_MINIMUM, connection);
        edit.apply();
    }

    /**
     * Get the hotword the user has enrolled.
     *
     * @param ctx the application context
     * @return the enrolled hotword or null if one has yet to be enrolled
     */
    public static String getHotword(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(HOTWORD, SaiyRequestParams.SILENCE);
    }

    /**
     * Set the user's enrolled hotword
     *
     * @param ctx     the application context
     * @param hotword that has been enrolled
     */
    public static void setHotword(@NonNull final Context ctx, final String hotword) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(HOTWORD, hotword);
        edit.apply();
    }

    /**
     * Get the user defined custom intro
     *
     * @param ctx the application context
     * @return the user's name or 'master' if none is applied
     */
    public static String getCustomIntro(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(CUSTOM_INTRO, null);
    }

    /**
     * Set the user defined custom intro
     *
     * @param ctx   the application context
     * @param intro by which they wish to be called
     */
    public static void setCustomIntro(@NonNull final Context ctx, final String intro) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(CUSTOM_INTRO, intro);
        edit.apply();
    }

    /**
     * Set the user's preference for randomising their custom intro.
     *
     * @param ctx       the application context
     * @param condition to be applied
     */
    public static void setCustomIntroRandom(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(CUSTOM_INTRO_RANDOM, condition);
        edit.apply();
    }

    /**
     * Get the user's preference for randomising their custom intro.
     *
     * @param ctx the application context
     * @return true if offline recognition is preferred
     */
    public static boolean getCustomIntroRandom(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(CUSTOM_INTRO_RANDOM, true);
    }

    /**
     * Get the user defined name by which they wish to be addressed.
     *
     * @param ctx the application context
     * @return the user's name or 'master' if none is applied
     */
    public static String getUserName(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(USER_NAME, ctx.getString(ai.saiy.android.R.string.master));
    }

    /**
     * Set the user defined name by which they wish to be addressed.
     *
     * @param ctx      the application context
     * @param userName by which they wish to be called
     */
    public static void setUserName(@NonNull final Context ctx, final String userName) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(USER_NAME, userName);
        edit.apply();
    }

    public static Gender getUserGender(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return Gender.getGender(pref.getString(USER_GENDER, Gender.FEMALE.name()));
    }

    public static void setUserGender(@NonNull final Context ctx, @NonNull final Gender gender) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(USER_GENDER, gender.name());
        edit.apply();
    }

    public static String getUserAccount(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getString(USER_ACCOUNT, null);
    }

    public static void setUserAccount(@NonNull final Context ctx, @NonNull final String account) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putString(USER_ACCOUNT, account);
        edit.apply();
    }

    public static boolean getTorchFix(Context context) {
        return getPref(context).getBoolean(TORCH_FIX, false);
    }

    public static void setTorchFix(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(TORCH_FIX, condition);
        edit.apply();
    }

    public static int getLocationProvider(Context context) {
        return getPref(context).getInt(LOCATION_PROVIDER, Constants.FUSED_LOCATION_PROVIDER);
    }

    public static void setLocation(Context context, @NonNull Location location) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(VEHICLE_LOCATION_LAT, String.valueOf(location.getLatitude()));
        edit.putString(VEHICLE_LOCATION_LONG, String.valueOf(location.getLongitude()));
        edit.apply();
    }

    public static @Nullable Pair<Double, Double> getLastLocation(Context context) {
        final SharedPreferences pref = getPref(context);
        final String latitudeString = pref.getString(VEHICLE_LOCATION_LAT, null);
        final String longitudeString = pref.getString(VEHICLE_LOCATION_LONG, null);
        if (ai.saiy.android.utils.UtilsString.notNaked(latitudeString) && ai.saiy.android.utils.UtilsString.notNaked(longitudeString)) {
            try {
                return new Pair<>(Double.parseDouble(latitudeString), Double.parseDouble(longitudeString));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    public static CommandHoroscopeValues.Sign getStarSign(Context context) {
        return CommandHoroscopeValues.Sign.valueOf(getPref(context).getString(STAR_SIGN, CommandHoroscopeValues.Sign.UNKNOWN.name()));
    }

    public static int getDobDay(Context context) {
        return getPref(context).getInt(DOB_DAY, 1);
    }

    public static int getDobMonth(Context context) {
        return getPref(context).getInt(DOB_MONTH, 0);
    }

    public static int getDobYear(Context context) {
        return getPref(context).getInt(DOB_YEAR, 1950);
    }

    public static int getUnknownContactVerbose(Context context) {
        return getPref(context).getInt(UNKNOWN_CONTACT_VERBOSE, 0);
    }

    public static void autoIncreaseUnknownContactVerbose(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putInt(UNKNOWN_CONTACT_VERBOSE, getUnknownContactVerbose(context) + 1);
        edit.apply();
    }

    public static int getEmailAutoVerbose(Context context) {
        return getPref(context).getInt(EMAIL_AUTO_VERBOSE, 0);
    }

    public static void autoIncreaseEmailAutoVerbose(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putInt(EMAIL_AUTO_VERBOSE, getEmailAutoVerbose(context) + 1);
        edit.apply();
    }

    public static void setHoroscope(Context context, int dayOfMonth, int month, int year, CommandHoroscopeValues.Sign sign) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putInt(DOB_DAY, dayOfMonth);
        edit.putInt(DOB_MONTH, month - 1);
        edit.putInt(DOB_YEAR, year);
        if (sign != null) {
            edit.putString(STAR_SIGN, sign.name());
        } else {
            edit.putString(STAR_SIGN, CommandHoroscopeValues.Sign.UNKNOWN.name());
        }
        edit.apply();
    }

    public static String getUserAttendeeName(Context context) {
        return getPref(context).getString(USER_ATTENDEE_NAME, null);
    }

    public static void setUserAttendeeName(Context context, String name) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(USER_ATTENDEE_NAME, name);
        edit.apply();
    }

    public static long getLastContactUpdate(Context context) {
        return getPref(context).getLong(LAST_CONTACT_UPDATE, 1L);
    }

    public static void setLastContactUpdate(Context context, long timeStamp) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putLong(LAST_CONTACT_UPDATE, timeStamp);
        edit.apply();
    }

    public static boolean isOfflineInstallationShown(Context context) {
        final SharedPreferences pref = getPref(context);
        if (pref.getBoolean(SHOWN_OFFLINE_INSTALLATION, false)) {
            return true;
        }
        final SharedPreferences.Editor edit = getEditor(pref);
        edit.putBoolean(SHOWN_OFFLINE_INSTALLATION, true);
        edit.apply();
        return false;
    }

    public static boolean getAccountOverview(Context context) {
        return getPref(context).getBoolean(ACCOUNT_OVERVIEW, false);
    }

    public static void markAccountOverview(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(ACCOUNT_OVERVIEW, true);
        edit.apply();
    }

    public static boolean getAdvertisementOverview(Context context) {
        return getPref(context).getBoolean(AD_OVERVIEW, false);
    }

    public static void markAdvertisementOverview(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(AD_OVERVIEW, true);
        edit.apply();
    }

    public static int getCreditsVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getInt(CREDITS_VERBOSE, ZERO);
    }

    public static void incrementCreditsVerbose(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putInt(CREDITS_VERBOSE, (getCreditsVerbose(ctx) + 1));
        edit.apply();
    }

    public static boolean getPremiumContentVerbose(Context context) {
        return getPref(context).getBoolean(PREMIUM_CONTENT_VERBOSE, false);
    }

    public static void setPremiumContentVerbose(Context context, boolean condition) {
        SharedPreferences.Editor a2 = getEditor(getPref(context));
        a2.putBoolean(PREMIUM_CONTENT_VERBOSE, condition);
        a2.apply();
    }

    public static long getHoroscopeIncrement(Context context) {
        return getPref(context).getLong(HOROSCOPE_INCREMENT, 0L);
    }

    public static void horoscopeAutoIncrease(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putLong(HOROSCOPE_INCREMENT, getHoroscopeIncrement(context) + 1);
        edit.apply();
    }

    public static boolean isCacheSpeech(Context context) {
        return getPref(context).getBoolean(CACHE_SPEECH, true);
    }

    public static void setCacheSpeech(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(CACHE_SPEECH, condition);
        edit.apply();
    }

    public static int getTTSPitch(Context context) {
        return getPref(context).getInt(TTS_PITCH, 100);
    }

    public static void setTTSPitch(Context context, int value) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putInt(TTS_PITCH, value);
        edit.apply();
    }

    public static int getTTSRate(Context context) {
        return getPref(context).getInt(TTS_RATE, 100);
    }

    public static void setTTSRate(Context context, int value) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putInt(TTS_RATE, value);
        edit.apply();
    }

    public static boolean getIgnoreRestrictedContent(Context context) {
        return getPref(context).getBoolean(IGNORE_RESTRICTED_CONTENT, true);
    }

    public static void setIgnoreRestrictedContent(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(IGNORE_RESTRICTED_CONTENT, condition);
        edit.apply();
    }

    public static boolean getSmsIdFix(Context context) {
        return getPref(context).getBoolean(SMS_ID_FIX, false);
    }

    public static void setSmsIdFix(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(SMS_ID_FIX, condition);
        edit.apply();
    }

    public static boolean getSmsBodyFix(Context context) {
        return getPref(context).getBoolean(SMS_BODY_FIX, true);
    }

    public static void setSmsBodyFix(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(SMS_BODY_FIX, condition);
        edit.apply();
    }

    /**
     * Set whether the recogniser should use a workaround
     *
     * @param ctx       the application context
     * @param condition to apply
     */
    public static void setRecogniserBusyFix(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(RECOGNISER_BUSY_FIX, condition);
        edit.apply();
    }

    /**
     * Get whether the recogniser should use a workaround
     *
     * @param ctx the application context
     * @return true as the default, or false if the user has disabled this
     */
    public static boolean getRecogniserBusyFix(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(RECOGNISER_BUSY_FIX, Installed.isGoogleNowLauncherDefault(ctx));
    }

    /**
     * Set whether the hotword should use a workaround
     *
     * @param ctx       the application context
     * @param condition to apply
     */
    public static void setOkayGoogleFix(@NonNull final Context ctx, final boolean condition) {
        final SharedPreferences pref = getPref(ctx);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(OKAY_GOOGLE_FIX, condition);
        edit.apply();
    }

    /**
     * Get whether the hotword should use a workaround
     *
     * @param ctx the application context
     * @return true as the default, or false if the user has disabled this
     */
    public static boolean getOkayGoogleFix(@NonNull final Context ctx) {
        final SharedPreferences pref = getPref(ctx);
        return pref.getBoolean(OKAY_GOOGLE_FIX, Installed.isGoogleNowLauncherDefault(ctx));
    }

    public static long getRecognizerBusyIncrement(Context context) {
        return getPref(context).getLong(RECOGNIZER_BUSY_INCREMENT, 0L);
    }

    public static void recognizerBusyAutoIncrease(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putLong(RECOGNIZER_BUSY_INCREMENT, getRecognizerBusyIncrement(context) + 1);
        edit.apply();
    }

    public static void resetRecognizerBusyIncrement(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putLong(RECOGNIZER_BUSY_INCREMENT, 0L);
        edit.apply();
    }

    public static boolean getAnonymousUsageStats(Context context) {
        return getPref(context).getBoolean(ANONYMOUS_USAGE_STATS, true);
    }

    public static void setAnonymousUsageStats(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(ANONYMOUS_USAGE_STATS, condition);
        edit.apply();
    }

    public static boolean getDoubleBeepFix(Context context) {
        return getPref(context).getBoolean(DOUBLE_BEEP_FIX, false);
    }

    public static void setDoubleBeepFix(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(DOUBLE_BEEP_FIX, condition);
        edit.apply();
    }

    public static boolean isNewUser(Context context) {
        return getPref(context).getBoolean(NEW_USER, false);
    }

    public static void markOldUser(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(NEW_USER, false);
        edit.apply();
    }

    public static boolean isFirstForMicroPhone(Context context) {
        final SharedPreferences pref = getPref(context);
        return pref.getBoolean(MIC_FIRST, true);
    }

    public static void markImportWarning(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(IMPORT_WARNING, true);
        edit.apply();
    }

    public static boolean getImportWarning(Context context) {
        return getPref(context).getBoolean(IMPORT_WARNING, false);
    }

    public static void markExportWarning(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(EXPORT_WARNING, true);
        edit.apply();
    }

    public static boolean getExportWarning(Context context) {
        return getPref(context).getBoolean(EXPORT_WARNING, false);
    }

    public static int getHeadsetOverviewCount(Context context) {
        return getPref(context).getInt(HEADSET_OVERVIEW_COUNT, 0);
    }

    public static void headsetOverviewCountAutoIncrease(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putInt(HEADSET_OVERVIEW_COUNT, getHeadsetOverviewCount(context) + 1);
        edit.apply();
    }

    public static void setAccessibilityChange(Context context) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(ACCESSIBILITY_CHANGE, true);
        edit.apply();
    }

    public static boolean getAccessibilityChange(Context context) {
        return getPref(context).getBoolean(ACCESSIBILITY_CHANGE, false);
    }

    public static void setDebugBilling(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(DEBUG_BILLING, condition);
        edit.apply();
    }

    public static boolean getDebugBilling(Context context) {
        return getPref(context).getBoolean(DEBUG_BILLING, false);
    }

    public static int getDefaultNote(Context context) {
        return getPref(context).getInt(DEFAULT_NOTE, NoteProvider.NOTE_ACTION_SELF_NOTE);
    }

    public static void setDefaultNote(Context context, int action) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putInt(DEFAULT_NOTE, action);
        edit.apply();
    }

    public static String setCodeVerifier(Context context) {
        return getPref(context).getString(ALEXA_CODE_VERIFIER, null);
    }

    public static void getCodeVerifier(Context context, String codeVerifier) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(ALEXA_CODE_VERIFIER, codeVerifier);
        edit.apply();
    }

    public static String getAlexaAccessToken(Context context) {
        return getPref(context).getString(ALEXA_ACCESS_TOKEN, null);
    }

    public static void setAlexaAccessToken(Context context, String accessToken) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(ALEXA_ACCESS_TOKEN, accessToken);
        edit.apply();
    }

    public static String getAlexaRefreshToken(Context context) {
        return getPref(context).getString(ALEXA_REFRESH_TOKEN, null);
    }

    public static void setAlexaRefreshToken(Context context, String refreshToken) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(ALEXA_REFRESH_TOKEN, refreshToken);
        edit.apply();
    }

    public static long getAlexaAccessTokenExpiry(Context context) {
        return getPref(context).getLong(ALEXA_ACCESS_TOKEN_EXPIRY, 0L);
    }

    public static void setAlexaAccessTokenExpiry(Context context, long expiryTime) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putLong(ALEXA_ACCESS_TOKEN_EXPIRY, expiryTime);
        edit.apply();
    }

    public static int getAlexaRegion(Context context, int defaultValue) {
        return getPref(context).getInt(ALEXA_REGION, defaultValue);
    }

    public static void setAlexaRegion(Context context, int i) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putInt(ALEXA_REGION, i);
        edit.apply();
    }

    public static String getUniqueID(Context context) {
        return getPref(context).getString(UNIQUE_ID, null);
    }

    public static void setUniqueID(Context context, String str) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(UNIQUE_ID, str);
        edit.apply();
    }

    public static boolean showAlexaNotification(Context context) {
        return getPref(context).getBoolean(ALEXA_NOTIFICATION_BUTTON, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
    }

    public static void setAlexaNotification(Context context, boolean showNotification) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(ALEXA_NOTIFICATION_BUTTON, showNotification);
        edit.apply();
    }

    public static boolean haveShownVolumeBug(Context context) {
        return getPref(context).getBoolean(SHOWN_VOLUME_BUG, false);
    }

    public static void setShownVolumeBug(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(SHOWN_VOLUME_BUG, condition);
        edit.apply();
    }

    public static boolean getShownPauseBug(Context context) {
        return getPref(context).getBoolean(SHOWN_PAUSE_BUG, false);
    }

    public static void setShownPauseBug(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(SHOWN_PAUSE_BUG, condition);
        edit.apply();
    }

    public static boolean isCheckReinstallationNeeded(Context context) {
        return getPref(context).getBoolean(REINSTALLATION_PROCESS, false);
    }

    public static void setCheckReinstallationNeeded(Context context, boolean isNeeded) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(REINSTALLATION_PROCESS, isNeeded);
        edit.apply();
    }

    public static boolean isCheckUnknownSourcesSettingNeeded(Context context) {
        return getPref(context).getBoolean(UNKNOWN_SOURCES, false);
    }

    public static void setCheckUnknownSourcesSettingNeeded(Context context, boolean isNeeded) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(UNKNOWN_SOURCES, isNeeded);
        edit.apply();
    }

    public static boolean announceCallerStats(Context context) {
        return getPref(context).getBoolean(ANNOUNCE_CALLER, false);
    }

    public static void setAnnounceCaller(Context context, boolean enable) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(ANNOUNCE_CALLER, enable);
        edit.apply();
    }

    public static String getDrivingProfile(Context context) {
        return getPref(context).getString(DRIVING_PROFILE, null);
    }

    public static void setDrivingProfile(Context context, String str) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(DRIVING_PROFILE, str);
        edit.apply();
    }

    public static String getQuietTimes(Context context) {
        return getPref(context).getString(QUIET_TIMES, null);
    }

    public static void setQuietTimes(Context context, String str) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putString(QUIET_TIMES, str);
        edit.apply();
    }

    public static boolean getOverrideSecure(Context context) {
        return getPref(context).getBoolean(OVERRIDE_SECURE, false);
    }

    public static void setOverrideSecure(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(OVERRIDE_SECURE, condition);
        edit.apply();
    }

    public static boolean getOverrideSecureHeadset(Context context) {
        return getPref(context).getBoolean(OVERRIDE_SECURE_HEADSET, false);
    }

    public static void setOverrideSecureHeadset(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(OVERRIDE_SECURE_HEADSET, condition);
        edit.apply();
    }

    public static boolean getOverrideSecureDriving(Context context) {
        return getPref(context).getBoolean(OVERRIDE_SECURE_DRIVING, false);
    }

    public static void setOverrideSecureDriving(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(OVERRIDE_SECURE_DRIVING, condition);
        edit.apply();
    }

    public static boolean getRunDiagnostics(Context context) {
        return getPref(context).getBoolean(RUN_DIAGNOSTICS, false);
    }

    public static void setRunDiagnostics(Context context, boolean condition) {
        SharedPreferences.Editor edit = getEditor(getPref(context));
        edit.putBoolean(RUN_DIAGNOSTICS, condition);
        edit.apply();
    }

    public static void markMicroPhoneAccession(Context context) {
        final SharedPreferences pref = getPref(context);
        final SharedPreferences.Editor edit = getEditor(pref);

        edit.putBoolean(MIC_FIRST, false);
        edit.apply();
    }

    public static boolean reset(Context context) {
        final SharedPreferences pref = getPref(context);
        final SharedPreferences.Editor edit = getEditor(pref);
        final boolean result = edit.clear().commit();
        if (result) {
            SPH.setAcceptedDisclaimer(context);
            SPH.setDeveloperNote(context);
        }
        return result;
    }
}
