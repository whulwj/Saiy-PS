package ai.saiy.android.custom;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class TaskerVariable implements Parcelable {
    @SerializedName("rowId")
    private long rowId;
    @SerializedName("name")
    private String variableName;
    @SerializedName("value")
    private String variableValue;

    public TaskerVariable(String name, String value) {
        this.variableName = name;
        this.variableValue = value;
    }

    public TaskerVariable(String name, String value, long id) {
        this.variableName = name;
        this.variableValue = value;
        this.rowId = id;
    }

    public static final Creator<TaskerVariable> CREATOR = new Creator<TaskerVariable>() {
        @Override
        public TaskerVariable createFromParcel(Parcel in) {
            return new TaskerVariable(in.readString(), in.readString(), in.readLong());
        }

        @Override
        public TaskerVariable[] newArray(int size) {
            return new TaskerVariable[size];
        }
    };

    public long getRowId() {
        return this.rowId;
    }

    public String getVariableName() {
        return this.variableName;
    }

    public String getVariableValue() {
        return this.variableValue;
    }

    public void setRowId(long id) {
        this.rowId = id;
    }

    public void setVariableName(String name) {
        this.variableName = name;
    }

    public void setVariableValue(String value) {
        this.variableValue = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(variableName);
        dest.writeString(variableValue);
        dest.writeLong(rowId);
    }
}
