package ai.saiy.android.firebase.database.reference;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.firebase.UtilsFirebase;
import ai.saiy.android.firebase.database.read.KnownBugs;
import ai.saiy.android.utils.MyLog;

public class KnownBugsReference {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = KnownBugsReference.class.getSimpleName();

    public KnownBugs getRequestKnownBugsList() {
        final com.google.android.gms.tasks.TaskCompletionSource<KnownBugs> taskCompletionSource = new com.google.android.gms.tasks.TaskCompletionSource<>();
        com.google.android.gms.tasks.Task<KnownBugs> task = taskCompletionSource.getTask();
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference(UtilsFirebase.DATABASE_READ).child("bugs").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (KnownBugsReference.DEBUG) {
                    MyLog.i(KnownBugsReference.CLS_NAME, "getRequestKnownBugsList: onDataChange");
                }
                taskCompletionSource.setResult(dataSnapshot.getValue(KnownBugs.class));
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (KnownBugsReference.DEBUG) {
                    MyLog.i(KnownBugsReference.CLS_NAME, "getRequestBeyondVerbal: onCancelled");
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
}
