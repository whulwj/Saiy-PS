package ai.saiy.android.amazon.resolve;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Pair;

import java.io.File;
import java.util.ArrayList;

import ai.saiy.android.amazon.directives.DirectiveType;
import ai.saiy.android.amazon.directives.StructuralDirective;

public class DirectiveList implements Parcelable {
    static final int ACTION_DEFAULT = 1;
    static final int ACTION_EXPECT_SPEECH = 2;

    static final int MISSING_BYTES = 7;
    static final int RESPONSE_EXCEPTION = 5;
    static final int NO_AUDIO_OR_DIRECTIVES = 1;

    private ArrayList<StructuralDirective> directiveList;
    private ArrayList<Pair<String, byte[]>> multiPartList;
    private File file;

    private int errorCode = 0;
    private int action = ACTION_DEFAULT;

    public static final Creator<DirectiveList> CREATOR = new Creator<DirectiveList>() {
        @Override
        public DirectiveList createFromParcel(Parcel in) {
            final DirectiveList directiveList = new DirectiveList();
            ArrayList<StructuralDirective> structuralDirectives;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                structuralDirectives = new ArrayList<>();
                in.readParcelableList(structuralDirectives, StructuralDirective.class.getClassLoader());
            } else {
                structuralDirectives = in.createTypedArrayList(StructuralDirective.CREATOR);
            }
            directiveList.setDirectiveList(structuralDirectives);
            final int size = in.readInt();
            if (size >= 0) {
                directiveList.multiPartList = new ArrayList<>();
                Pair<String, byte[]> part;
                for (int i = 0; i < size; ++i) {
                    part = new Pair<>(in.readString(), in.createByteArray());
                    directiveList.multiPartList.add(part);
                }
            }
            final String pathOfFile = in.readString();
            directiveList.file = (pathOfFile == null || TextUtils.isEmpty(pathOfFile))? null : new File(pathOfFile);
            directiveList.errorCode = in.readInt();
            directiveList.action = in.readInt();
            return directiveList;
        }

        @Override
        public DirectiveList[] newArray(int size) {
            return new DirectiveList[size];
        }
    };

    public int getAction() {
        return this.action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setDirectiveList(ArrayList<StructuralDirective> arrayList) {
        this.directiveList = arrayList;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(int code) {
        this.errorCode = code;
    }

    public void setMultiPartList(ArrayList<Pair<String, byte[]>> arrayList) {
        this.multiPartList = arrayList;
    }

    public File getFile() {
        return this.file;
    }

    public boolean hasDirectiveType() {
        return getDirectiveType() != DirectiveType.DIRECTIVE_NONE;
    }

    public DirectiveType getDirectiveType() {
        for (StructuralDirective next : this.directiveList) {
            if (next.getDirectiveType() != DirectiveType.DIRECTIVE_NONE) {
                return next.getDirectiveType();
            }
        }
        return DirectiveType.DIRECTIVE_NONE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.writeParcelableList(directiveList, flags);
        } else {
            parcel.writeTypedList(directiveList);
        }
        parcel.writeInt(multiPartList == null? -1 : multiPartList.size());
        if (multiPartList != null) {
            final int size = multiPartList.size();
            Pair<String, byte[]> part;
            for (int i = 0; i < size; ++i) {
                part = multiPartList.get(i);
                parcel.writeString(part.first);
                parcel.writeByteArray(part.second);
            }
        }
        parcel.writeString(file == null? null : file.getAbsolutePath());
        parcel.writeInt(errorCode);
        parcel.writeInt(action);
    }
}
