package ai.saiy.android.amazon.directives;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import ai.saiy.android.utils.UtilsString;

public class Payload implements Parcelable {
    @SerializedName("url")
    private final String url;
    @SerializedName("format")
    private final String format;

    @SerializedName("profile")
    private String profile;

    @SerializedName("token")
    private final String token;

    public Payload(String url, String format, String token) {
        this.url = url;
        this.format = format;
        this.token = token;
    }

    public static final Creator<Payload> CREATOR = new Creator<Payload>() {
        @Override
        public Payload createFromParcel(Parcel in) {
            final Payload payload = new Payload(in.readString(), in.readString(), in.readString());
            payload.profile = in.readString();
            return payload;
        }

        @Override
        public Payload[] newArray(int size) {
            return new Payload[size];
        }
    };

    public void setProfile(String str) {
        this.profile = str;
    }

    public boolean isCancel() {
        return UtilsString.notNaked(url) && url.contains("CancelAction");
    }

    public boolean isAbandon() {
        return (UtilsString.notNaked(url) && url.contains("ActionableAbandon")) || (UtilsString.notNaked(token) && token.contains("Fallback"));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(url);
        parcel.writeString(format);
        parcel.writeString(token);
        parcel.writeString(profile);
    }
}
