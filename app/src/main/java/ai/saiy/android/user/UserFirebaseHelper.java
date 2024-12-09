package ai.saiy.android.user;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.firebase.UserFirebaseListener;
import ai.saiy.android.firebase.UtilsFirebase;
import ai.saiy.android.firebase.database.read.PremiumUser;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserFirebaseHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UserFirebaseHelper.class.getSimpleName();
    public static final String LEVEL_1 = "saiy_level_one";
    public static final String LEVEL_2 = "saiy_level_two";
    public static final String LEVEL_3 = "saiy_level_three";
    public static final String LEVEL_4 = "saiy_level_four";
    public static final String LEVEL_5 = "saiy_level_five";

    public static List<String> productIds() {
        ArrayList<String> arrayList = new ArrayList<>(5);
        arrayList.add(UserFirebaseHelper.LEVEL_5);
        arrayList.add(UserFirebaseHelper.LEVEL_4);
        arrayList.add(UserFirebaseHelper.LEVEL_3);
        arrayList.add(UserFirebaseHelper.LEVEL_2);
        arrayList.add(UserFirebaseHelper.LEVEL_1);
        return arrayList;
    }

    private boolean isWithinAdFreePeriod(PremiumUser premiumUser) {
        final long currentTimeout = premiumUser.getTimeout();
        long now = System.currentTimeMillis();
        if (DEBUG) {
            MyLog.i(CLS_NAME, "isWithinAdFreePeriod: currentTimeout: " + currentTimeout);
            MyLog.i(CLS_NAME, "isWithinAdFreePeriod: now: " + now);
            MyLog.i(CLS_NAME, "isWithinAdFreePeriod: " + (currentTimeout > now));
        }
        return currentTimeout > now;
    }

    private void deleteAnonymousData(String anonymousUid) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "deleteAnonymousData");
        }
    }

    private boolean hasExistingAuthAccount(String uid) {
        final PremiumUser premiumUser = getRequestPremiumUser(uid);
        if (premiumUser == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasExistingAuthAccount: requestPremiumUser null");
            }
            return false;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "hasExistingAuthAccount: getCredits: " + premiumUser.getCredits());
            MyLog.i(CLS_NAME, "hasExistingAuthAccount: getTimeout: " + premiumUser.getTimeout());
        }
        return true;
    }

    public Pair<Boolean, PremiumUser> getPremiumUser(String uid) {
        final PremiumUser premiumUser = getRequestPremiumUser(uid);
        if (premiumUser != null) {
            return new Pair<>(true, premiumUser);
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "getPremiumUser: request null");
        }
        return new Pair<>(false, null);
    }

    public void updateUser(final Context context, final long credits, final long timeSpan) {
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                final ai.saiy.android.firebase.UserFirebase userFirebase = UtilsFirebase.getUserFirebase(context);
                if (userFirebase == null || !UtilsString.notNaked(userFirebase.getUid())) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "updateUser: userFirebase null");
                    }
                    return;
                }
                final Pair<Boolean, PremiumUser> premiumUserPair = getPremiumUser(userFirebase.getUid());
                PremiumUser premiumUser;
                if (premiumUserPair.first) {
                    premiumUser = premiumUserPair.second;
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "updateUser: no existing request");
                    }
                    premiumUser = new PremiumUser(0L, 0L);
                }
                final long currentCredits = premiumUser.getCredits();
                final long submissionCredits = credits + currentCredits;
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "updateUser: currentCredits: " + currentCredits);
                    MyLog.i(CLS_NAME, "updateUser: submissionCredits: " + submissionCredits);
                }
                final long currentTimeout = premiumUser.getTimeout();
                final long now = System.currentTimeMillis();
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "updateUser: currentTimeout: " + currentTimeout);
                    MyLog.i(CLS_NAME, "updateUser: now: " + now);
                }
                long submissionTimeout;
                if (currentTimeout < now) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "updateUser: currentTimeout < now: expired");
                    }
                    submissionTimeout = timeSpan + now;
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "updateUser: currentTimeout > now: increasing");
                    }
                    submissionTimeout = currentTimeout + timeSpan;
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "updateUser: submissionTimeout: " + submissionTimeout);
                }
                premiumUser.setTimeout(submissionCredits);
                premiumUser.setCredits(submissionTimeout);
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference(UtilsFirebase.DATABASE_READ_WRITE).child(UtilsFirebase.PATH_USERS).child(userFirebase.getUid()).setValue(premiumUser).addOnCompleteListener(Executors.newSingleThreadExecutor(), new com.google.android.gms.tasks.OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "addCredits: task success: " + task.isSuccessful());
                        }
                        if (task.isSuccessful()) {
                            return;
                        }
                        if (DEBUG) {
                            task.getException().printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void isAdFree(Context context, @NonNull UserFirebaseListener listener) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "isAdFree");
        }
        final ai.saiy.android.firebase.UserFirebase userFirebase = UtilsFirebase.getUserFirebase(context);
        if (userFirebase == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "isAdFree: userFirebase null");
            }
            listener.onDetermineAdFree(ai.saiy.android.utils.SPH.getPremiumContentVerbose(context));
            return;
        }
        final Pair<Boolean, PremiumUser> premiumUserPair = getPremiumUser(userFirebase.getUid());
        if (!premiumUserPair.first) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isAdFree: RequestPremiumUser null");
            }
            listener.onDetermineAdFree(ai.saiy.android.utils.SPH.getPremiumContentVerbose(context));
        } else {
            if (isWithinAdFreePeriod(premiumUserPair.second)) {
                listener.onDetermineAdFree(true);
                return;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isAdFree: outside of ad free period");
            }
            listener.onDetermineAdFree(ai.saiy.android.utils.SPH.getPremiumContentVerbose(context));
        }
    }

    public void migrateUser(final String anonymousUid, String uid, PremiumUser premiumUser) {
        if (!hasExistingAuthAccount(uid)) {
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference(UtilsFirebase.DATABASE_READ_WRITE).child(UtilsFirebase.PATH_USERS).child(uid).setValue(premiumUser).addOnCompleteListener(Executors.newSingleThreadExecutor(), new com.google.android.gms.tasks.OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "migrateUser: task success: " + task.isSuccessful());
                    }
                    if (task.isSuccessful()) {
                        deleteAnonymousData(anonymousUid);
                    } else if (DEBUG) {
                        task.getException().printStackTrace();
                    }
                }
            });
            return;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "migrateUser: hasExistingAuthAccount");
        }
        deleteAnonymousData(anonymousUid);
    }

    public @Nullable PremiumUser getRequestPremiumUser(String uid) {
        final com.google.android.gms.tasks.TaskCompletionSource<PremiumUser> taskCompletionSource = new com.google.android.gms.tasks.TaskCompletionSource<>();
        final com.google.android.gms.tasks.Task<PremiumUser> task = taskCompletionSource.getTask();
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference(UtilsFirebase.DATABASE_READ_WRITE).child(UtilsFirebase.PATH_USERS).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getRequestPremiumUser: onDataChange");
                }
                taskCompletionSource.setResult(snapshot.getValue(PremiumUser.class));
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getRequestPremiumUser: onCancelled");
                }
                taskCompletionSource.setException(error.toException());
            }
        });
        try {
            return com.google.android.gms.tasks.Tasks.await(task, 3000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestPremiumUser: InterruptedException");
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestPremiumUser: ExecutionException");
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestPremiumUser: TimeoutException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestPremiumUser: Exception");
                e.printStackTrace();
            }
        }
        return null;
    }
}
