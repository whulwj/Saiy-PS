package ai.saiy.android.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import androidx.annotation.NonNull;

public abstract class UtilsParcelable {
    public static byte[] marshall(@NonNull Parcelable parcelable) {
        final Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        final byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    public static String parcelable2String(@NonNull Parcelable parcelable) {
        final byte[] bytes = marshall(parcelable);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Parcel unmarshall(@NonNull byte[] bytes) {
        final Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // This is extremely important!
        return parcel;
    }

    public static <T> T unmarshall(@NonNull byte[] bytes, Parcelable.Creator<T> creator) {
        final Parcel parcel = unmarshall(bytes);
        final T result = creator.createFromParcel(parcel);
        parcel.recycle();
        return result;
    }

    public static <T> T unmarshall(String str, @NonNull Parcelable.Creator<T> creator) {
        final byte[] bytes = Base64.decode(str, Base64.DEFAULT);
        return unmarshall(bytes, creator);
    }
}
