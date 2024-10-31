package ai.saiy.android.common;

import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@InstallIn(SingletonComponent.class)
@Module
public abstract class ExecutorModule {
    @Provides
    @Singleton
    static Handler provideResultHandler() {
        return HandlerCompat.createAsync(Looper.getMainLooper());
    }
}
