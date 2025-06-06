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

package ai.saiy.android.utils;

import static ai.saiy.android.applications.Install.Location.PLAYSTORE;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.android.material.color.ColorContrast;
import com.google.android.material.color.ColorContrastOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

import java.io.IOException;

import ai.saiy.android.R;
import ai.saiy.android.applications.Install;
import ai.saiy.android.configuration.GoogleConfiguration;
import ai.saiy.android.lib.ProcessStateOwner;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.recognition.provider.wit.RecognitionWitHybrid;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.CurrentActivityProvider;
import dagger.hilt.android.HiltAndroidApp;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Helper Class to deal with application wide variables. Use with caution as the expected persistent
 * behaviour is not always consistent.
 * <p/>
 * Created by benrandall76@gmail.com on 23/02/2016.
 */
@HiltAndroidApp
public class Global extends MultiDexApplication implements Application.ActivityLifecycleCallbacks, ProcessStateOwner {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RecognitionWitHybrid.class.getSimpleName();
    public static final Install.Location installLocation = PLAYSTORE;
    private static boolean sIsInVoiceTutorial;
    private static String sGlobalAmazonID = "";
    private static volatile Bundle alexDirectiveBundle;

    private int mStartedCounter = 0;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        final FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());
        registerActivityLifecycleCallbacks(this);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ColorContrast.applyToActivitiesIfAvailable(
                this,
                new ColorContrastOptions.Builder()
                        .setMediumContrastThemeOverlay(R.style.ThemeOverlay_AppTheme_MediumContrast)
                        .setHighContrastThemeOverlay(R.style.ThemeOverlay_AppTheme_HighContrast)
                        .build());
        RxJavaPlugins.setFailOnNonBlockingScheduler(true);
        // TODO
        com.facebook.FacebookSdk.sdkInitialize(this);
        com.facebook.appevents.AppEventsLogger.activateApp(this);
        com.google.firebase.database.FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        setGlobalId();
        authenticateGoogleCloud();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        CurrentActivityProvider.onActivityCreated(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        mStartedCounter++;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        mStartedCounter--;
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        CurrentActivityProvider.onActivityDestroyed(activity);
    }

    public boolean isForeground() {
        return mStartedCounter > 0;
    }

    public static boolean isInVoiceTutorial() {
        return sIsInVoiceTutorial;
    }

    public static void setVoiceTutorialState(Context context, boolean isInVoiceTutorial) {
        if (sIsInVoiceTutorial && !isInVoiceTutorial) {
            Bundle bundle = new Bundle();
            bundle.putInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_SELF_AWARE);
            ai.saiy.android.service.helper.SelfAwareHelper.startServiceWithIntent(context, bundle);
        }
        sIsInVoiceTutorial = isInVoiceTutorial;
    }

    public static String getGlobalID() {
        return sGlobalAmazonID;
    }

    public static void setAlexDirectiveBundle(Bundle bundle) {
        alexDirectiveBundle = bundle;
    }

    public static Bundle getAlexDirectiveBundle() {
        return alexDirectiveBundle != null ? alexDirectiveBundle : new Bundle();
    }

    private void setGlobalId() {
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                if (UtilsString.notNaked(Global.getGlobalID())) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "setGlobalId: already set");
                    }
                    return;
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setGlobalId: setting");
                }
                try {
                    Global.sGlobalAmazonID = ai.saiy.android.user.SaiyAccount.getUniqueId(Global.this.getApplicationContext());
                } catch (RuntimeException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "setGlobalId: RuntimeException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "setGlobalId: Exception");
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void authenticateGoogleCloud() {
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                try {
                    GoogleConfiguration.authenticateExplicit();
                } catch (IOException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "authenticateGoogleCloud: IOException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "authenticateGoogleCloud: Exception");
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
