package ai.saiy.android.accessibility;

import android.content.Context;

import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.applications.ApplicationBasic;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsParcelable;

public class BlockedApplicationsHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = BlockedApplicationsHelper.class.getSimpleName();

    public static void save(Context context, BlockedApplications blockedApplications) {
        if (blockedApplications == null) {
            blockedApplications = defaultBlockedApplications(context);
        }
        String base64String = UtilsParcelable.parcelable2String(blockedApplications);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "save: base64String: " + base64String);
        }
        SPH.setBlockedNotificationApplications(context, base64String);
    }

    public static BlockedApplications getBlockedApplications(Context context) {
        if (havaBlockedNotificationApplications(context)) {
            try {
                BlockedApplications blockedApplications = UtilsParcelable.unmarshall(SPH.getBlockedNotificationApplications(context), BlockedApplications.CREATOR);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "blockedApps: " + blockedApplications);
                }
                return blockedApplications;
            } catch (JsonSyntaxException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "JsonSyntaxException");
                    e.printStackTrace();
                }
            } catch (NullPointerException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "NullPointerException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Exception");
                    e.printStackTrace();
                }
            }
        }
        return defaultBlockedApplications(context);
    }

    private static boolean havaBlockedNotificationApplications(Context context) {
        return SPH.getBlockedNotificationApplications(context) != null;
    }

    private static BlockedApplications defaultBlockedApplications(Context context) {
        ApplicationBasic applicationBasic = new ApplicationBasic(context.getString(R.string.app_name), context.getPackageName());
        ArrayList<ApplicationBasic> arrayList = new ArrayList<>(1);
        arrayList.add(applicationBasic);
        return new BlockedApplications(arrayList);
    }
}
