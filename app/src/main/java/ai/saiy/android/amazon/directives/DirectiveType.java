package ai.saiy.android.amazon.directives;

import android.os.Parcel;
import android.os.Parcelable;

public enum DirectiveType implements Parcelable {
    DIRECTIVE_NONE,
    DIRECTIVE_CANCEL,
    DIRECTIVE_ABANDON,
    DIRECTIVE_VOLUME,
    DIRECTIVE_MEDIA;

    public static final Creator<DirectiveType> CREATOR = new Creator<DirectiveType>() {
        @Override
        public DirectiveType createFromParcel(Parcel in) {
            final byte index = in.readByte();
            for (DirectiveType directiveType: DirectiveType.values()) {
                if (index == directiveType.ordinal()) {
                    return directiveType;
                }
            }
            return DIRECTIVE_NONE;
        }

        @Override
        public DirectiveType[] newArray(int size) {
            return new DirectiveType[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeByte((byte) ordinal());
    }
}
