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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.accessibility.BlockedApplications;
import ai.saiy.android.applications.Application;
import ai.saiy.android.applications.ApplicationBasic;
import ai.saiy.android.ui.activity.ActivityApplicationPickerMulti;
import ai.saiy.android.ui.components.UIAppPickerMultiAdapter;
import ai.saiy.android.ui.fragment.helper.FragmentAppPickerMultiHelper;
import ai.saiy.android.utils.MyLog;

public class FragmentAppPickerMulti extends Fragment implements View.OnClickListener {
    private static final Object lock = new Object();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;
    private ArrayList<Application> mObjects;
    private FragmentAppPickerMultiHelper helper;
    private Context mContext;

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentAppPickerMulti.class.getSimpleName();
    private boolean isAllChecked = false;

    public static FragmentAppPickerMulti newInstance(Bundle args) {
        final FragmentAppPickerMulti fragment = new FragmentAppPickerMulti();
        fragment.setArguments(new Bundle(args));
        return fragment;
    }

    public boolean isActive() {
        return getActivity() != null && getParentActivity().isActive() && isAdded() && !isRemoving();
    }

    public ActivityApplicationPickerMulti getParentActivity() {
        return (ActivityApplicationPickerMulti) getActivity();
    }

    public Context getApplicationContext() {
        return mContext;
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return mAdapter;
    }

    public ArrayList<Application> getObjects() {
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
        final int position = (Integer) view.getTag();
        final boolean isChecked = ((UIAppPickerMultiAdapter) getAdapter()).getCheckedArray().get(position);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClick: isChecked; " + isChecked);
        }
        ((UIAppPickerMultiAdapter) getAdapter()).getCheckedArray().put(position, !isChecked);
        getAdapter().notifyItemChanged(position);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClick: application: " + mObjects.get(position).getLabel());
            SparseBooleanArray sparseBooleanArray = ((UIAppPickerMultiAdapter) getAdapter()).getCheckedArray();
            final int size = sparseBooleanArray.size();
            for (int i = 0; i < size; i++) {
                int key = sparseBooleanArray.keyAt(i);
                boolean value = sparseBooleanArray.get(key);
                MyLog.i(CLS_NAME, "sparse array: key: " + key + " ~ value: " + value);
                if (value) {
                    MyLog.i(CLS_NAME, "sparse array: app: " + mObjects.get(key).getLabel());
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate: savedInstanceState: " + (bundle != null));
        }
        setHasOptionsMenu(true);
        this.helper = new FragmentAppPickerMultiHelper(this);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateOptionsMenu");
        }
        menuInflater.inflate(R.menu.menu_save, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateView");
        }
        final View rootView = layoutInflater.inflate(R.layout.layout_fragment_app_picker_multi, viewGroup, false);
        this.mRecyclerView = helper.getRecyclerView(rootView);
        this.mObjects = new ArrayList<>();
        this.mAdapter = helper.getAdapter(mObjects);
        this.mRecyclerView.setAdapter(mAdapter);
        return rootView;
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
        if (R.id.action_save == menuItem.getItemId()) {
            if (mObjects.isEmpty() || !isActive()) {
                return true;
            }
            final ArrayList<ApplicationBasic> arrayList = new ArrayList<>();
            final SparseBooleanArray sparseBooleanArray = ((UIAppPickerMultiAdapter) getAdapter()).getCheckedArray();
            final int size = sparseBooleanArray.size();
            for (int i = 0; i < size; i++) {
                int key = sparseBooleanArray.keyAt(i);
                boolean value = sparseBooleanArray.get(key);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "sparse array: key: " + key + " ~ value: " + value);
                }
                if (value) {
                    Application app = mObjects.get(key);
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "sparse array: app: " + app.getLabel());
                    }
                    arrayList.add(new ApplicationBasic(app.getLabel().toString(), app.getPackageName()));
                }
            }
            getParentActivity().setResult(arrayList, true);
            return true;
        } else if (R.id.action_select_all == menuItem.getItemId()) {
            if (mObjects.isEmpty() || !isActive()) {
                return true;
            }
            this.isAllChecked = !isAllChecked;
            final SparseBooleanArray sparseBooleanArray = ((UIAppPickerMultiAdapter) getAdapter()).getCheckedArray();
            sparseBooleanArray.clear();
            final int size = mObjects.size();
            for (int i = 0; i < size; i++) {
                sparseBooleanArray.put(i, isAllChecked);
            }
            getAdapter().notifyItemRangeChanged(0, size);
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
                getParentActivity().setTitle(getString(R.string.title_select_applications));
                final Bundle args = getArguments();
                final BlockedApplications blockedApps = (args == null) ? null : args.getParcelable(ActivityApplicationPickerMulti.EXTRA_BLOCKED_APPLICATIONS);
                this.helper.finaliseUI(blockedApps);
            }
        }
    }
}
