package ai.saiy.android.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.applications.ApplicationBasic;
import ai.saiy.android.firebase.UserFirebaseListener;
import ai.saiy.android.ui.fragment.FragmentAppPickerMulti;
import ai.saiy.android.user.UserFirebaseHelper;
import ai.saiy.android.utils.MyLog;

public class ActivityApplicationPickerMulti extends AppCompatActivity implements UserFirebaseListener {
    public static final String EXTRA_BLOCKED_APPLICATIONS = "extra_blocked_applications";
    public static final String EXTRA_APPLICATION_ARRAY = "extra_application_array";
    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityApplicationPickerMulti.class.getSimpleName();
    private ProgressBar progressBar;

    private void setupToolbar() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setupToolbar");
        }
        setSupportActionBar(findViewById(R.id.toolbar));
        this.progressBar = findViewById(R.id.progress);
        showProgress(true);
    }

    public void setResult(ArrayList<ApplicationBasic> arrayList, boolean isSuccessful) {
        int resultCode = Activity.RESULT_OK;
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setResult");
        }
        final Intent intent = new Intent();
        if (!isSuccessful) {
            resultCode = Activity.RESULT_CANCELED;
        } else if (arrayList == null) {
            intent.putParcelableArrayListExtra(EXTRA_APPLICATION_ARRAY, new ArrayList<>());
        } else {
            intent.putParcelableArrayListExtra(EXTRA_APPLICATION_ARRAY, arrayList);
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
        setResult(null, false);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_app_picker_multi_layout);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate: savedInstanceState: " + (bundle != null));
        }
        if (bundle != null) {
            return;
        }
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContent, FragmentAppPickerMulti.newInstance(getIntent().getExtras()), String.valueOf(0)).commitAllowingStateLoss();
        setupToolbar();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new UserFirebaseHelper().isAdFree(getApplication(), ActivityApplicationPickerMulti.this);
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
