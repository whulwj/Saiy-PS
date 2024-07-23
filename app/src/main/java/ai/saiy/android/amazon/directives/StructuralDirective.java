package ai.saiy.android.amazon.directives;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class StructuralDirective implements Parcelable {
    private DirectiveType directiveType;

    private String directiveParent;
    @SerializedName("directive")
    private final Directive directive;

    public StructuralDirective(Directive directive) {
        this.directive = directive;
    }

    public static final Creator<StructuralDirective> CREATOR = new Creator<StructuralDirective>() {
        @Override
        public StructuralDirective createFromParcel(Parcel in) {
            final StructuralDirective structuralDirective = new StructuralDirective(in.readParcelable(Directive.class.getClassLoader()));
            final byte index = in.readByte();
            for (DirectiveType directiveType: DirectiveType.values()) {
                if (index == directiveType.ordinal()) {
                    structuralDirective.directiveType = directiveType;
                    break;
                }
            }
            structuralDirective.directiveParent = in.readString();
            return structuralDirective;
        }

        @Override
        public StructuralDirective[] newArray(int size) {
            return new StructuralDirective[size];
        }
    };

    public Directive getDirective() {
        return this.directive;
    }

    public void setDirectiveType(DirectiveType directiveType) {
        this.directiveType = directiveType;
    }

    public void setDirectiveParent(String directiveParent) {
        this.directiveParent = directiveParent;
    }

    public String getDirectiveParent() {
        return this.directiveParent;
    }

    public DirectiveType getDirectiveType() {
        return this.directiveType != null ? this.directiveType : DirectiveType.DIRECTIVE_NONE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(directive, flags);
        parcel.writeByte((byte) directiveType.ordinal());
        parcel.writeString(directiveParent);
    }
}
