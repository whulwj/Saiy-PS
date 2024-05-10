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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.DividerItemDecoration;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.FragmentCustomisation;
import ai.saiy.android.ui.fragment.FragmentHome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

/**
 * Utility class to assist its parent fragment and avoid clutter there
 * <p>
 * Created by benrandall76@gmail.com on 25/07/2016.
 */

public class FragmentCustomisationHelper {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentCustomisationHelper.class.getSimpleName();

    private final FragmentCustomisation parentFragment;

    /**
     * Constructor
     *
     * @param parentFragment the parent fragment for this helper class
     */
    public FragmentCustomisationHelper(@NonNull final FragmentCustomisation parentFragment) {
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
        final int chevronResource = R.drawable.chevron;

        ContainerUI containerUI = new ContainerUI();
        containerUI.setTitle(getParent().getString(R.string.menu_custom_intro));
        containerUI.setSubtitle(getParent().getString(R.string.menu_tap_configure));
        containerUI.setIconMain(R.drawable.ic_crown);
        containerUI.setIconExtra(chevronResource);
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
     * Update the parent fragment with the UI components
     */
    public void finaliseUI() {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ContainerUI> tempArray = FragmentCustomisationHelper.this.getUIComponents();

                if (FragmentCustomisationHelper.this.getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)) {

                    try {
                        Thread.sleep(FragmentHome.DRAWER_CLOSE_DELAY);
                    } catch (final InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                            e.printStackTrace();
                        }
                    }
                }

                if (FragmentCustomisationHelper.this.getParent().isActive()) {

                    FragmentCustomisationHelper.this.getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        FragmentCustomisationHelper.this.getParent().getObjects().addAll(tempArray);
                        FragmentCustomisationHelper.this.getParent().getAdapter().notifyItemRangeInserted(0, FragmentCustomisationHelper.this.getParent().getObjects().size());
                    }});

                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                    }
                }
            }
        });
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
     * Show the custom intro dialog
     */
    @SuppressWarnings("ConstantConditions")
    public void showCustomIntroDialog() {

        final String currentIntro = SPH.getCustomIntro(getApplicationContext());
        String hint = null;
        String content = null;

        if (currentIntro != null) {
            if (currentIntro.isEmpty()) {
                hint = getApplicationContext().getString(R.string.silence);
            } else {
                content = currentIntro;
            }
        } else {
            hint = getApplicationContext().getString(R.string.custom_intro_hint);
        }

        final View customView = LayoutInflater.from(getParentActivity()).inflate(R.layout.md_dialog_input, null, false);
        final CheckBox checkBox = customView.findViewById(R.id.md_promptCheckbox);
        checkBox.setText(R.string.custom_intro_checkbox_title);
        checkBox.setChecked(SPH.getCustomIntroRandom(getApplicationContext()));
        final EditText editText = customView.findViewById(android.R.id.input);
        editText.setHint(hint);
        editText.setText(content);

        final AlertDialog materialDialog = new MaterialAlertDialogBuilder(getParentActivity())
                .setView(customView)
                .setTitle(R.string.menu_custom_intro)
                .setIcon(R.drawable.ic_crown)
                .setMessage(R.string.custom_intro_text)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final CharSequence input = editText.getText();
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "showCustomIntroDialog: input: " + input);
                        }

                        if (input != null) {
                            if (UtilsString.notNaked(input.toString())) {
                                SPH.setCustomIntroRandom(FragmentCustomisationHelper.this.getApplicationContext(),
                                        checkBox.isChecked());
                                SPH.setCustomIntro(FragmentCustomisationHelper.this.getApplicationContext(), input.toString().trim());
                            } else {
                                SPH.setCustomIntroRandom(FragmentCustomisationHelper.this.getApplicationContext(), false);
                                SPH.setCustomIntro(FragmentCustomisationHelper.this.getApplicationContext(), "");
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

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
    public FragmentCustomisation getParent() {
        return parentFragment;
    }
}
