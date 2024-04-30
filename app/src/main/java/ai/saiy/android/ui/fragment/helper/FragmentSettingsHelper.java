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

import static android.widget.AdapterView.INVALID_POSITION;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import ai.saiy.android.R;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.command.battery.BatteryInformation;
import ai.saiy.android.command.unknown.Unknown;
import ai.saiy.android.thirdparty.tasker.TaskerHelper;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.ui.fragment.FragmentSettings;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Utility class to assist its parent fragment and avoid clutter there
 * <p>
 * Created by benrandall76@gmail.com on 25/07/2016.
 */
public class FragmentSettingsHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentSettingsHelper.class.getSimpleName();

    private final FragmentSettings parentFragment;

    /**
     * Constructor
     *
     * @param parentFragment the parent fragment for this helper class
     */
    public FragmentSettingsHelper(@NonNull final FragmentSettings parentFragment) {
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
        containerUI.setTitle(getParent().getString(R.string.menu_language));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_options));
        containerUI.setIconMain(R.drawable.ic_language);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_unknown_commands));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_options));
        containerUI.setIconMain(R.drawable.ic_not_equal);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_volume_settings));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_set));
        containerUI.setIconMain(R.drawable.ic_volume_high);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_synthesised_voice));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_google);
        containerUI.setIconExtra(SPH.getNetworkSynthesis(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_temperature_units));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_options));
        containerUI.setIconMain(R.drawable.ic_thermometer);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_default_apps));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_set));
        containerUI.setIconMain(R.drawable.ic_apps);
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

        final RecyclerView mRecyclerView = (RecyclerView)
                parent.findViewById(R.id.layout_common_fragment_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getParentActivity(), null));

        return mRecyclerView;
    }

    /**
     * Get the adapter for this fragment
     *
     * @param mObjects list of {@link ContainerUI} elements
     * @return the {@link UIMainAdapter}
     */
    public UIMainAdapter getAdapter(@NonNull final ArrayList<ContainerUI> mObjects) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdapter");
        }
        return new UIMainAdapter(mObjects, getParent(), getParent());
    }

    /**
     * Update the parent fragment with the UI components
     */
    public void finaliseUI() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ContainerUI> tempArray = FragmentSettingsHelper.this.getUIComponents();

                if (FragmentSettingsHelper.this.getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)) {

                    try {
                        Thread.sleep(FragmentHome.DRAWER_CLOSE_DELAY);
                    } catch (final InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                            e.printStackTrace();
                        }
                    }
                }

                if (FragmentSettingsHelper.this.getParent().isActive()) {

                    FragmentSettingsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            FragmentSettingsHelper.this.getParent().getObjects().addAll(tempArray);
                            FragmentSettingsHelper.this.getParent().getAdapter().notifyItemRangeInserted(0, FragmentSettingsHelper.this.getParent().getObjects().size());
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

    /**
     * Show the temperature units selector
     */
    @SuppressWarnings("ConstantConditions")
    public void showTemperatureUnitsSelector() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final String[] units = FragmentSettingsHelper.this.getParent().getResources().getStringArray(R.array.array_temperature_units);

                for (int i = 0; i < units.length; i++) {
                    units[i] = StringUtils.capitalize(units[i]);
                }

                FragmentSettingsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(FragmentSettingsHelper.this.getParentActivity())
                                .setCancelable(false)
                                .setTitle(R.string.menu_temperature_units)
                                .setIcon(R.drawable.ic_thermometer)
                                .setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorTint)))
                                .setSingleChoiceItems((CharSequence[]) units, SPH.getDefaultTemperatureUnits(FragmentSettingsHelper.this.getApplicationContext()), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showTemperatureUnitsSelector: onSelection: " + which + ": " + units[which]);
                                        }
                                    }
                                })

                                .setPositiveButton(R.string.menu_select, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dialog instanceof AlertDialog) {
                                            switch (((AlertDialog) dialog).getListView().getCheckedItemPosition()) {

                                                case BatteryInformation.CELSIUS:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "showTemperatureUnitsSelector: onPositive: CELSIUS");
                                                    }
                                                    SPH.setDefaultTemperatureUnits(FragmentSettingsHelper.this.getApplicationContext(),
                                                            BatteryInformation.CELSIUS);
                                                    break;
                                                case BatteryInformation.FAHRENHEIT:
                                                    if (DEBUG) {
                                                        MyLog.i(CLS_NAME, "showTemperatureUnitsSelector: onPositive: FAHRENHEIT");
                                                    }
                                                    SPH.setDefaultTemperatureUnits(FragmentSettingsHelper.this.getApplicationContext(),
                                                            BatteryInformation.FAHRENHEIT);
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
                                            MyLog.i(CLS_NAME, "showTemperatureUnitsSelector: onNegative");
                                        }
                                        dialog.dismiss();
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showTemperatureUnitsSelector: onCancel");
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
     * Show the unknown command action selector
     */
    @SuppressWarnings("ConstantConditions")
    public void showUnknownCommandSelector() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                final String[] actions = FragmentSettingsHelper.this.getParent().getResources().getStringArray(R.array.array_unknown_action);

                for (int i = 0; i < actions.length; i++) {

                    switch (i) {

                    case Unknown.UNKNOWN_STATE:
                        break;
                    case Unknown.UNKNOWN_REPEAT:
                        break;
                    case Unknown.UNKNOWN_GOOGLE_SEARCH:
                        actions[i] = getParent().getString(R.string.menu_send_to) + " " + actions[i];
                        break;
                    case Unknown.UNKNOWN_WOLFRAM_ALPHA:
                        actions[i] = getParent().getString(R.string.menu_send_to) + " " + actions[i];
                        break;
                    case Unknown.UNKNOWN_TASKER:
                        actions[i] = getParent().getString(R.string.menu_send_to) + " " + actions[i];
                        break;
                }
            }

                final ArrayList<Integer> disabledIndicesList = new ArrayList<>();

                if (!Installed.isPackageInstalled(FragmentSettingsHelper.this.getApplicationContext(),
                        Installed.PACKAGE_WOLFRAM_ALPHA)) {
                    disabledIndicesList.add(Unknown.UNKNOWN_WOLFRAM_ALPHA);
                }

                if (!new TaskerHelper().isTaskerInstalled(FragmentSettingsHelper.this.getApplicationContext()).first) {
                    disabledIndicesList.add(Unknown.UNKNOWN_TASKER);
                }

                FragmentSettingsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final int defaultIndex = SPH.getCommandUnknownAction(FragmentSettingsHelper.this.getApplicationContext());
                        int checkedItem = INVALID_POSITION;
                        final List<String> items = new ArrayList<>();
                        for (int i = 0, j = 0; i < actions.length; i++, j++) {
                            if (disabledIndicesList.contains(i)) {
                                continue;
                            }
                            items.add(actions[i]);
                            if (i == defaultIndex) {
                                checkedItem = j;
                            }
                        }
                        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                        .setCancelable(false)
                        .setTitle(R.string.menu_unknown_commands)
                        .setMessage(R.string.content_unknown_command)
                        .setIcon(R.drawable.ic_not_equal)
                        .setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorTint)))
                        .setSingleChoiceItems(items.toArray(new String[0]), checkedItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUnknownCommandSelector: onSelection: " + which + ": " + items.get(which));
                                }
                            }
                        })

                                .setPositiveButton(R.string.menu_select, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dialog instanceof AlertDialog) {
                                            final int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                            int selectedIndex = INVALID_POSITION;
                                            if (position != INVALID_POSITION) {
                                                for (int i = 0; i < actions.length; i++) {
                                                    if (TextUtils.equals(actions[i], items.get(position))) {
                                                        selectedIndex = i;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "showUnknownCommandSelector: onPositive: " + selectedIndex);
                                            }

                                            SPH.setCommandUnknownAction(FragmentSettingsHelper.this.getApplicationContext(),
                                                    (selectedIndex == INVALID_POSITION) ? Unknown.UNKNOWN_STATE : selectedIndex);
                                        }
                                        dialog.dismiss();
                                    }
                                })

                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showUnknownCommandSelector: onNegative");
                                        }
                                        dialog.dismiss();
                                    }
                                })

                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(final DialogInterface dialog) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showUnknownCommandSelector: onCancel");
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
     * Show the pause detection slider
     */
    @SuppressWarnings("ConstantConditions")
    public void showVolumeSettingsSlider() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setView(R.layout.tts_volume_dialog_layout)
                .setCancelable(false)
                .setTitle(R.string.menu_volume_settings)
                .setIcon(R.drawable.ic_pause_octagon_outline)
                .setNegativeButton(R.string.text_default, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog instanceof AlertDialog) {
                            ((SeekBar) ((AlertDialog) dialog).findViewById(R.id.volumeSeekBar))
                                    .setProgress(4);
                        }
                    }
                })

                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog instanceof AlertDialog) {
                            final int volume = ((SeekBar) ((AlertDialog) dialog).findViewById(R.id.volumeSeekBar))
                                    .getProgress() * 10 - 40;

                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "showVolumeSettingsSlider: onPositive: setting: " + volume);
                            }

                            SPH.setTTSVolume(getApplicationContext(), volume);
                        }
                        dialog.dismiss();
                    }
                })

                .setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showVolumeSettingsSlider: onNegative");
                        }
                        dialog.dismiss();
                    }
                })

                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showVolumeSettingsSlider: onCancel");
                        }
                        dialog.dismiss();
                    }
                }).create();

        final int userVolume = SPH.getTTSVolume(getApplicationContext());
        final TextView seekText = (TextView) materialDialog.findViewById(R.id.volumeSeekBarText);
        final SeekBar seekbar = (SeekBar) materialDialog.findViewById(R.id.volumeSeekBar);

        switch (userVolume) {
            case -40:
                seekText.setText("40% " + getParent().getString(R.string.below) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(0);
                break;
            case -30:
                seekText.setText("30% " + getParent().getString(R.string.below) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(1);
                break;
            case -20:
                seekText.setText("20% " + getParent().getString(R.string.below) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(2);
                break;
            case -10:
                seekText.setText("10% " + getParent().getString(R.string.below) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(3);
                break;
            case 0:
                seekText.setText(StringUtils.capitalize(getParent().getString(R.string.adhere_to)) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(4);
                break;
            case 10:
                seekText.setText("10% " + getParent().getString(R.string.above) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(5);
                break;
            case 20:
                seekText.setText("20% " + getParent().getString(R.string.above) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(6);
                break;
            case 30:
                seekText.setText("30% " + getParent().getString(R.string.above) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(7);
                break;
            case 40:
                seekText.setText("40% " + getParent().getString(R.string.above) + " "
                        + getParent().getString(R.string.media_stream));
                seekbar.setProgress(8);
        }

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {

                switch (progress) {
                    case 0:
                        seekText.setText("40% " + getParent().getString(R.string.below) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 1:
                        seekText.setText("30% " + getParent().getString(R.string.below) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 2:
                        seekText.setText("20% " + getParent().getString(R.string.below) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 3:
                        seekText.setText("10% " + getParent().getString(R.string.below) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 4:
                        seekText.setText(StringUtils.capitalize(getParent().getString(R.string.adhere_to))
                                + " " + getParent().getString(R.string.media_stream));
                        break;
                    case 5:
                        seekText.setText("10% " + getParent().getString(R.string.above) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 6:
                        seekText.setText("20% " + getParent().getString(R.string.above) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 7:
                        seekText.setText("30% " + getParent().getString(R.string.above) + " "
                                + getParent().getString(R.string.media_stream));
                        break;
                    case 8:
                        seekText.setText("40% " + getParent().getString(R.string.above) + " "
                                + getParent().getString(R.string.media_stream));
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

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
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
    public FragmentSettings getParent() {
        return parentFragment;
    }
}
