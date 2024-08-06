package ai.saiy.android.ui.fragment.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ai.saiy.android.R;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.UIExportCustomisationAdapter;
import ai.saiy.android.ui.containers.ContainerCustomisation;
import ai.saiy.android.ui.fragment.FragmentExportCustomisation;
import ai.saiy.android.utils.MyLog;

public class FragmentExportCustomisationHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentExportCustomisationHelper.class.getSimpleName();

    private final FragmentExportCustomisation parentFragment;

    public FragmentExportCustomisationHelper(FragmentExportCustomisation parentFragment) {
        this.parentFragment = parentFragment;
    }

    private String getString(@StringRes int resId) {
        return getApplicationContext().getString(resId);
    }

    private Context getApplicationContext() {
        return parentFragment.getApplicationContext();
    }

    public UIExportCustomisationAdapter getAdapter(ArrayList<ContainerCustomisation> objects) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getAdapter");
        }
        return new UIExportCustomisationAdapter(objects, getParent());
    }

    public RecyclerView getRecyclerView(View view) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getRecyclerView");
        }
        final RecyclerView recyclerView = view.findViewById(R.id.layout_common_fragment_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        recyclerView.addItemDecoration(new ai.saiy.android.ui.components.DividerItemDecoration(getParentActivity(), null));
        return recyclerView;
    }

    public void finaliseUI() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (getParent().isActive()) {
                    if (getParentActivity().getDrawer().isDrawerOpen(GravityCompat.START)) {
                        try {
                            Thread.sleep(200L);
                        } catch (InterruptedException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "finaliseUI InterruptedException");
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                }
                if (getParent().isActive()) {
                    getParentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getParent().getObjects() == null) {
                                toast(getString(R.string.error_edit_customisations), Toast.LENGTH_LONG);
                            } else {
                                final List<ContainerCustomisation> objects = getParent().getObjectsFromArguments();
                                if (objects == null) {
                                    toast(getString(R.string.error_edit_customisations), Toast.LENGTH_LONG);
                                } else {
                                    getParent().getObjects().addAll(objects);
                                    getParent().getAdapter().notifyItemRangeInserted(0, getParent().getObjects().size());
                                }
                            }
                            showProgress(false);
                        }
                    });
                } else if (DEBUG) {
                    MyLog.w(CLS_NAME, "finaliseUI Fragment detached");
                }
            }
        });
    }

    public void toast(String text, int duration) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "makeToast: " + text);
        }
        if (getParent().isActive()) {
            getParentActivity().toast(text, duration);
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "toast Fragment detached");
        }
    }

    public void showProgress(boolean visible) {
        if (getParent().isActive()) {
            getParentActivity().showProgress(visible);
        }
    }

    public ActivityHome getParentActivity() {
        return parentFragment.getParentActivity();
    }

    public FragmentExportCustomisation getParent() {
        return parentFragment;
    }
}
