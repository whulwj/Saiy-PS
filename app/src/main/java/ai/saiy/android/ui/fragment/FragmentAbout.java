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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.helper.FragmentAboutHelper;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.model.Notices;

/**
 * Created by benrandall76@gmail.com on 18/07/2016.
 */

public class FragmentAbout extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentAbout.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;
    private ArrayList<ContainerUI> mObjects;
    private FragmentAboutHelper helper;

    private static final Object lock = new Object();

    private Context mContext;

    public FragmentAbout() {
    }

    @SuppressWarnings("UnusedParameters")
    public static FragmentAbout newInstance(@Nullable final Bundle args) {
        return new FragmentAbout();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        helper = new FragmentAboutHelper(this);
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        this.mContext = context.getApplicationContext();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(final Activity activity) {
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
                getParentActivity().setTitle(getString(R.string.title_about));
                helper.finaliseUI();
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
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
                ExecuteIntent.webSearch(getApplicationContext(), Constants.SAIY_WEB_URL);
                break;
            case 1:
                helper.showReleaseNotes();
                break;
            case 2:
                ExecuteIntent.webSearch(getApplicationContext(), Constants.SAIY_PRIVACY_URL);
                break;
            case 3:
                ExecuteIntent.webSearch(getApplicationContext(), Constants.SAIY_TOU_URL);
                break;
            case 4:
                prepareLicenses();
                break;
            case 5:
                getParentActivity().vibrate();
                SPH.setAnonymousUsageStats(getApplicationContext(), !SPH.getAnonymousUsageStats(getApplicationContext()));
                mObjects.get(position).setIconExtra(SPH.getAnonymousUsageStats(getApplicationContext()) ?
                        FragmentHome.CHECKED : FragmentHome.UNCHECKED);
                mAdapter.notifyItemChanged(position);
                break;
            case 6:
                ExecuteIntent.webSearch(getApplicationContext(), Constants.SAIY_GITHUB_URL);
                break;
            case 7:
                ExecuteIntent.webSearch(getApplicationContext(), Constants.SAIY_GITHUB_URL);
                break;
            case 8:
                ExecuteIntent.webSearch(getApplicationContext(), Constants.STUDIO_AHREMARK_WEB_URL);
                break;
            case 9:
                ExecuteIntent.webSearch(getApplicationContext(), Constants.SCHLAPA_WEB_URL);
                break;
            case 10:
                helper.showLegendsDialog();
                break;
            case 11:
                if (!ExecuteIntent.sendEmail(getApplicationContext(), new String[]{Constants.SAIY_ENQUIRIES_EMAIL},
                        getString(R.string.menu_commercial_enquiry), null)) {
                    getParentActivity().toast(getString(R.string.error_no_application),
                            Toast.LENGTH_LONG);
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
                getParentActivity().speak(R.string.lp_saiy_version, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 1:
                getParentActivity().speak(R.string.lp_release_notes, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 2:
                getParentActivity().speak(R.string.lp_privacy_policy, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 3:
                getParentActivity().speak(R.string.lp_terms_of_use, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 4:
                getParentActivity().speak(R.string.lp_license, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 5:
                getParentActivity().speak(R.string.lp_anonymous_usage_stats, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 6:
                getParentActivity().speak(R.string.lp_source_code, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 7:
                getParentActivity().speak(R.string.lp_developer_api, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 8:
                getParentActivity().speak(R.string.lp_hannes, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 9:
                getParentActivity().speak(R.string.lp_schlapa, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 10:
                getParentActivity().speak(R.string.lp_legends, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 11:
                getParentActivity().speak(R.string.lp_commercial, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * Prepare the license information to show to the user
     */
    private void prepareLicenses() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "prepareLicenses");
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final Notices notices = helper.getLicenses();
                FragmentAbout.this.getParentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        new LicensesDialog.Builder(FragmentAbout.this.getParentActivity())
                                .setNotices(notices).setIncludeOwnLicense(true)
                                .setShowFullLicenseText(false)
                                .setCloseText(R.string.close).build().show();
                    }
                });
            }
        });
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
