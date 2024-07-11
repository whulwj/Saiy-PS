package ai.saiy.android.contacts;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Contact implements Parcelable {
    private final String id;
    private final String name;
    private final String phoneticName;
    private final String forename;
    private final String surname;
    private String encoded;
    private final int wordCount;
    private final int frequency;
    private boolean hasPhoneNumber;
    private boolean hasAddress;
    private boolean hasEmail;
    private String number;
    private String address;
    private String emailAddress;
    private String generic;

    public enum Weighting {
        NONE,
        NAME,
        NUMBER,
        ADDRESS,
        EMAIL,
        IM
    }

    public Contact(String contactID, String name, String phoneticName, String forename, String surname, String encoded, int wordCountOfName, int frequency, boolean hasPhoneNumber, boolean hasAddress, boolean hasEmail) {
        this.id = contactID;
        this.name = name;
        this.phoneticName = phoneticName;
        this.forename = forename;
        this.surname = surname;
        this.encoded = encoded;
        this.wordCount = wordCountOfName;
        this.frequency = frequency;
        this.hasPhoneNumber = hasPhoneNumber;
        this.hasAddress = hasAddress;
        this.hasEmail = hasEmail;
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            final Contact contact = new Contact(in.readString(), in.readString(), in.readString(),
                    in.readString(), in.readString(), in.readString(), in.readInt(), in.readInt(),
                    in.readByte() != 0, in.readByte() != 0, in.readByte() != 0);
            contact.number = in.readString();
            contact.address = in.readString();
            contact.emailAddress = in.readString();
            contact.generic = in.readString();
            return contact;
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    public String getGeneric() {
        return this.generic;
    }

    public void setGeneric(String str) {
        this.generic = str;
    }

    public void setHasAddress(boolean condition) {
        this.hasAddress = condition;
    }

    public String getEmailAddress() {
        return this.emailAddress;
    }

    public void setEmailAddress(String str) {
        this.emailAddress = str;
    }

    public void setHasEmail(boolean condition) {
        this.hasEmail = condition;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String str) {
        this.address = str;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String str) {
        this.number = str;
    }

    public String getID() {
        return this.id;
    }

    public @NonNull String getForename() {
        return this.forename != null ? this.forename : "";
    }

    public String getName() {
        return this.name;
    }

    public @NonNull String getPhoneticName() {
        return this.phoneticName != null ? this.phoneticName : "";
    }

    public @NonNull String getSurname() {
        return this.surname != null ? this.surname : "";
    }

    public int getWordCount() {
        return this.wordCount;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public @NonNull String getEncoded() {
        return this.encoded != null ? this.encoded : "";
    }

    public boolean hasAddress() {
        return this.hasAddress;
    }

    public boolean hasEmail() {
        return this.hasEmail;
    }

    public boolean hasPhoneNumber() {
        return this.hasPhoneNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(phoneticName);
        parcel.writeString(forename);
        parcel.writeString(surname);
        parcel.writeString(encoded);
        parcel.writeInt(wordCount);
        parcel.writeInt(frequency);
        parcel.writeByte(hasPhoneNumber ? (byte) 1 : (byte) 0);
        parcel.writeByte(hasAddress ? (byte) 1 : (byte) 0);
        parcel.writeByte(hasEmail ? (byte) 1 : (byte) 0);
        parcel.writeString(number);
        parcel.writeString(address);
        parcel.writeString(emailAddress);
        parcel.writeString(generic);
    }
}
