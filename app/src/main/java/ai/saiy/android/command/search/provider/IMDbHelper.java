package ai.saiy.android.command.search.provider;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ai.saiy.android.command.search.CommandSearchValues;
import ai.saiy.android.utils.MyLog;

public class IMDbHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = IMDbHelper.class.getSimpleName();

    public static final int GENERIC = 0;
    public static final int ACTOR = 0b01000;
    public static final int GENRE = 0b10000;
    @IntDef({GENERIC, ACTOR, GENRE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    public static boolean search(Context context, CommandSearchValues commandSearchValues) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            intent.setData(Uri.parse("imdb:///find?q=" + commandSearchValues.getQuery()));
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "NullPointerException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }
        return false;
    }
}
