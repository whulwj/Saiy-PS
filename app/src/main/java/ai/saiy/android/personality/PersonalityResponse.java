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

package ai.saiy.android.personality;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import ai.saiy.android.R;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.localisation.SaiyResourcesHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

/**
 * Helper Class to get standard speech responses. Responses that are user defined,
 * or more personal should be handled in {@link PersonalityHelper}
 * <p>
 * Created by benrandall76@gmail.com on 13/02/2016.
 */
public final class PersonalityResponse {

    /**
     * Prevent instantiation
     */
    public PersonalityResponse() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    /**
     * Get the Beyond Verbal intro response
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the introduction
     */
    public static String getBeyondVerbalIntroResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_beyond_verbal);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the Beyond Verbal error response
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the introduction
     */
    public static String getBeyondVerbalErrorResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_beyond_verbal_error);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the Beyond Verbal verbose introduction.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getBeyondVerbalVerboseResponse(@NonNull final Context ctx,
                                                        @NonNull final SupportedLanguage sl) {
        return UtilsString.stripNameSpace(String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.beyond_verbal_verbose), PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the Beyond Verbal extra verbose introduction.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getBeyondVerbalExtraVerboseResponse(@NonNull final Context ctx,
                                                             @NonNull final SupportedLanguage sl) {
        return UtilsString.stripNameSpace(String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.beyond_verbal_extra_verbose), PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the Beyond Verbal connection error response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getBeyondVerbalServerErrorResponse(@NonNull final Context ctx,
                                                            @NonNull final SupportedLanguage sl) {
        return UtilsString.stripNameSpace(String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_beyond_verbal_connection), PersonalityHelper.getUserNameOrNot(ctx)));
    }

    public static String getRateMe(Context ctx, SupportedLanguage sl) {
        return String.format(SaiyResourcesHelper.getStringResource(ctx, sl, R.string.content_rate_me), SPH.getUserName(ctx));
    }

    public static String getBirthday(Context ctx, SupportedLanguage sl) {
        return String.format(SaiyResourcesHelper.getStringResource(ctx, sl, R.string.content_birthday), SPH.getUserName(ctx));
    }

    public static String getTimeInError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                R.array.array_error_time_in);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the standard Tasker task executed response.
     *
     * @param ctx      the application context
     * @param sl       the {@link SupportedLanguage}
     * @param taskName the task name
     * @return the required response
     */
    public static String getTaskerTaskExecutedResponse(@NonNull final Context ctx,
                                                       @NonNull final SupportedLanguage sl,
                                                       @NonNull final String taskName) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_tasker);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], taskName);
    }

    /**
     * Get the standard Tasker task executed response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getTaskerTaskNotMatchedResponse(@NonNull final Context ctx,
                                                         @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                R.array.array_error_tasker_match);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }


    /**
     * Get the standard Task task failed response.
     *
     * @param ctx      the application context
     * @param sl       the {@link SupportedLanguage}
     * @param taskName the task name
     * @return the required response
     */
    public static String getTaskerTaskFailedResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                                     @NonNull final String taskName) {
        return String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_tasker_execute), taskName);
    }

    /**
     * Get the standard no Tasker tasks response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getTaskerNoTasksResponse(@NonNull final Context ctx,
                                                  @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_tasker_no_tasks);
    }

    /**
     * Get the standard no Tasker external access response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getTaskerExternalAccessResponse(@NonNull final Context ctx,
                                                         @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_tasker_external_access);
    }

    /**
     * Get the standard no Tasker disabled response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getTaskerDisabledResponse(@NonNull final Context ctx,
                                                   @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_tasker_enable);
    }

    /**
     * Get the standard no Tasker not installed response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getTaskerInstallResponse(@NonNull final Context ctx,
                                                  @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_tasker_install);
    }

    /**
     * Get the standard no Tasker install order issue response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getTaskerInstallOrderResponse(@NonNull final Context ctx,
                                                       @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_tasker_permissions);
    }

    /**
     * Get the speech introduction, either a user defined one or an inbuilt intro, adding the user's
     * name if known.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the introduction
     */
    public static String getIntro(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {

        final String userIntro = SPH.getCustomIntro(ctx);

        if (userIntro != null) {

            if (userIntro.isEmpty()) {
                return SaiyRequestParams.SILENCE;
            } else {

                if (SPH.getCustomIntroRandom(ctx)) {

                    final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                            R.array.array_intro);

                    final ArrayList<String> list = new ArrayList<>(stringArray.length);
                    Collections.addAll(list, stringArray);
                    list.add(userIntro);

                    return UtilsString.stripNameSpace(String.format(list.get(new Random()
                            .nextInt(list.size())), PersonalityHelper.getUserNameOrNot(ctx)));
                } else {
                    return userIntro;
                }
            }
        } else {
            final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                    R.array.array_intro);
            return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                    PersonalityHelper.getUserNameOrNot(ctx)));
        }
    }

    /**
     * Get the standard profanity filter response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getErrorProfanityFilter(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_empty_profanity);
    }

    /**
     * Get the standard empty voice data response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getErrorEmptyVoiceData(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_empty_voice_data);
    }

    public static String getOrientationLock(@NonNull final Context ctx, @NonNull final SupportedLanguage sl, String str) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_orientation_lock);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], str);
    }

    public static String getCurrentOrientation(@NonNull final Context ctx, @NonNull final SupportedLanguage sl, String str) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_orientation_current);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], str);
    }

    public static String getCurrentOrientationLock(@NonNull final Context ctx, @NonNull final SupportedLanguage sl, String str) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_orientation_lock_current);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(ctx), str));
    }

    /**
     * Get the standard action unknown response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getErrorActionUnknown(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_unknown_action);
    }

    public static String getKillApplicationError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl, String str) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_kill_application);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(ctx), str));
    }

    /**
     * Get the standard remote command failed response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getErrorRemoteFailed(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                              @NonNull final String appName) {
        return String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_remote), appName);
    }

    /**
     * Get the standard remote command failed unknown response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getErrorRemoteFailedUnknown(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_remote_unknown);
    }

    /**
     * Get the standard remote command registration failure response.
     *
     * @param ctx     the application context
     * @param sl      the {@link SupportedLanguage}
     * @param appName the remote application name
     * @return the required response
     */
    public static String getErrorRemoteCommandRegister(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                                       @NonNull final String appName) {
        return String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_remote_command_register), appName);
    }

    /**
     * Get the standard remote command registration success response.
     *
     * @param ctx     the application context
     * @param sl      the {@link SupportedLanguage}
     * @param appName the remote application name
     * @return the required response
     */
    public static String getRemoteCommandRegisterSuccess(@NonNull final Context ctx,
                                                         @NonNull final SupportedLanguage sl,
                                                         @NonNull final String appName,
                                                         @NonNull final String keyphrase) {
        return String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.remote_command_register_response), appName, keyphrase);
    }

    /**
     * Get the standard remote command success response.
     *
     * @param ctx     the application context
     * @param sl      the {@link SupportedLanguage}
     * @param appName the remote application name
     * @return the required response
     */
    public static String getRemoteSuccess(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                          @NonNull final String appName) {
        return String.format(SaiyResourcesHelper.getStringResource(ctx, sl, R.string.remote_success), appName);
    }

    public static String getLaunchApplicationError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl, String str) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_launch_application);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(ctx), str));
    }

    /**
     * Get the standard no network connection response, adding the user's name if defined.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getNoNetwork(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_no_network);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    public static String getForeground(@NonNull final Context ctx, @NonNull final SupportedLanguage sl, String str) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_foreground);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(ctx), str);
    }

    /**
     * Get the standard no comprende response, adding the user's name if defined.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getNoComprende(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_no_comprende);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    public static String getNoComprendeForAlexa(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_no_comprende_alexa);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    public static String getAlexaNoNetwork(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_no_comprende);
        final String[] noNetworkArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_alexa_no_network);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                noNetworkArray[new Random().nextInt(noNetworkArray.length)]));
    }

    /**
     * Get the standard repeat command response, adding the user's name if defined.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getRepeatCommand(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_repeat_command);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the standard cancel response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getCancelled(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_cancel);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Get the standard user name response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getUserName(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_user_name);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String getFacebookConfirmation(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                                 @NonNull final String content) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_facebook_confirmation);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], content);
    }

    /**
     * Get the standard song recognition response.
     *
     * @param ctx     the application context
     * @param sl      the {@link SupportedLanguage}
     * @param appName the default provider
     * @return the required response
     */
    public static String getSongRecognitionResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                                    @NonNull final String appName) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_song_recognition);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], appName);
    }

    /**
     * Get the secure error response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getSecureErrorResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_secure);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the standard battery information response.
     *
     * @param ctx   the application context
     * @param sl    the {@link SupportedLanguage}
     * @param type  the requested battery information type
     * @param value the value of the requested type
     * @return the required response
     */
    public static String getBatteryResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl,
                                            @NonNull final String type, @NonNull final String value) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_battery);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], type, value);
    }

    /**
     * Get the battery error unknown request response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getBatteryErrorUnknownResponse(@NonNull final Context ctx,
                                                        @NonNull final SupportedLanguage sl) {
        return UtilsString.stripNameSpace(String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_battery_unknown), PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the battery error access request response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getBatteryErrorAccessResponse(@NonNull final Context ctx,
                                                       @NonNull final SupportedLanguage sl) {
        return UtilsString.stripNameSpace(String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_battery_access), PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the song recognition app opening error response.
     *
     * @param ctx     the application context
     * @param sl      the {@link SupportedLanguage}
     * @param appName the default provider
     * @return the required response
     */
    public static String getSongRecognitionErrorAppResponse(@NonNull final Context ctx,
                                                            @NonNull final SupportedLanguage sl,
                                                            @NonNull final String appName) {
        return UtilsString.stripNameSpace(String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_song_recognition_app_failed), PersonalityHelper.getUserNameOrNot(ctx), appName));
    }

    /**
     * Get the song recognition app no longer installed error.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getSongRecognitionErrorNoApp(@NonNull final Context ctx,
                                                      @NonNull final SupportedLanguage sl) {
        return SaiyResourcesHelper.getStringResource(ctx, sl, R.string.error_song_recognition_chooser);
    }

    /**
     * Get the song recognition app no longer installed error.
     *
     * @param ctx     the application context
     * @param sl      the {@link SupportedLanguage}
     * @param appName the default provider
     * @return the required response
     */
    public static String getSongRecognitionErrorAppUninstalled(@NonNull final Context ctx,
                                                               @NonNull final SupportedLanguage sl,
                                                               @NonNull final String appName) {
        return String.format(SaiyResourcesHelper.getStringResource(ctx, sl,
                R.string.error_song_recognition_default_app), appName);
    }

    /**
     * Get the standard user name response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getUserNameRepeat(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_user_name_repeat);
        return UtilsString.stripNameSpace(stringArray[new Random().nextInt(stringArray.length)]);
    }

    /**
     * Get the standard user name error response, adding the user's name if defined.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getUserNameError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_user_name_error);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the standard Wolfram Alpha error response, adding the user's name if defined.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getWolframAlphaError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                R.array.array_error_wolfram_alpha_unknown);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the Wolfram Alpha response intro.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getWolframAlphaIntro(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_wolfram_alpha_intro);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Get the standard no memory response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getNoMemory(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] responseArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_no_memory);
        final String[] extraArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_no_memory_extra);
        final String[] extraUnknownArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_no_memory_extra_unknown);

        String extra = extraArray[new Random().nextInt(extraArray.length)];

        if (extra.matches(ctx.getString(R.string.memory_extra_facebook)) &&
                !Installed.isPackageInstalled(ctx, Installed.PACKAGE_FACEBOOK)) {
            extra = extraUnknownArray[new Random().nextInt(extraUnknownArray.length)];
        } else if (extra.matches(ctx.getString(R.string.memory_extra_twitter)) &&
                !Installed.isPackageInstalled(ctx, Installed.PACKAGE_TWITTER)) {
            extra = extraUnknownArray[new Random().nextInt(extraUnknownArray.length)];
        } else if (extra.matches(ctx.getString(R.string.memory_extra_tinder)) &&
                !Installed.isPackageInstalled(ctx, Installed.PACKAGE_TINDER)) {
            extra = extraUnknownArray[new Random().nextInt(extraUnknownArray.length)];
        } else if (extra.matches(ctx.getString(R.string.memory_extra_whatsapp)) &&
                !Installed.isPackageInstalled(ctx, Installed.PACKAGE_WHATSAPP)) {
            extra = extraUnknownArray[new Random().nextInt(extraUnknownArray.length)];
        } else if (extra.matches(ctx.getString(R.string.memory_extra_snapchat)) &&
                !Installed.isPackageInstalled(ctx, Installed.PACKAGE_SNAPCHAT)) {
            extra = extraUnknownArray[new Random().nextInt(extraUnknownArray.length)];
        }

        return UtilsString.stripNameSpace(String.format(responseArray[new Random().nextInt(responseArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx), extra));
    }

    /**
     * Get the standard clipboard response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getClipboardSpell(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_clipboard_copy);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Get the standard clipboard error data response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getClipboardDataError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_clipboard_data);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Get the standard clipboard error access response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getClipboardAccessError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_clipboard_access);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Get the standard spell error response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getSpellError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_spell);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Get the vocal enrollment error response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getEnrollmentError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_enrollment);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    public static String getApplicationListError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_application_list);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    public static String getApplicationUnknownError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_application_unknown);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    public static String getSettingsDisplayError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_settings_display);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    public static String getHoroscopeError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_horoscope);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    public static String getWeatherError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_error_weather);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the vocal id error response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getVocalIDError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                R.array.array_error_vocal_id);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the vocal id high response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getVocalIDHigh(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                R.array.array_vocal_id_high);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the vocal id medium response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getVocalIDMedium(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                R.array.array_vocal_id_medium);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the vocal id low response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getVocalIDLow(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_vocal_id_low);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    /**
     * Get the vocal enrollment error response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getEnrollmentAPIError(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl,
                R.array.array_error_enrollment_api);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    /**
     * Get the BV analysis complete response.
     *
     * @param ctx the application context
     * @param sl  the {@link SupportedLanguage}
     * @return the required response
     */
    public static String getBVAnalysisCompleteResponse(@NonNull final Context ctx, @NonNull final SupportedLanguage sl) {
        final String[] stringArray = SaiyResourcesHelper.getArrayResource(ctx, sl, R.array.array_bv_analysis_complete);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(ctx)));
    }

    public static String getAudioConfirm(Context context, SupportedLanguage supportedLanguage, String str) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_audio_confirm);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], str);
    }

    public static String getAudioError(Context context, SupportedLanguage supportedLanguage, String str) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_audio);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(context), str));
    }

    public static String getAudioAlready(Context context, SupportedLanguage supportedLanguage, String str) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_audio_already);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], str);
    }

    public static String getAudioUnknownError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_audio_unknown);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getMusicUnknownError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_music_unknown);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getAlexaUnreachable(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_alexa_unreachable);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getAlexaVolumeResponse(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_alexa_volume_insert);
        return String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.alexa_volume_response), stringArray[new Random().nextInt(stringArray.length)]);
    }

    public static String getNoContactForSms(Context context, SupportedLanguage supportedLanguage, String contactName) {
        return String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.sms_error_3), contactName);
    }

    public static String getTwitterConfirmation(Context context, SupportedLanguage supportedLanguage, String str) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_twitter_confirmation);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], str);
    }

    public static String getCallConfirmation(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_call_confirmation);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String getCallConfirmationRepeat(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_call_confirmation_repeat);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getCallConfirmationMisHeard(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_call_confirmation_misheard);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getTextConfirmationMisHeard(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_text_confirmation_misheard);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getContactNotDetectedError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_contact_not_detected);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getContactNotDetectedExtra(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_contact_not_detected_extra);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String getMessageProofReadAcknowledge(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_message_proof_read_acknowledge);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String getEmailProofReadVerbose(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_email_proof_read_verbose);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String getNavigationDestinationError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_navigation_destination);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getHardwareUnknownError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_hardware_unknown);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getHardwareUnsupportedError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_hardware_unsupported);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getHardwareCameraError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_hardware_camera);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)],
                PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getConnectionEnabled(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_connection_enabled);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String ConnectionDisabled(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_connection_disabled);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String getHardwareToggle(Context context, SupportedLanguage supportedLanguage, String str, String str2) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_hardware_toggle);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], str, str2);
    }

    public static String getCalculateWolframAlpha(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_calculate_wolfram_alpha);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getAddressUnknownError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_address_unknown);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getLocationAccessError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_location_access);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getLocationUnknownError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_location_unknown);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getLocateVehicle(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_locate_vehicle);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getParkedVehicle(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_parked_vehicle);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getForegroundUnknownError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_foreground_unknown);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getForegroundSaiy(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_foreground_saiy);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String getCalendarAccessError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_calendar_access);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getCalendarEventsError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_calendar_events);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getSomersault(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_somersault);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String getSomersaultError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_somersault);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String getOrientationError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_orientation);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getUnknownOrientationError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_unknown_orientation);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getRedialError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_redial);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getMissedCallError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_missed_call);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getSearchError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_search);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String getSearchUnknownError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_search_unknown);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getDisplayedApplicationSettings(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_displayed_application_settings);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String getUnknownSettingsError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_unknown_settings);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getDefineError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_define);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getDefineUnknownError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_define_unknown);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getApplicationMatchError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_application_match);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getRunningApplicationMatchError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_application_running_match);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getNoAppError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_no_app);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getGenericAcknowledgement(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_generic_acknowledgement);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String getNoPermissionForSms(Context context, SupportedLanguage supportedLanguage) {
        return SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.sms_error_7);
    }

    public static String getSmsConfirmation(Context context, SupportedLanguage supportedLanguage, String contactName, String generic) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_sms_confirmation);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], contactName, generic);
    }

    public static String FacebookConfirmationMisheard(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_facebook_confirmation_misheard);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getFacebookAcknowledge(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_facebook_acknowledge);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String getFacebookPostError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_facebook_post_error);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getFacebookVerbose(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_facebook_verbose);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String getTwitterConfirmationMisheard(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_twitter_confirmation_misheard);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getTwitterAcknowledge(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_twitter_acknowledge);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context));
    }

    public static String getTwitterPostError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_twitter_post_error);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getFoursquareNearbyError(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_foursquare_nearby_error);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], PersonalityHelper.getUserNameOrNot(context)));
    }

    public static String getEmailConfirmation(Context context, SupportedLanguage supportedLanguage, String contactName, String generic) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_email_confirmation);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], contactName, generic);
    }

    public static String getMessageContentRequest(Context context, SupportedLanguage supportedLanguage, String contactName) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_message_content_request);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], contactName));
    }

    public static String getContactResponsePart2(Context context, SupportedLanguage supportedLanguage, String date, String time) {
        return String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.sms_contact_response_part_2a), date, time);
    }

    public static String getMessageSentConfirmation(Context context, SupportedLanguage supportedLanguage, String contactName) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_message_sent_confirmation);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], contactName);
    }

    public static String getDefaultAppFailed(Context context, SupportedLanguage supportedLanguage, String str) {
        return UtilsString.stripNameSpace(String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.error_default_app_failed), PersonalityHelper.getUserNameOrNot(context), str));
    }

    public static String getSearchConfirm(Context context, SupportedLanguage supportedLanguage, String engineName, String str) {
        return String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.search_confirm), engineName, str);
    }

    public static String getContactMissingData(Context context, SupportedLanguage supportedLanguage, String contactName) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_contact_missing_data);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], contactName));
    }

    public static String getSmsResponsePart1(Context context, SupportedLanguage supportedLanguage, String date, String time) {
        return String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.sms_response_part_1a), date, time);
    }

    public static String getDisplayedSettings(Context context, SupportedLanguage supportedLanguage, String settingsName) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_displayed_settings);
        return String.format(stringArray[new Random().nextInt(stringArray.length)], settingsName);
    }

    public static String getNavigationInstallError(Context context, SupportedLanguage supportedLanguage) {
        return SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.error_navigation_install);
    }

    public static String getDisplaySettingsError(Context context, SupportedLanguage supportedLanguage, String settingsName) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_error_display_settings);
        return UtilsString.stripNameSpace(String.format(stringArray[new Random().nextInt(stringArray.length)], settingsName));
    }

    public static String getSkypeInstallError(Context context, SupportedLanguage supportedLanguage) {
        return SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.error_skype_install);
    }

    public static String getCallingNumberError(Context context, SupportedLanguage supportedLanguage, String action) {
        return UtilsString.stripNameSpace(String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.error_calling_number), action));
    }

    public static String getSearchSuggestInstall(Context context, SupportedLanguage supportedLanguage, String str) {
        return String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.search_suggest_install), str);
    }

    public static String getSearchRequireInstall(Context context, SupportedLanguage supportedLanguage, String str) {
        return String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.search_require_install), str);
    }

    public static String getAlexaIntro(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_alexa_intro);
        return stringArray[new Random().nextInt(stringArray.length)];
    }

    public static String getAlexaVolumeInsert(Context context, SupportedLanguage supportedLanguage) {
        String[] stringArray = SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_alexa_volume_insert);
        return String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.alexa_volume_response), stringArray[new Random().nextInt(stringArray.length)]);
    }

    public static String getSmsContactResponsePart1(Context context, SupportedLanguage supportedLanguage, String address) {
        return String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.sms_contact_response_part_1), address);
    }

    public static String getNoRecordForSms(Context context, SupportedLanguage supportedLanguage, String contactName) {
        return String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.sms_error_2), contactName);
    }
}