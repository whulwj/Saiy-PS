package ai.saiy.android.ui.activity;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;

/**
 * Now we can retrieve the scoped activity provider from an activity instance with:
 * ActivityProviderEntryPoint entryPoint =
 *   EntryPointAccessors.fromActivity(this);
 * CurrentActivityProvider activityProvider = entryPoint.getActivityProvider();
 */
@EntryPoint
@InstallIn(ActivityComponent.class)
public interface ActivityProviderEntryPoint {
    CurrentActivityProvider getActivityProvider();
}
