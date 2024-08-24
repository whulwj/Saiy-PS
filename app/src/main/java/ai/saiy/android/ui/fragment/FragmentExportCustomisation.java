package ai.saiy.android.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.saiy.android.R;
import ai.saiy.android.custom.CustomCommand;
import ai.saiy.android.custom.CustomNickname;
import ai.saiy.android.custom.CustomPhrase;
import ai.saiy.android.custom.CustomReplacement;
import ai.saiy.android.custom.exports.ExportHelper;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.UIExportCustomisationAdapter;
import ai.saiy.android.ui.containers.ContainerCustomisation;
import ai.saiy.android.ui.fragment.helper.FragmentExportCustomisationHelper;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;

public class FragmentExportCustomisation extends Fragment implements View.OnClickListener {
    public static final String EXTRA_KEY = "container_customisation_key";
    private static final Object lock = new Object();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;
    private ArrayList<ContainerCustomisation> mObjects;
    private FragmentExportCustomisationHelper helper;
    private Context mContext;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentExportCustomisation.class.getSimpleName();

    private boolean isAllChecked = false;

    public static FragmentExportCustomisation newInstance(Bundle args) {
        final FragmentExportCustomisation fragmentExportCustomisation = new FragmentExportCustomisation();
        fragmentExportCustomisation.setArguments(args);
        return fragmentExportCustomisation;
    }

    public List<ContainerCustomisation> getObjectsFromArguments() {
        final Bundle args = getArguments();
        if (args != null && args.containsKey(EXTRA_KEY)) {
            return args.getParcelableArrayList(EXTRA_KEY);
        } else {
            return Collections.emptyList();
        }
    }

