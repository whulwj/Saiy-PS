package ai.saiy.android.amazon.directives;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Directive implements Parcelable {
    @SerializedName("header")
    private final Header header;
    @SerializedName("payload")
    private final Payload payload;

    public Directive(Header header, Payload payload) {
        this.header = header;
        this.payload = payload;
    }

    public static final Creator<Directive> CREATOR = new Creator<Directive>() {
        @Override
        public Directive createFromParcel(Parcel in) {
            return new Directive(in.readParcelable(Header.class.getClassLoader()), in.readParcelable(Payload.class.getClassLoader()));
        }

        @Override
        public Directive[] newArray(int size) {
            return new Directive[size];
        }
    };

    public Header getHeader() {
        return this.header;
    }

    public Payload getPayload() {
        return this.payload;
    }

    public boolean isExpectSpeech() {
        return header.getName().matches("ExpectSpeech");
    }

    public boolean isDirectiveVolume() {
        return header.getName().matches("AdjustVolume") || header.getName().matches("SetVolume") || header.getName().matches("SetMute");
    }

    public boolean isDirectiveMedia() {
        return header.getName().matches("PlayCommandIssued") || header.getName().matches("PauseCommandIssued") || header.getName().matches("NextCommandIssued") || header.getName().matches("PreviousCommandIssue");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(header, flags);
        parcel.writeParcelable(payload, flags);
    }
}
