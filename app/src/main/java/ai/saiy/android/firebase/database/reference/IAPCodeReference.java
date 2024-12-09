package ai.saiy.android.firebase.database.reference;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.firebase.UtilsFirebase;
import ai.saiy.android.firebase.database.read.IAPCode;
import ai.saiy.android.utils.MyLog;

public class IAPCodeReference {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = IAPCodeReference.class.getSimpleName();

    private IAPCode getRequestIAP() {
        final com.google.android.gms.tasks.TaskCompletionSource<IAPCode> taskCompletionSource = new com.google.android.gms.tasks.TaskCompletionSource<>();
        com.google.android.gms.tasks.Task<IAPCode> task = taskCompletionSource.getTask();
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference(UtilsFirebase.DATABASE_READ).child("iap").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (IAPCodeReference.DEBUG) {
                    MyLog.i(IAPCodeReference.CLS_NAME, "getRequestIAP: onDataChange");
                }
                taskCompletionSource.setResult(dataSnapshot.getValue(IAPCode.class));
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (IAPCodeReference.DEBUG) {
                    MyLog.i(IAPCodeReference.CLS_NAME, "getRequestIAP: onCancelled");
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

    public @NonNull Pair<Boolean, String> getKey(Context context) {
        final IAPCode iapCode = getRequestIAP();
        if (iapCode != null) {
            final String code = iapCode.getIapCode();
            if (ai.saiy.android.utils.UtilsString.notNaked(code)) {
                final String key = new ai.saiy.android.device.DeviceInfo().createKeys(context, code);
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
