package ai.saiy.android.firebase.database.reference;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.firebase.database.read.BeyondVerbal;
import ai.saiy.android.utils.MyLog;

public class BeyondVerbalReference {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = BeyondVerbalReference.class.getSimpleName();

    private BeyondVerbal getRequestBeyondVerbal() {
        final com.google.android.gms.tasks.TaskCompletionSource<BeyondVerbal> taskCompletionSource = new com.google.android.gms.tasks.TaskCompletionSource<>();
        com.google.android.gms.tasks.Task<BeyondVerbal> task = taskCompletionSource.getTask();
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("db_read").child("beyond_verbal").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (BeyondVerbalReference.DEBUG) {
                    MyLog.i(BeyondVerbalReference.CLS_NAME, "getRequestBeyondVerbal: onDataChange");
                }
                taskCompletionSource.setResult(dataSnapshot.getValue(BeyondVerbal.class));
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (BeyondVerbalReference.DEBUG) {
                    MyLog.i(BeyondVerbalReference.CLS_NAME, "getRequestBeyondVerbal: onCancelled");
                }
                taskCompletionSource.setException(error.toException());
            }
        });
        try {
            return com.google.android.gms.tasks.Tasks.await(task, 3000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestBeyondVerbal: InterruptedException");
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestBeyondVerbal: ExecutionException");
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestBeyondVerbal: TimeoutException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestBeyondVerbal: Exception");
                e.printStackTrace();
            }
        }
        return null;
    }

    public @NonNull Pair<Boolean, String> getKey(Context context) {
        final BeyondVerbal beyondVerbal = getRequestBeyondVerbal();
        if (beyondVerbal != null) {
            final String apiKey = beyondVerbal.getApiKey();
            if (ai.saiy.android.utils.UtilsString.notNaked(apiKey)) {
                final String key = new ai.saiy.android.device.DeviceInfo().createKeys(context, apiKey);
                if (ai.saiy.android.utils.UtilsString.notNaked(key)) {
                    return new Pair<>(true, key);
                }
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getKey: decrypted naked");
                }
            } else if (DEBUG) {
                MyLog.w(CLS_NAME, "getKey: encrypted naked");
            }
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "getKey: request null");
        }
        return new Pair<>(false, null);
    }
}
