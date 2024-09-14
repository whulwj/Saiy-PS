package ai.saiy.android.ui.activity;

import static android.widget.AdapterView.INVALID_POSITION;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ai.saiy.android.R;
import ai.saiy.android.api.SaiyDefaults;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.command.horoscope.HoroscopeHelper;
import ai.saiy.android.command.unknown.Unknown;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.thirdparty.tasker.TaskerHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsBundle;

public class ActivityShowDialog extends AppCompatActivity {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityShowDialog.class.getSimpleName();

    public static final String SHOW_DIALOG = "show_dialog";
    public static final int DIALOG_UNKNOWN_COMMANDS = 1;
    public static final int DIALOG_DOB = 2;
    public static final int DIALOG_AD = 3;

    private long then;

    private void showUnknownCommandSelector() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final String[] actions = getResources().getStringArray(R.array.array_unknown_action);
                for (int i = 0; i < actions.length; i++) {
                    switch (i) {
                        case Unknown.UNKNOWN_STATE:
                        case Unknown.UNKNOWN_REPEAT:
                            break;
                        case Unknown.UNKNOWN_GOOGLE_ASSISTANT:
                        case Unknown.UNKNOWN_ALEXA:
                        case Unknown.UNKNOWN_MICROSOFT_CORTANA:
                        case Unknown.UNKNOWN_WOLFRAM_ALPHA:
                        case Unknown.UNKNOWN_TASKER:
                            actions[i] = getString(R.string.menu_send_to) + " " + actions[i];
                            break;
                    }
                }

