package ai.saiy.android.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.applications.Application;
import ai.saiy.android.applications.ApplicationBasic;
import ai.saiy.android.ui.activity.ActivityApplicationPicker;
import ai.saiy.android.ui.fragment.helper.FragmentApplicationPickerHelper;
import ai.saiy.android.utils.MyLog;

public class FragmentApplicationPicker extends Fragment implements View.OnClickListener {
    private static final Object lock = new Object();
    private int type = ActivityApplicationPicker.ACCESSIBLE_APPLICATION_TYPE;

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentApplicationPicker.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;
    private ArrayList<Application> mObjects;
    private FragmentApplicationPickerHelper helper;
    private Context mContext;

    public static FragmentApplicationPicker newInstance(Bundle args) {
        final FragmentApplicationPicker fragment = new FragmentApplicationPicker();
        fragment.setArguments(new Bundle(args.getInt(ActivityApplicationPicker.EXTRA_TYPE, ActivityApplicationPicker.ACCESSIBLE_APPLICATION_TYPE)));
        return fragment;
    }

    public ActivityApplicationPicker getParentActivity() {
        return (ActivityApplicationPicker) getActivity();
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

    public int getType() {
        return type;
    }

    public boolean isActive() {
        return getActivity() != null && getParentActivity().isActive() && isAdded() && !isRemoving();
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
        final Application application = mObjects.get((Integer) view.getTag());
        final ApplicationBasic applicationBasic = new ApplicationBasic(application.b().toString(), application.c());
        applicationBasic.setAction(application.d());
        getParentActivity().setResult(applicationBasic);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate: savedInstanceState: " + String.valueOf(bundle != null));
        }
        this.helper = new FragmentApplicationPickerHelper(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateView");
        }
        final View rootView = layoutInflater.inflate(R.layout.layout_fragment_application_picker, viewGroup, false);
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
                getParentActivity().setTitle(getString(R.string.title_select_application));
                final Bundle args = getArguments();
                this.type = (args == null)? ActivityApplicationPicker.ACCESSIBLE_APPLICATION_TYPE : args.getInt(ActivityApplicationPicker.EXTRA_TYPE, ActivityApplicationPicker.ACCESSIBLE_APPLICATION_TYPE);
                helper.finaliseUI();
            }
        }
    }
}
