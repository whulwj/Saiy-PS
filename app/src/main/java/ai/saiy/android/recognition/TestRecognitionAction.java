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

package ai.saiy.android.recognition;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.custom.CustomCommandHelper;
import ai.saiy.android.database.DBSpeech;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Quantum;
import ai.saiy.android.ui.viewmodel.ViewModelBilling;
import ai.saiy.android.user.BillingReporter;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsToast;
import ai.saiy.android.utils.debug.DebugAction;

/**
 * Class that handles command from direct text input.
 * <p/>
 * Created by benrandall76@gmail.com on 09/02/2016.
 */
public class TestRecognitionAction {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = TestRecognitionAction.class.getSimpleName();

    /**
     * Constructor.
     * <p>
     * Handle the test command text input by the user. This can either be an attempt to test a command, or
     * an instruction to perform debugging of some sort.
     *
     * @param ctx              the application context
     * @param viewModelBilling the {@link ViewModelBilling}
     * @param commandText      the command text to test
     */
    public TestRecognitionAction(@NonNull final Context ctx, ViewModelBilling viewModelBilling, @NonNull final String commandText) {

        if (!commandText.startsWith(MyLog.DO_DEBUG)) {

            final Locale vrLocale = SPH.getVRLocale(ctx);

            final ArrayList<String> mocCommands = new ArrayList<>(1);
            mocCommands.add(commandText);

            final float[] mocConfidence = new float[1];
            mocConfidence[0] = 1F;

            final CommandRequest cr = new CommandRequest(vrLocale, SPH.getTTSLocale(ctx),
                    SupportedLanguage.getSupportedLanguage(vrLocale));
            cr.setResultsArray(mocCommands);
            cr.setConfidenceArray(mocConfidence);

            new Quantum(ctx).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cr);
        } else {
            runDebug(ctx, viewModelBilling, commandText);
        }
    }

    /**
     * The command text was a debugging instruction, handle here
     *
     * @param ctx              the application context
     * @param viewModelBilling the {@link ViewModelBilling}
     * @param commandText      the command text to test
     */
    private void runDebug(@NonNull final Context ctx, ViewModelBilling viewModelBilling, @NonNull final String commandText) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "runDebug: " + commandText);
        }

        final String[] instructionArray = commandText.split(MyLog.DO_DEBUG);

        if (instructionArray.length > 1) {

            try {

                final int action = Integer.parseInt(instructionArray[1].trim());

                switch (action) {
                    case DebugAction.DEBUG_VALIDATE_SIGNATURE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "runDebug: DEBUG_VALIDATE_SIGNATURE");
                        }
                        toast(ctx, DebugAction.validateSignatures(ctx) ? ctx.getString(R.string.success)
                                : ctx.getString(R.string.failed));
                        break;
                    case DebugAction.DEBUG_CLEAR_SYNTHESIS:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "runDebug: DEBUG_CLEAR_SYNTHESIS");
                        }

                        final DBSpeech speech = new DBSpeech(ctx);
                        speech.deleteTable();
                        toast(ctx, ctx.getString(R.string.success));
                        break;
                    case DebugAction.DEBUG_CLEAR_CUSTOM_COMMANDS:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "runDebug: DEBUG_CLEAR_CUSTOM_COMMANDS");
                        }
                        toast(ctx, CustomCommandHelper.deleteAllCommands(ctx) ? ctx.getString(R.string.success)
                                : ctx.getString(R.string.failed));
                        break;
                    case DebugAction.DEBUG_AUTHENTICATION:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "runDebug: DEBUG_AUTHENTICATION");
                        }
                        toast(ctx, "IAP: " + ai.saiy.android.utils.SPH.getPremiumContentVerbose(ctx) + " - Auth: " + (ai.saiy.android.utils.SPH.getFirebaseAnonymousUid(ctx) != null));
                        break;
                    case DebugAction.REPORT_BILLING:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "runDebug: REPORT_BILLING");
                        }
                        if (viewModelBilling.canBillingProceed()) {
                            toast(ctx, "Billing test running");
                            new BillingReporter(ctx, viewModelBilling.getBillingClient()).report();
                        } else {
                            toast(ctx, "Billing Client is in error");
                        }
                        break;
                    case DebugAction.DEBUG_BILLING:
                        if (ai.saiy.android.utils.SPH.getDebugBilling(ctx)) {
                            ai.saiy.android.utils.SPH.setDebugBilling(ctx, false);
                            toast(ctx, ctx.getString(R.string.disabled));
                        } else {
                            ai.saiy.android.utils.SPH.setDebugBilling(ctx, true);
                            toast(ctx, ctx.getString(R.string.enabled));
                        }
                        break;
                    default:
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "runDebug: default");
                        }
                        toast(ctx, ctx.getString(R.string.error) + " " + action);
                        break;
                }

            } catch (final NumberFormatException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "runDebug NumberFormatException");
                    e.printStackTrace();
                }
                toast(ctx, ctx.getString(R.string.error));
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "runDebug NullPointerException");
                    e.printStackTrace();
                }
                toast(ctx, ctx.getString(R.string.error));
            } catch (final IndexOutOfBoundsException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "runDebug IndexOutOfBoundsException");
                    e.printStackTrace();
                }
                toast(ctx, ctx.getString(R.string.error));
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "runDebug Exception");
                    e.printStackTrace();
                }
                toast(ctx, ctx.getString(R.string.error));
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "runDebug: couldn't extract debug constant");
            }
            toast(ctx, ctx.getString(R.string.error));
        }
    }

    /**
     * Toast the outcome of a debuggable action
     *
     * @param ctx        the application context
     * @param toastWords the words to toast
     */
    private void toast(@NonNull final Context ctx, @NonNull final String toastWords) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                UtilsToast.showToast(ctx, toastWords, Toast.LENGTH_SHORT);
            }
        });
    }
}
