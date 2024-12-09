package ai.saiy.android.firebase.database.reference;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.firebase.UtilsFirebase;
import ai.saiy.android.firebase.database.read.Twitter;
import ai.saiy.android.utils.MyLog;

public class TwitterReference {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = TwitterReference.class.getSimpleName();

    private Twitter getRequestTwitter() {
        final com.google.android.gms.tasks.TaskCompletionSource<Twitter> taskCompletionSource = new com.google.android.gms.tasks.TaskCompletionSource<>();
        final com.google.android.gms.tasks.Task<Twitter> task = taskCompletionSource.getTask();
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference(UtilsFirebase.DATABASE_READ).child("twitter").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getRequestTwitter: onDataChange");
                }
                taskCompletionSource.setResult(dataSnapshot.getValue(Twitter.class));
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getRequestTwitter: onCancelled");
                }
                taskCompletionSource.setException(error.toException());
            }
        });
        try {
            return com.google.android.gms.tasks.Tasks.await(task, 3000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestTwitter: InterruptedException");
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestTwitter: ExecutionException");
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestTwitter: TimeoutException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestTwitter: Exception");
                e.printStackTrace();
            }
        }
        return null;
    }

    public @NonNull Pair<Boolean, Pair<String, String>> getCredentials(Context context) {
        final Twitter twitter = getRequestTwitter();
        if (twitter != null) {
            final String key = twitter.getKey();
            final String secret = twitter.getSecret();
            if (ai.saiy.android.utils.UtilsString.notNaked(key) && ai.saiy.android.utils.UtilsString.notNaked(secret)) {
                String consumerKey = new ai.saiy.android.device.DeviceInfo().createKeys(context, key);
                String consumerSecret = new ai.saiy.android.device.DeviceInfo().createKeys(context, secret);
                if (ai.saiy.android.utils.UtilsString.notNaked(consumerKey) && ai.saiy.android.utils.UtilsString.notNaked(consumerSecret)) {
                    return new Pair<>(true, new Pair<>(consumerKey, consumerSecret));
                }
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getCredentials: decrypted naked");
                }
            } else if (DEBUG) {
                MyLog.w(CLS_NAME, "getCredentials: encrypted naked");
            }
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "getCredentials: request null");
        }
        return new Pair<>(false, null);
    }
}
