package ai.saiy.android.common;

import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@InstallIn(SingletonComponent.class)
@Module
public abstract class ExecutorModule {
    private static final class SingleHolder {
        private static final ExecutorService DEFAULT = Executors.newSingleThreadExecutor();
    }

    @Provides
    @Singleton
    static ExecutorService provideExecutor() {
        return SingleHolder.DEFAULT;
    }

    @Provides
    @Singleton
    static Handler provideResultHandler() {
        return HandlerCompat.createAsync(Looper.getMainLooper());
    }
}
