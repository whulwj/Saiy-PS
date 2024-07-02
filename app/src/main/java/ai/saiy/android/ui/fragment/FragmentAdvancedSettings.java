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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.command.settings.SettingsIntent;
import ai.saiy.android.command.unknown.Unknown;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.permissions.PermissionHelper;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.helper.FragmentAdvancedSettingsHelper;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Created by benrandall76@gmail.com on 18/07/2016.
 */

public class FragmentAdvancedSettings extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentAdvancedSettings.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;
    private ArrayList<ContainerUI> mObjects;
    private FragmentAdvancedSettingsHelper helper;

    private static final Object lock = new Object();

    private Context mContext;

    public FragmentAdvancedSettings() {
    }

    public static FragmentAdvancedSettings newInstance(@Nullable final Bundle args) {
        return new FragmentAdvancedSettings();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        helper = new FragmentAdvancedSettingsHelper(this);
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

    @Override
    public void onStart() {
        super.onStart();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStart");
        }

        synchronized (lock) {
            if (mObjects.isEmpty()) {
                getParentActivity().setTitle(getString(R.string.title_advanced));
                helper.finaliseUI();
            }
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateView");
        }

        final View rootView = inflater.inflate(R.layout.layout_common_fragment_parent, container, false);
        mRecyclerView = helper.getRecyclerView(rootView);
        mObjects = new ArrayList<>();
        mAdapter = helper.getAdapter(mObjects);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
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
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClick: " + view.getTag());
        }

        final int position = (int) view.getTag();

        switch (position) {

            case 0:
                final boolean isToastUnknown = SPH.getToastUnknown(getApplicationContext());
                if (!isToastUnknown && SPH.getCommandUnknownAction(getApplicationContext()) > Unknown.UNKNOWN_REPEAT) {
                    toast(getString(R.string.toast_unknown_error), Toast.LENGTH_LONG);
                    return;
                }
                getParentActivity().vibrate();
                SPH.setToastUnknown(getApplicationContext(), !isToastUnknown);
                mObjects.get(position).setIconExtra(SPH.getToastUnknown(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 1:
                getParentActivity().vibrate();
                SPH.setVibrateCondition(getApplicationContext(), !SPH.getVibrateCondition(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getVibrateCondition(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 2:
                getParentActivity().vibrate();
                SPH.setUseOffline(getApplicationContext(), !SPH.getUseOffline(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getUseOffline(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 3:
                //noinspection NewApi
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        && !PermissionHelper.checkUsageStatsPermission(getApplicationContext())) {
                    if (ExecuteIntent.settingsIntent(getApplicationContext(), IntentConstants.SETTINGS_USAGE_STATS)) {
                        getParentActivity().speak(R.string.app_speech_usage_stats, LocalRequest.ACTION_SPEAK_ONLY);
                    } else {
                        getParentActivity().speak(R.string.issue_usage_stats_bug, LocalRequest.ACTION_SPEAK_ONLY);
                    }
                } else {
                    helper.showHotwordSelector();
                }
                break;
            case 4:
                helper.showGenderSelector();
                break;
            case 5:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        && !PermissionHelper.checkUsageStatsPermission(getApplicationContext())) {
                    if (ExecuteIntent.settingsIntent(getApplicationContext(), IntentConstants.SETTINGS_USAGE_STATS)) {
                        getParentActivity().speak(R.string.app_speech_usage_stats, LocalRequest.ACTION_SPEAK_ONLY);
                    } else {
                        getParentActivity().speak(R.string.issue_usage_stats_bug, LocalRequest.ACTION_SPEAK_ONLY);
                    }
                } else {
                    helper.showDrivingProfileSelector();
                }
            case 6:
                helper.showQuietTimesDialog();
                break;
            case 7:
                helper.showDOBDialog();
                break;
            case 8:
                if (!SPH.getShownPauseBug(getApplicationContext())) {
                    SPH.setShownPauseBug(getApplicationContext(), true);
                    toast(getString(R.string.content_android_bug), Toast.LENGTH_LONG);
                }
                helper.showPauseDetectionSlider();
                break;
            case 9:
                getParentActivity().vibrate();
                SPH.setCallConfirmation(getApplicationContext(), !SPH.getCallConfirmation(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getCallConfirmation(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 10:
                if (SPH.announceCallerStats(getApplicationContext())) {
                    SPH.setAnnounceCaller(getApplicationContext(), false);
                    this.mObjects.get(position).setIconExtra(FragmentHome.UNCHECKED);
                    this.mAdapter.notifyItemChanged(position);
                    return;
                } else if (!PermissionHelper.checkNotificationPolicyPermission(getApplicationContext())) {
                    ExecuteIntent.settingsIntent(getApplicationContext(), IntentConstants.NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    getParentActivity().speak(R.string.app_speech_notification_policy, LocalRequest.ACTION_SPEAK_ONLY);
                    break;
                } else {
                    if (PermissionHelper.checkReadCallerPermissions(getApplicationContext())) {
                        SPH.setAnnounceCaller(getApplicationContext(), true);
                        this.mObjects.get(position).setIconExtra(FragmentHome.CHECKED);
                        this.mAdapter.notifyItemChanged(position);
                    }
                }
                break;
            case 11:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    helper.showAnnounceNotificationsDialog();
                } else if (SPH.getAccessibilityChange(getApplicationContext()) || !ai.saiy.android.service.helper.SelfAwareHelper.saiyAccessibilityRunning(getApplicationContext())) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean isNotificationListenerEnabled = false;
                            for (String s : NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext())) {
                                if (s.equals(getApplicationContext().getPackageName())) {
                                    isNotificationListenerEnabled = true;
                                    break;
                                }
                            }
                            if (isNotificationListenerEnabled) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "notification listener service running");
                                }
                                getParentActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        helper.showAnnounceNotificationsDialog();
                                    }
                                });
                            } else {
                                if (SettingsIntent.settingsIntent(getApplicationContext(), SettingsIntent.Type.NOTIFICATION_ACCESS)) {
                                    getParentActivity().speak(R.string.notifications_enable, LocalRequest.ACTION_SPEAK_ONLY);
                                    return;
                                }
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "notification listener: settings location unknown");
                                }
                                ai.saiy.android.applications.UtilsApplication.openApplicationSpecificSettings(getApplicationContext(), getApplicationContext().getPackageName());
                                getParentActivity().speak(getString(R.string.settings_missing, getString(R.string.notification_access)), LocalRequest.ACTION_SPEAK_ONLY);
                            }
                        }
                    }).start();
                } else {
                    SPH.setAccessibilityChange(getApplicationContext());
                    helper.showAccessibilityChangeDialog();
                }
                break;
            case 12:
                helper.showSignatureDialog();
                break;
            case 13:
                if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_DIAGNOSTICS))) {
                    getParentActivity().doFragmentAddTransaction(FragmentDiagnostics.newInstance(null), String.valueOf(ActivityHome.INDEX_FRAGMENT_DIAGNOSTICS), ActivityHome.ANIMATION_FADE, ActivityHome.MENU_INDEX_ADVANCED_SETTINGS);
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "onClick: INDEX_FRAGMENT_DIAGNOSTICS being added");
                }
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
                getParentActivity().speak(R.string.lp_toast_unknown, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 1:
                getParentActivity().speak(R.string.lp_haptic_feedback, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 2:
                getParentActivity().speak(R.string.lp_offline_recognition, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 3:
                getParentActivity().speak(R.string.lp_hotword_detection, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 4:
                getParentActivity().speak(R.string.lp_tts_gender, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 5:
                getParentActivity().speak(R.string.lp_driving_profile, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 6:
                getParentActivity().speak(R.string.lp_quiet_times, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 7:
                getParentActivity().speak(R.string.lp_dob, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 8:
                getParentActivity().speak(R.string.lp_pause_timeout, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 9:
                getParentActivity().speak(R.string.lp_confirm_before_calling, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 10:
                getParentActivity().speak(R.string.lp_announce_caller, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 11:
                getParentActivity().speak(R.string.lp_announce_notifications, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 12:
                getParentActivity().speak(R.string.lp_signature, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 13:
                getParentActivity().speak(R.string.lp_diagnostics, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            default:
                break;
        }

        return true;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        helper.onActivityResult(requestCode, resultCode, data);
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
