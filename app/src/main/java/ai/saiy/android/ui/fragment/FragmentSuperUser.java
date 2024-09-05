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
import android.os.AsyncTask;
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
import ai.saiy.android.command.settings.SettingsIntent;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.helper.FragmentSuperuserHelper;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * Created by benrandall76@gmail.com on 18/07/2016.
 */

public class FragmentSuperUser extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentSuperUser.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;
    private ArrayList<ContainerUI> mObjects;
    private FragmentSuperuserHelper helper;

    private static final Object lock = new Object();

    private Context mContext;

    public FragmentSuperUser() {
    }

    @SuppressWarnings("UnusedParameters")
    public static FragmentSuperUser newInstance(@Nullable final Bundle args) {
        return new FragmentSuperUser();
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

    public void showProgress(boolean visible) {
        if (isActive()) {
            getParentActivity().showProgress(visible);
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }

        helper = new FragmentSuperuserHelper(this);
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
                getParentActivity().setTitle(getString(R.string.title_superuser));
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

    private int getPosition(View view) {
        int position = (view == null) ? 0 : mRecyclerView.getChildAdapterPosition(view);
        if (view != null && RecyclerView.NO_POSITION == position) {
            final RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(view);
            if (viewHolder instanceof UIMainAdapter.ViewHolder) {
                position = ((UIMainAdapter.ViewHolder) viewHolder).getBoundPosition();
            }
        }
        return position;
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

        final int position = getPosition(view);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClick: " + position);
        }
        switch (position) {
            case 0:
                getParentActivity().toast(getString(R.string.menu_root), Toast.LENGTH_SHORT);
                break;
            case 1:
                if (ai.saiy.android.permissions.PermissionHelper.checkFilePermissions(getApplicationContext())) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (!ai.saiy.android.utils.UtilsFile.createDirs(getApplicationContext())) {
                                toast(getString(R.string.failed), Toast.LENGTH_SHORT);
                            } else {
                                if (ai.saiy.android.intent.ExecuteIntent.installQL(getApplicationContext())) {
                                    return;
                                }
                                toast(getString(R.string.failed), Toast.LENGTH_SHORT);
                            }
                        }
                    }).start();
                }
                break;
            case 2:
                getParentActivity().vibrate();
                SPH.setStartAtBoot(getApplicationContext(), !SPH.getStartAtBoot(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getStartAtBoot(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 3:
                helper.showVocalVerificationDialog();
                break;
            case 4:
                getParentActivity().vibrate();
                SPH.setPingCheck(getApplicationContext(), !SPH.getPingCheck(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getPingCheck(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 5:
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!helper.showBlackListSelector()) {
                            FragmentSuperUser.this.getParentActivity().toast(FragmentSuperUser.this.getString(R.string.blacklist_no_apps),
                                    Toast.LENGTH_LONG);
                        }
                    }
                });
                break;
            case 6:
                helper.showMemorySlider();
                break;
            case 7:
                helper.showAlgorithmSelector();
                break;
            case 8:
                helper.showNoteProviderSelector();
                break;
            case 9:
                getParentActivity().vibrate();
                SPH.setSmsBodyFix(getApplicationContext(), !SPH.getSmsBodyFix(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getSmsBodyFix(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 10:
                getParentActivity().vibrate();
                SPH.setSmsIdFix(getApplicationContext(), !SPH.getSmsIdFix(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getSmsIdFix(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 11:
                getParentActivity().vibrate();
                SPH.setRecogniserBusyFix(getApplicationContext(), !SPH.getRecogniserBusyFix(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getRecogniserBusyFix(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 12:
                getParentActivity().vibrate();
                SPH.setOkayGoogleFix(getApplicationContext(), !SPH.getOkayGoogleFix(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getOkayGoogleFix(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 13:
                getParentActivity().vibrate();
                SPH.setDoubleBeepFix(getApplicationContext(), !SPH.getDoubleBeepFix(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getDoubleBeepFix(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 14:
                getParentActivity().vibrate();
                SPH.setTorchFix(getApplicationContext(), !SPH.getTorchFix(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getTorchFix(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 15:
                getParentActivity().vibrate();
                ai.saiy.android.intent.ExecuteIntent.voiceSearchHandsFree(getParentActivity());
                break;
            case 16:
                getParentActivity().vibrate();
                SettingsIntent.clearPackagePreferredActivities(getApplicationContext());
                toast(getString(R.string.success_), Toast.LENGTH_SHORT);
                break;
            case 17:
                helper.showResetDialog();
                break;
            case 18:
                if (SPH.getOverrideSecure(getApplicationContext()) || SPH.getOverrideSecureDriving(getApplicationContext()) || SPH.getOverrideSecureHeadset(getApplicationContext())) {
                    helper.showOverrideSecureSelector();
                } else {
                    helper.showOverrideSecureConfirmation();
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

        final int position = getPosition(view);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onLongClick: " + position);
        }
        getParentActivity().toast("long press!", Toast.LENGTH_SHORT);

        switch (position) {
            case 0:
                getParentActivity().speak(R.string.lp_root, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 1:
                getParentActivity().speak(R.string.lp_install_ql, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 2:
                getParentActivity().speak(R.string.lp_start_boot, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 3:
                getParentActivity().speak(R.string.lp_vocal_verification, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 4:
                getParentActivity().speak(R.string.lp_ping, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 5:
                getParentActivity().speak(R.string.lp_blacklist, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 6:
                getParentActivity().speak(R.string.lp_memory_usage, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 7:
                getParentActivity().speak(R.string.lp_algorithms_2, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 8:
                getParentActivity().speak(R.string.lp_note_provider, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 9:
                getParentActivity().speak(R.string.lp_sms_body_fix, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 10:
                getParentActivity().speak(R.string.lp_sms_id_fix, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 11:
                getParentActivity().speak(R.string.lp_recogniser_busy_fix, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 12:
                getParentActivity().speak(R.string.lp_okay_google_fix, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 13:
                getParentActivity().speak(R.string.lp_double_beep_fix, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 14:
                getParentActivity().speak(R.string.lp_flashlight_fix, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 15:
                getParentActivity().speak(R.string.lp_wired_headset_fix, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 16:
                getParentActivity().speak(R.string.lp_reset_saiy_defaults, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 17:
                getParentActivity().speak(R.string.lp_reset_default, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 18:
                getParentActivity().speak(R.string.lp_override_secure, LocalRequest.ACTION_SPEAK_ONLY);
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

        helper.enrollmentCancelled.set(true);
        helper.cancelTimer();
        getParentActivity().showProgress(false);
    }
}
