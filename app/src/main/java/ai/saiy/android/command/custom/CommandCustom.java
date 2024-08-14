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

package ai.saiy.android.command.custom;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.net.URISyntaxException;
import java.util.Set;

import ai.saiy.android.api.remote.Request;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.command.http.CustomHttp;
import ai.saiy.android.command.http.HttpsProcessor;
import ai.saiy.android.custom.CCC;
import ai.saiy.android.custom.CustomCommand;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

/**
 * Resolve the user's {@link CustomCommand} and action the required outcome.
 * <p>
 * Created by benrandall76@gmail.com on 22/04/2016.
 */
public class CommandCustom {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandCustom.class.getSimpleName();

    private static final int CUSTOM_COMMAND_VERBOSE_LIMIT = 3;

    /**
     * Action the custom command request and return the {@link Outcome}
     *
     * @param ctx           the application context
     * @param customCommand the identified {@link CustomCommand}
     * @param sl            the {@link SupportedLanguage}
     * @return the created {@link Outcome}
     */
    public @NonNull Outcome getResponse(@NonNull final Context ctx, @NonNull final CustomCommand customCommand,
                               @NonNull final SupportedLanguage sl, @NonNull final CommandRequest cr) {

        final long then = System.nanoTime();

        if (DEBUG) {
            MyLog.i(CLS_NAME, "getCustomAction: " + customCommand.getCustomAction().name());
            MyLog.i(CLS_NAME, "isExactMatch: " + customCommand.isExactMatch());
            MyLog.i(CLS_NAME, "getAlgorithm: " + customCommand.getAlgorithm().name());
            MyLog.i(CLS_NAME, "getKeyphrase: " + customCommand.getKeyphrase());
            MyLog.i(CLS_NAME, "getResponseError: " + customCommand.getResponseError());
            MyLog.i(CLS_NAME, "getResponseSuccess: " + customCommand.getResponseSuccess());
            MyLog.i(CLS_NAME, "getTTSLocale: " + customCommand.getTTSLocale());
            MyLog.i(CLS_NAME, "getVRLocale: " + customCommand.getVRLocale());
            MyLog.i(CLS_NAME, "getAction: " + customCommand.getAction());
            MyLog.i(CLS_NAME, "getScore: " + customCommand.getScore());
            MyLog.i(CLS_NAME, "getCommandConstant: " + customCommand.getCommandConstant().name());
            MyLog.i(CLS_NAME, "getIntent: " + customCommand.getIntent());
            MyLog.i(CLS_NAME, "getExtraText: " + customCommand.getExtraText());

            if (customCommand.getAlgorithm() != null) {
                MyLog.i(CLS_NAME, "getAlgorithm: " + customCommand.getAlgorithm().name());
            }
        }

        final Outcome outcome = new Outcome();

        switch (customCommand.getCustomAction()) {

            case CUSTOM_SPEECH:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, CCC.CUSTOM_SPEECH.name());
                }

                outcome.setUtterance(customCommand.getResponseSuccess());
                outcome.setAction(customCommand.getAction());
                outcome.setOutcome(Outcome.SUCCESS);

