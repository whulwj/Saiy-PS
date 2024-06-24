package ai.saiy.android.ui.fragment.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.device.DeviceInfo;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIApplicationsAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentDevelopment;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;

public class FragmentDevelopmentHelper {
    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentDevelopmentHelper.class.getSimpleName();

    private final FragmentDevelopment parentFragment;

    public FragmentDevelopmentHelper(FragmentDevelopment parentFragment) {
        this.parentFragment = parentFragment;
    }

    private String getString(@StringRes int resId) {
        return getApplicationContext().getString(resId);
    }

    private ArrayList<ContainerUI> getUIComponents() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getUIComponents");
        }
        ArrayList<ContainerUI> arrayList = new ArrayList<>();
        ContainerUI containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_translation));
        containerUI.setSubtitle(getString(R.string.menu_tap_contribute));
        containerUI.setIconMain(R.drawable.ic_translate);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);
 
        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_report_bug));
        containerUI.setSubtitle(getString(R.string.menu_tap_send));
        containerUI.setIconMain(R.drawable.ic_bug);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);
 
        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_to_do_list));
        containerUI.setSubtitle(getString(R.string.menu_tap_view));
        containerUI.setIconMain(R.drawable.ic_command_list);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);
 
        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_source_code));
        containerUI.setSubtitle(getString(R.string.menu_tap_view));
        containerUI.setIconMain(R.drawable.ic_github);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);
 
        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_developer_api));
        containerUI.setSubtitle(getString(R.string.menu_tap_view));
        containerUI.setIconMain(R.drawable.ic_code);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);
        return arrayList;
    }

    private Context getApplicationContext() {
        return this.parentFragment.getApplicationContext();
    }

    public UIApplicationsAdapter getAdapter(ArrayList<ContainerUI> arrayList) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdapter");
        }
        return new UIApplicationsAdapter(arrayList, getParent(), getParent());
    }

    public RecyclerView getRecyclerView(View view) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getRecyclerView");
        }
        RecyclerView recyclerView = view.findViewById(R.id.layout_common_fragment_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getParentActivity(), null));
        return recyclerView;
    }

    public void finaliseUI() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ContainerUI> tempArray = getUIComponents();
                if (getParent().isActive()) {
                    if (getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)) {
                        try {
                            Thread.sleep(200L);
                        } catch (InterruptedException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                }
                if (getParent().isActive()) {
                    getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getParent().getObjects().addAll(tempArray);
                            getParent().getAdapter().notifyItemRangeInserted(0, getParent().getObjects().size());
                        }
                    });
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                }
            }
        });
    }

    public void toast(String text, int duration) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "makeToast: " + text);
        }
        if (getParent().isActive()) {
            getParentActivity().toast(text, duration);
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "toast Fragment detached");
        }
    }

    public void showTranslationDialog() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showTranslationDialog");
        }
        getParentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                        .setTitle(R.string.menu_translation)
                        .setMessage(R.string.content_translate_overview)
                        .setIcon(R.drawable.ic_translate)
                        .setNeutralButton(R.string.title_copy_link, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ai.saiy.android.command.clipboard.ClipboardHelper.setClipboardContent(getApplicationContext(), "https://crowdin.com/project/saiy");
                                toast(getString(R.string.title_clipboard_copied), Toast.LENGTH_SHORT);
                            }
                        })
                        .setPositiveButton(R.string.title_sign_up, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showTranslationDialog: onPositive");
                                }
                                dialog.dismiss();
                                ai.saiy.android.intent.ExecuteIntent.webSearch(getApplicationContext(), "https://crowdin.com/project/saiy");
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showTranslationDialog: onNegative");
                                }
                                dialog.dismiss();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(final DialogInterface dialog) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showTranslationDialog: onCancel");
                                }
                                dialog.dismiss();
                            }
                        }).create();
                materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
                materialDialog.show();
            }
        });
    }

    public void showToDoListDialog() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showToDoListDialog");
        }
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setTitle(R.string.menu_to_do_list)
                .setMessage(R.string.content_to_do_list)
                .setIcon(R.drawable.ic_command_list)
                .setPositiveButton(R.string.title_good_luck, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showToDoListDialog: onPositive");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showToDoListDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }

    public void showReportBugDialog() {
        getParentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                        .setTitle(R.string.menu_report_bug)
                        .setMessage(R.string.content_report_bug)
                        .setIcon(R.drawable.ic_bug)
                        .setPositiveButton(R.string.title_email, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showReportBugDialog: onPositive");
                                }
                                dialog.dismiss();
                                if (ExecuteIntent.sendEmail(getApplicationContext(), new String[]{Constants.SAIY_FEEDBACK_EMAIL},
                                        getString(R.string.title_bug_report), DeviceInfo.getDeviceInfo(getApplicationContext()))) {
                                    return;
                                }
                                toast(getString(R.string.error_no_application), Toast.LENGTH_LONG);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showReportBugDialog: onNegative");
                                }
                                dialog.dismiss();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(final DialogInterface dialog) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showReportBugDialog: onCancel");
                                }
                                dialog.dismiss();
                            }
                        }).create();
                materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
                materialDialog.show();
            }
        });
    }

    public ActivityHome getParentActivity() {
        return this.parentFragment.getParentActivity();
    }

    public FragmentDevelopment getParent() {
        return this.parentFragment;
    }
}
