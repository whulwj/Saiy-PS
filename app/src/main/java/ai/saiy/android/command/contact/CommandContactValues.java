package ai.saiy.android.command.contact;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.contacts.Contact;

public class CommandContactValues implements Parcelable {
    private String actionUtterance;
    private ArrayList<Choice> choiceArray;
    private String confirmationUtterance;
    private Contact contact;
    private String requiredNumber;
    private ArrayList<String> voiceData;
    private ArrayList<String> voiceDataTrimmed;
    private Type type = Type.UNKNOWN;
    private CallType callType = CallType.UNKNOWN;
    private IMType imType = IMType.UNKNOWN;
    private NavigationType navigationType = NavigationType.UNKNOWN;
    private ContactConfirm.ConfirmType confirmType = ContactConfirm.ConfirmType.CALL_CONFIRM;

    public enum CallType {
        UNKNOWN,
        HOME,
        WORK,
        MOBILE,
        SKYPE,
        VIBER,
        HANGOUT,
        DUO,
        NUMBER
    }

    public enum IMType {
        UNKNOWN,
        HANGOUT,
        WHATS_APP,
        SKYPE,
        VIBER,
        TELEGRAM
    }

    public enum NavigationType {
        UNKNOWN,
        HOME,
        WORK
    }

    public enum Type {
        UNKNOWN,
        DISPLAY,
        EDIT,
        CALL,
        NAVIGATE,
        TEXT,
        EMAIL,
        IM
    }

    public static final Creator<CommandContactValues> CREATOR = new Creator<CommandContactValues>() {
        @Override
        public CommandContactValues createFromParcel(Parcel in) {
            final CommandContactValues commandContactValues = new CommandContactValues();
            commandContactValues.actionUtterance = in.readString();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                commandContactValues.choiceArray = new ArrayList<>();
                in.readParcelableList(commandContactValues.choiceArray, Choice.class.getClassLoader());
            } else {
                commandContactValues.choiceArray = in.createTypedArrayList(Choice.CREATOR);
            }
            commandContactValues.confirmationUtterance = in.readString();
            commandContactValues.contact = in.readParcelable(Contact.class.getClassLoader());
            commandContactValues.requiredNumber = in.readString();
            commandContactValues.voiceData = in.createStringArrayList();
            commandContactValues.voiceDataTrimmed = in.createStringArrayList();
            byte index = in.readByte();
            for (Type type: Type.values()) {
                if (type.ordinal() == index) {
                    commandContactValues.type = type;
                    break;
                }
            }
            index = in.readByte();
            for (CallType callType: CallType.values()) {
                if (callType.ordinal() == index) {
                    commandContactValues.callType = callType;
                    break;
                }
            }
            index = in.readByte();
            for (IMType imType: IMType.values()) {
                if (imType.ordinal() == index) {
                    commandContactValues.imType = imType;
                    break;
                }
            }
            index = in.readByte();
            for (NavigationType navigationType: NavigationType.values()) {
                if (navigationType.ordinal() == index) {
                    commandContactValues.navigationType = navigationType;
                    break;
                }
            }
            index = in.readByte();
            for (ContactConfirm.ConfirmType confirmType: ContactConfirm.ConfirmType.values()) {
                if (confirmType.ordinal() == index) {
                    commandContactValues.confirmType = confirmType;
                    break;
                }
            }
            return commandContactValues;
        }

        @Override
        public CommandContactValues[] newArray(int size) {
            return new CommandContactValues[size];
        }
    };

    public String getActionUtterance() {
        return this.actionUtterance;
    }

    public CallType getCallType() {
        return this.callType;
    }

    public ArrayList<Choice> getChoiceArray() {
        return this.choiceArray;
    }

    public ContactConfirm.ConfirmType getConfirmType() {
        return this.confirmType;
    }

    public String getConfirmationUtterance() {
        return this.confirmationUtterance;
    }

    public Contact getContact() {
        return this.contact;
    }

    public IMType getIMType() {
        return this.imType;
    }

    public NavigationType getNavigationType() {
        return this.navigationType;
    }

    public String getRequiredNumber() {
        return this.requiredNumber;
    }

    public @NonNull Type getType() {
        return this.type;
    }

    public ArrayList<String> getVoiceData() {
        return this.voiceData;
    }

    public ArrayList<String> getVoiceDataTrimmed() {
        return this.voiceDataTrimmed;
    }

    public void setActionUtterance(String str) {
        this.actionUtterance = str;
    }

    public void setCallType(CallType callType) {
        this.callType = callType;
    }

    public void setChoiceArray(ArrayList<Choice> arrayList) {
        this.choiceArray = arrayList;
    }

    public void setConfirmType(ContactConfirm.ConfirmType confirmType) {
        this.confirmType = confirmType;
    }

    public void setConfirmationUtterance(String str) {
        this.confirmationUtterance = str;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public void setIMType(IMType iMType) {
        this.imType = iMType;
    }

    public void setNavigationType(NavigationType navigationType) {
        this.navigationType = navigationType;
    }

    public void setRequiredNumber(String str) {
        this.requiredNumber = str;
    }

    public void setType(@NonNull Type type) {
        this.type = type;
    }

    public void setVoiceData(ArrayList<String> arrayList) {
        this.voiceData = arrayList;
    }

    public void setVoiceDataTrimmed(ArrayList<String> arrayList) {
        this.voiceDataTrimmed = arrayList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(actionUtterance);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.writeParcelableList(choiceArray, flags);
        } else {
            parcel.writeTypedList(choiceArray);
        }
        parcel.writeString(confirmationUtterance);
        parcel.writeParcelable(contact, flags);
        parcel.writeString(requiredNumber);
        parcel.writeStringList(voiceData);
        parcel.writeStringList(voiceDataTrimmed);
        parcel.writeByte((byte) type.ordinal());
        parcel.writeByte((byte) callType.ordinal());
        parcel.writeByte((byte) imType.ordinal());
        parcel.writeByte((byte) navigationType.ordinal());
        parcel.writeByte((byte) confirmType.ordinal());
    }
}
