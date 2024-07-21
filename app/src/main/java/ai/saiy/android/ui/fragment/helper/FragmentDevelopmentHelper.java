package ai.saiy.android.ui.fragment.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.View;
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

import java.util.ArrayList;
import java.util.Arrays;

import ai.saiy.android.R;
import ai.saiy.android.device.DeviceInfo;
import ai.saiy.android.firebase.database.model.PremiumUser;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIApplicationsAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentDevelopment;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.user.UserFirebaseHelper;
import ai.saiy.android.utils.UtilsAuth;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

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
                                UtilsAuth.getFirebaseInstanceId();
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
        UtilsAuth.getFirebaseInstanceId();
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
        AsyncTask.execute(new Runnable() {
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
                                dialog.dismiss();
                                accountAction();
                            }
                        })
                        .setNegativeButton(R.string.title_maybe_later, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showAccountOverviewDialog: onNegative");
                                }
                                dialog.dismiss();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(final DialogInterface dialog) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showAccountOverviewDialog: onCancel");
                                }
                                dialog.dismiss();
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FragmentDevelopmentHelper.this.premiumUserPair = new UserFirebaseHelper().getPremiumUser(anonymousUid);
                    }
                }).start();
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
                                    dialog.dismiss();
                                    logOutAccount();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showDeleteAccountDialog: onNegative");
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(final DialogInterface dialog) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "showDeleteAccountDialog: onCancel");
                                    }
                                    dialog.dismiss();
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

    public ActivityHome getParentActivity() {
        return this.parentFragment.getParentActivity();
    }

    public FragmentDevelopment getParent() {
        return this.parentFragment;
    }
}
