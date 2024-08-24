package ai.saiy.android.command.facebook;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class CommandFacebookValues implements Parcelable {
    @SerializedName("contentType")
    private FacebookConfirm.ContentType contentType;
    @SerializedName("endIndex")
    private long endIndex;
    @SerializedName("isResolved")
    private boolean isResolved;
    @SerializedName("ranges")
    private int[][] ranges;
    @SerializedName("startIndex")
    private long startIndex;
    @SerializedName("text")
    private String text;

    public static final Creator<CommandFacebookValues> CREATOR = new Creator<CommandFacebookValues>() {
        @Override
        public CommandFacebookValues createFromParcel(Parcel in) {
            final CommandFacebookValues commandFacebookValues = new CommandFacebookValues();
            final byte index = in.readByte();
            for (FacebookConfirm.ContentType contentType: FacebookConfirm.ContentType.values()) {
                if (index == contentType.ordinal()) {
                    commandFacebookValues.contentType = contentType;
                    break;
                }
            }
            commandFacebookValues.isResolved = (in.readByte() != 0);
            final int size = in.readInt();
            if (size >= 0) {
                commandFacebookValues.ranges = new int[size][];
                for (int i = 0; i < size; ++i) {
                    commandFacebookValues.ranges[i] = in.createIntArray();
                }
            }
            commandFacebookValues.startIndex = in.readLong();
            commandFacebookValues.endIndex = in.readLong();
            commandFacebookValues.text = in.readString();
            return commandFacebookValues;
        }

        @Override
        public CommandFacebookValues[] newArray(int size) {
            return new CommandFacebookValues[size];
        }
    };

    public FacebookConfirm.ContentType getContentType() {
        return this.contentType;
    }

    public long getEndIndex() {
        return this.endIndex;
    }

    public int[][] getRanges() {
        return this.ranges;
    }

    public long getStartIndex() {
        return this.startIndex;
    }

    public String getText() {
        return this.text;
    }

    public boolean isResolved() {
        return this.isResolved;
    }

    public void setContentType(FacebookConfirm.ContentType contentType) {
        this.contentType = contentType;
    }

    public void setEndIndex(long index) {
        this.endIndex = index;
    }

    public void setRanges(int[][] ranges) {
        this.ranges = ranges;
    }

    public void setResolved(boolean resolved) {
        this.isResolved = resolved;
    }

    public void setStartIndex(long index) {
        this.startIndex = index;
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
        parcel.writeByte((byte) contentType.ordinal());
        parcel.writeByte((byte) (isResolved? 1:0));
        final int size = ranges == null? -1 : ranges.length;
        parcel.writeInt(size);
        for (int i = 0; i < size; ++i) {
            parcel.writeIntArray(ranges[i]);
        }
        parcel.writeLong(startIndex);
        parcel.writeLong(endIndex);
        parcel.writeString(text);
    }
}
