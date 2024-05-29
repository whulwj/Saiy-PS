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

package ai.saiy.android.ui.activity.helper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.IntentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.commons.lang3.StringUtils;

import ai.saiy.android.R;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.service.helper.SelfAwareHelper;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Created by benrandall76@gmail.com on 26/08/2016.
 */

public class ActivityHomeHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityHomeHelper.class.getSimpleName();
    private static final int REQUEST_CODE = 1;

    /**
     * Show the applications disclaimer
     *
     * @param act the Activity context in which to display the dialog
     */
    @SuppressWarnings("deprecation, ConstantConditions")
    public void showDisclaimer(@NonNull final Activity act) {

        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(act)
                .setTitle(R.string.menu_application_disclaimer)
                .setMessage(Html.fromHtml(act.getApplicationContext().getString(R.string.content_disclaimer)))
                .setIcon(R.drawable.ic_gavel)
                .setCancelable(false)
                .setPositiveButton(R.string.menu_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDisclaimer: onPositive");
                        }

                        SPH.setAcceptedDisclaimer(act.getApplicationContext());
                        dialog.dismiss();
                        ((ActivityHome) act).runStartConfiguration();
                    }
                })
                .setNegativeButton(R.string.menu_uninstall, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDisclaimer: onNegative");
                        }

                        SelfAwareHelper.stopService(act.getApplicationContext());
                        ExecuteIntent.uninstallApp(act.getApplicationContext(), act.getPackageName());
                        dialog.dismiss();
                        act.finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDisclaimer: onCancel");
                        }

                        SelfAwareHelper.stopService(act.getApplicationContext());
                        ExecuteIntent.uninstallApp(act.getApplicationContext(), act.getPackageName());
                        dialog.dismiss();
                        act.finish();
                    }
                }).create();

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    /**
     * Show the developer note
     *
     * @param act the Activity context in which to display the dialog
     */
    @SuppressWarnings("ConstantConditions")
    public void showDeveloperNote(@NonNull final Activity act) {

        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(act)
                .setTitle(R.string.menu_developer_note)
                .setMessage(R.string.content_developer_note)
                .setIcon(R.drawable.ic_note_text)
                .setPositiveButton(R.string.menu_lets_do_it, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDeveloperNote: onPositive");
                        }

                        SPH.setDeveloperNote(act.getApplicationContext());
                        dialog.dismiss();
                        ((ActivityHome) act).runStartConfiguration();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDeveloperNote: onCancel");
                        }

                        SPH.setDeveloperNote(act.getApplicationContext());
                        dialog.dismiss();
                        ((ActivityHome) act).runStartConfiguration();
                    }
                }).create();

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }

    /**
     * Show the what's new information
     *
     * @param act the Activity context in which to display the dialog
     */
    @SuppressWarnings("ConstantConditions")
    public void showWhatsNew(@NonNull final Activity act) {

        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(act)
                .setTitle(R.string.menu_whats_new)
                .setMessage(R.string.content_whats_new)
                .setIcon(R.drawable.ic_info)
                .setPositiveButton(R.string.menu_excited, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showWhatsNew: onPositive");
                        }

                        SPH.setWhatsNew(act.getApplicationContext());
                        dialog.dismiss();
                        ((ActivityHome) act).runStartConfiguration();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showWhatsNew: onCancel");
                        }

                        SPH.setWhatsNew(act.getApplicationContext());
                        dialog.dismiss();
                        ((ActivityHome) act).runStartConfiguration();
                    }
                }).create();

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    /**
     * Show the supported language selector
     *
     * @param act the Activity context in which to display the dialog
     */
    @SuppressWarnings("ConstantConditions")
    public void showLanguageSelector(@NonNull final Activity act) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final String[] languages = act.getResources().getStringArray(R.array.array_supported_languages);

                for (int i = 0; i < languages.length; i++) {
                    languages[i] = StringUtils.capitalize(languages[i]);
                }

                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(act)
                                .setCancelable(false)
                                .setTitle(R.string.menu_supported_languages)
                                .setSingleChoiceItems((CharSequence[]) languages, 0, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showLanguageSelector: onSelection: " + which + ": " + languages[which]);
                                        }
                                    }
                                })
                                .setMessage(R.string.content_supported_languages)
                                .setIcon(R.drawable.ic_language)
                                .setPositiveButton(R.string.menu_select, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showLanguageSelector: " + which);
                                        }

                                        dialog.dismiss();
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showLanguageSelector: onCancel");
                                        }

                                        dialog.dismiss();
                                    }
                                }).create();

                        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
                        materialDialog.show();
                    }
                });
            }
        });
    }

    public void checkAppRestrictionsStatus(Activity context) {
        final int appRestrictionsStatus = IntentCompat.getUnusedAppRestrictionsStatus(context);
        switch (appRestrictionsStatus) {
            // Status could not be fetched. Check logs for details.
            case IntentCompat.UNUSED_APP_RESTRICTION_STATUS_UNKNOWN:
                break;
            // Restrictions do not apply to your app on this device.
            case IntentCompat.UNUSED_APP_RESTRICTION_FEATURE_NOT_AVAILABLE:
                // Restrictions have been disabled by the user for your app.
            case IntentCompat.PERMISSION_REVOCATION_DISABLED:
                break;
            // If the user doesn't start your app for months, its permissions
            // will be revoked and/or it will be hibernated.
            case IntentCompat.APP_HIBERNATION_ENABLED:
            default:
                // If your app works primarily in the background, you can ask the user
                // to disable these restrictions. Check if you have already asked the
                // user to disable these restrictions. If not, you can show a message to
                // the user explaining why permission auto-reset and Hibernation should be
                // disabled. Tell them that they will now be redirected to a page where
                // they can disable these features.
                final Intent intent = IntentCompat.createManageUnusedAppRestrictionsIntent
                        (context, context.getPackageName());
                // Must use startActivityForResult(), not startActivity(), even if
                // you don't use the result code returned in onActivityResult().
                context.startActivityForResult(intent, REQUEST_CODE);
                break;
        }
    }
}
