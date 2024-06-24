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
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.helper.FragmentSettingsHelper;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Created by benrandall76@gmail.com on 18/07/2016.
 */

public class FragmentSettings extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentSettings.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<ContainerUI> mObjects;
    private FragmentSettingsHelper helper;

    private static final Object lock = new Object();

    private Context mContext;

    public FragmentSettings() {
    }

    public static FragmentSettings newInstance(@Nullable final Bundle args) {
        return new FragmentSettings();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        helper = new FragmentSettingsHelper(this);
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
                getParentActivity().setTitle(getString(R.string.title_settings));
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
                getParentActivity().showLanguageSelector();
                break;
            case 1:
                helper.showVoicesDialog();
                break;
            case 2:
                helper.showUnknownCommandSelector();
                break;
            case 3:
                switch (SPH.getHeadsetOverviewCount(getApplicationContext())) {
                    case 0:
                    case 1:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showHeadsetOverviewDialog: limit not reached");
                        }
                        SPH.headsetOverviewCountAutoIncrease(getApplicationContext());
                        helper.showHeadsetOverviewDialog();
                    default:
                        helper.showHeadsetDialog();
                }
                break;
            case 4:
                if (!SPH.haveShownVolumeBug(getApplicationContext())) {
                    SPH.setShownVolumeBug(getApplicationContext(), true);
                    getParentActivity().toast(getString(R.string.content_android_bug), Toast.LENGTH_LONG);
                }
                helper.showVolumeSettingsSlider();
                break;
            case 5:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    if (isActive()) {
                        getParentActivity().toast(getString(R.string.title_error_alexa_android_5), Toast.LENGTH_SHORT);
                    }
                    break;
                }
                SPH.setAlexaNotification(getApplicationContext(), !SPH.showAlexaNotification(getApplicationContext()));
                this.getObjects().get(position).setIconExtra(SPH.showAlexaNotification(getApplicationContext()) ? R.drawable.ic_toggle_switch_on : R.drawable.ic_toggle_switch_off);
                this.getAdapter().notifyItemChanged(position);
                ai.saiy.android.service.helper.SelfAwareHelper.restartService(getApplicationContext());
                break;
            case 6:
                if (!ai.saiy.android.intent.ExecuteIntent.launcherShortcut(getContext())) {
                    getParentActivity().toast(getString(R.string.menu_shortcut_failed), Toast.LENGTH_SHORT);
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    getParentActivity().toast(getString(R.string.success_), Toast.LENGTH_SHORT);
                }
                break;
            case 7:
                helper.showNetworkSynthesisDialog();
                break;
            case 8:
                helper.showTemperatureUnitsSelector();
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
                getParentActivity().toast(getString(R.string.lp_supported_languages), Toast.LENGTH_SHORT);
                break;
            case 1:
                getParentActivity().toast(getString(R.string.lp_tts_voice), Toast.LENGTH_SHORT);
                break;
            case 2:
                getParentActivity().toast(getString(R.string.lp_unknown_commands), Toast.LENGTH_SHORT);
                break;
            case 3:
                getParentActivity().toast(getString(R.string.lp_headset), Toast.LENGTH_SHORT);
                break;
            case 4:
                getParentActivity().toast(getString(R.string.lp_volume_settings), Toast.LENGTH_SHORT);
                break;
            case 5:
                getParentActivity().toast(getString(R.string.lp_alexa_shortcut), Toast.LENGTH_SHORT);
                break;
            case 6:
                getParentActivity().toast(getString(R.string.lp_launcher_shortcut), Toast.LENGTH_SHORT);
                break;
            case 7:
                getParentActivity().toast(getString(R.string.lp_network_synthesis), Toast.LENGTH_SHORT);
                break;
            case 8:
                getParentActivity().toast(getString(R.string.lp_temperature_units), Toast.LENGTH_SHORT);
                break;
            default:
                break;
        }

        return true;
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
    public RecyclerView.Adapter getAdapter() {
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
