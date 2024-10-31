package ai.saiy.android.database.helper;

import android.content.Context;
import android.os.Process;
import android.util.Pair;

import java.util.ArrayList;

import ai.saiy.android.database.DBApplication;
import ai.saiy.android.database.DBContact;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DatabaseHelper {
    private static final Object applicationLock = new Object();
    private static final Object contactLock = new Object();

    public void deleteApplications(final Context context) {
        synchronized (applicationLock) {
            Schedulers.io().scheduleDirect(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);
                    new DBApplication(context).deleteTable();
                }
            });
        }
    }

    public void insertApplications(final Context context, final ArrayList<Pair<String, String>> arrayList) {
        synchronized (applicationLock) {
            Schedulers.io().scheduleDirect(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);
                    new DBApplication(context).insertData(arrayList);
                }
            });
        }
    }

    public void deteleContacts(final Context context) {
        synchronized (contactLock) {
            Schedulers.io().scheduleDirect(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);
                    new DBContact(context).deleteTable();
                }
            });
        }
    }
}
