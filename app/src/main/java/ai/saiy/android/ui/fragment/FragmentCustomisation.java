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
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ai.saiy.android.R;
import ai.saiy.android.files.CachingDocumentFile;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.UIMainAdapter;
import ai.saiy.android.ui.containers.ContainerCustomisation;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.helper.FragmentCustomisationHelper;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsFile;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Created by benrandall76@gmail.com on 18/07/2016.
 */

public class FragmentCustomisation extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentCustomisation.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;
    private ArrayList<ContainerUI> mObjects;
    private FragmentCustomisationHelper helper;

    private static final Object lock = new Object();

    private Context mContext;

    public FragmentCustomisation() {
    }

    public static FragmentCustomisation newInstance(@Nullable final Bundle args) {
        return new FragmentCustomisation();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }

        helper = new FragmentCustomisationHelper(this);
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
                getParentActivity().setTitle(getString(R.string.title_customisation));
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        helper.onActivityResult(requestCode, resultCode, data);
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
                helper.showUserNameDialog();
                break;
            case 1:
                helper.showCustomIntroDialog();
                break;
            case 2:
                helper.showCustomCommandsDialog();
                break;
            case 3:
                helper.showCustomPhrasesDialog();
                break;
            case 4:
                helper.showContactPicker(FragmentCustomisationHelper.NICKNAME_REQ_CODE);
                break;
            case 5:
                helper.showCustomReplacementDialog();
                break;
            case 6:
                //TODO let user select
                if (ai.saiy.android.permissions.PermissionHelper.checkFilePermissions(getApplicationContext())) {
                    Schedulers.io().scheduleDirect(new Runnable() {
                        @Override
                        public void run() {
                            ai.saiy.android.utils.UtilsFile.createDirs(getApplicationContext());
                        }
                    });
                    helper.showSoundEffectDialog();
                }
                break;
            case 7:
                Schedulers.io().scheduleDirect(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(true);
                        final ArrayList<ContainerCustomisation> containerCustomisations = new ai.saiy.android.custom.CustomHelper().getCustomisations(getApplicationContext());
                        if (!ai.saiy.android.utils.UtilsList.notNaked(containerCustomisations)) {
                            if (isActive()) {
                                showProgress(false);
                                getParentActivity().vibrate();
                                toast(getString(R.string.no_customisations), Toast.LENGTH_SHORT);
                            }
                            return;
                        }
                        final Bundle args = new Bundle(1);
                        args.putParcelableArrayList(FragmentEditCustomisation.EXTRA_KEY, containerCustomisations);
                        if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_EDIT_CUSTOMISATION))) {
                            getParentActivity().doFragmentAddTransaction(FragmentEditCustomisation.newInstance(args), String.valueOf(ActivityHome.INDEX_FRAGMENT_EDIT_CUSTOMISATION), ActivityHome.ANIMATION_FADE, ActivityHome.INDEX_FRAGMENT_CUSTOMISATION);
                        } else if (DEBUG) {
                            MyLog.w(CLS_NAME, "onClick: INDEX_FRAGMENT_EDIT_CUSTOMISATION being added");
                        }
                    }
                });
                break;
            case 8:
                if (ai.saiy.android.utils.SPH.getImportWarning(getApplicationContext())) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.fromFile(UtilsFile.saiyDirectory(getApplicationContext())));
                        }
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        startActivityForResult(intent, FragmentCustomisationHelper.IMPORT_PICKER_REQ_CODE);
                        return;
                    }
                    Schedulers.io().scheduleDirect(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(true);
                            if (ai.saiy.android.utils.UtilsFile.createDirs(getApplicationContext())) {
                                final List<File> importFiles = ai.saiy.android.custom.imports.ImportHelper.getImportFiles(getApplicationContext());
                                if (ai.saiy.android.utils.UtilsList.notNaked(importFiles)) {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "importFiles: size: " + importFiles.size());
                                    }
                                    final ai.saiy.android.custom.imports.ImportHelper importHelper = new ai.saiy.android.custom.imports.ImportHelper();
                                    final ArrayList<Object> objectArray = importHelper.runImport(importFiles);
                                    insertCommands(importHelper, objectArray);
                                } else {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "onClick: IMPORT: importFiles: naked");
                                    }
                                    toast(getString(R.string.no_import_files_found, UtilsFile.RELATIVE_IMPORT_DIRECTORY), Toast.LENGTH_LONG);
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "onClick: IMPORT: createDirs failed");
                                }
                                toast(getString(R.string.storage_unavailable), Toast.LENGTH_LONG);
                            }
                            showProgress(false);
                        }
                    });
                } else {
                    ai.saiy.android.utils.SPH.markImportWarning(getApplicationContext());
                    helper.showImportWarningDialog();
                }
                break;
            case 9:
                if (ai.saiy.android.utils.SPH.getExportWarning(getApplicationContext())) {
                    Schedulers.io().scheduleDirect(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(true);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP || ai.saiy.android.utils.UtilsFile.createDirs(getApplicationContext())) {
                                final ArrayList<ContainerCustomisation> containerCustomisations = new ai.saiy.android.custom.CustomHelper().getCustomisations(getApplicationContext());
                                if (ai.saiy.android.utils.UtilsList.notNaked(containerCustomisations)) {
                                    final Bundle args = new Bundle(1);
                                    args.putParcelableArrayList(FragmentExportCustomisation.EXTRA_KEY, containerCustomisations);
                                    if (isActive() && !getParentActivity().isFragmentLoading(String.valueOf(ActivityHome.INDEX_FRAGMENT_EXPORT_CUSTOMISATION))) {
                                        getParentActivity().doFragmentAddTransaction(FragmentExportCustomisation.newInstance(args), String.valueOf(ActivityHome.INDEX_FRAGMENT_EXPORT_CUSTOMISATION), ActivityHome.ANIMATION_FADE, ActivityHome.INDEX_FRAGMENT_CUSTOMISATION);
                                    } else if (DEBUG) {
                                        MyLog.w(CLS_NAME, "onClick: INDEX_FRAGMENT_EXPORT_CUSTOMISATION being added");
                                    }
                                } else if (isActive()) {
                                    showProgress(false);
                                    getParentActivity().vibrate();
                                    toast(getString(R.string.title_no_commands_to_export), Toast.LENGTH_SHORT);
                                }
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "onClick: EXPORT: createDirs failed");
                                }
                                toast(getString(R.string.storage_unavailable), Toast.LENGTH_LONG);
                            }
                            showProgress(false);
                        }
                    });
                } else {
                    ai.saiy.android.utils.SPH.markExportWarning(getApplicationContext());
                    helper.showExportWarningDialog();
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
                getParentActivity().speak(R.string.lp_custom_name, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 1:
                getParentActivity().speak(R.string.lp_custom_intro, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 2:
                getParentActivity().speak(R.string.lp_custom_commands, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 3:
                getParentActivity().speak(R.string.lp_custom_phrases, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 4:
                getParentActivity().speak(R.string.lp_nicknames, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 5:
                getParentActivity().speak(R.string.lp_replacements, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 6:
                getParentActivity().speak(R.string.lp_sound_effects_2, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 7:
                getParentActivity().speak(R.string.lp_edit_customisations, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 8:
                getParentActivity().speak(R.string.lp_import_commands, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 9:
                getParentActivity().speak(R.string.lp_export_commands_2, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            default:
                break;
        }

        return true;
    }

    private void insertCommands(@NonNull ai.saiy.android.custom.imports.ImportHelper importHelper, ArrayList<Object> objectArray) {
        if (ai.saiy.android.utils.UtilsList.notNaked(objectArray)) {
            final int count = importHelper.insertCommands(getApplicationContext(), objectArray);
            switch (count) {
                case 0:
                    toast(getString(R.string.command_import_failed), Toast.LENGTH_LONG);
                    break;
                case 1:
                    toast(getString(R.string.command_imported_successfully), Toast.LENGTH_LONG);
                    break;
                default:
                    toast(getString(R.string.commands_imported_successfully, String.valueOf(count)), Toast.LENGTH_LONG);
                    break;
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onClick: IMPORT: objectArray: naked");
            }
            toast(getString(R.string.command_import_failed), Toast.LENGTH_LONG);
        }
    }

    public void importFiles(String directoryName, @NonNull DocumentFile[] documentFiles) {
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                showProgress(true);
                final List<CachingDocumentFile> importFiles = ai.saiy.android.custom.imports.ImportHelper.getImportFiles(documentFiles);
                if (ai.saiy.android.utils.UtilsList.notNaked(importFiles)) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "importFiles: size: " + importFiles.size());
                    }
                    final ai.saiy.android.custom.imports.ImportHelper importHelper = new ai.saiy.android.custom.imports.ImportHelper();
                    final ArrayList<Object> objectArray = importHelper.runImport(getApplicationContext(), importFiles);
                    insertCommands(importHelper, objectArray);
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onClick: IMPORT: importFiles: naked");
                    }
                    toast(getString(R.string.no_import_files_found, directoryName), Toast.LENGTH_LONG);
                }
                showProgress(false);
            }
        });
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
