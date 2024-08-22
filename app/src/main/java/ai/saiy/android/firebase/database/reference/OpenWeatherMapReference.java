package ai.saiy.android.firebase.database.reference;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.firebase.database.read.OpenWeatherMap;
import ai.saiy.android.utils.MyLog;

public class OpenWeatherMapReference {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = OpenWeatherMapReference.class.getSimpleName();

    private OpenWeatherMap getRequestOpenWeatherMap() {
        final com.google.android.gms.tasks.TaskCompletionSource<OpenWeatherMap> taskCompletionSource = new com.google.android.gms.tasks.TaskCompletionSource<>();
        com.google.android.gms.tasks.Task<OpenWeatherMap> task = taskCompletionSource.getTask();
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("db_read").child("open_weather_map").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getRequestOpenWeatherMap: onDataChange");
                }
                taskCompletionSource.setResult(dataSnapshot.getValue(OpenWeatherMap.class));
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getRequestOpenWeatherMap: onCancelled");
                }
                taskCompletionSource.setException(error.toException());
            }
        });
        try {
            return com.google.android.gms.tasks.Tasks.await(task, 3000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestOpenWeatherMap: InterruptedException");
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestOpenWeatherMap: ExecutionException");
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestOpenWeatherMap: TimeoutException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestOpenWeatherMap: Exception");
                e.printStackTrace();
            }
        }
        return null;
    }

    public Pair<Boolean, String> getAPIKey(Context context) {
        final OpenWeatherMap openWeatherMap = getRequestOpenWeatherMap();
        if (openWeatherMap != null) {
            final String key = openWeatherMap.getApiKey();
            if (ai.saiy.android.utils.UtilsString.notNaked(key)) {
                final String apiKey = new ai.saiy.android.device.DeviceInfo().createKeys(context, key);
                if (ai.saiy.android.utils.UtilsString.notNaked(apiKey)) {
                    return new Pair<>(true, apiKey);
                }
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "getAPIKey: decrypted naked");
                }
            } else if (DEBUG) {
                MyLog.w(CLS_NAME, "getAPIKey: encrypted naked");
            }
        } else if (DEBUG) {
            MyLog.w(CLS_NAME, "getAPIKey: request null");
        }
        return new Pair<>(false, null);
    }
}
