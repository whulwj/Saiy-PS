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

package ai.saiy.android.ui.fragment;

import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.settings.SettingsIntent;
import ai.saiy.android.firebase.helper.UtilsAnalytic;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.sound.VolumeHelper;
import ai.saiy.android.tutorial.Tutorial;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.helper.FragmentHomeHelper;
import ai.saiy.android.ui.notification.NotificationHelper;
import ai.saiy.android.ui.viewmodel.BillingViewModel;
import ai.saiy.android.utils.Conditions.Network;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * Created by benrandall76@gmail.com on 18/07/2016.
 */
@AndroidEntryPoint
public class FragmentHome extends Fragment implements View.OnClickListener, View.OnLongClickListener, Observer<BillingResult> {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentHome.class.getSimpleName();

    /**
     * This seemingly, arbitrary delay is to allow the animation of the drawer closing to complete
     * prior to doing any heavy UI work, which can cause the drawer to stutter. The consequence of this,
     * is the appearance of a blank fragment, albeit momentarily, negated somewhat by the fade-in animation.
     * Regardless, I would consider it more preferable than visual lag?
     */
    public static final long DRAWER_CLOSE_DELAY = 200L;

    public static final int CHECKED = R.drawable.ic_toggle_switch_on;
    public static final int UNCHECKED = R.drawable.ic_toggle_switch_off;
    public static final int CHEVRON = R.drawable.chevron;
    private static final int SYSTEM_OVERLAY_REQUEST_CODE = 132;
    private static final int RC_REQUEST = 1287;
    public static final int ACCOUNT_PICKER_REQUEST_CODE = 3773;
    private static final int REQUEST_AUDIO = 1;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;
    private ArrayList<ContainerUI> mObjects;
    private FragmentHomeHelper helper;
    private BillingViewModel billingViewModel;

    private static final Object lock = new Object();

    private Context mContext;

    public FragmentHome() {
    }

