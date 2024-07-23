package ai.saiy.android.command.time.online.model;

public class TimeResponse {
    @com.google.gson.annotations.SerializedName("data")
    private final Data data;

    public TimeResponse(Data data) {
        this.data = data;
    }

    public Data getData() {
        return this.data;
    }
}
