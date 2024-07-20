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

import android.content.Context;
import android.os.Bundle;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;

import ai.saiy.android.R;
import ai.saiy.android.applications.Install;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.recognition.provider.wit.RecognitionWitHybrid;
import ai.saiy.android.service.helper.LocalRequest;

/**
 * Helper Class to deal with application wide variables. Use with caution as the expected persistent
 * behaviour is not always consistent.
 * <p/>
 * Created by benrandall76@gmail.com on 23/02/2016.
 */
public class Global extends MultiDexApplication {

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = RecognitionWitHybrid.class.getSimpleName();
    public static final Install.Location installLocation = PLAYSTORE;
    public static String PROJECT_ID = "";
    private static boolean sIsInVoiceTutorial;
    private static String sGlobalAmazonID;
    private static volatile Bundle alexDirectiveBundle;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO
        PROJECT_ID = getApplicationContext().getString(R.string.gcp_project_id);
        setGlobalId();
        FirebaseApp.initializeApp(this);
        final FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance());
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (UtilsString.notNaked(Global.getGlobalID())) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "setGlobalId: already set");
                        return;
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
        }).start();
    }
}
