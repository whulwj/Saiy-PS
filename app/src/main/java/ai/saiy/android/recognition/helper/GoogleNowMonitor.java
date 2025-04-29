/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.recognition.helper;

import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Created by benrandall76@gmail.com on 05/09/2016.
 */

public class GoogleNowMonitor {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = GoogleNowMonitor.class.getSimpleName();

    private static final long MAX_DURATION = 180000L;
    private static final long HISTORY = 10000L;
    private static final int INTERVAL = 5000;
    private final long then = System.currentTimeMillis();

    /**
     * Start monitoring the foreground application to see if/when the user leaves voice recognition. Once they do,
     * the hotword detection can restart. A time limit to keep this process running is defined by
     * {@link #MAX_DURATION}
     *
     * @param ctx the application context
     * @param targetPackageName the target package
     */
    public void start(@NonNull final Context ctx, final String targetPackageName) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "start");
        }

        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);

                boolean timeout = true;
                if ((System.currentTimeMillis() - MAX_DURATION) < then) {
                    final String foregroundPackage = UtilsApplication.getForegroundPackage(ctx, HISTORY);

                    if (UtilsString.notNaked(foregroundPackage)) {
                        if (TextUtils.equals(targetPackageName, foregroundPackage)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "foreground remains " + targetPackageName);
                            }
                            Schedulers.trampoline().scheduleDirect(this, Math.max(0, Math.min(then + MAX_DURATION - System.currentTimeMillis(), INTERVAL)), TimeUnit.MILLISECONDS);
                            return;
                        } else {
                            GoogleNowMonitor.this.restartHotword(ctx);
                            timeout = false;
                        }
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "foreground package null");
                        }
                        Schedulers.trampoline().scheduleDirect(this, Math.max(0, Math.min(then + MAX_DURATION - System.currentTimeMillis(), INTERVAL)), TimeUnit.MILLISECONDS);
                        return;
                    }
                }

                if (DEBUG) {
                    MyLog.i(CLS_NAME, "shutting down: timeout: " + timeout);
                }

                if (timeout) {
                    GoogleNowMonitor.this.shutdownHotword(ctx);
                }
            }
        }, INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Send a request to restart the hotword detection
     *
     * @param ctx the application context
     */
    private void restartHotword(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "restartHotword");
        }

        final LocalRequest request = new LocalRequest(ctx);
        request.prepareDefault(LocalRequest.ACTION_START_HOTWORD, null);
        request.execute();
    }

    /**
     * Send a request to prevent the hotword detection from restarting
     *
     * @param ctx the application context
     */
    private void shutdownHotword(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "shutdownHotword");
        }

        final LocalRequest request = new LocalRequest(ctx);
        request.prepareDefault(LocalRequest.ACTION_STOP_HOTWORD, null);
        request.setShutdownHotword();
        request.execute();
    }
}
