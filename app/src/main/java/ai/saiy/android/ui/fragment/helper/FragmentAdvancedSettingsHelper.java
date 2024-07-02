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

package ai.saiy.android.ui.fragment.helper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ai.saiy.android.R;
import ai.saiy.android.accessibility.BlockedApplications;
import ai.saiy.android.accessibility.BlockedApplicationsHelper;
import ai.saiy.android.applications.ApplicationBasic;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.command.driving.DrivingProfile;
import ai.saiy.android.command.driving.DrivingProfileHelper;
import ai.saiy.android.command.horoscope.HoroscopeHelper;
import ai.saiy.android.command.settings.SettingsIntent;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.recognition.provider.android.RecognitionNative;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.service.helper.SelfAwareHelper;
import ai.saiy.android.tts.attributes.Gender;
import ai.saiy.android.ui.activity.ActivityApplicationPickerMulti;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentAdvancedSettings;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

/**
 * Utility class to assist its parent fragment and avoid clutter there
 * <p>
 * Created by benrandall76@gmail.com on 25/07/2016.
 */

public class FragmentAdvancedSettingsHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentAdvancedSettingsHelper.class.getSimpleName();
    private static final int APP_PICKER_MULTI_REQ_CODE = 1100;

    private final FragmentAdvancedSettings parentFragment;

    /**
     * Constructor
     *
     * @param parentFragment the parent fragment for this helper class
     */
    public FragmentAdvancedSettingsHelper(@NonNull final FragmentAdvancedSettings parentFragment) {
        this.parentFragment = parentFragment;
    }

    /**
     * Get the components for this fragment
     *
     * @return a list of {@link ContainerUI} elements
     */
    private ArrayList<ContainerUI> getUIComponents() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getUIComponents");
        }

        final ArrayList<ContainerUI> mObjects = new ArrayList<>();

        ContainerUI containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_toast));
        containerUI.setSubtitle(getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_comment_alert_outline);
        containerUI.setIconExtra(SPH.getToastUnknown(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_haptic));
        containerUI.setSubtitle(getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_vibration);
        containerUI.setIconExtra(SPH.getVibrateCondition(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_offline));
        containerUI.setSubtitle(getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_cloud_outline_off);
        containerUI.setIconExtra(SPH.getUseOffline(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_hotword_detection));
        containerUI.setSubtitle(getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_blur);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_tts_gender));
        containerUI.setSubtitle(getString(R.string.menu_tap_options));
        containerUI.setIconMain(R.drawable.ic_gender_transgender);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_driving_profile));
        containerUI.setSubtitle(getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_car);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_quiet_time));
        containerUI.setSubtitle(getString(R.string.menu_tap_set));
        containerUI.setIconMain(R.drawable.ic_sleep);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_date_of_birth));
        containerUI.setSubtitle(getString(R.string.menu_tap_set));
        containerUI.setIconMain(R.drawable.ic_baby);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_pause));
        containerUI.setSubtitle(getString(R.string.menu_tap_set));
        containerUI.setIconMain(R.drawable.ic_pause_circle_outline);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_call_confirm));
        containerUI.setSubtitle(getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_phone_paused);
        containerUI.setIconExtra(SPH.getCallConfirmation(getApplicationContext())? FragmentHome.CHECKED: FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_announce_caller));
        containerUI.setSubtitle(getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_phone_in_talk);
        containerUI.setIconExtra(SPH.announceCallerStats(getApplicationContext())? FragmentHome.CHECKED: FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.title_announce_notifications));
        containerUI.setSubtitle(getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_white_balance_irradescent);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_sms_email_signature));
        containerUI.setSubtitle(getString(R.string.menu_tap_set));
        containerUI.setIconMain(R.drawable.ic_transcribe_close);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.title_diagnostics));
        containerUI.setSubtitle(getString(R.string.menu_tap_run));
        containerUI.setIconMain(R.drawable.ic_pulse);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        return mObjects;
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

    /**
     * Get the adapter for this fragment
     *
     * @param objects list of {@link ContainerUI} elements
     * @return the {@link UIMainAdapter}
     */
    public UIMainAdapter getAdapter(@NonNull final ArrayList<ContainerUI> objects) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdapter");
        }
        return new UIMainAdapter(objects, getParent(), getParent());
    }

    /**
     * Update the parent fragment with the UI components
     */
    public void finaliseUI() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ContainerUI> tempArray = FragmentAdvancedSettingsHelper.this.getUIComponents();

                if (FragmentAdvancedSettingsHelper.this.getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)) {

                    try {
                        Thread.sleep(FragmentHome.DRAWER_CLOSE_DELAY);
                    } catch (final InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                            e.printStackTrace();
                        }
                    }
                }

                if (FragmentAdvancedSettingsHelper.this.getParent().isActive()) {

                    FragmentAdvancedSettingsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            FragmentAdvancedSettingsHelper.this.getParent().getObjects().addAll(tempArray);
                            FragmentAdvancedSettingsHelper.this.getParent().getAdapter().notifyItemRangeInserted(0, FragmentAdvancedSettingsHelper.this.getParent().getObjects().size());
                        }
                    });

                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                    }
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onActivityResult: " + requestCode + " ~ " + resultCode);
        }
        if (resultCode != Activity.RESULT_OK) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onActivityResult: RESULT_CANCELED");
            }
            return;
        }
        if (data == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onActivityResult: data null");
            }
            return;
        }
        if (requestCode == APP_PICKER_MULTI_REQ_CODE) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onActivityResult: APP_PICKER_MULTI_REQ_CODE");
            }
            if (!data.hasExtra(ActivityApplicationPickerMulti.EXTRA_APPLICATION_ARRAY)) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onActivityResult: no application array");
                }
                return;
            }
            final ArrayList<ApplicationBasic> arrayList = data.getParcelableArrayListExtra(ActivityApplicationPickerMulti.EXTRA_APPLICATION_ARRAY);
            if (DEBUG && arrayList != null) {
                for (ApplicationBasic applicationBasic : arrayList) {
                    MyLog.i(CLS_NAME, "getName: " + applicationBasic.getName());
                    MyLog.i(CLS_NAME, "getPackageName: " + applicationBasic.getPackageName());
                }
            }
            final BlockedApplications blockedApplications = BlockedApplicationsHelper.getBlockedApplications(getApplicationContext());
            blockedApplications.setApplicationArray(arrayList);
            BlockedApplicationsHelper.save(getApplicationContext(), blockedApplications);
        }
    }

    public void toast(String text, int duration) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "makeToast: " + text);
        }
        if (getParent().isActive()) {
            getParentActivity().toast(text, duration);
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "toast Fragment detached");
        }
    }

    /**
     * Show the gender selector
     */
    @SuppressWarnings("ConstantConditions")
    public void showGenderSelector() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final String[] gender = FragmentAdvancedSettingsHelper.this.getParent().getResources().getStringArray(R.array.array_gender);

                for (int i = 0; i < gender.length; i++) {
                    gender[i] = StringUtils.capitalize(gender[i]);
                }

                FragmentAdvancedSettingsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(FragmentAdvancedSettingsHelper.this.getParentActivity())
                                .setCancelable(false)
                                .setTitle(R.string.tts_gender_text)
                                .setIcon(R.drawable.ic_gender_transgender)
                                .setSingleChoiceItems((CharSequence[]) gender, SPH.getDefaultTTSGender(FragmentAdvancedSettingsHelper.this.getApplicationContext()).ordinal(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showGenderSelector: onSelection: " + which + ": " + gender[which]);
                                        }
                                    }
                                })

                                .setPositiveButton(R.string.menu_select, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dialog instanceof AlertDialog) {
                                            switch (((AlertDialog) dialog).getListView().getCheckedItemPosition()) {
                                                case 0:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "showGenderSelector: onPositive: MALE");
                                                    }
                                                    SPH.setDefaultTTSGender(FragmentAdvancedSettingsHelper.this.getApplicationContext(), Gender.MALE);
                                                    SPH.setDefaultTTSVoice(FragmentAdvancedSettingsHelper.this.getApplicationContext(), null);
                                                    break;
                                                case 1:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "showGenderSelector: onPositive: FEMALE");
                                                    }
                                                    SPH.setDefaultTTSGender(FragmentAdvancedSettingsHelper.this.getApplicationContext(), Gender.FEMALE);
                                                    SPH.setDefaultTTSVoice(FragmentAdvancedSettingsHelper.this.getApplicationContext(), null);
                                                    break;

                                            }
                                        }
                                        dialog.dismiss();
                                    }
                                })

                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showGenderSelector: onNegative");
                                        }
                                        dialog.dismiss();
                                    }
                                })

                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showGenderSelector: onCancel");
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

    /**
     * Show the Hotword selector
     */
    @SuppressWarnings("ConstantConditions")
    public void showHotwordSelector() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final String[] hotwordActions = FragmentAdvancedSettingsHelper.this.getParent().getResources().getStringArray(R.array.array_hotword);
                final ArrayList<Integer> selectedList = new ArrayList<>();
                if (SPH.getHotwordBoot(FragmentAdvancedSettingsHelper.this.getApplicationContext())) {
                    selectedList.add(0);
                }
                if (SPH.getHotwordStartDriving(getApplicationContext())) {
                    selectedList.add(1);
                }
                if (SPH.getHotwordStopDriving(getApplicationContext())) {
                    selectedList.add(2);
                }
                if (SPH.getHotwordWakelock(getApplicationContext())) {
                    selectedList.add(3);
                }
                selectedList.add(4);
                if (SPH.getHotwordOkayGoogle(getApplicationContext())) {
                    selectedList.add(5);
                }
                selectedList.add(6);
                if (SPH.getHotwordSecure(getApplicationContext())) {
                    selectedList.add(7);
                }

                FragmentAdvancedSettingsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean[] checkedItems = new boolean[hotwordActions.length];
                        for (int i = 0; i < selectedList.size(); i++) {
                            checkedItems[selectedList.get(i)] = true;
                        }
                        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(FragmentAdvancedSettingsHelper.this.getParentActivity())
                                .setCancelable(false)
                                .setTitle(R.string.hotword_intro_text)
                                .setIcon(R.drawable.ic_blur)
                                .setMultiChoiceItems(hotwordActions, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showHotwordSelector: onSelection: " + which + ", " + isChecked);
                                        }
                                    }
                                })
                                .setNeutralButton(R.string.clear, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dialog instanceof AlertDialog) {
                                            final ListAdapter adapter = ((AlertDialog) dialog).getListView().getAdapter();
                                            if (adapter instanceof BaseAdapter) {
                                                for (int i = checkedItems.length - 1; i >= 0; --i) {
                                                    checkedItems[i] = (4 == i || 6 == i);
                                                }
                                                ((BaseAdapter) adapter).notifyDataSetChanged();
                                            } else {
                                                MyLog.e(CLS_NAME, "onNegative:" + (adapter == null ? "adapter null" : "adapter not BaseAdapter"));
                                            }
                                        }
                                    }
                                })
                                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showHotwordSelector: onPositive");
                                        }

                                        final List<Integer> selectedIndices = new ArrayList<>();
                                        for (int i = 0; i < checkedItems.length; ++i) {
                                            if (checkedItems[i] || (4 == i || 6 == i)) {
                                                selectedIndices.add(i);
                                            }
                                        }
                                        final Integer[] selected = selectedIndices.toArray(new Integer[0]);

                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showHotwordSelector: onPositive: length: " + selected.length);
                                            for (final Integer aSelected : selected) {
                                                MyLog.i(CLS_NAME, "showHotwordSelector: onPositive: " + aSelected);
                                            }
                                        }

                                        SPH.setHotwordBoot(getApplicationContext(),
                                                ArrayUtils.contains(selected, 0));
                                        SPH.setHotwordStartDriving(getApplicationContext(),
                                                ArrayUtils.contains(selected, 1));
                                        SPH.setHotwordStopDriving(getApplicationContext(),
                                                ArrayUtils.contains(selected, 2));
                                        SPH.setHotwordWakelock(getApplicationContext(),
                                                ArrayUtils.contains(selected, 3));
                                        final boolean hotwordOkayGoogle = ArrayUtils.contains(selected, 5);
                                        SPH.setHotwordSecure(getApplicationContext(),
                                                ArrayUtils.contains(selected, 7));

                                        if (hotwordOkayGoogle != SPH.getHotwordOkayGoogle(getApplicationContext())) {
                                            toast(getString(R.string.menu_requires_hotword_restart), Toast.LENGTH_LONG);
                                        }
                                        SPH.setHotwordOkayGoogle(getApplicationContext(), hotwordOkayGoogle);
                                        dialog.dismiss();
                                        SelfAwareHelper.restartService(getApplicationContext());
                                    }
                                })

                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showHotwordSelector: onNegative");
                                        }
                                        dialog.dismiss();
                                    }
                                })

                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showHotwordSelector: onCancel");
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

    public void showDrivingProfileSelector() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final String[] stringArray = getParent().getResources().getStringArray(R.array.array_driving_profile);
                final DrivingProfile drivingProfile = DrivingProfileHelper.getDrivingProfile(getApplicationContext());
                final ArrayList<Integer> arrayList = new ArrayList<>();
                if (drivingProfile.shouldStartAutomatically()) {
                    arrayList.add(0);
                }
                if (drivingProfile.shouldStopAutomatically()) {
                    arrayList.add(1);
                }
                if (drivingProfile.getStartHotword()) {
                    arrayList.add(2);
                }
                if (drivingProfile.getAnnounceNotifications()) {
                    arrayList.add(3);
                }
                if (drivingProfile.getAnnounceCallerId()) {
                    arrayList.add(4);
                }
                getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean[] checkedItems = new boolean[stringArray.length];
                        for (int i = 0; i < arrayList.size(); i++) {
                            checkedItems[arrayList.get(i)] = true;
                        }
                        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(FragmentAdvancedSettingsHelper.this.getParentActivity())
                                .setCancelable(false)
                                .setTitle(R.string.driving_profile_intro_text)
                                .setIcon(R.drawable.ic_car)
                                .setMultiChoiceItems(stringArray, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showDrivingProfileSelector: onSelection: " + which + ", " + isChecked);
                                        }
                                    }
                                })
                                .setNeutralButton(R.string.clear, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dialog instanceof AlertDialog) {
                                            final ListAdapter adapter = ((AlertDialog) dialog).getListView().getAdapter();
                                            if (adapter instanceof BaseAdapter) {
                                                for (int i = checkedItems.length - 1; i >= 0; --i) {
                                                    checkedItems[i] = false;
                                                }
                                                ((BaseAdapter) adapter).notifyDataSetChanged();
                                            } else {
                                                MyLog.e(CLS_NAME, "onNegative:" + (adapter == null ? "adapter null" : "adapter not BaseAdapter"));
                                            }
                                        }
                                    }
                                })
                                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showDrivingProfileSelector: onPositive");
                                        }

                                        final List<Integer> selectedIndices = new ArrayList<>();
                                        for (int i = 0; i < checkedItems.length; ++i) {
                                            if (checkedItems[i]) {
                                                selectedIndices.add(i);
                                            }
                                        }
                                        final Integer[] selected = selectedIndices.toArray(new Integer[0]);

                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showDrivingProfileSelector: onPositive: length: " + selected.length);
                                            for (final Integer aSelected : selected) {
                                                MyLog.i(CLS_NAME, "showDrivingProfileSelector: onPositive: " + aSelected);
                                            }
                                        }
                                        drivingProfile.setStartAutomatically(ArrayUtils.contains(selected, 0));
                                        drivingProfile.setStopAutomatically(ArrayUtils.contains(selected, 1));
                                        drivingProfile.setStartHotword(ArrayUtils.contains(selected, 2));
                                        final boolean announceNotifications = ArrayUtils.contains(selected, 3);
                                        drivingProfile.setAnnounceNotifications(announceNotifications);

                                        boolean checkAnnounceCallerId;
                                        if (!announceNotifications) {
                                            checkAnnounceCallerId = true;
                                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            boolean isNotificationListenerRunning = false;
                                            for (String packageName : NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext())) {
                                                if (packageName.equals(getApplicationContext().getPackageName())) {
                                                    isNotificationListenerRunning = true;
                                                    break;
                                                }
                                            }
                                            if (isNotificationListenerRunning) {
                                                checkAnnounceCallerId = true;
                                            } else {
                                                if (DEBUG) {
                                                    MyLog.i(CLS_NAME, "notification listener service not running");
                                                }
                                                if (SettingsIntent.settingsIntent(getApplicationContext(), SettingsIntent.Type.NOTIFICATION_ACCESS)) {
                                                    getParentActivity().speak(R.string.notifications_enable, LocalRequest.ACTION_TOGGLE_DRIVING_PROFILE);
                                                    checkAnnounceCallerId = false;
                                                } else {
                                                    if (DEBUG) {
                                                        MyLog.w(CLS_NAME, "notification listener: settings location unknown");
                                                    }
                                                    getParentActivity().speak(getParent().getString(R.string.settings_missing, getString(R.string.notification_access)), LocalRequest.ACTION_SPEAK_ONLY);
                                                    checkAnnounceCallerId = false;
                                                }
                                            }
                                        } else if (ai.saiy.android.service.helper.SelfAwareHelper.saiyAccessibilityRunning(getApplicationContext())) {
                                            ai.saiy.android.service.helper.SelfAwareHelper.startAccessibilityService(getApplicationContext());
                                            checkAnnounceCallerId = true;
                                        } else {
                                            ai.saiy.android.intent.ExecuteIntent.settingsIntent(getApplicationContext(), IntentConstants.SETTINGS_ACCESSIBILITY);
                                            getParentActivity().speak(R.string.accessibility_enable, LocalRequest.ACTION_SPEAK_ONLY);
                                            checkAnnounceCallerId = false;
                                        }
                                        if (!checkAnnounceCallerId) {
                                            if (DEBUG) {
                                                MyLog.w(CLS_NAME, "showDrivingProfileSelector: proceed: false");
                                            }
                                            return;
                                        }
                                        final boolean announceCallerId = ArrayUtils.contains(selected, 4);
                                        if (announceCallerId && !ai.saiy.android.permissions.PermissionHelper.checkNotificationPolicyPermission(getApplicationContext())) {
                                            ai.saiy.android.intent.ExecuteIntent.settingsIntent(getApplicationContext(), IntentConstants.NOTIFICATION_POLICY_ACCESS_SETTINGS);
                                            getParentActivity().speak(R.string.app_speech_notification_policy, LocalRequest.ACTION_SPEAK_ONLY);
                                        } else if (ai.saiy.android.permissions.PermissionHelper.checkReadCallerPermissions(getApplicationContext())) {
                                            drivingProfile.setAnnounceCallerId(announceCallerId);
                                            DrivingProfileHelper.save(getApplicationContext(), drivingProfile);
                                            dialog.dismiss();
                                            ai.saiy.android.service.helper.SelfAwareHelper.restartService(getApplicationContext());
                                        }
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showDrivingProfileSelector: onNegative");
                                        }
                                        dialog.dismiss();
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showDrivingProfileSelector: onCancel");
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

    public void showAccessibilityChangeDialog() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showAccessibilityChangeDialog");
        }
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setTitle(R.string.menu_accessibility)
                .setMessage(R.string.content_accessibility_change)
                .setIcon(R.drawable.ic_information)
                .setPositiveButton(R.string.menu_accessibility_disable, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAccessibilityChangeDialog: onPositive");
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            SelfAwareHelper.disableAccessibilityService(getApplicationContext());
                            toast(getString(R.string.success), Toast.LENGTH_SHORT);
                        } else {
                            ai.saiy.android.intent.ExecuteIntent.settingsIntent(getApplicationContext(), IntentConstants.SETTINGS_ACCESSIBILITY);
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAccessibilityChangeDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
        materialDialog.show();
    }

    public void showAnnounceNotificationsDialog() {
        final View view = LayoutInflater.from(getParent().getContext()).inflate(R.layout.announce_notifications_dialog_layout, null);
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setCancelable(false)
                .setTitle(R.string.menu_announce_notifications)
                .setIcon(R.drawable.ic_white_balance_irradescent)
                .setView(view)
                .setNeutralButton(R.string.menu_blocked_apps, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAnnounceNotificationsDialog: onNeutral");
                        }
                        final Intent intent = new Intent(getApplicationContext(), ActivityApplicationPickerMulti.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.putExtra(ActivityApplicationPickerMulti.EXTRA_BLOCKED_APPLICATIONS, BlockedApplicationsHelper.getBlockedApplications(getApplicationContext()));
                        try {
                            getParent().startActivityForResult(intent, APP_PICKER_MULTI_REQ_CODE);
                        } catch (Exception e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "Exception");
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAnnounceNotificationsDialog: onPositive");
                        }
                        if (dialog instanceof AlertDialog) {
                            EditText editText = ((AlertDialog) dialog).getWindow().findViewById(R.id.etBlockedContent);
                            final CheckBox cbEnabled = ((AlertDialog) dialog).getWindow().findViewById(R.id.cbEnabled);
                            final CheckBox cbDeviceLocked = ((AlertDialog) dialog).getWindow().findViewById(R.id.cbDeviceLocked);
                            final CheckBox cbRestricted = ((AlertDialog) dialog).getWindow().findViewById(R.id.cbRestricted);
                            final CheckBox cbSMSContent = ((AlertDialog) dialog).getWindow().findViewById(R.id.cbSMSContent);
                            final CheckBox cbHangoutContent = ((AlertDialog) dialog).getWindow().findViewById(R.id.cbHangoutContent);
                            final CheckBox cbWhatsAppContent = ((AlertDialog) dialog).getWindow().findViewById(R.id.cbWhatsAppContent);
                            if (editText.getText() != null) {
                                String str = editText.getText().toString().trim();
                                if (ai.saiy.android.utils.UtilsString.notNaked(str) &&
                                        !ai.saiy.android.utils.UtilsString.regexCheck(str)) {
                                    toast(getString(R.string.input_format_error), Toast.LENGTH_SHORT);
                                    return;
                                }
                            }
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    BlockedApplications blockedApplications = BlockedApplicationsHelper.getBlockedApplications(getApplicationContext());
                                    blockedApplications.setText(null);
                                    BlockedApplicationsHelper.save(getApplicationContext(), blockedApplications);
                                    SPH.setAnnounceNotifications(getApplicationContext(), cbEnabled.isChecked());
                                    SPH.setAnnounceNotificationsSecure(getApplicationContext(), cbDeviceLocked.isChecked());
                                    SPH.setIgnoreRestrictedContent(getApplicationContext(), cbRestricted.isChecked());
                                    SPH.setAnnounceNotificationsSMS(getApplicationContext(), cbSMSContent.isChecked());
                                    SPH.setAnnounceNotificationsHangouts(getApplicationContext(), cbHangoutContent.isChecked());
                                    SPH.setAnnounceNotificationsWhatsapp(getApplicationContext(), cbWhatsAppContent.isChecked());
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                                        if (SPH.getAnnounceNotifications(getApplicationContext()) && !ai.saiy.android.service.helper.SelfAwareHelper.saiyAccessibilityRunning(getApplicationContext())) {
                                            ai.saiy.android.intent.ExecuteIntent.settingsIntent(getApplicationContext(), IntentConstants.SETTINGS_ACCESSIBILITY);
                                            getParentActivity().speak(R.string.accessibility_enable, LocalRequest.ACTION_SPEAK_ONLY);
                                        } else if (SPH.getAnnounceNotifications(getApplicationContext())) {
                                            ai.saiy.android.service.helper.SelfAwareHelper.startAccessibilityService(getApplicationContext());
                                        }
                                    }
                                }
                            });
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAnnounceNotificationsDialog: onNegative");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showAnnounceNotificationsDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();

        BlockedApplications blockedApplications = BlockedApplicationsHelper.getBlockedApplications(getApplicationContext());
        if (ai.saiy.android.utils.UtilsString.notNaked(blockedApplications.getText())) {
            ((EditText) view.findViewById(R.id.etBlockedContent)).setText(blockedApplications.getText());
        }
        ((CheckBox) view.findViewById(R.id.cbEnabled)).setChecked(SPH.getAnnounceNotifications(getApplicationContext()));
        ((CheckBox) view.findViewById(R.id.cbDeviceLocked)).setChecked(SPH.getAnnounceNotificationsSecure(getApplicationContext()));
        ((CheckBox) view.findViewById(R.id.cbRestricted)).setChecked(SPH.getIgnoreRestrictedContent(getApplicationContext()));
        ((CheckBox) view.findViewById(R.id.cbSMSContent)).setChecked(SPH.getAnnounceNotificationsSMS(getApplicationContext()));
        final CheckBox cbHangoutContent = view.findViewById(R.id.cbHangoutContent);
        if (Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_GOOGLE_HANGOUT)) {
            cbHangoutContent.setChecked(SPH.getAnnounceNotificationsHangouts(getApplicationContext()));
        } else {
            cbHangoutContent.setEnabled(false);
        }
        final CheckBox cbWhatsAppContent = view.findViewById(R.id.cbWhatsAppContent);
        if (Installed.isPackageInstalled(getApplicationContext(), Installed.PACKAGE_WHATSAPP)) {
            cbWhatsAppContent.setChecked(SPH.getAnnounceNotificationsWhatsapp(getApplicationContext()));
        } else {
            cbWhatsAppContent.setEnabled(false);
        }
    }

    public void showQuietTimesDialog() {
        final ai.saiy.android.quiet.QuietTime quietTime = ai.saiy.android.quiet.QuietTimeHelper.getQuietTimes(getApplicationContext());
        final View view = LayoutInflater.from(getParent().getContext()).inflate(R.layout.quiet_times_dialog_layout, null);
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setTitle(R.string.menu_quiet_time)
                .setIcon(R.drawable.ic_sleep)
                .setView(view)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showQuietTimesDialog: onPositive");
                        }
                        TimePicker startTimePicker = view.findViewById(R.id.tpQuietTimeStart);
                        TimePicker endTimePicker = view.findViewById(R.id.tpQuietTimeEnd);
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "Start: " + startTimePicker.getCurrentHour() + " : " + startTimePicker.getCurrentMinute());
                            MyLog.d(CLS_NAME, "End: " + endTimePicker.getCurrentHour() + " : " + endTimePicker.getCurrentMinute());
                        }
                        quietTime.setStartHour(startTimePicker.getCurrentHour());
                        quietTime.setStartMinute(startTimePicker.getCurrentMinute());
                        quietTime.setEndHour(endTimePicker.getCurrentHour());
                        quietTime.setEndMinute(endTimePicker.getCurrentMinute());
                        ai.saiy.android.quiet.QuietTimeHelper.save(getApplicationContext(), quietTime);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showQuietTimesDialog: onNegative");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showQuietTimesDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
        TimePicker startTimePicker = view.findViewById(R.id.tpQuietTimeStart);
        startTimePicker.setCurrentHour(quietTime.getStartHour());
        startTimePicker.setCurrentMinute(quietTime.getStartMinute());
        TimePicker endTimePicker = view.findViewById(R.id.tpQuietTimeEnd);
        endTimePicker.setCurrentHour(quietTime.getEndHour());
        endTimePicker.setCurrentMinute(quietTime.getEndMinute());
    }

    /**
     * Show the pause detection slider
     */
    @SuppressWarnings("ConstantConditions")
    public void showPauseDetectionSlider() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setView(R.layout.pause_detection_dialog_layout)
                .setCancelable(false)
                .setTitle(R.string.menu_pause)
                .setIcon(R.drawable.ic_pause_circle_outline)
                .setNeutralButton(R.string.text_default, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog instanceof AlertDialog) {
                            ((SeekBar) ((AlertDialog) dialog).findViewById(R.id.pauseSeekBar))
                                    .setProgress((int) (RecognitionNative.PAUSE_TIMEOUT / 1000));
                        }
                    }
                })

                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showPauseDetectionSlider: onPositive");
                        }

                        if (dialog instanceof AlertDialog) {
                            SPH.setPauseTimeout(FragmentAdvancedSettingsHelper.this.getApplicationContext(),
                                    (long) ((SeekBar) ((AlertDialog) dialog).findViewById(R.id.pauseSeekBar))
                                            .getProgress() * 1000);
                        }
                        dialog.dismiss();
                    }
                })

                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showPauseDetectionSlider: onNegative");
                        }
                        dialog.dismiss();
                    }
                })

                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showPauseDetectionSlider: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();

        final int currentTimeout = (int) (SPH.getPauseTimeout(getApplicationContext()) / 1000);
        final TextView seekText = (TextView) materialDialog.findViewById(R.id.pauseSeekBarText);

        switch (currentTimeout) {
            case 0:
                seekText.setText(getString(R.string.pause_detection_text)
                        + " " + getString(R.string.provider_default));
                break;
            case 1:
                seekText.setText(getString(R.string.pause_detection_text)
                        + " " + currentTimeout + " " + getString(R.string.second));
                break;
            default:
                seekText.setText(getString(R.string.pause_detection_text)
                        + " " + currentTimeout + " " + getString(R.string.seconds));
                break;
        }

        final SeekBar seekbar = (SeekBar) materialDialog.findViewById(R.id.pauseSeekBar);
        seekbar.setProgress(currentTimeout);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {

                switch (progress) {
                    case 0:
                        seekText.setText(getString(R.string.pause_detection_text)
                                + " " + getString(R.string.provider_default));
                        break;
                    case 1:
                        seekText.setText(getString(R.string.pause_detection_text)
                                + " " + progress + " " + getString(R.string.second));
                        break;
                    default:
                        seekText.setText(getString(R.string.pause_detection_text)
                                + " " + progress + " " + getString(R.string.seconds));
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
        });
    }

    public void showDOBDialog() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
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
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDOBDialog: onNegative");
                        }
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showDOBDialog: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        calendar.setLenient(true);
        calendar.set(SPH.getDobYear(getApplicationContext()), SPH.getDobMonth(getApplicationContext()), SPH.getDobDay(getApplicationContext()));
        ((DatePicker) materialDialog.getWindow().findViewById(R.id.dobDatePicker)).updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
    }

    public void showSignatureDialog() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setCancelable(false)
                .setView(R.layout.text_input_dialog_layout)
                .setTitle(R.string.menu_sms_email_signature)
                .setIcon(R.drawable.ic_transcribe_close)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showSignatureDialog: onPositive");
                        }
                        if (dialog instanceof AlertDialog) {
                            final EditText editText = ((AlertDialog) dialog).findViewById(android.R.id.input);
                            final CharSequence charSequence = (editText == null)? null : editText.getText();
                            if (charSequence != null) {
                                if (UtilsString.notNaked(charSequence.toString())) {
                                    final CheckBox checkBox = ((AlertDialog) dialog).findViewById(android.R.id.checkbox);
                                    SPH.setMessageSignatureCondition(getApplicationContext(), checkBox != null && checkBox.isChecked());
                                    SPH.setMessageSignature(getApplicationContext(), charSequence.toString().trim());
                                } else {
                                    SPH.setMessageSignatureCondition(getApplicationContext(), false);
                                    SPH.setMessageSignature(getApplicationContext(), "");
                                }
                            }
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        dialog.dismiss();
                    }
                }).create();
        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();

        final TextInputLayout textInputLayout = materialDialog.getWindow().findViewById(android.R.id.inputArea);
        textInputLayout.setHint(R.string.custom_signature_text);
        final EditText editText = textInputLayout.findViewById(android.R.id.input);
        editText.setText(SPH.getMessageSignature(getApplicationContext()));
        final CheckBox checkBox = textInputLayout.findViewById(android.R.id.checkbox);
        checkBox.setText(R.string.menu_enabled);
        checkBox.setChecked(SPH.getMessageSignatureCondition(getApplicationContext()));
        checkBox.jumpDrawablesToCurrentState();
        checkBox.setVisibility(View.VISIBLE);
    }

    private String getString(@StringRes int resId) {
        return getApplicationContext().getString(resId);
    }

    /**
     * Utility method to ensure we double check the context being used.
     *
     * @return the application context
     */
    private Context getApplicationContext() {
        return parentFragment.getApplicationContext();
    }

    /**
     * Utility to return the parent activity neatly cast. No need for instanceOf as this
     * fragment helper will never be attached to another activity.
     *
     * @return the {@link ActivityHome} parent
     */

    public ActivityHome getParentActivity() {
        return parentFragment.getParentActivity();
    }

    /**
     * Utility method to return the parent fragment this helper is helping.
     *
     * @return the parent fragment
     */
    public FragmentAdvancedSettings getParent() {
        return parentFragment;
    }
}
