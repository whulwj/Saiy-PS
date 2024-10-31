package ai.saiy.android.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import ai.saiy.android.firebase.FirebaseInstallationsHelper;
import ai.saiy.android.firebase.UserFirebaseListener;
import ai.saiy.android.firebase.UtilsFirebase;
import ai.saiy.android.user.UserFirebaseHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import io.reactivex.rxjava3.schedulers.Schedulers;

public final class ViewModelFirebaseAuth extends AndroidViewModel implements LifecycleEventObserver,
        UserFirebaseListener {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ViewModelFirebaseAuth.class.getSimpleName();

    private FirebaseAuth firebaseAuth;
    private volatile boolean isUserSignedIn;
    private final MutableLiveData<Boolean> isAddFree = new MutableLiveData<>();
    private volatile boolean havePersisted;

    private final AtomicInteger signInCount = new AtomicInteger();
    private final FirebaseAuth.AuthStateListener mAuth = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            final com.google.firebase.auth.FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser == null) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onAuthStateChanged: firebaseUser null");
                }
                signInAnonymously();
                return;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onAuthStateChanged: firebaseUser signed in: " + firebaseUser.getUid());
                MyLog.i(CLS_NAME, "onAuthStateChanged: firebaseUser anonymous: " + firebaseUser.isAnonymous());
            }
            ViewModelFirebaseAuth.this.isUserSignedIn = true;
            if (firebaseUser.isAnonymous()) {
                SPH.setFirebaseAnonymousUid(getApplication(), firebaseUser.getUid());
            } else {
                SPH.setFirebaseUid(getApplication(), firebaseUser.getUid());
            }
            if (havePersisted) {
                return;
            }
            ViewModelFirebaseAuth.this.havePersisted = true;
            persistFirebase();
        }
    };

    public ViewModelFirebaseAuth(@NonNull Application application) {
        super(application);
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
        if (Lifecycle.Event.ON_START == event) {
            if (firebaseAuth != null) {
                firebaseAuth.addAuthStateListener(mAuth);
            } else if (DEBUG) {
                MyLog.e(CLS_NAME, "mAuth null");
            }
        } else if (Lifecycle.Event.ON_STOP == event) {
            if (firebaseAuth != null) {
                firebaseAuth.removeAuthStateListener(mAuth);
            }
        }
    }

    private void persistFirebase() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "persistFirebase");
        }
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                com.google.firebase.database.FirebaseDatabase.getInstance().setPersistenceEnabled(true);
/*                final com.google.firebase.database.DatabaseReference databaseReference = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("db_read");
                databaseReference.child("bing").child("translate").keepSynced(true);
                databaseReference.child("bing").child("speaker_recognition").keepSynced(true);
                databaseReference.child("provider").child("translation").keepSynced(true);
                databaseReference.child("provider").child("weather").keepSynced(true);
                databaseReference.child("bugs").child("known_bugs").keepSynced(true);
                databaseReference.child("google").child("translate").keepSynced(true);
                databaseReference.child("google").child("geo").keepSynced(true);
                databaseReference.child("open_weather_map").keepSynced(true);
                databaseReference.child("twitter").keepSynced(true);
                databaseReference.child("weather_online").keepSynced(true);
                databaseReference.child("wordnik").keepSynced(true);
                databaseReference.child("beyond_verbal").keepSynced(true);
                databaseReference.child("foursquare").keepSynced(true);
                databaseReference.child("version").keepSynced(true);
                databaseReference.child("iap").keepSynced(true);*/
                final ai.saiy.android.firebase.UserFirebase userFirebase = UtilsFirebase.getUserFirebase(getApplication());
                if (userFirebase != null) {
                    com.google.firebase.database.FirebaseDatabase.getInstance().getReference("db_read_write").child("users").child(userFirebase.getUid()).keepSynced(true);
                } else if (DEBUG) {
                    MyLog.i(CLS_NAME, "persistFirebase: userFirebase null");
                }
                new UserFirebaseHelper().isAdFree(getApplication(), ViewModelFirebaseAuth.this);
            }
        });
    }

    /**
     * function to sign in to Firebase Anonymously
     */
    private void signInAnonymously() {
        firebaseAuth.signInAnonymously().addOnCompleteListener(Executors.newSingleThreadExecutor(), new com.google.android.gms.tasks.OnCompleteListener<com.google.firebase.auth.AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "signInAnonymously: onComplete: " + task.isSuccessful());
                }
                if (task.isSuccessful()) {
                    // Sign in success
                    ViewModelFirebaseAuth.this.isUserSignedIn = true;
                    FirebaseInstallationsHelper.getFirebaseInstanceId();
                    return;
                }
                if (DEBUG) {
                    task.getException().printStackTrace();
                }
                // If sign in fails, try it again later.
                if (signInCount.incrementAndGet() < 4) {
                    try {
                        Thread.sleep(signInCount.get() * 5000L);
                    } catch (InterruptedException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "signInAnonymously InterruptedException");
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public boolean userSignedIn() {
        return isUserSignedIn;
    }

    public @NonNull MutableLiveData<Boolean> isAddFree() {
        return isAddFree;
    }

    @Override
    public void onDetermineAdFree(boolean isAddFree) {
        ViewModelFirebaseAuth.this.isAddFree.postValue(isAddFree);
    }
}
