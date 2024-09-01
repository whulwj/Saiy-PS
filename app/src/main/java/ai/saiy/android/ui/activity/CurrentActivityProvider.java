package ai.saiy.android.ui.activity;

import android.app.Activity;

import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.android.internal.ThreadUtil;
import dagger.hilt.android.scopes.ActivityRetainedScoped;
import dagger.hilt.internal.GeneratedComponentManagerHolder;

/**
 * <a href="https://blog.p-y.wtf/using-an-activity-from-a-hilt-viewmodel" />
 */
@ActivityRetainedScoped
public final class CurrentActivityProvider {
    private final static boolean DEBUG = false;
    private @Nullable Activity mCurrentActivity;

    public @Inject CurrentActivityProvider() {
    }

    public void withActivity(@NotNull Consumer<Activity> block) {
        checkMainThread();
        final Activity activity = this.mCurrentActivity;
        if (activity == null) {
            if (DEBUG) {
                throw new IllegalStateException("Don't call this after the activity is finished!");
            } else {
                block.accept(null);
            }
        } else {
            block.accept(activity);
        }
    }

    private void checkMainThread() {
        ThreadUtil.ensureMainThread();
    }

    private static CurrentActivityProvider getProvider(Activity activity) {
        if (activity instanceof GeneratedComponentManagerHolder) {
            ActivityProviderEntryPoint entryPoint = EntryPointAccessors.fromActivity(activity, ActivityProviderEntryPoint.class);
            return entryPoint.getActivityProvider();
        }
        return null;
    }

    public static void onActivityCreated(@NotNull final Activity activity) {
        final CurrentActivityProvider provider = getProvider(activity);
        if (provider != null) {
            provider.mCurrentActivity = activity;
        }
    }

    public static void onActivityDestroyed(@NotNull final Activity activity) {
        final CurrentActivityProvider provider = getProvider(activity);
        if (provider != null && provider.mCurrentActivity == activity) {
            provider.mCurrentActivity = null;
        }
    }
}
