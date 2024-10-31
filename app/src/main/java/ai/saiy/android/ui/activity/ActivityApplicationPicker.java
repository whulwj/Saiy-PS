package ai.saiy.android.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import ai.saiy.android.R;
import ai.saiy.android.applications.ApplicationBasic;
import ai.saiy.android.firebase.UserFirebaseListener;
import ai.saiy.android.ui.fragment.FragmentApplicationPicker;
import ai.saiy.android.user.UserFirebaseHelper;
import ai.saiy.android.utils.MyLog;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ActivityApplicationPicker extends AppCompatActivity implements UserFirebaseListener {
    public static final String EXTRA_APPLICATION = "extra_application";
    public static final String EXTRA_TYPE = "extra_type";
    public static final int SEARCH_APPLICATION_TYPE = 1;
    public static final int ACCESSIBLE_APPLICATION_TYPE = 0;
    private ProgressBar progressBar;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityApplicationPicker.class.getSimpleName();

    private void setupToolbar() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setupToolbar");
        }
        setSupportActionBar(findViewById(R.id.toolbar));
        this.progressBar = findViewById(R.id.progress);
        showProgress(true);
    }

    public void setResult(ApplicationBasic applicationBasic) {
        int resultCode;
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setResult");
            if (applicationBasic != null) {
                MyLog.i(CLS_NAME, "setResult: getName: " + applicationBasic.getName());
                MyLog.i(CLS_NAME, "setResult: getPackageName: " + applicationBasic.getPackageName());
                MyLog.i(CLS_NAME, "setResult: getAction: " + applicationBasic.getAction());
            }
        }
        final Intent intent = new Intent();
        if (applicationBasic == null) {
            resultCode = Activity.RESULT_CANCELED;
        } else {
            resultCode = Activity.RESULT_OK;
            intent.putExtra(EXTRA_APPLICATION, applicationBasic);
        }
        setResult(resultCode, intent);
        finish();
    }

    @Override
    public void onDetermineAdFree(boolean isAddFree) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDetermineAdFree: " + isAddFree);
        }
    }

    /**
     * Utility method to show or hide the progress bar
     *
     * @param visible true to show, false to hide
     */
    public void showProgress(final boolean visible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    public boolean isActive() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ? !isDestroyed() && !isFinishing() : !isFinishing();
    }

    @Override
    public void onBackPressed() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBackPressed");
        }
        setResult(Activity.RESULT_CANCELED, new Intent());
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_app_picker_layout);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate: savedInstanceState: " + (bundle != null));
        }
        if (bundle != null) {
            return;
        }
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContent, FragmentApplicationPicker.newInstance(getIntent().getExtras()), String.valueOf(0)).commitAllowingStateLoss();
        setupToolbar();
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                new UserFirebaseHelper().isAdFree(getApplication(), ActivityApplicationPicker.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onNewIntent: ignoring");
        }
    }
}
