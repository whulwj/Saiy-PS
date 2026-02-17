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
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import ai.saiy.android.R;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.UIBugsAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentBugs;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.utils.MyLog;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Utility class to assist its parent fragment and avoid clutter there
 * <p>
 * Created by benrandall76@gmail.com on 25/07/2016.
 */

public class FragmentBugsHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentHomeHelper.class.getSimpleName();

    private final FragmentBugs parentFragment;

    /**
     * Constructor
     *
     * @param parentFragment the parent fragment for this helper class
     */
    public FragmentBugsHelper(@NonNull final FragmentBugs parentFragment) {
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
        containerUI.setTitle(getParent().getString(R.string.bug_title));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.ad_title));
        containerUI.setSubtitle(getParent().getString(R.string.ad_content));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.utter_title));
        containerUI.setSubtitle(getParent().getString(R.string.utter_content));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_recognition_time));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_recognition_time));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_consider_silence));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_consider_silence));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_alexa));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_alexa));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_offline_recognition));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_offline_recognition));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_no_speech));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_no_speech));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_delay_speech));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_delay_speech));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_recognition_result));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_recognition_result));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_large_memory));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_large_memory));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_high_battery));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_high_battery));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_hotword_work));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_hotword_work));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_customise_hotword));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_customise_hotword));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_okay_google));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_okay_google));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_message_incorrect));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_message_incorrect));;
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_recognizer_busy));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_recognizer_busy));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_beeps_twice));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_beeps_twice));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_driving_profile));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_driving_profile));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_shut_down));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_shut_down));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_toggle_mobile_data));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_toggle_mobile_data));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_answer_call));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_answer_call));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_activate));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_activate));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_tts_engine));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_tts_engine));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_speech_recognition));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_speech_recognition));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_reply));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_reply));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_sms_body));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_sms_body));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_link_tasker));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_link_tasker));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_bluetooth_headset));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_bluetooth_headset));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_wired_headset));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_wired_headset));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_pronounce));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_pronounce));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_permanent_notification));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_permanent_notification));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_snowman));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_snowman));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_slow_process));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_slow_process));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.bug_title_feature_disabled));
        containerUI.setSubtitle(getParent().getString(R.string.bug_content_feature_disabled));
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Skype ID's aren't detected?");
        containerUI.setSubtitle("In order for Saiy to detect the contact's Skype ID, you need to make sure this is stored correctly within their contact card. It should be entered under the 'IM' field heading, with the label option set to 'Skype'.");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Can I ask Saiy any question I like?");
        containerUI.setSubtitle("Such knowledge-based responses will rely heavily on Wolfram Alpha, which is disabled currently due to the reasons above. In the Settings Section, if you set the 'unknown commands' option to Google, then anything the application doesn't understand, will be passed to the Google Assistant to have a go!\n\nTap for the Settings Section");
        mObjects.add(containerUI);

        containerUI = new ContainerUI();
        containerUI.setTitle("Will Saiy be multilingual?");
        containerUI.setSubtitle("It will indeed! I'll be entirely reliant on the help of my users to achieve this. There is a link in the Development Section if you'd like to get involved. Thanks in advance!\n\nTap for the Development Section");
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
                parent.findViewById(R.id.layout_bugs_fragment_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        return mRecyclerView;
    }

    /**
     * Get the Edit Text for this fragment
     *
     * @param parent the view parent
     * @return the {@link EditText}
     */
    public EditText getEditText(@NonNull final View parent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getEditText");
        }

        final EditText editText = (EditText) parent.findViewById(R.id.etCommand);
        editText.setOnEditorActionListener(getParent());
        editText.setImeActionLabel(getParent().getString(R.string.menu_run), EditorInfo.IME_ACTION_GO);
        return editText;
    }

    /**
     * Get the Image Button for this fragment
     *
     * @param parent the view parent
     * @return the {@link ImageButton}
     */
    public ImageButton getImageButton(@NonNull final View parent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getImageButton");
        }
        final ImageButton imageButton = (ImageButton) parent.findViewById(R.id.ibRun);
        imageButton.setOnClickListener(getParent());
        return imageButton;
    }

    /**
     * Get the adapter for this fragment
     *
     * @param mObjects list of {@link ContainerUI} elements
     * @return the {@link UIBugsAdapter}
     */
    public UIBugsAdapter getAdapter(@NonNull final ArrayList<ContainerUI> mObjects) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdapter");
        }
        return new UIBugsAdapter(mObjects, getParent(), getParent());
    }

    /**
     * Update the parent fragment with the UI components. If the drawer is not open in the parent
     * Activity, we can assume this method is called as a result of the back button being pressed, or
     * the first initialisation of the application - neither of which require a delay.
     */
    public void finaliseUI() {
        Schedulers.single().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ContainerUI> tempArray = FragmentBugsHelper.this.getUIComponents();
                if (FragmentBugsHelper.this.getParent().isActive()) {

                    FragmentBugsHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        FragmentBugsHelper.this.getParent().getObjects().addAll(tempArray);
                        FragmentBugsHelper.this.getParent().getAdapter().notifyItemRangeInserted(0, FragmentBugsHelper.this.getParent().getObjects().size());
                    }});

                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                    }
                }
            }
        }, getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)? FragmentHome.DRAWER_CLOSE_DELAY : 0, TimeUnit.MILLISECONDS);
    }

    public void showLatestBugsDialog(ArrayList<String> bugs) {
        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setTitle(R.string.menu_latest_bugs)
                .setMessage(R.string.content_override_secure)
                .setIcon(R.drawable.ic_bug)
                .setItems(bugs.toArray(new String[0]), null)
                .setPositiveButton(R.string.menu_squash_them, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showLatestBugsDialog: onPositive");
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showLatestBugsDialog: onCancel");
                        }
                    }
                }).create();

        materialDialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation_right;
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
    public FragmentBugs getParent() {
        return parentFragment;
    }
}
