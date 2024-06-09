package ai.saiy.android.amazon.resolve;

import android.util.Pair;

import java.io.File;
import java.util.ArrayList;

import ai.saiy.android.amazon.directives.DirectiveType;
import ai.saiy.android.amazon.directives.StructuralDirective;

public class DirectiveList {
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
}
