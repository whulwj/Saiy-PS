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
import com.google.firebase.analytics.FirebaseAnalytics;

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
import ai.saiy.android.firebase.helper.UtilsAnalytic;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.permissions.PermissionHelper;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityFoursquareOAuth;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.activity.ActivityTwitterOAuth;
import ai.saiy.android.ui.components.UIApplicationsAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.helper.FragmentApplicationsHelper;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;

public class FragmentApplications extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    private static final Object lock = new Object();

    private static final boolean DEBUG = MyLog.DEBUG;
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
                    mObjects.get(5).setSubtitle(TokenHelper.hasToken(getApplicationContext()) ? getString(R.string.menu_tap_deauthorise) : getString(R.string.menu_tap_authorise));
                    mAdapter.notifyItemChanged(5);
                }
            });
        }
    }

    public void toast(String text, int duration) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "makeToast: " + text);
        }
        if (isActive()) {
            getParentActivity().toast(text, duration);
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "toast Fragment detached");
        }
    }

    public void setAttemptingReinstallation(boolean isAttempting) {
        this.isAttemptingReinstallation = isAttempting;
    }

    public boolean isAttemptingReinstallation() {
        return isAttemptingReinstallation;
    }

    public void showAlexaRegionDialog() {
        int checkedItem;
        int alexaRegion = ai.saiy.android.utils.SPH.getAlexaRegion(getApplicationContext(), UtilsNetwork.ALEXA_NORTH_AMERICA);
        if (DEBUG) {
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
                    }
                })
                .setPositiveButton(R.string.menu_select, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog instanceof AlertDialog) {
                            final int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "showAlexaRegionDialog: onPositive: " + position);
                            }
                            ai.saiy.android.utils.SPH.setAlexaRegion(getApplicationContext(), position);
                            showAlexaOverviewDialog();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAlexaRegionDialog: onNegative");
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAlexaRegionDialog: onCancel");
                        }
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
        if (DEBUG) {
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
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAlexaOverviewDialog: onCancel");
                        }
                    }
                }).create();

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
        view.findViewById(R.id.ibLoginWithAmazon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "ibLoginWithAmazon: onClick");
                }
                materialDialog.dismiss();
                authoriseAlexa();
            }
        });
    }

    public void authoriseAlexa() {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "authoriseAlexa");
        }
        showProgress(true);
        UtilsAnalytic.alexaAuthorised(getApplicationContext(), FirebaseAnalytics.getInstance(getApplicationContext()));
        new AuthorizationWrapper(getApplicationContext()).authoriseUser(new AuthorizationListener() {
            @Override
            public void onSuccess() {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onSuccess");
                }
                toast(getString(R.string.success_), Toast.LENGTH_SHORT);
                onAuthorizationStatusChange();
                UtilsAnalytic.alexaAuthSuccess(getApplicationContext(), FirebaseAnalytics.getInstance(getApplicationContext()));
            }

            @Override
            public void onError(Exception e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "onError");
                    e.printStackTrace();
                }
                toast(getString(R.string.failed), Toast.LENGTH_SHORT);
                showProgress(false);
                UtilsAnalytic.alexaAuthError(getApplicationContext(), FirebaseAnalytics.getInstance(getApplicationContext()));
            }

            @Override
            public void onCancel() {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onCancel");
                }
                showProgress(false);
            }
        });
    }

    public void signOutAlexa() {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "signOutAlexa");
        }
        showProgress(true);
        ai.saiy.android.utils.SPH.setAlexaAccessToken(getApplicationContext(), null);
        ai.saiy.android.utils.SPH.setAlexaRefreshToken(getApplicationContext(), null);
        ai.saiy.android.utils.SPH.setAlexaAccessTokenExpiry(getApplicationContext(), 0L);
        ai.saiy.android.utils.SPH.setAlexaNotification(getApplicationContext(), false);
        ai.saiy.android.service.helper.SelfAwareHelper.restartService(getApplicationContext());
        UtilsAnalytic.signOutAlexa(getApplicationContext(), FirebaseAnalytics.getInstance(getApplicationContext()));
        com.amazon.identity.auth.device.api.authorization.AuthorizationManager.signOut(getApplicationContext(), new com.amazon.identity.auth.device.api.Listener<Void, AuthError>() {
            @Override
            public void onError(AuthError authError) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "signOut: onError");
                }
                toast(getString(R.string.menu_logged_out), Toast.LENGTH_SHORT);
                onAuthorizationStatusChange();
            }

            @Override
            public void onSuccess(Void v) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "signOut: onSuccess");
                }
                toast(getString(R.string.menu_logged_out), Toast.LENGTH_SHORT);
                onAuthorizationStatusChange();
            }
        });
    }

    public void linkTwitter() {
        ai.saiy.android.intent.ExecuteIntent.saiyActivity(getApplicationContext(), ActivityTwitterOAuth.class, null, true);
    }

    public void linkFoursquare() {
        ai.saiy.android.intent.ExecuteIntent.saiyActivity(getApplicationContext(), ActivityFoursquareOAuth.class, null, true);
    }

    public boolean isActive() {
        return getActivity() != null && getParentActivity().isActive() && isAdded() && !isRemoving();
    }

    public ActivityHome getParentActivity() {
        return (ActivityHome) getActivity();
    }

    public Context getApplicationContext() {
        return mContext;
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return mAdapter;
    }

    public ArrayList<ContainerUI> getObjects() {
        return mObjects;
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

    private int getPosition(View view) {
        int position = (view == null) ? 0 : mRecyclerView.getChildAdapterPosition(view);
        if (view != null && RecyclerView.NO_POSITION == position) {
            final RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(view);
            if (viewHolder instanceof UIApplicationsAdapter.ViewHolder) {
                position = ((UIApplicationsAdapter.ViewHolder) viewHolder).getBoundPosition();
            }
        }
        return position;
    }

    @Override
    public void onClick(View view) {
        if (Global.isInVoiceTutorial()) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "onClick: tutorialActive");
            }
            toast(getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
            return;
        }

        final int position = getPosition(view);
        if (DEBUG) {
            MyLog.d(CLS_NAME, "onClick: " + position);
        }
        switch (position) {
            case 0:
                Install.showInstallLink(getApplicationContext(), Installed.PACKAGE_FACEBOOK);
                return;
            case 1:
                getParentActivity().vibrate();
                toast("Awaiting Facebook approval review", Toast.LENGTH_SHORT);
                //todo link facebook
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
                            ai.saiy.android.utils.UtilsFile.createDirs(getApplicationContext());
                        }
                    }).start();
                    if (TokenHelper.hasToken(getApplicationContext())) {
                        signOutAlexa();
                        return;
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "authAlexa: checking encryption provider");
                                }
                                try {
                                    ProviderInstaller.installIfNeeded(getApplicationContext());
                                    if (isActive()) {
                                        getParentActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "authAlexa: proceeding");
                                                }
                                                showAlexaRegionDialog();
                                            }
                                        });
                                    }
                                } catch (GooglePlayServicesNotAvailableException e) {
                                    if (DEBUG) {
                                        MyLog.e(CLS_NAME, "authAlexa: GooglePlayServicesNotAvailableException");
                                        e.printStackTrace();
                                    }
                                    if (isActive()) {
                                        showErrorNotification();
                                    }
                                } catch (GooglePlayServicesRepairableException e) {
                                    if (DEBUG) {
                                        MyLog.e(CLS_NAME, "authAlexa: GooglePlayServicesRepairableException");
                                        e.printStackTrace();
                                    }
                                    if (isActive()) {
                                        showErrorNotification();
                                    }
                                } catch (Throwable th) {
                                    if (isActive()) {
                                        showErrorNotification();
                                    } else if (DEBUG) {
                                        MyLog.w(CLS_NAME, "authAlexa: showing play services:" + th);
                                    }
                                }
                            }

                            private void showErrorNotification() {
                                getParentActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "authAlexa: showing play services notification");
                                        }
                                        com.google.android.gms.common.GoogleApiAvailability googleApiAvailability = com.google.android.gms.common.GoogleApiAvailability.getInstance();
                                        googleApiAvailability.showErrorNotification(getApplicationContext(), googleApiAvailability.isGooglePlayServicesAvailable(getApplicationContext()));
                                    }
                                });
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
                            ai.saiy.android.utils.UtilsFile.createDirs(getApplicationContext());
                        }
                    }).start();
                    helper.checkTaskerInstallation();
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
        if (DEBUG) {
            MyLog.d(CLS_NAME, "onCreate");
        }
        this.helper = new FragmentApplicationsHelper(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "onCreateView");
        }
        View inflate = layoutInflater.inflate(R.layout.layout_common_fragment_parent, viewGroup, false);
        this.mRecyclerView = helper.getRecyclerView(inflate);
        this.mObjects = new ArrayList<>();
        this.mAdapter = helper.getAdapter(mObjects);
        this.mRecyclerView.setAdapter(mAdapter);
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.d(CLS_NAME, "onDestroy");
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (Global.isInVoiceTutorial()) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "onLongClick: tutorialActive");
            }
            toast(getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
            return true;
        }

        if (DEBUG) {
            MyLog.d(CLS_NAME, "onLongClick: " + view.getTag());
        }
        final int position = getPosition(view);
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
                getParentActivity().speak(getString(R.string.lp_install_link, mObjects.get(position).getTitle()), LocalRequest.ACTION_SPEAK_ONLY);
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
        if (DEBUG) {
            MyLog.d(CLS_NAME, "onPause");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) {
            MyLog.d(CLS_NAME, "onResume: attemptingReinstallation: " + isAttemptingReinstallation());
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isAttemptingReinstallation()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onResume: coming from unknown sources");
                    }
                    FragmentApplications.this.isAttemptingReinstallation = false;
                    if (!ai.saiy.android.thirdparty.tasker.TaskerHelper.isUnknownSourceInstallAllowed(getApplicationContext())) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onResume: coming from unknown sources: user did not change");
                        }
                        ai.saiy.android.utils.SPH.setCheckUnknownSourcesSettingNeeded(getApplicationContext(), false);
                        return;
                    }
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onResume: coming from unknown sources: user changed");
                    }
                    ai.saiy.android.utils.SPH.setCheckReinstallationNeeded(getApplicationContext(), true);
                    Locale vrLocale = ai.saiy.android.utils.SPH.getVRLocale(getApplicationContext());
                    SupportedLanguage supportedLanguage = SupportedLanguage.getSupportedLanguage(vrLocale);
                    ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(getApplicationContext(), supportedLanguage);
                    String utterance = sr.getString(R.string.content_tasker_reinstall_3);
                    sr.reset();
                    ai.saiy.android.service.helper.LocalRequest localRequest = new ai.saiy.android.service.helper.LocalRequest(getApplicationContext());
                    localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, supportedLanguage, vrLocale, ai.saiy.android.utils.SPH.getTTSLocale(getApplicationContext()), utterance);
                    localRequest.setCondition(Condition.CONDITION_CHECK_REINSTALLATION);
                    localRequest.execute();
                }
            }
        }).start();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (DEBUG) {
            MyLog.d(CLS_NAME, "onStart");
        }
        synchronized (lock) {
            if (mObjects.isEmpty()) {
                getParentActivity().setTitle(getString(R.string.title_supported_apps));
                helper.finaliseUI();
            }
        }
    }
}
