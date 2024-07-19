package ai.saiy.android.command.twitter;

import android.os.Parcel;
import android.os.Parcelable;

public class CommandTwitterValues implements Parcelable {
    private TwitterConfirm.ContentType contentType;
    private long endIndex;
    private boolean isResolved;
    private int[][] ranges;
    private long startIndex;
    private String text;

    public static final Creator<CommandTwitterValues> CREATOR = new Creator<CommandTwitterValues>() {
        @Override
        public CommandTwitterValues createFromParcel(Parcel in) {
            final CommandTwitterValues commandTwitterValues = new CommandTwitterValues();
            final byte index = in.readByte();
            for (TwitterConfirm.ContentType contentType: TwitterConfirm.ContentType.values()) {
                if (index == contentType.ordinal()) {
                    commandTwitterValues.contentType = contentType;
                    break;
                }
            }
            commandTwitterValues.isResolved = (in.readByte() != 0);
            final int size = in.readInt();
            if (size >= 0) {
                commandTwitterValues.ranges = new int[size][];
                for (int i = 0; i < size; ++i) {
                    commandTwitterValues.ranges[i] = in.createIntArray();
                }
            }
            commandTwitterValues.startIndex = in.readLong();
            commandTwitterValues.endIndex = in.readLong();
            commandTwitterValues.text = in.readString();
            return commandTwitterValues;
        }

        @Override
        public CommandTwitterValues[] newArray(int size) {
            return new CommandTwitterValues[size];
        }
    };

    public TwitterConfirm.ContentType getContentType() {
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

    public void setContentType(TwitterConfirm.ContentType contentType) {
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