                break;
            case CUSTOM_DISPLAY_CONTACT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, CCC.CUSTOM_DISPLAY_CONTACT.name());
                }
                break;
            case CUSTOM_TASKER_TASK:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, CCC.CUSTOM_TASKER_TASK.name());
                }
                break;
            case CUSTOM_ACTIVITY:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, CCC.CUSTOM_ACTIVITY.name());
                }

                Intent intent = null;

                try {

                    intent = Intent.parseUri(customCommand.getIntent(), 0);

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Intent:" + intent.toUri(0));
                        examineIntent(intent);
                    }

                } catch (final URISyntaxException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "Intent.parseUri: URISyntaxException, " + e.getMessage());
                    }
                }

                if (intent != null && ExecuteIntent.executeIntent(ctx, intent)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Execute remoteIntent success");
                    }
                    outcome.setUtterance(UtilsString.notNaked(customCommand.getResponseSuccess())
                            ? customCommand.getResponseSuccess() : SaiyRequestParams.SILENCE);
                    outcome.setAction(customCommand.getAction());
                    outcome.setOutcome(Outcome.SUCCESS);
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Execute remoteIntent failed");
                    }
                    outcome.setUtterance(UtilsString.notNaked(customCommand.getResponseError())
                            ? customCommand.getResponseError() : SaiyRequestParams.SILENCE);
                    outcome.setAction(customCommand.getAction());
                    outcome.setOutcome(Outcome.FAILURE);
                }

                break;
            case CUSTOM_CALL_CONTACT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, CCC.CUSTOM_CALL_CONTACT.name());
                }

                break;
            case CUSTOM_LAUNCH_APPLICATION:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, CCC.CUSTOM_LAUNCH_APPLICATION.name());
                }

                break;
            case CUSTOM_LAUNCH_SHORTCUT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, CCC.CUSTOM_LAUNCH_SHORTCUT.name());
                }
                break;
            case CUSTOM_SEARCHABLE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, CCC.CUSTOM_SEARCHABLE.name());
                }
                break;
            case CUSTOM_INTENT_SERVICE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, CCC.CUSTOM_INTENT_SERVICE.name());
                }

                Intent remoteIntent = null;

                try {

                    remoteIntent = Intent.parseUri(customCommand.getIntent(), 0);

                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "remoteIntent:" + remoteIntent.toUri(0));
                        examineIntent(remoteIntent);
                    }

                } catch (final URISyntaxException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "remoteIntent.parseUri: URISyntaxException, " + e.getMessage());
                    }
                } catch (final NullPointerException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "remoteIntent.parseUri: NullPointerException, " + e.getMessage());
                    }
                }

                if (remoteIntent != null) {

                    final Pair<Boolean, String> pair = UtilsApplication.getAppNameFromPackage(ctx, remoteIntent.getPackage());

                    if (pair.first) {

                        Bundle bundle = remoteIntent.getExtras();

                        if (bundle == null) {
                            bundle = new Bundle();
                        }

                        bundle.putStringArrayList(Request.RESULTS_RECOGNITION, cr.getResultsArray());
                        bundle.putFloatArray(Request.CONFIDENCE_SCORES, cr.getConfidenceArray());
                        remoteIntent.putExtras(bundle);

                        final String appName = pair.second;

                        if (ExecuteIntent.startService(ctx, remoteIntent)) {

                            final String verboseWords;

                            if (SPH.getRemoteCommandVerbose(ctx) >= CUSTOM_COMMAND_VERBOSE_LIMIT) {
                                verboseWords = SaiyRequestParams.SILENCE;
                            } else {
                                SPH.incrementRemoteCommandVerbose(ctx);
                                verboseWords = PersonalityResponse.getRemoteSuccess(ctx, sl, appName);
                            }

                            outcome.setUtterance(verboseWords);
                            outcome.setAction(customCommand.getAction());
                            outcome.setOutcome(Outcome.SUCCESS);

                        } else {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "Execute remoteIntent failed");
                            }
                            outcome.setUtterance(PersonalityResponse.getErrorRemoteFailed(ctx, sl, appName));
                            outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                            outcome.setOutcome(Outcome.FAILURE);
                        }

                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "remoteIntent package name unknown");
                        }

                        outcome.setUtterance(PersonalityResponse.getErrorRemoteFailedUnknown(ctx, sl));
                        outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                        outcome.setOutcome(Outcome.FAILURE);
                    }

                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "remoteIntent failed");
                    }
                    outcome.setUtterance(PersonalityResponse.getErrorRemoteFailedUnknown(ctx, sl));
                    outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                    outcome.setOutcome(Outcome.FAILURE);
                }

                break;
            case CUSTOM_SEND_INTENT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, CCC.CUSTOM_SEND_INTENT.name());
                }
                final ai.saiy.android.command.intent.CustomIntent customIntent = new GsonBuilder().disableHtmlEscaping().create().fromJson(customCommand.getExtraText(),
                        new TypeToken<ai.saiy.android.command.intent.CustomIntent>() {}.getType());
                final Intent httpIntent = UtilsString.notNaked(customIntent.getAction())? new Intent(customIntent.getAction()) : new Intent();
                if (UtilsString.notNaked(customIntent.getPackageName())) {
                    httpIntent.setPackage(customIntent.getPackageName());
                    if (UtilsString.notNaked(customIntent.getClassName())) {
                        httpIntent.setComponent(new ComponentName(customIntent.getPackageName(), customIntent.getClassName()));
                    }
                }
                if (UtilsString.notNaked(customIntent.getCategory())) {
                    httpIntent.setPackage(customIntent.getCategory());
                }
                if (UtilsString.notNaked(customIntent.getMimeType())) {
                    httpIntent.setType(customIntent.getMimeType());
                }
                if (UtilsString.notNaked(customIntent.getData())) {
                    try {
                        httpIntent.setData(android.net.Uri.parse(customIntent.getData()));
                    } catch (Exception e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "Execute executeCustomIntent failed, " + e.getMessage());
                        }
                    }
                }
                if (UtilsString.notNaked(customIntent.getExtras())) {
                    final Bundle bundle = ai.saiy.android.utils.UtilsBundle.stringExtra(customIntent.getExtras());
                    if (!bundle.isEmpty()) {
                        httpIntent.putExtras(bundle);
                    }
                    if (httpIntent.getExtras() != null) {
                        examineIntent(httpIntent);
                    }
                }
                if (ai.saiy.android.intent.ExecuteIntent.executeCustomIntent(ctx, httpIntent, customIntent.getTarget())) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Execute executeCustomIntent success");
                    }
                    outcome.setUtterance(UtilsString.notNaked(customCommand.getResponseSuccess())
                            ? customCommand.getResponseSuccess() : SaiyRequestParams.SILENCE);
                    outcome.setOutcome(Outcome.SUCCESS);
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Execute executeCustomIntent failed");
                    }
                    outcome.setUtterance(UtilsString.notNaked(customCommand.getResponseError())
                            ? customCommand.getResponseError() : SaiyRequestParams.SILENCE);
                    outcome.setOutcome(Outcome.FAILURE);
                }
                outcome.setAction(customCommand.getAction());
                break;
            case CUSTOM_HTTP:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, CCC.CUSTOM_HTTP.name());
                }
                final CustomHttp customHttp = new GsonBuilder().disableHtmlEscaping().create().fromJson(customCommand.getExtraText(),
                        new TypeToken<CustomHttp>() {}.getType());
                final HttpsProcessor httpsProcessor = new HttpsProcessor();
                final android.util.Pair<Boolean, Object> processedPair = httpsProcessor.process(customHttp);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "CUSTOM_HTTP success: " + processedPair.first);
                }
                if (processedPair.first) {
                    outcome.setOutcome(Outcome.SUCCESS);
                    switch (customHttp.getSuccessHandling()) {
                        case CustomHttp.SUCCESS_SPEAK:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "SUCCESS_SPEAK");
                            }
                            outcome.setUtterance(UtilsString.notNaked(customCommand.getResponseSuccess())
                                    ? customCommand.getResponseSuccess() : SaiyRequestParams.SILENCE);
                            break;
                        case CustomHttp.SUCCESS_SPEAK_LISTEN:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "SUCCESS_SPEAK_LISTEN");
                            }
                            outcome.setUtterance(UtilsString.notNaked(customCommand.getResponseSuccess())
                                    ? customCommand.getResponseSuccess() : SaiyRequestParams.SILENCE);
                            break;
                        case CustomHttp.SUCCESS_SPEAK_OUTPUT:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "SUCCESS_SPEAK_OUTPUT have output: " + (processedPair.second != null));
                            }
                            outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "SUCCESS_SPEAK_OUTPUT instanceof String: " + (processedPair.second instanceof String));
                            }
                            outcome.setUtterance((processedPair.second instanceof String)? (String) processedPair.second : SaiyRequestParams.SILENCE);
                            break;
                        case CustomHttp.SUCCESS_SPEAK_LISTEN_OUTPUT:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "SUCCESS_SPEAK_LISTEN_OUTPUT have output: " + (processedPair.second != null));
                            }
                            outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "SUCCESS_SPEAK_LISTEN_OUTPUT instanceof String: " + (processedPair.second instanceof String));
                            }
                            outcome.setUtterance((processedPair.second instanceof String)? (String) processedPair.second : SaiyRequestParams.SILENCE);
                            break;
                        default:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "SUCCESS_NONE");
                            }
                            break;
                    }
                } else {
                    outcome.setOutcome(Outcome.FAILURE);
                    switch (customHttp.getErrorHandling()) {
                        case CustomHttp.ERROR_SPEAK:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "ERROR_SPEAK");
                            }
                            outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                            outcome.setUtterance(UtilsString.notNaked(customCommand.getResponseError())
                                    ? customCommand.getResponseError() : SaiyRequestParams.SILENCE);
                            break;
                        case CustomHttp.ERROR_SPEAK_LISTEN:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "ERROR_SPEAK_LISTEN");
                            }
                            outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                            outcome.setUtterance(UtilsString.notNaked(customCommand.getResponseError())
                                    ? customCommand.getResponseError() : SaiyRequestParams.SILENCE);
                            break;
                        case CustomHttp.ERROR_SPEAK_OUTPUT:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "ERROR_SPEAK_OUTPUT have error stream: " + (processedPair.second != null));
                            }
                            outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "ERROR_SPEAK_OUTPUT instanceof String: " + (processedPair.second instanceof String));
                            }
                            outcome.setUtterance((processedPair.second instanceof String)? (String) processedPair.second : SaiyRequestParams.SILENCE);
                            break;
                        case CustomHttp.ERROR_SPEAK_LISTEN_OUTPUT:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "ERROR_SPEAK_LISTEN_OUTPUT have error stream: " + (processedPair.second != null));
                            }
                            outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "ERROR_SPEAK_LISTEN_OUTPUT instanceof String: " + (processedPair.second instanceof String));
                            }
                            outcome.setUtterance((processedPair.second instanceof String)? (String) processedPair.second : SaiyRequestParams.SILENCE);
                            break;
                        default:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "ERROR_NONE");
                            }
                            outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
                            outcome.setUtterance(SaiyRequestParams.SILENCE);
                            break;
                    }
                }

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "CUSTOM_HTTP: isTasker: " + customHttp.isTasker());
                }
                if (customHttp.isTasker()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "CUSTOM_HTTP getTaskerTaskName: " + customHttp.getTaskerTaskName());
                        MyLog.i(CLS_NAME, "CUSTOM_HTTP getTaskerVariableName: " + customHttp.getTaskerVariableName());
                    }
                    if (processedPair.second instanceof String) {
                        final boolean result = ai.saiy.android.thirdparty.tasker.TaskerHelper.broadcastVariableValue(ctx, customHttp.getTaskerTaskName(), customHttp.getTaskerVariableName(), (String) processedPair.second);
                        if (result) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "CUSTOM_HTTP: update tasker success");
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "CUSTOM_HTTP: update tasker error");
                            }
                        }
                    }
                }
                break;
            case CUSTOM_AUTOMATE_FLOW:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, CCC.CUSTOM_AUTOMATE_FLOW.name());
                }
                final boolean result = ai.saiy.android.intent.ExecuteIntent.launchShortcut(ctx, customCommand.getExtraText());
                outcome.setOutcome(result? Outcome.SUCCESS : Outcome.FAILURE);
                if (result) {
                    outcome.setUtterance(UtilsString.notNaked(customCommand.getResponseSuccess())
                            ? customCommand.getResponseSuccess() : SaiyRequestParams.SILENCE);
                } else {
                    outcome.setUtterance(UtilsString.notNaked(customCommand.getResponseError())
                            ? customCommand.getResponseError() : SaiyRequestParams.SILENCE);
                }
                outcome.setAction(customCommand.getAction());
                break;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "DEFAULT");
                }
                break;
        }

        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }

        return outcome;
    }

    /**
     * For debugging the intent extras
     *
     * @param intent containing potential extras
     */
    private void examineIntent(@NonNull final Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "examineIntent");
        }

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
