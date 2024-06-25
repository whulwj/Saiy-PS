package ai.saiy.android.bluetooth;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.CallSuper;

import ai.saiy.android.utils.MyLog;

public class SaiyCountDownTimer extends CountDownTimer {
    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = SaiyCountDownTimer.class.getSimpleName();

    private volatile boolean isStarted;

    public SaiyCountDownTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    public void startTick() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startTick");
        }
        if (!this.isStarted) {
            this.isStarted = true;
            start();
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "startTick: already running");
        }
    }

    public void stopTick() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stopTick");
        }
        if (this.isStarted) {
            this.isStarted = false;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    cancel();
                }
            });
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "stopTick: not running");
        }
    }

    @CallSuper
    @Override
    public void onFinish() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onFinish");
        }
        this.isStarted = false;
    }

    @CallSuper
    @Override
    public void onTick(long millisUntilFinished) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onTick");
        }
    }
}
