package ai.saiy.android.command.driving;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class DrivingProfile implements Parcelable {
    private boolean startHotword;
    private boolean announceNotifications;
    private boolean announceCallerId;
    private boolean startAutomatically;
    private boolean stopAutomatically;
    private boolean enabled;

    public DrivingProfile(boolean startHotword, boolean announceNotifications, boolean announceCallerId, boolean startAutomatically, boolean stopAutomatically, boolean enabled) {
        this.announceCallerId = announceCallerId;
        this.announceNotifications = announceNotifications;
        this.startHotword = startHotword;
        this.startAutomatically = startAutomatically;
        this.stopAutomatically = stopAutomatically;
        this.enabled = enabled;
    }

    public static final Creator<DrivingProfile> CREATOR = new Creator<DrivingProfile>() {
        @Override
        public DrivingProfile createFromParcel(Parcel in) {
            return new DrivingProfile(in.readByte() != 0, in.readByte() != 0, in.readByte() != 0, in.readByte() != 0, in.readByte() != 0, in.readByte() != 0);
        }

        @Override
        public DrivingProfile[] newArray(int size) {
            return new DrivingProfile[size];
        }
    };

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setStartAutomatically(boolean startAutomatically) {
        this.startAutomatically = startAutomatically;
    }

    public boolean shouldStartAutomatically() {
        return this.startAutomatically;
    }

    public void setStopAutomatically(boolean stopAutomatically) {
        this.stopAutomatically = stopAutomatically;
    }

    public boolean shouldStopAutomatically() {
        return this.stopAutomatically;
    }

    public void setAnnounceCallerId(boolean condition) {
        this.announceCallerId = condition;
    }

    public boolean getAnnounceCallerId() {
        return this.announceCallerId;
    }

    public void setAnnounceNotifications(boolean condition) {
        this.announceNotifications = condition;
    }

    public boolean getAnnounceNotifications() {
        return this.announceNotifications;
    }

    public void setStartHotword(boolean condition) {
        this.startHotword = condition;
    }

    public boolean getStartHotword() {
        return this.startHotword;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeByte(startHotword ? (byte) 1 : (byte) 0);
        parcel.writeByte(announceNotifications ? (byte) 1 : (byte) 0);
        parcel.writeByte(announceCallerId ? (byte) 1 : (byte) 0);
        parcel.writeByte(startAutomatically ? (byte) 1 : (byte) 0);
        parcel.writeByte(stopAutomatically ? (byte) 1 : (byte) 0);
        parcel.writeByte(enabled ? (byte) 1 : (byte) 0);
    }

    @Override
    public @NonNull String toString() {
        return "DrivingProfile{" +
                "startHotword=" + startHotword +
                ", announceNotifications=" + announceNotifications +
                ", announceCallerId=" + announceCallerId +
                ", startAutomatically=" + startAutomatically +
                ", stopAutomatically=" + stopAutomatically +
                ", enabled=" + enabled +
                '}';
    }
}
