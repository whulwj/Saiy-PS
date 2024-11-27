package ai.saiy.android.firebase.database.reference;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.firebase.database.read.BingSpeakerRecognition;
import ai.saiy.android.utils.MyLog;

public class BingSpeakerRecognitionReference {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = BingSpeakerRecognitionReference.class.getSimpleName();

    private BingSpeakerRecognition getRequestBingSpeakerRecognition() {
        final com.google.android.gms.tasks.TaskCompletionSource<BingSpeakerRecognition> taskCompletionSource = new com.google.android.gms.tasks.TaskCompletionSource<>();
        com.google.android.gms.tasks.Task<BingSpeakerRecognition> task = taskCompletionSource.getTask();
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("db_read").child("bing").child("speaker_recognition").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (BingSpeakerRecognitionReference.DEBUG) {
                    MyLog.i(BingSpeakerRecognitionReference.CLS_NAME, "getRequestBingSpeakerRecognition: onDataChange");
                }
                taskCompletionSource.setResult(dataSnapshot.getValue(BingSpeakerRecognition.class));
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (BingSpeakerRecognitionReference.DEBUG) {
                    MyLog.i(BingSpeakerRecognitionReference.CLS_NAME, "getRequestBingSpeakerRecognition: onCancelled");
                }
                taskCompletionSource.setException(error.toException());
            }
        });
        try {
            return com.google.android.gms.tasks.Tasks.await(task, 3000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestBingSpeakerRecognition: InterruptedException");
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestBingSpeakerRecognition: ExecutionException");
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestBingSpeakerRecognition: TimeoutException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestBingSpeakerRecognition: Exception");
                e.printStackTrace();
            }
        }
        return null;
    }

    public @NonNull Pair<Boolean, String> getCredential(Context context) {
        final BingSpeakerRecognition bingSpeakerRecognition = getRequestBingSpeakerRecognition();
        if (bingSpeakerRecognition != null) {
            final String apiKey = bingSpeakerRecognition.getApiKey();
            if (ai.saiy.android.utils.UtilsString.notNaked(apiKey)) {
                final String key = new ai.saiy.android.device.DeviceInfo().createKeys(context, apiKey);
                if (ai.saiy.android.utils.UtilsString.notNaked(key)) {
                    return new Pair<>(true, key);
                }
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getCredential: decrypted naked");
                }
            } else if (DEBUG) {
                MyLog.w(CLS_NAME, "getCredential: encrypted naked");
            }
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "getCredential: request null");
        }
        return new Pair<>(false, null);
    }
}
