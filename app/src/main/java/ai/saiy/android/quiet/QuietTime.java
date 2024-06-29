package ai.saiy.android.quiet;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class QuietTime implements Parcelable {
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;

    public QuietTime(int startHour, int startMinute, int endHour, int endMinute) {
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.startHour = startHour;
        this.startMinute = startMinute;
    }

    public static final Creator<QuietTime> CREATOR = new Creator<QuietTime>() {
        @Override
        public QuietTime createFromParcel(Parcel in) {
            return new QuietTime(in.readInt(), in.readInt(), in.readInt(), in.readInt());
        }

        @Override
        public QuietTime[] newArray(int size) {
            return new QuietTime[size];
        }
    };

    public int getEndHour() {
        return this.endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return this.endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public int getStartHour() {
        return this.startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return this.startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(startHour);
        parcel.writeInt(startMinute);
        parcel.writeInt(endHour);
        parcel.writeInt(endMinute);
    }

    @Override
    public @NonNull String toString() {
        return "QuietTime{" +
                "startHour=" + startHour +
                ", startMinute=" + startMinute +
                ", endHour=" + endHour +
                ", endMinute=" + endMinute +
                '}';
    }
}
