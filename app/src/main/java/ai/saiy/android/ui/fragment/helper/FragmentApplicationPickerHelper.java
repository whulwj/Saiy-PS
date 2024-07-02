package ai.saiy.android.ui.fragment.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.applications.Application;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.ui.activity.ActivityApplicationPicker;
import ai.saiy.android.ui.components.UIApplicationPickerAdapter;
import ai.saiy.android.ui.fragment.FragmentApplicationPicker;
import ai.saiy.android.utils.MyLog;

public class FragmentApplicationPickerHelper {
    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentApplicationPickerHelper.class.getSimpleName();

    private final FragmentApplicationPicker parentFragment;

    public FragmentApplicationPickerHelper(FragmentApplicationPicker parentFragment) {
        this.parentFragment = parentFragment;
    }

    private Context getApplicationContext() {
        return parentFragment.getApplicationContext();
    }

    public UIApplicationPickerAdapter getAdapter(ArrayList<Application> objects) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdapter");
        }
        return new UIApplicationPickerAdapter(objects, getParent(), getParent().getString(R.string._auto_play));
    }

    public RecyclerView getRecyclerView(View view) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getRecyclerView");
        }
        final RecyclerView recyclerView = view.findViewById(R.id.rvApplicationPicker);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        recyclerView.addItemDecoration(new ai.saiy.android.ui.components.DividerItemDecoration(getParentActivity(), null));
        return recyclerView;
    }

    public void finaliseUI() {
        if (getParent().isActive()) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<Application> tempArray;
                    if (getParent().getType() == ActivityApplicationPicker.SEARCH_APPLICATION_TYPE) {
                        tempArray = Installed.getSearchApplications(getApplicationContext().getPackageManager());
                    } else {
                        tempArray = Installed.getAccessibleApplications(getApplicationContext().getPackageManager());
                    }
                    if (getParent().isActive()) {
                        getParentActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getParent().getObjects().addAll(tempArray);
                                getParent().getAdapter().notifyItemRangeInserted(0, getParent().getObjects().size());
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

    public ActivityApplicationPicker getParentActivity() {
        return parentFragment.getParentActivity();
    }

    public FragmentApplicationPicker getParent() {
        return parentFragment;
    }
}
