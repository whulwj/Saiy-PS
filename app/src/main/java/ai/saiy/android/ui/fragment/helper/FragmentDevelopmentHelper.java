package ai.saiy.android.ui.fragment.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ai.saiy.android.R;
import ai.saiy.android.device.DeviceInfo;
import ai.saiy.android.firebase.FirebaseInstallationsHelper;
import ai.saiy.android.firebase.UtilsFirebase;
import ai.saiy.android.firebase.database.read.PremiumUser;
import ai.saiy.android.firebase.database.write.Design;
import ai.saiy.android.firebase.database.write.EmotionAnalysis;
import ai.saiy.android.firebase.database.write.Enhancement;
import ai.saiy.android.firebase.database.write.NaturalLanguage;
import ai.saiy.android.firebase.database.write.VocalVerification;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIApplicationsAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentDevelopment;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.user.UserFirebaseHelper;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FragmentDevelopmentHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentDevelopmentHelper.class.getSimpleName();

    private volatile Pair<Boolean, PremiumUser> premiumUserPair;
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
        containerUI.setTitle(getString(R.string.menu_account));
        containerUI.setSubtitle(getString(R.string.menu_tap_manage));
        containerUI.setIconMain(R.drawable.ic_face);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_translation));
        containerUI.setSubtitle(getString(R.string.menu_tap_contribute));
        containerUI.setIconMain(R.drawable.ic_translate);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_design));
        containerUI.setSubtitle(getString(R.string.menu_tap_contribute));
        containerUI.setIconMain(R.drawable.ic_palette);
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
        containerUI.setTitle(getString(R.string.menu_suggest_natural_language));
        containerUI.setSubtitle(getString(R.string.menu_tap_send));
        containerUI.setIconMain(R.drawable.ic_routes);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_suggest_an_enhancement));
        containerUI.setSubtitle(getString(R.string.menu_tap_send));
        containerUI.setIconMain(R.drawable.ic_auto_fix);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_vocal_verification_feedback));
        containerUI.setSubtitle(getString(R.string.menu_tap_send));
        containerUI.setIconMain(R.drawable.ic_account_key);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_emotion_analysis_feedback));
        containerUI.setSubtitle(getString(R.string.menu_tap_send));
        containerUI.setIconMain(R.drawable.ic_yin_yang);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        arrayList.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_generic_feedback));
        containerUI.setSubtitle(getString(R.string.menu_tap_send));
        containerUI.setIconMain(R.drawable.ic_thumbs_up_down);
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

    /**
     * Get the recycler view for this fragment
     *
     * @param parent the view parent
     * @return the {@link RecyclerView}
     */
    public RecyclerView getRecyclerView(@NonNull final View parent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getRecyclerView");
        }
        final RecyclerView recyclerView = parent.findViewById(R.id.layout_common_fragment_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getParentActivity(), null));
        return recyclerView;
    }

    public void finaliseUI() {
        Schedulers.single().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ContainerUI> tempArray = getUIComponents();
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
        }, getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)? FragmentHome.DRAWER_CLOSE_DELAY : 0, TimeUnit.MILLISECONDS);
    }

    public void handleActivityResult(final int resultCode, Intent intent) {
        if (!getParent().isActive()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "handleActivityResult: no longer active");
            }
            return;
        }
        final IdpResponse idpResponse = IdpResponse.fromResultIntent(intent);
        if (resultCode != ActivityHome.RESULT_OK) {
            // Sign in failed
            if (DEBUG) {
                MyLog.i(CLS_NAME, "handleActivityResult: ResultCodes.CANCELLED");
            }
            if (idpResponse == null) {
                // User pressed back button
                snack(getParent().getView(), getString(R.string.cancelled), Snackbar.LENGTH_SHORT, null, null);
                return;
            }
            final @ErrorCodes.Code int code = (idpResponse.getError() == null)? ErrorCodes.UNKNOWN_ERROR : idpResponse.getError().getErrorCode();
            if (code == ErrorCodes.NO_NETWORK) {
                snack(getParent().getView(), getString(R.string.error_network), Snackbar.LENGTH_SHORT, null, null);
            } else if (code == ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT) {
                // We need to deal with a merge conflict. Occurs after catching an email link
                final AuthCredential pendingCredential = idpResponse.getCredentialForLinking();
                if (pendingCredential == null) {
                    MyLog.w(CLS_NAME, "Nothing to resolve");
                    snack(getParent().getView(), getString(R.string.content_account_error), Snackbar.LENGTH_SHORT, null, null);
                } else {
                    // Signed in anonymous, awaiting merge conflict
                    FirebaseAuth.getInstance().signInWithCredential(pendingCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                snack(getParent().getView(), getString(R.string.content_account_create_success), Snackbar.LENGTH_SHORT, null, null);
                                handleSignInResult(idpResponse.isNewUser());
                                FirebaseInstallationsHelper.getFirebaseInstanceId();
                            } else {
                                snack(getParent().getView(), getString(R.string.content_account_disabled), Snackbar.LENGTH_SHORT, null, null);
                            }
                        }
                    });
                }
            } else if (code == ErrorCodes.ERROR_USER_DISABLED) {
                snack(getParent().getView(), getString(R.string.content_account_disabled), Snackbar.LENGTH_SHORT, null, null);
            } else {
                snack(getParent().getView(), getString(R.string.content_account_error), Snackbar.LENGTH_SHORT, null, null);
            }
            return;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "handleActivityResult: ResultCodes.OK");
        }
        if (idpResponse == null) {
            snack(getParent().getView(), getString(R.string.content_account_error), Snackbar.LENGTH_SHORT, null, null);
            return;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "handleActivityResult: token: " + idpResponse.getIdpToken());
            MyLog.i(CLS_NAME, "handleActivityResult: secret: " + idpResponse.getIdpSecret());
            MyLog.i(CLS_NAME, "handleActivityResult: newUser: " + idpResponse.isNewUser());
            MyLog.i(CLS_NAME, "handleActivityResult: getProviderType: " + idpResponse.getProviderType());
        }
        snack(getParent().getView(), getString(R.string.content_account_create_success), Snackbar.LENGTH_SHORT, null, null);
        handleSignInResult(idpResponse.isNewUser());
        FirebaseInstallationsHelper.getFirebaseInstanceId();
    }

    private void handleSignInResult(final boolean isNewUser) {
        final com.google.firebase.auth.FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "handleActivityResult: firebaseUser null");
            }
            return;
        }
        final String uid = firebaseUser.getUid();
        if (!ai.saiy.android.utils.UtilsString.notNaked(uid)) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "handleActivityResult: firebaseUser uId naked");
            }
            return;
        }
        if (firebaseUser.isAnonymous()) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "handleActivityResult: firebaseUser remains anonymous");
            }
            return;
        }
        String anonymousUid = ai.saiy.android.utils.SPH.getFirebaseAnonymousUid(getApplicationContext());
        if (DEBUG) {
            MyLog.i(CLS_NAME, "handleActivityResult: anonymousUid: " + anonymousUid);
        }

        if (ai.saiy.android.utils.UtilsString.notNaked(anonymousUid)) {
            if (DEBUG) {
                if (isNewUser) {
                    MyLog.w(CLS_NAME, "handleActivityResult: migration new user: using anonymousUid");
                } else {
                    MyLog.i(CLS_NAME, "handleActivityResult: migration uIds");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "handleActivityResult: not previously anonymous");
            }
            anonymousUid = null;
        }
        ai.saiy.android.utils.SPH.setFirebaseAnonymousUid(getApplicationContext(), null);
        ai.saiy.android.utils.SPH.setFirebaseUid(getApplicationContext(), uid);
        ai.saiy.android.utils.SPH.setFirebaseMigratedUid(getApplicationContext(), anonymousUid);
        if (ai.saiy.android.utils.UtilsString.notNaked(anonymousUid)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "handleActivityResult: migrating");
            }
            migratePremium(anonymousUid, uid);
        }
    }

    private void migratePremium(final String anonymousUid, final String uid) {
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                new UserFirebaseHelper().getRequestPremiumUser(anonymousUid);
                if (premiumUserPair == null || !premiumUserPair.first) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "migratePremium: not premium");
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "migratePremium: premium user");
                    }
                    new UserFirebaseHelper().migrateUser(anonymousUid, uid, premiumUserPair.second);
                }
            }
        });
    }

    public void snack(@Nullable final View view, @Nullable final String text, final int length, @Nullable final String toAction,
                  @Nullable final View.OnClickListener onClickListener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "snack: " + text);
        }
        if (getParent().isActive()) {
            getParentActivity().snack(view, text, length, toAction, onClickListener);
        } else {
            MyLog.w(CLS_NAME, "snack Fragment detached");
        }
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

    public void showAccountOverviewDialog() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showAccountOverviewDialog");
        }
        if (SPH.getAccountOverview(getApplicationContext())) {
            accountAction();
            return;
        }
        SPH.markAccountOverview(getApplicationContext());
        getParentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                        .setTitle(R.string.menu_account)
                        .setMessage(R.string.content_account_overview)
                        .setIcon(R.drawable.ic_face)
                        .setPositiveButton(R.string.title_login, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showAccountOverviewDialog: onPositive");
                                }
                                accountAction();
                            }
                        })
                        .setNegativeButton(R.string.title_maybe_later, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showAccountOverviewDialog: onNegative");
                                }
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(final DialogInterface dialog) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showAccountOverviewDialog: onCancel");
                                }
                            }
                        }).create();
                materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
                materialDialog.show();
            }
        });
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
                                ai.saiy.android.intent.ExecuteIntent.webSearch(getApplicationContext(), "https://crowdin.com/project/saiy");
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showTranslationDialog: onNegative");
                                }
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(final DialogInterface dialog) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showTranslationDialog: onCancel");
                                }
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
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showToDoListDialog: onCancel");
                        }
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }

    public void showDesignOverviewDialog() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showDesignOverviewDialog");
        }
        if (ai.saiy.android.utils.SPH.isDesignOverviewShown(getApplicationContext())) {
            showDesignDialog();
            return;
        }
        ai.saiy.android.utils.SPH.markDesignOverviewShown(getApplicationContext());
        showProgress(false);
        getParentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                        .setTitle(R.string.menu_design)
                        .setMessage(R.string.content_design_overview)
                        .setIcon(R.drawable.ic_palette)
                        .setPositiveButton(R.string.title_cool, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showDesignOverviewDialog: onPositive");
                                }
                                showDesignDialog();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showDesignOverviewDialog: onNegative");
                                }
                                showDesignDialog();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(final DialogInterface dialog) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showDesignOverviewDialog: onCancel");
                                }
                                showDesignDialog();
                            }
                        }).create();
                materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
                materialDialog.show();
            }
        });
    }

    private void showDesignDialog() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setCancelable(false)
                .setView(R.layout.design_dialog_layout)
                .setTitle(R.string.menu_design)
                .setIcon(R.drawable.ic_palette)
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDesignDialog: onPositive");
                        }
                        if (dialog instanceof AlertDialog) {
                            final EditText editTextWebLink = ((AlertDialog) dialog).getWindow().findViewById(R.id.etWebLink);
                            final EditText editTextSummary = ((AlertDialog) dialog).getWindow().findViewById(R.id.etDesignSummary);
                            if (editTextWebLink.getText() == null || editTextSummary.getText() == null) {
                                dialog.dismiss();
                                return;
                            }
                            final String webLink = editTextWebLink.getText().toString().trim();
                            final String summary = editTextSummary.getText().toString().trim();
                            if (!UtilsString.notNaked(webLink)) {
                                toast(getString(R.string.design_web_link_empty), Toast.LENGTH_SHORT);
                                return;
                            }
                            dialog.dismiss();
                            toast(getString(R.string.menu_thanks_exclamation), Toast.LENGTH_SHORT);
                            sendSubmissionDesign(new Design(Design.getType(((Spinner) ((AlertDialog) dialog).getWindow().findViewById(R.id.spDesign)).getSelectedItemPosition()), webLink, summary, DateFormat.getDateTimeInstance().format(new Date())));
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDesignDialog: onNegative");
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDesignDialog: onCancel");
                        }
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
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
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(final DialogInterface dialog) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showReportBugDialog: onCancel");
                                }
                            }
                        }).create();
                materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
                materialDialog.show();
            }
        });
    }

    public void showNaturalLanguageDialog() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setView(R.layout.natural_language_dialog_layout)
                .setTitle(R.string.menu_suggest_natural_language)
                .setIcon(R.drawable.ic_routes)
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showNaturalLanguageDialog: onPositive");
                        }
                        if (dialog instanceof AlertDialog) {
                            final EditText editTextNaturalLanguage = ((AlertDialog) dialog).getWindow().findViewById(R.id.etNaturalLanguage);
                            final EditText editTextOutcome = ((AlertDialog) dialog).getWindow().findViewById(R.id.etOutcome);
                            if (editTextNaturalLanguage.getText() == null || editTextOutcome.getText() == null) {
                                dialog.dismiss();
                                return;
                            }
                            final String trim = editTextNaturalLanguage.getText().toString().trim();
                            final String outcome = editTextOutcome.getText().toString().trim();
                            if (!UtilsString.notNaked(trim) || !UtilsString.notNaked(outcome)) {
                                toast(getString(R.string.content_empty), Toast.LENGTH_SHORT);
                                return;
                            }
                            dialog.dismiss();
                            toast(getString(R.string.menu_understood_exclamation), Toast.LENGTH_SHORT);
                            sendSubmissionNaturalLanguage(new NaturalLanguage(trim, outcome, DateFormat.getDateTimeInstance().format(new Date())));
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showNaturalLanguageDialog: onNegative");
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showNaturalLanguageDialog: onCancel");
                        }
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    public void showEnhancementDialog() {
        final View customView = LayoutInflater.from(getParentActivity()).inflate(R.layout.md_dialog_input, null, false);
        customView.findViewById(R.id.md_promptCheckbox).setVisibility(View.GONE);
        final EditText editText = customView.findViewById(android.R.id.input);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500) {}});
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setView(customView)
                .setTitle(R.string.menu_suggest_an_enhancement)
                .setMessage(R.string.content_enhancement)
                .setIcon(R.drawable.ic_auto_fix)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CharSequence charSequence = null;
                        if (dialog instanceof AlertDialog) {
                            final EditText editText = ((AlertDialog) dialog).findViewById(android.R.id.input);
                            charSequence = (editText == null) ? null : editText.getText();
                        }
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showEnhancementDialog: input: " + charSequence);
                        }

                        if (charSequence == null) {
                            return;
                        }
                        final String description = charSequence.toString().trim();
                        if (!ai.saiy.android.utils.UtilsString.notNaked(description)) {
                            toast(getString(R.string.content_empty), Toast.LENGTH_SHORT);
                            return;
                        }
                        toast(getString(R.string.menu_super_exclamation), Toast.LENGTH_SHORT);
                        sendSubmissionEnhancement(new Enhancement(description, DateFormat.getDateTimeInstance().format(new Date())));
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    public void showVocalVerificationFeedbackDialog() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setView(R.layout.rating_dialog_layout)
                .setTitle(R.string.menu_vocal_verification_feedback)
                .setIcon(R.drawable.ic_account_key)
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showVocalVerificationFeedbackDialog: onPositive");
                        }
                        if (dialog instanceof AlertDialog) {
                            final EditText editText = ((AlertDialog) dialog).getWindow().findViewById(R.id.etFeedback);
                            if (editText.getText() == null) {
                                dialog.dismiss();
                                return;
                            }
                            final String feedback = editText.getText().toString().trim();
                            if (!UtilsString.notNaked(feedback)) {
                                toast(getString(R.string.content_empty), Toast.LENGTH_SHORT);
                                return;
                            }
                            dialog.dismiss();
                            toast(getString(R.string.menu_hear_you_exclamation), Toast.LENGTH_SHORT);
                            sendSubmissionVocalVerification(new VocalVerification(feedback, Math.round(((RatingBar) ((AlertDialog) dialog).getWindow().findViewById(R.id.ratingBar)).getRating()), DateFormat.getDateTimeInstance().format(new Date())));
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showVocalVerificationFeedbackDialog: onNegative");
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showVocalVerificationFeedbackDialog: onCancel");
                        }
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    public void showEmotionAnalysisFeedbackDialog() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setView(R.layout.rating_dialog_layout)
                .setTitle(R.string.menu_emotion_analysis_feedback)
                .setIcon(R.drawable.ic_yin_yang)
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showEmotionAnalysisFeedbackDialog: onPositive");
                        }
                        if (dialog instanceof AlertDialog) {
                            final EditText editText = ((AlertDialog) dialog).getWindow().findViewById(R.id.etFeedback);
                            if (editText.getText() == null) {
                                dialog.dismiss();
                                return;
                            }
                            final String feedback = editText.getText().toString().trim();
                            if (!UtilsString.notNaked(feedback)) {
                                toast(getString(R.string.content_empty), Toast.LENGTH_SHORT);
                                return;
                            }
                            dialog.dismiss();
                            toast(getString(R.string.menu_happy_days_exclamation), Toast.LENGTH_SHORT);
                            sendSubmissionEmotionAnalysis(new EmotionAnalysis(feedback, Math.round(((RatingBar) ((AlertDialog) dialog).getWindow().findViewById(R.id.ratingBar)).getRating()), DateFormat.getDateTimeInstance().format(new Date())));
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showEmotionAnalysisFeedbackDialog: onNegative");
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showEmotionAnalysisFeedbackDialog: onCancel");
                        }
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    public void showGenericDialog() {
        getParentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                        .setTitle(R.string.menu_generic_feedback)
                        .setMessage(R.string.content_generic)
                        .setIcon(R.drawable.ic_thumbs_up_down)
                        .setPositiveButton(R.string.title_email, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showGenericDialog: onPositive");
                                }
                                if (ExecuteIntent.sendEmail(getApplicationContext(), new String[]{Constants.SAIY_FEEDBACK_EMAIL},
                                        getString(R.string.title_feedback), DeviceInfo.getDeviceInfo(getApplicationContext()))) {
                                    return;
                                }
                                toast(getString(R.string.error_no_application), Toast.LENGTH_LONG);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showGenericDialog: onNegative");
                                }
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(final DialogInterface dialog) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showGenericDialog: onCancel");
                                }
                            }
                        }).create();
                materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
                materialDialog.show();
            }
        });
    }

    private void accountAction() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "accountAction");
        }
        final com.google.firebase.auth.FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || firebaseUser.isAnonymous()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "createAccount");
            }
            final String anonymousUid = SPH.getFirebaseAnonymousUid(getApplicationContext());
            if (ai.saiy.android.utils.UtilsString.notNaked(anonymousUid)) {
                Schedulers.io().scheduleDirect(new Runnable() {
                    @Override
                    public void run() {
                        FragmentDevelopmentHelper.this.premiumUserPair = new UserFirebaseHelper().getPremiumUser(anonymousUid);
                    }
                });
            }
            if (getParent().isActive()) {
                final AuthUI.SignInIntentBuilder intentBuilder = AuthUI.getInstance().createSignInIntentBuilder()
                        .setTheme(R.style.AppTheme).setLogo(R.mipmap.ic_launcher)
                        .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(), new AuthUI.IdpConfig.GoogleBuilder().build(), new AuthUI.IdpConfig.FacebookBuilder().build(), new AuthUI.IdpConfig.TwitterBuilder().build()))
                        .setTosAndPrivacyPolicyUrls(Constants.SAIY_TOU_URL, Constants.SAIY_PRIVACY_URL).setIsSmartLockEnabled(false);
                if (ai.saiy.android.utils.UtilsString.notNaked(anonymousUid)) {
                    intentBuilder.enableAnonymousUsersAutoUpgrade();
                }
                getParent().startActivityForResult(intentBuilder.setLockOrientation(false).build(), FragmentDevelopment.RC_ACCOUNT);
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "showDeleteAccountDialog");
            }
            getParentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                            .setTitle(R.string.menu_account)
                            .setMessage(R.string.content_delete_account)
                            .setIcon(R.drawable.ic_face)
                            .setPositiveButton(R.string.title_delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showDeleteAccountDialog: onPositive");
                                    }
                                    logOutAccount();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showDeleteAccountDialog: onNegative");
                                    }
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(final DialogInterface dialog) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showDeleteAccountDialog: onCancel");
                                    }
                                }
                            }).create();
                    materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
                    materialDialog.show();
                }
            });
        }
    }

    private void logOutAccount() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "logOutAccount");
        }
        AuthUI.getInstance().signOut(getApplicationContext()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    snack(getParent().getView(), getString(R.string.content_account_delete_success), Snackbar.LENGTH_SHORT, null, null);
                } else {
                    snack(getParent().getView(), getString(R.string.content_account_error), Snackbar.LENGTH_SHORT, null, null);
                }
                deleteAccount();
                SPH.setFirebaseUid(getApplicationContext(), null);
            }
        });
    }

    private void deleteAccount() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "deleteAccount");
        }
        AuthUI.getInstance().delete(getApplicationContext()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (DEBUG) {
                    if (task.isSuccessful()) {
                        MyLog.i(CLS_NAME, "deleteAccount: isSuccessful");
                    } else {
                        MyLog.i(CLS_NAME, "deleteAccount: failure");
                    }
                }
            }
        });
    }

    private void sendSubmissionDesign(@NonNull Design design) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendSubmissionDesign: userSignedIn: " + getParentActivity().userSignedIn());
        }
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference(UtilsFirebase.DATABASE_WRITE).child("design").child(design.getType()).setValue(design).addOnCompleteListener(getParentActivity(), new com.google.android.gms.tasks.OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "sendSubmissionDesign: task success: " + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        task.getException().printStackTrace();
                    }
                }
            }
        });
    }

    private void sendSubmissionNaturalLanguage(@NonNull NaturalLanguage naturalLanguage) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendSubmissionNaturalLanguage: userSignedIn: " + getParentActivity().userSignedIn());
        }
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference(UtilsFirebase.DATABASE_WRITE).child("natural_language").getRef().setValue(naturalLanguage).addOnCompleteListener(getParentActivity(), new com.google.android.gms.tasks.OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "sendSubmissionNaturalLanguage: task success: " + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        task.getException().printStackTrace();
                    }
                }
            }
        });
    }

    private void sendSubmissionEnhancement(@NonNull Enhancement enhancement) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendSubmissionBug: userSignedIn: " + getParentActivity().userSignedIn());
        }
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference(UtilsFirebase.DATABASE_WRITE).child("enhancement").getRef().setValue(enhancement).addOnCompleteListener(getParentActivity(), new com.google.android.gms.tasks.OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "sendSubmissionBug: task success: " + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        task.getException().printStackTrace();
                    }
                }
            }
        });
    }

    private void sendSubmissionVocalVerification(@NonNull VocalVerification vocalVerification) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendSubmissionVocalVerification: userSignedIn: " + getParentActivity().userSignedIn());
        }
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference(UtilsFirebase.DATABASE_WRITE).child("vocal_verification").getRef().setValue(vocalVerification).addOnCompleteListener(getParentActivity(), new com.google.android.gms.tasks.OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "sendSubmissionVocalVerification: task success: " + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        task.getException().printStackTrace();
                    }
                }
            }
        });
    }

    private void sendSubmissionEmotionAnalysis(@NonNull EmotionAnalysis emotionAnalysis) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sendSubmissionEmotionAnalysis: userSignedIn: " + getParentActivity().userSignedIn());
        }
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference(UtilsFirebase.DATABASE_WRITE).child("emotion_analysis").getRef().setValue(emotionAnalysis).addOnCompleteListener(getParentActivity(), new com.google.android.gms.tasks.OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "sendSubmissionEmotionAnalysis: task success: " + task.isSuccessful());
                    if (!task.isSuccessful()) {
                        task.getException().printStackTrace();
                    }
                }
            }
        });
    }

    public void showProgress(boolean visible) {
        if (getParent().isActive()) {
            getParentActivity().showProgress(visible);
        }
    }

    public ActivityHome getParentActivity() {
        return this.parentFragment.getParentActivity();
    }

    public FragmentDevelopment getParent() {
        return this.parentFragment;
    }
}
