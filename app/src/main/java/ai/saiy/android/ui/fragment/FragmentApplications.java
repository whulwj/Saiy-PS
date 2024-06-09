package ai.saiy.android.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.amazon.identity.auth.device.AuthError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.amazon.AuthorizationWrapper;
import ai.saiy.android.amazon.TokenHelper;
import ai.saiy.android.amazon.UtilsNetwork;
import ai.saiy.android.amazon.listener.AuthorizationListener;
import ai.saiy.android.applications.Install;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.permissions.PermissionHelper;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.helper.FragmentApplicationsHelper;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;

public class FragmentApplications extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    private static final Object lock = new Object();

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentApplications.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;
    private ArrayList<ContainerUI> mObjects;
    private FragmentApplicationsHelper helper;
    private volatile boolean isAttemptingReinstallation;
    private Context mContext;

    public static FragmentApplications newInstance(Bundle args) {
        return new FragmentApplications();
    }

    private void onAuthorizationStatusChange() {
        if (isActive()) {
            showProgress(false);
            getParentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FragmentApplications.this.mObjects.get(5).setSubtitle(TokenHelper.hasToken(FragmentApplications.this.getApplicationContext()) ? FragmentApplications.this.getString(R.string.menu_tap_deauthorise) : FragmentApplications.this.getString(R.string.menu_tap_authorise));
                    FragmentApplications.this.mAdapter.notifyItemChanged(5);
                }
            });
        }
    }

    public void toast(String text, int duration) {
        if (this.DEBUG) {
            MyLog.d(CLS_NAME, "makeToast: " + text);
        }
        if (isActive()) {
            getParentActivity().toast(text, duration);
        } else if (this.DEBUG) {
            MyLog.w(CLS_NAME, "toast Fragment detached");
        }
    }

    public void setAttemptingReinstallation(boolean isAttempting) {
        this.isAttemptingReinstallation = isAttempting;
    }

    public boolean isAttemptingReinstallation() {
        return this.isAttemptingReinstallation;
    }

    public void showAlexaRegionDialog() {
        int checkedItem;
        int alexaRegion = ai.saiy.android.utils.SPH.getAlexaRegion(getApplicationContext(), UtilsNetwork.ALEXA_NORTH_AMERICA);
        if (this.DEBUG) {
            MyLog.d(CLS_NAME, "showAlexaRegionDialog: " + alexaRegion);
        }
        switch (alexaRegion) {
            case UtilsNetwork.ALEXA_REGION_EUROPE:
            case UtilsNetwork.ALEXA_NORTH_AMERICA:
            case UtilsNetwork.ALEXA_ASIA:
                checkedItem = alexaRegion;
                break;
            default:
                checkedItem = UtilsNetwork.ALEXA_NORTH_AMERICA;
                break;
        }
        final String[] regions = getResources().getStringArray(R.array.array_alexa_region);
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setCancelable(false)
                .setTitle(R.string.menu_alexa_closest_region)
                .setIcon(R.drawable.ic_alexa)
                .setSingleChoiceItems(regions, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAlexaRegionDialog: onSelection: " + which + ": " + regions[which]);
                        }
                    }
                })
                .setNeutralButton(StringUtils.capitalize(getString(R.string.add_new)), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ExecuteIntent.settingsIntent(getParentActivity().getApplicationContext(),
                                IntentConstants.SETTINGS_ADD_ACCOUNT);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.menu_select, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog instanceof AlertDialog) {
                            final int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                            if (FragmentApplications.this.DEBUG) {
                                MyLog.i(CLS_NAME, "showAlexaRegionDialog: onPositive: " + position);
                            }
                            ai.saiy.android.utils.SPH.setAlexaRegion(FragmentApplications.this.getApplicationContext(), position);
                            FragmentApplications.this.showAlexaOverviewDialog();
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAlexaRegionDialog: onNegative");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAlexaRegionDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    public void showProgress(boolean visible) {
        if (isActive()) {
            getParentActivity().showProgress(visible);
        }
    }

    public void showAlexaOverviewDialog() {
        if (this.DEBUG) {
            MyLog.d(CLS_NAME, "showAlexaOverviewDialog");
        }
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.login_amazon_dialog_layout, null);
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setTitle(R.string.menu_login_amazon)
                .setIcon(R.drawable.ic_alexa)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAlexaOverviewDialog: onNegative");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAlexaOverviewDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
        view.findViewById(R.id.ibLoginWithAmazon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FragmentApplications.this.DEBUG) {
                    MyLog.i(CLS_NAME, "ibLoginWithAmazon: onClick");
                }
                materialDialog.dismiss();
                FragmentApplications.this.authoriseAlexa();
            }
        });
    }

    public void authoriseAlexa() {
        if (this.DEBUG) {
            MyLog.d(CLS_NAME, "authoriseAlexa");
        }
        showProgress(true);
        new AuthorizationWrapper(getApplicationContext()).authoriseUser(new AuthorizationListener() {
            @Override
            public void onSuccess() {
                if (FragmentApplications.this.DEBUG) {
                    MyLog.i(CLS_NAME, "onSuccess");
                }
                FragmentApplications.this.toast(FragmentApplications.this.getString(R.string.success_), Toast.LENGTH_SHORT);
                FragmentApplications.this.onAuthorizationStatusChange();
            }

            @Override
            public void onError(Exception e) {
                if (FragmentApplications.this.DEBUG) {
                    MyLog.e(CLS_NAME, "onError");
                    e.printStackTrace();
                }
                FragmentApplications.this.toast(FragmentApplications.this.getString(R.string.failed), Toast.LENGTH_SHORT);
                FragmentApplications.this.showProgress(false);
            }

            @Override
            public void onCancel() {
                if (FragmentApplications.this.DEBUG) {
                    MyLog.i(CLS_NAME, "onCancel");
                }
                FragmentApplications.this.showProgress(false);
            }
        });
    }

    public void deauthoriseAlexa() {
        if (this.DEBUG) {
            MyLog.d(CLS_NAME, "deauthoriseAlexa");
        }
        showProgress(true);
        ai.saiy.android.utils.SPH.setAlexaAccessToken(getApplicationContext(), null);
        ai.saiy.android.utils.SPH.setAlexaRefreshToken(getApplicationContext(), null);
        ai.saiy.android.utils.SPH.setAlexaAccessTokenExpiry(getApplicationContext(), 0L);
        ai.saiy.android.utils.SPH.setAlexaNotification(getApplicationContext(), false);
        ai.saiy.android.service.helper.SelfAwareHelper.restartService(getApplicationContext());
        com.amazon.identity.auth.device.api.authorization.AuthorizationManager.signOut(getApplicationContext(), new com.amazon.identity.auth.device.api.Listener<Void, AuthError>() {
            @Override
            public void onError(AuthError authError) {
                if (FragmentApplications.this.DEBUG) {
                    MyLog.i(CLS_NAME, "signOut: onError");
                }
                FragmentApplications.this.toast(FragmentApplications.this.getString(R.string.menu_logged_out), Toast.LENGTH_SHORT);
                FragmentApplications.this.onAuthorizationStatusChange();
            }

            @Override
            public void onSuccess(Void v) {
                if (FragmentApplications.this.DEBUG) {
                    MyLog.i(CLS_NAME, "signOut: onSuccess");
                }
                FragmentApplications.this.toast(FragmentApplications.this.getString(R.string.menu_logged_out), Toast.LENGTH_SHORT);
                FragmentApplications.this.onAuthorizationStatusChange();
            }
        });
    }

    public void linkTwitter() {
        //todo add ActivityTwitterOAuth
//        ai.saiy.android.intent.ExecuteIntent.saiyActivity(getApplicationContext(), (Class<?>) ActivityTwitterOAuth.class, (Bundle) null, true);
    }

    public void linkFoursquare() {
        if (this.DEBUG) {
            MyLog.d(CLS_NAME, "ActivityFoursquareOAuth");
        }
    }

    public boolean isActive() {
        return getActivity() != null && getParentActivity().isActive() && isAdded() && !isRemoving();
    }

    public ActivityHome getParentActivity() {
        return (ActivityHome) getActivity();
    }

    public Context getApplicationContext() {
        return this.mContext;
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return this.mAdapter;
    }

    public ArrayList<ContainerUI> getObjects() {
        return this.mObjects;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.mContext = activity.getApplicationContext();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context.getApplicationContext();
    }

    @Override
    public void onClick(View view) {
        if (this.DEBUG) {
            MyLog.d(CLS_NAME, "onClick: " + view.getTag());
        }
        if (Global.isInVoiceTutorial()) {
            if (this.DEBUG) {
                MyLog.d(CLS_NAME, "onClick: tutorialActive");
            }
            toast(getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
            return;
        }
        switch (((Integer) view.getTag()).intValue()) {
            case 0:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_FACEBOOK);
                return;
            case 1:
                getParentActivity().vibrate();
                //todo?
            case 2:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_TWITTER);
                return;
            case 3:
                getParentActivity().vibrate();
                linkTwitter();
                return;
            case 4:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_ALEXA);
                return;
            case 5:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    toast(getString(R.string.title_error_alexa_android_5), Toast.LENGTH_SHORT);
                    return;
                }
                if (PermissionHelper.checkFilePermissions(getApplicationContext())) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ai.saiy.android.utils.UtilsFile.createDirs(FragmentApplications.this.getApplicationContext());
                        }
                    }).start();
                    if (TokenHelper.hasToken(getApplicationContext())) {
                        deauthoriseAlexa();
                        return;
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (FragmentApplications.this.DEBUG) {
                                    MyLog.i(CLS_NAME, "authAlexa: checking encryption provider");
                                }
                                try {
                                    try {
                                        try {
                                            ProviderInstaller.installIfNeeded(FragmentApplications.this.getApplicationContext());
                                            if (FragmentApplications.this.isActive()) {
                                                FragmentApplications.this.getParentActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (FragmentApplications.this.DEBUG) {
                                                            MyLog.i(CLS_NAME, "authAlexa: proceeding");
                                                        }
                                                        FragmentApplications.this.showAlexaRegionDialog();
                                                    }
                                                });
                                            }
                                        } catch (GooglePlayServicesNotAvailableException e) {
                                            if (FragmentApplications.this.DEBUG) {
                                                MyLog.e(CLS_NAME, "authAlexa: GooglePlayServicesNotAvailableException");
                                                e.printStackTrace();
                                            }
                                            if (FragmentApplications.this.isActive()) {
                                                FragmentApplications.this.getParentActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (FragmentApplications.this.DEBUG) {
                                                            MyLog.i(CLS_NAME, "authAlexa: showing play services notification");
                                                        }
                                                        com.google.android.gms.common.GoogleApiAvailability googleApiAvailability = com.google.android.gms.common.GoogleApiAvailability.getInstance();
                                                        googleApiAvailability.showErrorNotification(FragmentApplications.this.getApplicationContext(), googleApiAvailability.isGooglePlayServicesAvailable(FragmentApplications.this.getApplicationContext()));
                                                    }
                                                });
                                            }
                                        }
                                    } catch (GooglePlayServicesRepairableException e2) {
                                        if (FragmentApplications.this.DEBUG) {
                                            MyLog.e(CLS_NAME, "authAlexa: GooglePlayServicesRepairableException");
                                            e2.printStackTrace();
                                        }
                                        if (FragmentApplications.this.isActive()) {
                                            FragmentApplications.this.getParentActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (FragmentApplications.this.DEBUG) {
                                                        MyLog.i(CLS_NAME, "authAlexa: showing play services notification");
                                                    }
                                                    com.google.android.gms.common.GoogleApiAvailability googleApiAvailability = com.google.android.gms.common.GoogleApiAvailability.getInstance();
                                                    googleApiAvailability.showErrorNotification(FragmentApplications.this.getApplicationContext(), googleApiAvailability.isGooglePlayServicesAvailable(FragmentApplications.this.getApplicationContext()));
                                                }
                                            });
                                        }
                                    }
                                } catch (Throwable th) {
                                    if (FragmentApplications.this.isActive()) {
                                        FragmentApplications.this.getParentActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (FragmentApplications.this.DEBUG) {
                                                    MyLog.i(CLS_NAME, "authAlexa: showing play services notification");
                                                }
                                                com.google.android.gms.common.GoogleApiAvailability googleApiAvailability = com.google.android.gms.common.GoogleApiAvailability.getInstance();
                                                googleApiAvailability.showErrorNotification(FragmentApplications.this.getApplicationContext(), googleApiAvailability.isGooglePlayServicesAvailable(FragmentApplications.this.getApplicationContext()));
                                            }
                                        });
                                    }
                                    throw th;
                                }
                            }
                        }).start();
                        return;
                    }
                }
                return;
            case 6:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_TASKER_MARKET);
                return;
            case 7:
                if (PermissionHelper.checkFilePermissions(getApplicationContext())) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ai.saiy.android.utils.UtilsFile.createDirs(FragmentApplications.this.getApplicationContext());
                        }
                    }).start();
                    this.helper.checkTaskerInstallation();
                    return;
                }
                return;
            case 8:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_LINKED_IN);
                return;
            case 9:
                getParentActivity().vibrate();
                toast(getString(R.string.content_coming_soon_exclamation), Toast.LENGTH_SHORT);
                return;
            case 10:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_FOUR_SQUARED);
                return;
            case 11:
                getParentActivity().vibrate();
                linkFoursquare();
                return;
            case 12:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_WOLFRAM_ALPHA);
                return;
            case 13:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_SHAZAM);
                return;
            case 14:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_SOUND_HOUND);
                return;
            case 15:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_TRACK_ID);
                return;
            case 16:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_GOOGLE_SOUND_SEARCH);
                return;
            case 17:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_EBAY);
                return;
            case 18:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_AMAZON);
                return;
            case 19:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_SPOTIFY_MUSIC);
                return;
            case 20:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_DEEZER);
                return;
            case 21:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_GOOGLE_MUSIC);
                return;
            case 22:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_ASPIRO_TINDER);
                return;
            case 23:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_AMAZON_MUSIC);
                return;
            case 24:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_NETFLIX);
                return;
            case 25:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_IMDB);
                return;
            case 26:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_GOOGLE_YOUTUBE);
                return;
            case 27:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_GOOGLE_MAPS);
                return;
            case 28:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_GOOGLE_EARTH);
                return;
            case 29:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_GOOGLE_SKY);
                return;
            case 30:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_GOOGLE_TRANSLATE);
                return;
            case 31:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_YELP);
                return;
            case 32:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_UBER);
                return;
            case 33:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_DROPBOX);
                return;
            case 34:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_EVERNOTE);
                return;
            case 35:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_BOX);
                return;
            case 36:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_GOOGLE_DOCS);
                return;
            case 37:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_GOOGLE_KEEP);
                return;
            case 38:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_AUTOMATE);
                return;
            case 39:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_GOOGLE_STORE);
                return;
            default:
                break;
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (this.DEBUG) {
            MyLog.d(CLS_NAME, "onCreate");
        }
        this.helper = new FragmentApplicationsHelper(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (this.DEBUG) {
            MyLog.d(CLS_NAME, "onCreateView");
        }
        View inflate = layoutInflater.inflate(R.layout.layout_common_fragment_parent, viewGroup, false);
        this.mRecyclerView = this.helper.getRecyclerView(inflate);
        this.mObjects = new ArrayList<>();
        this.mAdapter = this.helper.getAdapter(this.mObjects);
        this.mRecyclerView.setAdapter(this.mAdapter);
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.DEBUG) {
            MyLog.d(CLS_NAME, "onDestroy");
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (Global.isInVoiceTutorial()) {
            if (this.DEBUG) {
                MyLog.d(CLS_NAME, "onClick: tutorialActive");
            }
            toast(getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
            return true;
        }

        if (this.DEBUG) {
            MyLog.d(CLS_NAME, "onLongClick: " + view.getTag());
        }
        int position = (Integer) view.getTag();
        switch (position) {
            case 1:
                getParentActivity().speak(R.string.lp_link_facebook, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 2:
            case 4:
            case 6:
            case 8:
            case 10:
            default:
                getParentActivity().speak(getString(R.string.lp_install_link, this.mObjects.get(position).getTitle()), LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 3:
                getParentActivity().speak(R.string.lp_twitter, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 5:
                getParentActivity().speak(R.string.lp_auth_alexa, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 7:
                getParentActivity().speak(R.string.lp_tasker, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 9:
                getParentActivity().speak(R.string.lp_linkedin, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 11:
                getParentActivity().speak(R.string.lp_foursquare, LocalRequest.ACTION_SPEAK_ONLY);
                break;
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.DEBUG) {
            MyLog.d(CLS_NAME, "onPause");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.DEBUG) {
            MyLog.d(CLS_NAME, "onResume: attemptingReinstallation: " + isAttemptingReinstallation());
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (FragmentApplications.this.isAttemptingReinstallation()) {
                    if (FragmentApplications.this.DEBUG) {
                        MyLog.i(CLS_NAME, "onResume: coming from unknown sources");
                    }
                    FragmentApplications.this.isAttemptingReinstallation = false;
                    if (!ai.saiy.android.thirdparty.tasker.TaskerHelper.isUnknownSourceInstallAllowed(FragmentApplications.this.getApplicationContext())) {
                        if (FragmentApplications.this.DEBUG) {
                            MyLog.i(CLS_NAME, "onResume: coming from unknown sources: user did not change");
                        }
                        ai.saiy.android.utils.SPH.setCheckUnknownSourcesSettingNeeded(FragmentApplications.this.getApplicationContext(), false);
                        return;
                    }
                    if (FragmentApplications.this.DEBUG) {
                        MyLog.i(CLS_NAME, "onResume: coming from unknown sources: user changed");
                    }
                    ai.saiy.android.utils.SPH.setCheckReinstallationNeeded(FragmentApplications.this.getApplicationContext(), true);
                    Locale vrLocale = ai.saiy.android.utils.SPH.getVRLocale(FragmentApplications.this.getApplicationContext());
                    SupportedLanguage supportedLanguage = SupportedLanguage.getSupportedLanguage(vrLocale);
                    ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(FragmentApplications.this.getApplicationContext(), supportedLanguage);
                    String utterance = sr.getString(R.string.content_tasker_reinstall_3);
                    sr.reset();
                    ai.saiy.android.service.helper.LocalRequest localRequest = new ai.saiy.android.service.helper.LocalRequest(FragmentApplications.this.getApplicationContext());
                    localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, supportedLanguage, vrLocale, ai.saiy.android.utils.SPH.getTTSLocale(FragmentApplications.this.getApplicationContext()), utterance);
                    localRequest.setCondition(Condition.CONDITION_CHECK_REINSTALLATION);
                    localRequest.execute();
                }
            }
        }).start();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.DEBUG) {
            MyLog.d(CLS_NAME, "onStart");
        }
        synchronized (lock) {
            if (this.mObjects.isEmpty()) {
                getParentActivity().setTitle(getString(R.string.title_supported_apps));
                this.helper.finaliseUI();
            }
        }
    }
}
