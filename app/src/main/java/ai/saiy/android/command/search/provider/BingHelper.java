package ai.saiy.android.command.search.provider;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BingHelper {
    public static final int GENERIC = 0;
    public static final int IMAGE = 0b01000000000;
    public static final int VIDEO = 0b11000000000;
    @IntDef({GENERIC, IMAGE, VIDEO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}
}
