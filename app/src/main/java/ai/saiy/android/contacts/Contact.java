package ai.saiy.android.contacts;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Contact implements Parcelable {
    @SerializedName("id")
    private final String id;
    @SerializedName("name")
    private final String name;
    @SerializedName("phoneticName")
    private final String phoneticName;
    @SerializedName("forename")
    private final String forename;
    @SerializedName("surname")
    private final String surname;
    @SerializedName("wordCount")
    private final int wordCount;
    @SerializedName("frequency")
    private final int frequency;
    @SerializedName("hasPhoneNumber")
    private boolean hasPhoneNumber;
    @SerializedName("hasAddress")
    private boolean hasAddress;
    @SerializedName("hasEmail")
    private boolean hasEmail;
    @SerializedName("number")
    private String number;
    @SerializedName("address")
    private String address;
    @SerializedName("emailAddress")
    private String emailAddress;
    @SerializedName("generic")
    private String generic;

    public enum Weighting {
        NONE,
        NAME,
        NUMBER,
        ADDRESS,
        EMAIL,
        IM
    }

    public Contact(String contactID, String name, String phoneticName, String forename, String surname, int wordCountOfName, int frequency, boolean hasPhoneNumber, boolean hasAddress, boolean hasEmail) {
        this.id = contactID;
        this.name = name;
        this.phoneticName = phoneticName;
        this.forename = forename;
        this.surname = surname;
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
                    in.readString(), in.readString(), in.readInt(), in.readInt(),
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
