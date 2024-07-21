package ai.saiy.android.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ai.saiy.android.R;
import ai.saiy.android.applications.ApplicationActivityBasic;
import ai.saiy.android.firebase.UserFirebaseListener;
import ai.saiy.android.ui.fragment.FragmentActivityPicker;
import ai.saiy.android.user.UserFirebaseHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import me.drakeet.support.toast.ToastCompat;

public class ActivityActivityPicker extends AppCompatActivity implements UserFirebaseListener {
    public static final String EXTRA_APPLICATION = "extra_blocked_applications";
    private ProgressBar progressBar;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityActivityPicker.class.getSimpleName();

    private void setupToolbar() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setupToolbar");
        }
        setSupportActionBar(findViewById(R.id.toolbar));
        this.progressBar = findViewById(R.id.progress);
        showProgress(true);
    }

    public void setResult(ApplicationActivityBasic applicationActivityBasic) {
        int resultCode;
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setResult");
        }
        Intent intent = new Intent();
        if (applicationActivityBasic == null) {
            resultCode = Activity.RESULT_CANCELED;
        } else {
            resultCode = Activity.RESULT_OK;
            intent.putExtra(EXTRA_APPLICATION, applicationActivityBasic);
        }
        setResult(resultCode, intent);
        finish();
    }

    /**
     * Utility method to toast making sure it's on the main thread
     *
     * @param text   to toast
     * @param duration one of {@link Toast#LENGTH_SHORT} {@link Toast#LENGTH_LONG}
     */
    public void toast(final String text, final int duration) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "makeToast: " + text);
        }
        if (UtilsString.notNaked(text)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastCompat.makeText(getApplicationContext(), text, duration).show();
                }
            });
        }
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
        setContentView(R.layout.activity_activity_picker_layout);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate: savedInstanceState: " + (bundle != null));
        }
        if (bundle != null) {
            return;
        }
        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContent, FragmentActivityPicker.newInstance(getIntent().getExtras()), String.valueOf(0)).commitAllowingStateLoss();
        setupToolbar();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new UserFirebaseHelper().isAdFree(getApplication(), ActivityActivityPicker.this);
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
