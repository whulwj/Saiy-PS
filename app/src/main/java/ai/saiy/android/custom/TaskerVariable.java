package ai.saiy.android.custom;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class TaskerVariable implements Parcelable, Serializable {
    private static final long serialVersionUID = -8596910894134114597L;
    private long rowId;
    private String serialised;
    private String variableName;
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

    public TaskerVariable(String name, String value, long id, String serialised) {
        this.variableName = name;
        this.variableValue = value;
        this.rowId = id;
        this.serialised = serialised;
    }

    public static final Creator<TaskerVariable> CREATOR = new Creator<TaskerVariable>() {
        @Override
        public TaskerVariable createFromParcel(Parcel in) {
            return new TaskerVariable(in.readString(), in.readString(), in.readLong(), in.readString());
        }

        @Override
        public TaskerVariable[] newArray(int size) {
            return new TaskerVariable[size];
        }
    };

    public long getRowId() {
        return this.rowId;
    }

    public String getSerialised() {
        return this.serialised != null ? this.serialised : "";
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

    public void setSerialised(String serialised) {
        this.serialised = serialised;
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
        dest.writeString(serialised);
    }
}
