package ai.saiy.android.custom.exports;

import com.google.gson.annotations.SerializedName;

import ai.saiy.android.custom.Custom;

public class ExportConfiguration {
    public static final double VERSION_1 = 1.0d;

    @SerializedName("custom")
    private final Custom custom;
    @SerializedName("timestamp")
    private final long timestamp;
    @SerializedName("version")
    private final double version;

    public ExportConfiguration(Custom custom, double version, long timeStamp) {
        this.custom = custom;
        this.version = version;
        this.timestamp = timeStamp;
    }

    public Custom getCustom() {
        return this.custom;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public double getVersion() {
        return this.version;
    }
}
