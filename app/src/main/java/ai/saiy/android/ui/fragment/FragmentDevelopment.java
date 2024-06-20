package ai.saiy.android.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.components.UIApplicationsAdapter;
import ai.saiy.android.ui.containers.ContainerUI;
import ai.saiy.android.ui.fragment.helper.FragmentDevelopmentHelper;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;

public class FragmentDevelopment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    private static final Object lock = new Object();

    private final boolean DEBUG = MyLog.DEBUG;

    private final String CLS_NAME = FragmentDevelopment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;
    private ArrayList<ContainerUI> mObjects;
    private FragmentDevelopmentHelper helper;
    private Context mContext;

    public static FragmentDevelopment newInstance(Bundle args) {
        return new FragmentDevelopment();
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

    public boolean isActive() {
        return getActivity() != null && getParentActivity().isActive() && isAdded() && !isRemoving();
    }

    public ActivityHome getParentActivity() {
        return (ActivityHome) getActivity();
    }

    public Context getApplicationContext() {
        return this.mContext;
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return this.mAdapter;
    }

    public ArrayList<ContainerUI> getObjects() {
        return this.mObjects;
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

    private int getPosition(View view) {
        int position = (view == null) ? 0 : mRecyclerView.getChildAdapterPosition(view);
        if (view != null && RecyclerView.NO_POSITION == position) {
            final RecyclerView.ViewHolder viewHolder = mRecyclerView.getChildViewHolder(view);
            if (viewHolder instanceof UIApplicationsAdapter.ViewHolder) {
                position = ((UIApplicationsAdapter.ViewHolder) viewHolder).getBoundPosition();
            }
        }
        return position;
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
        switch (getPosition(view)) {
            case 0:
                this.helper.showTranslationDialog();
                break;
            case 1:
                this.helper.showReportBugDialog();
                break;
            case 2:
                this.helper.showToDoListDialog();
                break;
            case 3:
                ai.saiy.android.intent.ExecuteIntent.webSearch(getApplicationContext(), "https://github.com/brandall76/Saiy-PS");
                break;
            case 4:
                ai.saiy.android.intent.ExecuteIntent.webSearch(getApplicationContext(), "https://github.com/brandall76/API-Example-App");
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        this.helper = new FragmentDevelopmentHelper(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreateView");
        }
        View inflate = layoutInflater.inflate(R.layout.layout_common_fragment_parent, viewGroup, false);
        this.mRecyclerView = this.helper.getRecyclerView(inflate);
        this.mObjects = new ArrayList<>();
        this.mAdapter = this.helper.getAdapter(this.mObjects);
        this.mRecyclerView.setAdapter(this.mAdapter);
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
    public boolean onLongClick(View view) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onLongClick: " + view.getTag());
        }
        if (Global.isInVoiceTutorial()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onLongClick: tutorialActive");
            }
            toast(getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
            return true;
        }

        switch (getPosition(view)) {
            case 0:
                getParentActivity().speak(R.string.lp_translation, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 1:
                getParentActivity().speak(R.string.lp_bug_report, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 2:
                getParentActivity().speak(R.string.lp_to_do, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 3:
                getParentActivity().speak(R.string.lp_source_code, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 4:
                getParentActivity().speak(R.string.lp_developer_api, LocalRequest.ACTION_SPEAK_ONLY);
                break;
        }
        return true;
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
            if (this.mObjects.isEmpty()) {
                getParentActivity().setTitle(getString(R.string.menu_development));
                this.helper.finaliseUI();
            }
        }
    }
}