                final ArrayList<Integer> disabledIndicesList = new ArrayList<>();
                if (!Installed.isPackageInstalled(getApplicationContext(),
                        Installed.PACKAGE_MICROSOFT_CORTANA)) {
                    disabledIndicesList.add(Unknown.UNKNOWN_MICROSOFT_CORTANA);
                }
                if (!Installed.isPackageInstalled(getApplicationContext(),
                        Installed.PACKAGE_WOLFRAM_ALPHA)) {
                    disabledIndicesList.add(Unknown.UNKNOWN_WOLFRAM_ALPHA);
                }
                if (!new TaskerHelper().isTaskerInstalled(getApplicationContext()).first) {
                    disabledIndicesList.add(Unknown.UNKNOWN_TASKER);
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    disabledIndicesList.add(Unknown.UNKNOWN_ALEXA);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final int defaultIndex = SPH.getCommandUnknownAction(getApplicationContext());
                        final int checkedItem = (defaultIndex != Unknown.UNKNOWN_ALEXA || ai.saiy.android.amazon.TokenHelper.hasToken(getApplicationContext()))? defaultIndex : 0;
                        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(ActivityShowDialog.this)
                                .setCancelable(false)
                                .setTitle(R.string.content_unknown_command)
                                .setIcon(R.drawable.ic_help_circle)
                                .setSingleChoiceItems(actions, checkedItem, new DialogInterface.OnClickListener() {
                                    private int lastCheckedIndex = checkedItem;
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (disabledIndicesList.contains(which)) {
                                            if (dialog instanceof AlertDialog) {
                                                if (INVALID_POSITION != lastCheckedIndex) {
                                                    ((AlertDialog) dialog).getListView().setItemChecked(lastCheckedIndex, true);
                                                } else {
                                                    ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                                                }
                                            }
                                        } else {
                                            lastCheckedIndex = which;
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "showUnknownCommandSelector: onSelection: " + which + ": " + actions[which]);
                                            }
                                        }
                                    }
                                })
                                .setPositiveButton(R.string.menu_select, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dialog instanceof AlertDialog) {
                                            final int selectedIndex = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "showUnknownCommandSelector: onPositive: " + selectedIndex);
                                            }

                                            if (Unknown.UNKNOWN_ALEXA != selectedIndex || ai.saiy.android.amazon.TokenHelper.hasToken(getApplicationContext())) {
                                                SPH.setCommandUnknownAction(getApplicationContext(),
                                                        (selectedIndex == INVALID_POSITION) ? Unknown.UNKNOWN_STATE : selectedIndex);
                                                SPH.setToastUnknown(getApplicationContext(), selectedIndex <= Unknown.UNKNOWN_REPEAT);
                                                if (Unknown.UNKNOWN_ALEXA != selectedIndex) {
                                                    SPH.setDefaultRecognition(getApplicationContext(), SaiyDefaults.VR.NATIVE);
                                                }
                                            } else {
                                                final ai.saiy.android.service.helper.LocalRequest localRequest = new ai.saiy.android.service.helper.LocalRequest(getApplicationContext());
                                                localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(getApplicationContext(), localRequest.getSupportedLanguage(), R.string.amazon_notification_auth_request));
                                                localRequest.getBundle().putInt(ActivityHome.FRAGMENT_INDEX, ActivityHome.INDEX_FRAGMENT_SUPPORTED_APPS);
                                                localRequest.execute();
                                                ai.saiy.android.intent.ExecuteIntent.saiyActivity(getApplicationContext(), ActivityHome.class, localRequest.getBundle(), true);
                                            }
                                            finish();
                                        }
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showUnknownCommandSelector: onNegative");
                                        }
                                        finish();
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showUnknownCommandSelector: onCancel");
                                        }
                                        finish();
                                    }
                                }).create();

                        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
                        materialDialog.show();

                        materialDialog.getListView().setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                            @Override
                            public void onChildViewAdded(View parent, View child) {
                                if (child instanceof CheckedTextView) {
                                    final int itemIndex = Arrays.asList(actions).indexOf(((TextView) child).getText().toString());
                                    child.setEnabled(!disabledIndicesList.contains(itemIndex));
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

    private void showAdOverviewDialog() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.menu_donate)
                .setMessage(R.string.content_ad_overview)
                .setIcon(R.drawable.ic_gift)
                .setPositiveButton(R.string.title_watch_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAdOverviewDialog: onPositive");
                        }
                        startAd();
                    }
                })
                .setNegativeButton(R.string.title_maybe_later, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAdOverviewDialog: onNegative");
                        }
                        ActivityShowDialog.this.finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAdOverviewDialog: onCancel");
                        }
                        ActivityShowDialog.this.finish();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }

    private void showDOBDialog() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(this)
                .setView(R.layout.date_of_birth_dialog_layout)
                .setCancelable(false)
                .setTitle(R.string.menu_date_of_birth)
                .setIcon(R.drawable.ic_baby)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDOBDialog: onPositive");
                        }
                        if (dialog instanceof AlertDialog) {
                            DatePicker datePicker = ((AlertDialog) dialog).getWindow().findViewById(R.id.dobDatePicker);
                            HoroscopeHelper.calculateHoroscope(getApplicationContext(), datePicker.getDayOfMonth(), datePicker.getMonth(), datePicker.getYear());
                        }
                        ActivityShowDialog.this.finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDOBDialog: onNegative");
                        }
                        ActivityShowDialog.this.finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDOBDialog: onCancel");
                        }
                        ActivityShowDialog.this.finish();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        calendar.setLenient(true);
        calendar.set(SPH.getDobYear(getApplicationContext()), SPH.getDobMonth(getApplicationContext()), SPH.getDobDay(getApplicationContext()));
        ((DatePicker) materialDialog.getWindow().findViewById(R.id.dobDatePicker)).updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
    }

    private void startAd() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startAd");
        }
        ExecuteIntent.saiyActivity(this, ActivityDonate.class, null, true);
        finish();
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        setFinishOnTouchOutside(false);
        this.then = System.nanoTime();
        final Bundle extras = getIntent().getExtras();
        if (!UtilsBundle.notNaked(extras) || UtilsBundle.isSuspicious(extras)) {
            return;
        }
        switch (extras.getInt(SHOW_DIALOG, 0)) {
            case DIALOG_UNKNOWN_COMMANDS:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "DIALOG_UNKNOWN_COMMANDS");
                }
                showUnknownCommandSelector();
                break;
            case DIALOG_DOB:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "DIALOG_DOB");
                }
                showDOBDialog();
                break;
            case DIALOG_AD:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "DIALOG_AD");
                }
                showAdOverviewDialog();
                break;
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "DEFAULT");
                }
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
            MyLog.getElapsed(CLS_NAME, this.then);
        }
    }
}
