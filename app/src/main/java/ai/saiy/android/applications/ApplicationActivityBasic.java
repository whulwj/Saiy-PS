package ai.saiy.android.applications;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

public class ApplicationActivityBasic implements Parcelable {
    private String activityName;
    private String intentExtras;
    private CharSequence name;
    private String packageName;

    public ApplicationActivityBasic(CharSequence label, String packageName, String activityName, String intentExras) {
        this.name = label;
        this.packageName = packageName;
        this.activityName = activityName;
        this.intentExtras = intentExras;
    }

    public static final Creator<ApplicationActivityBasic> CREATOR = new Creator<ApplicationActivityBasic>() {
        @Override
        public ApplicationActivityBasic createFromParcel(Parcel in) {
            return new ApplicationActivityBasic(TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in), in.readString(), in.readString(), in.readString());
        }

        @Override
        public ApplicationActivityBasic[] newArray(int size) {
            return new ApplicationActivityBasic[size];
        }
    };

    public String getActivityName() {
        return this.activityName;
    }

    public String getIntentExtras() {
        return this.intentExtras;
    }

    public CharSequence getName() {
        return this.name;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public void setIntentExtras(String intentExtras) {
        this.intentExtras = intentExtras;
    }

    public void setName(CharSequence charSequence) {
        this.name = charSequence;
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
        TextUtils.writeToParcel(name, parcel, flags);
        parcel.writeString(packageName);
        parcel.writeString(activityName);
        parcel.writeString(intentExtras);
    }

    @Override
    public @NonNull String toString() {
        return "ApplicationActivityBasic{" +
                "activityName='" + activityName + '\'' +
                ", intentExtras='" + intentExtras + '\'' +
                ", name=" + name +
                ", packageName='" + packageName + '\'' +
                '}';
    }
}
