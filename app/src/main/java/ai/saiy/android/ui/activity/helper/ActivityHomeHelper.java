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
import android.os.Build;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.IntentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

import ai.saiy.android.R;
import ai.saiy.android.applications.Install;
import ai.saiy.android.firebase.UserFirebaseListener;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.service.helper.SelfAwareHelper;
import ai.saiy.android.tts.helper.TTSDefaults;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.user.UserFirebaseHelper;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Created by benrandall76@gmail.com on 26/08/2016.
 */

public class ActivityHomeHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityHomeHelper.class.getSimpleName();
    private static final int REQUEST_CODE = 1;

    private boolean mIsNotificationPermissionRequested;

    public void showStartTutorialDialog(final Activity activity) {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.content_start_voice_tutorial)
                .setIcon(R.drawable.ic_text_to_speech)
                .setPositiveButton(R.string.menu_yes_please, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showStartTutorialDialog: onPositive");
                                }
                                ((ActivityHome) activity).startTutorial();
                            }
                        }
                )
                .setNegativeButton(R.string.menu_not_right_now, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showStartTutorialDialog: onNegative");
                                }
                            }
                        }
                )
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showStartTutorialDialog: onCancel");
                        }
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    public void showUserGuideDialog(ActivityHome activity) {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.menu_user_guide)
                .setIcon(R.drawable.ic_library)
                .setItems(activity.getResources().getStringArray(R.array.array_user_guide), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Global.isInVoiceTutorial()) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onClick: tutorialActive");
                            }
                            activity.toast(activity.getApplicationContext().getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
                            return;
                        }
                        switch (which) {
                            case 0:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_BASIC");
                                }
                                ExecuteIntent.webSearch(activity.getApplicationContext(), Constants.USER_GUIDE_BASIC);
                                break;
                            case 1:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_CUSTOM_COMMANDS");
                                }
                                ExecuteIntent.webSearch(activity.getApplicationContext(), Constants.USER_CUSTOM_COMMANDS);
                                break;
                            case 2:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_CUSTOM_REPLACEMENTS");
                                }
                                ExecuteIntent.webSearch(activity.getApplicationContext(), Constants.USER_CUSTOM_REPLACEMENTS);
                                break;
                            case 3:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_SOUND_EFFECTS");
                                }
                                ExecuteIntent.webSearch(activity.getApplicationContext(), Constants.USER_SOUND_EFFECTS);
                                break;
                            case 4:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_TASKER");
                                }
                                ExecuteIntent.webSearch(activity.getApplicationContext(), Constants.USER_TASKER);
                                break;
                            case 5:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_TROUBLESHOOTING");
                                }
                                ExecuteIntent.webSearch(activity.getApplicationContext(), Constants.USER_TROUBLESHOOTING);
                                break;
                            case 6:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_COMING_SOON");
                                }
                                ExecuteIntent.webSearch(activity.getApplicationContext(), Constants.USER_COMING_SOON);
                                break;
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showUserGuideDialog: onNegative");
                        }
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }


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
        setUpRateMe(act.getApplicationContext());
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
                        ((ActivityHome) act).runStartConfiguration();
                    }
                }).create();

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    private void setUpRateMe(Context context) {
        final boolean isNewUser = SPH.isNewUser(context);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setUpRateMe: newUser: " + isNewUser);
        }
        if (isNewUser) {
            SPH.markOldUser(context);
        } else {
            SPH.setRateMe(context, SPH.getUsedIncrement(context) + 101);
        }
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
                                .setSingleChoiceItems(languages, 0, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (0 != which) {
                                            if (dialog instanceof AlertDialog) {
                                                ((AlertDialog) dialog).getListView().setItemChecked(0, true);
                                            }
                                        } else if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showLanguageSelector: onSelection: " + which + ": " + languages[which]);
                                        }
                                    }
                                })
                                .setIcon(R.drawable.ic_language)
                                .setPositiveButton(R.string.menu_select, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showLanguageSelector: " + which);
                                        }
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showLanguageSelector: onCancel");
                                        }
                                    }
                                }).create();

                        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
                        materialDialog.show();

                        materialDialog.getListView().setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                            @Override
                            public void onChildViewAdded(View parent, View child) {
                                if (child instanceof CheckedTextView) {
                                    final int itemIndex = Arrays.asList(languages).indexOf(((TextView) child).getText().toString());
                                    child.setEnabled(itemIndex == 0);
                                } else if (DEBUG) {
                                    MyLog.d(CLS_NAME, "onChildViewAdded: " + child.getClass().getSimpleName());
                                }
                            }

                            @Override
                            public void onChildViewRemoved(View parent, View child) {
                            }
                        });
                    }
                });
            }
        });
    }

    public void showNoTTSDialog(final Activity activity) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showNoTTSDialog");
        }
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.menu_tts)
                .setMessage(R.string.issue_tts_engine_text)
                .setIcon(R.drawable.ic_text_to_speech)
                .setPositiveButton(R.string.title_install, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showNoTTSDialog: onPositive");
                        }
                        Install.showInstallLink(activity.getApplicationContext(), TTSDefaults.TTS_PKG_NAME_GOOGLE);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showNoTTSDialog: onNegative");
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showNoTTSDialog: onCancel");
                        }
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }

    public void showBillingErrorDialog(@NonNull ActivityHome activity) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showBillingErrorDialog");
        }
        if (activity.isActive()) {
            activity.showProgress(false);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final AlertDialog materialDialog = new MaterialAlertDialogBuilder(activity)
                            .setTitle(R.string.menu_billing_error)
                            .setMessage(activity.getString(R.string.content_billing_error, Constants.SAIY_BILLING_EMAIL))
                            .setIcon(R.drawable.ic_security)
                            .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showBillingErrorDialog: onPositive");
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showBillingErrorDialog: onNegative");
                                    }
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(final DialogInterface dialog) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showBillingErrorDialog: onCancel");
                                    }
                                }
                            }).create();
                    materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
                    materialDialog.show();
                }
            });
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "showBillingErrorDialog: parent no longer active");
        }
    }

    public void showBillingSuccessDialog(@NonNull ActivityHome activity, @NonNull UserFirebaseListener listener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showBillingSuccessDialog");
        }
        if (activity.isActive()) {
            activity.showProgress(false);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final AlertDialog materialDialog = new MaterialAlertDialogBuilder(activity)
                            .setTitle(R.string.menu_billing_success)
                            .setMessage(R.string.content_billing_success)
                            .setIcon(R.drawable.ic_gift)
                            .setPositiveButton(R.string.title_cool, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showBillingSuccessDialog: onPositive");
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showBillingSuccessDialog: onNegative");
                                    }
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(final DialogInterface dialog) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showBillingSuccessDialog: onCancel");
                                    }
                                }
                            }).create();
                    materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
                    materialDialog.show();
                }
            });
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "showBillingErrorDialog: parent no longer active");
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                new UserFirebaseHelper().isAdFree(activity.getApplication(), listener);
            }
        }).start();
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

    /**
     * Check for notification permission before starting the service so that the notification is visible
     */
    public boolean isAcceptableToRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33/*Build.VERSION_CODES.TIRAMISU*/) {
            if (!mIsNotificationPermissionRequested) {
                mIsNotificationPermissionRequested = true;
                return true;
            }
        }
        return false;
    }
}
