package ai.saiy.android.command.note;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class NoteValues implements Parcelable {
    @SerializedName("contentType")
    private NoteManager.ContentType contentType;
    @SerializedName("body")
    private String noteBody;
    @SerializedName("title")
    private String noteTitle;

    public static final Creator<NoteValues> CREATOR = new Creator<NoteValues>() {
        @Override
        public NoteValues createFromParcel(Parcel in) {
            final NoteValues noteValues = new NoteValues();
            final byte index = in.readByte();
            for (NoteManager.ContentType contentType: NoteManager.ContentType.values()) {
                if (index == contentType.ordinal()) {
                    noteValues.contentType = contentType;
                    break;
                }
            }
            noteValues.noteBody = in.readString();
            noteValues.noteTitle = in.readString();
            return noteValues;
        }

        @Override
        public NoteValues[] newArray(int size) {
            return new NoteValues[size];
        }
    };

    public NoteManager.ContentType getContentType() {
        return this.contentType;
    }

    public String getNoteBody() {
        return this.noteBody;
    }

    public String getNoteTitle() {
        return this.noteTitle;
    }

    public void setContentType(NoteManager.ContentType contentType) {
        this.contentType = contentType;
    }

    public void setNoteBody(String str) {
        this.noteBody = str;
    }

    public void setNoteTitle(String str) {
        this.noteTitle = str;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeByte((byte) contentType.ordinal());
        parcel.writeString(noteTitle);
        parcel.writeString(noteBody);
    }
}
