package ai.saiy.android.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class UtilsWireless {
    //Settings.Global.AIRPLANE_MODE_TOGGLEABLE_RADIOS or Settings.System.AIRPLANE_MODE_TOGGLEABLE_RADIOS
    public static final String AIRPLANE_MODE_TOGGLEABLE_RADIOS = "airplane_mode_toggleable_radios";

    public static boolean isRadioAllowed(Context context, String type) {
        if (!isAirplaneModeOn(context)) {
            return true;
        }
        String toggleable = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)? Settings.Global.getString(context.getContentResolver(), AIRPLANE_MODE_TOGGLEABLE_RADIOS) :
                Settings.System.getString(context.getContentResolver(), AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        return toggleable != null && toggleable.contains(type);
    }

    private static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        }
    }
}
