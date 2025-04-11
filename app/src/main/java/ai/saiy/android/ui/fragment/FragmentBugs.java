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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.command.settings.SettingsIntent;
import ai.saiy.android.firebase.database.read.KnownBug;
import ai.saiy.android.firebase.database.read.KnownBugs;
import ai.saiy.android.firebase.database.reference.KnownBugsReference;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.recognition.TestRecognitionAction;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.UIBugsAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.helper.FragmentBugsHelper;
import ai.saiy.android.ui.viewmodel.ViewModelBilling;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Created by benrandall76@gmail.com on 18/07/2016.
 */
@AndroidEntryPoint
public class FragmentBugs extends Fragment implements View.OnClickListener, View.OnLongClickListener,
        TextView.OnEditorActionListener {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentBugs.class.getSimpleName();

    private static final int IME_GO = 99;

    private EditText editText;
    private ImageButton imageButton;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;
    private ArrayList<ContainerUI> mObjects;
    private FragmentBugsHelper helper;
    private ViewModelBilling viewModelBilling;

    private static final Object lock = new Object();

    private Context mContext;

    public FragmentBugs() {
    }

    @SuppressWarnings("UnusedParameters")
    public static FragmentBugs newInstance(@Nullable final Bundle args) {
        return new FragmentBugs();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        setHasOptionsMenu(true);
        helper = new FragmentBugsHelper(this);
        requestKnownBugs(false);
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

    /**
     * The RecyclerView in all fragments is initialised with an empty adapter. We start any heavy lifting
     * here to ensure that the transition between any fragments does not cause any visual lag (stuttering).
     * <p>
     * As our fragments don't contain any dynamic content, we only need to check if the adapter content is
     * empty, to know this is the first time we've received this callback. We synchronise the process, just
     * to avoid any weird and wonderful situations that never happen....
     */
    @Override
    public void onStart() {
        super.onStart();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStart");
        }

        synchronized (lock) {
            if (mObjects.isEmpty()) {
                getParentActivity().setTitle(getString(R.string.title_troubleshooting));
                helper.finaliseUI();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateOptionsMenu");
        }
        menuInflater.inflate(R.menu.menu_bugs, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateView");
        }
        final ViewModelProvider viewModelProvider = new ViewModelProvider(getActivity());
        this.viewModelBilling = viewModelProvider.get(ViewModelBilling.class);

        final View rootView = inflater.inflate(R.layout.fragment_bugs_layout, container, false);
        editText = helper.getEditText(rootView);
        imageButton = helper.getImageButton(rootView);
        mRecyclerView = helper.getRecyclerView(rootView);
        mObjects = new ArrayList<>();
        mAdapter = helper.getAdapter(mObjects);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (DEBUG) {
            MyLog.i(CLS_NAME,  "oonOptionsItemSelected");
        }
        if (R.id.action_bugs == menuItem.getItemId()) {
            requestKnownBugs(true);
            return true;
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
    }

    private int getPosition(View view) {
        int position = (view == null) ? 0 : mRecyclerView.getChildAdapterPosition(view);
        if (view != null && RecyclerView.NO_POSITION == position) {
            final RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(view);
            if (viewHolder instanceof UIBugsAdapter.ViewHolder) {
                position = ((UIBugsAdapter.ViewHolder) viewHolder).getBoundPosition();
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

        if (view != null && R.id.ibRun == view.getId()) {
            testCommand();
        } else {
            final int position = getPosition(view);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onClick: " + position);
            }
            switch (position) {
                case 0:
                case 18:
                case 33:
                case 37:
                    if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_DEVELOPMENT))) {
                        getParentActivity().doFragmentReplaceTransaction(FragmentDevelopment.newInstance(null), String.valueOf(ActivityHome.INDEX_FRAGMENT_DEVELOPMENT), ActivityHome.ANIMATION_FADE);
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onClick: INDEX_FRAGMENT_DEVELOPMENT being added");
                        }
                    }
                    break;
                case 6:
                    if (ExecuteIntent.showInstallOfflineVoiceFiles(getApplicationContext())) {
                        return;
                    }
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onClick: SETTINGS_OFFLINE");
                    }
                    if (!ExecuteIntent.settingsIntent(getApplicationContext(),
                            IntentConstants.SETTINGS_VOICE_SEARCH)) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onClick: SETTINGS_VOICE_SEARCH");
                        }
                        ExecuteIntent.settingsIntent(getApplicationContext(),
                                IntentConstants.SETTINGS_INPUT_METHOD);
                    }
                    break;
                case 7:
                case 23:
                    ExecuteIntent.settingsIntent(getApplicationContext(),
                            IntentConstants.SETTINGS_TEXT_TO_SPEECH);
                    break;
                case 8:
                case 36:
                    if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_SETTINGS))) {
                        getParentActivity().doFragmentAddTransaction(FragmentSettings.newInstance(null),
                                String.valueOf(ActivityHome.INDEX_FRAGMENT_SETTINGS), ActivityHome.ANIMATION_FADE,
                                ActivityHome.INDEX_FRAGMENT_BUGS);
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onClick: INDEX_FRAGMENT_SETTINGS being added");
                        }
                    }
                    break;
                case 10:
                case 14:
                case 15:
                case 16:
                case 26:
                case 29:
                    if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_SUPER_USER))) {
                        getParentActivity().doFragmentAddTransaction(FragmentSettings.newInstance(null),
                                String.valueOf(ActivityHome.INDEX_FRAGMENT_SUPER_USER), ActivityHome.ANIMATION_FADE,
                                ActivityHome.INDEX_FRAGMENT_BUGS);
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onClick: INDEX_FRAGMENT_SUPER_USER being added");
                        }
                    }
                    break;
                case 11:
                    SettingsIntent.settingsIntent(getApplicationContext(), SettingsIntent.Type.ACCESSIBILITY);
                    break;
                case 22:
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        toast(getString(R.string.device_not_supported), Toast.LENGTH_SHORT);
                    } else {
                        if (ExecuteIntent.showInstallOfflineVoiceFiles(getApplicationContext())) {
                            break;
                        }
                        SettingsIntent.settingsIntent(getApplicationContext(), SettingsIntent.Type.APPLICATION);
                        toast(getString(R.string.error_assistant_settings), Toast.LENGTH_LONG);
                    }
                    break;
                case 24:
                case 30:
                    if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_CUSTOMISATION))) {
                        getParentActivity().doFragmentAddTransaction(FragmentSettings.newInstance(null),
                                String.valueOf(ActivityHome.INDEX_FRAGMENT_CUSTOMISATION), ActivityHome.ANIMATION_FADE,
                                ActivityHome.INDEX_FRAGMENT_BUGS);
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onClick: INDEX_FRAGMENT_CUSTOMISATION being added");
                        }
                    }
                    break;
                case 27:
                    if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_SUPPORTED_APPS))) {
                        getParentActivity().doFragmentAddTransaction(FragmentSettings.newInstance(null),
                                String.valueOf(ActivityHome.INDEX_FRAGMENT_SUPPORTED_APPS), ActivityHome.ANIMATION_FADE,
                                ActivityHome.INDEX_FRAGMENT_BUGS);
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onClick: INDEX_FRAGMENT_SUPPORTED_APPS being added");
                        }
                    }
                    break;
                case 28:
                    SettingsIntent.settingsIntent(getApplicationContext(), SettingsIntent.Type.VOICE_SEARCH);
                    break;
                default:
                    break;
            }
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
        switch (position) {
            case 0:
                return false;
            case 7:
                ExecuteIntent.settingsIntent(getApplicationContext(), IntentConstants.SETTINGS_VOLUME);
                break;
            case 8:
                if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_SUPER_USER))) {
                    getParentActivity().doFragmentAddTransaction(FragmentSettings.newInstance(null),
                            String.valueOf(ActivityHome.INDEX_FRAGMENT_SUPER_USER), ActivityHome.ANIMATION_FADE,
                            ActivityHome.INDEX_FRAGMENT_BUGS);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onLongClick: INDEX_FRAGMENT_SUPER_USER being added");
                    }
                }
                break;
            case 14:
                UtilsApplication.openApplicationSpecificSettings(getApplicationContext(), "com.google.android.googlequicksearchbox");
                break;
            case 22:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    toast(getString(R.string.device_not_supported), Toast.LENGTH_SHORT);
                } else if (!SettingsIntent.settingsIntent(getApplicationContext(), SettingsIntent.Type.QUICK_LAUNCH)) {
                    SettingsIntent.settingsIntent(getApplicationContext(), SettingsIntent.Type.SETTINGS);
                }
                break;
            case 24:
            case 28:
                if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_DEVELOPMENT))) {
                    getParentActivity().doFragmentReplaceTransaction(FragmentDevelopment.newInstance(null), String.valueOf(ActivityHome.INDEX_FRAGMENT_DEVELOPMENT), ActivityHome.ANIMATION_FADE);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onLongClick: INDEX_FRAGMENT_DEVELOPMENT being added");
                    }
                }
                break;
            default:
                return false;
        }

        return true;
    }

    /**
     * Called when an action is being performed.
     *
     * @param v        The view that was clicked.
     * @param actionId Identifier of the action.  This will be either the
     *                 identifier you supplied, or {@link EditorInfo#IME_NULL
     *                 EditorInfo.IME_NULL} if being called due to the enter key
     *                 being pressed.
     * @param event    If triggered by an enter key, this is the event;
     *                 otherwise, this is null.
     * @return Return true if you have consumed the action, else false.
     */
    @Override
    public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onEditorAction: " + actionId);
        }

        switch (actionId) {

            case EditorInfo.IME_ACTION_GO:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onEditorAction: IME_ACTION_GO");
                }
                testCommand();
                return true;
            case IME_GO:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onEditorAction: IME_GO");
                }
                testCommand();
                return true;
            default:
                break;

        }

        return false;
    }

    /**
     * Run a test command input by the user
     */
    private void testCommand() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "testCommand");
        }

        if (editText.getText() != null) {

            final String commandText = editText.getText().toString();

            if (UtilsString.notNaked(commandText)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "testCommand: executing: " + commandText);
                }

                hideIME();

                new TestRecognitionAction(getApplicationContext(), viewModelBilling, commandText.trim());

            } else {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "testCommand: text naked");
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "testCommand: getText null");
            }
        }
    }

    /**
     * Hide the IME once the input is complete
     */
    private void hideIME() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "hideIME");
        }
        ((InputMethodManager)
                getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(editText.getApplicationWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
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

    private void requestKnownBugs(boolean forceUpdate) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "requestKnownBugs: " + forceUpdate);
        }
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                final KnownBugs requestKnownBugsList = new KnownBugsReference().getRequestKnownBugsList();
                if (requestKnownBugsList == null) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "requestKnownBugs: requestKnownBugsList: null");
                    }
                    return;
                }
                final List<KnownBug> knownBugsList = requestKnownBugsList.getBugs();
                if (!ai.saiy.android.utils.UtilsList.notNaked(knownBugsList)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "requestKnownBugs: knownBugsList: naked");
                    }
                    return;
                }
                final String previousContent = ai.saiy.android.utils.SPH.getLatestBugs(getApplicationContext());
                final String currentContent = requestKnownBugsList.toString();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "requestKnownBugs: currentContent: " + currentContent);
                }
                if (forceUpdate) {
                    ai.saiy.android.utils.SPH.setLatestBugs(getApplicationContext(), currentContent);
                } else if (ai.saiy.android.utils.UtilsString.notNaked(previousContent)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "requestKnownBugs: previousContent: " + previousContent);
                    }
                    if (previousContent.matches(Pattern.quote(currentContent))) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "requestKnownBugs: previous content matches");
                        }
                        int shownCount = ai.saiy.android.utils.SPH.getLatestBugsCount(getApplicationContext());
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "requestKnownBugs: shownCount: " + shownCount);
                        }
                        if (shownCount > 1) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "requestKnownBugs: seen twice already");
                            }
                            return;
                        }
                        ai.saiy.android.utils.SPH.updateLatestBugsCount(getApplicationContext());
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "requestKnownBugs: previous content doesn't match");
                        }
                        ai.saiy.android.utils.SPH.setLatestBugs(getApplicationContext(), currentContent);
                        ai.saiy.android.utils.SPH.resetLatestBugsCount(getApplicationContext());
                        ai.saiy.android.utils.SPH.updateLatestBugsCount(getApplicationContext());
                    }
                } else {
                    ai.saiy.android.utils.SPH.updateLatestBugsCount(getApplicationContext());
                    ai.saiy.android.utils.SPH.setLatestBugs(getApplicationContext(), currentContent);
                }
                final int size = knownBugsList.size();
                final ArrayList<String> bugs = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "bug: getTitle: " + knownBugsList.get(i).getTitle());
                        MyLog.i(CLS_NAME, "bug: getContent: " + knownBugsList.get(i).getContent());
                        MyLog.i(CLS_NAME, "---------------------------------");
                    }
                    bugs.add(("• " + knownBugsList.get(i).getTitle() + "\n\n" + knownBugsList.get(i).getContent()).replaceAll("_b", "\n"));
                }
                if (!ai.saiy.android.utils.UtilsList.notNaked(bugs)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "requestKnownBugs: content: naked");
                    }
                } else if (isActive()) {
                    getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            helper.showLatestBugsDialog(bugs);
                        }
                    });
                } else if (DEBUG) {
                    MyLog.i(CLS_NAME, "requestKnownBugs: no longer active");
                }
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
