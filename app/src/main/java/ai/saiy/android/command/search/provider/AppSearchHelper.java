package ai.saiy.android.command.search.provider;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ai.saiy.android.applications.Installed;
import ai.saiy.android.command.search.CommandSearchValues;
import ai.saiy.android.utils.MyLog;

public class AppSearchHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = AppSearchHelper.class.getSimpleName();

    private static final String GOOGLE_STORE_COLLECTION = "http://play.google.com/store/apps/collection/";

    public static final int GENERIC = 0;
    public static final int EDITOR =   0b000100000000000;
    public static final int FEATURED = 0b001000000000000;
    public static final int NEW_FREE = 0b001100000000000;
    public static final int NEW_PAID = 0b010000000000000;
    public static final int FREE =     0b010100000000000;
    public static final int PAID =     0b011000000000000;
    public static final int TRENDING = 0b011100000000000;
    public static final int GROSSING = 0b100000000000000;
    @IntDef({GENERIC, EDITOR, FEATURED, NEW_FREE, NEW_PAID, FREE, PAID, TRENDING, GROSSING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    public static boolean search(Context context, CommandSearchValues commandSearchValues) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(Installed.PACKAGE_GOOGLE_STORE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            switch (commandSearchValues.getAppSearchType()) {
                case EDITOR:
                    intent.setData(Uri.parse(GOOGLE_STORE_COLLECTION + "editors_choice"));
                    break;
                case FEATURED:
                    intent.setData(Uri.parse(GOOGLE_STORE_COLLECTION + "featured"));
                    break;
                case NEW_FREE:
                    intent.setData(Uri.parse(GOOGLE_STORE_COLLECTION + "topselling_new_free"));
                    break;
                case NEW_PAID:
                    intent.setData(Uri.parse(GOOGLE_STORE_COLLECTION + "topselling_new_paid"));
                    break;
                case FREE:
                    intent.setData(Uri.parse(GOOGLE_STORE_COLLECTION + "topselling_free"));
                    break;
                case PAID:
                    intent.setData(Uri.parse(GOOGLE_STORE_COLLECTION + "topselling_paid"));
                    break;
                case TRENDING:
                    intent.setData(Uri.parse(GOOGLE_STORE_COLLECTION + "movers_shakers"));
                    break;
                case GROSSING:
                    intent.setData(Uri.parse(GOOGLE_STORE_COLLECTION + "topgrossing"));
                    break;
                default:
                    intent.setData(Uri.parse("market://search?q=" + commandSearchValues.getQuery() + "&c=apps"));
                    break;
            }
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ActivityNotFoundException");
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
            }
        }
        return false;
    }
}
