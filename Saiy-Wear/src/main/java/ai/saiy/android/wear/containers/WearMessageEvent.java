package ai.saiy.android.wear.containers;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class WearMessageEvent implements Parcelable {
    public static final byte EVENT_NONE = 0;
    public static final byte EVENT_SPEECH = 1;
    public static final byte EVENT_DISPLAY = 2;
    public static final byte EVENT_UPDATE = 3;
    @IntDef({EVENT_NONE, EVENT_DISPLAY, EVENT_SPEECH, EVENT_UPDATE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    @SerializedName("eventMessage")
    private final String eventMessage;

    @SerializedName("eventType")
    private final byte eventType;

    @SerializedName("eventLocale")
    private final String eventLocale;

    @SerializedName("eventUri")
    private final String eventUri;

    public WearMessageEvent(byte eventType, String eventMessage, String eventLocale, String eventUri) {
        this.eventType = eventType;
        this.eventMessage = eventMessage;
        this.eventLocale = eventLocale;
        this.eventUri = eventUri;
    }

    public static final Creator<WearMessageEvent> CREATOR = new Creator<WearMessageEvent>() {
        @Override
        public WearMessageEvent createFromParcel(Parcel in) {
            return new WearMessageEvent(in.readByte(), in.readString(), in.readString(), in.readString());
        }

        @Override
        public WearMessageEvent[] newArray(int size) {
            return new WearMessageEvent[size];
        }
    };

    public String getEventLocale() {
        return this.eventLocale;
    }

    public String getEventMessage() {
        return this.eventMessage;
    }

    public byte getEventType() {
        return this.eventType;
    }

    public String getEventUri() {
        return this.eventUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(eventType);
        dest.writeString(eventMessage);
        dest.writeString(eventLocale);
        dest.writeString(eventUri);
    }
}
