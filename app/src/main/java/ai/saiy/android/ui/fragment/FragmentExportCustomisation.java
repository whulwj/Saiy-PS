package ai.saiy.android.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
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
import ai.saiy.android.ui.fragment.helper.FragmentCustomisationHelper;
import ai.saiy.android.ui.fragment.helper.FragmentExportCustomisationHelper;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsFile;

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

    private boolean runExport(@Nullable final DocumentFile documentFile, ArrayList<ContainerCustomisation> arrayList) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "runExport: size: " + arrayList.size());
        }
        if (documentFile == null && !ExportHelper.exportProceed(getApplicationContext())) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "exportProceed: false");
            }
            return false;
        }
        final com.google.gson.Gson gson = new com.google.gson.GsonBuilder().disableHtmlEscaping().create();
        final ExportHelper exportHelper = new ExportHelper(gson);
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
                    exportHelper.exportCustomPhrase(getApplicationContext(), documentFile, customPhrase);
                    break;
                case CUSTOM_NICKNAME:
                    final CustomNickname customNickname = gson.fromJson(serialised, new com.google.gson.reflect.TypeToken<CustomNickname>() {
                    }.getType());
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "customNickname: " + customNickname.getNickname());
                    }
                    exportHelper.exportCustomNickname(getApplicationContext(), documentFile, customNickname);
                    break;
                case CUSTOM_REPLACEMENT:
                    final CustomReplacement customReplacement = gson.fromJson(serialised, new com.google.gson.reflect.TypeToken<CustomReplacement>() {
                    }.getType());
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "customReplacement: " + customReplacement.getKeyphrase());
                    }
                    exportHelper.exportCustomReplacement(getApplicationContext(), documentFile, customReplacement);
                    break;
                case CUSTOM_COMMAND:
                    final CustomCommand customCommand = gson.fromJson(serialised, new com.google.gson.reflect.TypeToken<CustomCommand>() {
                    }.getType());
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "customCommandContainer: " + customCommand.getKeyphrase());
                    }
                    logCustomCommand(customCommand);
                    exportHelper.exportCustomCommand(getApplicationContext(), documentFile, customCommand);
                    break;
            }
        }
        return true;
    }

    private void logCustomCommand(@NonNull CustomCommand customCommand) {
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
        if (Global.isInVoiceTutorial()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onClick: tutorialActive");
            }
            toast(getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
            return;
        }

        int position = (view == null) ? 0 : mRecyclerView.getChildAdapterPosition(view);
        if (view != null && RecyclerView.NO_POSITION == position) {
            final RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(view);
            if (viewHolder instanceof UIExportCustomisationAdapter.ViewHolder) {
                position = ((UIExportCustomisationAdapter.ViewHolder) viewHolder).getBoundPosition();
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClick: " + position);
        }
        if (RecyclerView.NO_POSITION == position) {
            return;
        }

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onActivityResult: " + requestCode + " ~ " + resultCode);
        }
        if (requestCode != FragmentCustomisationHelper.EXPORT_PICKER_REQ_CODE) {
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onActivityResult: " + requestCode);
            }
            return;
        }
        final Uri directoryUri = data.getData();
        if (directoryUri == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onActivityResult: export uri null");
            }
            return;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onActivityResult: exportUri: " + directoryUri);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if (takeFlags != 0) {
                getApplicationContext().getContentResolver().takePersistableUriPermission(directoryUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
        final DocumentFile documentFile = DocumentFile.fromTreeUri(getApplicationContext(), directoryUri);
        if (documentFile == null || !documentFile.isDirectory()) {
            toast(getString(R.string.storage_unavailable), Toast.LENGTH_LONG);
            return;
        }
        runExport(documentFile);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.fromFile(UtilsFile.saiyDirectory(getApplicationContext())));
                }
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(intent, FragmentCustomisationHelper.EXPORT_PICKER_REQ_CODE);
                return true;
            }
            runExport(null);
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

    private ArrayList<ContainerCustomisation> getSelections() {
        final SparseBooleanArray sparseBooleanArray = ((UIExportCustomisationAdapter) getAdapter()).getCheckedArray();
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
        return arrayList;
    }

    private void runExport(@Nullable final DocumentFile documentFile) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showProgress(true);
                final ArrayList<ContainerCustomisation> arrayList = getSelections();
                final int itemsCount = arrayList.size();
                if (0 == itemsCount) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "sparse array: no items selected");
                    }
                } else {
                    if (runExport(documentFile, arrayList)) {
                        if (itemsCount > 1) {
                            toast(getString(R.string.commands_exported_successfully, String.valueOf(itemsCount)), Toast.LENGTH_LONG);
                        } else {
                            toast(getString(R.string.command_exported_successfully), Toast.LENGTH_LONG);
                        }
                        onBackPressed();
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "runExport: failed");
                        }
                        toast(getString(R.string.title_export_failed), Toast.LENGTH_LONG);
                    }
                }
                showProgress(false);
            }
        }).start();
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
