package ai.saiy.android.accessibility;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.saiy.android.applications.ApplicationBasic;

public class BlockedApplications implements Parcelable {
    private ArrayList<ApplicationBasic> applicationArray;
    private String text;

    public BlockedApplications(ArrayList<ApplicationBasic> arrayList) {
        this.applicationArray = arrayList;
    }

    public BlockedApplications(ArrayList<ApplicationBasic> arrayList, String text) {
        this.applicationArray = arrayList;
        this.text = text;
    }

    public static final Creator<BlockedApplications> CREATOR = new Creator<BlockedApplications>() {
        @Override
        public BlockedApplications createFromParcel(Parcel in) {
            ArrayList<ApplicationBasic> applicationArray = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                applicationArray = new ArrayList<>();
                in.readParcelableList(applicationArray, BlockedApplications.class.getClassLoader());
            } else {
                final int size = in.readInt();
                if (size >= 0) {
                    applicationArray = new ArrayList<>(size);
                    for (int i = 0; i < size; ++i) {
                        applicationArray.add(in.readParcelable(ApplicationBasic.class.getClassLoader()));
                    }
                }
            }
            return new BlockedApplications(applicationArray, in.readString());
        }

        @Override
        public BlockedApplications[] newArray(int size) {
            return new BlockedApplications[size];
        }
    };

    public List<ApplicationBasic> getApplicationArray() {
        return this.applicationArray != null ? this.applicationArray : Collections.emptyList();
    }

    public String getText() {
        return this.text;
    }

    public void setApplicationArray(ArrayList<ApplicationBasic> arrayList) {
        this.applicationArray = arrayList;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.writeParcelableList(applicationArray, flags);
        } else {
            parcel.writeInt(applicationArray == null ? -1 : applicationArray.size());
            if (applicationArray != null) {
                for (ApplicationBasic applicationBasic : applicationArray) {
                    parcel.writeParcelable(applicationBasic, flags);
                }
            }
        }
        parcel.writeString(text);
    }

    @Override
    public @NonNull String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("BlockedApplications{applicationArray=");
        if (applicationArray == null || applicationArray.isEmpty()) {
            stringBuilder.append("[]");
        } else {
            stringBuilder.append("[");
            for (int i = 0, size = applicationArray.size(); i < size; ++i) {
                stringBuilder.append(applicationArray.get(i));
                if (i < (size - 1)) {
                    stringBuilder.append(", ");
                }
            }
            stringBuilder.append("]");
        }
        stringBuilder.append(", text='");
        stringBuilder.append(text);
        stringBuilder.append('\'');
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
