package ai.saiy.android.command.http;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CustomHttp implements Serializable, Parcelable {
    public static final int ERROR_NONE = 0;
    public static final int ERROR_SPEAK = 1;
    public static final int ERROR_SPEAK_LISTEN = 2;
    public static final int ERROR_SPEAK_LISTEN_OUTPUT = 4;
    public static final int ERROR_SPEAK_OUTPUT = 3;
    @IntDef({ERROR_NONE, ERROR_SPEAK, ERROR_SPEAK_LISTEN, ERROR_SPEAK_OUTPUT, ERROR_SPEAK_LISTEN_OUTPUT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Error {}
    public static final int OUTPUT_TYPE_BYTE_ARRAY = 2;
    public static final int OUTPUT_TYPE_NONE = 0;
    public static final int OUTPUT_TYPE_STRING = 1;
    @IntDef({OUTPUT_TYPE_NONE, OUTPUT_TYPE_STRING, OUTPUT_TYPE_BYTE_ARRAY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OutputType {}
    public static final int SUCCESS_NONE = 0;
    public static final int SUCCESS_SPEAK = 1;
    public static final int SUCCESS_SPEAK_LISTEN = 2;
    public static final int SUCCESS_SPEAK_LISTEN_OUTPUT = 4;
    public static final int SUCCESS_SPEAK_OUTPUT = 3;
    @IntDef({SUCCESS_NONE, SUCCESS_SPEAK, SUCCESS_SPEAK_LISTEN, SUCCESS_SPEAK_OUTPUT, SUCCESS_SPEAK_LISTEN_OUTPUT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Success {}
    public static final int TYPE_DELETE = 3;
    public static final int TYPE_GET = 1;
    public static final int TYPE_POST = 0;
    public static final int TYPE_PUT = 2;
    @IntDef({TYPE_POST, TYPE_GET, TYPE_PUT, TYPE_DELETE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}
    private static final long serialVersionUID = 2221570297080158471L;

    private @Error int errorHandling;
    private boolean https;
    private int outputType;
    private String serialised;
    private @Success int successHandling;
    private boolean tasker;
    private String taskerTaskName;
    private String taskerVariableName;
    private @Type int type;
    private String urlString;

    public CustomHttp() {
    }

    public CustomHttp(String urlString, boolean isHttps, @Type int type, @OutputType int outputType, @Success int successHandling, @Error int errorHandling) {
        this.urlString = urlString;
        this.https = isHttps;
        this.type = type;
        this.outputType = outputType;
        this.successHandling = successHandling;
        this.errorHandling = errorHandling;
    }

    public static final Creator<CustomHttp> CREATOR = new Creator<CustomHttp>() {
        @Override
        public CustomHttp createFromParcel(Parcel in) {
            final CustomHttp customHttp = new CustomHttp(in.readString(), in.readByte() != 0, in.readInt(), in.readInt(),
                    in.readInt(), in.readInt());
            customHttp.serialised = in.readString();
            customHttp.tasker = in.readByte() != 0;
            customHttp.taskerTaskName = in.readString();
            customHttp.taskerVariableName = in.readString();
            return customHttp;
        }

        @Override
        public CustomHttp[] newArray(int size) {
            return new CustomHttp[size];
        }
    };

    public @Error int getErrorHandling() {
        return this.errorHandling;
    }

    public @OutputType int getOutputType() {
        return this.outputType;
    }

    public String getSerialised() {
        return this.serialised != null ? this.serialised : "";
    }

    public @Success int getSuccessHandling() {
        return this.successHandling;
    }

    public String getTaskerTaskName() {
        return this.taskerTaskName;
    }

    public String getTaskerVariableName() {
        return this.taskerVariableName;
    }

    public @Type int getType() {
        return this.type;
    }

    public String getUrlString() {
        return this.urlString;
    }

    public boolean isHttps() {
        return this.https;
    }

    public boolean isTasker() {
        return this.tasker;
    }

    public void setErrorHandling(@Error int error) {
        this.errorHandling = error;
    }

    public void setHttps(boolean isHttps) {
        this.https = isHttps;
    }

    public void setOutputType(@OutputType int type) {
        this.outputType = type;
    }

    public void setSerialised(String str) {
        this.serialised = str;
    }

    public void setSuccessHandling(@Success int success) {
        this.successHandling = success;
    }

    public void setTasker(boolean isTasker) {
        this.tasker = isTasker;
    }

    public void setTaskerTaskName(String str) {
        this.taskerTaskName = str;
    }

    public void setTaskerVariableName(String str) {
        this.taskerVariableName = str;
    }

    public void setType(@Type int type) {
        this.type = type;
    }

    public void setUrlString(String str) {
        this.urlString = str;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(urlString);
        dest.writeByte((byte) (https? 1 : 0));
        dest.writeInt(type);
        dest.writeInt(outputType);
        dest.writeInt(successHandling);
        dest.writeInt(errorHandling);
        dest.writeString(serialised);
        dest.writeByte((byte) (tasker? 1 : 0));
        dest.writeString(taskerTaskName);
        dest.writeString(taskerVariableName);
    }
}
