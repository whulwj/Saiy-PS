package ai.saiy.android.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class BluetoothConstants {
    public static final int CONNECTION_A2DP = 0;
    public static final int CONNECTION_SCO = 1;
    @IntDef({BluetoothConstants.CONNECTION_A2DP, BluetoothConstants.CONNECTION_SCO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HeadsetConnectionType {}

    public static final int STREAM_COMMUNICATION = 0;
    public static final int STREAM_CALL = 1;
    public static final int STREAM_VOICE_CALL = 2;
    @IntDef({BluetoothConstants.STREAM_COMMUNICATION, BluetoothConstants.STREAM_CALL, BluetoothConstants.STREAM_VOICE_CALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HeadsetStreamType {}

    public static final int SYSTEM_ONE = 0;
    public static final int SYSTEM_TWO = 1;
    public static final int SYSTEM_THREE = 2;
    @IntDef({BluetoothConstants.SYSTEM_ONE, BluetoothConstants.SYSTEM_TWO, BluetoothConstants.SYSTEM_THREE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HeadsetSystem {}
}
