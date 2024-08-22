package ai.saiy.android.firebase.database.reference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.firebase.database.read.IAP;
import ai.saiy.android.utils.MyLog;

public class IAPReference {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = IAPReference.class.getSimpleName();

    public @Nullable IAP getRequestIAP() {
        final com.google.android.gms.tasks.TaskCompletionSource<IAP> taskCompletionSource = new com.google.android.gms.tasks.TaskCompletionSource<>();
        final com.google.android.gms.tasks.Task<IAP> task = taskCompletionSource.getTask();
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("db_read").child("version").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getRequestIAP: onDataChange");
                }
                taskCompletionSource.setResult(dataSnapshot.getValue(IAP.class));
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getRequestIAP: onCancelled");
                }
                taskCompletionSource.setException(error.toException());
            }
        });
        try {
            return com.google.android.gms.tasks.Tasks.await(task, 3000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestIAP: InterruptedException");
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestIAP: ExecutionException");
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestIAP: TimeoutException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestIAP: Exception");
                e.printStackTrace();
            }
        }
        return null;
    }
}
