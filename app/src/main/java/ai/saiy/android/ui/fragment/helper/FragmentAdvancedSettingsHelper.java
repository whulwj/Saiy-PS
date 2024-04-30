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

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import ai.saiy.android.R;
import ai.saiy.android.recognition.provider.android.RecognitionNative;
import ai.saiy.android.tts.attributes.Gender;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentAdvancedSettings;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Utility class to assist its parent fragment and avoid clutter there
 * <p>
 * Created by benrandall76@gmail.com on 25/07/2016.
 */

public class FragmentAdvancedSettingsHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentAdvancedSettingsHelper.class.getSimpleName();

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
        containerUI.setTitle(getString(R.string.menu_motion));
        containerUI.setSubtitle(getString(R.string.menu_tap_toggle));
        containerUI.setIconMain(R.drawable.ic_bike);
        containerUI.setIconExtra(SPH.getMotionEnabled(getApplicationContext())
                ? FragmentHome.CHECKED : FragmentHome.UNCHECKED);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getString(R.string.menu_pause));
        containerUI.setSubtitle(getString(R.string.menu_tap_set));
        containerUI.setIconMain(R.drawable.ic_pause_octagon_outline);
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

        final RecyclerView mRecyclerView = (RecyclerView) parent.findViewById(R.id.layout_common_fragment_recycler_view);
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
                                .setTitle(R.string.menu_tts_gender)
                                .setMessage(R.string.tts_gender_text)
                                .setIcon(R.drawable.ic_gender_transgender)
                                .setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorTint)))
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

                if (SPH.getHotwordDriving(getApplicationContext())) {
                    selectedList.add(1);
                }

                if (SPH.getHotwordWakelock(getApplicationContext())) {
                    selectedList.add(2);
                }

                if (SPH.getHotwordSecure(getApplicationContext())) {
                    selectedList.add(3);
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
                                .setTitle(R.string.menu_hotword_detection)
                                .setMessage(R.string.hotword_intro_text)
                                .setIcon(R.drawable.ic_blur)
                                .setMultiChoiceItems((CharSequence[]) hotwordActions, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                                        if (DEBUG) {
                                            MyLog.i(CLS_NAME, "showHotwordSelector: onSelection: " + which + ", " + isChecked);
                                        }
                                    }
                                })

                                .setNegativeButton(R.string.clear, new DialogInterface.OnClickListener() {
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
                                            MyLog.i(CLS_NAME, "showHotwordSelector: onPositive");
                                        }

                                        final List<Integer> selectedIndices = new ArrayList<>();
                                        for (int i = checkedItems.length - 1; i >= 0; --i) {
                                            if (checkedItems[i]) {
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

                                        SPH.setHotwordDriving(getApplicationContext(),
                                                ArrayUtils.contains(selected, 1));

                                        if (SPH.getHotwordDriving(getApplicationContext())) {
                                            SPH.setMotionEnabled(getApplicationContext(), true);
                                        }

                                        SPH.setHotwordWakelock(getApplicationContext(),
                                                ArrayUtils.contains(selected, 2));

                                        SPH.setHotwordSecure(getApplicationContext(),
                                                ArrayUtils.contains(selected, 3));

                                        dialog.dismiss();
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


    /**
     * Show the pause detection slider
     */
    @SuppressWarnings("ConstantConditions")
    public void showPauseDetectionSlider() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setView(R.layout.pause_detection_dialog_layout)
                .setCancelable(false)
                .setTitle(R.string.menu_pause)
                .setIcon(R.drawable.ic_pause_octagon_outline)

                .setNegativeButton(R.string.text_default, new DialogInterface.OnClickListener() {
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

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_left;
        materialDialog.show();
    }

    private String getString(final int id) {
        return getApplicationContext().getString(id);
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
