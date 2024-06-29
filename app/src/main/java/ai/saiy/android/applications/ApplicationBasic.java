package ai.saiy.android.applications;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ApplicationBasic implements Parcelable {
    private String action;
    private String name;
    private String packageName;

    public ApplicationBasic(String name, String packageName) {
        this.name = name;
        this.packageName = packageName;
    }

    public static final Creator<ApplicationBasic> CREATOR = new Creator<ApplicationBasic>() {
        @Override
        public ApplicationBasic createFromParcel(Parcel in) {
            final String action = in.readString();
            final ApplicationBasic applicationBasic = new ApplicationBasic(in.readString(), in.readString());
            applicationBasic.setAction(action);
            return applicationBasic;
        }

        @Override
        public ApplicationBasic[] newArray(int size) {
            return new ApplicationBasic[size];
        }
    };

    public String getAction() {
        return this.action;
    }

    public String getName() {
        return this.name;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(action);
        parcel.writeString(name);
        parcel.writeString(packageName);
    }

    @Override
    public @NonNull String toString() {
        return "ApplicationBasic{" +
                "action='" + action + '\'' +
                ", name='" + name + '\'' +
                ", packageName='" + packageName + '\'' +
                '}';
    }
}
