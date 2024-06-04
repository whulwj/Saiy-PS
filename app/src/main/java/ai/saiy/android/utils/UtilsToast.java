package ai.saiy.android.utils;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.StringRes;

import me.drakeet.support.toast.ToastCompat;

public class UtilsToast {
    public static void showToast(Context context, @StringRes int resId, int duration) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            ToastCompat.makeText(context, resId, duration).show();
        } else {
            Toast.makeText(context, resId, duration).show();
        }
    }

    public static void showToast(Context context, CharSequence charSequence, int duration) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            ToastCompat.makeText(context, charSequence, duration).show();
        } else {
            Toast.makeText(context, charSequence, duration).show();
        }
    }
}
