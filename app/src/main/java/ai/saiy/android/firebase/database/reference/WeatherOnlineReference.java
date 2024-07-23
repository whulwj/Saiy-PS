package ai.saiy.android.firebase.database.reference;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.firebase.database.model.WeatherOnline;
import ai.saiy.android.utils.MyLog;

public class WeatherOnlineReference {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = WeatherOnlineReference.class.getSimpleName();

    private WeatherOnline getRequestWeatherOnline() {
        final com.google.android.gms.tasks.TaskCompletionSource<WeatherOnline> taskCompletionSource = new com.google.android.gms.tasks.TaskCompletionSource<>();
        com.google.android.gms.tasks.Task<WeatherOnline> task = taskCompletionSource.getTask();
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("db_read").child("weather_online").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (DEBUG) {
                   MyLog.i(CLS_NAME, "getRequestWeatherOnline: onDataChange");
                }
                taskCompletionSource.setResult(dataSnapshot.getValue(WeatherOnline.class));
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (DEBUG) {
                   MyLog.i(CLS_NAME, "getRequestWeatherOnline: onCancelled");
                }
                taskCompletionSource.setException(error.toException());
            }
        });
        try {
            return com.google.android.gms.tasks.Tasks.await(task, 3000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestWeatherOnline: InterruptedException");
                e.printStackTrace();
            }
            return null;
        } catch (ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestWeatherOnline: ExecutionException");
                e.printStackTrace();
            }
            return null;
        } catch (TimeoutException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestWeatherOnline: TimeoutException");
                e.printStackTrace();
            }
            return null;
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestWeatherOnline: Exception");
                e.printStackTrace();
            }
            return null;
        }
    }

    public Pair<Boolean, String> getAPIKey(Context context) {
        final WeatherOnline weatherOnline = getRequestWeatherOnline();
        if (weatherOnline != null) {
            final String key = weatherOnline.getApiKey();
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
