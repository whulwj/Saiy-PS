package ai.saiy.android.firebase.database.reference;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.firebase.database.read.Foursquare;
import ai.saiy.android.utils.MyLog;

public class FoursquareReference {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = FoursquareReference.class.getSimpleName();

    private Foursquare getRequestFoursquare() {
        final com.google.android.gms.tasks.TaskCompletionSource<Foursquare> taskCompletionSource = new com.google.android.gms.tasks.TaskCompletionSource<>();
        final com.google.android.gms.tasks.Task<Foursquare> task = taskCompletionSource.getTask();
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("db_read").child("foursquare").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getRequestFoursquare: onDataChange");
                }
                taskCompletionSource.setResult((dataSnapshot.getValue(Foursquare.class)));
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getRequestFoursquare: onCancelled");
                }
                taskCompletionSource.setException(error.toException());
            }
        });
        try {
            return com.google.android.gms.tasks.Tasks.await(task, 3000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestFoursquare: InterruptedException");
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestFoursquare: ExecutionException");
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestFoursquare: TimeoutException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestFoursquare: Exception");
                e.printStackTrace();
            }
        }
        return null;
    }

    public @NonNull Pair<Boolean, String> getClientId(Context context) {
        final Foursquare foursquare = getRequestFoursquare();
        if (foursquare != null) {
            final String foursquareClientId = foursquare.getClientId();
            if (ai.saiy.android.utils.UtilsString.notNaked(foursquareClientId)) {
                final String clientId = new ai.saiy.android.device.DeviceInfo().createKeys(context, foursquareClientId);
                if (ai.saiy.android.utils.UtilsString.notNaked(clientId)) {
                    return new Pair<>(true, clientId);
                }
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getClientId: decrypted naked");
                }
            } else if (DEBUG) {
                MyLog.w(CLS_NAME, "getClientId: encrypted naked");
            }
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "getClientId: request null");
        }
        return new Pair<>(false, null);
    }
}
