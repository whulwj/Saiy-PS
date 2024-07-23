package ai.saiy.android.command.time.online.model;

public class LocalTime {
    @com.google.gson.annotations.SerializedName("localtime")
    private final String localTime;

    public LocalTime(String localTime) {
        this.localTime = localTime;
    }

    public String getLocalTime() {
        return this.localTime;
    }
}
