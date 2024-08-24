package ai.saiy.android.command.time.online.model;

import com.google.gson.annotations.SerializedName;

public class LocalTime {
    @SerializedName("localtime")
    private final String localTime;

    public LocalTime(String localTime) {
        this.localTime = localTime;
    }

    public String getLocalTime() {
        return this.localTime;
    }
}
