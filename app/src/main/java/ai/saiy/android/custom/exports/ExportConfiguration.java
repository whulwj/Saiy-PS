package ai.saiy.android.custom.exports;

import java.io.Serializable;

import ai.saiy.android.custom.Custom;

public class ExportConfiguration implements Serializable {
    public static final double VERSION_1 = 1.0d;
    private static final long serialVersionUID = -5244857537828479547L;
    private final Custom custom;
    private final long timestamp;
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