    public static FragmentHome newInstance(@Nullable final Bundle args) {
        return new FragmentHome();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        helper = new FragmentHomeHelper(this);
    }

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);
        this.mContext = context.getApplicationContext();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(@NonNull final Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.mContext = activity.getApplicationContext();
        }
    }

    /**
     * The RecyclerView in all fragments is initialised with an empty adapter. We start any heavy lifting
     * here to ensure that the transition between any fragments does not cause any visual lag (stuttering).
     * <p>
     * As our fragments don't contain any dynamic content, we only need to check if the adapter content is
     * empty, to know this is the first time we've received this callback. We synchronise the process, just
     * to avoid any weird and wonderful situations that never happen....
     */
    @Override
    public void onStart() {
        super.onStart();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStart");
        }

        synchronized (lock) {
            if (mObjects.isEmpty()) {
                getParentActivity().setTitle(getString(R.string.title_home));
                helper.finaliseUI();
            }
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateView");
        }
        final ViewModelProvider viewModelProvider = new ViewModelProvider(getActivity());
        this.billingViewModel = viewModelProvider.get(BillingViewModel.class);

        final View rootView = inflater.inflate(R.layout.layout_common_fragment_parent, container, false);
        mRecyclerView = helper.getRecyclerView(rootView);
        mObjects = new ArrayList<>();
        mAdapter = helper.getAdapter(mObjects);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        billingViewModel.getProductDetailsResult().observe(getViewLifecycleOwner(), this);
    }

    @Override
    public void onClick(final View view) {
        if (Global.isInVoiceTutorial()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME,  "onClick: tutorialActive");
            }
            getParentActivity().toast(getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
            return;
        }
        final int position = (view == null) ? 0 : (int) view.getTag();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClick: " + position);
        }
        switch (position) {
            case 0:
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        ai.saiy.android.service.helper.LocalRequest localRequest = new ai.saiy.android.service.helper.LocalRequest(FragmentHome.this.getApplicationContext());
                        localRequest.prepareDefault(LocalRequest.ACTION_STOP_HOTWORD, null);
                        localRequest.setShutdownHotword();
                        localRequest.execute();
                        Pair<Boolean, Boolean> permissions = ai.saiy.android.permissions.PermissionHelper.checkTutorialPermissions(getApplicationContext());
                        if (permissions.first && permissions.second) {
                            startTutorial();
                        } else if (permissions.first) {
                            FragmentHome.this.requestSystemAlertPermissions();
                        } else {
                            FragmentHome.this.requestAudioPermissions();
                        }
                    }
                });

                break;
            case 1:
                helper.showUserGuideDialog();
                break;
            case 2:
                if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_COMMANDS))) {
                    getParentActivity().doFragmentReplaceTransaction(FragmentCommands.newInstance(null), String.valueOf(ActivityHome.INDEX_FRAGMENT_COMMANDS), ActivityHome.ANIMATION_FADE);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onClick: INDEX_FRAGMENT_COMMANDS being added");
                    }
                }
                break;
            case 3:
                if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_DEVELOPMENT))) {
                    getParentActivity().doFragmentReplaceTransaction(FragmentDevelopment.newInstance(null), String.valueOf(ActivityHome.INDEX_FRAGMENT_DEVELOPMENT), ActivityHome.ANIMATION_FADE);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onClick: INDEX_FRAGMENT_DEVELOPMENT being added");
                    }
                }
                break;
            case 4:
                if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_SETTINGS))) {
                    getParentActivity().doFragmentReplaceTransaction(FragmentSettings.newInstance(null), String.valueOf(ActivityHome.INDEX_FRAGMENT_SETTINGS), ActivityHome.ANIMATION_FADE);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onClick: INDEX_FRAGMENT_SETTINGS being added");
                    }
                }
                break;
            case 5:
                if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_CUSTOMISATION))) {
                    getParentActivity().doFragmentReplaceTransaction(FragmentCustomisation.newInstance(null), String.valueOf(ActivityHome.INDEX_FRAGMENT_CUSTOMISATION), ActivityHome.ANIMATION_FADE);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onClick: INDEX_FRAGMENT_CUSTOMISATION being added");
                    }
                }
                break;
            case 6:
                if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_ADVANCED_SETTINGS))) {
                    getParentActivity().doFragmentReplaceTransaction(FragmentAdvancedSettings.newInstance(null), String.valueOf(ActivityHome.INDEX_FRAGMENT_ADVANCED_SETTINGS), ActivityHome.ANIMATION_FADE);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onClick: INDEX_FRAGMENT_ADVANCED_SETTINGS being added");
                    }
                }
                break;
            case 7:
                if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_BUGS))) {
                    getParentActivity().doFragmentReplaceTransaction(FragmentBugs.newInstance(null), String.valueOf(ActivityHome.INDEX_FRAGMENT_BUGS), ActivityHome.ANIMATION_FADE);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onClick: INDEX_FRAGMENT_BUGS being added");
                    }
                }
                break;
            case 8:
                if (isActive()) {
                    showProgress(true);
                } else if (DEBUG) {
                    MyLog.i(CLS_NAME, "onClick: fragment detached");
                }
                final com.google.android.gms.common.GoogleApiAvailability googleApiAvailability = com.google.android.gms.common.GoogleApiAvailability.getInstance();
                final int connectionResult = googleApiAvailability.isGooglePlayServicesAvailable(getApplicationContext());
                if (connectionResult == ConnectionResult.SUCCESS) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "GoogleApiAvailability: SUCCESS");
                    }
                    if (Network.isNetworkAvailable(getApplicationContext())) {
                        helper.showAccountPicker();
                        return;
                    }
                    if (isActive()) {
                        toast(getString(R.string.network_error), Toast.LENGTH_SHORT);
                        showProgress(false);
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "onClick: fragment detached");
                        }
                    }
                    return;
                }
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "GoogleApiAvailability: play services unavailable");
                }
                if (isActive()) {
                    showProgress(false);
                } else if (DEBUG) {
                    MyLog.i(CLS_NAME, "onClick: fragment detached");
                }
                googleApiAvailability.showErrorNotification(getApplicationContext(), connectionResult);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(final View view) {
        if (Global.isInVoiceTutorial()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME,  "onLongClick: tutorialActive");
            }
            getParentActivity().toast(getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
            return true;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onLongClick: " + view.getTag());
        }

        getParentActivity().toast("long press!", Toast.LENGTH_SHORT);

        final int position = (int) view.getTag();
        switch (position) {
            case 0:
                getParentActivity().speak(R.string.lp_tutorial, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 1:
                getParentActivity().speak(R.string.lp_user_guide, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 2:
                getParentActivity().speak(R.string.lp_commands, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 3:
                getParentActivity().speak(R.string.lp_development, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 4:
                getParentActivity().speak(R.string.lp_settings, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 5:
                getParentActivity().speak(R.string.lp_customisation, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 6:
                getParentActivity().speak(R.string.lp_advanced, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 7:
                getParentActivity().speak(R.string.lp_bugs, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 8:
                getParentActivity().speak(R.string.lp_donate, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            default:
                break;
        }

        return true;
    }

    private void createPermissionsNotification(int permissionId) {
        NotificationHelper.createPermissionsNotification(getApplicationContext(), permissionId);
    }

    private boolean arePermissionsGranted(int[] grantResults) {
        for (int i : grantResults) {
            if (i != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void startTutorial() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startTutorial");
        }
        Pair<Boolean, Boolean> volumeStatus = getVolumeStatus();
        if (volumeStatus.first) {
            if (isActive()) {
                getParentActivity().toast(getString(R.string.tutorial_volume_error), Toast.LENGTH_LONG);
                if (volumeStatus.second) {
                    SettingsIntent.settingsIntent(getApplicationContext(), SettingsIntent.Type.SOUND);
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "startTutorial: no longer active");
                }
                return;
            }
        }
        if (!tutorialNetworkProceed()) {
            if (isActive()) {
                getParentActivity().toast(getString(R.string.tutorial_no_network), Toast.LENGTH_SHORT);
            } else if (DEBUG) {
                MyLog.i(CLS_NAME, "startTutorial: no longer active");
            }
        } else if (isActive()) {
            Global.setVoiceTutorialState(getApplicationContext(), true);
            getParentActivity().runOnUiThread(new Runnable() {
                public void run() {
                    FragmentHome.this.getParentActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });
            Bundle bundle = new Bundle();
            bundle.putInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_TUTORIAL);
            ai.saiy.android.service.helper.SelfAwareHelper.startServiceWithIntent(getApplicationContext(), bundle);
            Locale vrLocale = SPH.getVRLocale(getApplicationContext());
            SupportedLanguage supportedLanguage = SupportedLanguage.getSupportedLanguage(vrLocale);
            bundle = new Bundle();
            bundle.putInt(LocalRequest.EXTRA_TUTORIAL_STAGE, Tutorial.STAGE_INTRO);
            new Tutorial(getApplicationContext(), vrLocale, SPH.getTTSLocale(getApplicationContext()), supportedLanguage, bundle).execute();
            UtilsAnalytic.tutorialStarted(getApplicationContext(), FirebaseAnalytics.getInstance(getApplicationContext()));
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "startTutorial: no longer active");
        }
    }

    private boolean tutorialNetworkProceed() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "tutorialNetworkProceed: " + Network.isNetworkAvailable(getApplicationContext()));
        }
        return Network.isNetworkAvailable(getApplicationContext());
    }

    private Pair<Boolean, Boolean> getVolumeStatus() {
        boolean isInaudible = true;
        boolean isMediaVocal = VolumeHelper.getMediaVolumePercentage(getApplicationContext()) > 25;
        boolean volumeProfileEnabled = VolumeHelper.volumeProfileEnabled(getApplicationContext());
        Boolean isMute = !isMediaVocal || !volumeProfileEnabled;
        if (isMediaVocal || volumeProfileEnabled) {
            isInaudible = false;
        }
        return new Pair<>(isMute, isInaudible);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestSystemAlertPermissions() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getApplicationContext().getPackageName()));
        if (!isActive()) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "requestSystemAlertPermissions: no longer active");
            }
            return;
        }
        try {
            startActivityForResult(intent, SYSTEM_OVERLAY_REQUEST_CODE);
            return;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "requestSystemAlertPermissions: ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "requestSystemAlertPermissions: Exception");
                e.printStackTrace();
            }
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "requestSystemAlertPermissions: settings location unknown");
        }
        ExecuteIntent.openApplicationSpecificSettings(getApplicationContext(), getApplicationContext().getPackageName());
        getParentActivity().speak(getString(R.string.settings_missing, getString(R.string.content_system_overlays)), LocalRequest.ACTION_SPEAK_ONLY);
    }

    private void requestAudioPermissions() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "requestAudioPermissions");
        }
        final String[] strArr = {android.Manifest.permission.RECORD_AUDIO};
        if (ActivityCompat.shouldShowRequestPermissionRationale(getParentActivity(), android.Manifest.permission.RECORD_AUDIO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "requestAudioPermissions: showing rational");
            }
            getParentActivity().snack(this.getView(), getString(R.string.permission_audio_snack), Snackbar.LENGTH_INDEFINITE, getString(R.string.ok), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "requestAudioPermissions: on snack click");
                    }
                    ActivityCompat.requestPermissions(getParentActivity(), strArr, REQUEST_AUDIO);
                }
            });
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "requestAudioPermissions: requesting");
            }
            ActivityCompat.requestPermissions(getParentActivity(), strArr, REQUEST_AUDIO);
        }
    }

    private void startPurchaseFlow() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startPurchaseFlow");
        }
        if (!billingViewModel.queryProductDetails()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "startPurchaseFlow: mBillingClient null");
            }
            if (isActive()) {
                showProgress(false);
                toast(getString(R.string.iap_error_generic), Toast.LENGTH_LONG);
            }
        }
    }

    private void showSystemAlertRational() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showSystemAlertRational");
        }
        if (isActive()) {
            getParentActivity().snack(this.getView(), getString(R.string.permission_system_alert_snack), Snackbar.LENGTH_INDEFINITE, getString(R.string.ok), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "requestAudioPermissions: on snack click");
                    }
                    FragmentHome.this.requestSystemAlertPermissions();
                }
            });
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "requestSystemAlertPermissions: no longer active");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case SYSTEM_OVERLAY_REQUEST_CODE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: SYSTEM_OVERLAY_REQUEST_CODE");
                }
                if (isActive()) {
                    showProgress(true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !Settings.canDrawOverlays(getApplicationContext())) {
                                FragmentHome.this.showSystemAlertRational();
                            } else {
                                FragmentHome.this.startTutorial();
                            }
                            if (isActive()) {
                                showProgress(false);
                            } else if (DEBUG) {
                                MyLog.i(CLS_NAME, "onActivityResult: SYSTEM_OVERLAY_REQUEST_CODE: fragment detached");
                            }
                        }
                    }, 2000L);
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onActivityResult: SYSTEM_OVERLAY_REQUEST_CODE: fragment detached");
                    }
                }
                break;
            case RC_REQUEST:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: RC_REQUEST");
                }
                break;
            case ACCOUNT_PICKER_REQUEST_CODE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: ACCOUNT_PICKER_REQUEST_CODE");
                }
                if (!isActive()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onActivityResult: ACCOUNT_PICKER_REQUEST_CODE: user exiting");
                    }
                    showProgress(false);
                    return;
                }
                if (intent == null || !intent.hasExtra(AccountManager.KEY_ACCOUNT_NAME)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onActivityResult: accountName missing");
                    }
                    showProgress(false);
                    return;
                }
                final String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                if (!ai.saiy.android.utils.UtilsString.notNaked(accountName)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onActivityResult: accountName null");
                    }
                    showProgress(false);
                    return;
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onActivityResult: accountName " + accountName);
                }
                SPH.setUserAccount(getApplicationContext(), accountName);
                if (billingViewModel.canBillingProceed()) {
                    startPurchaseFlow();
                    return;
                }
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onActivityResult: canBillingProceed: false");
                }
                showProgress(false);
                toast(getString(R.string.iap_error_generic), Toast.LENGTH_LONG);
                break;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onActivityResult: DEFAULT");
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onRequestPermissionsResult");
        }
        switch (requestCode) {
            case REQUEST_AUDIO:
                if (!arePermissionsGranted(grantResults)) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onRequestPermissionsResult: REQUEST_AUDIO: PERMISSION_DENIED");
                    }
                    createPermissionsNotification(requestCode);
                    return;
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onRequestPermissionsResult: REQUEST_AUDIO: PERMISSION_GRANTED");
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(getApplicationContext())) {
                    startTutorial();
                } else {
                    requestSystemAlertPermissions();
                }
                return;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onRequestPermissionsResult: Unknown request?");
                }
        }
    }

    @Override
    public void onChanged(BillingResult billingResult) {
        if (!isActive()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onProductDetailsResponse: !isActive()");
            }
            return;
        }
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            final @Nullable List<ProductDetails> list = billingViewModel.getProductDetailsList().getValue();
            if (!ai.saiy.android.utils.UtilsList.notNaked(list)) {
                if (isActive()) {
                    showProgress(false);
                    toast(getString(R.string.iap_error_generic), Toast.LENGTH_LONG);
                }
                return;
            }

            helper.showPremiumDialog(list);
        } else {
            showProgress(false);
            toast(getString(R.string.iap_error_generic), Toast.LENGTH_LONG);
        }
    }

    public void toast(String text, int duration) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "makeToast: " + text);
        }
        if (isActive()) {
            getParentActivity().toast(text, duration);
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "toast Fragment detached");
        }
    }

    private void showProgress(boolean visible) {
        if (isActive()) {
            getParentActivity().showProgress(visible);
        }
    }

    public boolean isActive() {
        return getActivity() != null && getParentActivity().isActive() && isAdded() && !isRemoving();
    }

    /**
     * Utility to return the parent activity neatly cast. No need for instanceOf as this fragment will
     * never be attached to another activity.
     *
     * @return the {@link ActivityHome} parent
     */
    public ActivityHome getParentActivity() {
        return (ActivityHome) getActivity();
    }

    /**
     * Utility method to ensure we double check the context being used.
     *
     * @return the application context
     */
    public Context getApplicationContext() {
        return this.mContext;
    }

    /**
     * Get the current adapter
     *
     * @return the current adapter
     */
    public RecyclerView.Adapter<?> getAdapter() {
        return mAdapter;
    }

    /**
     * Get the current objects in the adapter
     *
     * @return the current objects
     */
    public ArrayList<ContainerUI> getObjects() {
        return mObjects;
    }

    public BillingViewModel getBillingViewModel() {
        return billingViewModel;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPause");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onResume");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
    }
}
