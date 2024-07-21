package ai.saiy.android.ui.fragment.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.applications.Application;
import ai.saiy.android.ui.activity.ActivityActivityPicker;
import ai.saiy.android.ui.components.UIActivityPickerAdapter;
import ai.saiy.android.ui.fragment.FragmentActivityPicker;
import ai.saiy.android.utils.MyLog;

public class FragmentActivityPickerHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentActivityPickerHelper.class.getSimpleName();

    private final FragmentActivityPicker parentFragment;

    public FragmentActivityPickerHelper(FragmentActivityPicker parentFragment) {
        this.parentFragment = parentFragment;
    }

    private Context getApplicationContext() {
        return this.parentFragment.getApplicationContext();
    }

    public UIActivityPickerAdapter getAdapter(ArrayList<Application> objects) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdapter");
        }
        return new UIActivityPickerAdapter(getApplicationContext().getPackageManager(), (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE), objects);
    }

    public ExpandableListView getExpandableListView(@NonNull View parent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getExpandableListView");
        }
        final ExpandableListView expandableListView = parent.findViewById(android.R.id.list);
        final int width = getParentActivity().getWindowManager().getDefaultDisplay().getWidth();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            expandableListView.setIndicatorBoundsRelative(width - 100, width);
        } else {
            expandableListView.setIndicatorBounds(width - 100, width);
        }
        expandableListView.setClickable(true);
        expandableListView.setTextFilterEnabled(true);
        expandableListView.setOnChildClickListener(getParent());
        expandableListView.setOnItemLongClickListener(getParent());
        return expandableListView;
    }

    public void finaliseUI() {
        if (getParent().isActive()) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<Application> accessibleApplications = ai.saiy.android.applications.Installed.getAccessibleApplications(getApplicationContext().getPackageManager());
                    if (getParent().isActive()) {
                        getParentActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getParent().getObjects().addAll(accessibleApplications);
                                getParent().getAdapter().notifyDataSetChanged();
                            }
                        });
                        showProgress(false);
                    } else if (DEBUG) {
                        MyLog.w(CLS_NAME, "finaliseUI: activity null or finishing");
                    }
                }
            });
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "finaliseUI: activity null or finishing");
        }
    }

    public void showProgress(boolean visible) {
        if (getParent().isActive()) {
            getParentActivity().showProgress(visible);
        }
    }

    public ActivityActivityPicker getParentActivity() {
        return this.parentFragment.getParentActivity();
    }

    public FragmentActivityPicker getParent() {
        return this.parentFragment;
    }
}
