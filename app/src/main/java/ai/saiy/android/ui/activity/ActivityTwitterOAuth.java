package ai.saiy.android.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.saiy.android.R;
import ai.saiy.android.command.twitter.TwitterOAuthView;
import ai.saiy.android.firebase.database.reference.TwitterReference;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import twitter4j.AccessToken;

/**
 * <a href="https://twitter4j.org/code-examples" />
 */
public class ActivityTwitterOAuth extends AppCompatActivity implements TwitterOAuthView.Listener {
    private TwitterOAuthView twitterOAuthView;
    private volatile Timer timer;

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ActivityTwitterOAuth.class.getSimpleName();
    private boolean isOAuthStarted = false;
    private final AtomicBoolean isFinishing = new AtomicBoolean();

    private void startShutdown() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startShutdown");
        }
        if (this.timer == null) {
            this.timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isFinishing.get()) {
                        return;
                    }
                    isFinishing.set(true);
                    finish();
                }
            }, 2000L);
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
    public void onFailure(TwitterOAuthView twitterOAuthView, TwitterOAuthView.Result result) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onFailure: " + result.name());
        }
        if (result != null) {
            switch (result) {
                case SUCCESS:
                    break;
                case CANCELLATION:
                    showMessage(getString(R.string.twitter_auth_cancelled));
                    break;
                default:
                    showMessage(getString(R.string.twitter_auth_error));
                    break;
            }
        }
    }

    @Override
    public void onSuccess(TwitterOAuthView twitterOAuthView, AccessToken accessToken) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onResetSuccess");
        }
        if (accessToken == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "accessToken null");
            }
            showMessage(getString(R.string.twitter_auth_error));
            return;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getScreenName: " + accessToken.getScreenName());
            MyLog.i(CLS_NAME, "getUserId: " + accessToken.getUserId());
            MyLog.i(CLS_NAME, "getToken: " + accessToken.getToken());
            MyLog.i(CLS_NAME, "getTokenSecret: " + accessToken.getTokenSecret());
        }
        //persist to the accessToken for future reference.
        SPH.setTwitterSecret(getApplicationContext(), accessToken.getTokenSecret());
        SPH.setTwitterToken(getApplicationContext(), accessToken.getToken());
        showMessage(getString(R.string.twitter_auth_success));
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
        this.twitterOAuthView = new TwitterOAuthView(this);
        setContentView(twitterOAuthView);
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
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final Pair<Boolean, Pair<String, String>> twitterPair = new TwitterReference().getCredentials(getApplicationContext());
                if (twitterPair.first) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            twitterOAuthView.start(twitterPair.second.first, twitterPair.second.second, "saiy-twitter://oauth", true, ActivityTwitterOAuth.this);
                        }
                    });
                    return;
                }
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "twitterPair first false");
                }
                showMessage(getString(R.string.twitter_auth_error));
            }
        });
    }
}
