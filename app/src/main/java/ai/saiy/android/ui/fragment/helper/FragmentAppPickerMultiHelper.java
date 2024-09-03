package ai.saiy.android.ui.fragment.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.saiy.android.R;
import ai.saiy.android.accessibility.BlockedApplications;
import ai.saiy.android.applications.Application;
import ai.saiy.android.applications.ApplicationBasic;
import ai.saiy.android.ui.activity.ActivityApplicationPickerMulti;
import ai.saiy.android.ui.components.UIAppPickerMultiAdapter;
import ai.saiy.android.ui.fragment.FragmentAppPickerMulti;
import ai.saiy.android.utils.MyLog;

public class FragmentAppPickerMultiHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentAppPickerMultiHelper.class.getSimpleName();

    private final FragmentAppPickerMulti parentFragment;

    public FragmentAppPickerMultiHelper(FragmentAppPickerMulti parentFragment) {
        this.parentFragment = parentFragment;
    }

    private Context getApplicationContext() {
        return parentFragment.getApplicationContext();
    }

    /**
     * Get the adapter for this fragment
     *
     * @param objects list of {@link Application} elements
     */
    public UIAppPickerMultiAdapter getAdapter(ArrayList<Application> objects) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdapter");
        }
        return new UIAppPickerMultiAdapter(objects, getParent());
    }

    public ActivityApplicationPickerMulti getParentActivity() {
        return parentFragment.getParentActivity();
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
        final RecyclerView recyclerView = parent.findViewById(R.id.rvAppPickerMulti);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        recyclerView.addItemDecoration(new ai.saiy.android.ui.components.DividerItemDecoration(getParentActivity(), null));
        return recyclerView;
    }

    /**
     * Update the parent fragment with the UI components
     */
    public void finaliseUI(final @Nullable BlockedApplications blockedApplications) {
        if (getParent().isActive()) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<Application> tempArray = ai.saiy.android.applications.Installed.getInstalledApplications(getApplicationContext(), true);
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "tempArray: size: " + tempArray.size());
                    }
                    final List<ApplicationBasic> applicationArray = (blockedApplications == null) ? Collections.emptyList() : blockedApplications.getApplicationArray();
                    final int size = tempArray.size();
                    for (int i = 0; i < size; i++) {
                        for (ApplicationBasic applicationBasic : applicationArray) {
                            if (tempArray.get(i).getPackageName().matches(applicationBasic.getPackageName())) {
                                ((UIAppPickerMultiAdapter) getParent().getAdapter()).getCheckedArray().put(i, true);
                            }
                        }
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

    public FragmentAppPickerMulti getParent() {
        return parentFragment;
    }
}
