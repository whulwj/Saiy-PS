package ai.saiy.android.command.intent;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CustomIntent implements Parcelable {
    public static final int TARGET_ACTIVITY = 0;
    public static final int TARGET_BROADCAST_RECEIVER = 1;
    public static final int TARGET_SERVICE = 2;
    @IntDef({TARGET_ACTIVITY, TARGET_BROADCAST_RECEIVER, TARGET_SERVICE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Target {}

    @SerializedName("action")
    private String action;
    @SerializedName("category")
    private String category;
    @SerializedName("className")
    private String className;
    @SerializedName("data")
    private String data;
    @SerializedName("extras")
    private String extras;
    @SerializedName("mimeType")
    private String mimeType;
    @SerializedName("packageName")
    private String packageName;
    @SerializedName("target")
    private @Target int target;

    public CustomIntent() {
    }

    public CustomIntent(int target, String action, String category, String packageName, String className, String data, String extras, String mimeType) {
        this.target = target;
        this.action = action;
        this.category = category;
        this.packageName = packageName;
        this.className = className;
        this.data = data;
        this.extras = extras;
        this.mimeType = mimeType;
    }

    public static final Creator<CustomIntent> CREATOR = new Creator<CustomIntent>() {
        @Override
        public CustomIntent createFromParcel(Parcel in) {
            return new CustomIntent(in.readInt(), in.readString(), in.readString(),
            in.readString(), in.readString(), in.readString(), in.readString(), in.readString());
        }

        @Override
        public CustomIntent[] newArray(int size) {
            return new CustomIntent[size];
        }
    };

    public String getAction() {
        return this.action != null ? this.action : "";
    }

    public String getCategory() {
        return this.category != null ? this.category : "";
    }

    public String getClassName() {
        return this.className != null ? this.className : "";
    }

    public String getData() {
        return this.data != null ? this.data : "";
    }

    public String getExtras() {
        return this.extras != null ? this.extras : "";
    }

    public String getMimeType() {
        return this.mimeType != null ? this.mimeType : "";
    }

    public String getPackageName() {
        return this.packageName != null ? this.packageName : "";
    }

    public @Target int getTarget() {
        return this.target;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setExtras(String extras) {
        this.extras = extras;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setTarget(@Target int target) {
        this.target = target;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(target);
        dest.writeString(action);
        dest.writeString(category);
        dest.writeString(packageName);
        dest.writeString(className);
        dest.writeString(data);
        dest.writeString(extras);
        dest.writeString(mimeType);
    }
}
