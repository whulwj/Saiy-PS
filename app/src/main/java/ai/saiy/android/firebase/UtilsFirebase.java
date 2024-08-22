package ai.saiy.android.firebase;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executors;

import ai.saiy.android.firebase.database.read.TranslationProvider;
import ai.saiy.android.firebase.database.read.WeatherProvider;
import ai.saiy.android.firebase.database.reference.TranslationProviderReference;
import ai.saiy.android.firebase.database.reference.WeatherProviderReference;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class UtilsFirebase {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UtilsFirebase.class.getSimpleName();

    public static UserFirebase getUserFirebase(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getUserFirebase");
        }
        final UserFirebase userFirebase = new UserFirebase();
        String firebaseUid = ai.saiy.android.utils.SPH.getFirebaseUid(context);
        if (UtilsString.notNaked(firebaseUid)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getUserFirebase: auth user");
            }
            userFirebase.setAnonymous(false);
            userFirebase.setMigrated(false);
            userFirebase.setUid(firebaseUid);
        } else {
            firebaseUid = ai.saiy.android.utils.SPH.getFirebaseMigratedUid(context);
        }
        if (UtilsString.notNaked(firebaseUid)) {
            if (DEBUG) {
                ai.saiy.android.utils.MyLog.w(CLS_NAME, "getUserFirebase: migrated user");
            }
            userFirebase.setAnonymous(false);
            userFirebase.setMigrated(true);
            userFirebase.setUid(firebaseUid);
        } else {
            firebaseUid = ai.saiy.android.utils.SPH.getFirebaseAnonymousUid(context);
        }
        if (UtilsString.notNaked(firebaseUid)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getUserFirebase: anonymous user");
            }
            userFirebase.setAnonymous(true);
            userFirebase.setMigrated(false);
            userFirebase.setUid(firebaseUid);
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getUserFirebase: checking instance");
            }
            final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                firebaseUid = firebaseUser.getUid();
                if (UtilsString.notNaked(firebaseUid)) {
                    userFirebase.setMigrated(false);
                    userFirebase.setUid(firebaseUid);
                    if (firebaseUser.isAnonymous()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getUserFirebase: checking instance: anonymous user");
                        }
                        userFirebase.setAnonymous(true);
                        ai.saiy.android.utils.SPH.setFirebaseAnonymousUid(context, firebaseUid);
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getUserFirebase: checking instance: auth user");
                        }
                        userFirebase.setAnonymous(false);
                        ai.saiy.android.utils.SPH.setFirebaseUid(context, firebaseUid);
                    }
                } else if (DEBUG) {
                    MyLog.i(CLS_NAME, "getUserFirebase: checking instance: uId naked");
                }
            } else if (DEBUG) {
                ai.saiy.android.utils.MyLog.w(CLS_NAME, "getUserFirebase: checking instance: user null");
            }
        }
        if (UtilsString.notNaked(firebaseUid)) {
            return userFirebase;
        }
        return null;
    }

    public static void doPeriodic(final Context context) {
        final long usedIncrement = ai.saiy.android.utils.SPH.getUsedIncrement(context);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "doPeriodic: " + usedIncrement);
        }
        if (usedIncrement <= 0 || usedIncrement % 5 != 0) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "doPeriodic: skipping");
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "doPeriodic: updating");
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final UserFirebase userFirebase = getUserFirebase(context);
                    if (userFirebase == null || !UtilsString.notNaked(userFirebase.getUid())) {
                        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(Executors.newSingleThreadExecutor(), new com.google.android.gms.tasks.OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "signInAnonymously: onComplete: " + task.isSuccessful());
                                }
                                if (!task.isSuccessful()) {
                                    // Sign in fails
                                    if (DEBUG) {
                                        task.getException().printStackTrace();
                                    }
                                    return;
                                }
                                // Sign in success, update UI with the signed-in user's information
                                try {
                                    final FirebaseUser firebaseUser = task.getResult().getUser();
                                    if (firebaseUser != null) {
                                        if (firebaseUser.isAnonymous()) {
                                            ai.saiy.android.utils.SPH.setFirebaseAnonymousUid(context, firebaseUser.getUid());
                                        } else {
                                            ai.saiy.android.utils.SPH.setFirebaseUid(context, firebaseUser.getUid());
                                        }
                                    } else {
                                        ai.saiy.android.utils.MyLog.w(CLS_NAME, "signInAnonymously: no user");
                                    }
                                    FirebaseInstallationsHelper.getFirebaseInstanceId();
                                    doPeriodicProviders(context);
                                } catch (NullPointerException e) {
                                    if (DEBUG) {
                                        ai.saiy.android.utils.MyLog.w(CLS_NAME, "signInAnonymously: NullPointerException");
                                        e.printStackTrace();
                                    }
                                } catch (Exception e) {
                                    if (DEBUG) {
                                        ai.saiy.android.utils.MyLog.w(CLS_NAME, "signInAnonymously: Exception");
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "doPeriodic: have firebase uid");
                        }
                        doPeriodicProviders(context);
                    }
                }
            }).start();
        }
    }

    private static void doPeriodicProviders(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "doPeriodicProviders");
        }
        final TranslationProvider translationProvider = new TranslationProviderReference().getRequestTranslation();
        if (translationProvider != null) {
            ai.saiy.android.utils.SPH.setDefaultTranslationProvider(context, translationProvider.getDefaultProvider());
        } else if (DEBUG) {
            ai.saiy.android.utils.MyLog.w(CLS_NAME, "doPeriodic: translationProvider null");
        }
        final WeatherProvider weatherProvider = new WeatherProviderReference().getRequestWeatherProvider();
        if (weatherProvider != null) {
            ai.saiy.android.utils.SPH.setWeatherProvider(context, weatherProvider.getDefaultProvider());
        } else if (DEBUG) {
            ai.saiy.android.utils.MyLog.w(CLS_NAME, "doPeriodic: weatherProvider null");
        }
    }
}
