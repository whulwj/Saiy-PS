package ai.saiy.android.common;

import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@InstallIn(SingletonComponent.class)
@Module
public abstract class ExecutorModule {
    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    /*
     * Gets the number of available cores
     * (not always the same as the maximum number of cores)
     */
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    // Instantiates the queue of Runnables as a LinkedBlockingQueue
    private static final BlockingQueue<Runnable> sWorkQueue = new LinkedBlockingQueue<>();
    private static final ExecutorService sExecutorService = new ThreadPoolExecutor(
            Math.min(4, NUMBER_OF_CORES), // Initial pool size
            NUMBER_OF_CORES,              // Max pool size
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            sWorkQueue
    );

    @Provides
    @Singleton
    static ExecutorService provideExecutor() {
        return sExecutorService;
    }

    @Provides
    @Singleton
    static Handler provideResultHandler() {
        return HandlerCompat.createAsync(Looper.getMainLooper());
    }
}
