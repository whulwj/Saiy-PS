package ai.saiy.android.firebase.database.reference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.saiy.android.firebase.database.read.WeatherProvider;
import ai.saiy.android.utils.MyLog;

public class WeatherProviderReference {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = WeatherProviderReference.class.getSimpleName();

    public @Nullable WeatherProvider getRequestWeatherProvider() {
        final com.google.android.gms.tasks.TaskCompletionSource<WeatherProvider> taskCompletionSource = new com.google.android.gms.tasks.TaskCompletionSource<>();
        final com.google.android.gms.tasks.Task<WeatherProvider> task = taskCompletionSource.getTask();
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("db_read").child("provider").child("weather").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getRequestWeatherProvider: onDataChange");
                }
                taskCompletionSource.setResult(dataSnapshot.getValue(WeatherProvider.class));
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getRequestWeatherProvider: onCancelled");
                }
                taskCompletionSource.setException(error.toException());
            }
        });
        try {
            return com.google.android.gms.tasks.Tasks.await(task, 3000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestWeatherProvider: InterruptedException");
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestWeatherProvider: ExecutionException");
                e.printStackTrace();
            }
        } catch (TimeoutException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestWeatherProvider: TimeoutException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getRequestWeatherProvider: Exception");
                e.printStackTrace();
            }
        }
        return null;
    }
}
