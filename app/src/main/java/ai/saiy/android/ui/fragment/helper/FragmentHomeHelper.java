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
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;

/**
 * Utility class to assist its parent fragment and avoid clutter there
 * <p>
 * Created by benrandall76@gmail.com on 25/07/2016.
 */

public class FragmentHomeHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentHomeHelper.class.getSimpleName();

    private final FragmentHome parentFragment;

    /**
     * Constructor
     *
     * @param parentFragment the parent fragment for this helper class
     */
    public FragmentHomeHelper(@NonNull final FragmentHome parentFragment) {
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
        containerUI.setTitle(getParent().getString(R.string.menu_voice_tutorial));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_begin));
        containerUI.setIconMain(R.drawable.ic_text_to_speech);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_user_guide));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_topics));
        containerUI.setIconMain(R.drawable.ic_library);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_command_list));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_view));
        containerUI.setIconMain(R.drawable.ic_command_list);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_development));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_contribute));
        containerUI.setIconMain(R.drawable.ic_pulse);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.title_settings));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_cog);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.title_customisation));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_tweak));
        containerUI.setIconMain(R.drawable.ic_fingerprint);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.title_advanced));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_pill);
        containerUI.setIconExtra(FragmentHome.CHEVRON);
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.title_troubleshooting_bugs));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_help));
        containerUI.setIconMain(R.drawable.ic_bug);
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
     * Update the parent fragment with the UI components. If the drawer is not open in the parent
     * Activity, we can assume this method is called as a result of the back button being pressed, or
     * the first initialisation of the application - neither of which require a delay.
     */
    public void finaliseUI() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ContainerUI> tempArray = FragmentHomeHelper.this.getUIComponents();

                if (FragmentHomeHelper.this.getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)) {

                    try {
                        Thread.sleep(FragmentHome.DRAWER_CLOSE_DELAY);
                    } catch (final InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                            e.printStackTrace();
                        }
                    }
                }

                if (FragmentHomeHelper.this.getParent().isActive()) {

                    FragmentHomeHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            FragmentHomeHelper.this.getParent().getObjects().addAll(tempArray);
                            FragmentHomeHelper.this.getParent().getAdapter().notifyItemRangeInserted(0, FragmentHomeHelper.this.getParent().getObjects().size());
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

    public void showUserGuideDialog() {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setTitle(R.string.menu_user_guide)
                .setIcon(R.drawable.ic_library)
        .setItems(getParentActivity().getResources().getStringArray(R.array.array_user_guide), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Global.isInVoiceTutorial()) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "onClick: tutorialActive");
                            }
                            getParentActivity().toast(getParent().getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
                            return;
                        }
                        switch (which) {
                            case 0:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_BASIC");
                                }
                                ExecuteIntent.webSearch(getApplicationContext(), Constants.USER_GUIDE_BASIC);
                                break;
                            case 1:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_CUSTOM_COMMANDS");
                                }
                                ExecuteIntent.webSearch(getApplicationContext(), Constants.USER_CUSTOM_COMMANDS);
                                break;
                            case 2:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_CUSTOM_REPLACEMENTS");
                                }
                                ExecuteIntent.webSearch(getApplicationContext(), Constants.USER_CUSTOM_REPLACEMENTS);
                                break;
                            case 3:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_SOUND_EFFECTS");
                                }
                                ExecuteIntent.webSearch(getApplicationContext(), Constants.USER_SOUND_EFFECTS);
                                break;
                            case 4:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_TASKER");
                                }
                                ExecuteIntent.webSearch(getApplicationContext(), Constants.USER_TASKER);
                                break;
                            case 5:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_TROUBLESHOOTING");
                                }
                                ExecuteIntent.webSearch(getApplicationContext(), Constants.USER_TROUBLESHOOTING);
                                break;
                            case 6:
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "showUserGuideDialog: UG_COMING_SOON");
                                }
                                ExecuteIntent.webSearch(getApplicationContext(), Constants.USER_COMING_SOON);
                                break;
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showUnknownCommandSelector: onNegative");
                        }
                    }
                }).create();
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
    public FragmentHome getParent() {
        return parentFragment;
    }
}
