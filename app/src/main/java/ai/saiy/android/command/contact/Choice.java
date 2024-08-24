package ai.saiy.android.command.contact;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;

import androidx.annotation.IntDef;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;

public class Choice implements Parcelable {
    protected static final int MOBILE = 1;
    protected static final int HOME = 2;
    protected static final int WORK = 3;
    protected static final int MOBILE_OR_HOME = 4;
    protected static final int MOBILE_OR_WORK = 5;
    protected static final int MOBILE_OR_HOME_OR_WORK = 6;
    protected static final int HOME_OR_WORK = 7;
    @IntDef({Choice.MOBILE, Choice.HOME, Choice.WORK, Choice.MOBILE_OR_HOME, Choice.MOBILE_OR_WORK, Choice.MOBILE_OR_HOME_OR_WORK, Choice.HOME_OR_WORK})
    @Retention(RetentionPolicy.SOURCE)
    protected @interface CombinedType {}

    @SerializedName("contactID")
    private final String contactID;
    @SerializedName("type")
    private final int type;
    @SerializedName("number")
    private final String number;
    @SerializedName("isPrimary")
    private final boolean isPrimary;

    public Choice(String contactID, int type, String number, boolean isPrimary) {
        this.contactID = contactID;
        this.type = type;
        this.number = number;
        this.isPrimary = isPrimary;
    }

    public static final Creator<Choice> CREATOR = new Creator<Choice>() {
        @Override
        public Choice createFromParcel(Parcel in) {
            return new Choice(in.readString(), in.readInt(), in.readString(), in.readByte() != 0);
        }

        @Override
        public Choice[] newArray(int size) {
            return new Choice[size];
        }
    };

    public static @CombinedType int getCombinedType(ArrayList<Choice> choices) {
        final HashSet<Integer> types = new HashSet<>(choices.size());
        for (Choice choice : choices) {
            types.add(choice.getType());
        }
        if (types.contains(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) && types.contains(ContactsContract.CommonDataKinds.Phone.TYPE_HOME) && types.contains(ContactsContract.CommonDataKinds.Phone.TYPE_WORK)) {
            return MOBILE_OR_HOME_OR_WORK;
        }
        if (types.contains(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) && types.contains(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)) {
            return MOBILE_OR_HOME;
        }
        if (types.contains(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) && types.contains(ContactsContract.CommonDataKinds.Phone.TYPE_WORK)) {
            return MOBILE_OR_WORK;
        }
        if (types.contains(ContactsContract.CommonDataKinds.Phone.TYPE_HOME) && types.contains(ContactsContract.CommonDataKinds.Phone.TYPE_WORK)) {
            return HOME_OR_WORK;
        }
        if (types.contains(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)) {
            return MOBILE;
        }
        if (types.contains(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)) {
            return HOME;
        }
        return types.contains(ContactsContract.CommonDataKinds.Phone.TYPE_WORK) ? WORK : MOBILE;
    }

    public boolean isPrimary() {
        return this.isPrimary;
    }

    public String getNumber() {
        return this.number;
    }

    public int getType() {
        return this.type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(contactID);
        parcel.writeInt(type);
        parcel.writeString(number);
        parcel.writeByte(isPrimary ? (byte) 1 : (byte) 0);
    }
}
