package ai.saiy.android.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FragmentDevelopment.class.getSimpleName();
    public static final int RC_ACCOUNT = 110;

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
        return mContext;
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return mAdapter;
    }

    public ArrayList<ContainerUI> getObjects() {
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
        if (Global.isInVoiceTutorial()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onClick: tutorialActive");
            }
            toast(getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
            return;
        }

        final int position = getPosition(view);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onClick: " + position);
        }
        switch (position) {
            case 0:
                helper.showAccountOverviewDialog();
                break;
            case 1:
                helper.showTranslationDialog();
                break;
            case 2:
                helper.showDesignOverviewDialog();
                break;
            case 3:
                helper.showReportBugDialog();
                break;
            case 4:
                helper.showToDoListDialog();
                break;
            case 5:
                helper.showNaturalLanguageDialog();
                break;
            case 6:
                helper.showEnhancementDialog();
                break;
            case 7:
                helper.showVocalVerificationFeedbackDialog();
                break;
            case 8:
                helper.showEmotionAnalysisFeedbackDialog();
                break;
            case 9:
                helper.showGenericDialog();
                break;
            case 10:
                ai.saiy.android.intent.ExecuteIntent.webSearch(getApplicationContext(), "https://github.com/brandall76/Saiy-PS");
                break;
            case 11:
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
    public boolean onLongClick(View view) {
        if (Global.isInVoiceTutorial()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onLongClick: tutorialActive");
            }
            toast(getString(R.string.tutorial_content_disabled), Toast.LENGTH_SHORT);
            return true;
        }

        final int position = getPosition(view);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onLongClick: " + position);
        }
        switch (position) {
            case 0:
                getParentActivity().speak(R.string.content_account_overview, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 1:
                getParentActivity().speak(R.string.lp_translation, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 2:
                getParentActivity().speak(R.string.lp_design, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 3:
                getParentActivity().speak(R.string.lp_bug_report, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 4:
                getParentActivity().speak(R.string.lp_to_do, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 5:
                getParentActivity().speak(R.string.lp_natural_language, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 6:
                getParentActivity().speak(R.string.lp_enhancement, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 7:
                getParentActivity().speak(R.string.lp_vocal_verification_feedback, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 8:
                getParentActivity().speak(R.string.lp_emotion_feedback, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 9:
                getParentActivity().speak(R.string.lp_generic, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 10:
                getParentActivity().speak(R.string.lp_source_code, LocalRequest.ACTION_SPEAK_ONLY);
                break;
            case 11:
                getParentActivity().speak(R.string.lp_developer_api, LocalRequest.ACTION_SPEAK_ONLY);
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, requestCode, intent);
        if (RC_ACCOUNT == requestCode) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onActivityResult: RC_ACCOUNT");
            }
            helper.handleActivityResult(resultCode, intent);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onActivityResult: DEFAULT");
            }
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
                getParentActivity().setTitle(getString(R.string.menu_development));
                helper.finaliseUI();
            }
        }
    }
}
