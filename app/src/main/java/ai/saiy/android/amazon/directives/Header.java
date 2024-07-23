package ai.saiy.android.amazon.directives;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Header implements Parcelable {
    @SerializedName("namespace")
    private final String namespace;

    @SerializedName("name")
    private final String name;

    @SerializedName("messageId")
    private final String messageId;

    @SerializedName("dialogRequestId")
    private final String dialogRequestId;

    public Header(String namespace, String name, String messageId, String dialogRequestId) {
        this.namespace = namespace;
        this.name = name;
        this.messageId = messageId;
        this.dialogRequestId = dialogRequestId;
    }

    public static final Creator<Header> CREATOR = new Creator<Header>() {
        @Override
        public Header createFromParcel(Parcel in) {
            return new Header(in.readString(), in.readString(), in.readString(), in.readString());
        }

        @Override
        public Header[] newArray(int size) {
            return new Header[size];
        }
    };

    public String getName() {
        return this.name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(namespace);
        parcel.writeString(name);
        parcel.writeString(messageId);
        parcel.writeString(dialogRequestId);
    }
}