    private boolean runExport(ArrayList<ContainerCustomisation> arrayList) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "runExport: size: " + arrayList.size());
        }
        final ExportHelper exportHelper = new ExportHelper();
        if (!exportHelper.exportProceed(getApplicationContext())) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "exportProceed: false");
            }
            return false;
        }
        final com.google.gson.Gson gson = new com.google.gson.GsonBuilder().disableHtmlEscaping().create();
        for (ContainerCustomisation containerCustomisation : arrayList) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getCustom: " + containerCustomisation.getCustom().name());
            }
            String serialised = containerCustomisation.getSerialised();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "serialised: " + serialised);
            }
            switch (containerCustomisation.getCustom()) {
                case CUSTOM_PHRASE:
                    final CustomPhrase customPhrase = gson.fromJson(serialised, new com.google.gson.reflect.TypeToken<CustomPhrase>() {
                    }.getType());
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "customPhrase: " + customPhrase.getKeyphrase());
                    }
                    exportHelper.exportCustomPhrase(customPhrase);
                    break;
                case CUSTOM_NICKNAME:
                    final CustomNickname customNickname = gson.fromJson(serialised, new com.google.gson.reflect.TypeToken<CustomNickname>() {
                    }.getType());
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "customNickname: " + customNickname.getNickname());
                    }
                    exportHelper.exportCustomNickname(customNickname);
                    break;
                case CUSTOM_REPLACEMENT:
                    final CustomReplacement customReplacement = (CustomReplacement) gson.fromJson(serialised, new com.google.gson.reflect.TypeToken<CustomReplacement>() {
                    }.getType());
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "customReplacement: " + customReplacement.getKeyphrase());
                    }
                    exportHelper.exportCustomReplacement(customReplacement);
                    break;
                case CUSTOM_COMMAND:
                    final CustomCommand customCommand = gson.fromJson(serialised, new com.google.gson.reflect.TypeToken<CustomCommand>() {
                    }.getType());
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "customCommandContainer: " + customCommand.getKeyphrase());
                    }
                    switch (customCommand.getCustomAction()) {
                        case CUSTOM_DISPLAY_CONTACT:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "CUSTOM_DISPLAY_CONTACT");
                            }
                            break;
                        case CUSTOM_TASKER_TASK:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "CUSTOM_TASKER_TASK");
                            }
                            break;
                        case CUSTOM_ACTIVITY:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "CUSTOM_ACTIVITY");
                            }
                            break;
                        case CUSTOM_CALL_CONTACT:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "CUSTOM_CALL_CONTACT");
                            }
                            break;
                        case CUSTOM_LAUNCH_APPLICATION:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "CUSTOM_LAUNCH_APPLICATION");
                            }
                            break;
                        case CUSTOM_LAUNCH_SHORTCUT:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "CUSTOM_LAUNCH_SHORTCUT");
                            }
                            break;
                        case CUSTOM_AUTOMATE_FLOW:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "CUSTOM_AUTOMATE_FLOW");
                            }
                            break;
                        case CUSTOM_SEARCHABLE:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "CUSTOM_SEARCHABLE");
                            }
                            break;
                        case CUSTOM_INTENT_SERVICE:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "CUSTOM_INTENT_SERVICE");
                            }
                            break;
                        case CUSTOM_HTTP:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "CUSTOM_HTTP");
                            }
                            break;
                        case CUSTOM_SEND_INTENT:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "CUSTOM_SEND_INTENT");
                            }
                            break;
                        default:
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "default custom type?");
                            }
                            break;
                    }
                    exportHelper.exportCustomCommand(customCommand);
                    break;
            }
        }
        return true;
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

    public void onBackPressed() {
        if (isActive()) {
            getParentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getParentActivity().onBackPressed();
                }
            });
        }
    }

    public ActivityHome getParentActivity() {
        return (ActivityHome) getActivity();
    }

    public Context getApplicationContext() {
        return mContext;
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return mAdapter;
    }

    public ArrayList<ContainerCustomisation> getObjects() {
        return mObjects;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.mContext = activity.getApplicationContext();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context.getApplicationContext();
    }

    @Override
    public void onClick(View view) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClick: " + view.getTag());
        }
        if (Global.isInVoiceTutorial()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onClick: tutorialActive");
            }
            toast(getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
            return;
        }
        final int position = (Integer) view.getTag();
        boolean isChecked = ((UIExportCustomisationAdapter) getAdapter()).getCheckedArray().get(position);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClick: isChecked; " + isChecked);
        }
        ((UIExportCustomisationAdapter) getAdapter()).getCheckedArray().put(position, !isChecked);
        getAdapter().notifyItemChanged(position);
        if (DEBUG) {
            ContainerCustomisation containerCustomisation = mObjects.get(position);
            MyLog.i(CLS_NAME, "onClick: getTitle: " + containerCustomisation.getTitle());
            MyLog.i(CLS_NAME, "onClick: getSubtitle: " + containerCustomisation.getSubtitle());
            SparseBooleanArray sparseBooleanArray = ((UIExportCustomisationAdapter) getAdapter()).getCheckedArray();
            int size = sparseBooleanArray.size();
            for (int i = 0; i < size; i++) {
                int key = sparseBooleanArray.keyAt(i);
                boolean value = sparseBooleanArray.get(key);
                MyLog.i(CLS_NAME, "sparse array: key: " + key + " ~ value: " + value);
                if (value) {
                    ContainerCustomisation cc = mObjects.get(key);
                    MyLog.i(CLS_NAME, "sparse array: getTitle: " + cc.getTitle());
                    MyLog.i(CLS_NAME, "sparse array: getSubtitle: " + cc.getSubtitle());
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        setHasOptionsMenu(true);
        this.helper = new FragmentExportCustomisationHelper(this);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateOptionsMenu");
        }
        menuInflater.inflate(R.menu.menu_export, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateView");
        }
        View inflate = layoutInflater.inflate(R.layout.layout_common_fragment_parent, viewGroup, false);
        this.mRecyclerView = helper.getRecyclerView(inflate);
        this.mObjects = new ArrayList<>();
        this.mAdapter = helper.getAdapter(mObjects);
        this.mRecyclerView.setAdapter(mAdapter);
        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onOptionsItemSelected");
        }
        if (R.id.action_export == menuItem.getItemId()) {
            if (mObjects.isEmpty() || !isActive()) {
                return true;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    showProgress(true);
                    SparseBooleanArray sparseBooleanArray = ((UIExportCustomisationAdapter) getAdapter()).getCheckedArray();
                    int size = sparseBooleanArray.size();
                    ArrayList<ContainerCustomisation> arrayList = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        int key = sparseBooleanArray.keyAt(i);
                        boolean value = sparseBooleanArray.get(key);
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "sparse array: key: " + key + " ~ value: " + value);
                        }
                        if (value) {
                            ContainerCustomisation containerCustomisation = mObjects.get(key);
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "sparse array: getTitle: " + containerCustomisation.getTitle());
                                MyLog.i(CLS_NAME, "sparse array: getSubtitle: " + containerCustomisation.getSubtitle());
                            }
                            arrayList.add(containerCustomisation);
                        }
                    }
                    switch (arrayList.size()) {
                        case 0:
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "sparse array: no items selected");
                            }
                            break;
                        case 1:
                            if (runExport(arrayList)) {
                                toast(getString(R.string.command_exported_successfully), Toast.LENGTH_LONG);
                                onBackPressed();
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "runExport: failed");
                                }
                                toast(getString(R.string.title_export_failed), Toast.LENGTH_LONG);
                            }
                            break;
                        default:
                            if (runExport(arrayList)) {
                                toast(getString(R.string.commands_exported_successfully, String.valueOf(arrayList.size())), Toast.LENGTH_LONG);
                                onBackPressed();
                            } else {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "runExport: failed");
                                }
                                toast(getString(R.string.title_export_failed), Toast.LENGTH_LONG);
                            }
                            break;
                    }
                    showProgress(false);
                }
            }).start();
            return true;
        } else if (R.id.action_select_all == menuItem.getItemId()) {
            if (mObjects.isEmpty() || !isActive()) {
                return true;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FragmentExportCustomisation.this.isAllChecked = !isAllChecked;
                    SparseBooleanArray sparseBooleanArray = ((UIExportCustomisationAdapter) getAdapter()).getCheckedArray();
                    sparseBooleanArray.clear();
                    final int size = mObjects.size();
                    for (int i = 0; i < size; i++) {
                        sparseBooleanArray.put(i, isAllChecked);
                    }
                    if (isActive()) {
                        getParentActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getAdapter().notifyItemRangeChanged(0, size);
                            }
                        });
                    }
                }
            }).start();
            return true;
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
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
    public void onStart() {
        super.onStart();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onStart");
        }
        synchronized (lock) {
            if (mObjects.isEmpty()) {
                getParentActivity().setTitle(getString(R.string.menu_export));
                helper.finaliseUI();
            }
        }
    }
}
