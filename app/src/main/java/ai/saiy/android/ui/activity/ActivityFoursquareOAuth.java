package ai.saiy.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.saiy.android.R;
import ai.saiy.android.command.foursquare.FoursquareOAuthView;
import ai.saiy.android.firebase.database.reference.FoursquareReference;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ActivityFoursquareOAuth extends AppCompatActivity implements FoursquareOAuthView.Listener {
    private FoursquareOAuthView foursquareOAuthView;
    private volatile Disposable disposable;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityFoursquareOAuth.class.getSimpleName();
    private boolean isOAuthStarted = false;
    private final AtomicBoolean isFinishing = new AtomicBoolean();

    private void startShutdown() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startShutdown");
        }
        if (this.disposable == null) {
            this.disposable = Schedulers.computation().scheduleDirect(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing.get()) {
                        return;
                    }
                    isFinishing.set(true);
                    finish();
                }
            }, 2000L, TimeUnit.MILLISECONDS);
        }
    }

    private void showMessage(final String str) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "showMessage");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ai.saiy.android.utils.UtilsToast.showToast(getApplicationContext(), str, Toast.LENGTH_SHORT);
                startShutdown();
            }
        });
    }

    @Override
    public void onFailure(FoursquareOAuthView foursquareOAuthView, FoursquareOAuthView.Result result) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onFailure: " + result.name());
        }
        if (result != null) {
            switch (result) {
                case SUCCESS:
                    break;
                case CANCELLATION:
                    showMessage(getString(R.string.foursquare_auth_cancelled));
                    break;
                default:
                    showMessage(getString(R.string.foursquare_auth_error));
                    break;
            }
        }
    }

    @Override
    public void onSuccess(FoursquareOAuthView foursquareOAuthView, String accessToken) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onResetSuccess");
        }
        if (accessToken == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "accessToken null");
            }
            showMessage(getString(R.string.foursquare_auth_error));
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "accessToken: " + accessToken);
            }
            SPH.setFoursquareToken(getApplicationContext(), accessToken);
            showMessage(getString(R.string.foursquare_auth_success));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onBackPressed");
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onCreate");
        }
        this.foursquareOAuthView = new FoursquareOAuthView(this);
        setContentView(foursquareOAuthView);
        this.isOAuthStarted = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onDestroy");
        }
        isFinishing.set(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onNewIntent");
        }
        startShutdown();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onPause");
        }
        if (isFinishing()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isFinishing");
            }
            overridePendingTransition(0, R.anim.slide_out_left);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onResume");
        }
        if (isOAuthStarted) {
            return;
        }
        this.isOAuthStarted = true;
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                final Pair<Boolean, String> foursquarePair = new FoursquareReference().getClientId(getApplicationContext());
                if (foursquarePair.first) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            foursquareOAuthView.start(foursquarePair.second, "http://localhost/", true, ActivityFoursquareOAuth.this);
                        }
                    });
                    return;
                }
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "foursquarePair first false");
                }
                showMessage(getString(R.string.foursquare_auth_error));
            }
        });
    }
}
