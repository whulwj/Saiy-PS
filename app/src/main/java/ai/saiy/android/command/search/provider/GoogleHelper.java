package ai.saiy.android.command.search.provider;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class GoogleHelper {
    public static final int GENERIC = 0;
    public static final int IMAGE = 0b0100000;
    public static final int VIDEO = 0b1000000;
    @IntDef({GENERIC, IMAGE, VIDEO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}
}
