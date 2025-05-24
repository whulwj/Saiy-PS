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
import android.provider.ContactsContract;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CommandRequest;
import ai.saiy.android.contacts.Contact;
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
        if (commandText.startsWith("DEBUG:" + DebugAction.DEBUG_CONTACT)) {
            debugContactId(ctx, commandText);
            return;
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
                    case DebugAction.DEBUG_CLEAR_BLOCKED_APPS:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "runDebug: DEBUG_CLEAR_BLOCKED_APPS");
                        }
                        ai.saiy.android.utils.SPH.setBlockedNotificationApplications(ctx, null);
                        toast(ctx, ctx.getString(R.string.success));
                        break;
                    case DebugAction.DEBUG_AUTHENTICATION:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "runDebug: DEBUG_AUTHENTICATION");
                        }
                        toast(ctx, "IAP: " + ai.saiy.android.utils.SPH.getPremiumContentVerbose(ctx) + " - Auth: " + (ai.saiy.android.utils.SPH.getFirebaseAnonymousUid(ctx) != null));
                        break;
                    case DebugAction.DEBUG_VOLUME:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "runDebug: DEBUG_VOLUME");
                        }
                        if (ai.saiy.android.utils.SPH.getToastDebug(ctx)) {
                            ai.saiy.android.utils.SPH.setToastDebug(ctx, false);
                            toast(ctx, "Volume debugging disabled");
                        } else {
                            ai.saiy.android.utils.SPH.setToastDebug(ctx, true);
                            toast(ctx, "Volume debugging enabled");
                        }
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

    private void debugContactId(@NonNull final Context ctx, @NonNull String str) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "debugContactId");
        }
        String sms;
        String contactIDs = null;
        if (ai.saiy.android.permissions.PermissionHelper.checkSMSReadPermissions(ctx, null) && ai.saiy.android.permissions.PermissionHelper.checkContactGroupPermissions(ctx, null)) {
            String contactName = str.replaceFirst("DEBUG:" + DebugAction.DEBUG_CONTACT + ":", "").trim();
            final ai.saiy.android.contacts.ContactHelper contactHelper = new ai.saiy.android.contacts.ContactHelper();
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(contactName);
            ArrayList<Contact> contacts = contactHelper.getContactFromName(ctx, Locale.getDefault(), arrayList, false);
            if (!ai.saiy.android.utils.UtilsList.notNaked(contacts)) {
                toast(ctx, "Could not find contact by name?");
                new ai.saiy.android.database.helper.DatabaseHelper().deleteContacts(ctx);
                return;
            }
            if (DEBUG) {
                for (Contact contact : contacts) {
                    MyLog.i(CLS_NAME, "contact name: " + contact.getName());
                    MyLog.i(CLS_NAME, "contact id: " + contact.getID());
                    MyLog.i(CLS_NAME, "contact has number: " + contact.hasPhoneNumber());
                }
            }
            Pair<Boolean, Contact> contactPair = contactHelper.getContact(ctx, contacts, Contact.Weighting.NUMBER, ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM);
            if (!(Boolean) contactPair.first) {
                toast(ctx, "Could not find contact number?");
                new ai.saiy.android.database.helper.DatabaseHelper().deleteContacts(ctx);
                return;
            }
            Contact contact = contactPair.second;
            if (!ai.saiy.android.utils.UtilsString.notNaked(contact.getNumber())) {
                toast(ctx, "Required number missing?");
                new ai.saiy.android.database.helper.DatabaseHelper().deleteContacts(ctx);
                return;
            }
            ArrayList<String> rawIdArray = contactHelper.getRawIdArray(ctx, contact.getID());
            if (!ai.saiy.android.utils.UtilsList.notNaked(rawIdArray)) {
                toast(ctx, "Failed to get raw ids?");
                new ai.saiy.android.database.helper.DatabaseHelper().deleteContacts(ctx);
                return;
            }
            String rawIdArrayString = rawIdArray.toString();
            String contactID = contactHelper.getIdFromNumber(ctx, contact.getNumber());
            if (!ai.saiy.android.utils.UtilsString.notNaked(contactID)) {
                toast(ctx, "Failed to get id from number?");
                new ai.saiy.android.database.helper.DatabaseHelper().deleteContacts(ctx);
                return;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "idFromNumberString: " + contactID);
            }
            for (Contact c : contacts) {
                contactIDs = contactIDs != null ? contactIDs + c.getID() + " : " : c.getID() + " : ";
            }
            final ai.saiy.android.command.sms.Message recentMessage = new ai.saiy.android.command.sms.SmsHelper().getMostRecentMessage(ctx);
            if (recentMessage == null) {
                sms = "message null";
            } else if (ai.saiy.android.utils.UtilsString.notNaked(recentMessage.getPerson())) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getPerson : " + recentMessage.getPerson());
                }
                sms = recentMessage.getPerson();
            } else {
                sms = "message person null";
            }
            ai.saiy.android.command.clipboard.ClipboardHelper.setClipboardContent(ctx, "Contact id collection: " + contactIDs + "\nRaw id collection: " + rawIdArrayString + "\nExtracted id: " + contactID + "\nLast SMS id: " + sms);
            toast(ctx, "Success! Results copied to clipboard");
        }
    }
}
