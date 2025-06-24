package ai.saiy.android.command.navigation;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import ai.saiy.android.applications.Installed;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

public class UtilsNavigation {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UtilsNavigation.class.getSimpleName();
    private static final int UNKNOWN = 0;
    private static final int WAZE = 1;
    private static final int AURA = 2;

    public static boolean haveSupportedApplication(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "haveSupportedApplication");
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("google.navigation:q=test&mode=d"));
        return UtilsList.notNaked(context.getPackageManager().queryIntentActivities(intent, 0));
    }

    public static boolean navigateToAddress(Context context, double latitude, double longitude, String address) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "navigateToAddress");
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        switch (getDefaultNavigationApplication(context)) {
            case WAZE:
                intent.setData(Uri.parse("https://waze.com/ul?ll=" + latitude + Constants.SEP_COMMA + longitude + "&navigate=yes"));
                break;
            case AURA:
                intent.setData(Uri.parse("com.sygic.aura://coordinate|" + longitude + "|" + latitude + "|drive"));
                break;
            default:
                intent.setData(Uri.parse("google.navigation:q=" + latitude + Constants.SEP_COMMA + longitude + address));
                break;
        }
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "navigateToAddress ActivityNotFoundException");
                e.printStackTrace();
            }
            return false;
        }
    }

    public static boolean navigateToAddress(Context context, String address) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "navigateToAddress");
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        switch (getDefaultNavigationApplication(context)) {
            case WAZE:
                intent.setData(Uri.parse("https://waze.com/ul?q=" + address + "&navigate=yes"));
                break;
            case AURA:
                intent.setData(Uri.parse("geo:0,0?q=" + address.replaceAll(Constants.SEP_SPACE, "+")));
                break;
            default:
                intent.setData(Uri.parse("google.navigation:q=" + address));
                break;
        }
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "navigateToAddress ActivityNotFoundException");
                e.printStackTrace();
            }
            return false;
        }
    }

    public static int getDefaultNavigationApplication(Context context) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("google.navigation:q=test&mode=d"));
        final ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, 0);
        if (resolveInfo == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getDefaultNavigationApplication: resolveInfo: null");
            }
            return UNKNOWN;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getDefaultNavigationApplication: resolveInfo: " + resolveInfo);
        }
        final String packageName = UtilsApplication.getPackageName(resolveInfo);
        if (!UtilsString.notNaked(packageName)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getDefaultNavigationApplication: resolveInfo: packageName naked");
            }
            return UNKNOWN;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getDefaultNavigationApplication: resolveInfo: packageName: " + packageName);
        }
        if (packageName.matches(Installed.PACKAGE_WAZE)) {
            return WAZE;
        }
        return packageName.matches(Installed.PACKAGE_AURA) ? AURA : UNKNOWN;
    }
}
