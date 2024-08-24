package ai.saiy.android.command.http;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CustomHttp implements Parcelable {
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

    @SerializedName("errorHandling")
    private @Error int errorHandling;
    @SerializedName("https")
    private boolean https;
    @SerializedName("outputType")
    private int outputType;
    @SerializedName("successHandling")
    private @Success int successHandling;
    @SerializedName("tasker")
    private boolean tasker;
    @SerializedName("taskerTaskName")
    private String taskerTaskName;
    @SerializedName("taskerVariableName")
    private String taskerVariableName;
    @SerializedName("type")
    private @Type int type;
    @SerializedName("urlString")
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
        dest.writeByte((byte) (tasker? 1 : 0));
        dest.writeString(taskerTaskName);
        dest.writeString(taskerVariableName);
    }
}
